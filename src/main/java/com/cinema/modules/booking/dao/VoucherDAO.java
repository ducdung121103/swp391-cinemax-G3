package com.cinema.modules.booking.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Voucher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO quản lý bảng vouchers (TV 4).
 */
public class VoucherDAO {

    public Voucher findById(Connection conn, Long voucherId) {
        String sql = "SELECT * FROM vouchers WHERE id = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, voucherId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVoucher(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Voucher findByCode(String code) {
        String sql = "SELECT * FROM vouchers WHERE UPPER(code) = UPPER(?) AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapVoucher(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean incrementUsedCount(Connection conn, Long voucherId) {
        String sql = "UPDATE vouchers SET used_count = used_count + 1, updated_at = GETDATE() " +
                     "WHERE id = ? AND used_count < usage_limit AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, voucherId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Voucher mapVoucher(ResultSet rs) throws SQLException {
        Voucher v = new Voucher();
        v.setId(rs.getLong("id"));
        v.setCode(rs.getString("code"));
        v.setDiscountType(rs.getString("discount_type"));
        v.setDiscountVal(rs.getBigDecimal("discount_val"));
        v.setMinOrderAmount(rs.getBigDecimal("min_order_amount"));
        v.setMaxDiscount(rs.getBigDecimal("max_discount"));
        v.setUsageLimit(rs.getInt("usage_limit"));
        v.setUsedCount(rs.getInt("used_count"));
        if (rs.getTimestamp("valid_from") != null) {
            v.setValidFrom(rs.getTimestamp("valid_from").toLocalDateTime());
        }
        if (rs.getTimestamp("valid_to") != null) {
            v.setValidTo(rs.getTimestamp("valid_to").toLocalDateTime());
        }
        v.setIsActive(rs.getBoolean("is_active"));
        return v;
    }
}
