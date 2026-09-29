package com.cinema.modules.booking.service.impl;

import com.cinema.common.transaction.TransactionManager;
import com.cinema.model.Booking;
import com.cinema.model.Ticket;
import com.cinema.modules.booking.dao.BookingDAO;
import com.cinema.modules.booking.dao.OrderItemDAO;
import com.cinema.modules.booking.dao.PaymentDAO;
import com.cinema.modules.booking.dao.SeatHoldingDAO;
import com.cinema.modules.booking.dao.TicketDAO;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.VoucherService;
import com.cinema.modules.booking.service.impl.VoucherServiceImpl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.List;

/**
 * Cài đặt Core Engine Đặt vé và Hóa đơn (TV 4).
 * Vận hành 100% bằng TransactionManager lồng nhau đảm bảo tính toàn vẹn dữ liệu ACID.
 */
public class BookingEngineServiceImpl implements BookingEngineService {
    private final BookingDAO bookingDAO;
    private final TicketDAO ticketDAO;
    private final OrderItemDAO orderItemDAO;
    private final PaymentDAO paymentDAO;
    private final SeatHoldingDAO seatHoldingDAO;
    private final VoucherService voucherService;

    public BookingEngineServiceImpl() {
        this.bookingDAO = new BookingDAO();
        this.ticketDAO = new TicketDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.paymentDAO = new PaymentDAO();
        this.seatHoldingDAO = new SeatHoldingDAO();
        this.voucherService = new VoucherServiceImpl();
    }

    public BookingEngineServiceImpl(BookingDAO bookingDAO, TicketDAO ticketDAO,
                                    OrderItemDAO orderItemDAO, PaymentDAO paymentDAO,
                                    SeatHoldingDAO seatHoldingDAO) {
        this(bookingDAO, ticketDAO, orderItemDAO, paymentDAO, seatHoldingDAO, new VoucherServiceImpl());
    }

    public BookingEngineServiceImpl(BookingDAO bookingDAO, TicketDAO ticketDAO,
                                    OrderItemDAO orderItemDAO, PaymentDAO paymentDAO,
                                    SeatHoldingDAO seatHoldingDAO, VoucherService voucherService) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.orderItemDAO = orderItemDAO;
        this.paymentDAO = paymentDAO;
        this.seatHoldingDAO = seatHoldingDAO;
        this.voucherService = voucherService;
    }

    @Override
    public boolean holdSeats(Long showtimeId, List<Long> seatIds, String sessionId) {
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
        return TransactionManager.executeInTransaction(conn -> {
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
                    .status("CONFIRMED")
                    .build();
            Long bookingId = bookingDAO.insertBooking(conn, b);

            // 5. Tạo Tickets (Ràng buộc uk_showtime_seat ngăn chặn trùng ghế)
            if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
                ticketDAO.insertTickets(conn, bookingId, dto.getShowtimeId(), dto.getSeatIds(), dto.getTicketPrices());
            }

            // 6. Tạo OrderItems nếu mua kèm Bắp Nước (tự động kiểm tra và trừ tồn kho rạp)
            if (dto.hasFnb()) {
                orderItemDAO.insertFnbOrderItems(conn, bookingId, dto.getFnbItems());
            }

            // 7. Ghi nhận Thanh toán
            paymentDAO.insertPayment(conn, bookingId, dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "VNPAY", finalAmount);

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
                    .status("SUCCESS")
                    .message("Đặt vé và thanh toán thành công!")
                    .build();
        });
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
