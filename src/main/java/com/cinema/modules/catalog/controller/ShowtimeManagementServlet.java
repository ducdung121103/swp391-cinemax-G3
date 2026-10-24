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
import com.cinema.modules.catalog.service.ShowtimeSlotPlanner;
import com.cinema.modules.catalog.service.ShowtimeSlotPlanner.Block;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
        req.setAttribute("rooms", cinemaId == null ? List.of() : screeningRoomService.getRoomsByCinema(cinemaId));
        if (createPage && parseLong(req.getParameter("movieId")) == null) {
            Set<Long> scheduledMovieIds = new HashSet<>();
            for (Showtime showtime : showtimeService.getShowtimesByCinema(cinemaId)) {
                if (showtime.getMovieId() != null) scheduledMovieIds.add(showtime.getMovieId());
            }
            Movie firstNewMovie = null;
            for (Movie movie : movies) {
                if (!scheduledMovieIds.contains(movie.getId())) {
                    firstNewMovie = movie;
                    break;
                }
            }
            if (firstNewMovie != null) {
                String target = req.getContextPath() + req.getServletPath() + "?movieId=" + firstNewMovie.getId();
                if (cinemaId != null) target += "&cinemaId=" + cinemaId;
                resp.sendRedirect(target);
                return;
            }
        }
        if (createPage) {
            prepareCreatePage(req);
            req.getRequestDispatcher("/WEB-INF/views/admin/catalog/showtime-form.jsp").forward(req, resp);
            return;
        }
        req.setAttribute("showtimes", showtimeService.getShowtimesByCinema(cinemaId));
        req.getRequestDispatcher("/WEB-INF/views/admin/catalog/showtimes.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
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
        String result = createShowtime(req, currentUser);
        String key = "OK".equals(result) ? "msg" : "error";
        String value = "OK".equals(result) ? "created" : result;
        String cinema = req.getParameter("cinemaId");
        String path = req.getServletPath();
        if ("OK".equals(result) && path.endsWith("/create")) {
            path = path.substring(0, path.length() - "/create".length());
        }
        String back = req.getContextPath() + path + "?" + key + "="
                + URLEncoder.encode(value, StandardCharsets.UTF_8);
        if (cinema != null && !cinema.isBlank()) {
            back += "&cinemaId=" + URLEncoder.encode(cinema, StandardCharsets.UTF_8);
        }
        if (!"OK".equals(result)) {
            back = append(back, "movieId", req.getParameter("movieId"));
            back = append(back, "screeningRoomId", req.getParameter("screeningRoomId"));
            String start = req.getParameter("startTime");
            if (start != null && start.length() >= 10) {
                back = append(back, "date", start.substring(0, 10));
            }
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

    private String updateShowtime(HttpServletRequest req, User currentUser) {
        try {
            Long showtimeId = Long.parseLong(req.getParameter("showtimeId"));
            Long screeningRoomId = Long.parseLong(req.getParameter("screeningRoomId"));
            LocalDateTime startTime = LocalDateTime.parse(req.getParameter("startTime"));
            String experienceFormat = req.getParameter("experienceFormat");
            String status = req.getParameter("status");
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
            Showtime showtime = new Showtime();
            showtime.setId(showtimeId);
            showtime.setScreeningRoomId(screeningRoomId);
            showtime.setStartTime(startTime);
            showtime.setExperienceFormat(experienceFormat);
            showtime.setStatus(status);
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
        Object cinemaValue = req.getAttribute("selectedCinemaId");
        Long cinemaId = cinemaValue instanceof Long ? (Long) cinemaValue : null;
        Set<Long> scheduledMovieIds = new HashSet<>();
        for (Showtime showtime : showtimeService.getShowtimesByCinema(cinemaId)) {
            if (showtime.getMovieId() != null) {
                scheduledMovieIds.add(showtime.getMovieId());
            }
        }
        List<Movie> newMovies = new ArrayList<>();
        if (movieValues != null) {
            for (Object value : movieValues) {
                Movie item = (Movie) value;
                if (!scheduledMovieIds.contains(item.getId())) {
                    newMovies.add(item);
                }
            }
        }
        req.setAttribute("newMovies", newMovies);
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
        List<Block> blocks = new ArrayList<>();
        if (room != null) {
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
        }
        blocks.sort((left, right) -> left.getStart().compareTo(right.getStart()));
        req.setAttribute("roomBlocks", blocks);
        req.setAttribute("startOptions", ShowtimeSlotPlanner.suggest(date, needed, blocks));
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

    private Long parseLong(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
