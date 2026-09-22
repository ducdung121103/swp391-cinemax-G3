package com.cinema.modules.identity.service.impl;

import com.cinema.modules.identity.service.EmailService;
import java.util.logging.Logger;

/**
 * Skeleton triển khai EmailService. Giả lập gửi email bằng log console trong môi trường phát triển (M-01.2).
 * // TODO: sinh viên tự implement — chọn thư viện gửi mail (JavaMail API) và cấu hình SMTP thật
 */
public class EmailServiceImpl implements EmailService {
    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl.class.getName());

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        // TODO: sinh viên tự implement — chọn thư viện gửi mail (JavaMail API) và cấu hình SMTP thật
        LOGGER.info(String.format("[EMAIL STUB] Gửi OTP xác thực đến: %s | Mã OTP: %s", toEmail, otpCode));
        System.out.println("[EMAIL STUB] Đang gửi OTP xác thực đến email: " + toEmail + " | Mã OTP: " + otpCode);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        // TODO: sinh viên tự implement — chọn thư viện gửi mail (JavaMail API) và cấu hình SMTP thật
        LOGGER.info(String.format("[EMAIL STUB] Gửi link reset password đến: %s | Reset Token: %s", toEmail, resetToken));
        System.out.println("[EMAIL STUB] Đang gửi yêu cầu đặt lại mật khẩu đến email: " + toEmail + " | Token: " + resetToken);
    }
}
