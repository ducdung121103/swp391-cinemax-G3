package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Filter parameters for Revenue & Sales Reports (M-10.1).
 * Simplified to core business filters: date range and cinema branch.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueFilterDTO {
    private LocalDate from;
    private LocalDate to;
    private Long cinemaId;
    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int pageSize = 20;
}
