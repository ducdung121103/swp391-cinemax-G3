package com.cinema.modules.booking.dto;

import java.time.LocalDateTime;

/**
 * Suất sắp chiếu của một booking đã xác nhận, dùng cho email nhắc.
 */
public class ShowtimeReminderDTO {
    private final Long bookingId;
    private final Long userId;
    private final String movieTitle;
    private final LocalDateTime startTime;
    private final String roomName;
    private final String cinemaName;

    public ShowtimeReminderDTO(Long bookingId, Long userId, String movieTitle,
                               LocalDateTime startTime, String roomName, String cinemaName) {
        this.bookingId = bookingId;
        this.userId = userId;
        this.movieTitle = movieTitle;
        this.startTime = startTime;
        this.roomName = roomName;
        this.cinemaName = cinemaName;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getCinemaName() {
        return cinemaName;
    }
}
