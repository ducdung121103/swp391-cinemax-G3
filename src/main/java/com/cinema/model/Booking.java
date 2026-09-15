package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * Entity Đơn đặt chỗ Master / Hóa đơn chính (Bảng bookings - Zone 4)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Booking extends BaseEntity {
    private String bookingCode;
    private Long userId;
    private Long staffId;
    private Long showtimeId;
    private Long voucherId;
    private String channel; // ONLINE, POS
    private BigDecimal totalTicketsAmount;
    private BigDecimal totalFnbAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String status; // PENDING, CONFIRMED, CANCELLED, EXPIRED

    // Quan hệ điều hướng
    private User user;
    private User staff;
    private Showtime showtime;
    private Voucher voucher;
    private List<Ticket> tickets;
    private List<OrderItem> orderItems;
    private List<Payment> payments;
}
