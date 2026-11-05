package com.cinema.modules.operation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class PosQuoteResult {
    private final BigDecimal ticketTotal;
    private final BigDecimal fnbTotal;
    private final BigDecimal ticketDiscountAmount;
    private final BigDecimal grandTotal;
}
