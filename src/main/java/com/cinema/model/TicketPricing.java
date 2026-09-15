package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity Bảng cấu hình giá vé động (Bảng ticket_pricings - Zone 3)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class TicketPricing extends BaseEntity {
    private String dayType; // WEEKDAY, WEEKEND, HOLIDAY
    private String timeSlot; // EARLY, STANDARD, PRIME
    private String experienceFormat; // 2D, 3D, IMAX
    private BigDecimal basePrice;
    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;
}
