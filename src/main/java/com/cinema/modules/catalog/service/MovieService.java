package com.cinema.modules.catalog.service;

import com.cinema.model.Genre;
import com.cinema.model.Movie;

import java.util.List;

/**
 * Public Service Interface quản lý Phim & Danh mục (TV 3).
 */
public interface MovieService {
    List<Movie> getFeaturedMovies(int limit);
    List<Movie> getNowShowingMovies(int limit);
    List<Movie> getComingSoonMovies(int limit);
    List<Movie> getNowShowingMovies();
    List<Movie> getComingSoonMovies();
    List<Genre> getAllGenres();
    Movie getMovieById(Long movieId);
    List<Movie> getRelatedMovies(Long movieId, int limit);
    List<Movie> browseMovies(String type, String keyword, Long genreId, String ageRating, String sortBy, int page, int pageSize);
    int countMovies(String type, String keyword, Long genreId, String ageRating);

    // =========================================================================
    // Các phương thức Quản trị Danh mục Phim dành riêng cho ADMIN (M-02.1)
    // =========================================================================
    List<Movie> getAllMoviesForAdmin(String keyword, String status, String ageRating, Long genreId);
    Long createMovie(Movie movie, List<Long> genreIds);
    boolean updateMovie(Movie movie, List<Long> genreIds);
    boolean updateMovieStatus(Long id, String newStatus);
    boolean deleteMovie(Long id);
}
