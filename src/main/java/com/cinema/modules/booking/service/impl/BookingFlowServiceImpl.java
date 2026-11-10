package com.cinema.modules.booking.service.impl;

import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.common.util.ValidationUtil;
import com.cinema.model.Cinema;
import com.cinema.model.FnBItem;
import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import com.cinema.modules.booking.dto.BookingCheckoutCommand;
import com.cinema.modules.booking.dto.BookingCheckoutResult;
import com.cinema.modules.booking.dto.BookingPageData;
import com.cinema.modules.booking.dto.BookingQuoteCommand;
import com.cinema.modules.booking.dto.BookingQuoteResult;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.dto.CustomerBookingHistoryDTO;
import com.cinema.modules.booking.dao.BookingDAO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.BookingFlowService;
import com.cinema.modules.booking.service.VNPayService;
import com.cinema.modules.catalog.service.PricingService;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.PricingServiceImpl;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.infrastructure.dao.CinemaDAO;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import com.cinema.modules.operation.dao.PosSeatDAO;
import com.cinema.modules.operation.service.FnBMenuService;
import com.cinema.modules.operation.service.impl.FnBMenuServiceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Cài đặt Service nghiệp vụ điều phối quy trình Đặt vé Online (3-tier architecture).
 */
public class BookingFlowServiceImpl implements BookingFlowService {
    private static final DateTimeFormatter SHOWTIME_TEXT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final ShowtimeService showtimeService;
    private final ScreeningRoomService screeningRoomService;
    private final PricingService pricingService;
    private final FnBMenuService fnbMenuService;
    private final PosSeatDAO posSeatDAO;
    private final CinemaDAO cinemaDAO;
    private final SystemSettingDAO systemSettingDAO;
    private final BookingEngineService bookingEngineService;
    private final VNPayService vnpayService;
    private final BookingDAO bookingDAO;

    public BookingFlowServiceImpl() {
        this(new ShowtimeServiceImpl(), new ScreeningRoomServiceImpl(), new PricingServiceImpl(),
                new FnBMenuServiceImpl(), new PosSeatDAO(), new CinemaDAO(), new SystemSettingDAO(),
                new BookingEngineServiceImpl(), new VNPayServiceImpl(), new BookingDAO());
    }

    public BookingFlowServiceImpl(ShowtimeService showtimeService, ScreeningRoomService screeningRoomService,
                                  PricingService pricingService, FnBMenuService fnbMenuService,
                                  PosSeatDAO posSeatDAO, CinemaDAO cinemaDAO, SystemSettingDAO systemSettingDAO,
                                  BookingEngineService bookingEngineService, VNPayService vnpayService) {
        this(showtimeService, screeningRoomService, pricingService, fnbMenuService, posSeatDAO, cinemaDAO,
                systemSettingDAO, bookingEngineService, vnpayService, new BookingDAO());
    }

    public BookingFlowServiceImpl(ShowtimeService showtimeService, ScreeningRoomService screeningRoomService,
                                  PricingService pricingService, FnBMenuService fnbMenuService,
                                  PosSeatDAO posSeatDAO, CinemaDAO cinemaDAO, SystemSettingDAO systemSettingDAO,
                                  BookingEngineService bookingEngineService, VNPayService vnpayService,
                                  BookingDAO bookingDAO) {
        this.showtimeService = showtimeService;
        this.screeningRoomService = screeningRoomService;
        this.pricingService = pricingService;
        this.fnbMenuService = fnbMenuService;
        this.posSeatDAO = posSeatDAO;
        this.cinemaDAO = cinemaDAO;
        this.systemSettingDAO = systemSettingDAO;
        this.bookingEngineService = bookingEngineService;
        this.vnpayService = vnpayService;
        this.bookingDAO = bookingDAO;
    }

