package com.cinema.modules.catalog.controller;

import com.cinema.common.constant.RoleConstant;
import com.cinema.common.dto.ApiResponse;
import com.cinema.common.util.JsonUtil;
import com.cinema.model.Cinema;
import com.cinema.model.MaintenanceSchedule;
import com.cinema.model.Movie;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Showtime;
import com.cinema.model.User;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import com.cinema.modules.catalog.service.ShowtimeRepeatPlanner;
import com.cinema.modules.catalog.service.ShowtimeRepeatPlanner.Preview;
import com.cinema.modules.catalog.service.ShowtimeSlotPlanner;
import com.cinema.modules.catalog.service.ShowtimeSlotPlanner.Block;
import com.cinema.modules.catalog.service.ShowtimeSlotPlanner.StartOption;
import com.cinema.modules.catalog.service.MovieService;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.MovieServiceImpl;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Quản lý lịch chiếu cho Manager và Admin.
 * GET thường trả trang form. GET ?format=json giữ API cũ.
 */
@WebServlet(name = "ShowtimeManagementServlet", urlPatterns = {
        "/manager/showtimes", "/manager/showtimes/create",
        "/admin/catalog/showtimes", "/admin/catalog/showtimes/create"
})
public class ShowtimeManagementServlet extends HttpServlet {
    private final ShowtimeService showtimeService = new ShowtimeServiceImpl();
    private final BookingEngineService bookingEngineService = new BookingEngineServiceImpl();
    private final MovieService movieService = new MovieServiceImpl();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("json".equalsIgnoreCase(req.getParameter("format"))) {
            writeJsonList(req, resp);
            return;
        }
        User currentUser = currentUser(req);
        if (!canManage(currentUser)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        Long cinemaId = resolveCinemaId(req, currentUser);
        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        List<Movie> movies = movieService.browseMovies("all", null, null, null, "title_asc", 1, 200);
        boolean createPage = req.getServletPath().endsWith("/create");
        String listPath = createPage
                ? req.getServletPath().substring(0, req.getServletPath().length() - "/create".length())
                : req.getServletPath();

        req.setAttribute("listPath", listPath);
        req.setAttribute("cinemas", cinemas);
        req.setAttribute("movies", movies);
        req.setAttribute("selectedCinemaId", cinemaId);
        req.setAttribute("actor", currentUser);
        req.setAttribute("msg", req.getParameter("msg"));
        req.setAttribute("error", req.getParameter("error"));
        req.setAttribute("createdCount", countParam(req.getParameter("createdCount")));
        req.setAttribute("skippedCount", countParam(req.getParameter("skippedCount")));
        req.setAttribute("rooms", cinemaId == null ? List.of() : screeningRoomService.getRoomsByCinema(cinemaId));
        if (createPage && parseLong(req.getParameter("movieId")) == null) {
            String target = req.getContextPath() + listPath;
            if (cinemaId != null) {
                target += "?cinemaId=" + cinemaId;
            }
            resp.sendRedirect(target);
            return;
        }
        if (createPage) {
            prepareCreatePage(req);
            if (req.getAttribute("selectedMovie") == null) {
                String target = req.getContextPath() + listPath;
                if (cinemaId != null) {
                    target += "?cinemaId=" + cinemaId;
                }
                resp.sendRedirect(target);
                return;
            }
            req.getRequestDispatcher("/WEB-INF/views/admin/catalog/showtime-form.jsp").forward(req, resp);
            return;
        }
        req.setAttribute("showtimes", showtimeService.getShowtimesByCinema(cinemaId));
        req.getRequestDispatcher("/WEB-INF/views/admin/catalog/showtimes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("delete".equals(req.getParameter("action"))) {
            deleteFromPage(req, resp);
            return;
        }
        if ("update".equals(req.getParameter("action"))) {
            updateFromPage(req, resp);
            return;
        }
        if ("page".equals(req.getParameter("view"))) {
            createFromPage(req, resp);
            return;
        }
        createJson(req, resp);
    }

