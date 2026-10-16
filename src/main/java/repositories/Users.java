package repositories;

import config.DBContext;
import models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class Users extends DBContext {

    public Connection getConnection() {
        return connection;
    }

    // Helper map ResultSet to User Object
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setUserId(rs.getInt("user_id"));
        u.setRoleId(rs.getInt("role_id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setFullName(rs.getString("fullName"));

        if (rs.getTimestamp("birthday") != null)
            u.setBirthday(rs.getTimestamp("birthday").toLocalDateTime());

        u.setPhone(rs.getString("phone"));
        u.setAvatarUrl(rs.getString("avatarURL"));
        u.setStatus(rs.getString("status"));
        u.setPoints(rs.getInt("points"));
        
        u.setTotalAccumulatedPoints(rs.getInt("total_accumulated_points"));
        u.setTierId(rs.getInt("tier_id"));
        try {
            int bId = rs.getInt("branch_id");
            if (!rs.wasNull()) {
                u.setBranchId(bId);
            }
        } catch (SQLException ignore) {
            // column may not exist in older schemas
        }

        if (rs.getTimestamp("created_at") != null)
            u.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null)
            u.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        if (rs.getTimestamp("last_login") != null)
            u.setLastLogin(rs.getTimestamp("last_login").toLocalDateTime());

        return u;
    }

    public User findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, username);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findByPhone(String phone) {
        String sql = "SELECT * FROM users WHERE phone = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, phone);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(User u) {
        String sql = "INSERT INTO users (role_id, username, email, password, fullName, birthday, phone, status, points, total_accumulated_points, tier_id, branch_id) "
                +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, u.getRoleId());
            st.setString(2, u.getUsername());
            st.setString(3, u.getEmail());
            st.setString(4, u.getPassword());
            st.setString(5, u.getFullName());

            if (u.getBirthday() != null)
                st.setTimestamp(6, Timestamp.valueOf(u.getBirthday()));
            else
                st.setNull(6, java.sql.Types.TIMESTAMP);

            st.setString(7, u.getPhone());
            st.setString(8, u.getStatus());
            st.setInt(9, u.getPoints() != null ? u.getPoints() : 0);
            st.setInt(10, u.getTotalAccumulatedPoints() != null ? u.getTotalAccumulatedPoints() : 0);
            st.setInt(11, u.getTierId() != null ? u.getTierId() : 1);
            if (u.getBranchId() != null) {
                st.setInt(12, u.getBranchId());
            } else {
                st.setNull(12, java.sql.Types.INTEGER);
            }

            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void updateLastLogin(int userId) {
        String sql = "UPDATE users SET last_login = SYSDATETIME() WHERE user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean updateProfileInfo(User u) {
        String sql = """
            UPDATE users
            SET fullName = ?, email = ?, phone = ?, birthday = ?, updated_at = SYSDATETIME()
            WHERE user_id = ?
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, u.getFullName());
            st.setString(2, u.getEmail());
            st.setString(3, u.getPhone());
            if (u.getBirthday() != null) {
                st.setTimestamp(4, Timestamp.valueOf(u.getBirthday()));
            } else {
                st.setNull(4, java.sql.Types.TIMESTAMP);
            }
            st.setInt(5, u.getUserId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean updatePassword(User u) {
        String sql = """
            UPDATE users
            SET password = ?, updated_at = SYSDATETIME()
            WHERE user_id = ?
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, u.getPassword());
            st.setInt(2, u.getUserId());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean checkPhoneExists(String phone) {
        String sql = "SELECT 1 FROM users WHERE phone = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, phone);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAvatarUrl(int userId, String avatarUrl) {
        String sql = "UPDATE users SET avatarURL = ?, updated_at = SYSDATETIME() WHERE user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, avatarUrl);
            st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public java.util.List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        java.util.List<User> users = new java.util.ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                try {
                    users.add(mapResultSetToUser(rs));
                } catch (Exception e) {
                    System.err.println("Error mapping user at row: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public java.util.List<User> getUsersByRole(int roleId) {
        String sql = "SELECT * FROM users WHERE role_id = ? ORDER BY created_at DESC";
        java.util.List<User> users = new java.util.ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, roleId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    try {
                        users.add(mapResultSetToUser(rs));
                    } catch (Exception e) {
                        System.err.println("Error mapping user in getUsersByRole: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public java.util.List<User> getUsersByStatus(String status) {
        String sql = "SELECT * FROM users WHERE status = ? ORDER BY created_at DESC";
        java.util.List<User> users = new java.util.ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, status);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    try {
                        users.add(mapResultSetToUser(rs));
                    } catch (Exception e) {
                        System.err.println("Error mapping user in getUsersByStatus: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public java.util.List<User> searchUsers(String keyword) {
        String sql = "SELECT * FROM users WHERE username LIKE ? OR email LIKE ? OR fullName LIKE ? ORDER BY created_at DESC";
        java.util.List<User> users = new java.util.ArrayList<>();
        String searchPattern = "%" + keyword + "%";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, searchPattern);
            st.setString(2, searchPattern);
            st.setString(3, searchPattern);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    try {
                        users.add(mapResultSetToUser(rs));
                    } catch (Exception e) {
                        System.err.println("Error mapping user in searchUsers: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public boolean updateUserStatus(int userId, String status) {
        String sql = "UPDATE users SET status = ? WHERE user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, status);
            st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkUsernameExists(String username) {
        return findByUsername(username) != null;
    }

    public boolean checkEmailExists(String email) {
        return findByEmail(email) != null;
    }

    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get all branch managers (including assigned ones)
    public java.util.List<User> getAllBranchManagers() {
        java.util.List<User> managers = new java.util.ArrayList<>();
        String sql = "SELECT u.* FROM users u " +
                "INNER JOIN roles r ON u.role_id = r.role_id " +
                "WHERE r.role_name = 'BRANCH_MANAGER' " +
                "AND u.status = 'ACTIVE' " +
                "ORDER BY u.fullName ASC";
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                managers.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return managers;
    }

    /**
     * Tìm user dựa trên email.
     * Dùng cho chức năng Quên mật khẩu và Đăng nhập.
     * 
     * @param email Email cần tìm
     * @return User object nếu tìm thấy, null nếu không có.
     */
    public User getByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, email);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUser(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Cập nhật mật khẩu mới cho user dựa trên email.
     */
    public boolean updatePassword(String email, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE email = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, newPassword); // Lưu ý: Nên mã hóa password (MD5/BCrypt) trước khi truyền vào đây
            st.setString(2, email);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Cộng thêm điểm cho user: vừa cập nhật points hiện tại,
     * vừa cập nhật total_accumulated_points.
     *
     * @param userId      ID user cần cộng điểm
     * @param deltaPoints số điểm cộng (dương). Nếu <= 0 sẽ không làm gì.
     * @return true nếu cập nhật thành công, ngược lại false.
     */
    public boolean addPoints(int userId, int deltaPoints) {
        if (deltaPoints <= 0) {
            return false;
        }

        String sql = """
                UPDATE users
                SET points = points + ?,
                    total_accumulated_points = total_accumulated_points + ?
                WHERE user_id = ?
                """;

        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, deltaPoints);
            st.setInt(2, deltaPoints);
            st.setInt(3, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Trừ điểm hiện có của user (dùng khi quy đổi điểm).
     * Không ảnh hưởng total_accumulated_points.
     *
     * @param userId          ID user
     * @param pointsToRedeem  số điểm muốn trừ (dương)
     * @return true nếu trừ thành công, false nếu không đủ điểm hoặc lỗi
     */
    public boolean redeemPoints(int userId, int pointsToRedeem) {
        if (pointsToRedeem <= 0) {
            return false;
        }

        String sql = "UPDATE users SET points = points - ? WHERE user_id = ? AND points >= ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, pointsToRedeem);
            st.setInt(2, userId);
            st.setInt(3, pointsToRedeem);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Lấy danh sách CINEMA_STAFF và BRANCH_MANAGER chưa được gán branch */
    public java.util.List<User> getUnassignedManagers() {
        String sql = """
            SELECT u.* FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE r.role_name = 'BRANCH_MANAGER' AND u.branch_id IS NULL AND u.status = 'ACTIVE'
            ORDER BY u.fullName ASC
            """;
        java.util.List<User> list = new java.util.ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy danh sách CINEMA_STAFF thuộc một branch */
    public java.util.List<User> getStaffByBranch(int branchId) {
        String sql = """
            SELECT u.* FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE r.role_name = 'CINEMA_STAFF' AND u.branch_id = ? AND u.status = 'ACTIVE'
            ORDER BY u.fullName ASC
            """;
        java.util.List<User> list = new java.util.ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, branchId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) list.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Lấy danh sách CINEMA_STAFF chưa được gán branch (branch_id IS NULL) */
    public java.util.List<User> getUnassignedStaff() {
        String sql = """
            SELECT u.* FROM users u
            JOIN roles r ON u.role_id = r.role_id
            WHERE r.role_name = 'CINEMA_STAFF' AND u.branch_id IS NULL AND u.status = 'ACTIVE'
            ORDER BY u.fullName ASC
            """;
        java.util.List<User> list = new java.util.ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql);
             ResultSet rs = st.executeQuery()) {
            while (rs.next()) list.add(mapResultSetToUser(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    /** Gán staff vào branch (hoặc null để gỡ ra) */
    public boolean updateBranchAssignment(int userId, Integer branchId) {
        String sql = "UPDATE users SET branch_id = ?, updated_at = SYSDATETIME() WHERE user_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            if (branchId != null) st.setInt(1, branchId);
            else st.setNull(1, java.sql.Types.INTEGER);
            st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }

    public boolean updateTier(int userId) {
        String sql = """
            UPDATE users
            SET tier_id = (
                SELECT TOP 1 tier_id
                FROM membership_tiers
                WHERE min_points_required <= (SELECT total_accumulated_points FROM users WHERE user_id = ?)
                ORDER BY min_points_required DESC
            )
            WHERE user_id = ?
        """;
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, userId);
            st.setInt(2, userId);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}