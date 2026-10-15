package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Ghế vật lý trong phòng chiếu (Bảng seats - Zone 1)
 */
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

    // Quan hệ điều hướng
    private SeatType seatType;
}
