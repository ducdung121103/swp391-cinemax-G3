package com.cinema.modules.identity.dao;

import com.cinema.common.context.DBContext;
import com.cinema.model.Role;
import com.cinema.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO quản lý người dùng và vai trò trong module identity.
 * Module Identity — TV 2 (Cường) — M-01.1 → M-01.4
 */
public class UserDAO {

    // ────────────────────────────────────────────────────────────────────────────
    // FIND (READ)
    // ────────────────────────────────────────────────────────────────────────────

    /** Tìm user theo email (bao gồm cả tài khoản UNVERIFIED để xử lý OTP). */
    public User findByEmail(String email) {
        String sql = "SELECT u.*, r.role_name, r.description AS role_desc " +
                     "FROM users u JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.email = ? AND u.is_deleted = 0";
        return querySingle(sql, ps -> ps.setString(1, email));
    }

    /** Tìm user theo số điện thoại (chống trùng lặp số điện thoại). */
    public User findByPhone(String phone) {
        if (phone == null || phone.isBlank()) return null;
        String sql = "SELECT u.*, r.role_name, r.description AS role_desc " +
                     "FROM users u JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.phone = ? AND u.is_deleted = 0";
        return querySingle(sql, ps -> ps.setString(1, phone.trim()));
    }

    /** Tìm user theo ID. */
    public User findById(Long id) {
        String sql = "SELECT u.*, r.role_name, r.description AS role_desc " +
                     "FROM users u JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.id = ? AND u.is_deleted = 0";
        return querySingle(sql, ps -> ps.setLong(1, id));
    }

