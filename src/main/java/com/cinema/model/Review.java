package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity ÄÃ¡nh giÃ¡ vÃ  bÃ¬nh luáº­n phim (Báº£ng reviews - Zone 3)
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
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
    private String status; // PENDING, APPROVED, HIDDEN
    private String customerName;
    private String movieTitle;

    // Quan há»‡ Ä‘iá»u hÆ°á»›ng
    private User user;
}

