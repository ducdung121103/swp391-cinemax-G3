package com.cinema.common.dao;

import com.cinema.common.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO truy vấn các cấu hình tham số hệ thống từ bảng system_settings.
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
                return Integer.parseInt(val);
            } catch (NumberFormatException ignored) {}
        }
        return defaultValue;
    }
}
