package com.cinema.modules.identity.service;

/**
 * Interface dịch vụ gửi Email - Module Identity (TV 2 - Cường).
 * Phục vụ: M-01.2 (OTP xác thực email, reset password), M-09.1 (Thông báo đặt vé, hoàn tiền).
 */
public interface EmailService {

    /**
     * Gửi mã OTP 6 số xác thực email khi đăng ký tài khoản mới.
     * @param toEmail   Địa chỉ email người nhận
     * @param otpCode   Mã OTP 6 chữ số
     */
    void sendOtpEmail(String toEmail, String otpCode);

    /**
     * Gửi email đặt lại mật khẩu kèm token (thời hạn 15 phút) - M-01.2.
     * @param toEmail       Địa chỉ email người nhận
     * @param resetToken    UUID token reset password
     * @param resetUrl      Đường dẫn đầy đủ để người dùng bấm vào
     */
    void sendPasswordResetEmail(String toEmail, String resetToken, String resetUrl);

    /**
     * Gửi email xác nhận đặt vé kèm QR e-ticket sau thanh toán thành công - M-09.1.
     * @param toEmail       Email khách hàng
     * @param bookingCode   Mã đặt vé
     * @param movieTitle    Tên phim
     * @param showtime      Thông tin suất chiếu (ngày, giờ, phòng, rạp)
     * @param seatInfo      Thông tin ghế
     * @param qrImageBase64 Ảnh QR dạng base64 (tuỳ chọn, có thể null nếu gửi sau)
     */
    void sendBookingConfirmationEmail(String toEmail, String bookingCode,
                                      String movieTitle, String showtime,
                                      String seatInfo, String qrImageBase64);
}
