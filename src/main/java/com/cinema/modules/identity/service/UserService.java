package com.cinema.modules.identity.service;

import com.cinema.model.User;

/**
 * Public Service Interface do TV 2 (Cường) cung cấp cho toàn hệ thống.
 * Các module khác CHỈ được gọi qua interface này, không import DAO trực tiếp.
 */
public interface UserService {

    // ── Iteration 1: Authentication ──────────────────────────────────────────

    /** Xác thực đăng nhập. Trả về User nếu đúng email+password, null nếu sai. */
    User authenticate(String email, String plainPassword);

    /**
     * Đăng ký tài khoản Customer mới.
     * Tạo user với status=UNVERIFIED, gửi OTP 6 số qua email.
     * @return User vừa tạo (chưa active), hoặc null nếu email đã tồn tại
     */
    User registerCustomer(String email, String plainPassword, String fullName, String phone);

    /**
     * Xác thực OTP sau đăng ký → kích hoạt tài khoản (status=ACTIVE).
     * @return true nếu OTP đúng và còn hạn
     */
    boolean verifyEmailOtp(String email, String otpCode);

    /**
     * Gửi lại OTP xác thực (khi người dùng bấm "Gửi lại").
     * @return true nếu gửi thành công (email tồn tại và đang UNVERIFIED)
     */
    boolean resendOtp(String email);

    // ── Iteration 1: Basic Queries ────────────────────────────────────────────

    /** Lấy thông tin user theo ID. */
    User getUserById(Long userId);

    /** Kiểm tra email đã được đăng ký hay chưa. */
    boolean isEmailTaken(String email);

    /** Kiểm tra số điện thoại đã được đăng ký hay chưa. */
    boolean isPhoneTaken(String phone);

    // ── Iteration 2: Password Management ─────────────────────────────────────

    /** Đổi mật khẩu (yêu cầu nhập mật khẩu cũ đúng). */
    boolean changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * Gửi email chứa link reset password (token 15 phút) — M-01.2 Forgot Password.
     * @param email     Email tài khoản cần reset
     * @param baseUrl   Base URL của ứng dụng (để tạo link đầy đủ, vd: http://localhost:8080/SWP391-Gr3)
     * @return true nếu email tồn tại trong hệ thống và đã gửi thành công
     */
    boolean sendPasswordResetEmail(String email, String baseUrl);

    /**
     * Lấy thông tin user bằng reset token còn hạn (để kiểm tra token hợp lệ trước khi hiển thị form).
     */
    User getUserByResetToken(String token);

    /**
     * Đặt lại mật khẩu mới bằng token reset (từ link email) — M-01.2.
     * @return true nếu token hợp lệ và cập nhật thành công
     */
    boolean resetPasswordByToken(String token, String newPassword);
}
