package com.cinema.modules.booking.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * DAO quản lý bảng order_items (TV 4).
 * Chuẩn 3NF tinh giản: Chỉ lưu các sản phẩm Bắp Nước F&B mua kèm trong đơn hàng.
 */
public class OrderItemDAO {

    public void insertFnbOrderItems(Connection conn, Long bookingId, Map<Long, Integer> fnbItems) throws SQLException {
        if (fnbItems == null || fnbItems.isEmpty()) return;

        String queryItemSql = "SELECT name, price FROM fnb_items WHERE id = ?";
        String insertSql = "INSERT INTO order_items (booking_id, fnb_item_id, item_name, quantity, unit_price, subtotal) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement psQuery = conn.prepareStatement(queryItemSql);
             PreparedStatement psInsert = conn.prepareStatement(insertSql)) {

            for (Map.Entry<Long, Integer> entry : fnbItems.entrySet()) {
                Long fnbId = entry.getKey();
                int qty = entry.getValue();

                psQuery.setLong(1, fnbId);
                try (ResultSet rs = psQuery.executeQuery()) {
                    if (rs.next()) {
                        String name = rs.getString("name");
                        BigDecimal price = rs.getBigDecimal("price");
                        BigDecimal subtotal = price.multiply(BigDecimal.valueOf(qty));

                        psInsert.setLong(1, bookingId);
                        psInsert.setLong(2, fnbId);
                        psInsert.setString(3, name);
                        psInsert.setInt(4, qty);
                        psInsert.setBigDecimal(5, price);
                        psInsert.setBigDecimal(6, subtotal);
                        psInsert.addBatch();
                    }
                }
            }
            psInsert.executeBatch();
        }
    }
}
