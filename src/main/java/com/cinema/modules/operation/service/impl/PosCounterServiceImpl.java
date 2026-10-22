package com.cinema.modules.operation.service.impl;

import com.cinema.common.constant.RoleConstant;
import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.model.Cinema;
import com.cinema.model.FnBItem;
import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import com.cinema.model.User;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.catalog.service.PricingService;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.PricingServiceImpl;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import com.cinema.modules.operation.dao.PosSeatDAO;
import com.cinema.modules.operation.dto.PosCheckoutCommand;
import com.cinema.modules.operation.dto.PosCheckoutResult;
import com.cinema.modules.operation.dto.PosCounterPageData;
import com.cinema.modules.operation.service.CheckoutConfirmationRequiredException;
import com.cinema.modules.operation.service.FnBMenuService;
import com.cinema.modules.operation.service.PosCounterService;
import com.cinema.modules.operation.service.PosOrderService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class PosCounterServiceImpl implements PosCounterService {
    private static final String CHECKOUT_CONFIRMATION_MESSAGE =
            "Hãy kiểm tra chi tiết đơn hàng và xác nhận thanh toán trước khi tạo đơn.";

    private final FnBMenuService fnbMenuService;
    private final ShowtimeService showtimeService;
    private final ScreeningRoomService screeningRoomService;
    private final PricingService pricingService;
    private final SystemSettingDAO settings;
    private final PosOrderService posOrderService;
    private final PosSeatDAO posSeatDAO;

    public PosCounterServiceImpl() {
        this(new FnBMenuServiceImpl(), new ShowtimeServiceImpl(), new ScreeningRoomServiceImpl(),
                new PricingServiceImpl(), new SystemSettingDAO(), new PosOrderServiceImpl(), new PosSeatDAO());
    }

    public PosCounterServiceImpl(FnBMenuService fnbMenuService, ShowtimeService showtimeService,
                                 ScreeningRoomService screeningRoomService, PricingService pricingService,
                                 SystemSettingDAO settings, PosOrderService posOrderService, PosSeatDAO posSeatDAO) {
        this.fnbMenuService = fnbMenuService;
        this.showtimeService = showtimeService;
        this.screeningRoomService = screeningRoomService;
        this.pricingService = pricingService;
        this.settings = settings;
        this.posOrderService = posOrderService;
        this.posSeatDAO = posSeatDAO;
    }

    @Override
    public PosCounterPageData loadPage(User staff, String requestedCinemaId, String requestedDate,
                                       String requestedShowtimeId, String sessionId) throws SQLException {
        if (staff == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản đang đăng nhập.");
        }

        if (RoleConstant.ADMIN.equalsIgnoreCase(roleName(staff))) {
            throw new SecurityException("Admin khong co quyen su dung quay POS.");
        }
        LocalDate today = LocalDate.now();
        LocalDate selectedDate = today;
        String errorMessage = null;
        try {
            selectedDate = parseDate(requestedDate);
        } catch (IllegalArgumentException e) {
            errorMessage = e.getMessage();
        }

        List<Cinema> cinemas = List.of();
        Long cinemaId;
        String setupMessage = null;
        String role = roleName(staff);
        if (RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            cinemas = screeningRoomService.getAllCinemas();
            if (cinemas.isEmpty()) {
                setupMessage = "Chưa có chi nhánh đang hoạt động để bán vé.";
                cinemaId = null;
            } else {
                Long requestedId = parseOptionalPositiveLong(requestedCinemaId);
                if (requestedCinemaId != null && !requestedCinemaId.isBlank() && requestedId == null) {
                    errorMessage = "Chi nhánh không hợp lệ.";
                    cinemaId = null;
                } else if (requestedId == null) {
                    cinemaId = cinemas.get(0).getId();
                } else if (cinemas.stream().noneMatch(cinema -> Objects.equals(cinema.getId(), requestedId))) {
                    errorMessage = "Chi nhánh không hợp lệ.";
                    cinemaId = null;
                } else {
                    cinemaId = requestedId;
                }
            }
        } else {
            requireCounterRole(staff);
            if (staff.getCinemaId() == null) {
                setupMessage = "Tài khoản chưa được gán chi nhánh. Hãy nhờ quản trị viên cập nhật tài khoản.";
                cinemaId = null;
            } else {
                Long requestedId = parseOptionalPositiveLong(requestedCinemaId);
                if (requestedId != null && !requestedId.equals(staff.getCinemaId())) {
                    throw new SecurityException("Bạn chỉ được bán vé tại chi nhánh được gán.");
                }
                cinemaId = staff.getCinemaId();
            }
        }

        List<Showtime> showtimes = List.of();
        List<FnBItem> fnbItems = List.of();
        Showtime selectedShowtime = null;
        Long selectedShowtimeId = null;
        List<Seat> seatMap = List.of();
        Map<Long, BigDecimal> seatPrices = Map.of();
        long availableSeatCount = 0;
        int seatColumnCount = 10;

        if (cinemaId != null) {
            showtimes = showtimeService.getSellableShowtimesByCinemaAndDate(cinemaId, selectedDate);
            fnbItems = fnbMenuService.getAvailableItems(cinemaId);
            if (requestedShowtimeId != null && !requestedShowtimeId.isBlank()) {
                selectedShowtimeId = parseOptionalPositiveLong(requestedShowtimeId);
                if (selectedShowtimeId == null) {
                    errorMessage = "Mã suất chiếu không hợp lệ. Hãy chọn lại suất chiếu.";
                } else {
                    selectedShowtime = findSellableShowtime(showtimes, selectedShowtimeId);
                    if (selectedShowtime == null) {
                        errorMessage = "Suất chiếu không còn bán hoặc không thuộc chi nhánh đã chọn.";
                        selectedShowtimeId = null;
                    } else {
                        seatMap = posSeatDAO.findForShowtime(selectedShowtimeId,
                                selectedShowtime.getScreeningRoomId(), sessionId);
                        Map<Long, BigDecimal> calculatedPrices = new HashMap<>();
                        for (Seat seat : seatMap) {
                            if ("AVAILABLE".equals(seat.getStatus())) {
                                calculatedPrices.put(seat.getId(), pricingService.calculateTicketPrice(
                                        selectedShowtimeId, seat.getSeatTypeId(), selectedShowtime.getStartTime(),
                                        selectedShowtime.getExperienceFormat()));
                            }
                        }
                        seatPrices = calculatedPrices;
                        availableSeatCount = seatMap.stream()
                                .filter(seat -> "AVAILABLE".equals(seat.getStatus())).count();
                        seatColumnCount = seatMap.stream()
                                .map(Seat::getGridColIndex)
                                .filter(Objects::nonNull)
                                .max(Integer::compareTo)
                                .orElse(10);
                    }
                }
            }
        }

        return PosCounterPageData.builder()
                .today(today)
                .selectedDate(selectedDate)
                .cinemaId(cinemaId)
                .selectedShowtimeId(selectedShowtimeId)
                .cinemas(cinemas)
                .showtimes(showtimes)
                .fnbItems(fnbItems)
                .selectedShowtime(selectedShowtime)
                .seatMap(seatMap)
                .seatPrices(seatPrices)
                .availableSeatCount(availableSeatCount)
                .seatColumnCount(seatColumnCount)
                .studentDiscount(discount("STUDENT_TICKET_DISCOUNT_PERCENT", 15))
                .seniorDiscount(discount("SENIOR_TICKET_DISCOUNT_PERCENT", 10))
                .setupMessage(setupMessage)
                .errorMessage(errorMessage)
                .build();
    }

    @Override
    public PosCheckoutResult checkout(PosCheckoutCommand command) {
        if (command == null || command.getStaff() == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản đang đăng nhập.");
        }
        User staff = command.getStaff();
        Long cinemaId = resolveCinemaForCheckout(staff, command.getRequestedCinemaId());
        LocalDate date = parseDate(command.getDate());
        long showtimeId = parseRequiredLong(command.getShowtimeId(), "Chọn suất chiếu.");
        Showtime showtime = findSellableShowtime(cinemaId, date, showtimeId);
        if (showtime == null) {
            throw new IllegalStateException("Suất chiếu đã bắt đầu, đã hủy hoặc không thuộc chi nhánh đang bán.");
        }
        if (showtime.getStartTime() == null || !showtime.getStartTime().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Không thể bán vé cho suất chiếu đã bắt đầu.");
        }
        if (!"true".equalsIgnoreCase(command.getCheckoutConfirmed())) {
            throw new CheckoutConfirmationRequiredException(CHECKOUT_CONFIRMATION_MESSAGE);
        }

        List<Long> seatIds = parseSeatIds(command.getSeatIds());
        String ticketClass = normalizedTicketClass(command.getTicketClass());
        int ticketDiscountPercent = discountPercentForTicketClass(ticketClass);
        BigDecimal multiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(ticketDiscountPercent).movePointLeft(2));
        BigDecimal baseTicketTotal = BigDecimal.ZERO;
        List<BigDecimal> ticketPrices = new ArrayList<>(seatIds.size());
        for (Long seatId : seatIds) {
            Seat seat = screeningRoomService.getSeatById(seatId);
            if (seat == null || !Objects.equals(seat.getScreeningRoomId(), showtime.getScreeningRoomId())
                    || !Boolean.TRUE.equals(seat.getIsActive()) || !"ACTIVE".equalsIgnoreCase(seat.getStatus())) {
                throw new IllegalArgumentException("Một trong các ghế đã bị khóa hoặc không thuộc phòng chiếu này.");
            }
            BigDecimal basePrice = pricingService.calculateTicketPrice(showtimeId, seat.getSeatTypeId(),
                    showtime.getStartTime(), showtime.getExperienceFormat());
            baseTicketTotal = baseTicketTotal.add(basePrice);
            ticketPrices.add(basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP));
        }

        if (!"CASH".equals(command.getPaymentMethod())) {
            throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
        }

        CreateBookingDTO bookingRequest = CreateBookingDTO.builder()
                .staffId(staff.getId())
                .showtimeId(showtimeId)
                .seatIds(seatIds)
                .ticketPrices(ticketPrices)
                .fnbItems(parseFnbItems(command.getFnbQuantities()))
                .channel("POS")
                .paymentMethod(command.getPaymentMethod())
                .ticketDiscountPercent(ticketDiscountPercent)
                .sessionId(command.getSessionId())
                .build();

        BookingResult result = posOrderService.processCounterCheckout(bookingRequest);
        result.setTicketDiscountAmount(baseTicketTotal.subtract(result.getTotalTicketsAmount()));
        return new PosCheckoutResult(cinemaId, date, showtime, result);
    }

    private Long resolveCinemaForCheckout(User staff, String requestedCinema) {
        if (RoleConstant.ADMIN.equalsIgnoreCase(roleName(staff))) {
            throw new SecurityException("Admin khong co quyen su dung quay POS.");
        }
        String role = roleName(staff);
        long requested = parseRequiredLong(requestedCinema, "Chọn chi nhánh.");
        if (RoleConstant.ADMIN.equalsIgnoreCase(role)) {
            Cinema cinema = screeningRoomService.getCinemaById(requested);
            if (cinema == null || !Boolean.TRUE.equals(cinema.getIsActive())) {
                throw new IllegalArgumentException("Chi nhánh không tồn tại hoặc đã ngừng hoạt động.");
            }
            return requested;
        }
        requireCounterRole(staff);
        if (staff.getCinemaId() == null || !staff.getCinemaId().equals(requested)) {
            throw new SecurityException("Bạn chỉ được bán vé tại chi nhánh được gán.");
        }
        return staff.getCinemaId();
    }

    private void requireCounterRole(User user) {
        String role = roleName(user);
        if (!RoleConstant.STAFF.equalsIgnoreCase(role) && !RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            throw new SecurityException("Tài khoản không được phép bán vé tại quầy.");
        }
    }

    private Showtime findSellableShowtime(Long cinemaId, LocalDate date, Long showtimeId) {
        if (cinemaId == null || date == null || showtimeId == null) {
            return null;
        }
        return findSellableShowtime(showtimeService.getSellableShowtimesByCinemaAndDate(cinemaId, date), showtimeId);
    }

    private Showtime findSellableShowtime(List<Showtime> showtimes, Long showtimeId) {
        return showtimes.stream()
                .filter(showtime -> Objects.equals(showtime.getId(), showtimeId))
                .findFirst()
                .orElse(null);
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
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Ngày chiếu không hợp lệ.", e);
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

    private Map<Long, Integer> parseFnbItems(Map<String, String> quantities) {
        Map<Long, Integer> items = new LinkedHashMap<>();
        if (quantities == null) {
            return items;
        }
        for (Map.Entry<String, String> entry : quantities.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isBlank()) {
                continue;
            }
            long itemId = parseRequiredLong(entry.getKey(), "Mã sản phẩm F&B không hợp lệ.");
            int quantity;
            try {
                quantity = Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Số lượng sản phẩm F&B không hợp lệ.", e);
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

    private long parseRequiredLong(String value, String message) {
        Long parsed = parseOptionalPositiveLong(value);
        if (parsed == null) {
            throw new IllegalArgumentException(message);
        }
        return parsed;
    }

    private Long parseOptionalPositiveLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            long parsed = Long.parseLong(value.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String roleName(User user) {
        return user.getRole() == null || user.getRole().getRoleName() == null
                ? "" : user.getRole().getRoleName();
    }
}
