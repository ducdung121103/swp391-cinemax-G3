package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entity Chi tiết các món Bắp Nước F&B mua kèm trong đơn hàng (Bảng order_items - Zone 4).
 * Chuẩn 3NF tinh giản: Vé xem phim được quản lý riêng tại bảng tickets.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class OrderItem extends BaseEntity {
    private Long bookingId;
    private Long fnbItemId;
    private String itemName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;

    // Quan hệ điều hướng
    private FnBItem fnbItem;
}
