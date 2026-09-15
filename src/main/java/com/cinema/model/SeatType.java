package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Entity Loại ghế ngồi (Bảng seat_types - Zone 1)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class SeatType extends BaseEntity {
    private String typeCode; // STANDARD, VIP, COUPLE
    private String name;
    private String colorHex;
    private BigDecimal surcharge;
    private String description;
}
