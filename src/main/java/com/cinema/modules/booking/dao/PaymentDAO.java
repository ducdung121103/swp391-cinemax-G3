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
