package com.cinema.modules.operation.service.impl;

import com.cinema.model.FnBCategory;
import com.cinema.model.FnBItem;
import com.cinema.modules.operation.dao.FnBCatalogDAO;
import com.cinema.modules.operation.service.FnBCatalogService;

import java.sql.SQLException;
import java.util.List;

public class FnBCatalogServiceImpl implements FnBCatalogService {
    private final FnBCatalogDAO catalogDAO;

    public FnBCatalogServiceImpl() {
        this(new FnBCatalogDAO());
    }

    public FnBCatalogServiceImpl(FnBCatalogDAO catalogDAO) {
        this.catalogDAO = catalogDAO;
    }

    @Override
    public List<FnBItem> getItems() throws SQLException {
        return catalogDAO.findItems();
    }

    @Override
    public List<FnBCategory> getCategories() throws SQLException {
        return catalogDAO.findCategories();
    }

    @Override
    public void saveItem(FnBItem item) throws SQLException {
        validateItem(item);
        item.setItemCode(item.getItemCode().trim());
        item.setName(item.getName().trim());
        item.setImageUrl(blankToNull(item.getImageUrl()));
        save(() -> catalogDAO.saveItem(item));
    }

    @Override
    public void setItemActive(long itemId, boolean active) throws SQLException {
        requirePositive(itemId, "Mã sản phẩm không hợp lệ.");
        save(() -> catalogDAO.setItemActive(itemId, active));
    }

    @Override
    public void saveCategory(FnBCategory category) throws SQLException {
        if (category == null) {
            throw new IllegalArgumentException("Thiếu thông tin danh mục.");
        }
        if (category.getId() != null) {
            requirePositive(category.getId(), "Mã danh mục không hợp lệ.");
        }
        if (category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập tên danh mục.");
        }
        category.setName(category.getName().trim());
        category.setDescription(blankToNull(category.getDescription()));
        save(() -> catalogDAO.saveCategory(category));
    }

    @Override
    public void setCategoryActive(long categoryId, boolean active) throws SQLException {
        requirePositive(categoryId, "Mã danh mục không hợp lệ.");
        save(() -> catalogDAO.setCategoryActive(categoryId, active));
    }

    private void validateItem(FnBItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Thiếu thông tin sản phẩm.");
        }
        if (item.getId() != null) {
            requirePositive(item.getId(), "Mã sản phẩm không hợp lệ.");
        }
        if (item.getCategoryId() == null || item.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Vui lòng chọn danh mục sản phẩm.");
        }
        if (item.getItemCode() == null || item.getItemCode().isBlank()
                || item.getName() == null || item.getName().isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập đầy đủ thông tin bắt buộc.");
        }
        if (item.getPrice() == null || item.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Giá phải lớn hơn hoặc bằng 0.");
        }
    }

    private void save(SqlAction action) throws SQLException {
        try {
            action.run();
        } catch (SQLException e) {
            if (e.getErrorCode() == 2601 || e.getErrorCode() == 2627
                    || "23000".equals(e.getSQLState())) {
                throw new IllegalArgumentException("Mã sản phẩm hoặc tên danh mục đã tồn tại.", e);
            }
            throw e;
        }
    }

    private static void requirePositive(long value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    @FunctionalInterface
    private interface SqlAction {
        void run() throws SQLException;
    }
}
