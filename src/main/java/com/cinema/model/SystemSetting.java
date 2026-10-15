package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Tham số cấu hình toàn cục hệ thống (Bảng system_settings - Zone 1)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SystemSetting {
    private String settingKey;
    private String settingValue;
    private String description;
    @Builder.Default
    private String groupName = "SYSTEM";
    private Integer updatedBy;
    private LocalDateTime updatedAt;

    // Helper: Lấy giá trị dưới dạng số nguyên an toàn
    public int getIntValue(int defaultValue) {
        if (settingValue == null || settingValue.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(settingValue.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
