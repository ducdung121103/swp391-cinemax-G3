package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Cụm rạp chiếu (Bảng cinemas - Zone 1)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Cinema extends BaseEntity {
    private String cinemaCode;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String email;
    private Integer totalRooms;
}
