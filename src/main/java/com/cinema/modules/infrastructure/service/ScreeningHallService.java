package com.cinema.modules.infrastructure.service;

import com.cinema.model.Branch;
import com.cinema.model.ScreeningHall;
import com.cinema.model.Seat;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Public Service Interface do TV 1 cung cấp cho các module khác.
 * Bất kỳ module nào (TV 3, TV 4) cần dữ liệu phòng/chi nhánh đều BẮT BUỘC gọi qua Interface này.
 */
public interface ScreeningHallService {
    List<Branch> getAllBranches();
    Branch getBranchById(Long branchId);
    List<ScreeningHall> getHallsByBranch(Long branchId);
    ScreeningHall getHallById(Long hallId);
    boolean isHallAvailable(Long hallId, LocalDateTime startTime, LocalDateTime endTime);
    List<Seat> getHallSeatMatrix(Long hallId);
}
