package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Filter parameters for Revenue & Sales Reports (M-10.1).
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
    private Long movieId;
    private String channel; // ALL, ONLINE, POS
    private String groupBy; // DAY, WEEK, MONTH
    @Builder.Default
    private int page = 1;
    @Builder.Default
    private int pageSize = 20;
}
