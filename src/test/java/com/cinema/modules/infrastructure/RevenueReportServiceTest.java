package com.cinema.modules.infrastructure;

import com.cinema.modules.infrastructure.dao.RevenueReportDAO;
import com.cinema.modules.infrastructure.dto.RevenueFilterDTO;
import com.cinema.modules.infrastructure.dto.RevenueKpiDTO;
import com.cinema.modules.infrastructure.service.impl.RevenueReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for RevenueReportService business logic and validation rules (M-10.1).
 */
@ExtendWith(MockitoExtension.class)
class RevenueReportServiceTest {

    @Mock
    private RevenueReportDAO revenueReportDAO;

    private RevenueReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new RevenueReportServiceImpl(revenueReportDAO);
    }

    @Test
    @DisplayName("Should resolve default filter to current month when dates are omitted")
    void testResolveAndValidateFilterDefaults() {
        RevenueFilterDTO filter = service.resolveAndValidateFilter(null, null, null, null, null);

        assertNotNull(filter);
        LocalDate now = LocalDate.now();
        assertEquals(now.withDayOfMonth(1), filter.getFrom());
        assertEquals(now, filter.getTo());
        assertNull(filter.getCinemaId());
        assertEquals(1, filter.getPage());
        assertEquals(20, filter.getPageSize());
    }

    @Test
    @DisplayName("Should successfully parse valid custom date range and parameters")
    void testResolveAndValidateFilterCustomValid() {
        RevenueFilterDTO filter = service.resolveAndValidateFilter("2026-10-01", "2026-10-31", "2", "2", "50");

        assertNotNull(filter);
        assertEquals(LocalDate.of(2026, 10, 1), filter.getFrom());
        assertEquals(LocalDate.of(2026, 10, 31), filter.getTo());
        assertEquals(2L, filter.getCinemaId());
        assertEquals(2, filter.getPage());
        assertEquals(50, filter.getPageSize());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when from date is after to date")
    void testResolveAndValidateFilterFromAfterTo() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.resolveAndValidateFilter("2026-11-15", "2026-11-01", null, null, null));

        assertTrue(ex.getMessage().contains("Ngày bắt đầu không được lớn hơn ngày kết thúc"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when date range exceeds 366 days")
    void testResolveAndValidateFilterRangeTooLarge() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                service.resolveAndValidateFilter("2025-01-01", "2026-01-15", null, null, null));

        assertTrue(ex.getMessage().contains("không được vượt quá 366 ngày"));
    }

    @Test
    @DisplayName("Should retrieve revenue KPIs for current filter period")
    void testGetRevenueKpis() {
        LocalDate from = LocalDate.of(2026, 10, 11);
        LocalDate to = LocalDate.of(2026, 10, 20);
        RevenueFilterDTO filter = RevenueFilterDTO.builder()
                .from(from)
                .to(to)
                .cinemaId(2L)
                .build();

        RevenueKpiDTO expectedKpi = RevenueKpiDTO.builder()
                .totalRevenue(BigDecimal.valueOf(1200000))
                .totalConfirmedBookings(12)
                .totalTicketsSold(24)
                .build();

        when(revenueReportDAO.getKpiMetrics(eq(from), eq(to), eq(2L))).thenReturn(expectedKpi);

        RevenueKpiDTO result = service.getRevenueKpis(filter);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(1200000), result.getTotalRevenue());
        assertEquals(12, result.getTotalConfirmedBookings());
        assertEquals(24, result.getTotalTicketsSold());

        verify(revenueReportDAO).getKpiMetrics(eq(from), eq(to), eq(2L));
    }
}
