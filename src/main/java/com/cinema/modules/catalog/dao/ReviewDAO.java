package com.cinema.modules.catalog.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Review;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quan ly danh gia phim.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public class ReviewDAO {

    public static class ReviewSummary {
        private final double averageRating;
        private final int totalReviews;

        public ReviewSummary(double averageRating, int totalReviews) {
            this.averageRating = averageRating;
            this.totalReviews = totalReviews;
        }

        public double getAverageRating() {
            return averageRating;
        }

        public int getTotalReviews() {
            return totalReviews;
        }
    }

    private Review mapReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setId(rs.getLong("id"));
        review.setMovieId(rs.getLong("movie_id"));
        review.setUserId(rs.getLong("user_id"));
        review.setRatingScore(rs.getInt("rating_score"));
        review.setComment(rs.getString("comment"));
        review.setStatus(rs.getString("status"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            review.setCreatedAt(createdAt.toLocalDateTime());
        }

        try {
            review.setCustomerName(rs.getString("customer_name"));
        } catch (SQLException ignored) {
        }

        try {
            review.setMovieTitle(rs.getString("movie_title"));
        } catch (SQLException ignored) {
        }

        return review;
    }

    public Long insertReview(Review review) throws SQLException {
        String sql = "INSERT INTO reviews (movie_id, user_id, rating_score, comment, status, is_deleted, created_at, updated_at) "
                + "VALUES (?, ?, ?, ?, ?, 0, GETDATE(), GETDATE())";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, review.getMovieId());
            ps.setLong(2, review.getUserId());
            ps.setInt(3, review.getRatingScore());
            ps.setString(4, review.getComment());
            ps.setString(5, review.getStatus());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return null;
        }
    }

    public boolean existsByUserAndMovie(Long userId, Long movieId) {
        String sql = "SELECT COUNT(1) FROM reviews WHERE user_id = ? AND movie_id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] existsByUserAndMovie error: " + e.getMessage());
        }
        return false;
    }

    public boolean hasWatchedMovie(Long userId, Long movieId) {
        String sql = "SELECT COUNT(1) "
                + "FROM ticket_checkin_logs tcl "
                + "JOIN tickets t ON tcl.ticket_id = t.id "
                + "JOIN bookings b ON t.booking_id = b.id "
                + "JOIN showtimes st ON t.showtime_id = st.id "
                + "WHERE b.user_id = ? "
                + "  AND st.movie_id = ? "
                + "  AND b.status = 'CONFIRMED' "
                + "  AND tcl.status = 'SUCCESS' "
                + "  AND b.is_deleted = 0 "
                + "  AND t.is_deleted = 0";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] hasWatchedMovie error: " + e.getMessage());
        }
        return false;
    }

    public Review findByUserAndMovie(Long userId, Long movieId) {
        String sql = "SELECT TOP 1 r.*, u.full_name AS customer_name "
                + "FROM reviews r "
                + "JOIN users u ON r.user_id = u.id "
                + "WHERE r.user_id = ? AND r.movie_id = ? AND r.is_deleted = 0";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            ps.setLong(2, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReview(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] findByUserAndMovie error: " + e.getMessage());
        }
        return null;
    }

    public List<Review> findApprovedByMovie(Long movieId) {
        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT r.*, u.full_name AS customer_name "
                + "FROM reviews r "
                + "JOIN users u ON r.user_id = u.id "
                + "WHERE r.movie_id = ? "
                + "  AND r.status = 'APPROVED' "
                + "  AND r.is_deleted = 0 "
                + "ORDER BY r.created_at DESC";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] findApprovedByMovie error: " + e.getMessage());
        }
        return reviews;
    }

    public ReviewSummary getApprovedSummaryByMovie(Long movieId) {
        String sql = "SELECT AVG(CAST(rating_score AS FLOAT)) AS average_rating, COUNT(*) AS total_reviews "
                + "FROM reviews "
                + "WHERE movie_id = ? AND status = 'APPROVED' AND is_deleted = 0";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, movieId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double average = rs.getDouble("average_rating");
                    int total = rs.getInt("total_reviews");
                    return new ReviewSummary(total > 0 ? average : 0.0, total);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] getApprovedSummaryByMovie error: " + e.getMessage());
        }
        return new ReviewSummary(0.0, 0);
    }

    public List<Review> findAllForAdmin(String status) {
        List<Review> reviews = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT r.*, u.full_name AS customer_name, m.title AS movie_title ")
                .append("FROM reviews r ")
                .append("JOIN users u ON r.user_id = u.id ")
                .append("JOIN movies m ON r.movie_id = m.id ")
                .append("WHERE r.is_deleted = 0 ");

        boolean hasStatus = status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status);
        if (hasStatus) {
            sql.append("AND r.status = ? ");
        }
        sql.append("ORDER BY r.created_at DESC, r.id DESC");

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            if (hasStatus) {
                ps.setString(1, status.trim().toUpperCase());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    reviews.add(mapReview(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] findAllForAdmin error: " + e.getMessage());
        }
        return reviews;
    }

    public Review findById(Long reviewId) {
        String sql = "SELECT r.*, u.full_name AS customer_name, m.title AS movie_title "
                + "FROM reviews r "
                + "JOIN users u ON r.user_id = u.id "
                + "JOIN movies m ON r.movie_id = m.id "
                + "WHERE r.id = ? AND r.is_deleted = 0";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reviewId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapReview(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] findById error: " + e.getMessage());
        }
        return null;
    }

    public boolean updateStatus(Long reviewId, String newStatus) {
        String sql = "UPDATE reviews SET status = ?, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setLong(2, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] updateStatus error: " + e.getMessage());
        }
        return false;
    }

    public boolean deleteById(Long reviewId) {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, reviewId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ReviewDAO] deleteById error: " + e.getMessage());
        }
        return false;
    }
}


