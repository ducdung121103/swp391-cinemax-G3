package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Lịch sử biến động điểm thưởng (Bảng point_histories - Zone 2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class PointHistory extends BaseEntity {
    private Long userId;
    private Long bookingId;
    private Integer points;
    private String type; // EARNED, REDEEMED
    private String description;
}
