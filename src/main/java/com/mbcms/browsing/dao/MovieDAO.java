package com.mbcms.browsing.dao;

import com.mbcms.browsing.config.DBContext;
import com.mbcms.browsing.models.Movie;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MovieDAO {

    /**
     * Helper để map ResultSet sang Movie object
     */
    private Movie mapRow(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setMovieId(rs.getInt("movie_id"));
        m.setTitle(rs.getString("title"));
        m.setDescription(rs.getString("description"));
        m.setDuration(rs.getInt("duration"));

        if (rs.getDate("release_date") != null) {
            m.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }
        if (rs.getDate("end_date") != null) {
            m.setEndDate(rs.getDate("end_date").toLocalDate());
        }

        m.setRating(rs.getDouble("rating"));
        m.setAgeRating(rs.getString("age_rating"));
        m.setDirector(rs.getString("director"));
        m.setCast(rs.getString("cast"));
        m.setPosterUrl(rs.getString("poster_url"));

        // Trailer URL
        try {
            m.setTrailerUrl(rs.getString("trailer_url"));
        } catch (SQLException ignored) {
            // Trường hợp schema không có cột trailer_url
        }

        m.setActive(rs.getBoolean("is_active"));

        if (rs.getTimestamp("created_at") != null) {
            m.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        }
        if (rs.getTimestamp("updated_at") != null) {
            m.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        }

        // Map genres từ chuỗi STRING_AGG
        try {
            String genreList = rs.getString("genre_list");
            if (genreList != null && !genreList.trim().isEmpty()) {
                String[] parts = genreList.split("\\s*,\\s*");
                List<String> glist = new ArrayList<>();
                for (String p : parts) {
                    if (!p.isBlank() && !glist.contains(p)) {
                        glist.add(p);
                    }
                }
                m.setGenres(glist);
            }
        } catch (SQLException ignored) {
        }

        return m;
    }

    /**
     * Lấy danh sách phim Đang Chiếu (Now Showing)
     * Điều kiện: release_date <= GETDATE() và (end_date IS NULL hoặc end_date >= GETDATE())
     */
    public List<Movie> getNowShowingMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) m.*, STRING_AGG(g.genre_name, ', ') AS genre_list
            FROM movies m
            LEFT JOIN movie_genres mg ON m.movie_id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.genre_id
            WHERE m.is_active = 1
              AND m.release_date <= CAST(GETDATE() AS DATE)
              AND (m.end_date IS NULL OR m.end_date >= CAST(GETDATE() AS DATE))
            GROUP BY m.movie_id, m.title, m.description, m.duration,
                     m.release_date, m.end_date, m.rating, m.age_rating,
                     m.director, m.cast, m.poster_url, m.trailer_url,
                     m.is_active, m.created_at, m.updated_at
            ORDER BY m.release_date DESC, m.rating DESC
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 12);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getNowShowingMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách phim Sắp Chiếu (Coming Soon)
     * Điều kiện: release_date > GETDATE()
     */
    public List<Movie> getComingSoonMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) m.*, STRING_AGG(g.genre_name, ', ') AS genre_list
            FROM movies m
            LEFT JOIN movie_genres mg ON m.movie_id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.genre_id
            WHERE m.is_active = 1
              AND m.release_date > CAST(GETDATE() AS DATE)
            GROUP BY m.movie_id, m.title, m.description, m.duration,
                     m.release_date, m.end_date, m.rating, m.age_rating,
                     m.director, m.cast, m.poster_url, m.trailer_url,
                     m.is_active, m.created_at, m.updated_at
            ORDER BY m.release_date ASC
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 12);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getComingSoonMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách phim Nổi bật (Featured / Carousel) cho Banner trang chủ
     */
    public List<Movie> getFeaturedMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) m.*, STRING_AGG(g.genre_name, ', ') AS genre_list
            FROM movies m
            LEFT JOIN movie_genres mg ON m.movie_id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.genre_id
            WHERE m.is_active = 1
            GROUP BY m.movie_id, m.title, m.description, m.duration,
                     m.release_date, m.end_date, m.rating, m.age_rating,
                     m.director, m.cast, m.poster_url, m.trailer_url,
                     m.is_active, m.created_at, m.updated_at
            ORDER BY m.rating DESC, m.release_date DESC
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getFeaturedMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy chi tiết một phim theo ID
     */
    public Movie getMovieById(int id) {
        String sql = """
            SELECT m.*, STRING_AGG(g.genre_name, ', ') AS genre_list
            FROM movies m
            LEFT JOIN movie_genres mg ON m.movie_id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.genre_id
            WHERE m.movie_id = ?
            GROUP BY m.movie_id, m.title, m.description, m.duration,
                     m.release_date, m.end_date, m.rating, m.age_rating,
                     m.director, m.cast, m.poster_url, m.trailer_url,
                     m.is_active, m.created_at, m.updated_at
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getMovieById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy các phim liên quan (cùng thể loại)
     */
    public List<Movie> getRelatedMovies(int movieId, int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) m.*, STRING_AGG(g.genre_name, ', ') AS genre_list
            FROM movies m
            LEFT JOIN movie_genres mg ON m.movie_id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.genre_id
            WHERE m.is_active = 1
              AND m.movie_id != ?
              AND m.movie_id IN (
                  SELECT DISTINCT mg2.movie_id 
                  FROM movie_genres mg2 
                  WHERE mg2.genre_id IN (
                      SELECT mg3.genre_id FROM movie_genres mg3 WHERE mg3.movie_id = ?
                  )
              )
            GROUP BY m.movie_id, m.title, m.description, m.duration,
                     m.release_date, m.end_date, m.rating, m.age_rating,
                     m.director, m.cast, m.poster_url, m.trailer_url,
                     m.is_active, m.created_at, m.updated_at
            ORDER BY m.rating DESC
        """;

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, limit > 0 ? limit : 4);
            ps.setInt(2, movieId);
            ps.setInt(3, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getRelatedMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm kiếm và duyệt phim tổng hợp (Browse / Filter / Search / Pagination)
     *
     * @param type       "now_showing", "coming_soon", hoặc "all"
     * @param keyword    Từ khóa tìm kiếm theo Tiêu đề, Đạo diễn, Diễn viên
     * @param genreId    ID thể loại phim
     * @param ageRating  Nhãn lứa tuổi (P, K, T13, T16, T18)
     * @param sortBy     Kiểu sắp xếp (release_desc, release_asc, rating_desc, title_asc, duration_desc)
     * @param page       Trang hiện tại (bắt đầu từ 1)
     * @param pageSize   Số lượng mỗi trang
     */
    public List<Movie> browseMovies(String type, String keyword, Integer genreId, String ageRating,
                                    String sortBy, int page, int pageSize) {
        List<Movie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();

        sql.append("""
            SELECT m.*, STRING_AGG(g.genre_name, ', ') AS genre_list
            FROM movies m
            LEFT JOIN movie_genres mg ON m.movie_id = mg.movie_id
            LEFT JOIN genres g ON mg.genre_id = g.genre_id
            WHERE m.is_active = 1
        """);

        List<Object> params = new ArrayList<>();

        // Lọc theo trạng thái Đang chiếu / Sắp chiếu
        if ("now_showing".equalsIgnoreCase(type)) {
            sql.append(" AND m.release_date <= CAST(GETDATE() AS DATE) AND (m.end_date IS NULL OR m.end_date >= CAST(GETDATE() AS DATE)) ");
        } else if ("coming_soon".equalsIgnoreCase(type)) {
            sql.append(" AND m.release_date > CAST(GETDATE() AS DATE) ");
        }

        // Tìm theo từ khóa
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (m.title LIKE ? OR m.director LIKE ? OR m.cast LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        // Lọc theo thể loại
        if (genreId != null && genreId > 0) {
            sql.append(" AND m.movie_id IN (SELECT movie_id FROM movie_genres WHERE genre_id = ?) ");
            params.add(genreId);
        }

        // Lọc theo nhãn tuổi
        if (ageRating != null && !ageRating.trim().isEmpty() && !"ALL".equalsIgnoreCase(ageRating)) {
            sql.append(" AND m.age_rating = ? ");
            params.add(ageRating.trim());
        }

        sql.append("""
            GROUP BY m.movie_id, m.title, m.description, m.duration,
                     m.release_date, m.end_date, m.rating, m.age_rating,
                     m.director, m.cast, m.poster_url, m.trailer_url,
                     m.is_active, m.created_at, m.updated_at
        """);

        // Sắp xếp
        if ("release_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY m.release_date ASC ");
        } else if ("rating_desc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY m.rating DESC, m.release_date DESC ");
        } else if ("title_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY m.title ASC ");
        } else if ("duration_desc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY m.duration DESC ");
        } else {
            // Mặc định: ngày phát hành mới nhất
            sql.append(" ORDER BY m.release_date DESC ");
        }

        // Phân trang
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        int offset = (Math.max(1, page) - 1) * pageSize;
        params.add(offset);
        params.add(pageSize);

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in browseMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Đếm tổng số phim phù hợp với bộ lọc tìm kiếm
     */
    public int countMovies(String type, String keyword, Integer genreId, String ageRating) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT m.movie_id) FROM movies m WHERE m.is_active = 1 ");

        List<Object> params = new ArrayList<>();

        if ("now_showing".equalsIgnoreCase(type)) {
            sql.append(" AND m.release_date <= CAST(GETDATE() AS DATE) AND (m.end_date IS NULL OR m.end_date >= CAST(GETDATE() AS DATE)) ");
        } else if ("coming_soon".equalsIgnoreCase(type)) {
            sql.append(" AND m.release_date > CAST(GETDATE() AS DATE) ");
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (m.title LIKE ? OR m.director LIKE ? OR m.cast LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (genreId != null && genreId > 0) {
            sql.append(" AND m.movie_id IN (SELECT movie_id FROM movie_genres WHERE genre_id = ?) ");
            params.add(genreId);
        }

        if (ageRating != null && !ageRating.trim().isEmpty() && !"ALL".equalsIgnoreCase(ageRating)) {
            sql.append(" AND m.age_rating = ? ");
            params.add(ageRating.trim());
        }

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in countMovies: " + e.getMessage());
        }
        return 0;
    }
}
