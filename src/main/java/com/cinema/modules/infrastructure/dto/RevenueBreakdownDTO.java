package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Breakdown item for categorical distributions (Movie, Cinema, Channel, Payment Method).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueBreakdownDTO {
    private String categoryKey;
    private String categoryName;
    @Builder.Default
    private BigDecimal revenue = BigDecimal.ZERO;
    @Builder.Default
    private double sharePercent = 0.0;
    @Builder.Default
    private int ticketsSold = 0;
    @Builder.Default
    private int bookingsCount = 0;
}
