package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Filter criteria for Enterprise BI & Branch Comparison Reports (M-10.2).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComparisonFilterDTO {

    public enum CompareMode {
        PREVIOUS_PERIOD,
        SAME_PERIOD_LAST_YEAR
    }

    private LocalDate from;
    private LocalDate to;
    @Builder.Default
    private CompareMode compareMode = CompareMode.PREVIOUS_PERIOD;
    private String city;
    @Builder.Default
    private boolean includeInactive = false;
}
