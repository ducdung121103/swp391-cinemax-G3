package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Vai trò người dùng (Bảng roles - Zone 2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Role extends BaseEntity {
    private String roleName; // ADMIN, MANAGER, STAFF, CUSTOMER
    private String description;
}
