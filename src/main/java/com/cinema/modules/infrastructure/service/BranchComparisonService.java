package com.cinema.modules.infrastructure.service;

import com.cinema.modules.infrastructure.dto.ChainSummaryDTO;
import com.cinema.modules.infrastructure.dto.CinemaMetricDTO;
import com.cinema.modules.infrastructure.dto.ComparisonFilterDTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Enterprise BI & Branch Comparison Service interface (M-10.2).
 */
public interface BranchComparisonService {

    /**
     * Parse, validate and normalize incoming query parameters with sensible defaults.
     */
    ComparisonFilterDTO normalizeFilter(String fromStr, String toStr, String modeStr, String city, String includeInactiveStr);

    /**
     * Compute multi-dimensional metrics, ranks, growth, and performance tiers for all cinema branches.
     */
    List<CinemaMetricDTO> compareBranches(ComparisonFilterDTO filter);

    /**
     * Aggregate enterprise chain-wide summary KPI metrics.
     */
    ChainSummaryDTO getChainSummary(List<CinemaMetricDTO> metrics, ComparisonFilterDTO filter);

    /**
     * Calculate comparison date range based on comparison mode.
     */
    LocalDate[] calculateComparisonPeriod(LocalDate from, LocalDate to, ComparisonFilterDTO.CompareMode mode);

    /**
     * Retrieve distinct operating cities for the filter toolbar.
     */
    List<String> getAvailableCities();
}
