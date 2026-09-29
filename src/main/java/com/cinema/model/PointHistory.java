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
    private Integer pointsChange;
    private Integer balanceAfter;
    private String transactionType; // EARN, REDEEM
    private String reason;

    public Integer getPoints() { return pointsChange; }
    public void setPoints(Integer points) { this.pointsChange = points; }
    public String getType() { return transactionType; }
    public void setType(String type) { this.transactionType = type; }
    public String getDescription() { return reason; }
    public void setDescription(String description) { this.reason = description; }
}
