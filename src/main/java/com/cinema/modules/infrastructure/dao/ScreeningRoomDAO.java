package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import com.cinema.common.transaction.TransactionManager;
import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
import com.cinema.model.SeatType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý screening_rooms, seats và seat_types trong module infrastructure (TV 1).
 */
public class ScreeningRoomDAO {

    public List<ScreeningRoom> findByCinema(Long cinemaId) {
        List<ScreeningRoom> list = new ArrayList<>();
        String sql = "SELECT * FROM screening_rooms WHERE cinema_id = ? AND is_deleted = 0 ORDER BY id ASC";
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
        String sql = "SELECT r.*, c.name as cinema_name, c.cinema_code, c.city " +
                     "FROM screening_rooms r " +
                     "JOIN cinemas c ON r.cinema_id = c.id " +
                     "WHERE r.id = ? AND r.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ScreeningRoom r = mapRoom(rs);
                    Cinema c = new Cinema();
                    c.setId(r.getCinemaId());
                    c.setName(rs.getString("cinema_name"));
                    c.setCinemaCode(rs.getString("cinema_code"));
                    c.setCity(rs.getString("city"));
                    r.setCinema(c);
                    return r;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách tất cả phòng chiếu kèm thông tin chi nhánh Cụm rạp cho trang quản trị.
     */
    public List<ScreeningRoom> findAllRoomsWithCinema(Long cinemaId, String roomType, String status) {
        List<ScreeningRoom> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT r.*, c.name as cinema_name, c.cinema_code, c.city " +
                "FROM screening_rooms r " +
                "JOIN cinemas c ON r.cinema_id = c.id " +
                "WHERE r.is_deleted = 0 AND c.is_deleted = 0 "
        );

        List<Object> params = new ArrayList<>();

        if (cinemaId != null && cinemaId > 0) {
            sql.append("AND r.cinema_id = ? ");
            params.add(cinemaId);
        }

        if (roomType != null && !roomType.trim().isEmpty() && !"ALL".equalsIgnoreCase(roomType.trim())) {
            sql.append("AND r.room_type = ? ");
            params.add(roomType.trim());
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim())) {
            sql.append("AND r.status = ? ");
            params.add(status.trim());
        }

