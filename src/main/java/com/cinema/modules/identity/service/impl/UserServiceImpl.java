package com.cinema.modules.identity.service.impl;

import com.cinema.common.util.PasswordUtil;
import com.cinema.model.User;
import com.cinema.modules.identity.dao.UserDAO;
import com.cinema.modules.identity.service.UserService;

/**
 * Cài đặt nghiệp vụ tài khoản và phân quyền (TV 2).
 */
public class UserServiceImpl implements UserService {
    private final UserDAO userDAO = new UserDAO();

    @Override
    public User authenticate(String email, String plainPassword) {
        if (email == null || plainPassword == null) return null;
        User user = userDAO.findByEmail(email.trim());
        if (user != null && PasswordUtil.checkPassword(plainPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    @Override
    public User registerCustomer(String email, String plainPassword, String fullName, String phone) {
        String hash = PasswordUtil.hashPassword(plainPassword);
        User user = User.builder()
                .email(email.trim().toLowerCase())
                .passwordHash(hash)
                .fullName(fullName.trim())
                .phone(phone != null ? phone.trim() : null)
                .build();
        Long generatedId = userDAO.insertCustomer(user);
        if (generatedId != null) {
            return userDAO.findById(generatedId);
        }
        return null;
    }

    @Override
    public User getUserById(Long userId) {
        return userDAO.findById(userId);
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userDAO.findById(userId);
        if (user != null && PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) {
            String newHash = PasswordUtil.hashPassword(newPassword);
            return userDAO.updatePassword(userId, newHash);
        }
        return false;
    }
}
