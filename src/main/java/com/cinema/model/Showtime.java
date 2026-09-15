package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Lịch chiếu phim chi tiết (Bảng showtimes - Zone 3)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Showtime extends BaseEntity {
    private Long movieId;
    private Long screeningHallId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String experienceFormat; // 2D, 3D, IMAX
    private String status; // SCHEDULED, OPENING, FINISHED, CANCELLED

    // Quan hệ điều hướng
    private Movie movie;
    private ScreeningHall screeningHall;
}
