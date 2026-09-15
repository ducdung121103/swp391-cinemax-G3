package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.List;

/**
 * Entity Danh mục phim chiếu rạp (Bảng movies - Zone 3)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Movie extends BaseEntity {
    private String title;
    private String posterUrl;
    private String trailerUrl;
    private Integer durationMinutes;
    private LocalDate releaseDate;
    private String ageRating; // P, K, T13, T16, T18, C
    private String language;
    private String director;
    private String actors;
    private String synopsis;
    private String status; // COMING_SOON, NOW_SHOWING, STOPPED

    // Quan hệ điều hướng
    private List<Genre> genres;
}