    /** Lấy toàn bộ danh sách user (cho Admin User Management - M-01.4). */
    public List<User> findAll(int page, int pageSize) {
        String sql = "SELECT u.*, r.role_name, r.description AS role_desc " +
                     "FROM users u JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.is_deleted = 0 " +
                     "ORDER BY u.created_at DESC " +
                     "OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        List<User> users = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, (page - 1) * pageSize);
            ps.setInt(2, pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /** Đếm tổng số user (dùng cho phân trang). */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM users WHERE is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // INSERT
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Tạo tài khoản Customer mới với trạng thái UNVERIFIED (chờ xác thực email OTP).
     * @return ID được sinh ra, hoặc null nếu lỗi (email/phone đã tồn tại)
     */
    public Long insertCustomer(User user) {
        String sql = "INSERT INTO users (role_id, email, password_hash, full_name, phone, " +
                     "loyalty_points, status, email_verified, otp_code, otp_expires_at) " +
                     "VALUES ((SELECT id FROM roles WHERE role_name = 'CUSTOMER'), " +
                     "?, ?, ?, ?, 0, 'UNVERIFIED', 0, ?, ?)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getPhone());
            ps.setString(5, user.getOtpCode());
            ps.setObject(6, user.getOtpExpiresAt()); // LocalDateTime → DATETIME2
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        } catch (SQLException e) {
            // Email hoặc Phone đã tồn tại → trả về null
            e.printStackTrace();
        }
        return null;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // OTP OPERATIONS — M-01.2
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Cập nhật mã OTP mới cho user (dùng khi gửi lại OTP).
     */
    public boolean saveOtp(Long userId, String otpCode, LocalDateTime expiresAt) {
        String sql = "UPDATE users SET otp_code = ?, otp_expires_at = ?, updated_at = GETDATE() " +
                     "WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, otpCode);
            ps.setObject(2, expiresAt);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Xác thực OTP và kích hoạt tài khoản (status ACTIVE, email_verified = 1).
     * Chỉ thành công khi OTP đúng VÀ chưa hết hạn.
     *
     * @return true nếu xác thực thành công
     */
    public boolean verifyOtpAndActivate(String email, String otpCode) {
        // Kiểm tra OTP đúng và còn hạn
        String checkSql = "SELECT id FROM users " +
                          "WHERE email = ? AND otp_code = ? " +
                          "AND otp_expires_at > GETDATE() " +
                          "AND status = 'UNVERIFIED' AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, email);
            check.setString(2, otpCode);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) {
                    Long userId = rs.getLong("id");
                    // Kích hoạt tài khoản
                    String updateSql = "UPDATE users SET status = 'ACTIVE', email_verified = 1, " +
                                       "otp_code = NULL, otp_expires_at = NULL, updated_at = GETDATE() " +
                                       "WHERE id = ?";
                    try (PreparedStatement update = conn.prepareStatement(updateSql)) {
                        update.setLong(1, userId);
                        return update.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // UPDATE OPERATIONS
    // ────────────────────────────────────────────────────────────────────────────

    /** Cập nhật hash mật khẩu mới (dùng cho changePassword và resetPassword). */
    public boolean updatePassword(Long userId, String newPasswordHash) {
        String sql = "UPDATE users SET password_hash = ?, updated_at = GETDATE() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPasswordHash);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Lưu token reset password và thời hạn (M-01.2 - Forgot Password). */
    public boolean saveResetToken(Long userId, String token, LocalDateTime expiresAt) {
        String sql = "UPDATE users SET reset_password_token = ?, reset_token_expires_at = ?, " +
                     "updated_at = GETDATE() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setObject(2, expiresAt);
            ps.setLong(3, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Tìm user theo reset token (còn hạn) — dùng ở trang đặt lại mật khẩu. */
    public User findByValidResetToken(String token) {
        String sql = "SELECT u.*, r.role_name, r.description AS role_desc " +
                     "FROM users u JOIN roles r ON u.role_id = r.id " +
                     "WHERE u.reset_password_token = ? AND u.reset_token_expires_at > GETDATE() " +
                     "AND u.is_deleted = 0";
        return querySingle(sql, ps -> ps.setString(1, token));
    }

    /** Xóa token reset sau khi dùng xong. */
    public boolean clearResetToken(Long userId) {
        String sql = "UPDATE users SET reset_password_token = NULL, reset_token_expires_at = NULL, " +
                     "updated_at = GETDATE() WHERE id = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cập nhật thông tin hồ sơ cá nhân (M-01.3 - Customer Profile).
     */
    public boolean updateProfile(Long userId, String fullName, String phone, String avatarUrl) {
        String sql = "UPDATE users SET full_name = ?, phone = ?, avatar_url = ?, updated_at = GETDATE() " +
                     "WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName);
            ps.setString(2, phone);
            ps.setString(3, avatarUrl);
            ps.setLong(4, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Khóa / mở khóa tài khoản (M-01.4 - Admin User Management).
     * @param status 'ACTIVE' hoặc 'LOCKED'
     */
    public boolean updateStatus(Long userId, String status) {
        String sql = "UPDATE users SET status = ?, updated_at = GETDATE() WHERE id = ? AND is_deleted = 0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ────────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ────────────────────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface StatementSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private User querySingle(String sql, StatementSetter setter) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setRoleId(rs.getLong("role_id"));
        u.setEmail(rs.getString("email"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setPhone(rs.getString("phone"));
        u.setLoyaltyPoints(rs.getInt("loyalty_points"));
        u.setStatus(rs.getString("status"));
        u.setAvatarUrl(rs.getString("avatar_url"));
        u.setCinemaId(rs.getObject("cinema_id") != null ? rs.getLong("cinema_id") : null);
        u.setTierId(rs.getObject("tier_id") != null ? rs.getLong("tier_id") : null);
        u.setEmailVerified(rs.getBoolean("email_verified"));
        u.setIs2faEnabled(rs.getBoolean("is_2fa_enabled"));
        u.setOtpCode(rs.getString("otp_code"));

        // Timestamp → LocalDateTime
        Timestamp otpExpTs = rs.getTimestamp("otp_expires_at");
        if (otpExpTs != null) u.setOtpExpiresAt(otpExpTs.toLocalDateTime());

        Timestamp resetExpTs = rs.getTimestamp("reset_token_expires_at");
        if (resetExpTs != null) u.setResetTokenExpiresAt(resetExpTs.toLocalDateTime());
        u.setResetPasswordToken(rs.getString("reset_password_token"));

        Role r = new Role();
        r.setId(rs.getLong("role_id"));
        r.setRoleName(rs.getString("role_name"));
        r.setDescription(rs.getString("role_desc"));
        u.setRole(r);

        return u;
    }
}
