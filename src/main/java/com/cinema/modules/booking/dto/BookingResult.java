package com.cinema.modules.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * DTO trả kết quả sau khi BookingEngineService thực thi thành công.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResult {
    private Long bookingId;
    private String bookingCode;
    private BigDecimal finalAmount;
    private String status;
    private String message;
}
