package com.cinema.common.context;

import com.cinema.common.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * DBContext quáº£n lÃ½ táº­p trung Connection Pool (HikariCP) cho toÃ n bá»™ á»©ng dá»¥ng.
 * Äáº£m báº£o hiá»‡u nÄƒng cao, chá»‘ng rÃ² rá»‰ káº¿t ná»‘i (Memory/Connection Leak).
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public class DBContext {
    private static final HikariDataSource dataSource;

    static {
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(DatabaseConfig.getProperty("db.driver", "com.microsoft.sqlserver.jdbc.SQLServerDriver"));
            config.setJdbcUrl(DatabaseConfig.getProperty("db.url", "jdbc:sqlserver://localhost:1433;databaseName=cinema_chain_db;encrypt=true;trustServerCertificate=true"));
            config.setUsername(DatabaseConfig.getProperty("db.username", "sa"));
            config.setPassword(DatabaseConfig.getProperty("db.password", "123456"));

            // Cáº¥u hÃ¬nh Pool
            config.setMaximumPoolSize(DatabaseConfig.getIntProperty("hikari.maximumPoolSize", 10));
            config.setMinimumIdle(DatabaseConfig.getIntProperty("hikari.minimumIdle", 2));
            config.setIdleTimeout(DatabaseConfig.getIntProperty("hikari.idleTimeout", 30000));
            config.setMaxLifetime(DatabaseConfig.getIntProperty("hikari.maxLifetime", 1800000));
            config.setConnectionTimeout(DatabaseConfig.getIntProperty("hikari.connectionTimeout", 10000));

            dataSource = new HikariDataSource(config);
            System.out.println("[DBContext] Khá»Ÿi táº¡o HikariCP Connection Pool thÃ nh cÃ´ng!");
        } catch (Exception e) {
            System.err.println("[DBContext] Lá»—i khá»Ÿi táº¡o HikariDataSource: " + e.getMessage());
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBContext() {}

    /**
     * Cáº¥p phÃ¡t má»™t Connection tá»« HikariCP Pool.
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * ÄÃ³ng Connection Pool khi á»©ng dá»¥ng táº¯t.
     */
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("[DBContext] ÄÃ£ Ä‘Ã³ng HikariCP Connection Pool!");
        }
    }
}