    @Override
    public BookingPageData loadBookingPage(String rawShowtimeId, String sessionId) throws SQLException {
        if (rawShowtimeId == null || rawShowtimeId.isBlank()) {
            return null;
        }

        Long showtimeId;
        try {
            showtimeId = Long.parseLong(rawShowtimeId.trim());
        } catch (NumberFormatException e) {
            return null;
        }

        Showtime showtime = showtimeService.getShowtimeById(showtimeId);
        if (showtime == null || showtime.getStartTime() == null || showtime.getScreeningRoomId() == null
                || !"SCHEDULED".equals(showtime.getStatus())) {
            return null;
        }

        // 1. Xác định rạp chiếu
        Cinema currentCinema = null;
        Long cinemaId = null;
        if (showtime.getScreeningRoom() != null) {
            if (showtime.getScreeningRoom().getCinema() != null && showtime.getScreeningRoom().getCinema().getName() != null) {
                currentCinema = showtime.getScreeningRoom().getCinema();
                cinemaId = currentCinema.getId();
            } else if (showtime.getScreeningRoom().getCinemaId() != null) {
                cinemaId = showtime.getScreeningRoom().getCinemaId();
                currentCinema = cinemaDAO.findById(cinemaId);
                if (currentCinema != null) {
                    showtime.getScreeningRoom().setCinema(currentCinema);
                }
            }
        }

        // 2. Tải sơ đồ ghế và tính bảng giá động
        List<Seat> seatMap = posSeatDAO.findForShowtime(showtime.getId(), showtime.getScreeningRoomId(), sessionId);
        Map<Long, BigDecimal> seatPrices = new HashMap<>();
        for (Seat seat : seatMap) {
            if ("AVAILABLE".equals(seat.getStatus())) {
                BigDecimal price = pricingService.calculateTicketPrice(
                        showtime.getId(), seat.getSeatTypeId(), showtime.getStartTime(),
                        showtime.getExperienceFormat());
                seatPrices.put(seat.getId(), price);
            }
        }
        long availableSeatCount = seatMap.stream().filter(s -> "AVAILABLE".equals(s.getStatus())).count();
        int seatColumnCount = seatMap.stream()
                .map(Seat::getGridColIndex)
                .filter(Objects::nonNull)
                .max(Integer::compareTo)
                .orElse(10);

        // 3. Tải danh mục bắp nước còn hàng của chi nhánh
        List<FnBItem> fnbItems = List.of();
        if (cinemaId != null) {
            try {
                fnbItems = fnbMenuService.getAvailableItems(cinemaId);
            } catch (Exception ignored) {}
        }

        // 4. Lấy cấu hình tỷ lệ giảm giá từ System Settings
        int studentDiscount = systemSettingDAO.getIntSetting("STUDENT_TICKET_DISCOUNT_PERCENT", 15);
        int seniorDiscount = systemSettingDAO.getIntSetting("SENIOR_TICKET_DISCOUNT_PERCENT", 10);

        return BookingPageData.builder()
                .showtime(showtime)
                .showtimeText(showtime.getStartTime().format(SHOWTIME_TEXT))
                .currentCinema(currentCinema)
                .seatMap(seatMap)
                .seatPrices(seatPrices)
                .seatColumnCount(seatColumnCount)
                .availableSeatCount(availableSeatCount)
                .fnbItems(fnbItems)
                .studentDiscount(studentDiscount)
                .seniorDiscount(seniorDiscount)
                .build();
    }

    @Override
    public boolean holdSeats(String rawShowtimeId, List<Long> seatIds, String sessionId) {
        if (rawShowtimeId == null || rawShowtimeId.isBlank()) {
            throw new IllegalArgumentException("Thiếu thông tin suất chiếu.");
        }
        if (seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Chọn ít nhất một ghế.");
        }

        Long showtimeId = Long.parseLong(rawShowtimeId.trim());
        List<Seat> seats = screeningRoomService.getSeatsByIds(seatIds);
        ValidationUtil.validateCoupleSeats(seats);

        return bookingEngineService.holdSeats(showtimeId, seatIds, sessionId);
    }

