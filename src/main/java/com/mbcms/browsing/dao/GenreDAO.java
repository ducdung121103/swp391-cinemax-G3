package com.mbcms.browsing.dao;

import com.mbcms.browsing.config.DBContext;
import com.mbcms.browsing.models.Genre;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GenreDAO {

    /**
     * Lấy toàn bộ thể loại phim đang kích hoạt
     */
    public List<Genre> getAllGenres() {
        List<Genre> list = new ArrayList<>();
        String sql = "SELECT genre_id, genre_name, description, is_active FROM genres WHERE is_active = 1 ORDER BY genre_name ASC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Genre g = new Genre();
                g.setGenreId(rs.getInt("genre_id"));
                g.setGenreName(rs.getString("genre_name"));
                g.setDescription(rs.getString("description"));
                g.setActive(rs.getBoolean("is_active"));
                list.add(g);
            }
        } catch (SQLException e) {
            System.err.println("Error in getAllGenres: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lấy thể loại theo ID
     */
    public Genre getGenreById(int id) {
        String sql = "SELECT genre_id, genre_name, description, is_active FROM genres WHERE genre_id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Genre g = new Genre();
                    g.setGenreId(rs.getInt("genre_id"));
                    g.setGenreName(rs.getString("genre_name"));
                    g.setDescription(rs.getString("description"));
                    g.setActive(rs.getBoolean("is_active"));
                    return g;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error in getGenreById: " + e.getMessage());
        }
        return null;
    }
}
