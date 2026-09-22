package com.cinema.modules.identity.service;

/**
 * Interface dịch vụ gửi Email (xác thực email, OTP, đặt lại mật khẩu - M-01.2).
 */
public interface EmailService {
    /**
     * Gửi mã OTP xác thực email khi đăng ký tài khoản.
     * // TODO: sinh viên tự implement — chọn thư viện gửi mail (JavaMail API) và cấu hình SMTP thật
     */
    void sendOtpEmail(String toEmail, String otpCode);

    /**
     * Gửi liên kết / token đặt lại mật khẩu khi quên mật khẩu.
     * // TODO: sinh viên tự implement — chọn thư viện gửi mail (JavaMail API) và cấu hình SMTP thật
     */
    void sendPasswordResetEmail(String toEmail, String resetToken);
}
