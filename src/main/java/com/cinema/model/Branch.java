package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Chi nhánh rạp chiếu (Bảng branches - Zone 1)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Branch extends BaseEntity {
    private String branchCode;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String email;
    private Integer totalHalls;
}
