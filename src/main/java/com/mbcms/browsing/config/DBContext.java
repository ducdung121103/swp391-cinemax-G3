package com.mbcms.browsing.config;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DBContext - Quản lý kết nối JDBC tới Microsoft SQL Server
 */
public class DBContext {

    private static final Logger LOGGER = Logger.getLogger(DBContext.class.getName());
    private static Properties properties = new Properties();

    static {
        try (InputStream input = DBContext.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                properties.load(input);
            } else {
                LOGGER.log(Level.WARNING, "db.properties not found in classpath. Using default settings.");
                properties.setProperty("db.url", "jdbc:sqlserver://localhost:8087;databaseName=MovieBrowsingDB;encrypt=true;trustServerCertificate=true");
                properties.setProperty("db.username", "sa");
                properties.setProperty("db.password", "06112003");
            }
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Error initializing database driver: " + ex.getMessage(), ex);
        }
    }

    /**
     * Mở một kết nối mới tới database
     */
    public static Connection getConnection() throws SQLException {
        String url = properties.getProperty("db.url");
        String user = properties.getProperty("db.username");
        String pass = properties.getProperty("db.password");

        try {
            return DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            // Nếu cổng 8087 không kết nối được, thử fallback sang cổng mặc định 1433
            if (url.contains(":8087")) {
                String fallbackUrl = url.replace(":8087", ":1433");
                try {
                    LOGGER.log(Level.INFO, "Retrying connection with default port 1433...");
                    return DriverManager.getConnection(fallbackUrl, user, pass);
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Failed to connect on fallback port 1433: " + ex.getMessage());
                }
            }
            throw e;
        }
    }

    /**
     * Kiểm tra trạng thái kết nối
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Database test connection failed: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("Testing SQL Server Connection...");
        if (testConnection()) {
            System.out.println(">>> Kết nối SQL Server THÀNH CÔNG!");
        } else {
            System.err.println(">>> Kết nối THẤT BẠI! Vui lòng kiểm tra cổng kết nối (1433 hoặc 8087), tên DB và tài khoản sa.");
        }
    }
}
