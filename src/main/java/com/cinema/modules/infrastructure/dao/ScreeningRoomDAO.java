package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
import com.cinema.model.SeatType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Package-Private DAO quản lý screening_rooms và seats.
 */
public class ScreeningRoomDAO {

    public List<ScreeningRoom> findByCinema(Long cinemaId) {
        List<ScreeningRoom> list = new ArrayList<>();
        String sql = "SELECT * FROM screening_rooms WHERE cinema_id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cinemaId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRoom(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ScreeningRoom findById(Long id) {
        String sql = "SELECT * FROM screening_rooms WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRoom(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isAvailable(Long roomId, LocalDateTime start, LocalDateTime end) {
        // 1. Kiểm tra xung đột với lịch bảo trì phòng chiếu
        String maintenanceSql = "SELECT COUNT(*) FROM maintenance_schedules " +
                     "WHERE screening_room_id = ? AND is_deleted = 0 " +
                     "AND NOT (end_time <= ? OR start_time >= ?)";

        // 2. Kiểm tra xung đột với các suất chiếu đang có (status != 'CANCELLED' và is_deleted = 0)
        String showtimeSql = "SELECT COUNT(*) FROM showtimes " +
                     "WHERE screening_room_id = ? AND status != 'CANCELLED' AND is_deleted = 0 " +
                     "AND NOT (end_time <= ? OR start_time >= ?)";

        try (Connection conn = DBContext.getConnection()) {
            try (PreparedStatement ps1 = conn.prepareStatement(maintenanceSql)) {
                ps1.setLong(1, roomId);
                ps1.setTimestamp(2, java.sql.Timestamp.valueOf(start));
                ps1.setTimestamp(3, java.sql.Timestamp.valueOf(end));
                try (ResultSet rs1 = ps1.executeQuery()) {
                    if (rs1.next() && rs1.getInt(1) > 0) {
                        return false; // Xung đột với lịch bảo trì
                    }
                }
            }

            try (PreparedStatement ps2 = conn.prepareStatement(showtimeSql)) {
                ps2.setLong(1, roomId);
                ps2.setTimestamp(2, java.sql.Timestamp.valueOf(start));
                ps2.setTimestamp(3, java.sql.Timestamp.valueOf(end));
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next() && rs2.getInt(1) > 0) {
                        return false; // Xung đột với suất chiếu khác trong phòng
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<Seat> findSeatsByRoom(Long roomId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT s.*, st.type_code, st.name as type_name, st.color_hex, st.surcharge " +
                     "FROM seats s " +
                     "JOIN seat_types st ON s.seat_type_id = st.id " +
                     "WHERE s.screening_room_id = ? AND s.is_deleted = 0 " +
                     "ORDER BY s.grid_row_index ASC, s.grid_col_index ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, roomId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setId(rs.getLong("id"));
                    seat.setScreeningRoomId(rs.getLong("screening_room_id"));
                    seat.setSeatTypeId(rs.getLong("seat_type_id"));
                    seat.setSeatRow(rs.getString("seat_row"));
                    seat.setSeatNumber(rs.getInt("seat_number"));
                    seat.setSeatCode(rs.getString("seat_code"));
                    seat.setGridRowIndex(rs.getInt("grid_row_index"));
                    seat.setGridColIndex(rs.getInt("grid_col_index"));
                    seat.setIsActive(rs.getBoolean("is_active"));

                    SeatType st = new SeatType();
                    st.setId(rs.getLong("seat_type_id"));
                    st.setTypeCode(rs.getString("type_code"));
                    st.setName(rs.getString("type_name"));
                    st.setColorHex(rs.getString("color_hex"));
                    st.setSurcharge(rs.getBigDecimal("surcharge"));
                    seat.setSeatType(st);

                    seats.add(seat);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    public Seat findSeatById(Long seatId) {
        String sql = "SELECT s.*, st.type_code, st.name as type_name, st.color_hex, st.surcharge " +
                     "FROM seats s " +
                     "JOIN seat_types st ON s.seat_type_id = st.id " +
                     "WHERE s.id = ? AND s.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Seat seat = new Seat();
                    seat.setId(rs.getLong("id"));
                    seat.setScreeningRoomId(rs.getLong("screening_room_id"));
                    seat.setSeatTypeId(rs.getLong("seat_type_id"));
                    seat.setSeatRow(rs.getString("seat_row"));
                    seat.setSeatNumber(rs.getInt("seat_number"));
                    seat.setSeatCode(rs.getString("seat_code"));
                    seat.setGridRowIndex(rs.getInt("grid_row_index"));
                    seat.setGridColIndex(rs.getInt("grid_col_index"));
                    seat.setIsActive(rs.getBoolean("is_active"));

                    SeatType st = new SeatType();
                    st.setId(rs.getLong("seat_type_id"));
                    st.setTypeCode(rs.getString("type_code"));
                    st.setName(rs.getString("type_name"));
                    st.setColorHex(rs.getString("color_hex"));
                    st.setSurcharge(rs.getBigDecimal("surcharge"));
                    seat.setSeatType(st);
                    return seat;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ScreeningRoom mapRoom(ResultSet rs) throws SQLException {
        ScreeningRoom r = new ScreeningRoom();
        r.setId(rs.getLong("id"));
        r.setCinemaId(rs.getLong("cinema_id"));
        r.setName(rs.getString("name"));
        r.setRoomType(rs.getString("room_type"));
        r.setTotalRows(rs.getInt("total_rows"));
        r.setTotalColumns(rs.getInt("total_columns"));
        r.setTotalCapacity(rs.getInt("total_capacity"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
