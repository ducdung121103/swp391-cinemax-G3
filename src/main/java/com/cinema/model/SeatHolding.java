package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Khóa ghế tạm thời 5 phút thời gian thực (Bảng seat_holdings - Zone 4)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SeatHolding extends BaseEntity {
    private Long showtimeId;
    private Long seatId;
    private String sessionId;
    private LocalDateTime heldAt;
    private LocalDateTime expiresAt;

    // Quan hệ điều hướng
    private Seat seat;
    private Showtime showtime;
}
