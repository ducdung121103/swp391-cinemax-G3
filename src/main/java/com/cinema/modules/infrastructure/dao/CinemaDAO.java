package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Cinema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý bảng cinemas trong module infrastructure (TV 1).
 * Tuân thủ quy tắc kiến trúc: chỉ module infrastructure mới được truy xuất DAO này!
 */
public class CinemaDAO {

    public List<Cinema> findAll() {
        List<Cinema> list = new ArrayList<>();
        String sql = "SELECT * FROM cinemas WHERE is_deleted = 0 AND is_active = 1 ORDER BY id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Cinema findById(Long id) {
        String sql = "SELECT * FROM cinemas WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy danh sách tất cả cụm rạp cho trang quản trị Admin có hỗ trợ tìm kiếm và lọc.
     * Cập nhật đếm số phòng chiếu thực tế từ bảng screening_rooms.
     */
    public List<Cinema> findAllForAdmin(String keyword, String city, Boolean isActive) {
        List<Cinema> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT c.*, " +
                "(SELECT COUNT(*) FROM screening_rooms r WHERE r.cinema_id = c.id AND r.is_deleted = 0) AS actual_rooms " +
                "FROM cinemas c WHERE c.is_deleted = 0 "
        );

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND (LOWER(c.name) LIKE ? OR LOWER(c.cinema_code) LIKE ? OR LOWER(c.address) LIKE ?) ");
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (city != null && !city.trim().isEmpty() && !"ALL".equalsIgnoreCase(city.trim())) {
            sql.append("AND LOWER(c.city) = LOWER(?) ");
            params.add(city.trim());
        }

        if (isActive != null) {
            sql.append("AND c.is_active = ? ");
            params.add(isActive ? 1 : 0);
        }

        sql.append("ORDER BY c.id DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Cinema c = mapRow(rs);
                    int actualRooms = rs.getInt("actual_rooms");
                    if (actualRooms > 0 || c.getTotalRooms() == null || c.getTotalRooms() == 0) {
                        c.setTotalRooms(actualRooms);
                    }
                    list.add(c);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới cụm rạp (kiểm tra trùng mã rạp).
     */
    public Long insert(Cinema cinema) throws SQLException {
        if (existsByCode(cinema.getCinemaCode())) {
            throw new IllegalArgumentException("Mã cụm rạp '" + cinema.getCinemaCode() + "' đã tồn tại trên hệ thống!");
        }

        String sql = "INSERT INTO cinemas (cinema_code, name, address, city, phone, email, total_rooms, is_active, is_deleted, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0, GETDATE(), GETDATE())";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, cinema.getCinemaCode().trim().toUpperCase());
            ps.setString(2, cinema.getName().trim());
            ps.setString(3, cinema.getAddress() != null ? cinema.getAddress().trim() : "");
            ps.setString(4, cinema.getCity() != null ? cinema.getCity().trim() : "Hà Nội");
            ps.setString(5, cinema.getPhone() != null ? cinema.getPhone().trim() : "");
            ps.setString(6, cinema.getEmail() != null ? cinema.getEmail().trim() : "");
            ps.setInt(7, cinema.getTotalRooms() != null ? cinema.getTotalRooms() : 0);
            ps.setBoolean(8, cinema.getIsActive() != null ? cinema.getIsActive() : true);

            int affected = ps.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Cập nhật thông tin cụm rạp.
     */
    public boolean update(Cinema cinema) throws SQLException {
        if (existsByCodeExcludingId(cinema.getCinemaCode(), cinema.getId())) {
            throw new IllegalArgumentException("Mã cụm rạp '" + cinema.getCinemaCode() + "' đã thuộc về rạp khác!");
        }

        String sql = "UPDATE cinemas SET cinema_code = ?, name = ?, address = ?, city = ?, phone = ?, email = ?, " +
                     "is_active = ?, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, cinema.getCinemaCode().trim().toUpperCase());
            ps.setString(2, cinema.getName().trim());
            ps.setString(3, cinema.getAddress() != null ? cinema.getAddress().trim() : "");
            ps.setString(4, cinema.getCity() != null ? cinema.getCity().trim() : "");
            ps.setString(5, cinema.getPhone() != null ? cinema.getPhone().trim() : "");
            ps.setString(6, cinema.getEmail() != null ? cinema.getEmail().trim() : "");
            ps.setBoolean(7, cinema.getIsActive() != null ? cinema.getIsActive() : true);
            ps.setLong(8, cinema.getId());

            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Bật / Tắt trạng thái hoạt động của rạp.
     */
    public boolean toggleActiveStatus(Long id, boolean isActive) {
        String sql = "UPDATE cinemas SET is_active = ?, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, isActive);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa mềm (Soft Delete) cụm rạp.
     */
    public boolean softDelete(Long id) {
        String sql = "UPDATE cinemas SET is_deleted = 1, is_active = 0, updated_at = GETDATE() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean existsByCode(String cinemaCode) {
        if (cinemaCode == null || cinemaCode.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM cinemas WHERE UPPER(cinema_code) = UPPER(?) AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cinemaCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean existsByCodeExcludingId(String cinemaCode, Long id) {
        if (cinemaCode == null || cinemaCode.trim().isEmpty() || id == null) return false;
        String sql = "SELECT COUNT(*) FROM cinemas WHERE UPPER(cinema_code) = UPPER(?) AND id != ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cinemaCode.trim());
            ps.setLong(2, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Lấy danh sách thành phố duy nhất có rạp để hiển thị trên bộ lọc.
     */
    public List<String> findAllCities() {
        List<String> cities = new ArrayList<>();
        String sql = "SELECT DISTINCT city FROM cinemas WHERE is_deleted = 0 AND city IS NOT NULL ORDER BY city ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String c = rs.getString("city");
                if (c != null && !c.trim().isEmpty()) {
                    cities.add(c.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cities;
    }

    public int countTotalCinemas() {
        String sql = "SELECT COUNT(*) FROM cinemas WHERE is_deleted = 0 AND is_active = 1";
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

    private Cinema mapRow(ResultSet rs) throws SQLException {
        Cinema c = new Cinema();
        c.setId(rs.getLong("id"));
        c.setCinemaCode(rs.getString("cinema_code"));
        c.setName(rs.getString("name"));
        c.setAddress(rs.getString("address"));
        c.setCity(rs.getString("city"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setTotalRooms(rs.getInt("total_rooms"));
        c.setIsActive(rs.getBoolean("is_active"));
        c.setIsDeleted(rs.getBoolean("is_deleted"));
        return c;
    }
}
