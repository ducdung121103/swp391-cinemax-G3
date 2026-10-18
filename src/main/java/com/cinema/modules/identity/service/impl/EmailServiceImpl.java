package com.cinema.modules.identity.service.impl;

import com.cinema.modules.identity.service.EmailService;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Triển khai EmailService thật qua Gmail SMTP (TLS port 587).
 * Cấu hình trong: src/main/resources/mail.properties
 * Yêu cầu: Gmail App Password (bật 2-Step Verification trước).
 *
 * Module Identity — TV 2 (Cường) — M-01.2, M-09.1
 */
public class EmailServiceImpl implements EmailService {

    private static final Logger LOGGER = Logger.getLogger(EmailServiceImpl.class.getName());

    // Cấu hình SMTP đọc từ mail.properties
    private final String smtpHost;
    private final String smtpPort;
    private final String username;
    private final String password;
    private final String fromName;
    private final String fromAddress;
    private final int otpExpiryMinutes;

    // Chủ đề màu sắc thương hiệu CineMax
    private static final String PRIMARY_COLOR = "#E50914";
    private static final String BG_DARK       = "#0D0F14";
    private static final String CARD_COLOR    = "#141820";
    private static final String TEXT_LIGHT    = "#EAEAEA";
    private static final String GOLD_COLOR    = "#F5C518";

    public EmailServiceImpl() {
        Properties props = loadMailProperties();
        this.smtpHost        = props.getProperty("mail.smtp.host", "smtp.gmail.com");
        this.smtpPort        = props.getProperty("mail.smtp.port", "587");
        this.username        = props.getProperty("mail.username", "");
        String rawPass       = props.getProperty("mail.password", "");
        this.password        = rawPass != null ? rawPass.replaceAll("\\s+", "") : "";
        this.fromName        = props.getProperty("mail.from.name", "CineMax Cinema");
        this.fromAddress     = props.getProperty("mail.from.address", this.username);
        this.otpExpiryMinutes = Integer.parseInt(props.getProperty("mail.otp.expiry.minutes", "10"));
    }

