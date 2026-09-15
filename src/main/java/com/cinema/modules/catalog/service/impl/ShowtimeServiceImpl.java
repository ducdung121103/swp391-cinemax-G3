package com.cinema.modules.catalog.service.impl;

import com.cinema.model.Showtime;
import com.cinema.modules.catalog.dao.ShowtimeDAO;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.infrastructure.service.ScreeningHallService;
import com.cinema.modules.infrastructure.service.impl.ScreeningHallServiceImpl;

import java.time.LocalDate;
import java.util.List;

/**
 * Cài đặt nghiệp vụ Lịch chiếu phim (TV 3).
 * Minh họa chuẩn quy tắc kiến trúc: Giao tiếp với TV 1 QUA ScreeningHallService, CẤM gọi DAO!
 */
public class ShowtimeServiceImpl implements ShowtimeService {
    private final ShowtimeDAO showtimeDAO = new ShowtimeDAO();
    // Gọi Public Service của TV 1 (Hạ tầng rạp)
    private final ScreeningHallService screeningHallService = new ScreeningHallServiceImpl();

    @Override
    public List<Showtime> getShowtimesByMovieAndDate(Long movieId, LocalDate date) {
        return showtimeDAO.findByMovieAndDate(movieId, date);
    }

    @Override
    public List<Showtime> getShowtimesByBranchAndDate(Long branchId, LocalDate date) {
        // Có thể mở rộng truy vấn theo branchId
        return showtimeDAO.findByMovieAndDate(1L, date);
    }

    @Override
    public Showtime getShowtimeById(Long showtimeId) {
        return showtimeDAO.findById(showtimeId);
    }

    @Override
    public boolean createShowtime(Showtime showtime) {
        // TV 3 kiểm tra phòng chiếu có khả dụng không qua Service của TV 1
        boolean available = screeningHallService.isHallAvailable(
                showtime.getScreeningHallId(),
                showtime.getStartTime(),
                showtime.getEndTime()
        );
        if (!available) {
            throw new IllegalStateException("Phòng chiếu đang bận hoặc đang trong lịch bảo trì!");
        }
        return showtimeDAO.insert(showtime);
    }
}
