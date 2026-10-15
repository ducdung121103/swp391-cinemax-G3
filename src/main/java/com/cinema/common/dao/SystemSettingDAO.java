package com.cinema.common.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.SystemSetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DAO truy vấn và quản lý các cấu hình tham số hệ thống từ bảng system_settings (TV 1 & Common).
 */
public class SystemSettingDAO {

    public String getSettingValue(String key, String defaultValue) {
        String sql = "SELECT setting_value FROM system_settings WHERE UPPER(setting_key) = UPPER(?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String val = rs.getString("setting_value");
                    if (val != null && !val.trim().isEmpty()) {
                        return val.trim();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    public int getIntSetting(String key, int defaultValue) {
        String val = getSettingValue(key, null);
        if (val != null) {
            try {
                return Integer.parseInt(val.trim());
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }

    /**
     * Lấy danh sách cấu hình hệ thống theo nhóm (hoặc toàn bộ nếu groupName null/ALL).
     */
    public List<SystemSetting> findAllSettings(String groupName) {
        List<SystemSetting> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM system_settings ");
        List<Object> params = new ArrayList<>();

        if (groupName != null && !groupName.trim().isEmpty() && !"ALL".equalsIgnoreCase(groupName.trim())) {
            sql.append("WHERE UPPER(group_name) = UPPER(?) ");
            params.add(groupName.trim());
        }

        sql.append("ORDER BY group_name ASC, setting_key ASC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapSetting(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Thêm mới hoặc Cập nhật tham số cấu hình (Upsert).
     */
    public boolean upsertSetting(String key, String value, String description, String groupName, Integer updatedBy) {
        String sql = "MERGE INTO system_settings AS target " +
                     "USING (SELECT ? AS setting_key) AS source " +
                     "ON target.setting_key = source.setting_key " +
                     "WHEN MATCHED THEN " +
                     "    UPDATE SET setting_value = ?, description = COALESCE(?, target.description), " +
                     "               group_name = COALESCE(?, target.group_name), updated_by = ?, updated_at = GETDATE() " +
                     "WHEN NOT MATCHED THEN " +
                     "    INSERT (setting_key, setting_value, description, group_name, updated_by, updated_at) " +
                     "    VALUES (?, ?, ?, ?, ?, GETDATE());";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            String k = key.trim().toUpperCase();
            String v = value != null ? value.trim() : "";
            String g = (groupName != null && !groupName.trim().isEmpty()) ? groupName.trim().toUpperCase() : "SYSTEM";

            ps.setString(1, k);
            ps.setString(2, v);
            ps.setString(3, description);
            ps.setString(4, g);
            if (updatedBy != null) ps.setInt(5, updatedBy); else ps.setNull(5, java.sql.Types.INTEGER);

            ps.setString(6, k);
            ps.setString(7, v);
            ps.setString(8, description);
            ps.setString(9, g);
            if (updatedBy != null) ps.setInt(10, updatedBy); else ps.setNull(10, java.sql.Types.INTEGER);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật hàng loạt tham số cấu hình từ Form Admin.
     */
    public boolean batchUpdateSettings(Map<String, String> keyValues, Integer updatedBy) {
        if (keyValues == null || keyValues.isEmpty()) return true;

        String sql = "UPDATE system_settings SET setting_value = ?, updated_by = ?, updated_at = GETDATE() WHERE setting_key = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (Map.Entry<String, String> entry : keyValues.entrySet()) {
                ps.setString(1, entry.getValue() != null ? entry.getValue().trim() : "");
                if (updatedBy != null) ps.setInt(2, updatedBy); else ps.setNull(2, java.sql.Types.INTEGER);
                ps.setString(3, entry.getKey().trim());
                ps.addBatch();
            }
            ps.executeBatch();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lấy danh sách các nhóm cấu hình phân biệt.
     */
    public List<String> findAllGroups() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT group_name FROM system_settings ORDER BY group_name ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String g = rs.getString("group_name");
                if (g != null && !g.trim().isEmpty()) {
                    list.add(g.trim());
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private SystemSetting mapSetting(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("updated_at");
        return SystemSetting.builder()
                .settingKey(rs.getString("setting_key"))
                .settingValue(rs.getString("setting_value"))
                .description(rs.getString("description"))
                .groupName(rs.getString("group_name"))
                .updatedBy(rs.getObject("updated_by") != null ? rs.getInt("updated_by") : null)
                .updatedAt(ts != null ? ts.toLocalDateTime() : null)
                .build();
    }
}
