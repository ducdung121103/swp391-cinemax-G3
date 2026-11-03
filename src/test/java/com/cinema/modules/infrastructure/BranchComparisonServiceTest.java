package com.cinema.modules.infrastructure;

import com.cinema.modules.infrastructure.dao.BranchComparisonDAO;
import com.cinema.modules.infrastructure.dto.ChainSummaryDTO;
import com.cinema.modules.infrastructure.dto.CinemaMetricDTO;
import com.cinema.modules.infrastructure.dto.ComparisonFilterDTO;
import com.cinema.modules.infrastructure.service.BranchComparisonService;
import com.cinema.modules.infrastructure.service.impl.BranchComparisonServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class BranchComparisonServiceTest {

    private BranchComparisonDAO dao;
    private BranchComparisonService service;

    @BeforeEach
    void setUp() {
        dao = Mockito.mock(BranchComparisonDAO.class);
        service = new BranchComparisonServiceImpl(dao);
    }

    @Test
    @DisplayName("Filter normalization sets current month by default and swaps invalid dates")
    void testNormalizeFilter() {
        ComparisonFilterDTO defaultFilter = service.normalizeFilter(null, null, null, null, null);
        assertNotNull(defaultFilter.getFrom());
        assertNotNull(defaultFilter.getTo());
        assertEquals(1, defaultFilter.getFrom().getDayOfMonth());
        assertFalse(defaultFilter.getFrom().isAfter(defaultFilter.getTo()));
        assertEquals(ComparisonFilterDTO.CompareMode.PREVIOUS_PERIOD, defaultFilter.getCompareMode());

        // Test swap when from > to
        ComparisonFilterDTO swapped = service.normalizeFilter("2026-11-20", "2026-11-01", "YOY", "Hà Nội", "true");
        assertEquals(LocalDate.parse("2026-11-01"), swapped.getFrom());
        assertEquals(LocalDate.parse("2026-11-20"), swapped.getTo());
        assertEquals(ComparisonFilterDTO.CompareMode.SAME_PERIOD_LAST_YEAR, swapped.getCompareMode());
        assertEquals("Hà Nội", swapped.getCity());
        assertTrue(swapped.isIncludeInactive());

        // Test range constraint > 366 days
        ComparisonFilterDTO capped = service.normalizeFilter("2024-01-01", "2026-11-01", null, null, null);
        assertTrue(java.time.temporal.ChronoUnit.DAYS.between(capped.getFrom(), capped.getTo()) <= 366);
    }

    @Test
    @DisplayName("Comparison period calculation generates correct previous period and YoY dates")
    void testCalculateComparisonPeriod() {
        LocalDate from = LocalDate.of(2026, 11, 1);
        LocalDate to = LocalDate.of(2026, 11, 10); // 10 days

        // PREVIOUS_PERIOD -> 10 days before 2026-11-01 => 2026-10-22 to 2026-10-31
        LocalDate[] prevPeriod = service.calculateComparisonPeriod(from, to, ComparisonFilterDTO.CompareMode.PREVIOUS_PERIOD);
        assertEquals(LocalDate.of(2026, 10, 22), prevPeriod[0]);
        assertEquals(LocalDate.of(2026, 10, 31), prevPeriod[1]);

        // SAME_PERIOD_LAST_YEAR -> 2025-11-01 to 2025-11-10
        LocalDate[] yoyPeriod = service.calculateComparisonPeriod(from, to, ComparisonFilterDTO.CompareMode.SAME_PERIOD_LAST_YEAR);
        assertEquals(LocalDate.of(2025, 11, 1), yoyPeriod[0]);
        assertEquals(LocalDate.of(2025, 11, 10), yoyPeriod[1]);
    }

    @Test
    @DisplayName("Branch comparison ranks branches by revenue descending and calculates share %")
    void testCompareBranchesRankingAndShare() {
        List<CinemaMetricDTO> mockCinemas = new ArrayList<>();
        mockCinemas.add(CinemaMetricDTO.builder().cinemaId(1L).cinemaCode("C-HN01").cinemaName("CineMax Bà Triệu").active(true).build());
        mockCinemas.add(CinemaMetricDTO.builder().cinemaId(2L).cinemaCode("C-SG01").cinemaName("CineMax Landmark").active(true).build());
        mockCinemas.add(CinemaMetricDTO.builder().cinemaId(3L).cinemaCode("C-DN01").cinemaName("CineMax Đà Nẵng").active(true).build());

        when(dao.getBaseCinemas(any(), anyBoolean())).thenReturn(mockCinemas);
        when(dao.getRoomCounts()).thenReturn(Map.of(1L, 4, 2L, 6, 3L, 3));

        BranchComparisonDAO.CinemaBookingAggregate agg1 = new BranchComparisonDAO.CinemaBookingAggregate();
        agg1.cinemaId = 1L; agg1.revenue = new BigDecimal("30000000"); agg1.fnbRevenue = new BigDecimal("5000000"); agg1.bookingCount = 100; agg1.ticketsSold = 200;

        BranchComparisonDAO.CinemaBookingAggregate agg2 = new BranchComparisonDAO.CinemaBookingAggregate();
        agg2.cinemaId = 2L; agg2.revenue = new BigDecimal("70000000"); agg2.fnbRevenue = new BigDecimal("15000000"); agg2.bookingCount = 250; agg2.ticketsSold = 500;

        BranchComparisonDAO.CinemaBookingAggregate agg3 = new BranchComparisonDAO.CinemaBookingAggregate();
        agg3.cinemaId = 3L; agg3.revenue = new BigDecimal("10000000"); agg3.fnbRevenue = new BigDecimal("2000000"); agg3.bookingCount = 40; agg3.ticketsSold = 80;

        when(dao.getBookingAggregates(any(), any())).thenReturn(Map.of(1L, agg1, 2L, agg2, 3L, agg3));
        when(dao.getComparisonRevenues(any(), any())).thenReturn(Map.of(1L, new BigDecimal("20000000"), 2L, new BigDecimal("80000000"), 3L, BigDecimal.ZERO));
        when(dao.getShowtimesAndOccupancy(any(), any())).thenReturn(new HashMap<>());
        when(dao.getTopMoviePerCinema(any(), any())).thenReturn(Map.of(2L, "Avatar 2"));

        ComparisonFilterDTO filter = ComparisonFilterDTO.builder()
                .from(LocalDate.of(2026, 11, 1))
                .to(LocalDate.of(2026, 11, 30))
                .compareMode(ComparisonFilterDTO.CompareMode.PREVIOUS_PERIOD)
                .build();

        List<CinemaMetricDTO> results = service.compareBranches(filter);
        assertEquals(3, results.size());

        // Rank 1: C-SG01 (70M)
        assertEquals("C-SG01", results.get(0).getCinemaCode());
        assertEquals(1, results.get(0).getRank());
        assertEquals(new BigDecimal("70000000"), results.get(0).getRevenue());
        assertEquals("Avatar 2", results.get(0).getTopMovieName());

        // Rank 2: C-HN01 (30M)
        assertEquals("C-HN01", results.get(1).getCinemaCode());
        assertEquals(2, results.get(1).getRank());

        // Rank 3: C-DN01 (10M)
        assertEquals("C-DN01", results.get(2).getCinemaCode());
        assertEquals(3, results.get(2).getRank());

        // Total = 110M. SG share = 70/110 * 100 = 63.64%
        assertEquals(new BigDecimal("63.64"), results.get(0).getRevenueSharePercent());
    }

    @Test
    @DisplayName("Performance tiers correctly categorize Top Tier, Above Average and Below Average")
    void testPerformanceTiers() {
        List<CinemaMetricDTO> mockCinemas = new ArrayList<>();
        mockCinemas.add(CinemaMetricDTO.builder().cinemaId(1L).cinemaCode("C-1").active(true).build());
        mockCinemas.add(CinemaMetricDTO.builder().cinemaId(2L).cinemaCode("C-2").active(true).build());
        mockCinemas.add(CinemaMetricDTO.builder().cinemaId(3L).cinemaCode("C-3").active(true).build());

        when(dao.getBaseCinemas(any(), anyBoolean())).thenReturn(mockCinemas);
        when(dao.getRoomCounts()).thenReturn(Map.of());

        // Chain avg = (150M + 100M + 50M) / 3 = 100M
        // C-1 = 150M >= 1.2 * 100M (120M) -> TOP_TIER
        // C-2 = 100M >= 100M -> ABOVE_AVERAGE
        // C-3 = 50M < 70M (0.7 * 100M) -> UNDERPERFORMING
        BranchComparisonDAO.CinemaBookingAggregate a1 = new BranchComparisonDAO.CinemaBookingAggregate(); a1.cinemaId = 1L; a1.revenue = new BigDecimal("150000000");
        BranchComparisonDAO.CinemaBookingAggregate a2 = new BranchComparisonDAO.CinemaBookingAggregate(); a2.cinemaId = 2L; a2.revenue = new BigDecimal("100000000");
        BranchComparisonDAO.CinemaBookingAggregate a3 = new BranchComparisonDAO.CinemaBookingAggregate(); a3.cinemaId = 3L; a3.revenue = new BigDecimal("50000000");

        when(dao.getBookingAggregates(any(), any())).thenReturn(Map.of(1L, a1, 2L, a2, 3L, a3));
        when(dao.getComparisonRevenues(any(), any())).thenReturn(Map.of());
        when(dao.getShowtimesAndOccupancy(any(), any())).thenReturn(Map.of());
        when(dao.getTopMoviePerCinema(any(), any())).thenReturn(Map.of());

        ComparisonFilterDTO filter = ComparisonFilterDTO.builder()
                .from(LocalDate.of(2026, 11, 1)).to(LocalDate.of(2026, 11, 30))
                .build();

        List<CinemaMetricDTO> results = service.compareBranches(filter);
        assertEquals(CinemaMetricDTO.PerformanceTier.TOP_TIER, results.get(0).getPerformanceTier());
        assertEquals(CinemaMetricDTO.PerformanceTier.ABOVE_AVERAGE, results.get(1).getPerformanceTier());
        assertEquals(CinemaMetricDTO.PerformanceTier.UNDERPERFORMING, results.get(2).getPerformanceTier());
    }

    @Test
    @DisplayName("Chain summary calculates accurate totals, growth rate, best and worst performers")
    void testChainSummaryCalculations() {
        List<CinemaMetricDTO> metrics = new ArrayList<>();
        metrics.add(CinemaMetricDTO.builder().cinemaName("Rạp A").revenue(new BigDecimal("100000000")).comparisonRevenue(new BigDecimal("80000000")).active(true).roomsCount(5).showtimesCount(50).ticketsSold(1000).confirmedBookings(500).occupancyRate(new BigDecimal("60.0")).build());
        metrics.add(CinemaMetricDTO.builder().cinemaName("Rạp B").revenue(new BigDecimal("60000000")).comparisonRevenue(new BigDecimal("70000000")).active(true).roomsCount(3).showtimesCount(30).ticketsSold(600).confirmedBookings(300).occupancyRate(new BigDecimal("40.0")).build());

        ComparisonFilterDTO filter = ComparisonFilterDTO.builder()
                .from(LocalDate.of(2026, 11, 1)).to(LocalDate.of(2026, 11, 30))
                .build();

        ChainSummaryDTO summary = service.getChainSummary(metrics, filter);
        assertEquals(new BigDecimal("160000000"), summary.getTotalRevenue());
        assertEquals(new BigDecimal("150000000"), summary.getTotalComparisonRevenue());
        // Chain growth = (160 - 150) / 150 * 100 = 6.67%
        assertEquals(new BigDecimal("6.67"), summary.getChainGrowthPercent());
        assertEquals(2, summary.getActiveCinemasCount());
        assertEquals(8, summary.getTotalRoomsCount());
        assertEquals(1600, summary.getTotalTicketsSold());
        assertEquals(new BigDecimal("80000000"), summary.getChainAverageRevenuePerCinema());
        assertEquals(new BigDecimal("50.00"), summary.getChainAverageOccupancyRate());
        assertEquals("Rạp A", summary.getBestPerformerName());
        assertEquals("Rạp B", summary.getWorstPerformerName());
    }
}
