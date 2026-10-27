package com.cinema.modules.catalog.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Genre;
import com.cinema.model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý danh mục phim điện ảnh (Bảng movies & movie_genres - TV 3).
 * Tích hợp đầy đủ các tính năng: Trang chủ, Lọc phim đa tiêu chí, Phân trang, Chi tiết & Phim liên quan.
 */
public class MovieDAO {

    /**
     * Map ResultSet sang Movie entity kèm theo danh sách genres
     */
    private Movie mapMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setId(rs.getLong("id"));
        m.setTitle(rs.getString("title"));
        m.setOriginalTitle(rs.getString("original_title"));
        m.setPosterUrl(rs.getString("poster_url"));
        m.setTrailerUrl(rs.getString("trailer_url"));
        m.setDurationMinutes(rs.getInt("duration_minutes"));
        if (rs.getDate("release_date") != null) {
            m.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }
        if (rs.getDate("end_date") != null) {
            m.setEndDate(rs.getDate("end_date").toLocalDate());
        }
        m.setAgeRating(rs.getString("age_rating"));
        m.setLanguage(rs.getString("language"));
        m.setDirector(rs.getString("director"));
        m.setActors(rs.getString("actors"));
        m.setCastMembers(rs.getString("cast_members"));
        m.setSynopsis(rs.getString("synopsis"));
        m.setStatus(rs.getString("status"));
        m.setRating(4.8); // Điểm đánh giá sao mặc định

        // Đọc chuỗi thể loại từ STRING_AGG
        try {
            String genreList = rs.getString("genre_list");
            if (genreList != null && !genreList.trim().isEmpty()) {
                String[] parts = genreList.split("\\s*,\\s*");
                List<Genre> glist = new ArrayList<>();
                for (String p : parts) {
                    if (!p.isBlank()) {
                        Genre g = new Genre();
                        g.setName(p);
                        glist.add(g);
                    }
                }
                m.setGenres(glist);
            }
        } catch (SQLException ignored) {
        }

        try {
            String genreIdsStr = rs.getString("genre_ids");
            if (genreIdsStr != null && !genreIdsStr.trim().isEmpty()) {
                String[] parts = genreIdsStr.split("\\s*,\\s*");
                List<Long> idList = new ArrayList<>();
                for (String p : parts) {
                    if (!p.isBlank()) {
                        try {
                            idList.add(Long.parseLong(p.trim()));
                        } catch (NumberFormatException ignored) {}
                    }
                }
                m.setGenreIds(idList);
            }
        } catch (SQLException ignored) {
        }

