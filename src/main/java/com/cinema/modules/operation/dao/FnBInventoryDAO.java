package com.cinema.modules.operation.dao;

import com.cinema.common.context.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Database access for per-cinema F&B stock. */
public class FnBInventoryDAO {
    public List<Map<String, Object>> findInventory(long cinemaId) throws SQLException {
        String sql = "SELECT f.id, f.item_code, f.name, f.price, f.image_url, f.is_combo, "
                + "COALESCE(i.stock_quantity, 0) stock_quantity, "
                + "COALESCE(i.warning_threshold, 10) warning_threshold "
                + "FROM fnb_items f LEFT JOIN cinema_inventories i "
                + "ON i.fnb_item_id = f.id AND i.cinema_id = ? "
                + "WHERE f.is_deleted = 0 ORDER BY f.name";
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cinemaId);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("id", result.getLong("id"));
                    row.put("code", result.getString("item_code"));
                    row.put("name", result.getString("name"));
                    row.put("price", result.getBigDecimal("price"));
                    row.put("imageUrl", result.getString("image_url"));
                    row.put("stock", result.getInt("stock_quantity"));
                    row.put("threshold", result.getInt("warning_threshold"));
                    rows.add(row);
                }
            }
        }
        return rows;
    }

    public void save(long cinemaId, long itemId, int quantity, int threshold) throws SQLException {
        if (quantity < 0 || threshold < 0) {
            throw new IllegalArgumentException("Tồn kho và ngưỡng cảnh báo phải >= 0.");
        }
        String sql = "MERGE cinema_inventories WITH (HOLDLOCK) AS target "
                + "USING (SELECT ? cinema_id, ? fnb_item_id) AS source "
                + "ON target.cinema_id = source.cinema_id AND target.fnb_item_id = source.fnb_item_id "
                + "WHEN MATCHED THEN UPDATE SET stock_quantity = ?, warning_threshold = ?, updated_at = GETDATE() "
                + "WHEN NOT MATCHED THEN INSERT(cinema_id, fnb_item_id, stock_quantity, warning_threshold) "
                + "VALUES(?, ?, ?, ?);";
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, cinemaId);
            statement.setLong(2, itemId);
            statement.setInt(3, quantity);
            statement.setInt(4, threshold);
            statement.setLong(5, cinemaId);
            statement.setLong(6, itemId);
            statement.setInt(7, quantity);
            statement.setInt(8, threshold);
            statement.executeUpdate();
        }
    }
}
