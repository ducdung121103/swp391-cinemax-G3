package com.cinema.modules.booking.service.impl;

import com.cinema.common.transaction.TransactionManager;
import com.cinema.common.util.ValidationUtil;
import com.cinema.model.Movie;
import com.cinema.model.Booking;
import com.cinema.model.Payment;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import com.cinema.model.Ticket;
import com.cinema.model.User;
import com.cinema.modules.booking.dao.BookingDAO;
import com.cinema.modules.booking.dao.OrderItemDAO;
import com.cinema.modules.booking.dao.PaymentDAO;
import com.cinema.modules.booking.dao.SeatHoldingDAO;
import com.cinema.modules.booking.dao.TicketDAO;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.dto.FnbReceipt;
import com.cinema.modules.booking.dto.ShowtimeReminderDTO;
import com.cinema.modules.booking.dto.TicketReceipt;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.VoucherService;
import com.cinema.modules.catalog.service.MovieService;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.MovieServiceImpl;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.identity.service.EmailService;
import com.cinema.modules.identity.service.UserService;
import com.cinema.modules.identity.service.impl.EmailServiceImpl;
import com.cinema.modules.identity.service.impl.UserServiceImpl;
import com.cinema.modules.infrastructure.dao.ScreeningRoomDAO;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cài đặt Core Engine Đặt vé và Hóa đơn (TV 4).
 * Vận hành 100% bằng TransactionManager lồng nhau đảm bảo tính toàn vẹn dữ liệu ACID.
 */
public class BookingEngineServiceImpl implements BookingEngineService {
    private static final Logger LOGGER = Logger.getLogger(BookingEngineServiceImpl.class.getName());
    private static final DateTimeFormatter SHOWTIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    private final BookingDAO bookingDAO;
    private final TicketDAO ticketDAO;
    private final OrderItemDAO orderItemDAO;
    private final PaymentDAO paymentDAO;
    private final SeatHoldingDAO seatHoldingDAO;
    private final VoucherService voucherService;
    private final EmailService emailService;
    private final UserService userService;
    private final ShowtimeService showtimeService;
    private final MovieService movieService;
    private final ScreeningRoomDAO screeningRoomDAO;

    public BookingEngineServiceImpl() {
        this.bookingDAO = new BookingDAO();
        this.ticketDAO = new TicketDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.paymentDAO = new PaymentDAO();
        this.seatHoldingDAO = new SeatHoldingDAO();
        this.voucherService = new VoucherServiceImpl();
        this.emailService = new EmailServiceImpl();
        this.userService = new UserServiceImpl();
        this.showtimeService = new ShowtimeServiceImpl();
        this.movieService = new MovieServiceImpl();
        this.screeningRoomDAO = new ScreeningRoomDAO();
    }

    public BookingEngineServiceImpl(BookingDAO bookingDAO, TicketDAO ticketDAO,
                                    OrderItemDAO orderItemDAO, PaymentDAO paymentDAO,
                                    SeatHoldingDAO seatHoldingDAO) {
        this(bookingDAO, ticketDAO, orderItemDAO, paymentDAO, seatHoldingDAO, new VoucherServiceImpl(), null);
    }

    public BookingEngineServiceImpl(BookingDAO bookingDAO, TicketDAO ticketDAO,
                                    OrderItemDAO orderItemDAO, PaymentDAO paymentDAO,
                                    SeatHoldingDAO seatHoldingDAO, VoucherService voucherService) {
        this(bookingDAO, ticketDAO, orderItemDAO, paymentDAO, seatHoldingDAO, voucherService, null);
    }

