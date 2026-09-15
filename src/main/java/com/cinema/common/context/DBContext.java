package com.cinema.common.context;

import com.cinema.common.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DBContext quản lý tập trung Connection Pool (HikariCP) cho toàn bộ ứng dụng.
 * Đảm bảo hiệu năng cao, chống rò rỉ kết nối (Memory/Connection Leak).
 */
public class DBContext {
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(DatabaseConfig.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            config.setJdbcUrl(DatabaseConfig.getProperty("db.url", "jdbc:mysql://localhost:3306/cinema_chain_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8"));
            config.setUsername(DatabaseConfig.getProperty("db.username", "root"));
            config.setPassword(DatabaseConfig.getProperty("db.password", "123456"));

            // Cấu hình Pool
            config.setMaximumPoolSize(DatabaseConfig.getIntProperty("hikari.maximumPoolSize", 10));
            config.setMinimumIdle(DatabaseConfig.getIntProperty("hikari.minimumIdle", 2));
            config.setIdleTimeout(DatabaseConfig.getIntProperty("hikari.idleTimeout", 30000));
            config.setMaxLifetime(DatabaseConfig.getIntProperty("hikari.maxLifetime", 1800000));
            config.setConnectionTimeout(DatabaseConfig.getIntProperty("hikari.connectionTimeout", 10000));

            // Tối ưu hóa cho MySQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");

            dataSource = new HikariDataSource(config);
            System.out.println("[DBContext] Khởi tạo HikariCP Connection Pool thành công!");
        } catch (Exception e) {
            System.err.println("[DBContext] Lỗi khởi tạo HikariDataSource: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBContext() {}

    /**
     * Cấp phát một Connection từ HikariCP Pool.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Đóng Connection Pool khi ứng dụng tắt.
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DBContext] Đã đóng HikariCP Connection Pool!");
        }
    }
}
