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
    public List<Showtime> getShowtimesByCinema(Long cinemaId) {
        return cinemaId == null ? List.of() : showtimeDAO.findByCinema(cinemaId);
    }

    @Override
    public List<Showtime> getOccupyingByRoom(Long roomId, LocalDate date) {
        if (roomId == null || date == null) return List.of();
        return showtimeDAO.findOccupyingByRoom(roomId, date);
    }

    @Override
    public List<Showtime> searchScheduled(Long movieId, Long cinemaId, LocalDate date) {
        return showtimeDAO.findScheduled(movieId, cinemaId, date == null ? LocalDate.now() : date);
    }

    @Override
    public LocalDate findLatestScheduledDate(Long movieId, Long cinemaId) {
        return showtimeDAO.findLatestScheduledDate(movieId, cinemaId);
    }

    @Override
    public Showtime getShowtimeById(Long showtimeId) {
        return showtimeDAO.findById(showtimeId);
    }

    @Override
    public int getCleaningBufferMinutes() {
        return systemSettingDAO.getIntSetting("CLEANING_BUFFER_MINUTES", 15);
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

    @Override
    public boolean updateShowtime(Showtime showtime) {
        if (showtime == null || showtime.getId() == null || showtime.getStartTime() == null || showtime.getScreeningRoomId() == null) {
            throw new IllegalArgumentException("Thông tin suất chiếu không hợp lệ!");
        }
        Showtime current = showtimeDAO.findById(showtime.getId());
        if (current == null) {
            throw new IllegalArgumentException("Không tìm thấy suất chiếu!");
        }
        String format = showtime.getExperienceFormat() == null ? "2D" : showtime.getExperienceFormat().trim();
        if (!"2D".equals(format) && !"3D".equals(format) && !"IMAX".equals(format)) {
            throw new IllegalArgumentException("Định dạng suất chiếu không hợp lệ!");
        }
        String status = showtime.getStatus() == null ? current.getStatus() : showtime.getStatus().trim();
        if (!"SCHEDULED".equals(status) && !"OPENING".equals(status) && !"FINISHED".equals(status) && !"CANCELLED".equals(status)) {
            throw new IllegalArgumentException("Trạng thái suất chiếu không hợp lệ!");
        }
        Movie movie = movieDAO.findById(current.getMovieId());
        int durationMinutes = (movie != null && movie.getDurationMinutes() != null) ? movie.getDurationMinutes() : 120;
        int bufferMinutes = systemSettingDAO.getIntSetting("CLEANING_BUFFER_MINUTES", 15);
        LocalDateTime endTime = showtime.getStartTime().plusMinutes(durationMinutes + bufferMinutes);
        if (!"CANCELLED".equals(status) && !"FINISHED".equals(status)) {
            boolean available = screeningRoomService.isRoomAvailable(
                    showtime.getScreeningRoomId(), showtime.getStartTime(), endTime, current.getId());
            if (!available) {
                throw new IllegalStateException("Phòng chiếu đang bận hoặc đang trong lịch bảo trì!");
            }
        }
        current.setScreeningRoomId(showtime.getScreeningRoomId());
        current.setStartTime(showtime.getStartTime());
        current.setEndTime(endTime);
        current.setExperienceFormat(format);
        current.setStatus(status);
        return showtimeDAO.update(current);
    }
}
