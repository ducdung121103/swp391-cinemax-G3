package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Multi-dimensional operational and financial metrics per cinema branch (M-10.2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CinemaMetricDTO {

    public enum PerformanceTier {
        TOP_TIER,       // Far above chain average (>= 120%)
        ABOVE_AVERAGE,  // Between 100% and 120% of chain average
        BELOW_AVERAGE,  // Between 70% and 100% of chain average
        UNDERPERFORMING // Below 70% of chain average
    }

    private Long cinemaId;
    private String cinemaCode;
    private String cinemaName;
    private String city;
    private boolean active;

    private int rank;
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal comparisonRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal growthPercent = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal revenueSharePercent = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal ticketRevenue = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal fnbRevenue = BigDecimal.ZERO;

    private int confirmedBookings;
    private int ticketsSold;
    @Builder.Default
    private BigDecimal averageTicketPrice = BigDecimal.ZERO;

    private int roomsCount;
    private int showtimesCount;
    @Builder.Default
    private BigDecimal occupancyRate = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal revenuePerRoom = BigDecimal.ZERO;
    @Builder.Default
    private BigDecimal revenuePerShowtime = BigDecimal.ZERO;

    private String topMovieName;
    @Builder.Default
    private PerformanceTier performanceTier = PerformanceTier.BELOW_AVERAGE;
}
