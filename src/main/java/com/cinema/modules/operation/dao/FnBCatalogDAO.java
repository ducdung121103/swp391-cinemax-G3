package com.cinema.modules.operation.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.FnBCategory;
import com.cinema.model.FnBItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FnBCatalogDAO {
    public List<FnBItem> findItems() throws SQLException {
        String sql = "SELECT f.*, c.name AS category_name FROM fnb_items f JOIN fnb_categories c ON c.id=f.category_id WHERE f.is_deleted=0 ORDER BY f.id DESC";
        List<FnBItem> items = new ArrayList<>();
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                FnBItem i = new FnBItem(); i.setId(rs.getLong("id")); i.setCategoryId(rs.getLong("category_id"));
                i.setItemCode(rs.getString("item_code")); i.setName(rs.getString("name")); i.setPrice(rs.getBigDecimal("price"));
                i.setImageUrl(rs.getString("image_url")); i.setIsCombo(rs.getBoolean("is_combo")); i.setIsActive(rs.getBoolean("is_active"));
                FnBCategory cat = new FnBCategory(); cat.setId(i.getCategoryId()); cat.setName(rs.getString("category_name")); i.setCategory(cat); items.add(i);
            }
        }
        return items;
    }

    public List<FnBCategory> findCategories() throws SQLException {
        List<FnBCategory> categories = new ArrayList<>();
        try (Connection c = DBContext.getConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM fnb_categories WHERE is_deleted=0 ORDER BY name"); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { FnBCategory x = new FnBCategory(); x.setId(rs.getLong("id")); x.setName(rs.getString("name")); x.setDescription(rs.getString("description")); x.setIsActive(rs.getBoolean("is_active")); categories.add(x); }
        }
        return categories;
    }

    public void saveItem(FnBItem i) throws SQLException {
        String sql = i.getId() == null ? "INSERT INTO fnb_items(category_id,item_code,name,price,image_url,is_combo) VALUES(?,?,?,?,?,?)" : "UPDATE fnb_items SET category_id=?,item_code=?,name=?,price=?,image_url=?,is_combo=?,updated_at=GETDATE() WHERE id=? AND is_deleted=0";
        try (Connection c=DBContext.getConnection(); PreparedStatement ps=c.prepareStatement(sql)) {
            ps.setLong(1,i.getCategoryId()); ps.setString(2,i.getItemCode()); ps.setString(3,i.getName()); ps.setBigDecimal(4,i.getPrice()); ps.setString(5,i.getImageUrl()); ps.setBoolean(6,Boolean.TRUE.equals(i.getIsCombo()));
            if(i.getId()!=null) ps.setLong(7,i.getId()); ps.executeUpdate();
        }
    }
    public void setItemActive(long id, boolean active) throws SQLException { updateStatus("UPDATE fnb_items SET is_active=?,updated_at=GETDATE() WHERE id=? AND is_deleted=0", id, active); }
    public void saveCategory(FnBCategory x) throws SQLException {
        String sql=x.getId()==null?"INSERT INTO fnb_categories(name,description) VALUES(?,?)":"UPDATE fnb_categories SET name=?,description=?,updated_at=GETDATE() WHERE id=? AND is_deleted=0";
        try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setString(1,x.getName());ps.setString(2,x.getDescription());if(x.getId()!=null)ps.setLong(3,x.getId());ps.executeUpdate();}
    }
    public void setCategoryActive(long id, boolean active) throws SQLException { updateStatus("UPDATE fnb_categories SET is_active=?,updated_at=GETDATE() WHERE id=? AND is_deleted=0", id, active); }
    private void updateStatus(String sql,long id,boolean active)throws SQLException{try(Connection c=DBContext.getConnection();PreparedStatement ps=c.prepareStatement(sql)){ps.setBoolean(1,active);ps.setLong(2,id);ps.executeUpdate();}}
}
