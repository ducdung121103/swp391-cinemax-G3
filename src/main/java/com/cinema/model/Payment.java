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
 * Entity Lá»‹ch sá»­ thanh toÃ¡n giao dá»‹ch (Báº£ng payments - Zone 4)
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
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
    private String status; // SUCCESS, FAILED,
}

