package com.cinema.modules.catalog.service.impl;

import com.cinema.model.Movie;
import com.cinema.modules.catalog.dao.MovieDAO;
import com.cinema.modules.catalog.service.MovieService;

import java.util.List;

/**
 * Cài đặt nghiệp vụ Danh mục phim (TV 3).
 */
public class MovieServiceImpl implements MovieService {
    private final MovieDAO movieDAO = new MovieDAO();

    @Override
    public List<Movie> getNowShowingMovies() {
        return movieDAO.findByStatus("NOW_SHOWING");
    }

    @Override
    public List<Movie> getComingSoonMovies() {
        return movieDAO.findByStatus("COMING_SOON");
    }

    @Override
    public Movie getMovieById(Long movieId) {
        return movieDAO.findById(movieId);
    }
}
