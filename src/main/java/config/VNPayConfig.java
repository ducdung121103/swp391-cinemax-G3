package config;

import java.io.InputStream;
import java.util.Properties;

public class VNPayConfig {

    public static String vnp_PayUrl;
    public static String vnp_ReturnUrl;
    public static String vnp_TmnCode;
    public static String secretKey;
    public static String vnp_ApiUrl;

    // Khối static này sẽ chạy duy nhất 1 lần khi class được gọi lần đầu tiên
    static {
        try (InputStream input = VNPayConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties prop = new Properties();
            if (input == null) {
                System.err.println("CẢNH BÁO: Không tìm thấy file config.properties trong thư mục resources!");
            } else {
                // Nạp file properties
                prop.load(input);

                // Gán giá trị vào biến
                vnp_PayUrl = prop.getProperty("vnp_PayUrl");
                vnp_ReturnUrl = prop.getProperty("vnp_ReturnUrl");
                vnp_TmnCode = prop.getProperty("vnp_TmnCode");
                secretKey = prop.getProperty("vnp_HashSecret");
                vnp_ApiUrl = prop.getProperty("vnp_ApiUrl");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}