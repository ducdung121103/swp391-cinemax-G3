package com.cinema.modules.infrastructure.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Detailed booking item for the drill-down table.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailDTO {
    private Long bookingId;
    private String bookingCode;
    private LocalDateTime paymentTime;
    private String movieTitle;
    private String cinemaName;
    private String roomName;
    private String channel;
    private String paymentMethod;
    private BigDecimal ticketsAmount;
    private BigDecimal fnbAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String status;
}
