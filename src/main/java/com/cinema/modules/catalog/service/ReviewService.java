package com.cinema.modules.catalog.service;

import com.cinema.model.Review;
import com.cinema.modules.catalog.dao.ReviewDAO;

import java.util.List;

/**
 * Service nghiep vu danh gia phim.
 */
public interface ReviewService {
    Long submitReview(Long userId, Long movieId, Integer ratingScore, String comment);

    List<Review> getApprovedReviewsByMovie(Long movieId);

    ReviewDAO.ReviewSummary getApprovedSummaryByMovie(Long movieId);

    Review getUserReviewForMovie(Long userId, Long movieId);

    List<Review> getAllReviewsForAdmin(String status);

    boolean approveReview(Long reviewId);

    boolean hideReview(Long reviewId);

    boolean deleteReview(Long reviewId);
}

