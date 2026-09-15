package com.cinema.modules.identity;

import com.cinema.model.MembershipTier;
import com.cinema.modules.identity.service.LoyaltyService;
import com.cinema.modules.identity.service.impl.LoyaltyServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Unit Tests for LoyaltyService (TV 2 Membership & Loyalty)")
class LoyaltyServiceTest {

    private final LoyaltyService loyaltyService = new LoyaltyServiceImpl() {
        @Override
        public MembershipTier getTierByUserId(Long userId) {
            if (userId == 100L) {
                MembershipTier tier = new MembershipTier();
                tier.setId(2L);
                tier.setTierName("VIP_SILVER");
                tier.setDiscountPercent(5);
                tier.setPointRate(new BigDecimal("1.20"));
                return tier;
            } else if (userId == 200L) {
                MembershipTier tier = new MembershipTier();
                tier.setId(3L);
                tier.setTierName("VIP_GOLD");
                tier.setDiscountPercent(10);
                tier.setPointRate(new BigDecimal("1.50"));
                return tier;
            } else if (userId == 300L) {
                MembershipTier tier = new MembershipTier();
                tier.setId(4L);
                tier.setTierName("DIAMOND");
                tier.setDiscountPercent(15);
                tier.setPointRate(new BigDecimal("2.00"));
                return tier;
            }
            return null; // Khách chưa có hạng
        }
    };

    @Test
    @DisplayName("Should compute 5% discount for VIP_SILVER member")
    void testSilverTierDiscount() {
        BigDecimal orderTotal = new BigDecimal("200000.00");
        BigDecimal discount = loyaltyService.calculateDiscount(100L, orderTotal);

        assertEquals(new BigDecimal("10000.00"), discount);
    }

    @Test
    @DisplayName("Should compute 10% discount for VIP_GOLD member")
    void testGoldTierDiscount() {
        BigDecimal orderTotal = new BigDecimal("200000.00");
        BigDecimal discount = loyaltyService.calculateDiscount(200L, orderTotal);

        assertEquals(new BigDecimal("20000.00"), discount);
    }

    @Test
    @DisplayName("Should compute 15% discount for DIAMOND member")
    void testDiamondTierDiscount() {
        BigDecimal orderTotal = new BigDecimal("300000.00");
        BigDecimal discount = loyaltyService.calculateDiscount(300L, orderTotal);

        assertEquals(new BigDecimal("45000.00"), discount);
    }

    @Test
    @DisplayName("Should return zero discount for regular customer or non-existent tier")
    void testNoTierDiscount() {
        BigDecimal orderTotal = new BigDecimal("200000.00");
        BigDecimal discount = loyaltyService.calculateDiscount(999L, orderTotal);

        assertEquals(new BigDecimal("0.00"), discount);
    }
}
