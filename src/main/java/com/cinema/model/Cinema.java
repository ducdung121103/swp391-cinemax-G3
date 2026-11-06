package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Cá»¥m ráº¡p chiáº¿u (Báº£ng cinemas - Zone 1)
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
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