    private void createFromPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = currentUser(req);
        if (!canManage(currentUser)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String result = req.getParameterValues("slot") != null
                ? createSelected(req, currentUser)
                : createShowtime(req, currentUser);
        boolean success = "OK".equals(result) || (result != null && result.startsWith("OK|"));
        String cinema = req.getParameter("cinemaId");
        String path = req.getServletPath();
        if (success && path.endsWith("/create")) {
            path = path.substring(0, path.length() - "/create".length());
        }
        String back;
        if (result != null && result.startsWith("OK|")) {
            String[] parts = result.split("\\|");
            back = req.getContextPath() + path + "?msg=created&createdCount=" + parts[1] + "&skippedCount=" + parts[2];
        } else if ("OK".equals(result)) {
            back = req.getContextPath() + path + "?msg=created";
        } else {
            String message = result == null ? "Không thể tạo suất chiếu!" : result;
            back = req.getContextPath() + path + "?error=" + URLEncoder.encode(message, StandardCharsets.UTF_8);
            back = append(back, "movieId", req.getParameter("movieId"));
            back = append(back, "screeningRoomId", req.getParameter("screeningRoomId"));
            String date = req.getParameter("date");
            if (date == null || date.isBlank()) {
                String start = req.getParameter("startTime");
                if (start != null && start.length() >= 10) {
                    date = start.substring(0, 10);
                }
            }
            back = append(back, "date", date);
            back = append(back, "until", req.getParameter("until"));
            back = appendAll(back, "dow", req.getParameterValues("dow"));
            back = appendAll(back, "time", req.getParameterValues("time"));
        }
        if (cinema != null && !cinema.isBlank()) {
            back += "&cinemaId=" + URLEncoder.encode(cinema, StandardCharsets.UTF_8);
        }
        resp.sendRedirect(back);
    }

    private void updateFromPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = currentUser(req);
        if (!canManage(currentUser)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String result = updateShowtime(req, currentUser);
        String key = "OK".equals(result) ? "msg" : "error";
        String value = "OK".equals(result) ? "updated" : result;
        String cinema = req.getParameter("cinemaId");
        String path = req.getServletPath();
        if (path.endsWith("/create")) {
            path = path.substring(0, path.length() - "/create".length());
        }
        String back = req.getContextPath() + path + "?" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
        if (cinema != null && !cinema.isBlank()) {
            back += "&cinemaId=" + URLEncoder.encode(cinema, StandardCharsets.UTF_8);
        }
        resp.sendRedirect(back);
    }

    private void deleteFromPage(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        User currentUser = currentUser(req);
        if (!canManage(currentUser)) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String result = removeShowtime(req, currentUser);
        String key = "DELETED".equals(result) || "KEPT".equals(result) ? "msg" : "error";
        String value = "DELETED".equals(result) ? "deleted" : "KEPT".equals(result) ? "kept" : result;
        String cinema = req.getParameter("cinemaId");
        String path = req.getServletPath();
        if (path.endsWith("/create")) {
            path = path.substring(0, path.length() - "/create".length());
        }
        String back = req.getContextPath() + path + "?" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
        if (cinema != null && !cinema.isBlank()) {
            back += "&cinemaId=" + URLEncoder.encode(cinema, StandardCharsets.UTF_8);
        }
        resp.sendRedirect(back);
    }

