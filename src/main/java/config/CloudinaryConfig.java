package config;

import com.cloudinary.Cloudinary;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CloudinaryConfig {
    private static Cloudinary cloudinary;
    private static Properties props;

    static {
        props = new Properties();
        try (InputStream input = CloudinaryConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("Không tìm thấy config.properties");
            }
            props.load(input);

            Map<String, Object> config = new HashMap<>();
            config.put("cloud_name", getProperty("cloudinary.cloud_name"));
            config.put("api_key", getProperty("cloudinary.api_key"));
            config.put("api_secret", getProperty("cloudinary.api_secret"));
            config.put("secure", true);

            cloudinary = new Cloudinary(config);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi load config Cloudinary", e);
        }
    }

    private static String getProperty(String key) {
        String value = props.getProperty(key);
        if (value == null || value.trim().isEmpty()) {
            throw new RuntimeException("Thiếu property: " + key);
        }
        return value.trim();
    }

    public static Cloudinary getCloudinary() {
        return cloudinary;
    }

    public static String getUploadFolder() {
        return props.getProperty("cloudinary.upload_folder", "avatars");
    }
}
