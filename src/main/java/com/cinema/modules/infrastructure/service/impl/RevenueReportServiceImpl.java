package com.cinema.modules.infrastructure.service.impl;

import com.cinema.common.dto.PageResult;
import com.cinema.model.Cinema;
import com.cinema.model.Movie;
import com.cinema.modules.infrastructure.dao.RevenueReportDAO;
import com.cinema.modules.infrastructure.dto.BookingDetailDTO;
import com.cinema.modules.infrastructure.dto.RevenueBreakdownDTO;
import com.cinema.modules.infrastructure.dto.RevenueFilterDTO;
import com.cinema.modules.infrastructure.dto.RevenueKpiDTO;
import com.cinema.modules.infrastructure.dto.RevenueTimeSeriesDTO;
import com.cinema.modules.infrastructure.service.RevenueReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Implementation of RevenueReportService.
 */
public class RevenueReportServiceImpl implements RevenueReportService {

    private static final Logger logger = LoggerFactory.getLogger(RevenueReportServiceImpl.class);
    private final RevenueReportDAO revenueReportDAO;

    public RevenueReportServiceImpl() {
        this.revenueReportDAO = new RevenueReportDAO();
    }

    public RevenueReportServiceImpl(RevenueReportDAO revenueReportDAO) {
        this.revenueReportDAO = revenueReportDAO;
    }

    @Override
    public RevenueFilterDTO resolveAndValidateFilter(String fromStr, String toStr, String cinemaIdStr,
                                                     String movieIdStr, String channel, String groupBy,
                                                     String pageStr, String pageSizeStr) {
        LocalDate now = LocalDate.now();
        LocalDate from;
        LocalDate to;

        try {
            from = (fromStr != null && !fromStr.isBlank()) ? LocalDate.parse(fromStr.trim()) : now.withDayOfMonth(1);
        } catch (Exception e) {
            from = now.withDayOfMonth(1);
        }

        try {
            to = (toStr != null && !toStr.isBlank()) ? LocalDate.parse(toStr.trim()) : now;
        } catch (Exception e) {
            to = now;
        }

        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc!");
        }

        long daysBetween = ChronoUnit.DAYS.between(from, to);
        if (daysBetween > 366) {
            throw new IllegalArgumentException("Khoảng thời gian báo cáo không được vượt quá 366 ngày (1 năm)!");
        }

