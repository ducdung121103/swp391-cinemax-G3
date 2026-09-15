package com.cinema.modules.identity.service;

import com.cinema.model.MembershipTier;

import java.math.BigDecimal;

/**
 * Public Service Interface quản lý điểm thưởng và hạng hội viên (TV 2).
 * TV 4 sẽ gọi qua Interface này để kiểm tra ưu đãi và cộng/trừ điểm khi mua vé.
 */
public interface LoyaltyService {
    MembershipTier getTierByUserId(Long userId);
    int getUserPoints(Long userId);
    boolean addPoints(Long userId, Long bookingId, int points, String description);
    boolean deductPoints(Long userId, Long bookingId, int points, String description);
    BigDecimal calculateDiscount(Long userId, BigDecimal orderTotal);
}
