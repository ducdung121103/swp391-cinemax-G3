package com.cinema.modules.catalog.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.TicketPricing;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * DAO quản lý bảng ticket_pricings (TV 3).
 */
public class TicketPricingDAO {

    public BigDecimal findBasePrice(LocalDateTime showTime, String format) {
        DayOfWeek dow = showTime.getDayOfWeek();
        String dayType = (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) ? "WEEKEND" : "WEEKDAY";
        int hour = showTime.getHour();
        String timeSlot = (hour < 12) ? "EARLY" : (hour < 17 ? "STANDARD" : "PRIME");

        // Cú pháp SQL Server: dùng SELECT TOP 1 thay vì LIMIT 1
        String sql = "SELECT TOP 1 base_price FROM ticket_pricings " +
                     "WHERE day_type = ? AND time_slot = ? AND experience_format = ? AND is_deleted = 0 " +
                     "ORDER BY id DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dayType);
            ps.setString(2, timeSlot);
            ps.setString(3, format);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("base_price");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Giá mặc định nếu chưa cấu hình
        return new BigDecimal("85000.00");
    }

    public BigDecimal findSeatTypeSurcharge(Long seatTypeId) {
        if (seatTypeId == null) return BigDecimal.ZERO;
        String sql = "SELECT surcharge FROM seat_types WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatTypeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal surcharge = rs.getBigDecimal("surcharge");
                    return surcharge != null ? surcharge : BigDecimal.ZERO;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}
