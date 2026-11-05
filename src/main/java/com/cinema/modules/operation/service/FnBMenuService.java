package com.cinema.modules.operation.service;

import com.cinema.model.FnBItem;

import java.util.List;

public interface FnBMenuService {
    List<FnBItem> getAvailableItems();

    List<FnBItem> getAvailableItems(long cinemaId);
}
