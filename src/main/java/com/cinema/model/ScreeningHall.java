package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Phòng chiếu phim (Bảng screening_halls - Zone 1)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ScreeningHall extends BaseEntity {
    private Long branchId;
    private String name;
    private String hallType; // STANDARD_2D, IMAX_3D, VIP_LUXURY
    private Integer totalRows;
    private Integer totalColumns;
    private Integer totalCapacity;
    private String status; // ACTIVE, MAINTENANCE, CLOSED

    // Quan hệ điều hướng
    private Branch branch;
}
