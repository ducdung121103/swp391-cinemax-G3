package com.cinema.modules.catalog.service;

import com.cinema.model.Showtime;

import java.time.LocalDate;
import java.util.List;

/**
 * Public Service Interface quản lý Suất chiếu (TV 3).
 * TV 4 và TV 5 sẽ gọi qua Interface này để lấy thông tin suất chiếu.
 */
public interface ShowtimeService {
    List<Showtime> getShowtimesByMovieAndDate(Long movieId, LocalDate date);
    List<Showtime> getShowtimesByBranchAndDate(Long branchId, LocalDate date);
    Showtime getShowtimeById(Long showtimeId);
    boolean createShowtime(Showtime showtime);
}
