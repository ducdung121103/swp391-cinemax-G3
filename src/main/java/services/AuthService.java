package services;

import repositories.Roles;
import repositories.Users;
import models.Role;
import models.User;
import utils.EmailSender;
import utils.Password;
import java.time.LocalDate;
import java.util.Random;

public class AuthService {

    private final Users usersDao = new Users();
    private final Roles rolesDao = new Roles();

    public void registerUser(String username, String fullName, String email, String phone,
                             String rawPassword, String birthdayStr) throws Exception {

        // Check duplicate
        if (usersDao.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (usersDao.findByEmail(email) != null) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (usersDao.findByPhone(phone) != null) {
            throw new IllegalArgumentException("Phone number is already used.");
        }

        // Get role default
        Integer roleId = rolesDao.getRoleIdByName("CUSTOMER");
        if (roleId == null) {
            throw new RuntimeException("System Error: Role 'CUSTOMER' not found.");
        }

        // Create object
        User u = new User();
        u.setRoleId(roleId);
        u.setUsername(username);
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPassword(Password.hashPassword(rawPassword));
        u.setStatus("ACTIVE");
        u.setPoints(0);

        // Xử lí ngày sinh
        if (birthdayStr != null && !birthdayStr.isEmpty()) {
            try {
                u.setBirthday(LocalDate.parse(birthdayStr).atStartOfDay());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }

        // Insert to DB
        boolean isInserted = usersDao.insert(u);
        if (!isInserted) {
            throw new RuntimeException("Registration failed due to database error.");
        }
    }

    public User loginUser(String username, String password) throws Exception {
        // Find user
        User user = usersDao.findByUsername(username);

        // Validate Password
        if (user == null || !Password.verifyPassword(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        // Check Status
        if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Your account is " + user.getStatus() + ". Please contact admin.");
        }

        // Update Last Login
        usersDao.updateLastLogin(user.getUserId());

        return user;
    }

    // Helper function: get role's name
    public String getRoleName(int roleId) {
        Role role = rolesDao.getRoleById(roleId);
        return (role != null) ? role.getRoleName().toUpperCase() : "GUEST";
    }

    // Kiểm tra email và gửi OTP
    public String initiateForgotPassword(String email) {
        User user = usersDao.getByEmail(email);
        if (user == null) {
            return null;
        }

        String otp = generateOTP();
        boolean isSent = sendOtpEmail(email, otp);

        return isSent ? otp : null; // Trả về OTP để Controller lưu vào Session
    }

    // Sinh mã OTP
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    // Tạo nội dung HTML và gọi Utils để gửi
    private boolean sendOtpEmail(String email, String otp) {
        String subject = "Mã xác thực đặt lại mật khẩu - MyCinema";
        String content = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd;'>"
                + "<h2 style='color: #006A71;'>Yêu cầu đặt lại mật khẩu</h2>"
                + "<p>Xin chào,</p>"
                + "<p>Bạn vừa yêu cầu đặt lại mật khẩu. Mã OTP của bạn là:</p>"
                + "<h1 style='color: #FFACAC; letter-spacing: 5px;'>" + otp + "</h1>"
                + "<p>Mã này sẽ hết hạn sau 5 phút.</p>"
                + "<p>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email này.</p>"
                + "</div>";

        return EmailSender.send(email, subject, content);
    }

    // Đổi mật khẩu mới
    public boolean resetPassword(String email, String newPassword) {
        String hashedPassword = Password.hashPassword(newPassword);

        return usersDao.updatePassword(email, hashedPassword);
    }
}