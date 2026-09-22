package com.cinema.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Entity Tài khoản người dùng (Bảng users - Zone 2)
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true, exclude = "passwordHash")
public class User extends BaseEntity {
    private Long roleId;
    private Long cinemaId;
    private String email;
    private String passwordHash;
    private String fullName;
    private String phone;
    private Integer loyaltyPoints;
    private Long tierId;
    private String avatarUrl;
    private String status; // ACTIVE, BANNED, UNVERIFIED
    private Boolean is2faEnabled;
    private Boolean emailVerified;
    private String otpCode;
    private java.time.LocalDateTime otpExpiresAt;
    private String resetPasswordToken;
    private java.time.LocalDateTime resetTokenExpiresAt;

    // Quan hệ điều hướng
    private Role role;
    private MembershipTier membershipTier;
    private Cinema cinema;
}
