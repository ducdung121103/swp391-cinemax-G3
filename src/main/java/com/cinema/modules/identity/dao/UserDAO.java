package com.cinema.modules.identity.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Role;
import com.cinema.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * DAO quản lý người dùng và vai trò trong module identity (TV 2).
 */
public class UserDAO {

    public User findByEmail(String email) {
        String sql = "SELECT u.*, r.role_name, r.description as role_desc " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.email = ? AND u.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findById(Long id) {
        String sql = "SELECT u.*, r.role_name, r.description as role_desc " +
                     "FROM users u " +
                     "JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.id = ? AND u.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Long insertCustomer(User user) {
        String sql = "INSERT INTO users (role_id, email, password_hash, full_name, phone, loyalty_points, status) " +
                     "VALUES ((SELECT id FROM roles WHERE role_name = 'CUSTOMER'), ?, ?, ?, ?, 0, 'ACTIVE')";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getPhone());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updatePassword(Long userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ? WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setRoleId(rs.getLong("role_id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setLoyaltyPoints(rs.getInt("loyalty_points"));
        u.setStatus(rs.getString("status"));
        u.setBranchId(rs.getObject("branch_id") != null ? rs.getLong("branch_id") : null);
        u.setTierId(rs.getObject("tier_id") != null ? rs.getLong("tier_id") : null);
        u.setAvatarUrl(rs.getString("avatar_url"));

        Role r = new Role();
        r.setId(rs.getLong("role_id"));
        r.setRoleName(rs.getString("role_name"));
        r.setDescription(rs.getString("role_desc"));
        u.setRole(r);

        return u;
    }
}