    // ────────────────────────────────────────────────────────────────────────────
    // PUBLIC API
    // ────────────────────────────────────────────────────────────────────────────

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        String subject = "🎬 [CineMax] Mã xác thực OTP của bạn";
        String htmlBody = buildOtpEmailHtml(toEmail, otpCode);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken, String resetUrl) {
        String subject = "🔐 [CineMax] Yêu cầu đặt lại mật khẩu";
        String htmlBody = buildPasswordResetEmailHtml(toEmail, resetUrl);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    @Override
    public void sendBookingConfirmationEmail(String toEmail, String bookingCode,
                                              String movieTitle, String showtime,
                                              String seatInfo, String qrImageBase64) {
        String subject = "🎫 [CineMax] Xác nhận đặt vé — " + movieTitle;
        String htmlBody = buildBookingConfirmEmailHtml(toEmail, bookingCode, movieTitle, showtime, seatInfo);
        sendHtmlEmail(toEmail, subject, htmlBody);
    }

    // ────────────────────────────────────────────────────────────────────────────
    // PRIVATE — SMTP CORE
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Gửi email HTML qua Gmail SMTP. Chạy trên luồng chính (đồng bộ).
     * Nếu lỗi SMTP → chỉ log, không ném exception ra ngoài để tránh rollback transaction.
     */
    private void sendHtmlEmail(String toEmail, String subject, String htmlBody) {
        // Nếu chưa cấu hình mail.properties → chỉ log ra console, không báo lỗi
        if (username == null || username.isBlank() || username.equals("your-email@gmail.com")) {
            LOGGER.warning("[EMAIL STUB] mail.properties chưa được cấu hình. Gửi đến: "
                    + toEmail + " | Subject: " + subject);
            System.out.println("[EMAIL STUB] → " + toEmail + " | " + subject);
            return;
        }

        Properties smtpProps = new Properties();
        smtpProps.put("mail.smtp.host", smtpHost);
        smtpProps.put("mail.smtp.port", smtpPort);
        smtpProps.put("mail.smtp.auth", "true");
        smtpProps.put("mail.smtp.starttls.enable", "true");
        smtpProps.put("mail.smtp.starttls.required", "true");
        smtpProps.put("mail.smtp.ssl.protocols", "TLSv1.2");
        smtpProps.put("mail.smtp.connectiontimeout", "10000");
        smtpProps.put("mail.smtp.timeout", "15000");

        Session session = Session.getInstance(smtpProps, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromAddress, fromName, "UTF-8"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject, "UTF-8");
            message.setContent(htmlBody, "text/html; charset=UTF-8");

            Transport.send(message);
            LOGGER.info("[EMAIL] Gửi thành công → " + toEmail + " | " + subject);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "[EMAIL ERROR] Không thể gửi email đến: " + toEmail, e);
        }
    }

    // ────────────────────────────────────────────────────────────────────────────
    // PRIVATE — HTML TEMPLATE BUILDERS
    // ────────────────────────────────────────────────────────────────────────────

    /** Template email gửi OTP xác thực tài khoản */
    private String buildOtpEmailHtml(String toEmail, String otpCode) {
        // Tách từng chữ số để hiển thị ô vuông đẹp
        String[] digits = otpCode.split("");
        StringBuilder digitBoxes = new StringBuilder();
        for (String d : digits) {
            digitBoxes.append(
                "<span style='display:inline-block;width:48px;height:58px;line-height:58px;" +
                "text-align:center;font-size:28px;font-weight:800;color:" + GOLD_COLOR + ";" +
                "background:#1E2333;border:2px solid #2E3550;border-radius:10px;margin:0 4px;'>" +
                d + "</span>"
            );
        }

        return buildEmailWrapper(
            "Xác Thực Tài Khoản CineMax",
            "<p style='color:" + TEXT_LIGHT + ";font-size:15px;'>Chào mừng bạn đến với <strong style='color:" + PRIMARY_COLOR + ";'>CineMax</strong>! 🎬</p>" +
            "<p style='color:#9AA0B2;font-size:14px;'>Nhập mã OTP dưới đây để hoàn tất đăng ký tài khoản:</p>" +
            "<div style='text-align:center;margin:30px 0;'>" + digitBoxes + "</div>" +
            "<p style='text-align:center;color:#9AA0B2;font-size:13px;'>⏰ Mã có hiệu lực trong <strong style='color:" + GOLD_COLOR + ";'>" + otpExpiryMinutes + " phút</strong> kể từ khi nhận email.</p>" +
            "<hr style='border:none;border-top:1px solid #2E3550;margin:20px 0;'/>" +
            "<p style='color:#5C6380;font-size:12px;'>Nếu bạn không thực hiện yêu cầu này, hãy bỏ qua email này. Tài khoản của bạn vẫn an toàn.</p>"
        );
    }

    /** Template email reset mật khẩu */
    private String buildPasswordResetEmailHtml(String toEmail, String resetUrl) {
        return buildEmailWrapper(
            "Đặt Lại Mật Khẩu",
            "<p style='color:" + TEXT_LIGHT + ";font-size:15px;'>Chúng tôi nhận được yêu cầu <strong>đặt lại mật khẩu</strong> cho tài khoản CineMax của bạn.</p>" +
            "<p style='color:#9AA0B2;font-size:14px;'>Bấm vào nút bên dưới để tạo mật khẩu mới:</p>" +
            "<div style='text-align:center;margin:32px 0;'>" +
            "  <a href='" + resetUrl + "' style='display:inline-block;padding:14px 36px;background:" + PRIMARY_COLOR + ";" +
            "  color:#fff;text-decoration:none;border-radius:8px;font-size:15px;font-weight:700;" +
            "  letter-spacing:0.5px;'>🔐 Đặt Lại Mật Khẩu Ngay</a>" +
            "</div>" +
            "<p style='text-align:center;color:#9AA0B2;font-size:13px;'>⏰ Liên kết có hiệu lực trong <strong style='color:" + GOLD_COLOR + ";'>15 phút</strong>.</p>" +
            "<hr style='border:none;border-top:1px solid #2E3550;margin:20px 0;'/>" +
            "<p style='color:#5C6380;font-size:12px;'>Nếu bạn không yêu cầu đặt lại mật khẩu, hãy bỏ qua email này và mật khẩu cũ của bạn vẫn còn nguyên.</p>" +
            "<p style='color:#5C6380;font-size:11px;word-break:break-all;'>Hoặc copy link: " + resetUrl + "</p>"
        );
    }

    /** Template email xác nhận đặt vé */
    private String buildBookingConfirmEmailHtml(String toEmail, String bookingCode,
                                                 String movieTitle, String showtime, String seatInfo) {
        return buildEmailWrapper(
            "Xác Nhận Đặt Vé Thành Công 🎫",
            "<p style='color:" + TEXT_LIGHT + ";font-size:15px;'>Cảm ơn bạn đã đặt vé tại <strong style='color:" + PRIMARY_COLOR + ";'>CineMax</strong>!</p>" +
            "<div style='background:#1E2333;border-radius:12px;padding:20px;margin:20px 0;border-left:4px solid " + PRIMARY_COLOR + ";'>" +
            "  <p style='margin:6px 0;color:" + TEXT_LIGHT + ";font-size:14px;'>🎬 <strong>Phim:</strong> " + movieTitle + "</p>" +
            "  <p style='margin:6px 0;color:" + TEXT_LIGHT + ";font-size:14px;'>📅 <strong>Suất chiếu:</strong> " + showtime + "</p>" +
            "  <p style='margin:6px 0;color:" + TEXT_LIGHT + ";font-size:14px;'>💺 <strong>Ghế:</strong> " + seatInfo + "</p>" +
            "  <p style='margin:6px 0;color:" + GOLD_COLOR + ";font-size:14px;'>🎟️ <strong>Mã đặt vé:</strong> " + bookingCode + "</p>" +
            "</div>" +
            "<p style='color:#9AA0B2;font-size:13px;'>Vui lòng mang mã QR trong email này đến rạp để soát vé. Chúc bạn xem phim vui!</p>"
        );
    }

    /**
     * Bọc nội dung trong khung email HTML hoàn chỉnh với theme Cinema Dark.
     */
    private String buildEmailWrapper(String title, String content) {
        return "<!DOCTYPE html><html lang='vi'><head><meta charset='UTF-8'/>" +
               "<meta name='viewport' content='width=device-width,initial-scale=1'/></head>" +
               "<body style='margin:0;padding:0;background-color:#07090E;font-family:\"Segoe UI\",Arial,sans-serif;'>" +
               "  <table width='100%' cellpadding='0' cellspacing='0'><tr><td align='center' style='padding:40px 20px;'>" +
               "    <table width='580' cellpadding='0' cellspacing='0' style='background:" + CARD_COLOR + ";border-radius:16px;overflow:hidden;border:1px solid #2E3550;'>" +
               "      <!-- HEADER -->" +
               "      <tr><td style='background:linear-gradient(135deg,#1A0508,#3D0A14);padding:28px 36px;text-align:center;'>" +
               "        <p style='margin:0;font-size:28px;font-weight:900;color:#fff;letter-spacing:2px;'>" +
               "          <span style='color:" + PRIMARY_COLOR + ";'>CINE</span>MAX</p>" +
               "        <p style='margin:6px 0 0;font-size:12px;color:#9AA0B2;letter-spacing:1px;'>HỆ THỐNG CỤM RẠP CHIẾU PHIM TOÀN QUỐC</p>" +
               "      </td></tr>" +
               "      <!-- TITLE BAR -->" +
               "      <tr><td style='padding:24px 36px 0;'>" +
               "        <h2 style='margin:0;font-size:20px;font-weight:700;color:" + TEXT_LIGHT + ";border-bottom:2px solid " + PRIMARY_COLOR + ";padding-bottom:12px;'>" +
               title + "</h2>" +
               "      </td></tr>" +
               "      <!-- CONTENT -->" +
               "      <tr><td style='padding:20px 36px 32px;'>" + content + "</td></tr>" +
               "      <!-- FOOTER -->" +
               "      <tr><td style='background:#0D0F14;padding:16px 36px;text-align:center;border-top:1px solid #2E3550;'>" +
               "        <p style='margin:0;font-size:11px;color:#5C6380;'>© " + java.time.Year.now() + " CineMax — SWP391 Group 3 | Email tự động, vui lòng không reply.</p>" +
               "      </td></tr>" +
               "    </table>" +
               "  </td></tr></table>" +
               "</body></html>";
    }

    // ────────────────────────────────────────────────────────────────────────────
    // PRIVATE UTILITY
    // ────────────────────────────────────────────────────────────────────────────

    private Properties loadMailProperties() {
        Properties props = new Properties();
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("mail.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                LOGGER.warning("[EMAIL] Không tìm thấy mail.properties. Email sẽ chạy ở chế độ STUB.");
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "[EMAIL] Lỗi đọc mail.properties", e);
        }
        return props;
    }
}
