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

    // =========================================================================
    // Triển khai Quản trị Danh mục Phim dành riêng cho ADMIN (M-02.1)
    // =========================================================================

    private void validateMovie(Movie movie) {
        if (movie == null) {
            throw new IllegalArgumentException("Dữ liệu phim không được để trống.");
        }
        if (movie.getTitle() == null || movie.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên phim không được để trống.");
        }
        if (movie.getTitle().trim().length() > 200) {
            throw new IllegalArgumentException("Tên phim không được vượt quá 200 ký tự.");
        }
        if (movie.getDurationMinutes() == null || movie.getDurationMinutes() <= 0 || movie.getDurationMinutes() > 500) {
            throw new IllegalArgumentException("Thời lượng phim phải lớn hơn 0 và không quá 500 phút.");
        }
        if (movie.getReleaseDate() == null) {
            throw new IllegalArgumentException("Ngày phát hành là bắt buộc.");
        }
        if (movie.getEndDate() != null && movie.getEndDate().isBefore(movie.getReleaseDate())) {
            throw new IllegalArgumentException("Ngày kết thúc không được nhỏ hơn ngày phát hành.");
        }
        if (movie.getAgeRating() == null || movie.getAgeRating().trim().isEmpty()) {
            movie.setAgeRating("P");
        } else {
            String ar = movie.getAgeRating().trim().toUpperCase();
            if (!List.of("P", "K", "T13", "T16", "T18", "C").contains(ar)) {
                throw new IllegalArgumentException("Độ tuổi phân loại không hợp lệ (chỉ chấp nhận: P, K, T13, T16, T18, C).");
            }
            movie.setAgeRating(ar);
        }
        if (movie.getStatus() == null || movie.getStatus().trim().isEmpty()) {
            movie.setStatus("COMING_SOON");
        } else {
            String st = movie.getStatus().trim().toUpperCase();
            if (!List.of("COMING_SOON", "NOW_SHOWING", "ENDED").contains(st)) {
                throw new IllegalArgumentException("Trạng thái phim không hợp lệ (chỉ chấp nhận: COMING_SOON, NOW_SHOWING, ENDED).");
            }
            movie.setStatus(st);
        }
    }

    @Override
    public List<Movie> getAllMoviesForAdmin(String keyword, String status, String ageRating, Long genreId) {
        return movieDAO.getAllMoviesForAdmin(keyword, status, ageRating, genreId);
    }

    @Override
    public Long createMovie(Movie movie, List<Long> genreIds) {
        validateMovie(movie);
        try {
            return movieDAO.createMovie(movie, genreIds);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Lỗi khi tạo phim mới", e);
        }
    }

    @Override
    public boolean updateMovie(Movie movie, List<Long> genreIds) {
        if (movie == null || movie.getId() == null || movie.getId() <= 0) {
            throw new IllegalArgumentException("ID phim không hợp lệ để cập nhật.");
        }
        validateMovie(movie);
        try {
            return movieDAO.updateMovie(movie, genreIds);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Lỗi khi cập nhật phim", e);
        }
    }

    @Override
    public boolean updateMovieStatus(Long id, String newStatus) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID phim không hợp lệ.");
        }
        if (newStatus == null || !List.of("COMING_SOON", "NOW_SHOWING", "ENDED").contains(newStatus.trim().toUpperCase())) {
            throw new IllegalArgumentException("Trạng thái phim không hợp lệ.");
        }
        try {
            return movieDAO.updateMovieStatus(id, newStatus.trim().toUpperCase());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Lỗi khi cập nhật trạng thái phim", e);
        }
    }

    @Override
    public boolean deleteMovie(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("ID phim không hợp lệ.");
        }
        try {
            return movieDAO.softDeleteMovie(id);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage() != null ? e.getMessage() : "Lỗi khi xóa phim", e);
        }
    }
}
