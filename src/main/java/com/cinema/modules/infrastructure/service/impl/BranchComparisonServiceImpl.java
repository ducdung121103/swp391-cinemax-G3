package com.cinema.modules.infrastructure.service.impl;

import com.cinema.modules.infrastructure.dao.BranchComparisonDAO;
import com.cinema.modules.infrastructure.dto.ChainSummaryDTO;
import com.cinema.modules.infrastructure.dto.CinemaMetricDTO;
import com.cinema.modules.infrastructure.dto.ComparisonFilterDTO;
import com.cinema.modules.infrastructure.service.BranchComparisonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for Enterprise BI & Branch Comparison (M-10.2).
 */
public class BranchComparisonServiceImpl implements BranchComparisonService {

    private static final Logger log = LoggerFactory.getLogger(BranchComparisonServiceImpl.class);
    private final BranchComparisonDAO dao;

    public BranchComparisonServiceImpl() {
        this.dao = new BranchComparisonDAO();
    }

    public BranchComparisonServiceImpl(BranchComparisonDAO dao) {
        this.dao = dao;
    }

    @Override
    public ComparisonFilterDTO normalizeFilter(String fromStr, String toStr, String modeStr, String city, String includeInactiveStr) {
        LocalDate today = LocalDate.now();
        LocalDate from;
        LocalDate to;

        try {
            from = (fromStr != null && !fromStr.trim().isEmpty())
                    ? LocalDate.parse(fromStr.trim())
                    : today.withDayOfMonth(1);
        } catch (Exception e) {
            from = today.withDayOfMonth(1);
        }

        try {
            to = (toStr != null && !toStr.trim().isEmpty())
                    ? LocalDate.parse(toStr.trim())
                    : today.withDayOfMonth(today.lengthOfMonth());
        } catch (Exception e) {
            to = today.withDayOfMonth(today.lengthOfMonth());
        }

        // Validate boundary constraints
        if (from.isAfter(to)) {
            LocalDate temp = from;
            from = to;
            to = temp;
        }

        if (ChronoUnit.DAYS.between(from, to) > 366) {
            from = to.minusDays(365);
        }

        ComparisonFilterDTO.CompareMode mode = ComparisonFilterDTO.CompareMode.PREVIOUS_PERIOD;
        if ("SAME_PERIOD_LAST_YEAR".equalsIgnoreCase(modeStr) || "YOY".equalsIgnoreCase(modeStr)) {
            mode = ComparisonFilterDTO.CompareMode.SAME_PERIOD_LAST_YEAR;
        }

        boolean includeInactive = "true".equalsIgnoreCase(includeInactiveStr) || "1".equals(includeInactiveStr);

        return ComparisonFilterDTO.builder()
                .from(from)
                .to(to)
                .compareMode(mode)
                .city((city != null && !city.trim().isEmpty()) ? city.trim() : null)
                .includeInactive(includeInactive)
                .build();
    }

    @Override
    public LocalDate[] calculateComparisonPeriod(LocalDate from, LocalDate to, ComparisonFilterDTO.CompareMode mode) {
        if (mode == ComparisonFilterDTO.CompareMode.SAME_PERIOD_LAST_YEAR) {
            return new LocalDate[]{from.minusYears(1), to.minusYears(1)};
        }
        // PREVIOUS_PERIOD of equal duration immediately preceding 'from'
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        LocalDate compTo = from.minusDays(1);
        LocalDate compFrom = compTo.minusDays(days - 1);
        return new LocalDate[]{compFrom, compTo};
    }

