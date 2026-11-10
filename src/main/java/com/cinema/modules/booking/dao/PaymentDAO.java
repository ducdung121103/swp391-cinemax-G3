package com.cinema.modules.booking.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import com.cinema.model.Payment;

/**
 * DAO quản lý bảng payments (TV 4).
 */
public class PaymentDAO {

    public void insertPayment(Connection conn, Long bookingId, String method, BigDecimal amount) throws SQLException {
        String txnNo = "TXN-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        insertPayment(conn, bookingId, method, amount, txnNo, "SUCCESS");
    }

    public void insertPendingVnpayPayment(Connection conn, Long bookingId, BigDecimal amount) throws SQLException {
        insertPayment(conn, bookingId, "VNPAY", amount, null, "PENDING");
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

    public Payment findByBookingIdForUpdate(Connection conn, Long bookingId) throws SQLException {
        String sql = "SELECT TOP 1 id, booking_id, payment_method, amount, transaction_no, payment_time, status "
                + "FROM payments WITH (UPDLOCK, HOLDLOCK) WHERE booking_id = ? ORDER BY id DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Payment payment = new Payment();
                payment.setId(rs.getLong("id"));
                payment.setBookingId(rs.getLong("booking_id"));
                payment.setPaymentMethod(rs.getString("payment_method"));
                payment.setAmount(rs.getBigDecimal("amount"));
                payment.setTransactionNo(rs.getString("transaction_no"));
                payment.setStatus(rs.getString("status"));
                return payment;
            }
        }
    }

    public boolean updatePendingVnpayPayment(Connection conn, Long bookingId, BigDecimal amount,
                                             String transactionNo, String status) throws SQLException {
        String sql = "UPDATE payments SET transaction_no = ?, status = ?, payment_time = GETDATE() "
                + "WHERE booking_id = ? AND payment_method = 'VNPAY' AND status = 'PENDING' AND amount = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionNo);
            ps.setString(2, status);
            ps.setLong(3, bookingId);
            ps.setBigDecimal(4, amount);
            return ps.executeUpdate() == 1;
        }
    }
}
