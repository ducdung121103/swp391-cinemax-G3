package com.cinema.modules.booking;

import com.cinema.model.Voucher;
import com.cinema.modules.booking.dao.VoucherDAO;
import com.cinema.modules.booking.service.VoucherService;
import com.cinema.modules.booking.service.impl.VoucherServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for VoucherService (TV 4 Promotion & Vouchers)")
class VoucherServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private VoucherDAO voucherDAO;

    private VoucherService voucherService;

    @BeforeEach
    void setUp() {
        voucherService = new VoucherServiceImpl(voucherDAO);
    }

    @Test
    @DisplayName("Should correctly calculate percentage discount capped at maxDiscount")
    void testCalculatePercentageDiscountWithMaxCap() {
        Voucher voucher = Voucher.builder()
                .code("SUMMER20")
                .discountType("PERCENT")
                .discountVal(new BigDecimal("20.00")) // 20%
                .minOrderAmount(new BigDecimal("100000.00"))
                .maxDiscount(new BigDecimal("30000.00")) // Cap at 30k
                .usageLimit(100)
                .usedCount(10)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .build();
        voucher.setIsActive(true);

        when(voucherDAO.findById(any(), eq(1L))).thenReturn(voucher);

        // 200,000 * 20% = 40,000 > maxDiscount 30,000 -> Should return 30,000
        BigDecimal discount = voucherService.calculateAndValidateDiscount(mockConnection, 1L, new BigDecimal("200000.00"));
        assertEquals(new BigDecimal("30000.00"), discount);
    }

    @Test
    @DisplayName("Should correctly apply fixed amount discount")
    void testCalculateFixedAmountDiscount() {
        Voucher voucher = Voucher.builder()
                .code("GIAM50K")
                .discountType("FIXED_AMOUNT")
                .discountVal(new BigDecimal("50000.00"))
                .minOrderAmount(new BigDecimal("100000.00"))
                .usageLimit(50)
                .usedCount(5)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .build();
        voucher.setIsActive(true);

        when(voucherDAO.findById(any(), eq(2L))).thenReturn(voucher);

        BigDecimal discount = voucherService.calculateAndValidateDiscount(mockConnection, 2L, new BigDecimal("150000.00"));
        assertEquals(new BigDecimal("50000.00"), discount);
    }

    @Test
    @DisplayName("Should throw IllegalStateException when order amount is less than minOrderAmount")
    void testRejectMinOrderAmountNotMet() {
        Voucher voucher = Voucher.builder()
                .code("VIPMIN")
                .discountType("PERCENT")
                .discountVal(new BigDecimal("10.00"))
                .minOrderAmount(new BigDecimal("200000.00"))
                .usageLimit(100)
                .usedCount(0)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .build();
        voucher.setIsActive(true);

        when(voucherDAO.findById(any(), eq(3L))).thenReturn(voucher);

        assertThrows(IllegalStateException.class, () ->
                voucherService.calculateAndValidateDiscount(mockConnection, 3L, new BigDecimal("150000.00")));
    }

    @Test
    @DisplayName("Should throw IllegalStateException when voucher has exceeded usage limit")
    void testRejectExceededUsageLimit() {
        Voucher voucher = Voucher.builder()
                .code("LIMITED")
                .discountType("FIXED_AMOUNT")
                .discountVal(new BigDecimal("20000.00"))
                .minOrderAmount(BigDecimal.ZERO)
                .usageLimit(10)
                .usedCount(10) // Limit reached
                .validFrom(LocalDateTime.now().minusDays(1))
                .validTo(LocalDateTime.now().plusDays(1))
                .build();
        voucher.setIsActive(true);

        when(voucherDAO.findById(any(), eq(4L))).thenReturn(voucher);

        assertThrows(IllegalStateException.class, () ->
                voucherService.calculateAndValidateDiscount(mockConnection, 4L, new BigDecimal("100000.00")));
    }
}
