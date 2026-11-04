package com.cinema.common.util;

import java.util.regex.Pattern;

/**
 * Tiá»‡n Ã­ch kiá»ƒm tra tÃ­nh há»£p lá»‡ cá»§a dá»¯ liá»‡u Ä‘áº§u vÃ o (Validation).
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public final class ValidationUtil {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(0[3|5|7|8|9])[0-9]{8}$");
    // Máº­t kháº©u: Tá»‘i thiá»ƒu 8 kÃ½ tá»±, Ã­t nháº¥t 1 chá»¯ hoa, 1 chá»¯ thÆ°á»ng, 1 sá»‘, 1 kÃ½ tá»± Ä‘áº·c biá»‡t
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$");

    private ValidationUtil() {}

    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }
}

