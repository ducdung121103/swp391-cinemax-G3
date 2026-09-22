package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Lịch bảo trì phòng chiếu (Bảng maintenance_schedules - Zone 1)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class MaintenanceSchedule extends BaseEntity {
    private Long screeningRoomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;
    private Long createdBy;
}
