package com.cinema.modules.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO dữ liệu lịch sử đặt vé và vé điện tử hiển thị cho khách hàng.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerBookingHistoryDTO {
    private Long bookingId;
    private String bookingCode;
    private LocalDateTime createdAt;
    private String status; // CONFIRMED, PENDING, CANCELLED
    private String channel; // ONLINE, POS
    private BigDecimal totalTicketsAmount;
    private BigDecimal totalFnbAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private String paymentMethod; // VNPAY, CASH, etc.

    // Thông tin khách hàng
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;

    // Phim & Rạp chiếu
    private Long movieId;
    private String movieTitle;
    private String posterUrl;
    private Integer durationMinutes;
    private String ageRating; // P, K, T13, T16, T18, C
    private LocalDateTime showtimeStartTime;
    private LocalDateTime showtimeEndTime;
    private String experienceFormat; // 2D, 3D, IMAX
    private String roomName;
    private String cinemaName;
    private String cinemaAddress;

    // Danh sách vé & Bắp nước đi kèm
    @Builder.Default
    private List<TicketReceipt> tickets = new ArrayList<>();
    @Builder.Default
    private List<FnbReceipt> fnbItems = new ArrayList<>();

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String getFormattedCreatedAt() {
        return createdAt != null ? createdAt.format(DATE_TIME_FORMATTER) : "";
    }

    public String getDisplayCustomerName() {
        if (customerName != null && !customerName.isBlank()) {
            return customerName;
        }
        return "Khách lẻ tại quầy";
    }

    public String getFormattedShowtime() {
        if (showtimeStartTime == null) return "";
        return showtimeStartTime.format(TIME_ONLY) + " &bull; " + showtimeStartTime.format(DATE_ONLY);
    }

    public String getSeatCodesString() {
        if (tickets == null || tickets.isEmpty()) return "Chưa có ghế";
        return tickets.stream()
                .map(TicketReceipt::getSeatCode)
                .filter(code -> code != null && !code.isBlank())
                .collect(Collectors.joining(", "));
    }

    public int getTicketCount() {
        return tickets != null ? tickets.size() : 0;
    }

    public boolean isUpcoming() {
        return showtimeStartTime != null && showtimeStartTime.isAfter(LocalDateTime.now());
    }
}

