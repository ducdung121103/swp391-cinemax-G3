package com.cinema.modules.booking.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

/**
 * DAO quản lý bảng payments (TV 4).
 */
public class PaymentDAO {

    public void insertPayment(Connection conn, Long bookingId, String method, BigDecimal amount) throws SQLException {
        // GHI CHÚ BẢO VỆ ĐỒ ÁN (M-06.1 - M-06.3):
        // Cổng thanh toán VNPay và Tiền mặt tại quầy (POS) hiện được giả lập tự động thành công (Status = SUCCESS)
        // phục vụ mục đích kiểm thử học thuật và bảo vệ đồ án SWP391. Chưa tích hợp SHA-512 checksum, URL Redirect và IPN Webhook thật.
        String txnNo = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String sql = "INSERT INTO payments (booking_id, payment_method, amount, transaction_no, status) " +
                     "VALUES (?, ?, ?, ?, 'SUCCESS')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setString(2, method != null ? method : "VNPAY");
            ps.setBigDecimal(3, amount);
            ps.setString(4, txnNo);
            ps.executeUpdate();
        }
    }
}
