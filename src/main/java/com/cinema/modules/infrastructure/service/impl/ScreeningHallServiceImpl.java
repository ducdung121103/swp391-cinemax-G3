package com.cinema.modules.infrastructure.service.impl;

import com.cinema.model.Branch;
import com.cinema.model.ScreeningHall;
import com.cinema.model.Seat;
import com.cinema.modules.infrastructure.dao.BranchDAO;
import com.cinema.modules.infrastructure.dao.ScreeningHallDAO;
import com.cinema.modules.infrastructure.service.ScreeningHallService;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Cài đặt nghiệp vụ Hạ tầng cụm rạp và phòng chiếu (TV 1).
 */
public class ScreeningHallServiceImpl implements ScreeningHallService {
    private final BranchDAO branchDAO = new BranchDAO();
    private final ScreeningHallDAO screeningHallDAO = new ScreeningHallDAO();

    @Override
    public List<Branch> getAllBranches() {
        return branchDAO.findAll();
    }

    @Override
    public Branch getBranchById(Long branchId) {
        return branchDAO.findById(branchId);
    }

    @Override
    public List<ScreeningHall> getHallsByBranch(Long branchId) {
        return screeningHallDAO.findByBranch(branchId);
    }

    @Override
    public ScreeningHall getHallById(Long hallId) {
        return screeningHallDAO.findById(hallId);
    }

    @Override
    public boolean isHallAvailable(Long hallId, LocalDateTime startTime, LocalDateTime endTime) {
        return screeningHallDAO.isAvailable(hallId, startTime, endTime);
    }

    @Override
    public List<Seat> getHallSeatMatrix(Long hallId) {
        return screeningHallDAO.findSeatsByHall(hallId);
    }
}
