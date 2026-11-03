package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Time-series aggregated revenue point for charts and trend tables.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueTimeSeriesDTO {
    private String periodLabel;
    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal ticketRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal fnbRevenue = BigDecimal.ZERO;
    @Builder.Default
    private int bookingsCount = 0;
    @Builder.Default
    private int ticketsCount = 0;
}
