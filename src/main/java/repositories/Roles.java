package repositories;

import config.DBContext;
import models.Role;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Roles extends DBContext {

    /**
     * Lấy Role ID dựa trên tên Role (Ví dụ: "CUSTOMER")
     * Dùng cho việc đăng ký tài khoản mới.
     */
    public Integer getRoleIdByName(String roleName) {
        String sql = "SELECT role_id FROM roles WHERE role_name = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, roleName);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("role_id");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lấy thông tin Role dựa trên ID
     * Dùng cho việc Login để biết user thuộc nhóm nào mà redirect.
     */
    public Role getRoleById(int roleId) {
        String sql = "SELECT * FROM roles WHERE role_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, roleId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    Role role = new Role();
                    role.setRoleId(rs.getInt("role_id"));
                    role.setRoleName(rs.getString("role_name"));
                    if (rs.getTimestamp("created_at") != null)
                        role.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    if (rs.getTimestamp("updated_at") != null)
                        role.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    return role;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get all roles from the database
     */
    public java.util.List<Role> getAllRoles() {
        String sql = "SELECT * FROM roles ORDER BY role_name";
        java.util.List<Role> roles = new java.util.ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("role_id"));
                role.setRoleName(rs.getString("role_name"));
                if (rs.getTimestamp("created_at") != null)
                    role.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                if (rs.getTimestamp("updated_at") != null)
                    role.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                roles.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Get ADMIN, CINEMA_STAFF, STAFF and BRANCH_MANAGER roles for user creation
     */
    public java.util.List<Role> getCreatableRoles() {
        String sql = "SELECT * FROM roles WHERE role_name IN ('ADMIN', 'CINEMA_STAFF', 'STAFF', 'BRANCH_MANAGER') ORDER BY role_name";
        java.util.List<Role> roles = new java.util.ArrayList<>();
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next()) {
                Role role = new Role();
                role.setRoleId(rs.getInt("role_id"));
                role.setRoleName(rs.getString("role_name"));
                if (rs.getTimestamp("created_at") != null)
                    role.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                if (rs.getTimestamp("updated_at") != null)
                    role.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                roles.add(role);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }
}