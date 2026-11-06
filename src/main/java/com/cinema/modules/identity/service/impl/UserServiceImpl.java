package com.cinema.modules.identity.service.impl;

import com.cinema.common.util.PasswordUtil;
import com.cinema.common.constant.RoleConstant;
import com.cinema.common.util.ValidationUtil;
import com.cinema.model.User;
import com.cinema.modules.identity.dao.UserDAO;
import com.cinema.modules.identity.service.EmailService;
import com.cinema.modules.identity.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
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

        if (!PasswordUtil.checkPassword(plainPassword, user.getPasswordHash())) {
            return null;
        }

        // Mật khẩu đúng nhưng tài khoản bị khóa: trả user để servlet hiện thông báo khóa.
        if ("LOCKED".equalsIgnoreCase(user.getStatus())) {
            LOGGER.warning("[AUTH] Tài khoản bị khóa: " + email);
            return user;
        }

        // Chặn tài khoản chưa xác thực email — trả user để AuthServlet chuyển trang OTP
        if ("UNVERIFIED".equalsIgnoreCase(user.getStatus())) {
            LOGGER.info("[AUTH] Tài khoản chưa xác thực OTP: " + email);
            return user;
        }

        return user;
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
        if (userId == null || oldPassword == null || !ValidationUtil.isValidPassword(newPassword)) return false;
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

    @Override
    public String updateProfile(Long userId, String fullName, String phone, String avatarUrl) {
        if (userId == null || !ValidationUtil.isNotEmpty(fullName) || fullName.trim().length() > 120) {
            return "NAME";
        }
        String normalizedPhone = (phone == null || phone.isBlank()) ? null : phone.trim();
        if (normalizedPhone != null && !ValidationUtil.isValidPhone(normalizedPhone)) {
            return "PHONE";
        }
        String normalizedAvatar = (avatarUrl == null || avatarUrl.isBlank()) ? null : avatarUrl.trim();
        if (normalizedAvatar != null && normalizedAvatar.length() > 255) {
            return "AVATAR";
        }
        if (normalizedPhone != null) {
            User owner = userDAO.findByPhone(normalizedPhone);
            if (owner != null && !userId.equals(owner.getId())) {
                return "PHONE_TAKEN";
            }
        }
        boolean updated = userDAO.updateProfile(userId, fullName.trim(), normalizedPhone, normalizedAvatar);
        return updated ? "OK" : "FAILED";
    }

    @Override
    public List<User> searchUsers(String keyword, String roleName, String status, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = pageSize < 1 ? 20 : Math.min(pageSize, 50);
        return userDAO.search(keyword, roleName, status, safePage, safeSize);
    }

    @Override
    public int countUsers(String keyword, String roleName, String status) {
        return userDAO.countSearch(keyword, roleName, status);
    }

    @Override
    public String toggleAccountStatus(Long actorId, Long targetId) {
        if (actorId == null || targetId == null) return "NOT_FOUND";
        if (actorId.equals(targetId)) return "SELF";
        User target = userDAO.findById(targetId);
        if (target == null) return "NOT_FOUND";
        boolean active = "ACTIVE".equalsIgnoreCase(target.getStatus());
        boolean locked = "LOCKED".equalsIgnoreCase(target.getStatus());
        if (!active && !locked) return "FAILED";
        if (active && isAdmin(target) && userDAO.countActiveAdmins() <= 1) return "LAST_ADMIN";
        String next = active ? "LOCKED" : "ACTIVE";
        return userDAO.updateStatus(targetId, next) ? "OK" : "FAILED";
    }

    @Override
    public String assignRoleAndBranch(Long actorId, Long targetId, String roleName, Long cinemaId) {
        if (actorId == null || targetId == null) return "NOT_FOUND";
        if (actorId.equals(targetId)) return "SELF";
        if (!RoleConstant.STAFF.equals(roleName) && !RoleConstant.MANAGER.equals(roleName)) return "ROLE";
        if (cinemaId == null) return "CINEMA";
        User target = userDAO.findById(targetId);
        if (target == null) return "NOT_FOUND";
        if (isAdmin(target)) return "ADMIN";
        if (isCustomer(target)) return "CUSTOMER";
        boolean updated = userDAO.updateRoleAndCinema(targetId, roleName, cinemaId);
        return updated ? "OK" : "FAILED";
    }

    private boolean isAdmin(User user) {
        return user.getRole() != null && RoleConstant.ADMIN.equalsIgnoreCase(user.getRole().getRoleName());
    }

    private boolean isCustomer(User user) {
        return user.getRole() != null && RoleConstant.CUSTOMER.equalsIgnoreCase(user.getRole().getRoleName());
    }

    /** Tạo OTP 6 chữ số ngẫu nhiên, đảm bảo không bắt đầu bằng 0. */
    private String generateOtp() {
        int otp = 100000 + (int) (Math.random() * 900000);
        return String.valueOf(otp);
    }
}
