package com.cinema.modules.identity.service.impl;

import com.cinema.common.context.DBContext;
import com.cinema.common.transaction.TransactionManager;
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
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 1. Khóa dòng và đọc điểm hiện tại của user để đảm bảo tính nguyên tử
                String lockSql = "SELECT loyalty_points FROM users WITH (UPDLOCK, ROWLOCK) WHERE id = ?";
                int currentPoints = 0;
                try (PreparedStatement psLock = conn.prepareStatement(lockSql)) {
                    psLock.setLong(1, userId);
                    try (ResultSet rs = psLock.executeQuery()) {
                        if (!rs.next()) {
                            return false; // User không tồn tại
                        }
                        currentPoints = rs.getInt("loyalty_points");
                    }
                }

                int balanceAfter = currentPoints + points;

                // 2. Cập nhật số dư điểm mới
                String updateSql = "UPDATE users SET loyalty_points = ?, updated_at = GETDATE() WHERE id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setInt(1, balanceAfter);
                    psUpdate.setLong(2, userId);
                    psUpdate.executeUpdate();
                }

                // 3. Ghi log lịch sử biến động điểm theo đúng schema point_histories
                String logSql = "INSERT INTO point_histories (user_id, booking_id, points_change, balance_after, transaction_type, reason) VALUES (?, ?, ?, ?, 'EARN', ?)";
                try (PreparedStatement psLog = conn.prepareStatement(logSql)) {
                    psLog.setLong(1, userId);
                    if (bookingId != null) psLog.setLong(2, bookingId); else psLog.setNull(2, java.sql.Types.BIGINT);
                    psLog.setInt(3, points);
                    psLog.setInt(4, balanceAfter);
                    psLog.setString(5, description);
                    psLog.executeUpdate();
                }

                return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deductPoints(Long userId, Long bookingId, int points, String description) {
        try {
            return TransactionManager.executeInTransaction(conn -> {
                // 1. Khóa dòng và đọc điểm hiện tại của user
                String lockSql = "SELECT loyalty_points FROM users WITH (UPDLOCK, ROWLOCK) WHERE id = ?";
                int currentPoints = 0;
                try (PreparedStatement psLock = conn.prepareStatement(lockSql)) {
                    psLock.setLong(1, userId);
                    try (ResultSet rs = psLock.executeQuery()) {
                        if (!rs.next()) {
                            return false; // User không tồn tại
                        }
                        currentPoints = rs.getInt("loyalty_points");
                    }
                }

                if (currentPoints < points) {
                    return false; // Không đủ điểm để trừ
                }

                int balanceAfter = currentPoints - points;

                // 2. Cập nhật số dư điểm mới
                String updateSql = "UPDATE users SET loyalty_points = ?, updated_at = GETDATE() WHERE id = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                    psUpdate.setInt(1, balanceAfter);
                    psUpdate.setLong(2, userId);
                    psUpdate.executeUpdate();
                }

                // 3. Ghi log lịch sử biến động điểm theo đúng schema point_histories
                String logSql = "INSERT INTO point_histories (user_id, booking_id, points_change, balance_after, transaction_type, reason) VALUES (?, ?, ?, ?, 'REDEEM', ?)";
                try (PreparedStatement psLog = conn.prepareStatement(logSql)) {
                    psLog.setLong(1, userId);
                    if (bookingId != null) psLog.setLong(2, bookingId); else psLog.setNull(2, java.sql.Types.BIGINT);
                    psLog.setInt(3, -points);
                    psLog.setInt(4, balanceAfter);
                    psLog.setString(5, description);
                    psLog.executeUpdate();
                }

                return true;
            });
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
