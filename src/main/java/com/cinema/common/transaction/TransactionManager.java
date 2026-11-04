package com.cinema.common.transaction;

import com.cinema.common.context.DBContext;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * TransactionManager quáº£n lÃ½ Transaction phÃ¢n tÃ¡n lá»“ng nhau (Nested Transactions)
 * sá»­ dá»¥ng ThreadLocal cÃ´ láº­p Connection theo tá»«ng luá»“ng request cá»§a Tomcat.
 * 
 * Quy táº¯c an toÃ n:
 * - Chá»‰ Root Transaction (depth == 0) má»›i cÃ³ quyá»n má»Ÿ káº¿t ná»‘i, setAutoCommit(false),
 *   commit, rollback vÃ  close káº¿t ná»‘i.
 * - CÃ¡c hÃ m con gá»i lá»“ng bÃªn trong sáº½ tÃ¡i sá»­ dá»¥ng Connection hiá»‡n táº¡i cá»§a Root,
 *   trÃ¡nh lá»—i SQLException: Connection is closed.
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public class TransactionManager {
    private static final ThreadLocal<Connection> connectionHolder = new ThreadLocal<>();
    private static final ThreadLocal<Integer> depthHolder = ThreadLocal.withInitial(() -> 0);

    private TransactionManager() {}

    public static void setTestConnection(Connection conn) {
        connectionHolder.set(conn);
    }

    public static void clearTestConnection() {
        connectionHolder.remove();
        depthHolder.remove();
    }

    /**
     * Láº¥y Connection hiá»‡n táº¡i cá»§a Transaction (náº¿u cÃ³) hoáº·c má»Ÿ má»›i.
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
     * Thá»±c thi má»™t chuá»—i tÃ¡c vá»¥ nghiá»‡p vá»¥ bÃªn trong má»™t Database Transaction duy nháº¥t.
     */
    public static <T> T executeInTransaction(TransactionCallable<T> action) {
        Connection conn = null;
        int depth = depthHolder.get();
        boolean isRoot = (depth == 0);

        try {
            if (isRoot) {
                conn = connectionHolder.get();
                if (conn == null || conn.isClosed()) {
                    conn = DBContext.getConnection();
                    connectionHolder.set(conn);
                }
                conn.setAutoCommit(false); // Báº¯t Ä‘áº§u Transaction táº¡i Root
            } else {
                conn = connectionHolder.get(); // TÃ¡i sá»­ dá»¥ng Connection cá»§a Root
            }
            depthHolder.set(depth + 1);

            T result = action.call(conn); // Thá»±c thi logic nghiá»‡p vá»¥

            if (isRoot) {
                conn.commit(); // Chá»‰ Root má»›i Ä‘Æ°á»£c Commit xuá»‘ng DB
            }
            return result;
        } catch (Exception e) {
            if (isRoot && conn != null) {
                try {
                    conn.rollback(); // Lá»—i báº¥t ká»³ táº§ng nÃ o -> Root Rollback toÃ n bá»™ sáº¡ch sáº½
                    System.err.println("[TransactionManager] Rollback thÃ nh cÃ´ng do lá»—i: " + e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw (e instanceof RuntimeException) ? (RuntimeException) e : new RuntimeException("Giao dá»‹ch tháº¥t báº¡i: " + e.getMessage(), e);
        } finally {
            depthHolder.set(depth); // Tráº£ láº¡i Ä‘á»™ sÃ¢u trÆ°á»›c Ä‘Ã³
            if (isRoot) {
                if (conn != null) {
                    try {
                        conn.setAutoCommit(true);
                        conn.close(); // Tráº£ Connection vá» HikariCP Pool
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
                connectionHolder.remove(); // Giáº£i phÃ³ng ThreadLocal chá»‘ng memory leak
                depthHolder.remove();
            }
        }
    }

    @FunctionalInterface
    public interface TransactionCallable<T> {
        T call(Connection conn) throws Exception;
    }
}

