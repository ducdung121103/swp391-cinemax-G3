package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * Entity Hỗ trợ & Khiếu nại khách hàng (Bảng support_tickets - Zone 2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SupportTicket extends BaseEntity {
    private String ticketCode;
    private Long userId;
    private Long staffId;
    private String category; // BOOKING, PAYMENT, FNB, TECHNICAL, OTHER
    private String subject;
    private String content;
    private String response;
    private String status; // OPEN, IN_PROGRESS, RESOLVED, CLOSED
    private String priority; // LOW, MEDIUM, HIGH, URGENT

    // Quan hệ điều hướng
    private User user;
    private User staff;
}
