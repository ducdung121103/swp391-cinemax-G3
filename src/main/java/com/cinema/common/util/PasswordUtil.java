package com.cinema.common.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiện ích mã hóa một chiều mật khẩu theo thuật toán Salted BCrypt.
 * Đạt chuẩn bảo mật Enterprise, chống tấn công Rainbow Table / Brute Force.
 */
public final class PasswordUtil {
    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() {}

    /**
     * Băm mật khẩu thô thành chuỗi mã hóa BCrypt kèm Salt.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Mật khẩu không được để trống!");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Kiểm tra tính khớp giữa mật khẩu thô và mật khẩu đã băm.
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            return false;
        }
    }
}