    @Override
    public List<CinemaMetricDTO> compareBranches(ComparisonFilterDTO filter) {
        List<CinemaMetricDTO> cinemas = dao.getBaseCinemas(filter.getCity(), filter.isIncludeInactive());
        if (cinemas.isEmpty()) {
            return cinemas;
        }

        LocalDate[] compPeriod = calculateComparisonPeriod(filter.getFrom(), filter.getTo(), filter.getCompareMode());
        LocalDate compFrom = compPeriod[0];
        LocalDate compTo = compPeriod[1];

        Map<Long, Integer> roomCounts = dao.getRoomCounts();
        Map<Long, BranchComparisonDAO.CinemaBookingAggregate> bookingAggs = dao.getBookingAggregates(filter.getFrom(), filter.getTo());
        Map<Long, BigDecimal> compRevenues = dao.getComparisonRevenues(compFrom, compTo);
        Map<Long, BranchComparisonDAO.ShowtimesAndOccupancy> showtimesAndOcc = dao.getShowtimesAndOccupancy(filter.getFrom(), filter.getTo());
        Map<Long, String> topMovies = dao.getTopMoviePerCinema(filter.getFrom(), filter.getTo());

        BigDecimal totalChainRevenue = BigDecimal.ZERO;

        for (CinemaMetricDTO cinema : cinemas) {
            Long cId = cinema.getCinemaId();

            // Rooms
            int rooms = roomCounts.getOrDefault(cId, 0);
            cinema.setRoomsCount(rooms);

            // Current booking metrics
            BranchComparisonDAO.CinemaBookingAggregate agg = bookingAggs.get(cId);
            if (agg != null) {
                cinema.setRevenue(agg.revenue);
                cinema.setFnbRevenue(agg.fnbRevenue);
                cinema.setConfirmedBookings(agg.bookingCount);
                cinema.setTicketsSold(agg.ticketsSold);

                BigDecimal ticketRev = agg.revenue.subtract(agg.fnbRevenue);
                if (ticketRev.compareTo(BigDecimal.ZERO) < 0) {
                    ticketRev = BigDecimal.ZERO;
                }
                cinema.setTicketRevenue(ticketRev);

                if (agg.ticketsSold > 0) {
                    cinema.setAverageTicketPrice(ticketRev.divide(BigDecimal.valueOf(agg.ticketsSold), 0, RoundingMode.HALF_UP));
                }
            }

            totalChainRevenue = totalChainRevenue.add(cinema.getRevenue());

            // Comparison revenue & growth
            BigDecimal compRev = compRevenues.getOrDefault(cId, BigDecimal.ZERO);
            cinema.setComparisonRevenue(compRev);

            if (compRev.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal growth = cinema.getRevenue().subtract(compRev)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(compRev, 2, RoundingMode.HALF_UP);
                cinema.setGrowthPercent(growth);
            } else if (cinema.getRevenue().compareTo(BigDecimal.ZERO) > 0) {
                cinema.setGrowthPercent(BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP));
            } else {
                cinema.setGrowthPercent(BigDecimal.ZERO);
            }

            // Showtimes & Occupancy
            BranchComparisonDAO.ShowtimesAndOccupancy so = showtimesAndOcc.get(cId);
            if (so != null) {
                cinema.setShowtimesCount(so.showtimesCount);
                cinema.setOccupancyRate(so.occupancyRate);
            }

            // Derived per-room and per-showtime
            if (rooms > 0) {
                cinema.setRevenuePerRoom(cinema.getRevenue().divide(BigDecimal.valueOf(rooms), 0, RoundingMode.HALF_UP));
            }
            if (cinema.getShowtimesCount() > 0) {
                cinema.setRevenuePerShowtime(cinema.getRevenue().divide(BigDecimal.valueOf(cinema.getShowtimesCount()), 0, RoundingMode.HALF_UP));
            }

            // Top movie
            cinema.setTopMovieName(topMovies.getOrDefault(cId, "—"));
        }

        // Sort descending by revenue to assign rank
        cinemas.sort(Comparator.comparing(CinemaMetricDTO::getRevenue).reversed()
                .thenComparing(CinemaMetricDTO::getCinemaName));

        int rank = 1;
        for (CinemaMetricDTO cinema : cinemas) {
            cinema.setRank(rank++);
            if (totalChainRevenue.compareTo(BigDecimal.ZERO) > 0) {
                cinema.setRevenueSharePercent(cinema.getRevenue().multiply(BigDecimal.valueOf(100))
                        .divide(totalChainRevenue, 2, RoundingMode.HALF_UP));
            }
        }

        // Performance Tiers based on chain average
        int activeCount = (int) cinemas.stream().filter(CinemaMetricDTO::isActive).count();
        if (activeCount == 0) activeCount = cinemas.size();

        BigDecimal chainAvgRev = totalChainRevenue.divide(BigDecimal.valueOf(activeCount), 0, RoundingMode.HALF_UP);
        BigDecimal topTierThreshold = chainAvgRev.multiply(BigDecimal.valueOf(1.20));
        BigDecimal belowAvgThreshold = chainAvgRev.multiply(BigDecimal.valueOf(0.70));

