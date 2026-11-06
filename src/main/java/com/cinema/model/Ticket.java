package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entity VÃ© xem phim váº­t lÃ½ / Ä‘iá»‡n tá»­ chi tiáº¿t (Báº£ng tickets - Zone 4).
 * Chá»©a trá»±c tiáº¿p showtimeId Ä‘á»ƒ báº£o Ä‘áº£m rÃ ng buá»™c toÃ n váº¹n chá»‘ng bÃ¡n trÃ¹ng gháº¿ uk_showtime_seat.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Ticket extends BaseEntity {
    private Long bookingId;
    private Long showtimeId;
    private Long seatId;
    private String barcode;
    private String qrSignature; // HMAC-SHA256 signature for anti-counterfeit QR code
    private BigDecimal ticketPrice;
    private String status; // VALID, USED, REFUNDED, CANCELLED

    // Quan há»‡ Ä‘iá»u hÆ°á»›ng
    private Seat seat;
    private Showtime showtime;
}

