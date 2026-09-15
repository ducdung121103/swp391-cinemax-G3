package com.cinema.modules.booking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO nhận yêu cầu tạo đơn đặt chỗ từ Online Web hoặc quầy POS.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingDTO {
    private Long userId;
    private Long staffId;
    private Long showtimeId;
    private Long voucherId;
    private String channel; // ONLINE, POS
    private String paymentMethod; // VNPAY, CASH, MOMO, POINTS
    private String sessionId;

    /** Danh sách ID ghế chọn mua */
    private List<Long> seatIds;

    /** Giá vé tương ứng cho từng ghế */
    private List<BigDecimal> ticketPrices;

    /** Danh sách F&B mua kèm: key = fnb_item_id, value = số lượng */
    private Map<Long, Integer> fnbItems;

    public boolean hasFnb() {
        return fnbItems != null && !fnbItems.isEmpty();
    }
}
