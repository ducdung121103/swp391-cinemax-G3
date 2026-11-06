package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Lá»‹ch chiáº¿u phim chi tiáº¿t (Báº£ng showtimes - Zone 3)
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Showtime extends BaseEntity {
    private Long movieId;
    private Long screeningRoomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String experienceFormat; // 2D, 3D, IMAX
    private String status; // SCHEDULED, OPENING, FINISHED, CANCELLED

    // Quan há»‡ Ä‘iá»u hÆ°á»›ng
    private Movie movie;
    private ScreeningRoom screeningRoom;
}

