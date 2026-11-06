package com.cinema.modules.identity.dao;

import com.cinema.common.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Ghi thông báo trong ứng dụng. Một cặp type + reference chỉ được một dòng.
 */
public class NotificationDAO {

    public boolean exists(String type, String referenceId) {
        String sql = "SELECT COUNT(*) FROM notifications WHERE type = ? AND reference_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, type);
            ps.setString(2, referenceId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insertIfAbsent(Long userId, String type, String referenceId, String title, String message) {
        String sql = "INSERT INTO notifications (user_id, title, message, type, reference_id) " +
                     "SELECT ?, ?, ?, ?, ? WHERE NOT EXISTS (" +
                     "SELECT 1 FROM notifications WHERE type = ? AND reference_id = ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setString(2, title);
            ps.setString(3, message);
            ps.setString(4, type);
            ps.setString(5, referenceId);
            ps.setString(6, type);
            ps.setString(7, referenceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
