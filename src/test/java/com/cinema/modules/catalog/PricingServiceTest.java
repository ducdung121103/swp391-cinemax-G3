package com.cinema.modules.catalog;

import com.cinema.modules.catalog.dao.TicketPricingDAO;
import com.cinema.modules.catalog.service.PricingService;
import com.cinema.modules.catalog.service.impl.PricingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for PricingService (TV 3 Dynamic Pricing)")
class PricingServiceTest {

    @Mock
    private TicketPricingDAO pricingDAO;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingServiceImpl(pricingDAO);
    }

    @Test
    @DisplayName("Should return standard base price for standard seat")
    void testStandardSeatPricing() {
        LocalDateTime time = LocalDateTime.of(2024, 10, 25, 14, 0);
        when(pricingDAO.findBasePrice(any(), eq("2D"))).thenReturn(new BigDecimal("85000.00"));
        when(pricingDAO.findSeatTypeSurcharge(1L)).thenReturn(BigDecimal.ZERO);

        BigDecimal price = pricingService.calculateTicketPrice(1L, 1L, time, "2D");
        assertEquals(new BigDecimal("85000.00"), price);
    }

    @Test
    @DisplayName("Should add 15,000 VND surcharge for VIP seat")
    void testVipSeatPricing() {
        LocalDateTime time = LocalDateTime.of(2024, 10, 25, 19, 30);
        when(pricingDAO.findBasePrice(any(), eq("2D"))).thenReturn(new BigDecimal("95000.00"));
        when(pricingDAO.findSeatTypeSurcharge(2L)).thenReturn(new BigDecimal("15000.00"));

        BigDecimal price = pricingService.calculateTicketPrice(1L, 2L, time, "2D");
        assertEquals(new BigDecimal("110000.00"), price);
    }

    @Test
    @DisplayName("Should add 40,000 VND surcharge for Sweetbox Couple seat")
    void testCoupleSeatPricing() {
        LocalDateTime time = LocalDateTime.of(2024, 10, 26, 20, 0);
        when(pricingDAO.findBasePrice(any(), eq("2D"))).thenReturn(new BigDecimal("105000.00"));
        when(pricingDAO.findSeatTypeSurcharge(3L)).thenReturn(new BigDecimal("40000.00"));

        BigDecimal price = pricingService.calculateTicketPrice(1L, 3L, time, "2D");
        assertEquals(new BigDecimal("145000.00"), price);
    }

    @Test
    @DisplayName("Should compute 3D IMAX price with VIP surcharge correctly")
    void testImax3DVipPricing() {
        LocalDateTime time = LocalDateTime.of(2024, 10, 26, 20, 0);
        when(pricingDAO.findBasePrice(any(), eq("3D"))).thenReturn(new BigDecimal("150000.00"));
        when(pricingDAO.findSeatTypeSurcharge(2L)).thenReturn(new BigDecimal("15000.00"));

        BigDecimal price = pricingService.calculateTicketPrice(2L, 2L, time, "3D");
        assertEquals(new BigDecimal("165000.00"), price);
    }
}
