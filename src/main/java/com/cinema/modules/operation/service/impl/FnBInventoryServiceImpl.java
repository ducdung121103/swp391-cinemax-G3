package com.cinema.modules.operation.service.impl;

import com.cinema.model.Cinema;
import com.cinema.model.User;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import com.cinema.modules.operation.dao.FnBInventoryDAO;
import com.cinema.modules.operation.service.FnBInventoryService;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class FnBInventoryServiceImpl implements FnBInventoryService {
    private final FnBInventoryDAO inventoryDAO;
    private final ScreeningRoomService screeningRoomService;

    public FnBInventoryServiceImpl() {
        this(new FnBInventoryDAO(), new ScreeningRoomServiceImpl());
    }

    public FnBInventoryServiceImpl(FnBInventoryDAO inventoryDAO, ScreeningRoomService screeningRoomService) {
        this.inventoryDAO = inventoryDAO;
        this.screeningRoomService = screeningRoomService;
    }

    @Override
    public List<Cinema> getActiveCinemas() {
        return screeningRoomService.getAllCinemas();
    }

    @Override
    public long resolveCinemaId(User user, String requestedCinemaId) {
        if (user == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản đang đăng nhập.");
        }
        if (user.getCinemaId() != null) {
            return user.getCinemaId();
        }

        List<Cinema> cinemas = getActiveCinemas();
        if (requestedCinemaId != null && !requestedCinemaId.isBlank()) {
            long cinemaId = parsePositiveLong(requestedCinemaId, "Chi nhánh không hợp lệ.");
            boolean exists = cinemas.stream().anyMatch(cinema -> cinemaId == cinema.getId());
            if (!exists) {
                throw new IllegalArgumentException("Chi nhánh không hợp lệ.");
            }
            return cinemaId;
        }
        if (!cinemas.isEmpty()) {
            return cinemas.get(0).getId();
        }
        throw new IllegalArgumentException("Chưa có rạp để quản lý tồn kho.");
    }

    @Override
    public List<Map<String, Object>> getInventory(long cinemaId) throws SQLException {
        requirePositive(cinemaId, "Chi nhánh không hợp lệ.");
        return inventoryDAO.findInventory(cinemaId);
    }

    @Override
    public void updateStock(long cinemaId, long itemId, int quantity, int warningThreshold) throws SQLException {
        requirePositive(cinemaId, "Chi nhánh không hợp lệ.");
        requirePositive(itemId, "Sản phẩm không hợp lệ.");
        if (quantity < 0 || warningThreshold < 0) {
            throw new IllegalArgumentException("Tồn kho và ngưỡng cảnh báo phải lớn hơn hoặc bằng 0.");
        }
        inventoryDAO.save(cinemaId, itemId, quantity, warningThreshold);
    }

    private static long parsePositiveLong(String value, String message) {
        try {
            long parsed = Long.parseLong(value.trim());
            requirePositive(parsed, message);
            return parsed;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(message, e);
        }
    }

    private static void requirePositive(long value, String message) {
        if (value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
