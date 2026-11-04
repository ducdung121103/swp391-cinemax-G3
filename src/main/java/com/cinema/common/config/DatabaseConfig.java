package com.cinema.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Äá»c cáº¥u hÃ¬nh káº¿t ná»‘i CSDL tá»« file db.properties (hoáº·c db.local.properties).
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
public class DatabaseConfig {
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // Æ¯u tiÃªn Ä‘á»c db.local.properties náº¿u cÃ³
        InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.local.properties");
        if (is == null) {
            is = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties");
        }

        if (is != null) {
            try {
                properties.load(is);
            } catch (IOException e) {
                System.err.println("[DatabaseConfig] Lá»—i náº¡p properties: " + e.getMessage());
            } finally {
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
        } else {
            System.err.println("[DatabaseConfig] KhÃ´ng tÃ¬m tháº¥y db.properties trong classpath!");
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String val = properties.getProperty(key);
        if (val == null || val.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}

