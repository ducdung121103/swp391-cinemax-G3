package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Cinema;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý bảng cinemas.
 * Nằm trong module infrastructure, tuyệt đối cấm các module khác import!
 */
public class CinemaDAO {

    public List<Cinema> findAll() {
        List<Cinema> list = new ArrayList<>();
        String sql = "SELECT * FROM cinemas WHERE is_deleted = 0 ORDER BY id ASC";
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
