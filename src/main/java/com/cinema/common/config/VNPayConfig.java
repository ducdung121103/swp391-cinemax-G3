package com.cinema.common.config;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Cấu hình và tiện ích băm bảo mật SHA-512 cho Cổng thanh toán VNPay (M-06.1, M-06.2).
 */
public class VNPayConfig {
    private static final Properties properties = new Properties();

    static {
        loadProperties();
    }

    private static void loadProperties() {
        InputStream is = VNPayConfig.class.getClassLoader().getResourceAsStream("vnpay.properties");
        if (is != null) {
            try {
                properties.load(is);
            } catch (IOException e) {
                System.err.println("[VNPayConfig] Lỗi nạp vnpay.properties: " + e.getMessage());
            } finally {
                try {
                    is.close();
                } catch (IOException ignored) {}
            }
        }
    }

    public static String getTmnCode() {
        return properties.getProperty("vnp.tmn.code", "ZXDFWHE3");
    }

    public static String getHashSecret() {
        return properties.getProperty("vnp.hash.secret", "2QXUI4J4840R47M811F74I8445S7N34R");
    }

    public static String getPayUrl() {
        return properties.getProperty("vnp.pay.url", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
    }

    public static String getReturnUrl() {
        return properties.getProperty("vnp.return.url", "http://localhost:8080/SWP391/vnpay-return");
    }

    public static String getVersion() {
        return properties.getProperty("vnp.version", "2.1.0");
    }

    public static String getCommand() {
        return properties.getProperty("vnp.command", "pay");
    }

    /**
     * Băm chuỗi bằng thuật toán HmacSHA512 theo chuẩn tài liệu VNPay 2.1.0.
     */
    public static String hmacSHA512(final String key, final String data) {
        try {
            if (key == null || data == null) {
                throw new NullPointerException("Key hoặc data không được null khi băm HMAC-SHA512");
            }
            final Mac hmac512 = Mac.getInstance("HmacSHA512");
            byte[] hmacKeyBytes = key.getBytes(StandardCharsets.UTF_8);
            final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
            hmac512.init(secretKey);
            byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
            byte[] result = hmac512.doFinal(dataBytes);
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception ex) {
            System.err.println("[VNPayConfig] Lỗi băm SHA-512: " + ex.getMessage());
            return "";
        }
    }

    /**
     * Sắp xếp các tham số theo bảng chữ cái A-Z và tạo chuỗi hash data + query string.
     */
    public static String hashAllFields(Map<String, String> fields) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }
        return hmacSHA512(getHashSecret(), hashData.toString());
    }

    /**
     * Lấy địa chỉ IP của Client thực hiện giao dịch.
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
                ipAddress = "127.0.0.1";
            }
        }
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress != null ? ipAddress : "127.0.0.1";
    }
}

