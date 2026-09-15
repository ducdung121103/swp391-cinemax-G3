package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.ScreeningHall;
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
 * Package-Private DAO quản lý screening_halls và seats.
 */
public class ScreeningHallDAO {

    public List<ScreeningHall> findByBranch(Long branchId) {
        List<ScreeningHall> list = new ArrayList<>();
        String sql = "SELECT * FROM screening_halls WHERE branch_id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, branchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapHall(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public ScreeningHall findById(Long id) {
        String sql = "SELECT * FROM screening_halls WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapHall(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isAvailable(Long hallId, LocalDateTime start, LocalDateTime end) {
        // Kiểm tra xem phòng có đang trong lịch bảo trì không
        String sql = "SELECT COUNT(*) FROM maintenance_schedules " +
                     "WHERE screening_hall_id = ? AND is_deleted = 0 " +
                     "AND NOT (end_time <= ? OR start_time >= ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, hallId);
            ps.setString(2, start.toString());
            ps.setString(3, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public List<Seat> findSeatsByHall(Long hallId) {
        List<Seat> seats = new ArrayList<>();
        String sql = "SELECT s.*, st.type_code, st.name as type_name, st.color_hex, st.surcharge " +
                     "FROM seats s " +
                     "JOIN seat_types st ON s.seat_type_id = st.id " +
                     "WHERE s.screening_hall_id = ? AND s.is_deleted = 0 " +
                     "ORDER BY s.grid_row_index ASC, s.grid_col_index ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, hallId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat();
                    seat.setId(rs.getLong("id"));
                    seat.setScreeningHallId(rs.getLong("screening_hall_id"));
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

    private ScreeningHall mapHall(ResultSet rs) throws SQLException {
        ScreeningHall h = new ScreeningHall();
        h.setId(rs.getLong("id"));
        h.setBranchId(rs.getLong("branch_id"));
        h.setName(rs.getString("name"));
        h.setHallType(rs.getString("hall_type"));
        h.setTotalRows(rs.getInt("total_rows"));
        h.setTotalColumns(rs.getInt("total_columns"));
        h.setTotalCapacity(rs.getInt("total_capacity"));
        h.setStatus(rs.getString("status"));
        return h;
    }
}
