package com.cinema.modules.operation.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Seat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Loads seats that are not sold or temporarily held for a showtime. */
public class PosSeatDAO {
    public List<Seat> findAvailable(long showtimeId, long roomId) throws SQLException {
        String sql = "SELECT s.id, s.screening_room_id, s.seat_type_id, s.seat_row, s.seat_number, s.seat_code "
                + "FROM seats s WHERE s.screening_room_id = ? AND s.is_deleted = 0 "
                + "AND NOT EXISTS (SELECT 1 FROM tickets t WHERE t.showtime_id = ? AND t.seat_id = s.id "
                + "AND t.is_deleted = 0 AND t.status NOT IN ('CANCELLED', 'REFUNDED')) "
                + "AND NOT EXISTS (SELECT 1 FROM seat_holdings h WHERE h.showtime_id = ? "
                + "AND h.seat_id = s.id AND h.expires_at > GETDATE()) "
                + "ORDER BY s.seat_row, s.seat_number";
        List<Seat> seats = new ArrayList<>();
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, roomId);
            statement.setLong(2, showtimeId);
            statement.setLong(3, showtimeId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Seat seat = new Seat();
                    seat.setId(result.getLong("id"));
                    seat.setScreeningRoomId(result.getLong("screening_room_id"));
                    seat.setSeatTypeId(result.getLong("seat_type_id"));
                    seat.setSeatRow(result.getString("seat_row"));
                    seat.setSeatNumber(result.getInt("seat_number"));
                    seat.setSeatCode(result.getString("seat_code"));
                    seats.add(seat);
                }
            }
        }
        return seats;
    }
}
