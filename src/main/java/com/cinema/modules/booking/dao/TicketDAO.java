package com.cinema.modules.booking.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import com.cinema.model.Ticket;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

/**
 * DAO quản lý bảng tickets (TV 4).
 * Chứa ràng buộc uk_showtime_seat ngăn chặn 100% việc bán trùng ghế ở tầng CSDL.
 */
public class TicketDAO {

    public void insertTickets(Connection conn, Long bookingId, Long showtimeId, List<Long> seatIds, List<BigDecimal> prices) throws SQLException {
        String sql = "INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status) " +
                     "VALUES (?, ?, ?, ?, ?, 'VALID')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < seatIds.size(); i++) {
                Long seatId = seatIds.get(i);
                BigDecimal price = (prices != null && i < prices.size()) ? prices.get(i) : new BigDecimal("85000.00");
                String barcode = "TK-" + showtimeId + "-" + seatId + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                ps.setLong(1, bookingId);
                ps.setLong(2, showtimeId);
                ps.setLong(3, seatId);
                ps.setString(4, barcode);
                ps.setBigDecimal(5, price);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Ticket findByBarcode(String barcode) {
        String sql = "SELECT t.*, s.seat_code, s.seat_row, s.seat_number " +
                     "FROM tickets t " +
                     "JOIN seats s ON t.seat_id = s.id " +
                     "WHERE t.barcode = ? AND t.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, barcode);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Ticket t = new Ticket();
                    t.setId(rs.getLong("id"));
                    t.setBookingId(rs.getLong("booking_id"));
                    t.setShowtimeId(rs.getLong("showtime_id"));
                    t.setSeatId(rs.getLong("seat_id"));
                    t.setBarcode(rs.getString("barcode"));
                    t.setTicketPrice(rs.getBigDecimal("ticket_price"));
                    t.setStatus(rs.getString("status"));

                    Seat s = new Seat();
                    s.setId(rs.getLong("seat_id"));
                    s.setSeatCode(rs.getString("seat_code"));
                    s.setSeatRow(rs.getString("seat_row"));
                    s.setSeatNumber(rs.getInt("seat_number"));
                    t.setSeat(s);

                    return t;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateStatus(Connection conn, Long ticketId, String status) throws SQLException {
        String sql = "UPDATE tickets SET status = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, ticketId);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean updateStatus(Long ticketId, String status) {
        String sql = "UPDATE tickets SET status = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, ticketId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
