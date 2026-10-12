package com.cinema.modules.catalog.service.impl;

import com.cinema.model.Genre;
import com.cinema.model.Movie;
import com.cinema.modules.catalog.dao.GenreDAO;
import com.cinema.modules.catalog.dao.MovieDAO;
import com.cinema.modules.catalog.service.MovieService;

import java.util.List;

/**
 * Cài đặt nghiệp vụ Danh mục phim (TV 3).
 */
public class MovieServiceImpl implements MovieService {
    private final MovieDAO movieDAO;
    private final GenreDAO genreDAO;

    public MovieServiceImpl() {
        this.movieDAO = new MovieDAO();
        this.genreDAO = new GenreDAO();
    }

    public MovieServiceImpl(MovieDAO movieDAO, GenreDAO genreDAO) {
        this.movieDAO = movieDAO;
        this.genreDAO = genreDAO;
    }

    @Override
    public List<Movie> getFeaturedMovies(int limit) {
        return movieDAO.getFeaturedMovies(limit);
    }

    @Override
    public List<Movie> getNowShowingMovies(int limit) {
        return movieDAO.getNowShowingMovies(limit);
    }

    @Override
    public List<Movie> getComingSoonMovies(int limit) {
        return movieDAO.getComingSoonMovies(limit);
    }

    @Override
    public List<Movie> getNowShowingMovies() {
        return movieDAO.getNowShowingMovies(12);
    }

    @Override
    public List<Movie> getComingSoonMovies() {
        return movieDAO.getComingSoonMovies(12);
    }

    @Override
    public List<Genre> getAllGenres() {
        return genreDAO.findAllActive();
    }

    @Override
    public Movie getMovieById(Long movieId) {
        return movieDAO.getMovieById(movieId);
    }

    @Override
    public List<Movie> getRelatedMovies(Long movieId, int limit) {
        return movieDAO.getRelatedMovies(movieId, limit);
    }

    @Override
    public List<Movie> browseMovies(String type, String keyword, Long genreId, String ageRating,
                                    String sortBy, int page, int pageSize) {
        return movieDAO.browseMovies(type, keyword, genreId, ageRating, sortBy, page, pageSize);
    }

    @Override
    public int countMovies(String type, String keyword, Long genreId, String ageRating) {
        return movieDAO.countMovies(type, keyword, genreId, ageRating);
    }
}
