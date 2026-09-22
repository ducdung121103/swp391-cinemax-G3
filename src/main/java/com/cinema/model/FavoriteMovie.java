package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity Phim yêu thích của khách hàng (Bảng favorite_movies - Zone 2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class FavoriteMovie implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private Long movieId;
    private LocalDateTime createdAt;

    // Quan hệ điều hướng
    private Movie movie;
    private User user;
}