    public BookingEngineServiceImpl(BookingDAO bookingDAO, TicketDAO ticketDAO,
                                    OrderItemDAO orderItemDAO, PaymentDAO paymentDAO,
                                    SeatHoldingDAO seatHoldingDAO, VoucherService voucherService,
                                    ScreeningRoomDAO screeningRoomDAO) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.orderItemDAO = orderItemDAO;
        this.paymentDAO = paymentDAO;
        this.seatHoldingDAO = seatHoldingDAO;
        this.voucherService = voucherService;
        this.emailService = null;
        this.userService = null;
        this.showtimeService = null;
        this.movieService = null;
        this.screeningRoomDAO = screeningRoomDAO;
    }

    @Override
    public boolean holdSeats(Long showtimeId, List<Long> seatIds, String sessionId) {
        if (screeningRoomDAO != null && seatIds != null && !seatIds.isEmpty()) {
            List<Seat> seats = screeningRoomDAO.findSeatsByIds(seatIds);
            if (!seats.isEmpty()) {
                ValidationUtil.validateCoupleSeats(seats);
            }
        }
        return TransactionManager.executeInTransaction(conn -> {
            for (Long seatId : seatIds) {
                seatHoldingDAO.holdSeat(conn, showtimeId, seatId, sessionId);
            }
            return true;
        });
    }

    @Override
    public boolean releaseHoldSeats(Long showtimeId, List<Long> seatIds) {
        return TransactionManager.executeInTransaction(conn -> {
            seatHoldingDAO.releaseHoldings(conn, showtimeId, seatIds);
            return true;
        });
    }

    @Override
    public BookingResult createBooking(CreateBookingDTO dto) {
        if (dto == null || dto.getShowtimeId() == null) {
            throw new IllegalArgumentException("Thiếu thông tin suất chiếu.");
        }
        if (dto.getSeatIds() == null || dto.getSeatIds().isEmpty()) {
            throw new IllegalArgumentException("Chọn ít nhất một ghế.");
        }
        if (dto.getSeatIds().stream().anyMatch(seatId -> seatId == null || seatId <= 0)) {
            throw new IllegalArgumentException("Danh sách ghế không hợp lệ.");
        }
        if (dto.getTicketPrices() == null || dto.getTicketPrices().size() != dto.getSeatIds().size()
                || dto.getTicketPrices().stream().anyMatch(price -> price == null || price.signum() < 0)) {
            throw new IllegalArgumentException("Thông tin giá vé không hợp lệ.");
        }
        if (dto.hasFnb() && dto.getFnbItems().entrySet().stream()
                .anyMatch(entry -> entry.getKey() == null || entry.getValue() == null || entry.getValue() <= 0)) {
            throw new IllegalArgumentException("Số lượng sản phẩm F&B không hợp lệ.");
        }

        if (screeningRoomDAO != null && dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
            List<Seat> seats = screeningRoomDAO.findSeatsByIds(dto.getSeatIds());
            if (!seats.isEmpty()) {
                ValidationUtil.validateCoupleSeats(seats);
            }
        }
        boolean awaitingVnpay = "ONLINE".equalsIgnoreCase(dto.getChannel())
                && "VNPAY".equalsIgnoreCase(dto.getPaymentMethod());
        BookingResult result = TransactionManager.executeInTransaction(conn -> {
            List<Long> lockOrder = new ArrayList<>(dto.getSeatIds());
            lockOrder.sort(Comparator.naturalOrder());
            ticketDAO.assertSeatsAvailable(conn, dto.getShowtimeId(), lockOrder, dto.getSessionId());

            // 1. Tính toán tiền vé (sử dụng bảng giá động, nghiêm cấm fallback giá cứng)
            BigDecimal ticketsTotal = BigDecimal.ZERO;
            if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
                if (dto.getTicketPrices() == null || dto.getTicketPrices().isEmpty()) {
                    throw new IllegalArgumentException("Thiếu thông tin giá vé, không thể tạo booking");
                }
                if (dto.getTicketPrices().size() != dto.getSeatIds().size()) {
                    throw new IllegalArgumentException("Số lượng giá vé không khớp với số lượng ghế đã chọn");
                }
                for (BigDecimal p : dto.getTicketPrices()) {
                    ticketsTotal = ticketsTotal.add(p);
                }
            }

            // 2. Tính toán tiền F&B bắp nước đi kèm
            BigDecimal fnbTotal = BigDecimal.ZERO;
            if (dto.hasFnb()) {
                fnbTotal = orderItemDAO.calculateFnbTotal(conn, dto.getFnbItems());
            }

            // 3. Tính toán giảm giá Voucher (nếu có)
            BigDecimal orderTotal = ticketsTotal.add(fnbTotal);
            BigDecimal discount = BigDecimal.ZERO;
            if (dto.getVoucherId() != null) {
                discount = voucherService.calculateAndValidateDiscount(conn, dto.getVoucherId(), orderTotal);
            }

            BigDecimal finalAmount = orderTotal.subtract(discount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }
            if (dto.getExpectedFinalAmount() != null
                    && dto.getExpectedFinalAmount().compareTo(finalAmount) != 0) {
                throw new IllegalStateException(
                        "Giá hoặc tồn kho đã thay đổi. Vui lòng lấy lại báo giá trước khi thanh toán.");
            }
            if (awaitingVnpay && finalAmount.signum() <= 0) {
                throw new IllegalArgumentException("Số tiền thanh toán VNPay phải lớn hơn 0.");
            }

            // 4. Tạo Booking Master
            Booking b = Booking.builder()
                    .userId(dto.getUserId())
                    .staffId(dto.getStaffId())
                    .showtimeId(dto.getShowtimeId())
                    .voucherId(dto.getVoucherId())
                    .channel(dto.getChannel() != null ? dto.getChannel() : "ONLINE")
                    .totalTicketsAmount(ticketsTotal)
                    .totalFnbAmount(fnbTotal)
                    .discountAmount(discount)
                    .finalAmount(finalAmount)
                    .status(awaitingVnpay ? "PENDING" : "CONFIRMED")
                    .build();
            Long bookingId = bookingDAO.insertBooking(conn, b);

            // 5. Tạo Tickets (Ràng buộc uk_showtime_seat ngăn chặn trùng ghế)
            List<TicketReceipt> ticketReceipts = new ArrayList<>();
            List<FnbReceipt> fnbReceipts = new ArrayList<>();
            if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
                ticketDAO.insertTickets(conn, bookingId, dto.getShowtimeId(), dto.getSeatIds(),
                        dto.getTicketPrices(), awaitingVnpay ? "PENDING" : "VALID");
                List<TicketReceipt> insertedTickets = ticketDAO.findReceiptItems(conn, bookingId);
                if (insertedTickets != null) {
                    ticketReceipts.addAll(insertedTickets);
                }
            }

            // 6. Tạo OrderItems nếu mua kèm Bắp Nước (tự động kiểm tra và trừ tồn kho rạp)
            if (dto.hasFnb()) {
                orderItemDAO.insertFnbOrderItems(conn, bookingId, dto.getFnbItems());
                List<FnbReceipt> insertedItems = orderItemDAO.findReceiptItems(conn, bookingId);
                if (insertedItems != null) {
                    fnbReceipts.addAll(insertedItems);
                }
            }

            // 7. Ghi nhận Thanh toán
            if (awaitingVnpay) {
                paymentDAO.insertPendingVnpayPayment(conn, bookingId, finalAmount);
            } else {
                paymentDAO.insertPayment(conn, bookingId,
                        dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "VNPAY", finalAmount);
            }

            // 8. Tăng số lượt sử dụng voucher một cách nguyên tử trong cùng transaction
            if (dto.getVoucherId() != null) {
                voucherService.applyVoucherUsage(conn, dto.getVoucherId());
            }

            // 9. Xóa bản ghi giữ ghế tạm thời vì đã mua chính thức thành công
            if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
                seatHoldingDAO.releaseHoldings(conn, dto.getShowtimeId(), dto.getSeatIds());
            }

            return BookingResult.builder()
                    .bookingId(bookingId)
                    .bookingCode(b.getBookingCode())
                    .finalAmount(finalAmount)
                    .status(awaitingVnpay ? "PENDING" : "SUCCESS")
                    .message(awaitingVnpay ? "Đang chờ thanh toán VNPay." : "Đặt vé và thanh toán thành công!")
                    .tickets(ticketReceipts)
                    .totalTicketsAmount(ticketsTotal)
                    .totalFnbAmount(fnbTotal)
                    .discountAmount(discount)
                    .ticketDiscountPercent(dto.getTicketDiscountPercent())
                    .paymentMethod(dto.getPaymentMethod())
                    .fnbItems(fnbReceipts)
                    .build();
        });
        if ("SUCCESS".equals(result.getStatus())) {
            sendConfirmationAfterCommit(dto, result);
        }
        return result;
    }

    @Override
    public boolean finalizeVnpayPayment(String bookingCode, BigDecimal amount, String transactionNo,
                                       boolean succeeded) {
        if (bookingCode == null || bookingCode.isBlank() || amount == null || amount.signum() <= 0) {
            return false;
        }
        FinalizationOutcome outcome = TransactionManager.executeInTransaction(conn ->
                finalizeVnpayPayment(conn, bookingCode, amount, transactionNo, succeeded));
        if (outcome.bookingResult != null) {
            CreateBookingDTO notification = CreateBookingDTO.builder()
                    .userId(outcome.userId)
                    .showtimeId(outcome.showtimeId)
                    .build();
            sendConfirmationAfterCommit(notification, outcome.bookingResult);
        }
        return outcome.succeeded;
    }

    @Override
    public BookingResult renewPendingVnpayBooking(String bookingCode, Long userId) {
        if (bookingCode == null || bookingCode.isBlank() || userId == null) return null;
        return TransactionManager.executeInTransaction(conn -> {
            Booking booking = bookingDAO.findByCode(conn, bookingCode.trim());
            if (booking == null || booking.getId() == null || !userId.equals(booking.getUserId())
                    || !"PENDING".equalsIgnoreCase(booking.getStatus())
                    || booking.getFinalAmount() == null || booking.getFinalAmount().signum() <= 0) {
                return null;
            }
            Payment payment = paymentDAO.findByBookingIdForUpdate(conn, booking.getId());
            if (payment == null || !"VNPAY".equalsIgnoreCase(payment.getPaymentMethod())
                    || !"PENDING".equalsIgnoreCase(payment.getStatus())
                    || payment.getAmount() == null
                    || payment.getAmount().compareTo(booking.getFinalAmount()) != 0
                    || !bookingDAO.refreshPendingVnpayExpiry(conn, booking.getId(), userId)) {
                return null;
            }
            return BookingResult.builder()
                    .bookingId(booking.getId())
                    .bookingCode(booking.getBookingCode())
                    .finalAmount(booking.getFinalAmount())
                    .status("PENDING")
                    .paymentMethod("VNPAY")
                    .build();
        });
    }

    @Override
    public boolean cancelPendingVnpayBooking(String bookingCode, Long userId) {
        if (bookingCode == null || bookingCode.isBlank() || userId == null) return false;
        return TransactionManager.executeInTransaction(conn -> {
            Booking booking = bookingDAO.findByCode(conn, bookingCode.trim());
            if (booking == null || !userId.equals(booking.getUserId())
                    || !"PENDING".equalsIgnoreCase(booking.getStatus())
                    || booking.getFinalAmount() == null) {
                return false;
            }
            FinalizationOutcome outcome = finalizeVnpayPayment(
                    conn, bookingCode.trim(), booking.getFinalAmount(), null, false);
            return outcome.expired;
        });
    }

    @Override
    public int expirePendingVnpayBookings() {
        return TransactionManager.executeInTransaction(conn -> {
            int expired = 0;
            for (String bookingCode : bookingDAO.findExpiredVnpayBookingCodes(conn)) {
                Booking booking = bookingDAO.findByCode(conn, bookingCode);
                if (booking == null || booking.getFinalAmount() == null) continue;
                FinalizationOutcome outcome = finalizeVnpayPayment(
                        conn, bookingCode, booking.getFinalAmount(), null, false);
                if (!outcome.succeeded && outcome.expired) expired++;
            }
            return expired;
        });
    }

    private FinalizationOutcome finalizeVnpayPayment(Connection conn, String bookingCode, BigDecimal amount,
                                                      String transactionNo, boolean succeeded) throws SQLException {
        Booking booking = bookingDAO.findByCode(conn, bookingCode);
        if (booking == null || booking.getId() == null || booking.getFinalAmount() == null
                || booking.getFinalAmount().compareTo(amount) != 0) {
            return FinalizationOutcome.notFinalized();
        }

        Payment payment = paymentDAO.findByBookingIdForUpdate(conn, booking.getId());
        if (payment == null || !"VNPAY".equalsIgnoreCase(payment.getPaymentMethod())
                || payment.getAmount() == null || payment.getAmount().compareTo(amount) != 0) {
            return FinalizationOutcome.notFinalized();
        }

        if ("CONFIRMED".equalsIgnoreCase(booking.getStatus())
                && "SUCCESS".equalsIgnoreCase(payment.getStatus())) {
            boolean sameTransaction = transactionNo != null
                    && transactionNo.equals(payment.getTransactionNo());
            return succeeded && sameTransaction
                    ? FinalizationOutcome.alreadyFinalized() : FinalizationOutcome.notFinalized();
        }
        if (!"PENDING".equalsIgnoreCase(booking.getStatus())
                || !"PENDING".equalsIgnoreCase(payment.getStatus())) {
            return FinalizationOutcome.notFinalized();
        }

        if (succeeded) {
            if (transactionNo == null || transactionNo.isBlank()) {
                return FinalizationOutcome.notFinalized();
            }
            List<Long> seatIds = ticketDAO.findSeatIdsByBooking(conn, booking.getId());
            if (!paymentDAO.updatePendingVnpayPayment(
                            conn, booking.getId(), amount, transactionNo, "SUCCESS")
                    || !bookingDAO.updatePendingStatus(conn, booking.getId(), "CONFIRMED")) {
                throw new IllegalStateException("Không thể xác nhận trạng thái thanh toán VNPay.");
            }
            int activatedTickets = ticketDAO.activatePendingTickets(conn, booking.getId());
            if (activatedTickets == 0) {
                throw new IllegalStateException("Không tìm thấy vé chờ thanh toán cho đơn VNPay.");
            }
            seatHoldingDAO.releaseHoldings(conn, booking.getShowtimeId(), seatIds);
            booking.setStatus("CONFIRMED");
            BookingResult receipt = BookingResult.builder()
                    .bookingId(booking.getId())
                    .bookingCode(booking.getBookingCode())
                    .finalAmount(booking.getFinalAmount())
                    .status("SUCCESS")
                    .paymentMethod("VNPAY")
                    .tickets(ticketDAO.findReceiptItems(conn, booking.getId()))
                    .totalTicketsAmount(booking.getTotalTicketsAmount())
                    .totalFnbAmount(booking.getTotalFnbAmount())
                    .discountAmount(booking.getDiscountAmount())
                    .build();
            return FinalizationOutcome.confirmed(booking.getUserId(), booking.getShowtimeId(), receipt);
        }

        if (!paymentDAO.updatePendingVnpayPayment(conn, booking.getId(), amount, transactionNo, "FAILED")
                || !bookingDAO.updatePendingStatus(conn, booking.getId(), "CANCELLED")) {
            throw new IllegalStateException("Không thể hủy đơn thanh toán VNPay chưa hoàn tất.");
        }
        List<Long> seatIds = ticketDAO.findSeatIdsByBooking(conn, booking.getId());
        orderItemDAO.restoreFnbStock(conn, booking.getId());
        ticketDAO.deleteBookingTickets(conn, booking.getId());
        if (booking.getVoucherId() != null
                && !voucherService.reverseVoucherUsage(conn, booking.getVoucherId())) {
            throw new IllegalStateException("Không thể hoàn lại lượt sử dụng voucher cho đơn VNPay.");
        }
        seatHoldingDAO.releaseHoldings(conn, booking.getShowtimeId(), seatIds);
        return FinalizationOutcome.expired();
    }

    private static final class FinalizationOutcome {
        private final boolean succeeded;
        private final boolean expired;
        private final Long userId;
        private final Long showtimeId;
        private final BookingResult bookingResult;

        private FinalizationOutcome(boolean succeeded, boolean expired, Long userId, Long showtimeId,
                                    BookingResult bookingResult) {
            this.succeeded = succeeded;
            this.expired = expired;
            this.userId = userId;
            this.showtimeId = showtimeId;
            this.bookingResult = bookingResult;
        }

        private static FinalizationOutcome notFinalized() {
            return new FinalizationOutcome(false, false, null, null, null);
        }

        private static FinalizationOutcome alreadyFinalized() {
            return new FinalizationOutcome(true, false, null, null, null);
        }

        private static FinalizationOutcome confirmed(Long userId, Long showtimeId, BookingResult result) {
            return new FinalizationOutcome(true, false, userId, showtimeId, result);
        }

        private static FinalizationOutcome expired() {
            return new FinalizationOutcome(false, true, null, null, null);
        }
    }

    @Override
    public List<ShowtimeReminderDTO> findBookingsNeedingReminder(int withinMinutes) {
        int window = withinMinutes <= 0 ? 120 : withinMinutes;
        return bookingDAO.findBookingsNeedingReminder(window);
    }

    private void sendConfirmationAfterCommit(CreateBookingDTO dto, BookingResult result) {
        if (emailService == null || dto == null || dto.getUserId() == null || result == null) return;
        if (!"SUCCESS".equals(result.getStatus())) return;
        try {
            User user = userService.getUserById(dto.getUserId());
            if (user == null || user.getEmail() == null || user.getEmail().isBlank()) return;
            Showtime showtime = showtimeService.getShowtimeById(dto.getShowtimeId());
            Movie movie = showtime == null ? null : movieService.getMovieById(showtime.getMovieId());
            String movieTitle = movie == null || movie.getTitle() == null ? "Phim" : movie.getTitle();
            String when = formatShowtime(showtime);
            String seats = String.join(", ", ticketDAO.findSeatCodesByBooking(result.getBookingId()));
            if (seats.isBlank()) seats = "Đang cập nhật";
            emailService.sendBookingConfirmationEmail(user.getEmail(), result.getBookingCode(), movieTitle, when, seats, null);
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Không gửi được email xác nhận vé " + result.getBookingCode(), e);
        }
    }

    private String formatShowtime(Showtime showtime) {
        if (showtime == null || showtime.getStartTime() == null) return "Đang cập nhật";
        String when = showtime.getStartTime().format(SHOWTIME_FORMAT);
        ScreeningRoom room = showtime.getScreeningRoom();
        if (room == null) return when;
        String place = room.getName() == null ? "" : room.getName();
        if (room.getCinema() != null && room.getCinema().getName() != null) {
            place = room.getCinema().getName() + " · " + place;
        }
        return place.isBlank() ? when : when + " · " + place;
    }

    @Override
    public int countTicketsByShowtime(Long showtimeId) {
        return ticketDAO.countByShowtime(showtimeId);
    }

    @Override
    public Ticket getTicketByBarcode(String barcode) {
        return ticketDAO.findByBarcode(barcode);
    }

    @Override
    public boolean updateTicketStatus(Long ticketId, String status) {
        return ticketDAO.updateStatus(ticketId, status);
    }
}
