package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entity Vé xem phim vật lý / điện tử chi tiết (Bảng tickets - Zone 4).
 * Chứa trực tiếp showtimeId để bảo đảm ràng buộc toàn vẹn chống bán trùng ghế uk_showtime_seat.
 */
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

    // Quan hệ điều hướng
    private Seat seat;
    private Showtime showtime;
}
