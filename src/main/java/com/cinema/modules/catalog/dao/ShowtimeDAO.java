package com.cinema.modules.catalog.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Movie;
import com.cinema.model.ScreeningHall;
import com.cinema.model.Showtime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý bảng showtimes (TV 3).
 */
public class ShowtimeDAO {

    public List<Showtime> findByMovieAndDate(Long movieId, LocalDate date) {
        List<Showtime> list = new ArrayList<>();
        String sql = "SELECT st.*, m.title as movie_title, sh.name as hall_name " +
                     "FROM showtimes st " +
                     "JOIN movies m ON st.movie_id = m.id " +
                     "JOIN screening_halls sh ON st.screening_hall_id = sh.id " +
                     "WHERE st.movie_id = ? AND DATE(st.start_time) = ? AND st.is_deleted = 0 " +
                     "ORDER BY st.start_time ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, movieId);
            ps.setString(2, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapShowtime(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Showtime findById(Long id) {
        String sql = "SELECT st.*, m.title as movie_title, sh.name as hall_name " +
                     "FROM showtimes st " +
                     "JOIN movies m ON st.movie_id = m.id " +
                     "JOIN screening_halls sh ON st.screening_hall_id = sh.id " +
                     "WHERE st.id = ? AND st.is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShowtime(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(Showtime s) {
        String sql = "INSERT INTO showtimes (movie_id, screening_hall_id, start_time, end_time, experience_format, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, s.getMovieId());
            ps.setLong(2, s.getScreeningHallId());
            ps.setTimestamp(3, Timestamp.valueOf(s.getStartTime()));
            ps.setTimestamp(4, Timestamp.valueOf(s.getEndTime()));
            ps.setString(5, s.getExperienceFormat());
            ps.setString(6, s.getStatus() != null ? s.getStatus() : "SCHEDULED");
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Showtime mapShowtime(ResultSet rs) throws SQLException {
        Showtime st = new Showtime();
        st.setId(rs.getLong("id"));
        st.setMovieId(rs.getLong("movie_id"));
        st.setScreeningHallId(rs.getLong("screening_hall_id"));
        if (rs.getTimestamp("start_time") != null) {
            st.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        }
        if (rs.getTimestamp("end_time") != null) {
            st.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
        }
        st.setExperienceFormat(rs.getString("experience_format"));
        st.setStatus(rs.getString("status"));

        Movie m = new Movie();
        m.setId(rs.getLong("movie_id"));
        m.setTitle(rs.getString("movie_title"));
        st.setMovie(m);

        ScreeningHall sh = new ScreeningHall();
        sh.setId(rs.getLong("screening_hall_id"));
        sh.setName(rs.getString("hall_name"));
        st.setScreeningHall(sh);

        return st;
    }
}
