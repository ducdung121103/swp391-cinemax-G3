package com.cinema.common.transaction;

import com.cinema.common.context.DBContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * TransactionManager quản lý Transaction phân tán lồng nhau (Nested Transactions)
 * sử dụng ThreadLocal cô lập Connection theo từng luồng request của Tomcat.
 * 
 * Quy tắc an toàn:
 * - Chỉ Root Transaction (depth == 0) mới có quyền mở kết nối, setAutoCommit(false),
 *   commit, rollback và close kết nối.
 * - Các hàm con gọi lồng bên trong sẽ tái sử dụng Connection hiện tại của Root,
 *   tránh lỗi SQLException: Connection is closed.
 */
public class TransactionManager {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private static final ThreadLocal<Integer> depthHolder = ThreadLocal.withInitial(() -> 0);

    private TransactionManager() {}

    /**
     * Lấy Connection hiện tại của Transaction (nếu có) hoặc mở mới.
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = connectionHolder.get();
        if (conn == null || conn.isClosed()) {
            conn = DBContext.getConnection();
            connectionHolder.set(conn);
        }
        return conn;
    }

    /**
     * Thực thi một chuỗi tác vụ nghiệp vụ bên trong một Database Transaction duy nhất.
     */
    public static <T> T executeInTransaction(TransactionCallable<T> action) {
        Connection conn = null;
        int depth = depthHolder.get();
        boolean isRoot = (depth == 0);

        try {
            if (isRoot) {
                conn = DBContext.getConnection();
                conn.setAutoCommit(false); // Bắt đầu Transaction tại Root
                connectionHolder.set(conn);
            } else {
                conn = connectionHolder.get(); // Tái sử dụng Connection của Root
            }
            depthHolder.set(depth + 1);

            T result = action.call(conn); // Thực thi logic nghiệp vụ

            if (isRoot) {
                conn.commit(); // Chỉ Root mới được Commit xuống DB
            }
            return result;
        } catch (Exception e) {
            if (isRoot && conn != null) {
                try {
                    conn.rollback(); // Lỗi bất kỳ tầng nào -> Root Rollback toàn bộ sạch sẽ
                    System.err.println("[TransactionManager] Rollback thành công do lỗi: " + e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Giao dịch thất bại: " + e.getMessage(), e);
        } finally {
            depthHolder.set(depth); // Trả lại độ sâu trước đó
            if (isRoot) {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close(); // Trả Connection về HikariCP Pool
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                connectionHolder.remove(); // Giải phóng ThreadLocal chống memory leak
                depthHolder.remove();
            }
        }
    }

    @FunctionalInterface
    public interface TransactionCallable<T> {
        T call(Connection conn) throws Exception;
    }
}
