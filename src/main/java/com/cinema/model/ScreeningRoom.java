package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Phòng chiếu phim (Bảng screening_rooms - Zone 1)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ScreeningRoom extends BaseEntity {
    private Long cinemaId;
    private String name;
    private String roomType; // STANDARD_2D, IMAX_3D, VIP_LUXURY
    private Integer totalRows;
    private Integer totalColumns;
    private Integer totalCapacity;
    private String status; // ACTIVE, MAINTENANCE, CLOSED

    // Quan hệ điều hướng
    private Cinema cinema;
}
