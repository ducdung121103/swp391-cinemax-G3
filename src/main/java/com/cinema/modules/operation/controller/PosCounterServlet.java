package com.cinema.modules.operation.controller;

import com.cinema.common.constant.RoleConstant;
import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.model.Cinema;
import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import com.cinema.model.User;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.catalog.service.PricingService;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.PricingServiceImpl;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.infrastructure.dao.CinemaDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import com.cinema.modules.operation.dao.FnBItemDAO;
import com.cinema.modules.operation.dao.PosSeatDAO;
import com.cinema.modules.operation.service.PosOrderService;
import com.cinema.modules.operation.service.impl.PosOrderServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Counter flow for selecting a showtime, seats, optional concessions, and payment. */
@WebServlet(name = "PosCounterServlet", urlPatterns = {"/admin/operation/pos"})
public class PosCounterServlet extends HttpServlet {
    private static final String VIEW = "/WEB-INF/views/admin/operation/pos-counter.jsp";

    private final FnBItemDAO fnbItemDAO = new FnBItemDAO();
    private final ShowtimeService showtimeService = new ShowtimeServiceImpl();
    private final ScreeningRoomService roomService = new ScreeningRoomServiceImpl();
    private final PricingService pricingService = new PricingServiceImpl();
    private final SystemSettingDAO settings = new SystemSettingDAO();
    private final PosOrderService posOrderService = new PosOrderServiceImpl();
    private final PosSeatDAO posSeatDAO = new PosSeatDAO();
    private final CinemaDAO cinemaDAO = new CinemaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User staff = currentUser(req);
        if (staff == null) {
            resp.sendRedirect(req.getContextPath() + "/login?msg=login_required");
            return;
        }

        req.setAttribute("staff", staff);
        req.setAttribute("today", LocalDate.now());
        LocalDate selectedDate = resolveDate(req.getParameter("date"), req);
        req.setAttribute("selectedDate", selectedDate);

        Long cinemaId = resolveCinemaForPage(req, resp, staff);
        if (resp.isCommitted()) {
            return;
        }
        if (cinemaId != null) {
            req.setAttribute("cinemaId", cinemaId);
            req.setAttribute("showtimes", showtimeService.getSellableShowtimesByCinemaAndDate(cinemaId, selectedDate));
            req.setAttribute("fnbItems", fnbItemDAO.findAllActiveForCinema(cinemaId));
        }

        String selectedShowtimeParam = req.getParameter("showtimeId");
        if (cinemaId != null && selectedShowtimeParam != null && !selectedShowtimeParam.isBlank()) {
            try {
                long showtimeId = Long.parseLong(selectedShowtimeParam);
                Showtime showtime = findSellableShowtime(cinemaId, selectedDate, showtimeId);
                if (showtime == null) {
                    req.setAttribute("posError", "Suất chiếu không còn bán hoặc không thuộc chi nhánh đã chọn.");
                } else {
                    req.setAttribute("selectedShowtime", showtime.getId());
                    req.setAttribute("selectedShowtimeDetails", showtime);
                    List<Seat> seatMap = posSeatDAO.findForShowtime(showtimeId, showtime.getScreeningRoomId(),
                            req.getSession().getId());
                    req.setAttribute("seatMap", seatMap);
                    Map<Long, BigDecimal> seatPrices = new HashMap<>();
                    for (Seat seat : seatMap) {
                        if ("AVAILABLE".equals(seat.getStatus())) {
                            seatPrices.put(seat.getId(), pricingService.calculateTicketPrice(showtimeId,
                                    seat.getSeatTypeId(), showtime.getStartTime(), showtime.getExperienceFormat()));
                        }
                    }
                    req.setAttribute("seatPrices", seatPrices);
                    req.setAttribute("availableSeatCount", seatMap.stream()
                            .filter(seat -> "AVAILABLE".equals(seat.getStatus())).count());
                    req.setAttribute("seatColumnCount", seatMap.stream()
                            .map(Seat::getGridColIndex)
                            .filter(Objects::nonNull)
                            .max(Integer::compareTo)
                            .orElse(10));
                }
            } catch (NumberFormatException e) {
                req.setAttribute("posError", "Mã suất chiếu không hợp lệ. Hãy chọn lại suất chiếu.");
            } catch (SQLException e) {
                throw new ServletException("Không thể tải sơ đồ ghế.", e);
            }
        }

