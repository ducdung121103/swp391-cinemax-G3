package repositories;

import config.DBContext;
import models.CinemaBranch;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CinemaBranches extends DBContext {

    // Mapper: Chuyển dữ liệu từ SQL sang Object Java
    private CinemaBranch mapRow(ResultSet rs) throws SQLException {
        CinemaBranch b = new CinemaBranch();
        b.setBranchId(rs.getInt("branch_id"));
        b.setBranchName(rs.getString("branch_name"));
        b.setAddress(rs.getString("address"));
        b.setPhone(rs.getString("phone"));
        b.setEmail(rs.getString("email"));

        int mgrId = rs.getInt("manager_id");
        // Kiểm tra null cho manager_id
        b.setManagerId(rs.wasNull() ? null : mgrId);

        b.setActive(rs.getBoolean("is_active"));

        // Convert Timestamp SQL -> LocalDateTime Java
        if (rs.getTimestamp("created_at") != null)
            b.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        if (rs.getTimestamp("updated_at") != null)
            b.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());

        try {
            b.setManagerName(rs.getString("manager_name"));
        } catch (SQLException e) {
        }

        return b;
    }

    public List<CinemaBranch> findAll(String keyword, Boolean isActive, int page, int pageSize) {
        List<CinemaBranch> list = new ArrayList<>();
        int offset = (page - 1) * pageSize;

        StringBuilder sql = new StringBuilder(
                "SELECT b.*, u.fullName as manager_name " +
                        "FROM cinema_branches b " +
                        "LEFT JOIN users u ON b.manager_id = u.user_id " +
                        "WHERE 1=1 ");

        // Nối chuỗi SQL
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND b.branch_name LIKE ?");
        }
        if (isActive != null) {
            sql.append(" AND b.is_active = ?");
        }
        sql.append(" ORDER BY b.branch_id DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            // Truyền tham số với kiểu dữ liệu rõ ràng (Chống lỗi JDBC)
            if (keyword != null && !keyword.trim().isEmpty()) {
                st.setString(paramIndex++, "%" + keyword.trim() + "%");
            }
            if (isActive != null) {
                st.setBoolean(paramIndex++, isActive);
            }

            // Phân trang bắt buộc phải dùng setInt
            st.setInt(paramIndex++, offset);
            st.setInt(paramIndex++, pageSize);

            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public int countAll(String keyword, Boolean isActive) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM cinema_branches WHERE 1=1");

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND branch_name LIKE ?");
        }
        if (isActive != null) {
            sql.append(" AND is_active = ?");
        }

        try (PreparedStatement st = connection.prepareStatement(sql.toString())) {
            int paramIndex = 1;

            if (keyword != null && !keyword.trim().isEmpty()) {
                st.setString(paramIndex++, "%" + keyword.trim() + "%");
            }
            if (isActive != null) {
                st.setBoolean(paramIndex++, isActive);
            }

            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public CinemaBranch findById(int id) {
        // Cũng dùng LEFT JOIN ở đây để khi Edit form hiện lên có thể biết tên quản lý
        // cũ
        String sql = "SELECT b.*, u.fullName as manager_name " +
                "FROM cinema_branches b " +
                "LEFT JOIN users u ON b.manager_id = u.user_id " +
                "WHERE b.branch_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next())
                    return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insert(CinemaBranch b) {
        String sql = "INSERT INTO cinema_branches (branch_name, address, phone, email, manager_id, is_active) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, b.getBranchName());
            st.setString(2, b.getAddress());
            st.setString(3, b.getPhone());
            st.setString(4, b.getEmail());

            if (b.getManagerId() != null)
                st.setInt(5, b.getManagerId());
            else
                st.setNull(5, java.sql.Types.INTEGER);

            st.setBoolean(6, b.isActive());
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(CinemaBranch b) {
        String sql = "UPDATE cinema_branches SET branch_name=?, address=?, phone=?, email=?, manager_id=?, is_active=?, updated_at=SYSDATETIME() WHERE branch_id=?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, b.getBranchName());
            st.setString(2, b.getAddress());
            st.setString(3, b.getPhone());
            st.setString(4, b.getEmail());

            if (b.getManagerId() != null)
                st.setInt(5, b.getManagerId());
            else
                st.setNull(5, java.sql.Types.INTEGER);

            st.setBoolean(6, b.isActive());
            st.setInt(7, b.getBranchId());

            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Get all active branches
    public List<CinemaBranch> getActiveBranches() {
        List<CinemaBranch> list = new ArrayList<>();
        String sql = "SELECT * FROM cinema_branches WHERE is_active = 1 ORDER BY branch_name ASC";
        try (PreparedStatement st = connection.prepareStatement(sql);
                ResultSet rs = st.executeQuery()) {
            while (rs.next())
                list.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM cinema_branches WHERE branch_id = ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, id);
            return st.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Lấy danh sách các chi nhánh do 1 manager quản lý
    public List<CinemaBranch> findListByManagerId(int managerId) {
        List<CinemaBranch> list = new ArrayList<>();
        String sql = "SELECT b.*, u.fullName as manager_name " +
                "FROM cinema_branches b " +
                "LEFT JOIN users u ON b.manager_id = u.user_id " +
                "WHERE b.manager_id = ? AND b.is_active = 1";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, managerId);
            try (ResultSet rs = st.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // =========================================================================
    // CÁC HÀM KIỂM TRA TRÙNG LẶP (UNIQUE VALIDATION)
    // =========================================================================

    public boolean isNameExists(String branchName, int excludeBranchId) {
        String sql = "SELECT COUNT(*) FROM cinema_branches WHERE branch_name = ? AND branch_id != ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, branchName.trim());
            st.setInt(2, excludeBranchId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isEmailExists(String email, int excludeBranchId) {
        String sql = "SELECT COUNT(*) FROM cinema_branches WHERE email = ? AND branch_id != ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, email.trim());
            st.setInt(2, excludeBranchId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isPhoneExists(String phone, int excludeBranchId) {
        String sql = "SELECT COUNT(*) FROM cinema_branches WHERE phone = ? AND branch_id != ?";
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, phone.trim());
            st.setInt(2, excludeBranchId);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}