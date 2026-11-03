package com.cinema.modules.infrastructure.service;

import com.cinema.modules.infrastructure.dto.BookingDetailDTO;
import com.cinema.modules.infrastructure.dto.RevenueBreakdownDTO;
import com.cinema.modules.infrastructure.dto.RevenueFilterDTO;
import com.cinema.modules.infrastructure.dto.RevenueKpiDTO;
import com.cinema.modules.infrastructure.dto.RevenueTimeSeriesDTO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Shared service interface for exporting reports to Excel (.xlsx) and PDF.
 */
public interface ReportExportService {

    /**
     * Exports Revenue & Sales report to multi-sheet formatted Excel workbook (.xlsx).
     */
    void exportRevenueExcel(RevenueFilterDTO filter,
                            RevenueKpiDTO kpi,
                            List<RevenueTimeSeriesDTO> timeSeries,
                            List<RevenueBreakdownDTO> byMovie,
                            List<RevenueBreakdownDTO> byCinema,
                            List<RevenueBreakdownDTO> byChannel,
                            List<BookingDetailDTO> bookings,
                            OutputStream out) throws IOException;

    /**
     * Exports Revenue & Sales report to PDF document with Unicode support.
     */
    void exportRevenuePdf(RevenueFilterDTO filter,
                          RevenueKpiDTO kpi,
                          List<RevenueBreakdownDTO> byMovie,
                          List<RevenueBreakdownDTO> byCinema,
                          List<RevenueBreakdownDTO> byChannel,
                          OutputStream out) throws Exception;

    /**
     * Exports Enterprise BI & Branch Comparison report to formatted Excel workbook (.xlsx).
     */
    void exportComparisonExcel(List<com.cinema.modules.infrastructure.dto.CinemaMetricDTO> metrics,
                               com.cinema.modules.infrastructure.dto.ChainSummaryDTO summary,
                               com.cinema.modules.infrastructure.dto.ComparisonFilterDTO filter,
                               OutputStream out) throws IOException;

    /**
     * Exports Enterprise BI & Branch Comparison report to landscape PDF document.
     */
    void exportComparisonPdf(List<com.cinema.modules.infrastructure.dto.CinemaMetricDTO> metrics,
                             com.cinema.modules.infrastructure.dto.ChainSummaryDTO summary,
                             com.cinema.modules.infrastructure.dto.ComparisonFilterDTO filter,
                             OutputStream out) throws Exception;
}