    private void writeJsonList(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        User currentUser = currentUser(req);
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("UNAUTHORIZED", "Vui lòng đăng nhập!")));
            return;
        }
        if (!canManage(currentUser)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("FORBIDDEN", "Không có quyền truy cập lịch chiếu!")));
            return;
        }
        Long cinemaId = parseLong(req.getParameter("cinemaId"));
        if (cinemaId == null) cinemaId = currentUser.getCinemaId();
        if (cinemaId == null) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("INVALID_PARAM", "Vui lòng cung cấp mã cụm rạp (cinemaId)")));
            return;
        }
        LocalDate date = parseDate(req.getParameter("date"));
        resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(
                showtimeService.getShowtimesByCinemaAndDate(cinemaId, date),
                "Lấy danh sách suất chiếu thành công")));
    }

    private void createJson(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json; charset=UTF-8");
        User currentUser = currentUser(req);
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("UNAUTHORIZED", "Vui lòng đăng nhập!")));
            return;
        }
        if (!canManage(currentUser)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("FORBIDDEN", "Chỉ Quản lý rạp hoặc Admin mới có quyền tạo suất chiếu!")));
            return;
        }
        String result = createShowtime(req, currentUser);
        if ("OK".equals(result)) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(null, "Tạo suất chiếu mới thành công!")));
        } else if ("FORBIDDEN".equals(result)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("FORBIDDEN", "Quản lý chỉ được phép tạo suất chiếu cho cụm rạp của mình!")));
        } else if ("ROOM_NOT_FOUND".equals(result)) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("ROOM_NOT_FOUND", "Không tìm thấy phòng chiếu!")));
        } else {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("BAD_REQUEST", result)));
        }
    }

    private String createShowtime(HttpServletRequest req, User currentUser) {
        try {
            Long movieId = Long.parseLong(req.getParameter("movieId"));
            Long screeningRoomId = Long.parseLong(req.getParameter("screeningRoomId"));
            LocalDateTime startTime = LocalDateTime.parse(req.getParameter("startTime"));
            String experienceFormat = req.getParameter("experienceFormat");
            if (experienceFormat == null || experienceFormat.isBlank()) experienceFormat = "2D";
            experienceFormat = experienceFormat.trim();

            ScreeningRoom room = screeningRoomService.getRoomById(screeningRoomId);
            if (room == null) return "ROOM_NOT_FOUND";
            if (!formatsFor(room).contains(experienceFormat)) {
                return "Phòng này không chiếu định dạng đã chọn";
            }
            String role = currentUser.getRole().getRoleName();
            if (RoleConstant.MANAGER.equalsIgnoreCase(role)
                    && (currentUser.getCinemaId() == null || !currentUser.getCinemaId().equals(room.getCinemaId()))) {
                return "FORBIDDEN";
            }
            Showtime showtime = Showtime.builder()
                    .movieId(movieId)
                    .screeningRoomId(screeningRoomId)
                    .startTime(startTime)
                    .experienceFormat(experienceFormat)
                    .status("SCHEDULED")
                    .build();
            return showtimeService.createShowtime(showtime) ? "OK" : "Không thể tạo suất chiếu!";
        } catch (IllegalStateException e) {
            return "Phòng chiếu đang bận hoặc đang trong lịch bảo trì";
        } catch (Exception e) {
            return e.getMessage() == null ? "Không thể tạo suất chiếu!" : e.getMessage();
        }
    }

    private String removeShowtime(HttpServletRequest req, User currentUser) {
        try {
            Long showtimeId = Long.parseLong(req.getParameter("showtimeId"));
            Showtime existing = showtimeService.getShowtimeById(showtimeId);
            if (existing == null || existing.getScreeningRoom() == null) return "Không tìm thấy suất chiếu!";
            String role = currentUser.getRole().getRoleName();
            if (RoleConstant.MANAGER.equalsIgnoreCase(role)) {
                Long ownCinema = currentUser.getCinemaId();
                Long currentCinema = existing.getScreeningRoom().getCinemaId();
                if (ownCinema == null || !ownCinema.equals(currentCinema)) {
                    return "Quản lý chỉ xóa suất của rạp mình";
                }
            }
            int tickets = bookingEngineService.countTicketsByShowtime(showtimeId);
            if (tickets < 0) return "Không kiểm tra được vé của suất này";
            if (tickets > 0) {
                return showtimeService.cancelShowtime(showtimeId) ? "KEPT" : "Không hủy được suất đã có vé";
            }
            return showtimeService.deleteShowtime(showtimeId) ? "DELETED" : "Không xóa được suất chiếu";
        } catch (Exception e) {
            return e.getMessage() == null ? "Không xóa được suất chiếu" : e.getMessage();
        }
    }

    private String updateShowtime(HttpServletRequest req, User currentUser) {
        try {
            Long showtimeId = Long.parseLong(req.getParameter("showtimeId"));
            Long screeningRoomId = Long.parseLong(req.getParameter("screeningRoomId"));
            LocalDateTime startTime = LocalDateTime.parse(req.getParameter("startTime"));
            String experienceFormat = req.getParameter("experienceFormat");
            Showtime existing = showtimeService.getShowtimeById(showtimeId);
            if (existing == null || existing.getScreeningRoom() == null) return "Không tìm thấy suất chiếu!";
            ScreeningRoom room = screeningRoomService.getRoomById(screeningRoomId);
            if (room == null) return "ROOM_NOT_FOUND";
            String role = currentUser.getRole().getRoleName();
            if (RoleConstant.MANAGER.equalsIgnoreCase(role)) {
                Long ownCinema = currentUser.getCinemaId();
                Long currentCinema = existing.getScreeningRoom().getCinemaId();
                if (ownCinema == null || !ownCinema.equals(currentCinema) || !ownCinema.equals(room.getCinemaId())) {
                    return "FORBIDDEN";
                }
            }
            // Trạng thái không chỉnh tay trên form: giữ nguyên; hủy suất dùng action delete.
            Showtime showtime = new Showtime();
            showtime.setId(showtimeId);
            showtime.setScreeningRoomId(screeningRoomId);
            showtime.setStartTime(startTime);
            showtime.setExperienceFormat(experienceFormat);
            showtime.setStatus(existing.getStatus());
            return showtimeService.updateShowtime(showtime) ? "OK" : "Không thể cập nhật suất chiếu!";
        } catch (IllegalStateException e) {
            return "Phòng chiếu đang bận hoặc đang trong lịch bảo trì";
        } catch (Exception e) {
            return e.getMessage() == null ? "Không thể cập nhật suất chiếu!" : e.getMessage();
        }
    }

    private void prepareCreatePage(HttpServletRequest req) {
        int buffer = showtimeService.getCleaningBufferMinutes();
        req.setAttribute("bufferMinutes", buffer);
        List<?> movieValues = (List<?>) req.getAttribute("movies");
        Long movieId = parseLong(req.getParameter("movieId"));
        Movie movie = null;
        if (movieValues != null && movieId != null) {
            for (Object value : movieValues) {
                Movie item = (Movie) value;
                if (movieId.equals(item.getId())) {
                    movie = item;
                    break;
                }
            }
        }
        req.setAttribute("selectedMovie", movie);
        if (movie == null) {
            return;
        }
        List<?> roomValues = (List<?>) req.getAttribute("rooms");
        Long requestedRoomId = parseLong(req.getParameter("screeningRoomId"));
        ScreeningRoom room = null;
        if (roomValues != null) {
            for (Object value : roomValues) {
                ScreeningRoom item = (ScreeningRoom) value;
                if (requestedRoomId != null && requestedRoomId.equals(item.getId())) {
                    room = item;
                }
            }
            if (room == null && !roomValues.isEmpty()) {
                room = (ScreeningRoom) roomValues.get(0);
            }
        }
        LocalDate date = parseDate(req.getParameter("date"));
        req.setAttribute("selectedRoom", room);
        req.setAttribute("selectedRoomId", room == null ? null : room.getId());
        req.setAttribute("formats", formatsFor(room));
        req.setAttribute("selectedDate", date.toString());
        int duration = movie.getDurationMinutes() == null ? 120 : movie.getDurationMinutes();
        int needed = duration + buffer;
        req.setAttribute("neededMinutes", needed);
        fillRepeat(req, room, date, needed);
    }

    private void fillRepeat(HttpServletRequest req, ScreeningRoom room, LocalDate start, int needed) {
        LocalDate until = parseDateOrNull(req.getParameter("until"));
        Set<DayOfWeek> days = new HashSet<>();
        String[] dows = req.getParameterValues("dow");
        if (dows != null) {
            for (String dow : dows) {
                Long number = parseLong(dow);
                if (number != null && number >= 1 && number <= 7) {
                    days.add(DayOfWeek.of(number.intValue()));
                }
            }
        }
        if (days.isEmpty()) {
            days.add(start.getDayOfWeek());
        }
        List<LocalDate> dates = ShowtimeRepeatPlanner.dates(start, until, days);
        List<LocalTime> times = readTimes(req);
        Map<LocalDate, List<Block>> byDate = new LinkedHashMap<>();
        List<StartOption> options = List.of();
        LocalDate suggestDay = dates.isEmpty() ? start : dates.get(0);
        if (room != null) {
            for (LocalDate day : dates) {
                List<Block> dayBlocks = blocksFor(room, day);
                byDate.put(day, dayBlocks);
                if (options.isEmpty()) {
                    List<StartOption> found = ShowtimeSlotPlanner.suggest(day, needed, dayBlocks);
                    if (!found.isEmpty()) {
                        options = found;
                        suggestDay = day;
                    }
                }
            }
        }
        List<Preview> rows = ShowtimeRepeatPlanner.preview(dates, times, needed, byDate);
        int free = 0;
        for (Preview row : rows) {
            if (row.isFree()) {
                free++;
            }
        }
        int requested = dates.size() * times.size();
        req.setAttribute("weekDays", ShowtimeRepeatPlanner.week(days));
        req.setAttribute("untilDate", until == null ? "" : until.toString());
        req.setAttribute("chosenTimes", ShowtimeRepeatPlanner.chosen(times, needed));
        req.setAttribute("previewRows", rows);
        req.setAttribute("freeCount", free);
        req.setAttribute("blockedCount", rows.size() - free);
        req.setAttribute("previewTruncated", requested > rows.size());
        req.setAttribute("startOptions", options);
        req.setAttribute("suggestDateLabel", suggestDay.format(DateTimeFormatter.ofPattern("dd/MM")));
        req.setAttribute("repeatSummary", ShowtimeRepeatPlanner.summary(dates));
        req.setAttribute("roomBlocks", room == null ? List.of() : blocksFor(room, start));
    }

    private List<Block> blocksFor(ScreeningRoom room, LocalDate date) {
        List<Block> blocks = new ArrayList<>();
        if (room == null || date == null) {
            return blocks;
        }
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
        for (Showtime showtime : showtimeService.getOccupyingByRoom(room.getId(), date)) {
            String title = showtime.getMovie() == null || showtime.getMovie().getTitle() == null
                    ? "Suất chiếu" : showtime.getMovie().getTitle();
            if (showtime.getStartTime() != null && showtime.getEndTime() != null) {
                blocks.add(new Block(title, showtime.getStartTime(), showtime.getEndTime()));
            }
        }
        for (MaintenanceSchedule schedule : screeningRoomService.getMaintenanceSchedulesByRoom(room.getId())) {
            if (schedule.getStartTime() == null || schedule.getEndTime() == null) continue;
            if (Boolean.TRUE.equals(schedule.getIsDeleted())) continue;
            if (schedule.getStartTime().isBefore(dayEnd) && schedule.getEndTime().isAfter(dayStart)) {
                String reason = schedule.getReason() == null || schedule.getReason().isBlank()
                        ? "Bảo trì" : "Bảo trì · " + schedule.getReason();
                blocks.add(new Block(reason, schedule.getStartTime(), schedule.getEndTime()));
            }
        }
        blocks.sort((left, right) -> left.getStart().compareTo(right.getStart()));
        return blocks;
    }

    private List<LocalTime> readTimes(HttpServletRequest req) {
        LinkedHashSet<LocalTime> times = new LinkedHashSet<>();
        addTimes(times, req.getParameterValues("time"));
        LocalTime toggled = parseTime(req.getParameter("toggleTime"));
        if (toggled != null && !times.remove(toggled) && times.size() < ShowtimeRepeatPlanner.MAX_TIMES) {
            times.add(toggled);
        }
        if ("1".equals(req.getParameter("addClock"))) {
            LocalTime added = parseTime(req.getParameter("addTime"));
            if (added != null && !times.contains(added) && times.size() < ShowtimeRepeatPlanner.MAX_TIMES) {
                times.add(added);
            }
        }
        List<LocalTime> sorted = new ArrayList<>(times);
        sorted.sort(Comparator.naturalOrder());
        return sorted;
    }

    private void addTimes(Set<LocalTime> times, String[] raw) {
        if (raw == null) {
            return;
        }
        for (String item : raw) {
            LocalTime time = parseTime(item);
            if (time != null && times.size() < ShowtimeRepeatPlanner.MAX_TIMES) {
                times.add(time);
            }
        }
    }

    private String createSelected(HttpServletRequest req, User currentUser) {
        String[] slots = req.getParameterValues("slot");
        if (slots == null || slots.length == 0) {
            return "Chọn ít nhất một suất trống";
        }
        try {
            Long movieId = Long.parseLong(req.getParameter("movieId"));
            Long screeningRoomId = Long.parseLong(req.getParameter("screeningRoomId"));
            String experienceFormat = req.getParameter("experienceFormat");
            if (experienceFormat == null || experienceFormat.isBlank()) {
                experienceFormat = "2D";
            }
            experienceFormat = experienceFormat.trim();
            ScreeningRoom room = screeningRoomService.getRoomById(screeningRoomId);
            String guarded = guardRoom(currentUser, room, experienceFormat);
            if (guarded != null) {
                return guarded;
            }
            int created = 0;
            int skipped = 0;
            String failure = null;
            Set<String> seen = new HashSet<>();
            int accepted = 0;
            for (String slot : slots) {
                if (slot == null || !seen.add(slot)) {
                    continue;
                }
                accepted++;
                if (accepted > ShowtimeRepeatPlanner.MAX_ROWS) {
                    break;
                }
                try {
                    LocalDateTime startTime = LocalDateTime.parse(slot);
                    Showtime showtime = Showtime.builder()
                            .movieId(movieId)
                            .screeningRoomId(screeningRoomId)
                            .startTime(startTime)
                            .experienceFormat(experienceFormat)
                            .status("SCHEDULED")
                            .build();
                    if (showtimeService.createShowtime(showtime)) {
                        created++;
                    } else {
                        skipped++;
                    }
                } catch (IllegalStateException busy) {
                    skipped++;
                } catch (RuntimeException ex) {
                    failure = ex.getMessage();
                    break;
                }
            }
            if (created == 0 && failure != null) {
                return failure;
            }
            if (created == 0) {
                return "Phòng chiếu đang bận hoặc đang trong lịch bảo trì";
            }
            return "OK|" + created + "|" + skipped;
        } catch (Exception e) {
            return e.getMessage() == null ? "Không thể tạo suất chiếu!" : e.getMessage();
        }
    }

    private String guardRoom(User currentUser, ScreeningRoom room, String experienceFormat) {
        if (room == null) {
            return "Không tìm thấy phòng chiếu!";
        }
        if (!formatsFor(room).contains(experienceFormat)) {
            return "Phòng này không chiếu định dạng đã chọn";
        }
        String role = currentUser.getRole().getRoleName();
        if (RoleConstant.MANAGER.equalsIgnoreCase(role)
                && (currentUser.getCinemaId() == null || !currentUser.getCinemaId().equals(room.getCinemaId()))) {
            return "Quản lý chỉ được tạo suất cho rạp của mình";
        }
        return null;
    }

    private List<String> formatsFor(ScreeningRoom room) {
        String type = room == null || room.getRoomType() == null ? "" : room.getRoomType();
        if ("IMAX_3D".equals(type)) return List.of("3D", "IMAX");
        if ("STANDARD_2D".equals(type) || "VIP_LUXURY".equals(type)) return List.of("2D");
        return List.of("2D", "3D", "IMAX");
    }

    private String append(String url, String key, String value) {
        if (value == null || value.isBlank()) return url;
        return url + "&" + key + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String appendAll(String url, String key, String[] values) {
        if (values == null) {
            return url;
        }
        for (String value : values) {
            url = append(url, key, value);
        }
        return url;
    }

    private Long resolveCinemaId(HttpServletRequest req, User currentUser) {
        String role = currentUser.getRole() == null ? "" : currentUser.getRole().getRoleName();
        if (RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            return currentUser.getCinemaId();
        }
        Long requested = parseLong(req.getParameter("cinemaId"));
        if (requested != null) return requested;
        if (currentUser.getCinemaId() != null) return currentUser.getCinemaId();
        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        return cinemas.isEmpty() ? null : cinemas.get(0).getId();
    }

    private boolean canManage(User user) {
        if (user == null || user.getRole() == null) return false;
        String role = user.getRole().getRoleName();
        return RoleConstant.ADMIN.equalsIgnoreCase(role) || RoleConstant.MANAGER.equalsIgnoreCase(role);
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("currentUser");
        return value instanceof User ? (User) value : null;
    }

    private LocalDate parseDate(String raw) {
        try {
            return raw == null || raw.isBlank() ? LocalDate.now() : LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private LocalDate parseDateOrNull(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : LocalDate.parse(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private LocalTime parseTime(String raw) {
        try {
            if (raw == null || raw.isBlank()) {
                return null;
            }
            String value = raw.trim();
            if (value.length() >= 5) {
                value = value.substring(0, 5);
            }
            return LocalTime.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private int countParam(String raw) {
        Long value = parseLong(raw);
        if (value == null || value < 0) {
            return 0;
        }
        return value > Integer.MAX_VALUE ? Integer.MAX_VALUE : value.intValue();
    }

    private Long parseLong(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