        Long cinemaId = null;
        if (cinemaIdStr != null && !cinemaIdStr.isBlank() && !"ALL".equalsIgnoreCase(cinemaIdStr)) {
            try {
                cinemaId = Long.parseLong(cinemaIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        Long movieId = null;
        if (movieIdStr != null && !movieIdStr.isBlank() && !"ALL".equalsIgnoreCase(movieIdStr)) {
            try {
                movieId = Long.parseLong(movieIdStr.trim());
            } catch (NumberFormatException ignored) {}
        }

        String normalizedChannel = (channel != null && !channel.isBlank()) ? channel.trim().toUpperCase() : "ALL";
        if (!"ALL".equals(normalizedChannel) && !"ONLINE".equals(normalizedChannel) && !"POS".equals(normalizedChannel)) {
            normalizedChannel = "ALL";
        }

        String normalizedGroupBy = (groupBy != null && !groupBy.isBlank()) ? groupBy.trim().toUpperCase() : "DAY";
        if (!"DAY".equals(normalizedGroupBy) && !"WEEK".equals(normalizedGroupBy) && !"MONTH".equals(normalizedGroupBy)) {
            normalizedGroupBy = "DAY";
        }

        int page = 1;
        if (pageStr != null && !pageStr.isBlank()) {
            try {
                page = Math.max(1, Integer.parseInt(pageStr.trim()));
            } catch (NumberFormatException ignored) {}
        }

        int pageSize = 20;
        if (pageSizeStr != null && !pageSizeStr.isBlank()) {
            try {
                pageSize = Math.min(100, Math.max(5, Integer.parseInt(pageSizeStr.trim())));
            } catch (NumberFormatException ignored) {}
        }

        return RevenueFilterDTO.builder()
                .from(from)
                .to(to)
                .cinemaId(cinemaId)
                .movieId(movieId)
                .channel(normalizedChannel)
                .groupBy(normalizedGroupBy)
                .page(page)
                .pageSize(pageSize)
                .build();
    }

    @Override
    public RevenueKpiDTO getRevenueKpis(RevenueFilterDTO filter) {
        RevenueKpiDTO currentKpi = revenueReportDAO.getKpiMetrics(
                filter.getFrom(), filter.getTo(), filter.getCinemaId(), filter.getMovieId(), filter.getChannel());

        // Previous equal period calculation
        long days = ChronoUnit.DAYS.between(filter.getFrom(), filter.getTo()) + 1;
        LocalDate prevTo = filter.getFrom().minusDays(1);
        LocalDate prevFrom = prevTo.minusDays(days - 1);

        RevenueKpiDTO prevKpi = revenueReportDAO.getKpiMetrics(
                prevFrom, prevTo, filter.getCinemaId(), filter.getMovieId(), filter.getChannel());

        currentKpi.setRevenueGrowthPercent(calculateGrowth(currentKpi.getTotalRevenue(), prevKpi.getTotalRevenue()));
        currentKpi.setBookingsGrowthPercent(calculateGrowth(
                BigDecimal.valueOf(currentKpi.getTotalConfirmedBookings()),
                BigDecimal.valueOf(prevKpi.getTotalConfirmedBookings())));
        currentKpi.setTicketsGrowthPercent(calculateGrowth(
                BigDecimal.valueOf(currentKpi.getTotalTicketsSold()),
                BigDecimal.valueOf(prevKpi.getTotalTicketsSold())));

        return currentKpi;
    }

    @Override
    public List<RevenueTimeSeriesDTO> getTimeSeriesRevenue(RevenueFilterDTO filter) {
        return revenueReportDAO.getTimeSeriesRevenue(
                filter.getFrom(), filter.getTo(), filter.getCinemaId(), filter.getMovieId(), filter.getChannel(), filter.getGroupBy());
    }

    @Override
    public List<RevenueBreakdownDTO> getRevenueByMovie(RevenueFilterDTO filter, int limit) {
        return revenueReportDAO.getRevenueByMovie(
                filter.getFrom(), filter.getTo(), filter.getCinemaId(), filter.getMovieId(), filter.getChannel(), limit);
    }

    @Override
    public List<RevenueBreakdownDTO> getRevenueByCinema(RevenueFilterDTO filter) {
        return revenueReportDAO.getRevenueByCinema(
                filter.getFrom(), filter.getTo(), filter.getCinemaId(), filter.getMovieId(), filter.getChannel());
    }

@Override
    public PageResult<BookingDetailDTO> getBookingDetailsPaged(RevenueFilterDTO filter) {
        return revenueReportDAO.getBookingDetailsPaged(filter);
    }

    @Override
    public List<Movie> getAllMoviesForFilter() {
        return revenueReportDAO.getAllMoviesForFilter();
    }

    @Override
    public List<Cinema> getAllCinemasForFilter() {
        return revenueReportDAO.getAllCinemasForFilter();
    }

    /**
     * Calculates percentage growth between current and previous values.
     * Prevents divide-by-zero errors.
     */
    public static Double calculateGrowth(BigDecimal current, BigDecimal previous) {
        if (current == null) current = BigDecimal.ZERO;
        if (previous == null) previous = BigDecimal.ZERO;

        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) > 0) {
                return 100.0;
            } else if (current.compareTo(BigDecimal.ZERO) < 0) {
                return -100.0;
            } else {
                return 0.0;
            }
        }

        BigDecimal diff = current.subtract(previous);
        return diff.multiply(BigDecimal.valueOf(100))
                .divide(previous, 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
