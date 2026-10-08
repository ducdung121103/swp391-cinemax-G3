package com.cinema.modules.catalog.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý danh mục thể loại phim (Bảng genres & movie_genres - TV 3).
 */
public class GenreDAO {

    public List<Genre> findAllActive() {
        List<Genre> list = new ArrayList<>();
        String sql = "SELECT id, name, description FROM genres WHERE is_active = 1 AND is_deleted = 0 ORDER BY name ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Genre g = new Genre();
                g.setId(rs.getLong("id"));
                g.setName(rs.getString("name"));
                g.setDescription(rs.getString("description"));
                list.add(g);
            }
        } catch (SQLException e) {
            System.err.println("[GenreDAO] Error findAllActive: " + e.getMessage());
        }
        return list;
    }

    public List<Genre> findByMovieId(Long movieId) {
        List<Genre> list = new ArrayList<>();
        String sql = "SELECT g.id, g.name, g.description " +
                     "FROM genres g " +
                     "INNER JOIN movie_genres mg ON g.id = mg.genre_id " +
                     "WHERE mg.movie_id = ? AND g.is_active = 1 AND g.is_deleted = 0 " +
                     "ORDER BY g.name ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Genre g = new Genre();
                    g.setId(rs.getLong("id"));
                    g.setName(rs.getString("name"));
                    g.setDescription(rs.getString("description"));
                    list.add(g);
                }
            }
        } catch (SQLException e) {
            System.err.println("[GenreDAO] Error findByMovieId: " + e.getMessage());
        }
        return list;
    }
}
