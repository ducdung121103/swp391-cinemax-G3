package com.cinema.modules.identity.service.impl;

import com.cinema.common.util.PasswordUtil;
import com.cinema.model.User;
import com.cinema.modules.identity.dao.UserDAO;
import com.cinema.modules.identity.service.EmailService;
import com.cinema.modules.identity.service.UserService;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Triển khai nghiệp vụ Identity: Xác thực, Đăng ký OTP, Phân quyền.
 * Module Identity — TV 2 (Cường) — M-01.1 → M-01.4
 */
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = Logger.getLogger(UserServiceImpl.class.getName());

    private static final int OTP_EXPIRY_MINUTES     = 10;  // OTP hết hạn sau 10 phút
    private static final int RESET_TOKEN_MINUTES    = 15;  // Reset token hết hạn sau 15 phút

    private final UserDAO      userDAO      = new UserDAO();
    private final EmailService emailService = new EmailServiceImpl();

    // ────────────────────────────────────────────────────────────────────────────
    // ITERATION 1: AUTHENTICATION & OTP REGISTER
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public User authenticate(String email, String plainPassword) {
        if (email == null || plainPassword == null) return null;

        User user = userDAO.findByEmail(email.trim().toLowerCase());
        if (user == null) return null;

        // Kiểm tra tài khoản bị xóa mềm hoặc bị khóa
        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            LOGGER.warning("[AUTH] Tài khoản bị khóa: " + email);
            return null;
        }

        // Chặn tài khoản chưa xác thực email — trả null để AuthServlet xử lý riêng
        if ("UNVERIFIED".equalsIgnoreCase(user.getStatus())) {
            LOGGER.info("[AUTH] Tài khoản chưa xác thực OTP: " + email);
            // Trả về user với status=UNVERIFIED để controller phân biệt
            return PasswordUtil.checkPassword(plainPassword, user.getPasswordHash()) ? user : null;
        }

        // Xác thực mật khẩu BCrypt
        if (PasswordUtil.checkPassword(plainPassword, user.getPasswordHash())) {
            return user;
        }
        return null;
    }

    @Override
    public User registerCustomer(String email, String plainPassword, String fullName, String phone) {
        if (email == null || plainPassword == null || fullName == null) return null;

        String normalizedEmail = email.trim().toLowerCase();
        String normalizedPhone = (phone != null && !phone.isBlank()) ? phone.trim() : null;

        // Kiểm tra email đã tồn tại (kể cả UNVERIFIED)
        User existing = userDAO.findByEmail(normalizedEmail);
        if (existing != null) {
            LOGGER.info("[REGISTER] Email đã tồn tại: " + normalizedEmail);
            return null;
        }

        // Kiểm tra số điện thoại đã tồn tại
        if (normalizedPhone != null && userDAO.findByPhone(normalizedPhone) != null) {
            LOGGER.info("[REGISTER] Số điện thoại đã tồn tại: " + normalizedPhone);
            return null;
        }

        // Tạo OTP 6 chữ số ngẫu nhiên
        String otpCode    = generateOtp();
        LocalDateTime otpExpiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        // Tạo User entity với status=UNVERIFIED
        User newUser = User.builder()
                .email(normalizedEmail)
                .passwordHash(PasswordUtil.hashPassword(plainPassword))
                .fullName(fullName.trim())
                .phone(normalizedPhone)
                .otpCode(otpCode)
                .otpExpiresAt(otpExpiry)
                .build();

        Long generatedId = userDAO.insertCustomer(newUser);
        if (generatedId == null) {
            LOGGER.warning("[REGISTER] insertCustomer thất bại (email/phone trùng?): " + normalizedEmail);
            return null;
        }

        // Gửi email OTP (async-safe: EmailServiceImpl tự bắt exception nội bộ)
        emailService.sendOtpEmail(normalizedEmail, otpCode);
        LOGGER.info("[REGISTER] Đã tạo tài khoản UNVERIFIED và gửi OTP: " + normalizedEmail);

        return userDAO.findById(generatedId);
    }

    @Override
    public boolean verifyEmailOtp(String email, String otpCode) {
        if (email == null || otpCode == null) return false;
        boolean result = userDAO.verifyOtpAndActivate(email.trim().toLowerCase(), otpCode.trim());
        if (result) {
            LOGGER.info("[OTP] Xác thực thành công: " + email);
        } else {
            LOGGER.warning("[OTP] Mã sai hoặc hết hạn: " + email);
        }
        return result;
    }

    @Override
    public boolean resendOtp(String email) {
        if (email == null) return false;
        String normalizedEmail = email.trim().toLowerCase();
        User user = userDAO.findByEmail(normalizedEmail);

        if (user == null || !"UNVERIFIED".equalsIgnoreCase(user.getStatus())) {
            return false;
        }

        String newOtp = generateOtp();
        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);

        boolean saved = userDAO.saveOtp(user.getId(), newOtp, newExpiry);
        if (saved) {
            emailService.sendOtpEmail(normalizedEmail, newOtp);
            LOGGER.info("[OTP] Gửi lại OTP cho: " + normalizedEmail);
        }
        return saved;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // BASIC QUERIES
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public User getUserById(Long userId) {
        return userDAO.findById(userId);
    }

    @Override
    public boolean isEmailTaken(String email) {
        if (email == null || email.isBlank()) return false;
        return userDAO.findByEmail(email.trim().toLowerCase()) != null;
    }

    @Override
    public boolean isPhoneTaken(String phone) {
        if (phone == null || phone.isBlank()) return false;
        return userDAO.findByPhone(phone.trim()) != null;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // ITERATION 2: PASSWORD MANAGEMENT
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || oldPassword == null || newPassword == null) return false;
        User user = userDAO.findById(userId);
        if (user == null) return false;
        if (!PasswordUtil.checkPassword(oldPassword, user.getPasswordHash())) return false;
        String newHash = PasswordUtil.hashPassword(newPassword);
        return userDAO.updatePassword(userId, newHash);
    }

    @Override
    public boolean sendPasswordResetEmail(String email, String baseUrl) {
        if (email == null) return false;
        User user = userDAO.findByEmail(email.trim().toLowerCase());
        if (user == null || "UNVERIFIED".equalsIgnoreCase(user.getStatus())) return false;

        String token   = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(RESET_TOKEN_MINUTES);

        boolean saved = userDAO.saveResetToken(user.getId(), token, expiry);
        if (saved) {
            String resetUrl = baseUrl + "/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), token, resetUrl);
            LOGGER.info("[RESET] Đã gửi link reset password đến: " + email);
        }
        return saved;
    }

    @Override
    public User getUserByResetToken(String token) {
        if (token == null || token.isBlank()) return null;
        return userDAO.findByValidResetToken(token.trim());
    }

    @Override
    public boolean resetPasswordByToken(String token, String newPassword) {
        if (token == null || newPassword == null) return false;
        User user = userDAO.findByValidResetToken(token);
        if (user == null) return false;

        String newHash = PasswordUtil.hashPassword(newPassword);
        boolean updated = userDAO.updatePassword(user.getId(), newHash);
        if (updated) {
            userDAO.clearResetToken(user.getId());
            LOGGER.info("[RESET] Đặt lại mật khẩu thành công cho user: " + user.getId());
        }
        return updated;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // PRIVATE UTILITY
    // ────────────────────────────────────────────────────────────────────────────

    /** Tạo OTP 6 chữ số ngẫu nhiên, đảm bảo không bắt đầu bằng 0. */
    private String generateOtp() {
        int otp = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(otp);
    }
}
