package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Enterprise aggregate metrics across the entire cinema chain (M-10.2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChainSummaryDTO {

    @Builder.Default
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal totalComparisonRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal chainGrowthPercent = BigDecimal.ZERO;

    private int totalCinemasCount;
    private int activeCinemasCount;
    private int totalRoomsCount;
    private int totalShowtimesCount;
    private int totalBookings;
    private int totalTicketsSold;

    @Builder.Default
    private BigDecimal chainAverageRevenuePerCinema = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal chainAverageOccupancyRate = BigDecimal.ZERO;

    private String bestPerformerName;
    private BigDecimal bestPerformerRevenue;
    private String worstPerformerName;
    private BigDecimal worstPerformerRevenue;
}
