package com.cinema.modules.booking.dao;

import com.cinema.model.Booking;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

/**
 * DAO quản lý bảng bookings (TV 4).
 */
public class BookingDAO {

    public Long insertBooking(Connection conn, Booking b) throws SQLException {
        if (b.getBookingCode() == null || b.getBookingCode().isBlank()) {
            String code = "BK-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            b.setBookingCode(code);
        }

        String sql = "INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, " +
                     "total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getBookingCode());
            if (b.getUserId() != null) ps.setLong(2, b.getUserId()); else ps.setNull(2, java.sql.Types.BIGINT);
            if (b.getStaffId() != null) ps.setLong(3, b.getStaffId()); else ps.setNull(3, java.sql.Types.BIGINT);
            ps.setLong(4, b.getShowtimeId());
            if (b.getVoucherId() != null) ps.setLong(5, b.getVoucherId()); else ps.setNull(5, java.sql.Types.BIGINT);
            ps.setString(6, b.getChannel() != null ? b.getChannel() : "ONLINE");
            ps.setBigDecimal(7, b.getTotalTicketsAmount());
            ps.setBigDecimal(8, b.getTotalFnbAmount());
            ps.setBigDecimal(9, b.getDiscountAmount());
            ps.setBigDecimal(10, b.getFinalAmount());
            ps.setString(11, b.getStatus() != null ? b.getStatus() : "CONFIRMED");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        }
        return null;
    }

    public Booking findByCode(Connection conn, String bookingCode) throws SQLException {
        String sql = "SELECT * FROM bookings WHERE booking_code = ? AND is_deleted = 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, bookingCode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Booking.builder()
                            .bookingCode(rs.getString("booking_code"))
                            .userId(rs.getObject("user_id") != null ? rs.getLong("user_id") : null)
                            .staffId(rs.getObject("staff_id") != null ? rs.getLong("staff_id") : null)
                            .showtimeId(rs.getLong("showtime_id"))
                            .voucherId(rs.getObject("voucher_id") != null ? rs.getLong("voucher_id") : null)
                            .channel(rs.getString("channel"))
                            .totalTicketsAmount(rs.getBigDecimal("total_tickets_amount"))
                            .totalFnbAmount(rs.getBigDecimal("total_fnb_amount"))
                            .discountAmount(rs.getBigDecimal("discount_amount"))
                            .finalAmount(rs.getBigDecimal("final_amount"))
                            .status(rs.getString("status"))
                            .build();
                }
            }
        }
        return null;
    }

    public boolean updateStatusByCode(Connection conn, String bookingCode, String status) throws SQLException {
        String sql = "UPDATE bookings SET status = ?, updated_at = GETDATE() WHERE booking_code = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, bookingCode);
            return ps.executeUpdate() > 0;
        }
    }
}
