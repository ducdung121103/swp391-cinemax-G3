package com.cinema.modules.identity;

import com.cinema.common.util.QrCodeUtil;
import com.cinema.modules.identity.service.EmailService;
import com.cinema.modules.identity.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class EmailServiceIntegrationTest {

    @Test
    @Disabled("Chỉ bật khi muốn kiểm tra gửi mail thủ công")
    void testSendOtpEmail() {
        EmailService emailService = new EmailServiceImpl();
        // Gửi OTP test 6 số đến email của bạn
        emailService.sendOtpEmail("buidangcuong1612@gmail.com", "889922");
    }

    @Test
    @Disabled("Chỉ bật khi muốn kiểm tra gửi mail xác nhận vé + QR thủ công")
    void testSendBookingConfirmationEmailWithQr() {
        EmailService emailService = new EmailServiceImpl();
        String bookingCode = "BK-DEMO-QR-001";
        String qr = QrCodeUtil.toPngBase64(QrCodeUtil.bookingPayload(bookingCode), 240);
        emailService.sendBookingConfirmationEmail(
                "buidangcuong1612@gmail.com",
                bookingCode,
                "Dune: Part Two",
                "19:30 15/11/2026 · CineMax Hà Nội · Phòng 1",
                "D5, D6",
                qr
        );
    }

    @Test
    @Disabled("Chỉ bật khi muốn kiểm tra gửi OTP đổi mật khẩu thủ công")
    void testSendPasswordChangeOtpEmail() {
        EmailService emailService = new EmailServiceImpl();
        emailService.sendPasswordChangeOtpEmail("buidangcuong1612@gmail.com", "482915");
    }
}
