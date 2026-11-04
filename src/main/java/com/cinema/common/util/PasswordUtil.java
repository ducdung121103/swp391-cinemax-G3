package com.cinema.common.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Tiá»‡n Ã­ch mÃ£ hÃ³a má»™t chiá»u máº­t kháº©u theo thuáº­t toÃ¡n Salted BCrypt.
 * Äáº¡t chuáº©n báº£o máº­t Enterprise, chá»‘ng táº¥n cÃ´ng Rainbow Table / Brute Force.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public final class PasswordUtil {
    private static final int LOG_ROUNDS = 12;

    private PasswordUtil() {}

    /**
     * BÄƒm máº­t kháº©u thÃ´ thÃ nh chuá»—i mÃ£ hÃ³a BCrypt kÃ¨m Salt.
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Máº­t kháº©u khÃ´ng Ä‘Æ°á»£c Ä‘á»ƒ trá»‘ng!");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(LOG_ROUNDS));
    }

    /**
     * Kiá»ƒm tra tÃ­nh khá»›p giá»¯a máº­t kháº©u thÃ´ vÃ  máº­t kháº©u Ä‘Ã£ bÄƒm.
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

