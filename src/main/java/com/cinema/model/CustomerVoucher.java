package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Kho Voucher cá nhân của khách hàng (Bảng customer_vouchers - Zone 2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class CustomerVoucher extends BaseEntity {
    private Long userId;
    private Long voucherId;
    private Boolean isUsed;
    private LocalDateTime usedAt;
    private LocalDateTime assignedAt;

    // Quan hệ điều hướng
    private Voucher voucher;
}
