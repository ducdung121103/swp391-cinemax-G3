package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entity Danh mục sản phẩm Bắp & Nước (Bảng fnb_items - Zone 5)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class FnBItem extends BaseEntity {
    private Long categoryId;
    private String itemCode; // POPCORN-CARAMEL-L, PEPSI-M...
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private Boolean isCombo;

    // Quan hệ điều hướng
    private FnBCategory category;
}
