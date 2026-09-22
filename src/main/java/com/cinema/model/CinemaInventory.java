package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Quản lý tồn kho bắp nước tại từng rạp (Bảng cinema_inventories - Zone 5)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class CinemaInventory extends BaseEntity {
    private Long cinemaId;
    private Long fnbItemId;
    private Integer stockQuantity;
    private Integer warningThreshold;

    // Quan hệ điều hướng
    private Cinema cinema;
    private FnBItem fnbItem;
}
