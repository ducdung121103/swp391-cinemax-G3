package com.cinema.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for PasswordUtil (BCrypt Hashing)")
class PasswordUtilTest {

    @Test
    @DisplayName("Should generate valid BCrypt hash with 60 characters and $2a$ prefix")
    void testHashPasswordSuccess() {
        String plain = "123456";
        String hash = PasswordUtil.hashPassword(plain);

        assertNotNull(hash);
        assertEquals(60, hash.length());
        assertTrue(hash.startsWith("$2a$12$"));
    }

    @Test
    @DisplayName("Should successfully match plain password with generated hash")
    void testCheckPasswordValid() {
        String plain = "adminPassword2024!";
        String hash = PasswordUtil.hashPassword(plain);

        assertTrue(PasswordUtil.checkPassword(plain, hash));
    }

    @Test
    @DisplayName("Should return false when plain password does not match hash")
    void testCheckPasswordInvalid() {
        String hash = PasswordUtil.hashPassword("correctPassword");

        assertFalse(PasswordUtil.checkPassword("wrongPassword", hash));
    }

    @Test
    @DisplayName("Should return false when hash or password is null")
    void testCheckPasswordNullHandling() {
        assertFalse(PasswordUtil.checkPassword(null, "someHash"));
        assertFalse(PasswordUtil.checkPassword("somePassword", null));
        assertFalse(PasswordUtil.checkPassword(null, null));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when hashing null or empty password")
    void testHashPasswordEmptyThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword(null));
        assertThrows(IllegalArgumentException.class, () -> PasswordUtil.hashPassword("   "));
    }
}
