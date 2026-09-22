package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entity Thông báo In-App hệ thống (Bảng notifications - Zone 2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Notification implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long userId;
    private String title;
    private String message;
    private String type; // BOOKING, PAYMENT, SHOWTIME, PROMOTION, SYSTEM
    private String referenceId;
    private Boolean isRead;
    private LocalDateTime createdAt;

    // Quan hệ điều hướng
    private User user;
}
