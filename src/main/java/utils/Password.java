package utils;

import org.mindrot.jbcrypt.BCrypt;
import java.util.regex.Pattern;

public class Password {

    // Regex giải thích:
    // ^                 : Bắt đầu chuỗi
    // (?=.*[A-Za-z])    : Phải chứa ít nhất 1 chữ cái (hoa hoặc thường)
    // (?=.*\d)          : Phải chứa ít nhất 1 số
    // (?=.*[@$!%*#?&])  : Phải chứa ít nhất 1 ký tự đặc biệt trong nhóm này
    // .{8,}             : Độ dài tối thiểu 8 ký tự (dấu . chấp nhận mọi ký tự)
    // $                 : Kết thúc chuỗi
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&]).{8,}$");

//      Hash password by using BCrypt
    public static String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt());
    }

//      Check password input and password in DB
    public static boolean verifyPassword(String plainTextPassword, String hashedPassword) {
        if (hashedPassword == null) {
            return false;
        }

        // Trường hợp cũ: mật khẩu đã được hash bằng BCrypt
        if (hashedPassword.startsWith("$2a$")) {
            return BCrypt.checkpw(plainTextPassword, hashedPassword);
        }

        // Fallback: hỗ trợ mật khẩu đang lưu ở dạng plain text (dùng cho dữ liệu mẫu/dev)
        return plainTextPassword.equals(hashedPassword);
    }

//      Check password valid
    public static boolean isValidPassword(String password) {
        if (password == null) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }
}