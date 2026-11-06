package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Gháº¿ váº­t lÃ½ trong phÃ²ng chiáº¿u (Báº£ng seats - Zone 1)
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Seat extends BaseEntity {
    private Long screeningRoomId;
    private Long seatTypeId;
    private String seatRow; // A, B, C...
    private Integer seatNumber; // 1, 2, 3...
    private String seatCode; // A01, F08...
    private Integer gridRowIndex;
    private Integer gridColIndex;
    @Builder.Default
    private String status = "ACTIVE"; // ACTIVE, MAINTENANCE, BLOCKED

    // Quan há»‡ Ä‘iá»u hÆ°á»›ng
    private SeatType seatType;
}

