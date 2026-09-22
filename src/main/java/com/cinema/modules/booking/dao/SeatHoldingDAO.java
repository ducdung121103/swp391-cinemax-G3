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
        // Cú pháp SQL Server: dùng GETDATE() thay cho NOW()
        String sql = "DELETE FROM seat_holdings WHERE expires_at <= GETDATE()";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    /**
     * Tầng 2: Giữ ghế với cơ chế Upsert an toàn chống race-condition.
     * Housekeeping protocol chống lỗi Duplicate Key.
     * Đã chuyển đổi sang MERGE WITH (HOLDLOCK) trong Microsoft SQL Server để đảm bảo tính nguyên tử (atomic),
     * tuyệt đối ngăn chặn 2 session đồng thời đặt trùng 1 ghế còn hạn (chống double-booking).
     */
    public boolean holdSeat(Connection conn, Long showtimeId, Long seatId, String sessionId) throws SQLException {
        cleanExpiredHoldings(conn);

        String sql = "MERGE INTO seat_holdings WITH (HOLDLOCK) AS target " +
                     "USING (SELECT ? AS showtime_id, ? AS seat_id, ? AS session_id) AS source " +
                     "ON target.showtime_id = source.showtime_id AND target.seat_id = source.seat_id " +
                     "WHEN MATCHED AND target.expires_at <= GETDATE() THEN " +
                     "    UPDATE SET session_id = source.session_id, " +
                     "               held_at = GETDATE(), " +
                     "               expires_at = DATEADD(MINUTE, 5, GETDATE()) " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (showtime_id, seat_id, session_id, held_at, expires_at) " +
                     "    VALUES (source.showtime_id, source.seat_id, source.session_id, GETDATE(), DATEADD(MINUTE, 5, GETDATE()));";
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
