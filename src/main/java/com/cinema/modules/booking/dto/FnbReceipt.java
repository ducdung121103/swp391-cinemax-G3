package com.cinema.modules.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** An F&B line recorded on a completed counter order. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FnbReceipt {
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
