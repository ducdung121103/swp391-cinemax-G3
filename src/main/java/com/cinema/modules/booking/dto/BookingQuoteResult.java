package com.cinema.modules.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingQuoteResult {
    private BigDecimal baseTicketTotal;
    private int discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal discountedTicketTotal;
    private BigDecimal fnbTotal;
    private BigDecimal grandTotal;
}

