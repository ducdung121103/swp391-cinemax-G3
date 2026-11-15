package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Lá»‹ch chiáº¿u phim chi tiáº¿t (Báº£ng showtimes - Zone 3)
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Showtime extends BaseEntity {
    private Long movieId;
    private Long screeningRoomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String experienceFormat; // 2D, 3D, IMAX
    private String status; // SCHEDULED, OPENING, FINISHED, CANCELLED

    // Quan há»‡ Ä‘iá»u hÆ°á»›ng
    private Movie movie;
    private ScreeningRoom screeningRoom;

    public String getFormattedStartTime() {
        if (startTime == null) return "";
        return startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
    }

    public String getFormattedTimeOnly() {
        if (startTime == null) return "";
        return startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFormattedDateOnly() {
        if (startTime == null) return "";
        return startTime.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /** Số phút trước giờ chiếu mà Manager/Admin không còn được sửa/xóa suất. */
    public static final int EDIT_LOCK_MINUTES = 30;

    /**
     * Trạng thái hiển thị (Cách A): tính theo đồng hồ, không cần job cập nhật DB.
     * CANCELLED trong DB luôn thắng; còn lại suy ra từ start/end.
     */
    public String getDisplayStatus() {
        if (status != null && "CANCELLED".equalsIgnoreCase(status.trim())) {
            return "CANCELLED";
        }
        if (startTime == null) {
            return status == null || status.isBlank() ? "SCHEDULED" : status.trim();
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = endTime != null ? endTime : startTime;
        if (!now.isBefore(end)) {
            return "FINISHED";
        }
        if (!now.isBefore(startTime)) {
            return "OPENING";
        }
        return "SCHEDULED";
    }

    /** true nếu đã hủy / đã chiếu / còn &lt; 30 phút trước giờ chiếu (hoặc đã bắt đầu). */
    public boolean isEditLocked() {
        if (status != null && "CANCELLED".equalsIgnoreCase(status.trim())) {
            return true;
        }
        if (startTime == null) {
            return true;
        }
        LocalDateTime lockFrom = startTime.minusMinutes(EDIT_LOCK_MINUTES);
        return !LocalDateTime.now().isBefore(lockFrom);
    }
}

