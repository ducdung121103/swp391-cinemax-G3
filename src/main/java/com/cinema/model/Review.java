package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Đánh giá và bình luận phim (Bảng reviews - Zone 3)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Review extends BaseEntity {
    private Long movieId;
    private Long userId;
    private Integer ratingScore; // 1 -> 5 sao
    private String comment;
    private String status; // PENDING, APPROVED, REJECTED

    // Quan hệ điều hướng
    private User user;
}
