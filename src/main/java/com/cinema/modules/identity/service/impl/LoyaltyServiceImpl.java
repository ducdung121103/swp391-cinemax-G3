package com.cinema.modules.identity.service.impl;

import com.cinema.common.context.DBContext;
import com.cinema.model.MembershipTier;
import com.cinema.modules.identity.service.LoyaltyService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Cài đặt nghiệp vụ tính điểm và ưu đãi hạng hội viên (TV 2).
 */
public class LoyaltyServiceImpl implements LoyaltyService {

    @Override
    public MembershipTier getTierByUserId(Long userId) {
        String sql = "SELECT mt.* FROM membership_tiers mt " +
                     "JOIN users u ON u.tier_id = mt.id " +
                     "WHERE u.id = ? AND mt.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    MembershipTier tier = new MembershipTier();
                    tier.setId(rs.getLong("id"));
                    tier.setTierName(rs.getString("tier_name"));
                    tier.setMinSpent(rs.getBigDecimal("min_spent"));
                    tier.setDiscountPercent(rs.getInt("discount_percent"));
                    tier.setPointRate(rs.getBigDecimal("point_rate"));
                    return tier;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int getUserPoints(Long userId) {
        String sql = "SELECT loyalty_points FROM users WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public boolean addPoints(Long userId, Long bookingId, int points, String description) {
        String updateSql = "UPDATE users SET loyalty_points = loyalty_points + ?, updated_at = GETDATE() WHERE id = ?";
        String logSql = "INSERT INTO point_histories (user_id, booking_id, points, type, description) VALUES (?, ?, ?, 'EARNED', ?)";
        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(updateSql);
                 PreparedStatement ps2 = conn.prepareStatement(logSql)) {
                ps1.setInt(1, points);
                ps1.setLong(2, userId);
                ps1.executeUpdate();

                ps2.setLong(1, userId);
                if (bookingId != null) ps2.setLong(2, bookingId); else ps2.setNull(2, java.sql.Types.BIGINT);
                ps2.setInt(3, points);
                ps2.setString(4, description);
                ps2.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deductPoints(Long userId, Long bookingId, int points, String description) {
        String updateSql = "UPDATE users SET loyalty_points = loyalty_points - ?, updated_at = GETDATE() WHERE id = ? AND loyalty_points >= ?";
        String logSql = "INSERT INTO point_histories (user_id, booking_id, points, type, description) VALUES (?, ?, ?, 'REDEEMED', ?)";
        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(updateSql);
                 PreparedStatement ps2 = conn.prepareStatement(logSql)) {
                ps1.setInt(1, points);
                ps1.setLong(2, userId);
                ps1.setInt(3, points);
                int rows = ps1.executeUpdate();
                if (rows > 0) {
                    ps2.setLong(1, userId);
                    if (bookingId != null) ps2.setLong(2, bookingId); else ps2.setNull(2, java.sql.Types.BIGINT);
                    ps2.setInt(3, -points);
                    ps2.setString(4, description);
                    ps2.executeUpdate();
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public BigDecimal calculateDiscount(Long userId, BigDecimal orderTotal) {
        MembershipTier tier = getTierByUserId(userId);
        if (tier != null && tier.getDiscountPercent() > 0 && orderTotal != null) {
            BigDecimal rate = BigDecimal.valueOf(tier.getDiscountPercent()).divide(BigDecimal.valueOf(100));
            return orderTotal.multiply(rate).setScale(2, java.math.RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
    }
}
