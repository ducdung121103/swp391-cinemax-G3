package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entity Chính sách hạng hội viên (Bảng membership_tiers - Zone 2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class MembershipTier extends BaseEntity {
    private String tierName; // STANDARD, VIP_SILVER, VIP_GOLD, DIAMOND
    private BigDecimal minSpent;
    private Integer discountPercent;
    private BigDecimal pointRate;
}
