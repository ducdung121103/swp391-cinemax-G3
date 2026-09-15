package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Nhật ký soát vé tại cửa phòng chiếu qua Camera QR (Bảng ticket_checkin_logs - Zone 5)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class TicketCheckinLog extends BaseEntity {
    private Long ticketId;
    private Long staffId;
    private LocalDateTime checkinTime;
    private String status; // SUCCESS, REJECTED_ALREADY_USED, WRONG_SHOWTIME

    // Quan hệ điều hướng
    private Ticket ticket;
    private User staff;
}
