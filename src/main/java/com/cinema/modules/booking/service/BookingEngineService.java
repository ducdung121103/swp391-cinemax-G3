package com.cinema.modules.booking.service;

import com.cinema.model.Ticket;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.dto.ShowtimeReminderDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Public Service Interface Cốt lõi của TV 4 (Core Booking & Billing Engine).
 * Quản lý toàn bộ giao dịch đặt vé, giữ ghế 5 phút và thanh toán.
 * TV 5 (POS Quầy & Soát vé) sẽ gọi qua Interface này!
 */
public interface BookingEngineService {
    boolean holdSeats(Long showtimeId, List<Long> seatIds, String sessionId);
    boolean releaseHoldSeats(Long showtimeId, List<Long> seatIds);
    BookingResult createBooking(CreateBookingDTO dto);
    BookingResult renewPendingVnpayBooking(String bookingCode, Long userId);
    boolean cancelPendingVnpayBooking(String bookingCode, Long userId);
    boolean finalizeVnpayPayment(String bookingCode, BigDecimal amount, String transactionNo, boolean succeeded);
    int expirePendingVnpayBookings();
    List<ShowtimeReminderDTO> findBookingsNeedingReminder(int withinMinutes);
    int countTicketsByShowtime(Long showtimeId);
    Ticket getTicketByBarcode(String barcode);
    boolean updateTicketStatus(Long ticketId, String status);
}
