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

    public BookingEngineServiceImpl() {
        this.bookingDAO = new BookingDAO();
        this.ticketDAO = new TicketDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.paymentDAO = new PaymentDAO();
        this.seatHoldingDAO = new SeatHoldingDAO();
    }

    public BookingEngineServiceImpl(BookingDAO bookingDAO, TicketDAO ticketDAO,
                                    OrderItemDAO orderItemDAO, PaymentDAO paymentDAO,
                                    SeatHoldingDAO seatHoldingDAO) {
        this.bookingDAO = bookingDAO;
        this.ticketDAO = ticketDAO;
        this.orderItemDAO = orderItemDAO;
        this.paymentDAO = paymentDAO;
        this.seatHoldingDAO = seatHoldingDAO;
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
            // 1. Tính toán tiền vé
            BigDecimal ticketsTotal = BigDecimal.ZERO;
            if (dto.getTicketPrices() != null && !dto.getTicketPrices().isEmpty()) {
                for (BigDecimal p : dto.getTicketPrices()) {
                    ticketsTotal = ticketsTotal.add(p);
                }
            } else if (dto.getSeatIds() != null) {
                ticketsTotal = new BigDecimal("85000.00").multiply(BigDecimal.valueOf(dto.getSeatIds().size()));
            }

            // 2. Tính toán tiền F&B bắp nước đi kèm
            BigDecimal fnbTotal = BigDecimal.ZERO;
            if (dto.hasFnb()) {
                fnbTotal = orderItemDAO.calculateFnbTotal(conn, dto.getFnbItems());
            }

            BigDecimal discount = BigDecimal.ZERO;
            BigDecimal finalAmount = ticketsTotal.add(fnbTotal).subtract(discount);
            if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                finalAmount = BigDecimal.ZERO;
            }

            // 3. Tạo Booking Master
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

            // 4. Tạo Tickets (Ràng buộc uk_showtime_seat ngăn chặn trùng ghế)
            if (dto.getSeatIds() != null && !dto.getSeatIds().isEmpty()) {
                ticketDAO.insertTickets(conn, bookingId, dto.getShowtimeId(), dto.getSeatIds(), dto.getTicketPrices());
            }

            // 5. Tạo OrderItems nếu mua kèm Bắp Nước
            if (dto.hasFnb()) {
                orderItemDAO.insertFnbOrderItems(conn, bookingId, dto.getFnbItems());
            }

            // 6. Ghi nhận Thanh toán
            paymentDAO.insertPayment(conn, bookingId, dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "VNPAY", finalAmount);

            // 7. Xóa bản ghi giữ ghế tạm thời vì đã mua chính thức thành công
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
