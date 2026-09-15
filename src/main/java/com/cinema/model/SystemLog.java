package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Nhật ký kiểm toán hệ thống (Bảng system_logs - Zone 1)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SystemLog extends BaseEntity {
    private Long userId;
    private String action;
    private String module;
    private String ipAddress;
    private String details;
}