        sql.append("ORDER BY c.id ASC, r.id ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ScreeningRoom r = mapRoom(rs);
                    Cinema c = new Cinema();
                    c.setId(r.getCinemaId());
                    c.setName(rs.getString("cinema_name"));
                    c.setCinemaCode(rs.getString("cinema_code"));
                    c.setCity(rs.getString("city"));
                    r.setCinema(c);
                    list.add(r);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới phòng chiếu:
     * 1. Tính total_capacity = total_rows * total_columns
     * 2. INSERT phòng chiếu mới
     * 3. Tự động cập nhật total_rooms của cụm rạp
     * 4. Tự động sinh ma trận ghế mặc định (Standard, VIP, Couple) để phòng dùng được ngay!
     */
    public Long insertRoom(ScreeningRoom room) {
        int rows = (room.getTotalRows() != null && room.getTotalRows() > 0) ? room.getTotalRows() : 10;
        int cols = (room.getTotalColumns() != null && room.getTotalColumns() > 0) ? room.getTotalColumns() : 10;
        int capacity = rows * cols;
        room.setTotalRows(rows);
        room.setTotalColumns(cols);
        room.setTotalCapacity(capacity);

        return TransactionManager.executeInTransaction(conn -> {
            String insertSql = "INSERT INTO screening_rooms (cinema_id, name, room_type, total_rows, total_columns, total_capacity, status, is_deleted, created_at, updated_at) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, 0, GETDATE(), GETDATE())";

            Long newRoomId = null;
            try (PreparedStatement ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setLong(1, room.getCinemaId());
                ps.setString(2, room.getName().trim());
                ps.setString(3, room.getRoomType() != null ? room.getRoomType() : "STANDARD_2D");
                ps.setInt(4, rows);
                ps.setInt(5, cols);
                ps.setInt(6, capacity);
                ps.setString(7, room.getStatus() != null ? room.getStatus() : "ACTIVE");

                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        newRoomId = rs.getLong(1);
                    }
                }
            }

            if (newRoomId == null) {
                throw new SQLException("Không thể khởi tạo phòng chiếu mới!");
            }

            // Cập nhật lại số phòng chiếu cho Cinema
            String updateCinemaSql = "UPDATE cinemas SET total_rooms = (SELECT COUNT(*) FROM screening_rooms WHERE cinema_id = ? AND is_deleted = 0) WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateCinemaSql)) {
                ps.setLong(1, room.getCinemaId());
                ps.setLong(2, room.getCinemaId());
                ps.executeUpdate();
            }

            // Tự động sinh ma trận ghế mặc định cho phòng chiếu vừa tạo
            seedSeatsForRoom(conn, newRoomId, rows, cols, 3, 5);

            return newRoomId;
        });
    }

    /**
     * Cập nhật thông tin phòng chiếu (tên, định dạng, trạng thái).
     */
    public boolean updateRoom(ScreeningRoom room) {
        String sql = "UPDATE screening_rooms SET name = ?, room_type = ?, status = ?, updated_at = GETDATE() " +
                     "WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getName().trim());
            ps.setString(2, room.getRoomType());
            ps.setString(3, room.getStatus());
            ps.setLong(4, room.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Đổi nhanh trạng thái phòng chiếu (ACTIVE, MAINTENANCE, INACTIVE).
     */
    public boolean updateRoomStatus(Long roomId, String status) {
        String sql = "UPDATE screening_rooms SET status = ?, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean softDeleteRoom(Long roomId) {
        return TransactionManager.executeInTransaction(conn -> {
            Long cinemaId = null;
            String getCinemaSql = "SELECT cinema_id FROM screening_rooms WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(getCinemaSql)) {
                ps.setLong(1, roomId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        cinemaId = rs.getLong(1);
                    }
                }
            }

            String sql = "UPDATE screening_rooms SET is_deleted = 1, status = 'INACTIVE', updated_at = GETDATE() WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, roomId);
                ps.executeUpdate();
            }

            if (cinemaId != null) {
                String updateCinemaSql = "UPDATE cinemas SET total_rooms = (SELECT COUNT(*) FROM screening_rooms WHERE cinema_id = ? AND is_deleted = 0) WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateCinemaSql)) {
                    ps.setLong(1, cinemaId);
                    ps.setLong(2, cinemaId);
                    ps.executeUpdate();
                }
            }
            return true;
        });
    }

    // =========================================================================
    // QUẢN LÝ GHẾ & SƠ ĐỒ GHẾ (SEAT LAYOUT DESIGNER - M-04.3)
    // =========================================================================

    public List<SeatType> findAllSeatTypes() {
        List<SeatType> list = new ArrayList<>();
        String sql = "SELECT * FROM seat_types WHERE is_deleted = 0 ORDER BY id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SeatType st = new SeatType();
                st.setId(rs.getLong("id"));
                st.setTypeCode(rs.getString("type_code"));
                st.setName(rs.getString("name"));
                st.setColorHex(rs.getString("color_hex"));
                st.setSurcharge(rs.getBigDecimal("surcharge"));
                st.setDescription(rs.getString("description"));
                list.add(st);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateSeatTypeSurcharge(Long seatTypeId, BigDecimal surcharge, String colorHex) {
        String sql = "UPDATE seat_types SET surcharge = ?, color_hex = ?, updated_at = GETDATE() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, surcharge != null ? surcharge : BigDecimal.ZERO);
            ps.setString(2, colorHex != null ? colorHex.trim() : "#e50914");
            ps.setLong(3, seatTypeId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Tái tạo ma trận ghế tự động cho phòng chiếu (Xóa ghế cũ và sinh lại theo số hàng/cột mới).
     */
    public boolean regenerateRoomSeatMatrix(Long roomId, int totalRows, int totalColumns, int stdRows, int vipRows) {
        int capacity = totalRows * totalColumns;
        return TransactionManager.executeInTransaction(conn -> {
            // 1. Cập nhật thông số phòng
            String updateRoomSql = "UPDATE screening_rooms SET total_rows = ?, total_columns = ?, total_capacity = ?, updated_at = GETDATE() WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(updateRoomSql)) {
                ps.setInt(1, totalRows);
                ps.setInt(2, totalColumns);
                ps.setInt(3, capacity);
                ps.setLong(4, roomId);
                ps.executeUpdate();
            }

            // 2. Xóa toàn bộ ghế cũ của phòng
            String deleteSeatsSql = "DELETE FROM seats WHERE screening_room_id = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteSeatsSql)) {
                ps.setLong(1, roomId);
                ps.executeUpdate();
            }

            // 3. Tự động sinh lại ma trận ghế
            seedSeatsForRoom(conn, roomId, totalRows, totalColumns, stdRows, vipRows);
            return true;
        });
    }

    /**
     * Cập nhật hàng loạt ghế được chọn trên giao diện sơ đồ (đổi Loại ghế, Trạng thái ACTIVE / MAINTENANCE / BLOCKED).
     */
    public boolean updateBatchSeats(Long roomId, List<Long> seatIds, Long seatTypeId, String status, Boolean isActive) {
        if (seatIds == null || seatIds.isEmpty()) return true;

        return TransactionManager.executeInTransaction(conn -> {
            StringBuilder sql = new StringBuilder("UPDATE seats SET updated_at = GETDATE() ");
            List<Object> params = new ArrayList<>();

            if (seatTypeId != null && seatTypeId > 0) {
                sql.append(", seat_type_id = ? ");
                params.add(seatTypeId);
            }
            if (status != null && !status.trim().isEmpty()) {
                sql.append(", status = ? ");
                params.add(status.trim());
            }
            if (isActive != null) {
                sql.append(", is_active = ? ");
                params.add(isActive ? 1 : 0);
            }

            sql.append("WHERE screening_room_id = ? AND id = ?");

            try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                for (Long sId : seatIds) {
                    int pIdx = 1;
                    for (Object p : params) {
                        ps.setObject(pIdx++, p);
                    }
                    ps.setLong(pIdx++, roomId);
                    ps.setLong(pIdx, sId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            return true;
        });
    }

    /**
     * Đổi loại ghế cho toàn bộ 1 hàng (ví dụ Hàng D -> VIP, Hàng J -> COUPLE).
     */
    public boolean updateRowSeatType(Long roomId, String seatRow, Long seatTypeId) {
        String sql = "UPDATE seats SET seat_type_id = ?, updated_at = GETDATE() " +
                     "WHERE screening_room_id = ? AND seat_row = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, seatTypeId);
            ps.setLong(2, roomId);
            ps.setString(3, seatRow.trim().toUpperCase());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void seedSeatsForRoom(Connection conn, Long roomId, int totalRows, int totalCols, int stdRows, int vipRows) throws SQLException {
        String insertSeatSql = "INSERT INTO seats (screening_room_id, seat_type_id, seat_row, seat_number, seat_code, grid_row_index, grid_col_index, status, is_active, is_deleted, created_at) " +
                               "VALUES (?, ?, ?, ?, ?, ?, ?, 'ACTIVE', 1, 0, GETDATE())";

        try (PreparedStatement ps = conn.prepareStatement(insertSeatSql)) {
            for (int r = 1; r <= totalRows; r++) {
                char rowLetter = (char) ('A' + (r - 1));
                String rowStr = String.valueOf(rowLetter);

                // Phân bổ loại ghế:
                // 1 -> stdRows: Standard (1)
                // stdRows + 1 -> stdRows + vipRows: VIP (2)
                // Còn lại: Couple (3)
                long typeId = 1L; // STANDARD
                if (r > stdRows && r <= (stdRows + vipRows)) {
                    typeId = 2L; // VIP
                } else if (r > (stdRows + vipRows)) {
                    typeId = 3L; // COUPLE
                }

                for (int c = 1; c <= totalCols; c++) {
                    String seatCode = rowStr + String.format("%02d", c);

                    ps.setLong(1, roomId);
                    ps.setLong(2, typeId);
                    ps.setString(3, rowStr);
                    ps.setInt(4, c);
                    ps.setString(5, seatCode);
                    ps.setInt(6, r);
                    ps.setInt(7, c);
                    ps.addBatch();
                }
            }
            ps.executeBatch();
        }
    }

    // =========================================================================
    // CÁC HÀM CŨ GIỮ NGUYÊN HỢP ĐỒNG 100%
    // =========================================================================

    public boolean isAvailable(Long roomId, LocalDateTime start, LocalDateTime end) {
        String maintenanceSql = "SELECT COUNT(*) FROM maintenance_schedules " +
                     "WHERE screening_room_id = ? AND is_deleted = 0 " +
                     "AND NOT (end_time <= ? OR start_time >= ?)";

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
                        return false;
                    }
                }
            }

            try (PreparedStatement ps2 = conn.prepareStatement(showtimeSql)) {
                ps2.setLong(1, roomId);
                ps2.setTimestamp(2, java.sql.Timestamp.valueOf(start));
                ps2.setTimestamp(3, java.sql.Timestamp.valueOf(end));
                try (ResultSet rs2 = ps2.executeQuery()) {
                    if (rs2.next() && rs2.getInt(1) > 0) {
                        return false;
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
                    seat.setStatus(rs.getString("status"));
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
                    seat.setStatus(rs.getString("status"));
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

    public int countTotalRooms() {
        String sql = "SELECT COUNT(*) FROM screening_rooms WHERE is_deleted = 0 AND status = 'ACTIVE'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countTotalSeats() {
        String sql = "SELECT COUNT(*) FROM seats WHERE is_deleted = 0 AND is_active = 1 AND status = 'ACTIVE'";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
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
