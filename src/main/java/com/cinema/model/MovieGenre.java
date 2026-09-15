package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Entity Bảng liên kết nhiều - nhiều giữa Phim & Thể loại (Bảng movie_genres - Zone 3)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MovieGenre implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long movieId;
    private Long genreId;
}
