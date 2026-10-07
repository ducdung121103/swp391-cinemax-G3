package com.mbcms.browsing.services;

import com.mbcms.browsing.dao.GenreDAO;
import com.mbcms.browsing.dao.MovieDAO;
import com.mbcms.browsing.models.Genre;
import com.mbcms.browsing.models.Movie;

import java.util.List;

public class MovieService {

    private final MovieDAO movieDAO;
    private final GenreDAO genreDAO;

    public MovieService() {
        this.movieDAO = new MovieDAO();
        this.genreDAO = new GenreDAO();
    }

    public MovieService(MovieDAO movieDAO, GenreDAO genreDAO) {
        this.movieDAO = movieDAO;
        this.genreDAO = genreDAO;
    }

    public List<Movie> getFeaturedMovies(int limit) {
        return movieDAO.getFeaturedMovies(limit);
    }

    public List<Movie> getNowShowingMovies(int limit) {
        return movieDAO.getNowShowingMovies(limit);
    }

    public List<Movie> getComingSoonMovies(int limit) {
        return movieDAO.getComingSoonMovies(limit);
    }

    public Movie getMovieById(int id) {
        return movieDAO.getMovieById(id);
    }

    public List<Movie> getRelatedMovies(int movieId, int limit) {
        return movieDAO.getRelatedMovies(movieId, limit);
    }

    public List<Genre> getAllGenres() {
        return genreDAO.getAllGenres();
    }

    public List<Movie> browseMovies(String type, String keyword, Integer genreId, String ageRating,
                                    String sortBy, int page, int pageSize) {
        return movieDAO.browseMovies(type, keyword, genreId, ageRating, sortBy, page, pageSize);
    }

    public int countMovies(String type, String keyword, Integer genreId, String ageRating) {
        return movieDAO.countMovies(type, keyword, genreId, ageRating);
    }
}
