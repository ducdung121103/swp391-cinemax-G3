package com.cinema.modules.booking.dao;

import com.cinema.common.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * DAO quản lý khóa ghế tạm thời (Bảng seat_holdings - TV 4).
 * Triển khai đầy đủ Housekeeping protocol chống lỗi Duplicate Key.
 */
public class SeatHoldingDAO {

    /**
     * Tầng 1: Xóa toàn bộ các bản ghi giữ ghế đã hết hạn trên toàn hệ thống.
     */
    public void cleanExpiredHoldings(Connection conn) throws SQLException {
        String sql = "DELETE FROM seat_holdings WHERE expires_at <= NOW()";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    /**
     * Tầng 2: Giữ ghế với cơ chế Upsert an toàn chống race-condition.
     */
    public boolean holdSeat(Connection conn, Long showtimeId, Long seatId, String sessionId) throws SQLException {
        cleanExpiredHoldings(conn);

        String sql = "INSERT INTO seat_holdings (showtime_id, seat_id, session_id, held_at, expires_at) " +
                     "VALUES (?, ?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 5 MINUTE)) " +
                     "ON DUPLICATE KEY UPDATE " +
                     "session_id = IF(expires_at <= NOW(), VALUES(session_id), session_id), " +
                     "held_at = IF(expires_at <= NOW(), VALUES(held_at), held_at), " +
                     "expires_at = IF(expires_at <= NOW(), VALUES(expires_at), expires_at)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, showtimeId);
            ps.setLong(2, seatId);
            ps.setString(3, sessionId);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa bản ghi giữ ghế khi đã mua thành công hoặc khi khách hủy.
     */
    public void releaseHoldings(Connection conn, Long showtimeId, List<Long> seatIds) throws SQLException {
        if (seatIds == null || seatIds.isEmpty()) return;
        String sql = "DELETE FROM seat_holdings WHERE showtime_id = ? AND seat_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (Long seatId : seatIds) {
                ps.setLong(1, showtimeId);
                ps.setLong(2, seatId);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
