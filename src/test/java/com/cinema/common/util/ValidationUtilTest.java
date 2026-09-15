package com.cinema.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit Tests for ValidationUtil")
class ValidationUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {"admin@cinema.com", "customer.vip@gmail.com", "user123@fpt.edu.vn", "test+tag@sub.domain.org"})
    @DisplayName("Should validate valid email addresses")
    void testValidEmail(String email) {
        assertTrue(ValidationUtil.isValidEmail(email));
    }

    @ParameterizedTest
    @ValueSource(strings = {"plainaddress", "missing@domain", "@missinguser.com", "spaces in@email.com", "", "   "})
    @DisplayName("Should reject invalid email addresses")
    void testInvalidEmail(String email) {
        assertFalse(ValidationUtil.isValidEmail(email));
    }

    @Test
    @DisplayName("Should reject null email")
    void testNullEmail() {
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0901234567", "0987654321", "0356789012", "0778899001", "0891234567"})
    @DisplayName("Should validate correct Vietnamese phone numbers")
    void testValidPhone(String phone) {
        assertTrue(ValidationUtil.isValidPhone(phone));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0123456789", "090123456", "09012345678", "abcdefghij", "", "   "})
    @DisplayName("Should reject invalid phone numbers")
    void testInvalidPhone(String phone) {
        assertFalse(ValidationUtil.isValidPhone(phone));
    }

    @Test
    @DisplayName("Should test isNotEmpty helper method")
    void testIsNotEmpty() {
        assertTrue(ValidationUtil.isNotEmpty("Cinema"));
        assertFalse(ValidationUtil.isNotEmpty(""));
        assertFalse(ValidationUtil.isNotEmpty("   "));
        assertFalse(ValidationUtil.isNotEmpty(null));
    }
}
