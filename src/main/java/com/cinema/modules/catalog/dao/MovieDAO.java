package com.cinema.modules.catalog.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Movie;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý bảng movies (TV 3).
 */
public class MovieDAO {

    public List<Movie> findByStatus(String status) {
        List<Movie> list = new ArrayList<>();
        String sql = "SELECT * FROM movies WHERE status = ? AND is_deleted = 0 ORDER BY release_date DESC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapMovie(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Movie findById(Long id) {
        String sql = "SELECT * FROM movies WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapMovie(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Movie mapMovie(ResultSet rs) throws SQLException {
        Movie m = new Movie();
        m.setId(rs.getLong("id"));
        m.setTitle(rs.getString("title"));
        m.setPosterUrl(rs.getString("poster_url"));
        m.setTrailerUrl(rs.getString("trailer_url"));
        m.setDurationMinutes(rs.getInt("duration_minutes"));
        if (rs.getDate("release_date") != null) {
            m.setReleaseDate(rs.getDate("release_date").toLocalDate());
        }
        m.setAgeRating(rs.getString("age_rating"));
        m.setLanguage(rs.getString("language"));
        m.setDirector(rs.getString("director"));
        m.setActors(rs.getString("actors"));
        m.setSynopsis(rs.getString("synopsis"));
        m.setStatus(rs.getString("status"));
        return m;
    }
}