        for (CinemaMetricDTO cinema : cinemas) {
            if (chainAvgRev.compareTo(BigDecimal.ZERO) <= 0) {
                cinema.setPerformanceTier(CinemaMetricDTO.PerformanceTier.BELOW_AVERAGE);
            } else if (cinema.getRevenue().compareTo(topTierThreshold) >= 0) {
                cinema.setPerformanceTier(CinemaMetricDTO.PerformanceTier.TOP_TIER);
            } else if (cinema.getRevenue().compareTo(chainAvgRev) >= 0) {
                cinema.setPerformanceTier(CinemaMetricDTO.PerformanceTier.ABOVE_AVERAGE);
            } else if (cinema.getRevenue().compareTo(belowAvgThreshold) >= 0) {
                cinema.setPerformanceTier(CinemaMetricDTO.PerformanceTier.BELOW_AVERAGE);
            } else {
                cinema.setPerformanceTier(CinemaMetricDTO.PerformanceTier.UNDERPERFORMING);
            }
        }

        return cinemas;
    }

    @Override
    public ChainSummaryDTO getChainSummary(List<CinemaMetricDTO> metrics, ComparisonFilterDTO filter) {
        if (metrics == null || metrics.isEmpty()) {
            return ChainSummaryDTO.builder().build();
        }

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCompRevenue = BigDecimal.ZERO;
        int totalCinemas = metrics.size();
        int activeCinemas = 0;
        int totalRooms = 0;
        int totalShowtimes = 0;
        int totalBookings = 0;
        int totalTickets = 0;
        BigDecimal sumOccupancy = BigDecimal.ZERO;
        int occupancyCount = 0;

        CinemaMetricDTO best = null;
        CinemaMetricDTO worst = null;

        for (CinemaMetricDTO m : metrics) {
            totalRevenue = totalRevenue.add(m.getRevenue());
            totalCompRevenue = totalCompRevenue.add(m.getComparisonRevenue());
            if (m.isActive()) activeCinemas++;
            totalRooms += m.getRoomsCount();
            totalShowtimes += m.getShowtimesCount();
            totalBookings += m.getConfirmedBookings();
            totalTickets += m.getTicketsSold();

            if (m.getOccupancyRate().compareTo(BigDecimal.ZERO) > 0) {
                sumOccupancy = sumOccupancy.add(m.getOccupancyRate());
                occupancyCount++;
            }

            if (best == null || m.getRevenue().compareTo(best.getRevenue()) > 0) {
                best = m;
            }
            if (worst == null || (m.getRevenue().compareTo(worst.getRevenue()) < 0 && m.isActive())) {
                worst = m;
            }
        }

        BigDecimal chainGrowth = BigDecimal.ZERO;
        if (totalCompRevenue.compareTo(BigDecimal.ZERO) > 0) {
            chainGrowth = totalRevenue.subtract(totalCompRevenue)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(totalCompRevenue, 2, RoundingMode.HALF_UP);
        } else if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            chainGrowth = BigDecimal.valueOf(100.00).setScale(2, RoundingMode.HALF_UP);
        }

        int activeDivider = activeCinemas > 0 ? activeCinemas : totalCinemas;
        BigDecimal avgRev = totalRevenue.divide(BigDecimal.valueOf(activeDivider), 0, RoundingMode.HALF_UP);
        BigDecimal avgOcc = occupancyCount > 0
                ? sumOccupancy.divide(BigDecimal.valueOf(occupancyCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ChainSummaryDTO.builder()
                .totalRevenue(totalRevenue)
                .totalComparisonRevenue(totalCompRevenue)
                .chainGrowthPercent(chainGrowth)
                .totalCinemasCount(totalCinemas)
                .activeCinemasCount(activeCinemas)
                .totalRoomsCount(totalRooms)
                .totalShowtimesCount(totalShowtimes)
                .totalBookings(totalBookings)
                .totalTicketsSold(totalTickets)
                .chainAverageRevenuePerCinema(avgRev)
                .chainAverageOccupancyRate(avgOcc)
                .bestPerformerName(best != null ? best.getCinemaName() : "—")
                .bestPerformerRevenue(best != null ? best.getRevenue() : BigDecimal.ZERO)
                .worstPerformerName(worst != null ? worst.getCinemaName() : (best != null ? best.getCinemaName() : "—"))
                .worstPerformerRevenue(worst != null ? worst.getRevenue() : BigDecimal.ZERO)
                .build();
    }

    @Override
    public List<String> getAvailableCities() {
        return dao.getAvailableCities();
    }
}