        Object flashError = req.getSession().getAttribute("posError");
        if (req.getAttribute("posError") == null && flashError != null) {
            req.setAttribute("posError", flashError);
        }
        req.getSession().removeAttribute("posError");
        req.setAttribute("studentDiscount", discount("STUDENT_TICKET_DISCOUNT_PERCENT", 15));
        req.setAttribute("seniorDiscount", discount("SENIOR_TICKET_DISCOUNT_PERCENT", 10));
        req.setAttribute("checkoutResult", req.getSession().getAttribute("posCheckoutResult"));
        req.getSession().removeAttribute("posCheckoutResult");
        req.setAttribute("checkoutShowtime", req.getSession().getAttribute("posCheckoutShowtime"));
        req.getSession().removeAttribute("posCheckoutShowtime");
        req.getRequestDispatcher(VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        User staff = currentUser(req);
        if (staff == null) {
            resp.sendRedirect(req.getContextPath() + "/login?msg=login_required");
            return;
        }

        Long cinemaId = staff.getCinemaId();
        LocalDate selectedDate = LocalDate.now();
        Long showtimeId = null;
        try {
            cinemaId = resolveCinemaForCheckout(staff, req.getParameter("cinemaId"));
            selectedDate = parseDate(req.getParameter("date"));
            showtimeId = parseRequiredLong(req.getParameter("showtimeId"), "Chọn suất chiếu.");

            Showtime showtime = findSellableShowtime(cinemaId, selectedDate, showtimeId);
            if (showtime == null) {
                throw new IllegalStateException("Suất chiếu đã bắt đầu, đã hủy hoặc không thuộc chi nhánh đang bán.");
            }
            if (showtime.getStartTime() == null || !showtime.getStartTime().isAfter(LocalDateTime.now())) {
                throw new IllegalStateException("Không thể bán vé cho suất chiếu đã bắt đầu.");
            }
            if (!"true".equalsIgnoreCase(req.getParameter("checkoutConfirmed"))) {
                req.getSession().setAttribute("posError", "Hãy kiểm tra chi tiết đơn hàng và xác nhận thanh toán trước khi tạo đơn.");
                resp.sendRedirect(posUrl(req, cinemaId, selectedDate, showtimeId));
                return;
            }

            List<Long> seatIds = parseSeatIds(req.getParameterValues("seatIds"));
            List<BigDecimal> ticketPrices = new ArrayList<>(seatIds.size());
            BigDecimal baseTicketTotal = BigDecimal.ZERO;
            String ticketClass = normalizedTicketClass(req.getParameter("ticketClass"));
            int ticketDiscountPercent = discountPercentForTicketClass(ticketClass);
            BigDecimal multiplier = discountMultiplier(ticketClass);
            for (Long seatId : seatIds) {
                Seat seat = roomService.getSeatById(seatId);
                if (seat == null || !Objects.equals(seat.getScreeningRoomId(), showtime.getScreeningRoomId())
                        || !Boolean.TRUE.equals(seat.getIsActive()) || !"ACTIVE".equalsIgnoreCase(seat.getStatus())) {
                    throw new IllegalArgumentException("Một trong các ghế đã bị khóa hoặc không thuộc phòng chiếu này.");
                }
                BigDecimal basePrice = pricingService.calculateTicketPrice(showtimeId, seat.getSeatTypeId(),
                        showtime.getStartTime(), showtime.getExperienceFormat());
                baseTicketTotal = baseTicketTotal.add(basePrice);
                ticketPrices.add(basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP));
            }

            String paymentMethod = req.getParameter("paymentMethod");
            if (!"CASH".equals(paymentMethod)) {
                throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
            }

            Map<Long, Integer> fnbItems = parseFnbItems(req.getParameterMap());
            CreateBookingDTO dto = CreateBookingDTO.builder()
                    .staffId(staff.getId())
                    .showtimeId(showtimeId)
                    .seatIds(seatIds)
                    .ticketPrices(ticketPrices)
                    .fnbItems(fnbItems)
                    .channel("POS")
                    .paymentMethod(paymentMethod)
                    .ticketDiscountPercent(ticketDiscountPercent)
                    .sessionId(req.getSession().getId())
                    .build();

            BookingResult result = posOrderService.processCounterCheckout(dto);
            result.setTicketDiscountAmount(baseTicketTotal.subtract(result.getTotalTicketsAmount()));
            req.getSession().setAttribute("posCheckoutResult", result);
            req.getSession().setAttribute("posCheckoutShowtime", showtime);
            resp.sendRedirect(posUrl(req, cinemaId, selectedDate, null));
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (IllegalArgumentException | IllegalStateException e) {
            req.getSession().setAttribute("posError", e.getMessage());
            String selectedShowtime = req.getParameter("showtimeId");
            Long redirectShowtimeId = parseOptionalLong(selectedShowtime);
            resp.sendRedirect(posUrl(req, cinemaId, selectedDate, redirectShowtimeId));
        } catch (Exception e) {
            throw new ServletException("Không thể tạo giao dịch POS.", e);
        }
    }

