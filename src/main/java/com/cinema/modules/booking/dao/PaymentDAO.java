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
        insertPayment(conn, bookingId, method, amount, txnNo, "SUCCESS");
    }

    public void insertPayment(Connection conn, Long bookingId, String method, BigDecimal amount,
                              String transactionNo, String status) throws SQLException {
        String sql = "INSERT INTO payments (booking_id, payment_method, amount, transaction_no, status) " +
                     "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            ps.setString(2, method != null ? method : "VNPAY");
            ps.setBigDecimal(3, amount);
            ps.setString(4, transactionNo);
            ps.setString(5, status != null ? status : "SUCCESS");
            ps.executeUpdate();
        }
    }

    public void updatePaymentStatusByBookingId(Connection conn, Long bookingId, String transactionNo,
                                              String status) throws SQLException {
        String sql = "UPDATE payments SET transaction_no = ?, status = ?, payment_time = GETDATE() WHERE booking_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionNo);
            ps.setString(2, status);
            ps.setLong(3, bookingId);
            ps.executeUpdate();
        }
    }
}
