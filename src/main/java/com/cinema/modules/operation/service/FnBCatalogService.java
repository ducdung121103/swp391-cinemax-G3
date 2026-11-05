package com.cinema.modules.operation.service;

import com.cinema.model.FnBCategory;
import com.cinema.model.FnBItem;

import java.sql.SQLException;
import java.util.List;

public interface FnBCatalogService {
    List<FnBItem> getItems() throws SQLException;

    List<FnBCategory> getCategories() throws SQLException;

    void saveItem(FnBItem item) throws SQLException;

    void setItemActive(long itemId, boolean active) throws SQLException;

    void saveCategory(FnBCategory category) throws SQLException;

    void setCategoryActive(long categoryId, boolean active) throws SQLException;
}
