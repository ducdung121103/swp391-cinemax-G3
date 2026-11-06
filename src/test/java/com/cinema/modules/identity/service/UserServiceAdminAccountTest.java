package com.cinema.modules.identity.service;

import com.cinema.modules.identity.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Unit Tests for Admin Account Management: Create Staff/Manager & Soft Delete")
class UserServiceAdminAccountTest {

    private final UserService userService = new UserServiceImpl();

    @Test
    @DisplayName("createStaffOrManager: Unauthorized when actor is null")
    void testCreate_Unauthorized_Fails() {
        String res = userService.createStaffOrManager(null, "Test User", "test@cinema.com", "0912345678", "STAFF", 1L, "Cinemax@123");
        assertEquals("UNAUTHORIZED", res);
    }

    @Test
    @DisplayName("createStaffOrManager: Fails when name is empty")
    void testCreate_EmptyName_Fails() {
        String res = userService.createStaffOrManager(1L, "   ", "test@cinema.com", "0912345678", "STAFF", 1L, "Cinemax@123");
        assertEquals("INVALID_NAME", res);
    }

    @Test
    @DisplayName("createStaffOrManager: Fails on invalid email format")
    void testCreate_InvalidEmail_Fails() {
        String res = userService.createStaffOrManager(1L, "Test User", "invalid-email", "0912345678", "STAFF", 1L, "Cinemax@123");
        assertEquals("INVALID_EMAIL", res);
    }

    @Test
    @DisplayName("createStaffOrManager: Fails on invalid phone format")
    void testCreate_InvalidPhone_Fails() {
        String res = userService.createStaffOrManager(1L, "Test User", "valid@cinema.com", "12345", "STAFF", 1L, "Cinemax@123");
        assertEquals("INVALID_PHONE", res);
    }

    @Test
    @DisplayName("createStaffOrManager: Fails on weak password (< 8 chars or missing special char)")
    void testCreate_WeakPassword_Fails() {
        String res = userService.createStaffOrManager(1L, "Test User", "valid@cinema.com", "0912345678", "STAFF", 1L, "123456");
        assertEquals("INVALID_PASSWORD", res);
    }

    @Test
    @DisplayName("createStaffOrManager: Fails on invalid role (e.g. CUSTOMER or RANDOM)")
    void testCreate_InvalidRole_Fails() {
        String res = userService.createStaffOrManager(1L, "Test User", "valid@cinema.com", "0912345678", "CUSTOMER", 1L, "Cinemax@123");
        assertEquals("INVALID_ROLE", res);
    }

    @Test
    @DisplayName("createStaffOrManager: Fails when cinemaId is null or non-positive")
    void testCreate_MissingCinema_Fails() {
        String res1 = userService.createStaffOrManager(1L, "Test User", "valid@cinema.com", "0912345678", "STAFF", null, "Cinemax@123");
        assertEquals("CINEMA_REQUIRED", res1);

        String res2 = userService.createStaffOrManager(1L, "Test User", "valid@cinema.com", "0912345678", "MANAGER", 0L, "Cinemax@123");
        assertEquals("CINEMA_REQUIRED", res2);
    }

    @Test
    @DisplayName("softDeleteUser: Fails when actor or target is null")
    void testSoftDelete_NullInput_Fails() {
        assertEquals("NOT_FOUND", userService.softDeleteUser(null, 2L));
        assertEquals("NOT_FOUND", userService.softDeleteUser(1L, null));
    }

    @Test
    @DisplayName("softDeleteUser: Fails when actor deletes self")
    void testSoftDelete_Self_Fails() {
        assertEquals("SELF", userService.softDeleteUser(1L, 1L));
    }
}