    private Long resolveCinemaForPage(HttpServletRequest req, HttpServletResponse resp, User staff) throws IOException {
        String role = roleName(staff);
        String requestedCinema = req.getParameter("cinemaId");
        if (RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            List<Cinema> cinemas = cinemaDAO.findAll();
            req.setAttribute("cinemas", cinemas);
            if (cinemas.isEmpty()) {
                req.setAttribute("posSetupMessage", "Chưa có chi nhánh đang hoạt động để bán vé.");
                return null;
            }
            Long selected = parseOptionalLong(requestedCinema);
            if (requestedCinema != null && !requestedCinema.isBlank() && selected == null) {
                req.setAttribute("posError", "Chi nhánh không hợp lệ.");
                return null;
            }
            if (selected == null) {
                selected = cinemas.get(0).getId();
            }
            Long finalSelected = selected;
            boolean exists = cinemas.stream().anyMatch(cinema -> Objects.equals(cinema.getId(), finalSelected));
            if (!exists) {
                req.setAttribute("posError", "Chi nhánh không hợp lệ.");
                return null;
            }
            return selected;
        }

        if (!RoleConstant.STAFF.equalsIgnoreCase(role) && !RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Tài khoản không được phép bán vé tại quầy.");
            return null;
        }
        if (staff.getCinemaId() == null) {
            req.setAttribute("posSetupMessage", "Tài khoản chưa được gán chi nhánh. Hãy nhờ quản trị viên cập nhật tài khoản.");
            return null;
        }
        Long requested = parseOptionalLong(requestedCinema);
        if (requested != null && !requested.equals(staff.getCinemaId())) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn chỉ được bán vé tại chi nhánh được gán.");
            return null;
        }
        return staff.getCinemaId();
    }

    private Long resolveCinemaForCheckout(User staff, String requestedCinema) {
        String role = roleName(staff);
        Long requested = parseRequiredLong(requestedCinema, "Chọn chi nhánh.");
        if (RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            Cinema cinema = cinemaDAO.findById(requested);
            if (cinema == null || !Boolean.TRUE.equals(cinema.getIsActive())) {
                throw new IllegalArgumentException("Chi nhánh không tồn tại hoặc đã ngừng hoạt động.");
            }
            return requested;
        }
        if (!RoleConstant.STAFF.equalsIgnoreCase(role) && !RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            throw new SecurityException("Tài khoản không được phép bán vé tại quầy.");
        }
        if (staff.getCinemaId() == null || !staff.getCinemaId().equals(requested)) {
            throw new SecurityException("Bạn chỉ được bán vé tại chi nhánh được gán.");
        }
        return staff.getCinemaId();
    }

    private Showtime findSellableShowtime(Long cinemaId, LocalDate date, Long showtimeId) {
        if (cinemaId == null || date == null || showtimeId == null) {
            return null;
        }
        return showtimeService.getSellableShowtimesByCinemaAndDate(cinemaId, date).stream()
                .filter(showtime -> Objects.equals(showtime.getId(), showtimeId))
                .findFirst()
                .orElse(null);
    }

    private LocalDate resolveDate(String value, HttpServletRequest req) {
        try {
            return parseDate(value);
        } catch (IllegalArgumentException e) {
            req.setAttribute("posError", e.getMessage());
            return LocalDate.now();
        }
    }

    private LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return LocalDate.now();
        }
        try {
            LocalDate date = LocalDate.parse(value.trim());
            if (date.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Chỉ chọn ngày hôm nay hoặc ngày tương lai.");
            }
            return date;
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày chiếu không hợp lệ.");
        }
    }

    private List<Long> parseSeatIds(String[] rawSeats) {
        if (rawSeats == null || rawSeats.length == 0) {
            throw new IllegalArgumentException("Chọn ít nhất một ghế.");
        }
        List<Long> seatIds = new ArrayList<>(rawSeats.length);
        Set<Long> unique = new HashSet<>();
        for (String rawSeat : rawSeats) {
            long seatId = parseRequiredLong(rawSeat, "Danh sách ghế không hợp lệ.");
            if (!unique.add(seatId)) {
                throw new IllegalArgumentException("Danh sách ghế bị trùng.");
            }
            seatIds.add(seatId);
        }
        return seatIds;
    }

    private Map<Long, Integer> parseFnbItems(Map<String, String[]> parameters) {
        Map<Long, Integer> items = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
            if (!entry.getKey().startsWith("fnb_")) {
                continue;
            }
            String[] values = entry.getValue();
            if (values == null || values.length == 0 || values[0].isBlank()) {
                continue;
            }
            long itemId = parseRequiredLong(entry.getKey().substring(4), "Mã sản phẩm F&B không hợp lệ.");
            int quantity;
            try {
                quantity = Integer.parseInt(values[0]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng sản phẩm F&B không hợp lệ.");
            }
            if (quantity < 0 || quantity > 99) {
                throw new IllegalArgumentException("Số lượng mỗi sản phẩm phải từ 0 đến 99.");
            }
            if (quantity > 0) {
                items.put(itemId, quantity);
            }
        }
        return items;
    }

    private String normalizedTicketClass(String value) {
        String ticketClass = value == null ? "STANDARD" : value.trim().toUpperCase();
        if (!"STANDARD".equals(ticketClass) && !"STUDENT".equals(ticketClass) && !"SENIOR".equals(ticketClass)) {
            throw new IllegalArgumentException("Loại giá vé không hợp lệ.");
        }
        return ticketClass;
    }

    private BigDecimal discountMultiplier(String ticketClass) {
        return BigDecimal.ONE.subtract(BigDecimal.valueOf(discountPercentForTicketClass(ticketClass)).movePointLeft(2));
    }

    private int discountPercentForTicketClass(String ticketClass) {
        if ("STUDENT".equals(ticketClass)) {
            return discount("STUDENT_TICKET_DISCOUNT_PERCENT", 15).intValue();
        }
        if ("SENIOR".equals(ticketClass)) {
            return discount("SENIOR_TICKET_DISCOUNT_PERCENT", 10).intValue();
        }
        return 0;
    }

    private BigDecimal discount(String key, int fallback) {
        int percent = settings.getIntSetting(key, fallback);
        return BigDecimal.valueOf(Math.max(0, Math.min(100, percent)));
    }

    private Long parseRequiredLong(String value, String message) {
        Long parsed = parseOptionalLong(value);
        if (parsed == null || parsed <= 0) {
            throw new IllegalArgumentException(message);
        }
        return parsed;
    }

    private Long parseOptionalLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (User) session.getAttribute("currentUser");
    }

    private String roleName(User user) {
        return user.getRole() == null || user.getRole().getRoleName() == null
                ? "" : user.getRole().getRoleName();
    }

    private String posUrl(HttpServletRequest req, Long cinemaId, LocalDate date, Long showtimeId) {
        StringBuilder url = new StringBuilder(req.getContextPath()).append("/admin/operation/pos?");
        if (cinemaId != null) {
            url.append("cinemaId=").append(cinemaId).append('&');
        }
        url.append("date=").append(date == null ? LocalDate.now() : date);
        if (showtimeId != null) {
            url.append("&showtimeId=").append(showtimeId);
        }
        return url.toString();
    }
}