        return m;
    }

    /**
     * Lấy danh sách phim Đang Chiếu (Now Showing)
     */
    public List<Movie> getNowShowingMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT TOP (?) m.*, STRING_AGG(g.name, ', ') AS genre_list " +
                     "FROM movies m " +
                     "LEFT JOIN movie_genres mg ON m.id = mg.movie_id " +
                     "LEFT JOIN genres g ON mg.genre_id = g.id " +
                     "WHERE m.is_deleted = 0 " +
                     "  AND (m.status = 'NOW_SHOWING' OR (m.release_date <= CAST(GETDATE() AS DATE) AND (m.end_date IS NULL OR m.end_date >= CAST(GETDATE() AS DATE)))) " +
                     "GROUP BY m.id, m.title, m.original_title, m.duration_minutes, m.release_date, m.end_date, " +
                     "         m.age_rating, m.language, m.director, m.actors, m.cast_members, m.synopsis, " +
                     "         m.poster_url, m.trailer_url, m.status, m.is_deleted, m.created_at, m.updated_at " +
                     "ORDER BY m.release_date DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 12);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error getNowShowingMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách phim Sắp Chiếu (Coming Soon)
     */
    public List<Movie> getComingSoonMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT TOP (?) m.*, STRING_AGG(g.name, ', ') AS genre_list " +
                     "FROM movies m " +
                     "LEFT JOIN movie_genres mg ON m.id = mg.movie_id " +
                     "LEFT JOIN genres g ON mg.genre_id = g.id " +
                     "WHERE m.is_deleted = 0 " +
                     "  AND (m.status = 'COMING_SOON' OR m.release_date > CAST(GETDATE() AS DATE)) " +
                     "GROUP BY m.id, m.title, m.original_title, m.duration_minutes, m.release_date, m.end_date, " +
                     "         m.age_rating, m.language, m.director, m.actors, m.cast_members, m.synopsis, " +
                     "         m.poster_url, m.trailer_url, m.status, m.is_deleted, m.created_at, m.updated_at " +
                     "ORDER BY m.release_date ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 12);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error getComingSoonMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy danh sách phim Nổi bật (Featured Carousel)
     */
    public List<Movie> getFeaturedMovies(int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT TOP (?) m.*, STRING_AGG(g.name, ', ') AS genre_list " +
                     "FROM movies m " +
                     "LEFT JOIN movie_genres mg ON m.id = mg.movie_id " +
                     "LEFT JOIN genres g ON mg.genre_id = g.id " +
                     "WHERE m.is_deleted = 0 " +
                     "GROUP BY m.id, m.title, m.original_title, m.duration_minutes, m.release_date, m.end_date, " +
                     "         m.age_rating, m.language, m.director, m.actors, m.cast_members, m.synopsis, " +
                     "         m.poster_url, m.trailer_url, m.status, m.is_deleted, m.created_at, m.updated_at " +
                     "ORDER BY m.release_date DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 5);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error getFeaturedMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy chi tiết một phim theo ID
     */
    public Movie getMovieById(Long id) {
        String sql = "SELECT m.*, STRING_AGG(g.name, ', ') AS genre_list, STRING_AGG(CAST(g.id AS VARCHAR), ',') AS genre_ids " +
                     "FROM movies m " +
                     "LEFT JOIN movie_genres mg ON m.id = mg.movie_id " +
                     "LEFT JOIN genres g ON mg.genre_id = g.id " +
                     "WHERE m.id = ? AND m.is_deleted = 0 " +
                     "GROUP BY m.id, m.title, m.original_title, m.duration_minutes, m.release_date, m.end_date, " +
                     "         m.age_rating, m.language, m.director, m.actors, m.cast_members, m.synopsis, " +
                     "         m.poster_url, m.trailer_url, m.status, m.is_deleted, m.created_at, m.updated_at";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMovie(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error getMovieById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lấy các phim liên quan (cùng thể loại)
     */
    public List<Movie> getRelatedMovies(Long movieId, int limit) {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT TOP (?) m.*, STRING_AGG(g.name, ', ') AS genre_list " +
                     "FROM movies m " +
                     "LEFT JOIN movie_genres mg ON m.id = mg.movie_id " +
                     "LEFT JOIN genres g ON mg.genre_id = g.id " +
                     "WHERE m.is_deleted = 0 AND m.id != ? " +
                     "  AND m.id IN ( " +
                     "      SELECT DISTINCT mg2.movie_id FROM movie_genres mg2 " +
                     "      WHERE mg2.genre_id IN (SELECT mg3.genre_id FROM movie_genres mg3 WHERE mg3.movie_id = ?) " +
                     "  ) " +
                     "GROUP BY m.id, m.title, m.original_title, m.duration_minutes, m.release_date, m.end_date, " +
                     "         m.age_rating, m.language, m.director, m.actors, m.cast_members, m.synopsis, " +
                     "         m.poster_url, m.trailer_url, m.status, m.is_deleted, m.created_at, m.updated_at " +
                     "ORDER BY m.release_date DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit > 0 ? limit : 4);
            ps.setLong(2, movieId);
            ps.setLong(3, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error getRelatedMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Tìm kiếm và duyệt phim tổng hợp (Browse / Filter / Search / Pagination)
     */
    public List<Movie> browseMovies(String type, String keyword, Long genreId, String ageRating,
                                    String sortBy, int page, int pageSize) {
        List<Movie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT m.*, STRING_AGG(g.name, ', ') AS genre_list ")
           .append("FROM movies m ")
           .append("LEFT JOIN movie_genres mg ON m.id = mg.movie_id ")
           .append("LEFT JOIN genres g ON mg.genre_id = g.id ")
           .append("WHERE m.is_deleted = 0 ");

        List<Object> params = new ArrayList<>();

        if ("now_showing".equalsIgnoreCase(type)) {
            sql.append(" AND (m.status = 'NOW_SHOWING' OR (m.release_date <= CAST(GETDATE() AS DATE) AND (m.end_date IS NULL OR m.end_date >= CAST(GETDATE() AS DATE)))) ");
        } else if ("coming_soon".equalsIgnoreCase(type)) {
            sql.append(" AND (m.status = 'COMING_SOON' OR m.release_date > CAST(GETDATE() AS DATE)) ");
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (m.title LIKE ? OR m.director LIKE ? OR m.actors LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (genreId != null && genreId > 0) {
            sql.append(" AND m.id IN (SELECT movie_id FROM movie_genres WHERE genre_id = ?) ");
            params.add(genreId);
        }

        if (ageRating != null && !ageRating.trim().isEmpty() && !"ALL".equalsIgnoreCase(ageRating)) {
            sql.append(" AND m.age_rating = ? ");
            params.add(ageRating.trim());
        }

        sql.append("GROUP BY m.id, m.title, m.original_title, m.duration_minutes, m.release_date, m.end_date, ")
           .append("         m.age_rating, m.language, m.director, m.actors, m.cast_members, m.synopsis, ")
           .append("         m.poster_url, m.trailer_url, m.status, m.is_deleted, m.created_at, m.updated_at ");

        // Sắp xếp
        if ("release_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY m.release_date ASC ");
        } else if ("title_asc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY m.title ASC ");
        } else if ("duration_desc".equalsIgnoreCase(sortBy)) {
            sql.append(" ORDER BY m.duration_minutes DESC ");
        } else {
            sql.append(" ORDER BY m.release_date DESC ");
        }

        // Phân trang SQL Server
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
                    list.add(mapMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error browseMovies: " + e.getMessage());
        }
        return list;
    }

    /**
     * Đếm tổng số phim phù hợp bộ lọc tìm kiếm
     */
    public int countMovies(String type, String keyword, Long genreId, String ageRating) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT m.id) FROM movies m WHERE m.is_deleted = 0 ");

        List<Object> params = new ArrayList<>();

        if ("now_showing".equalsIgnoreCase(type)) {
            sql.append(" AND (m.status = 'NOW_SHOWING' OR (m.release_date <= CAST(GETDATE() AS DATE) AND (m.end_date IS NULL OR m.end_date >= CAST(GETDATE() AS DATE)))) ");
        } else if ("coming_soon".equalsIgnoreCase(type)) {
            sql.append(" AND (m.status = 'COMING_SOON' OR m.release_date > CAST(GETDATE() AS DATE)) ");
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (m.title LIKE ? OR m.director LIKE ? OR m.actors LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (genreId != null && genreId > 0) {
            sql.append(" AND m.id IN (SELECT movie_id FROM movie_genres WHERE genre_id = ?) ");
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
            System.err.println("[MovieDAO] Error countMovies: " + e.getMessage());
        }
        return 0;
    }

    // Các hàm tương thích ngược (Legacy support)
    public List<Movie> findByStatus(String status) {
        if ("NOW_SHOWING".equalsIgnoreCase(status)) {
            return getNowShowingMovies(100);
        } else if ("COMING_SOON".equalsIgnoreCase(status)) {
            return getComingSoonMovies(100);
        }
        return browseMovies("all", null, null, null, "release_desc", 1, 100);
    }

    /**
     * Lấy danh sách toàn bộ phim cho ADMIN quản lý (kèm lọc, tìm kiếm, danh sách thể loại)
     */
    public List<Movie> getAllMoviesForAdmin(String keyword, String status, String ageRating, Long genreId) {
        List<Movie> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT m.*, ")
           .append("       STRING_AGG(g.name, ', ') AS genre_list, ")
           .append("       STRING_AGG(CAST(g.id AS VARCHAR), ',') AS genre_ids ")
           .append("FROM movies m ")
           .append("LEFT JOIN movie_genres mg ON m.id = mg.movie_id ")
           .append("LEFT JOIN genres g ON mg.genre_id = g.id ")
           .append("WHERE m.is_deleted = 0 ");

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (m.title LIKE ? OR m.original_title LIKE ? OR m.director LIKE ? OR m.actors LIKE ?) ");
            String kw = "%" + keyword.trim() + "%";
            params.add(kw);
            params.add(kw);
            params.add(kw);
            params.add(kw);
        }

        if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {
            sql.append(" AND m.status = ? ");
            params.add(status.trim());
        }

        if (ageRating != null && !ageRating.trim().isEmpty() && !"ALL".equalsIgnoreCase(ageRating)) {
            sql.append(" AND m.age_rating = ? ");
            params.add(ageRating.trim());
        }

        if (genreId != null && genreId > 0) {
            sql.append(" AND m.id IN (SELECT movie_id FROM movie_genres WHERE genre_id = ?) ");
            params.add(genreId);
        }

        sql.append("GROUP BY m.id, m.title, m.original_title, m.duration_minutes, m.release_date, m.end_date, ")
           .append("         m.age_rating, m.language, m.director, m.actors, m.cast_members, m.synopsis, ")
           .append("         m.poster_url, m.trailer_url, m.status, m.is_deleted, m.created_at, m.updated_at ")
           .append("ORDER BY m.created_at DESC, m.id DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[MovieDAO] Error getAllMoviesForAdmin: " + e.getMessage());
        }
        return list;
    }

    /**
     * Thêm mới phim kèm danh sách thể loại trong Transaction
     */
    public Long createMovie(Movie movie, List<Long> genreIds) throws SQLException {
        String sql = "INSERT INTO movies (title, original_title, duration_minutes, release_date, end_date, " +
                     "                   age_rating, language, director, actors, cast_members, synopsis, " +
                     "                   poster_url, trailer_url, status, is_deleted, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 0, GETDATE(), GETDATE())";
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            Long movieId = null;
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, movie.getTitle());
                ps.setString(2, movie.getOriginalTitle());
                ps.setInt(3, movie.getDurationMinutes());
                ps.setDate(4, movie.getReleaseDate() != null ? java.sql.Date.valueOf(movie.getReleaseDate()) : null);
                ps.setDate(5, movie.getEndDate() != null ? java.sql.Date.valueOf(movie.getEndDate()) : null);
                ps.setString(6, movie.getAgeRating() != null ? movie.getAgeRating() : "P");
                ps.setString(7, movie.getLanguage());
                ps.setString(8, movie.getDirector());
                ps.setString(9, movie.getActors());
                ps.setString(10, movie.getCastMembers());
                ps.setString(11, movie.getSynopsis());
                ps.setString(12, movie.getPosterUrl());
                ps.setString(13, movie.getTrailerUrl());
                ps.setString(14, movie.getStatus() != null ? movie.getStatus() : "COMING_SOON");

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Thêm phim thất bại, không có bản ghi nào được tạo.");
                }

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        movieId = rs.getLong(1);
                    }
                }
            }

            if (movieId == null) {
                conn.rollback();
                throw new SQLException("Không lấy được ID phim vừa tạo.");
            }

            if (genreIds != null && !genreIds.isEmpty()) {
                String genreSql = "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)";
                try (PreparedStatement psGenre = conn.prepareStatement(genreSql)) {
                    for (Long gId : genreIds) {
                        if (gId != null && gId > 0) {
                            psGenre.setLong(1, movieId);
                            psGenre.setLong(2, gId);
                            psGenre.addBatch();
                        }
                    }
                    psGenre.executeBatch();
                }
            }

            conn.commit();
            return movieId;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("[MovieDAO] Rollback error: " + ex.getMessage()); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật thông tin phim và liên kết thể loại trong Transaction
     */
    public boolean updateMovie(Movie movie, List<Long> genreIds) throws SQLException {
        String sql = "UPDATE movies SET title = ?, original_title = ?, duration_minutes = ?, " +
                     "                  release_date = ?, end_date = ?, age_rating = ?, language = ?, " +
                     "                  director = ?, actors = ?, cast_members = ?, synopsis = ?, " +
                     "                  poster_url = ?, trailer_url = ?, status = ?, updated_at = GETDATE() " +
                     "WHERE id = ? AND is_deleted = 0";
        Connection conn = null;
        try {
            conn = DBContext.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, movie.getTitle());
                ps.setString(2, movie.getOriginalTitle());
                ps.setInt(3, movie.getDurationMinutes());
                ps.setDate(4, movie.getReleaseDate() != null ? java.sql.Date.valueOf(movie.getReleaseDate()) : null);
                ps.setDate(5, movie.getEndDate() != null ? java.sql.Date.valueOf(movie.getEndDate()) : null);
                ps.setString(6, movie.getAgeRating() != null ? movie.getAgeRating() : "P");
                ps.setString(7, movie.getLanguage());
                ps.setString(8, movie.getDirector());
                ps.setString(9, movie.getActors());
                ps.setString(10, movie.getCastMembers());
                ps.setString(11, movie.getSynopsis());
                ps.setString(12, movie.getPosterUrl());
                ps.setString(13, movie.getTrailerUrl());
                ps.setString(14, movie.getStatus() != null ? movie.getStatus() : "COMING_SOON");
                ps.setLong(15, movie.getId());

                int affected = ps.executeUpdate();
                if (affected == 0) {
                    conn.rollback();
                    return false;
                }
            }

            // Xóa liên kết thể loại cũ
            String delGenresSql = "DELETE FROM movie_genres WHERE movie_id = ?";
            try (PreparedStatement psDel = conn.prepareStatement(delGenresSql)) {
                psDel.setLong(1, movie.getId());
                psDel.executeUpdate();
            }

            // Ghi nhận liên kết thể loại mới
            if (genreIds != null && !genreIds.isEmpty()) {
                String genreSql = "INSERT INTO movie_genres (movie_id, genre_id) VALUES (?, ?)";
                try (PreparedStatement psGenre = conn.prepareStatement(genreSql)) {
                    for (Long gId : genreIds) {
                        if (gId != null && gId > 0) {
                            psGenre.setLong(1, movie.getId());
                            psGenre.setLong(2, gId);
                            psGenre.addBatch();
                        }
                    }
                    psGenre.executeBatch();
                }
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { System.err.println("[MovieDAO] Rollback error: " + ex.getMessage()); }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Cập nhật nhanh trạng thái phim
     */
    public boolean updateMovieStatus(Long id, String newStatus) throws SQLException {
        String sql = "UPDATE movies SET status = ?, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Xóa mềm phim có kiểm tra suất chiếu hoạt động
     */
    public boolean softDeleteMovie(Long id) throws SQLException {
        String checkSql = "SELECT COUNT(1) FROM showtimes WHERE movie_id = ? AND status IN ('SCHEDULED', 'OPENING') AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setLong(1, id);
            try (ResultSet rs = psCheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    throw new SQLException("Không thể xóa phim đang có " + rs.getInt(1) + " suất chiếu hoạt động (SCHEDULED hoặc OPENING)!");
                }
            }

            String delSql = "UPDATE movies SET is_deleted = 1, status = 'ENDED', updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";
            try (PreparedStatement psDel = conn.prepareStatement(delSql)) {
                psDel.setLong(1, id);
                return psDel.executeUpdate() > 0;
            }
        }
    }

    public Movie findById(Long id) {
        return getMovieById(id);
    }
}
