package com.cinema.modules.operation.service.impl;

import com.cinema.model.FnBItem;
import com.cinema.modules.operation.dao.FnBItemDAO;
import com.cinema.modules.operation.service.FnBMenuService;

import java.util.List;

public class FnBMenuServiceImpl implements FnBMenuService {
    private final FnBItemDAO itemDAO;

    public FnBMenuServiceImpl() {
        this(new FnBItemDAO());
    }

    public FnBMenuServiceImpl(FnBItemDAO itemDAO) {
        this.itemDAO = itemDAO;
    }

    @Override
    public List<FnBItem> getAvailableItems() {
        return itemDAO.findAllActive();
    }

    @Override
    public List<FnBItem> getAvailableItems(long cinemaId) {
        if (cinemaId <= 0) {
            throw new IllegalArgumentException("Chi nhánh không hợp lệ.");
        }
        return itemDAO.findAllActiveForCinema(cinemaId);
    }
}
