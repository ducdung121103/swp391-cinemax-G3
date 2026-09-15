package com.cinema.modules.identity.service;

import com.cinema.model.User;

/**
 * Public Service Interface do TV 2 cung cấp cho các module khác.
 */
public interface UserService {
    User authenticate(String email, String plainPassword);
    User registerCustomer(String email, String plainPassword, String fullName, String phone);
    User getUserById(Long userId);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
}
