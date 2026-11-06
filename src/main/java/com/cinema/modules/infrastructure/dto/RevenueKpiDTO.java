package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Aggregated KPI metrics for Revenue & Sales Reports (M-10.1).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueKpiDTO {
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal ticketRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal fnbRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;
    @Builder.Default
    private int totalConfirmedBookings = 0;
    @Builder.Default
    private int totalTicketsSold = 0;
    @Builder.Default
    private BigDecimal averageOrderValue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal averageTicketPrice = BigDecimal.ZERO;
}
