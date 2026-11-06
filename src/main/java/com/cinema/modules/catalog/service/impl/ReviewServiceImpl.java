package com.cinema.modules.catalog.service.impl;

import com.cinema.model.Review;
import com.cinema.modules.catalog.dao.ReviewDAO;
import com.cinema.modules.catalog.service.ReviewService;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * Cai dat nghiep vu danh gia va duyet danh gia phim.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public class ReviewServiceImpl implements ReviewService {
    private static final Set<String> ALLOWED_STATUSES = Set.of("PENDING", "APPROVED", "HIDDEN");

    private final ReviewDAO reviewDAO;

    public ReviewServiceImpl() {
        this.reviewDAO = new ReviewDAO();
    }

    public ReviewServiceImpl(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    @Override
    public Long submitReview(Long userId, Long movieId, Integer ratingScore, String comment) {
        validateReviewInput(userId, movieId, ratingScore, comment);

        if (reviewDAO.existsByUserAndMovie(userId, movieId)) {
            throw new IllegalArgumentException("Ban da gui danh gia cho phim nay.");
        }

        if (!reviewDAO.hasWatchedMovie(userId, movieId)) {
            throw new IllegalArgumentException("Ban chi co the danh gia phim sau khi da xem phim tai rap.");
        }

        Review review = Review.builder()
                .movieId(movieId)
                .userId(userId)
                .ratingScore(ratingScore)
                .comment(comment.trim())
                .status("PENDING")
                .build();

        try {
            return reviewDAO.insertReview(review);
        } catch (SQLException e) {
            String message = e.getMessage() != null ? e.getMessage() : "Loi khi luu danh gia.";
            if (message.toLowerCase().contains("uk_reviews_user_movie") || message.toLowerCase().contains("duplicate")) {
                throw new IllegalArgumentException("Ban da gui danh gia cho phim nay.");
            }
            throw new RuntimeException(message, e);
        }
    }

    @Override
    public List<Review> getApprovedReviewsByMovie(Long movieId) {
        return reviewDAO.findApprovedByMovie(movieId);
    }

    @Override
    public ReviewDAO.ReviewSummary getApprovedSummaryByMovie(Long movieId) {
        return reviewDAO.getApprovedSummaryByMovie(movieId);
    }

    @Override
    public Review getUserReviewForMovie(Long userId, Long movieId) {
        if (userId == null || userId <= 0 || movieId == null || movieId <= 0) {
            return null;
        }
        return reviewDAO.findByUserAndMovie(userId, movieId);
    }

    @Override
    public List<Review> getAllReviewsForAdmin(String status) {
        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            String normalized = status.trim().toUpperCase();
            if (!ALLOWED_STATUSES.contains(normalized)) {
                throw new IllegalArgumentException("Trang thai danh gia khong hop le.");
            }
            return reviewDAO.findAllForAdmin(normalized);
        }
        return reviewDAO.findAllForAdmin("ALL");
    }

    @Override
    public boolean approveReview(Long reviewId) {
        Review review = getValidExistingReview(reviewId);
        if (!"PENDING".equalsIgnoreCase(review.getStatus()) && !"HIDDEN".equalsIgnoreCase(review.getStatus())) {
            throw new IllegalArgumentException("Chi co the duyet danh gia dang PENDING hoac HIDDEN.");
        }
        return reviewDAO.updateStatus(reviewId, "APPROVED");
    }

    @Override
    public boolean hideReview(Long reviewId) {
        Review review = getValidExistingReview(reviewId);
        if (!"APPROVED".equalsIgnoreCase(review.getStatus())) {
            throw new IllegalArgumentException("Chi co the an danh gia dang APPROVED.");
        }
        return reviewDAO.updateStatus(reviewId, "HIDDEN");
    }

    @Override
    public boolean deleteReview(Long reviewId) {
        getValidExistingReview(reviewId);
        return reviewDAO.deleteById(reviewId);
    }

    private Review getValidExistingReview(Long reviewId) {
        if (reviewId == null || reviewId <= 0) {
            throw new IllegalArgumentException("ID danh gia khong hop le.");
        }
        Review review = reviewDAO.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Khong tim thay danh gia.");
        }
        return review;
    }

    private void validateReviewInput(Long userId, Long movieId, Integer ratingScore, String comment) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Ban can dang nhap de gui danh gia.");
        }
        if (movieId == null || movieId <= 0) {
            throw new IllegalArgumentException("Phim khong hop le.");
        }
        if (ratingScore == null || ratingScore < 1 || ratingScore > 5) {
            throw new IllegalArgumentException("So sao danh gia phai tu 1 den 5.");
        }
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Noi dung danh gia khong duoc de trong.");
        }
    }
}


