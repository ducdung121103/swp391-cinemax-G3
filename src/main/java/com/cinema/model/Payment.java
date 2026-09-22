package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity Lịch sử thanh toán giao dịch (Bảng payments - Zone 4)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Payment extends BaseEntity {
    private Long bookingId;
    private String paymentMethod; // VNPAY, CASH, POINTS
    private BigDecimal amount;
    private String transactionNo;
    private LocalDateTime paymentTime;
    private String status; // SUCCESS, FAILED, REFUNDED
}
