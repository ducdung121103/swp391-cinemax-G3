package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entity Chi tiáº¿t cÃ¡c mÃ³n Báº¯p NÆ°á»›c F&B mua kÃ¨m trong Ä‘Æ¡n hÃ ng (Báº£ng order_items - Zone 4).
 * Chuáº©n 3NF tinh giáº£n: VÃ© xem phim Ä‘Æ°á»£c quáº£n lÃ½ riÃªng táº¡i báº£ng tickets.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
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

    // Quan há»‡ Ä‘iá»u hÆ°á»›ng
    private FnBItem fnbItem;
}

