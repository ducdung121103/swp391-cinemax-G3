package com.cinema.modules.catalog.service;

import com.cinema.model.Movie;

import java.util.List;

/**
 * Public Service Interface quản lý Phim (TV 3).
 */
public interface MovieService {
    List<Movie> getNowShowingMovies();
    List<Movie> getComingSoonMovies();
    Movie getMovieById(Long movieId);
}
