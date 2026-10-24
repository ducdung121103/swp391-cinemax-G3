package com.cinema.modules.catalog.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Cinema;
import com.cinema.model.Movie;
import com.cinema.model.ScreeningRoom;
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

    public List<Showtime> findScheduled(Long movieId, Long cinemaId, LocalDate date) {
        StringBuilder sql = new StringBuilder(selectSql());
        sql.append("WHERE st.is_deleted = 0 AND st.status = 'SCHEDULED' AND CAST(st.start_time AS DATE) = ? ");
        List<Object> params = new ArrayList<>();
        params.add(date.toString());
        if (movieId != null) {
            sql.append("AND st.movie_id = ? ");
            params.add(movieId);
        }
        if (cinemaId != null) {
            sql.append("AND sr.cinema_id = ? ");
            params.add(cinemaId);
        }
        sql.append("ORDER BY st.start_time ASC");
        return query(sql.toString(), params);
    }

    public LocalDate findLatestScheduledDate(Long movieId, Long cinemaId) {
        StringBuilder sql = new StringBuilder(
                "SELECT MAX(CAST(st.start_time AS DATE)) AS show_date FROM showtimes st " +
                "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
                "WHERE st.is_deleted = 0 AND st.status = 'SCHEDULED' ");
        List<Object> params = new ArrayList<>();
        if (movieId != null) {
            sql.append("AND st.movie_id = ? ");
            params.add(movieId);
        }
        if (cinemaId != null) {
            sql.append("AND sr.cinema_id = ? ");
            params.add(cinemaId);
        }
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getDate("show_date") != null) {
                    return rs.getDate("show_date").toLocalDate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Showtime> findByMovieAndDate(Long movieId, LocalDate date) {
        List<Showtime> list = new ArrayList<>();
        String sql = selectSql() +
                     "WHERE st.movie_id = ? AND CAST(st.start_time AS DATE) = ? AND st.is_deleted = 0 " +
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

    public List<Showtime> findOccupyingByRoom(Long roomId, LocalDate date) {
        String sql = selectSql() +
                     "WHERE st.screening_room_id = ? AND st.is_deleted = 0 AND st.status <> 'CANCELLED' " +
                     "AND st.start_time < ? AND st.end_time > ? " +
                     "ORDER BY st.start_time ASC";
        List<Object> params = new ArrayList<>();
        params.add(roomId);
        params.add(Timestamp.valueOf(date.plusDays(1).atStartOfDay()));
        params.add(Timestamp.valueOf(date.atStartOfDay()));
        return query(sql, params);
    }

    public List<Showtime> findByCinema(Long cinemaId) {
        String sql = selectSql() +
                     "WHERE sr.cinema_id = ? AND st.is_deleted = 0 " +
                     "ORDER BY st.start_time ASC";
        List<Object> params = new ArrayList<>();
        params.add(cinemaId);
        return query(sql, params);
    }

    public List<Showtime> findByCinemaAndDate(Long cinemaId, LocalDate date) {
        List<Showtime> list = new ArrayList<>();
        String sql = selectSql() +
                     "WHERE sr.cinema_id = ? AND CAST(st.start_time AS DATE) = ? AND st.is_deleted = 0 " +
                     "ORDER BY st.start_time ASC";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, cinemaId);
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
        String sql = selectSql() + "WHERE st.id = ? AND st.is_deleted = 0";
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
        String sql = "INSERT INTO showtimes (movie_id, screening_room_id, start_time, end_time, experience_format, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, s.getMovieId());
            ps.setLong(2, s.getScreeningRoomId());
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

    public boolean update(Showtime s) {
        String sql = "UPDATE showtimes SET screening_room_id = ?, start_time = ?, end_time = ?, " +
                     "experience_format = ?, status = ? WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, s.getScreeningRoomId());
            ps.setTimestamp(2, Timestamp.valueOf(s.getStartTime()));
            ps.setTimestamp(3, Timestamp.valueOf(s.getEndTime()));
            ps.setString(4, s.getExperienceFormat());
            ps.setString(5, s.getStatus());
            ps.setLong(6, s.getId());
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
        st.setScreeningRoomId(rs.getLong("screening_room_id"));
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

        ScreeningRoom sr = new ScreeningRoom();
        sr.setId(rs.getLong("screening_room_id"));
        sr.setName(rs.getString("room_name"));
        long cinemaId = rs.getLong("cinema_id");
        if (!rs.wasNull()) {
            sr.setCinemaId(cinemaId);
            Cinema cinema = new Cinema();
            cinema.setId(cinemaId);
            cinema.setName(rs.getString("cinema_name"));
            sr.setCinema(cinema);
        }
        st.setScreeningRoom(sr);

        return st;
    }

    private String selectSql() {
        return "SELECT st.id, st.movie_id, st.screening_room_id, st.start_time, st.end_time, " +
               "st.experience_format, st.status, m.title AS movie_title, sr.name AS room_name, " +
               "sr.cinema_id, c.name AS cinema_name " +
               "FROM showtimes st " +
               "JOIN movies m ON st.movie_id = m.id " +
               "JOIN screening_rooms sr ON st.screening_room_id = sr.id " +
               "LEFT JOIN cinemas c ON sr.cinema_id = c.id ";
    }

    private List<Showtime> query(String sql, List<Object> params) {
        List<Showtime> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
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
}