    @Override
    public BookingQuoteResult quote(BookingQuoteCommand command) {
        if (command == null || command.getShowtimeId() == null || command.getShowtimeId().isBlank()) {
            throw new IllegalArgumentException("Thiếu thông tin suất chiếu.");
        }
        if (command.getSeatIds() == null || command.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế.");
        }

        Long showtimeId = Long.parseLong(command.getShowtimeId().trim());
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);
        if (showtime == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin suất chiếu.");
        }

        List<Seat> seats = screeningRoomService.getSeatsByIds(command.getSeatIds());
        ValidationUtil.validateCoupleSeats(seats);

        int discountPercent = getDiscountPercentForClass(command.getTicketClass());
        BigDecimal multiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(discountPercent).movePointLeft(2));

        BigDecimal baseTicketTotal = BigDecimal.ZERO;
        BigDecimal discountedTicketTotal = BigDecimal.ZERO;

        for (Seat seat : seats) {
            Long seatTypeId = (seat != null) ? seat.getSeatTypeId() : 1L;
            BigDecimal basePrice = pricingService.calculateTicketPrice(
                    showtimeId, seatTypeId, showtime.getStartTime(), showtime.getExperienceFormat());
            baseTicketTotal = baseTicketTotal.add(basePrice);
            BigDecimal discountedPrice = basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            discountedTicketTotal = discountedTicketTotal.add(discountedPrice);
        }

        BigDecimal fnbTotal = BigDecimal.ZERO;
        if (command.getFnbQuantities() != null && !command.getFnbQuantities().isEmpty()) {
            Long cinemaId = showtime.getScreeningRoom() != null ? showtime.getScreeningRoom().getCinemaId() : null;
            if (cinemaId != null) {
                fnbTotal = calculateFnbTotal(cinemaId, command.getFnbQuantities());
            }
        }

        return BookingQuoteResult.builder()
                .baseTicketTotal(baseTicketTotal)
                .discountPercent(discountPercent)
                .discountAmount(baseTicketTotal.subtract(discountedTicketTotal))
                .discountedTicketTotal(discountedTicketTotal)
                .fnbTotal(fnbTotal)
                .grandTotal(discountedTicketTotal.add(fnbTotal))
                .build();
    }

    @Override
    public BookingCheckoutResult checkout(BookingCheckoutCommand command) {
        if (command == null || command.getShowtimeId() == null || command.getShowtimeId().isBlank()) {
            throw new IllegalArgumentException("Thiếu thông tin suất chiếu.");
        }
        if (command.getSeatIds() == null || command.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ít nhất một ghế.");
        }

        Long showtimeId = Long.parseLong(command.getShowtimeId().trim());
        Showtime showtime = showtimeService.getShowtimeById(showtimeId);
        if (showtime == null) {
            throw new IllegalArgumentException("Không tìm thấy thông tin suất chiếu.");
        }

        List<Seat> seats = screeningRoomService.getSeatsByIds(command.getSeatIds());
        ValidationUtil.validateCoupleSeats(seats);

        int discountPercent = getDiscountPercentForClass(command.getTicketClass());
        BigDecimal multiplier = BigDecimal.ONE.subtract(BigDecimal.valueOf(discountPercent).movePointLeft(2));

        List<BigDecimal> ticketPrices = new ArrayList<>();
        for (Seat seat : seats) {
            Long seatTypeId = (seat != null) ? seat.getSeatTypeId() : 1L;
            BigDecimal basePrice = pricingService.calculateTicketPrice(
                    showtimeId, seatTypeId, showtime.getStartTime(), showtime.getExperienceFormat());
            BigDecimal discountedPrice = basePrice.multiply(multiplier).setScale(2, RoundingMode.HALF_UP);
            ticketPrices.add(discountedPrice);
        }

        Long voucherId = null;
        if (command.getVoucherId() != null && !command.getVoucherId().trim().isEmpty()) {
            try {
                voucherId = Long.parseLong(command.getVoucherId().trim());
            } catch (NumberFormatException ignored) {}
        }

        String paymentMethod = command.getPaymentMethod();
        if (paymentMethod == null || paymentMethod.isBlank()) {
            paymentMethod = "VNPAY";
        }

        CreateBookingDTO dto = CreateBookingDTO.builder()
                .userId(command.getUser() != null ? command.getUser().getId() : null)
                .showtimeId(showtimeId)
                .seatIds(command.getSeatIds())
                .ticketPrices(ticketPrices)
                .fnbItems(command.getFnbQuantities())
                .ticketDiscountPercent(discountPercent)
                .voucherId(voucherId)
                .channel("ONLINE")
                .paymentMethod(paymentMethod)
                .sessionId(command.getSessionId())
                .build();

        BookingResult bookingResult = bookingEngineService.createBooking(dto);

        String paymentUrl = null;
        if ("VNPAY".equalsIgnoreCase(paymentMethod) && command.getRequest() != null) {
            paymentUrl = vnpayService.createPaymentUrl(
                    command.getRequest(),
                    bookingResult.getBookingCode(),
                    bookingResult.getFinalAmount(),
                    "Thanh toan ve CineMax " + bookingResult.getBookingCode());
        }

        return BookingCheckoutResult.builder()
                .bookingResult(bookingResult)
                .paymentMethod(paymentMethod)
                .paymentUrl(paymentUrl)
                .bookingCode(bookingResult.getBookingCode())
                .build();
    }

    @Override
    public String retryVnpayPayment(String bookingCode, Long userId,
                                    jakarta.servlet.http.HttpServletRequest request) {
        BookingResult pendingBooking = bookingEngineService.renewPendingVnpayBooking(bookingCode, userId);
        if (pendingBooking == null) {
            throw new IllegalStateException("Đơn không còn chờ thanh toán hoặc không thuộc tài khoản của bạn.");
        }
        return vnpayService.createPaymentUrl(request, pendingBooking.getBookingCode(),
                pendingBooking.getFinalAmount(), "Thanh toan ve CineMax " + pendingBooking.getBookingCode());
    }

    @Override
    public boolean cancelPendingVnpayBooking(String bookingCode, Long userId) {
        return bookingEngineService.cancelPendingVnpayBooking(bookingCode, userId);
    }

    @Override
    public List<CustomerBookingHistoryDTO> getCustomerBookingHistory(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return bookingDAO.findHistoryByUserId(userId);
    }

    @Override
    public CustomerBookingHistoryDTO getCustomerBookingDetail(String bookingCode, Long userId) {
        if (bookingCode == null || bookingCode.isBlank()) {
            return null;
        }
        return bookingDAO.findHistoryDetailByCode(bookingCode, userId);
    }

    private int getDiscountPercentForClass(String ticketClass) {
        if ("STUDENT".equalsIgnoreCase(ticketClass)) {
            return systemSettingDAO.getIntSetting("STUDENT_TICKET_DISCOUNT_PERCENT", 15);
        }
        if ("SENIOR".equalsIgnoreCase(ticketClass)) {
            return systemSettingDAO.getIntSetting("SENIOR_TICKET_DISCOUNT_PERCENT", 10);
        }
        return 0;
    }

    private BigDecimal calculateFnbTotal(Long cinemaId, Map<Long, Integer> quantities) {
        if (quantities == null || quantities.isEmpty() || cinemaId == null) {
            return BigDecimal.ZERO;
        }
        Map<Long, FnBItem> availableItems = new HashMap<>();
        try {
            for (FnBItem item : fnbMenuService.getAvailableItems(cinemaId)) {
                availableItems.put(item.getId(), item);
            }
        } catch (Exception ignored) {}

        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            FnBItem item = availableItems.get(entry.getKey());
            if (item != null && item.getPrice() != null && entry.getValue() != null) {
                total = total.add(item.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            }
        }
        return total;
    }
}
