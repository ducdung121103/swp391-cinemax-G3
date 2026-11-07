package com.cinema.modules.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingCheckoutResult {
    private BookingResult bookingResult;
    private String paymentMethod;
    private String paymentUrl;
    private String bookingCode;
}

