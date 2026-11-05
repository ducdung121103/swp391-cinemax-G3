package com.cinema.modules.operation.service;

import com.cinema.model.Cinema;
import com.cinema.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public interface FnBInventoryService {
    List<Cinema> getActiveCinemas();

    long resolveCinemaId(User user, String requestedCinemaId);

    List<Map<String, Object>> getInventory(long cinemaId) throws SQLException;

    void updateStock(long cinemaId, long itemId, int quantity, int warningThreshold) throws SQLException;
}
