package com.cinema.modules.infrastructure.service;

import com.cinema.common.dto.PageResult;
import com.cinema.model.Cinema;
import com.cinema.model.Movie;
import com.cinema.modules.infrastructure.dto.BookingDetailDTO;
import com.cinema.modules.infrastructure.dto.RevenueBreakdownDTO;
import com.cinema.modules.infrastructure.dto.RevenueFilterDTO;
import com.cinema.modules.infrastructure.dto.RevenueKpiDTO;
import com.cinema.modules.infrastructure.dto.RevenueTimeSeriesDTO;

import java.util.List;

/**
 * Service interface for Revenue & Sales Reports (M-10.1).
 */
public interface RevenueReportService {

    /**
     * Parses and validates filter parameters with defaults and range checks.
     */
    RevenueFilterDTO resolveAndValidateFilter(String fromStr, String toStr, String cinemaIdStr,
                                              String movieIdStr, String channel, String groupBy,
                                              String pageStr, String pageSizeStr);

    /**
     * Computes current period KPIs and calculates period-over-period growth vs previous equal period.
     */
    RevenueKpiDTO getRevenueKpis(RevenueFilterDTO filter);

    /**
     * Retrieves time-series revenue aggregated by DAY, WEEK, or MONTH.
     */
    List<RevenueTimeSeriesDTO> getTimeSeriesRevenue(RevenueFilterDTO filter);

    /**
     * Retrieves revenue breakdown by movie (Top N).
     */
    List<RevenueBreakdownDTO> getRevenueByMovie(RevenueFilterDTO filter, int limit);

    /**
     * Retrieves revenue breakdown by cinema branch.
     */
    List<RevenueBreakdownDTO> getRevenueByCinema(RevenueFilterDTO filter);

    /**
     * Retrieves revenue breakdown by channel (ONLINE vs POS).
     */
    List<RevenueBreakdownDTO> getRevenueByChannel(RevenueFilterDTO filter);

    /**
     * Retrieves revenue breakdown by payment method.
     */
    List<RevenueBreakdownDTO> getRevenueByPaymentMethod(RevenueFilterDTO filter);

    /**
     * Retrieves paginated booking drill-down details.
     */
    PageResult<BookingDetailDTO> getBookingDetailsPaged(RevenueFilterDTO filter);

    /**
     * Retrieves list of movies for dropdown filter.
     */
    List<Movie> getAllMoviesForFilter();

    /**
     * Retrieves list of cinemas for dropdown filter.
     */
    List<Cinema> getAllCinemasForFilter();
}
