package com.cinema.modules.catalog.service.impl;

import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.model.Movie;
import com.cinema.model.Showtime;
import com.cinema.modules.catalog.dao.MovieDAO;
import com.cinema.modules.catalog.dao.ShowtimeDAO;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Cài đặt nghiệp vụ Lịch chiếu phim (TV 3).
 * Minh họa chuẩn quy tắc kiến trúc: Giao tiếp với TV 1 QUA ScreeningRoomService, CẤM gọi DAO!
 */
public class ShowtimeServiceImpl implements ShowtimeService {
    private final ShowtimeDAO showtimeDAO;
    private final MovieDAO movieDAO;
    private final SystemSettingDAO systemSettingDAO;
    // Gọi Public Service của TV 1 (Hạ tầng rạp)
    private final ScreeningRoomService screeningRoomService;

    public ShowtimeServiceImpl() {
        this.showtimeDAO = new ShowtimeDAO();
        this.movieDAO = new MovieDAO();
        this.systemSettingDAO = new SystemSettingDAO();
        this.screeningRoomService = new ScreeningRoomServiceImpl();
    }

    public ShowtimeServiceImpl(ShowtimeDAO showtimeDAO, MovieDAO movieDAO, SystemSettingDAO systemSettingDAO, ScreeningRoomService screeningRoomService) {
        this.showtimeDAO = showtimeDAO;
        this.movieDAO = movieDAO;
        this.systemSettingDAO = systemSettingDAO;
        this.screeningRoomService = screeningRoomService;
    }

    @Override
    public List<Showtime> getShowtimesByMovieAndDate(Long movieId, LocalDate date) {
        return showtimeDAO.findByMovieAndDate(movieId, date);
    }

    @Override
    public List<Showtime> getShowtimesByCinemaAndDate(Long cinemaId, LocalDate date) {
        return showtimeDAO.findByCinemaAndDate(cinemaId, date);
    }

    @Override
    public Showtime getShowtimeById(Long showtimeId) {
        return showtimeDAO.findById(showtimeId);
    }

    @Override
    public boolean createShowtime(Showtime showtime) {
        if (showtime == null || showtime.getStartTime() == null || showtime.getMovieId() == null || showtime.getScreeningRoomId() == null) {
            throw new IllegalArgumentException("Thông tin suất chiếu không hợp lệ!");
        }

        // Tự động tính toán end_time = start_time + duration_minutes + buffer_minutes (đọc từ system_settings)
        Movie movie = movieDAO.findById(showtime.getMovieId());
        int durationMinutes = (movie != null && movie.getDurationMinutes() != null) ? movie.getDurationMinutes() : 120;
        int bufferMinutes = systemSettingDAO.getIntSetting("CLEANING_BUFFER_MINUTES", 15);
        LocalDateTime calculatedEndTime = showtime.getStartTime().plusMinutes(durationMinutes + bufferMinutes);
        showtime.setEndTime(calculatedEndTime);

        // TV 3 kiểm tra phòng chiếu có khả dụng không qua Service của TV 1
        boolean available = screeningRoomService.isRoomAvailable(
                showtime.getScreeningRoomId(),
                showtime.getStartTime(),
                showtime.getEndTime()
        );
        if (!available) {
            throw new IllegalStateException("Phòng chiếu đang bận hoặc đang trong lịch bảo trì!");
        }
        return showtimeDAO.insert(showtime);
    }
}
