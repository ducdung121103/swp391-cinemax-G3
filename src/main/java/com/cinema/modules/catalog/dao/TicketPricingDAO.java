package com.cinema.modules.catalog.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.TicketPricing;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
                     "WHERE day_type = ? AND time_slot = ? AND experience_format = ? " +
                     "AND cinema_id IS NULL AND is_deleted = 0 " +
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

    public List<TicketPricing> findAllActive() {
        String sql = "SELECT p.*, c.name AS cinema_name, s.name AS seat_type_name " +
                     "FROM ticket_pricings p " +
                     "LEFT JOIN cinemas c ON p.cinema_id = c.id " +
                     "LEFT JOIN seat_types s ON p.seat_type_id = s.id " +
                     "WHERE p.is_deleted = 0 ORDER BY p.id DESC";
        List<TicketPricing> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapPricing(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public TicketPricing findById(Long id) {
        String sql = "SELECT p.*, c.name AS cinema_name, s.name AS seat_type_name " +
                     "FROM ticket_pricings p " +
                     "LEFT JOIN cinemas c ON p.cinema_id = c.id " +
                     "LEFT JOIN seat_types s ON p.seat_type_id = s.id " +
                     "WHERE p.id = ? AND p.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapPricing(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(TicketPricing pricing) {
        String sql = "INSERT INTO ticket_pricings (cinema_id, day_type, time_slot, experience_format, seat_type_id, base_price, effective_from, effective_to) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindWrite(ps, pricing);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(TicketPricing pricing) {
        String sql = "UPDATE ticket_pricings SET cinema_id = ?, day_type = ?, time_slot = ?, experience_format = ?, " +
                     "seat_type_id = ?, base_price = ?, effective_from = ?, effective_to = ?, updated_at = GETDATE() " +
                     "WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bindWrite(ps, pricing);
            ps.setLong(9, pricing.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean softDelete(Long id) {
        String sql = "UPDATE ticket_pricings SET is_deleted = 1, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void bindWrite(PreparedStatement ps, TicketPricing pricing) throws SQLException {
        if (pricing.getCinemaId() == null) ps.setNull(1, java.sql.Types.INTEGER);
        else ps.setLong(1, pricing.getCinemaId());
        ps.setString(2, pricing.getDayType());
        ps.setString(3, pricing.getTimeSlot());
        ps.setString(4, pricing.getExperienceFormat());
        ps.setLong(5, pricing.getSeatTypeId() == null ? 1L : pricing.getSeatTypeId());
        ps.setBigDecimal(6, pricing.getBasePrice());
        ps.setDate(7, Date.valueOf(pricing.getEffectiveFrom()));
        if (pricing.getEffectiveTo() == null) ps.setNull(8, java.sql.Types.DATE);
        else ps.setDate(8, Date.valueOf(pricing.getEffectiveTo()));
    }

    private TicketPricing mapPricing(ResultSet rs) throws SQLException {
        TicketPricing pricing = new TicketPricing();
        pricing.setId(rs.getLong("id"));
        long cinemaId = rs.getLong("cinema_id");
        pricing.setCinemaId(rs.wasNull() ? null : cinemaId);
        pricing.setDayType(rs.getString("day_type"));
        pricing.setTimeSlot(rs.getString("time_slot"));
        pricing.setExperienceFormat(rs.getString("experience_format"));
        pricing.setSeatTypeId(rs.getLong("seat_type_id"));
        pricing.setBasePrice(rs.getBigDecimal("base_price"));
        if (rs.getDate("effective_from") != null) pricing.setEffectiveFrom(rs.getDate("effective_from").toLocalDate());
        if (rs.getDate("effective_to") != null) pricing.setEffectiveTo(rs.getDate("effective_to").toLocalDate());
        pricing.setCinemaName(rs.getString("cinema_name"));
        pricing.setSeatTypeName(rs.getString("seat_type_name"));
        return pricing;
    }
}
