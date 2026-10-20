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

/** Database access for F&B items and categories. */
public class FnBCatalogDAO {
    private static final String FIND_ITEMS_SQL = "SELECT f.*, c.name AS category_name "
            + "FROM fnb_items f JOIN fnb_categories c ON c.id = f.category_id "
            + "WHERE f.is_deleted = 0 ORDER BY f.id DESC";
    private static final String FIND_CATEGORIES_SQL = "SELECT * FROM fnb_categories "
            + "WHERE is_deleted = 0 ORDER BY name";
    private static final String INSERT_ITEM_SQL = "INSERT INTO fnb_items "
            + "(category_id, item_code, name, price, image_url, is_combo) VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_ITEM_SQL = "UPDATE fnb_items SET category_id = ?, item_code = ?, name = ?, "
            + "price = ?, image_url = ?, is_combo = ?, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";
    private static final String INSERT_CATEGORY_SQL = "INSERT INTO fnb_categories (name, description) VALUES (?, ?)";
    private static final String UPDATE_CATEGORY_SQL = "UPDATE fnb_categories SET name = ?, description = ?, "
            + "updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";

    public List<FnBItem> findItems() throws SQLException {
        List<FnBItem> items = new ArrayList<>();
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ITEMS_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                items.add(mapItem(result));
            }
        }
        return items;
    }

    public List<FnBCategory> findCategories() throws SQLException {
        List<FnBCategory> categories = new ArrayList<>();
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_CATEGORIES_SQL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                categories.add(mapCategory(result));
            }
        }
        return categories;
    }

    public void saveItem(FnBItem item) throws SQLException {
        boolean insert = item.getId() == null;
        String sql = insert ? INSERT_ITEM_SQL : UPDATE_ITEM_SQL;
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, item.getCategoryId());
            statement.setString(2, item.getItemCode());
            statement.setString(3, item.getName());
            statement.setBigDecimal(4, item.getPrice());
            statement.setString(5, item.getImageUrl());
            statement.setBoolean(6, Boolean.TRUE.equals(item.getIsCombo()));
            if (!insert) {
                statement.setLong(7, item.getId());
            }
            statement.executeUpdate();
        }
    }

    public void setItemActive(long itemId, boolean active) throws SQLException {
        updateStatus("UPDATE fnb_items SET is_active = ?, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0",
                itemId, active);
    }

    public void saveCategory(FnBCategory category) throws SQLException {
        boolean insert = category.getId() == null;
        String sql = insert ? INSERT_CATEGORY_SQL : UPDATE_CATEGORY_SQL;
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, category.getName());
            statement.setString(2, category.getDescription());
            if (!insert) {
                statement.setLong(3, category.getId());
            }
            statement.executeUpdate();
        }
    }

    public void setCategoryActive(long categoryId, boolean active) throws SQLException {
        updateStatus("UPDATE fnb_categories SET is_active = ?, updated_at = GETDATE() "
                + "WHERE id = ? AND is_deleted = 0", categoryId, active);
    }

    private void updateStatus(String sql, long id, boolean active) throws SQLException {
        try (Connection connection = DBContext.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, active);
            statement.setLong(2, id);
            statement.executeUpdate();
        }
    }

    private FnBItem mapItem(ResultSet result) throws SQLException {
        FnBItem item = new FnBItem();
        item.setId(result.getLong("id"));
        item.setCategoryId(result.getLong("category_id"));
        item.setItemCode(result.getString("item_code"));
        item.setName(result.getString("name"));
        item.setPrice(result.getBigDecimal("price"));
        item.setImageUrl(result.getString("image_url"));
        item.setIsCombo(result.getBoolean("is_combo"));
        item.setIsActive(result.getBoolean("is_active"));

        FnBCategory category = new FnBCategory();
        category.setId(item.getCategoryId());
        category.setName(result.getString("category_name"));
        item.setCategory(category);
        return item;
    }

    private FnBCategory mapCategory(ResultSet result) throws SQLException {
        FnBCategory category = new FnBCategory();
        category.setId(result.getLong("id"));
        category.setName(result.getString("name"));
        category.setDescription(result.getString("description"));
        category.setIsActive(result.getBoolean("is_active"));
        return category;
    }
}
