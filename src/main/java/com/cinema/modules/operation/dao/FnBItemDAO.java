package com.cinema.modules.operation.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.FnBCategory;
import com.cinema.model.FnBItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý danh mục sản phẩm bắp nước (TV 5).
 */
public class FnBItemDAO {

    public List<FnBItem> findAllActive() {
        List<FnBItem> list = new ArrayList<>();
        String sql = "SELECT f.*, c.name as category_name " +
                     "FROM fnb_items f " +
                     "JOIN fnb_categories c ON f.category_id = c.id " +
                     "WHERE f.is_active = 1 AND f.is_deleted = 0 ORDER BY f.id ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FnBItem item = new FnBItem();
                item.setId(rs.getLong("id"));
                item.setCategoryId(rs.getLong("category_id"));
                item.setItemCode(rs.getString("item_code"));
                item.setName(rs.getString("name"));
                item.setPrice(rs.getBigDecimal("price"));
                item.setImageUrl(rs.getString("image_url"));
                item.setIsCombo(rs.getBoolean("is_combo"));

                FnBCategory cat = new FnBCategory();
                cat.setId(rs.getLong("category_id"));
                cat.setName(rs.getString("category_name"));
                item.setCategory(cat);

                list.add(item);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
