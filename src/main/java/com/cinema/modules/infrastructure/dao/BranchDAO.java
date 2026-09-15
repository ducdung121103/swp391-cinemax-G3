package com.cinema.modules.infrastructure.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Branch;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý bảng branches.
 * Nằm trong module infrastructure, tuyệt đối cấm các module khác import!
 */
public class BranchDAO {

    public List<Branch> findAll() {
        List<Branch> list = new ArrayList<>();
        String sql = "SELECT * FROM branches WHERE is_deleted = 0 ORDER BY id ASC";
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

    public Branch findById(Long id) {
        String sql = "SELECT * FROM branches WHERE id = ? AND is_deleted = 0";
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

    private Branch mapRow(ResultSet rs) throws SQLException {
        Branch b = new Branch();
        b.setId(rs.getLong("id"));
        b.setBranchCode(rs.getString("branch_code"));
        b.setName(rs.getString("name"));
        b.setAddress(rs.getString("address"));
        b.setCity(rs.getString("city"));
        b.setPhone(rs.getString("phone"));
        b.setEmail(rs.getString("email"));
        b.setTotalHalls(rs.getInt("total_halls"));
        b.setIsActive(rs.getBoolean("is_active"));
        b.setIsDeleted(rs.getBoolean("is_deleted"));
        return b;
    }
}
