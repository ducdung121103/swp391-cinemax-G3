package com.cinema.modules.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** A ticket line printed on the POS receipt. */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketReceipt {
    private String seatCode;
    private String barcode;
    private BigDecimal ticketPrice;
}
