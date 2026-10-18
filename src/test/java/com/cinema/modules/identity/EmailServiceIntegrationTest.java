package com.cinema.modules.identity;

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
}
