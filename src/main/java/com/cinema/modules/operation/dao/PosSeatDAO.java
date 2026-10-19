package com.cinema.modules.operation.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Loads the complete room map and the sale state of each seat for a showtime. */
public class PosSeatDAO {
    public List<Seat> findForShowtime(long showtimeId, long roomId, String sessionId) throws SQLException {
        String sql = "SELECT s.id, s.screening_room_id, s.seat_type_id, s.seat_row, s.seat_number, s.seat_code, "
                + "s.grid_row_index, s.grid_col_index, "
                + "CASE WHEN s.is_active = 0 OR s.status <> 'ACTIVE' THEN 'UNAVAILABLE' "
                + "WHEN EXISTS (SELECT 1 FROM tickets t WHERE t.showtime_id = ? AND t.seat_id = s.id) THEN 'SOLD' "
                + "WHEN EXISTS (SELECT 1 FROM seat_holdings h WHERE h.showtime_id = ? AND h.seat_id = s.id "
                + "AND h.session_id <> ? "
                + "AND h.expires_at > GETDATE()) THEN 'HELD' ELSE 'AVAILABLE' END AS sale_status "
                + "FROM seats s WHERE s.screening_room_id = ? AND s.is_deleted = 0 "
                + "ORDER BY s.grid_row_index, s.grid_col_index, s.seat_row, s.seat_number";
        List<Seat> seats = new ArrayList<>();
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, showtimeId);
            statement.setLong(2, showtimeId);
            statement.setString(3, sessionId);
            statement.setLong(4, roomId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Seat seat = new Seat();
                    seat.setId(result.getLong("id"));
                    seat.setScreeningRoomId(result.getLong("screening_room_id"));
                    seat.setSeatTypeId(result.getLong("seat_type_id"));
                    seat.setSeatRow(result.getString("seat_row"));
                    seat.setSeatNumber(result.getInt("seat_number"));
                    seat.setSeatCode(result.getString("seat_code"));
                    seat.setGridRowIndex(result.getInt("grid_row_index"));
                    seat.setGridColIndex(result.getInt("grid_col_index"));
                    seat.setStatus(result.getString("sale_status"));
                    seats.add(seat);
                }
            }
        }
        return seats;
    }
}
