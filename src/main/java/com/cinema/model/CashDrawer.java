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
 * Entity Quản lý ca làm việc và két tiền mặt quầy thu ngân POS (Bảng cash_drawers - Zone 5)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class CashDrawer extends BaseEntity {
    private Long branchId;
    private Long staffId;
    private LocalDateTime openingTime;
    private LocalDateTime closingTime;
    private BigDecimal startingCash;
    private BigDecimal totalCashSales;
    private BigDecimal endingCash;
    private BigDecimal differenceAmount;
    private String status; // OPEN, CLOSED

    // Quan hệ điều hướng
    private Branch branch;
    private User staff;
}
