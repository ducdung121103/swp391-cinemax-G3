package com.cinema.modules.booking.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import com.cinema.model.Ticket;
import com.cinema.modules.booking.dto.TicketReceipt;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * DAO quản lý bảng tickets (TV 4).
 * Chứa ràng buộc uk_showtime_seat ngăn chặn 100% việc bán trùng ghế ở tầng CSDL.
 */
public class TicketDAO {

    /**
     * Rechecks seat eligibility inside the booking transaction. The row and key-range
     * locks serialize a POS sale with online holds and other counter sales.
     */
    public void assertSeatsAvailable(Connection conn, Long showtimeId, List<Long> seatIds,
                                     String sessionId) throws SQLException {
        if (showtimeId == null || seatIds == null || seatIds.isEmpty()) {
            throw new IllegalArgumentException("Chọn suất chiếu và ít nhất một ghế.");
        }

        Set<Long> uniqueSeatIds = new HashSet<>();
        String seatSql = "SELECT s.seat_code FROM seats s WITH (UPDLOCK, HOLDLOCK) " +
                         "JOIN showtimes st WITH (UPDLOCK, HOLDLOCK) " +
                         "ON st.screening_room_id = s.screening_room_id " +
                         "JOIN screening_rooms sr WITH (UPDLOCK, HOLDLOCK) ON sr.id = st.screening_room_id " +
                         "JOIN cinemas c WITH (HOLDLOCK) ON c.id = sr.cinema_id " +
                         "WHERE s.id = ? AND st.id = ? AND s.is_deleted = 0 AND s.is_active = 1 " +
                         "AND s.status = 'ACTIVE' AND st.is_deleted = 0 " +
                         "AND st.status IN ('SCHEDULED', 'OPENING') AND st.start_time > GETDATE() " +
                         "AND sr.is_deleted = 0 AND sr.status = 'ACTIVE' AND c.is_deleted = 0 AND c.is_active = 1";
        String ticketSql = "SELECT 1 FROM tickets WITH (UPDLOCK, HOLDLOCK) " +
                           "WHERE showtime_id = ? AND seat_id = ?";
        String holdingSql = "SELECT session_id FROM seat_holdings WITH (UPDLOCK, HOLDLOCK) " +
                            "WHERE showtime_id = ? AND seat_id = ? AND expires_at > GETDATE()";

        try (PreparedStatement seatStatement = conn.prepareStatement(seatSql);
             PreparedStatement ticketStatement = conn.prepareStatement(ticketSql);
             PreparedStatement holdingStatement = conn.prepareStatement(holdingSql)) {
            for (Long seatId : seatIds) {
                if (seatId == null || !uniqueSeatIds.add(seatId)) {
                    throw new IllegalArgumentException("Danh sách ghế không hợp lệ hoặc bị trùng.");
                }

                seatStatement.setLong(1, seatId);
                seatStatement.setLong(2, showtimeId);
                String seatCode;
                try (ResultSet rs = seatStatement.executeQuery()) {
                    if (!rs.next()) {
                        throw new IllegalArgumentException("Ghế không thuộc phòng của suất chiếu hoặc không còn được bán.");
                    }
                    seatCode = rs.getString("seat_code");
                }

                ticketStatement.setLong(1, showtimeId);
                ticketStatement.setLong(2, seatId);
                try (ResultSet rs = ticketStatement.executeQuery()) {
                    if (rs.next()) {
                        throw new IllegalStateException("Ghế " + seatCode + " vừa được bán. Tải lại sơ đồ ghế.");
                    }
                }

                holdingStatement.setLong(1, showtimeId);
                holdingStatement.setLong(2, seatId);
                try (ResultSet rs = holdingStatement.executeQuery()) {
                    if (rs.next() && (sessionId == null || !sessionId.equals(rs.getString("session_id")))) {
                        throw new IllegalStateException("Ghế " + seatCode + " đang được khách khác giữ.");
                    }
                }
            }
        }
    }

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

    public List<TicketReceipt> findReceiptItems(Connection conn, Long bookingId) throws SQLException {
        String sql = "SELECT s.seat_code, t.barcode, t.ticket_price " +
                     "FROM tickets t JOIN seats s ON s.id = t.seat_id " +
                     "WHERE t.booking_id = ? ORDER BY s.seat_row, s.seat_number";
        List<TicketReceipt> items = new java.util.ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(TicketReceipt.builder()
                            .seatCode(rs.getString("seat_code"))
                            .barcode(rs.getString("barcode"))
                            .ticketPrice(rs.getBigDecimal("ticket_price"))
                            .build());
                }
            }
        }
        return items;
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
