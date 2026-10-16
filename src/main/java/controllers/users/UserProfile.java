package controllers.users;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import models.User;
import utils.Password;

import java.io.IOException;
import services.ProfileService;

@WebServlet(name = "UserProfile", urlPatterns = {
        "/profile",
        "/profile/update-info",
        "/profile/update-password",
        "/customer/profile"
})
public class UserProfile extends HttpServlet {
    private final ProfileService userService = new ProfileService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        request.setAttribute("u", user);

        // Xử lý thông báo
        String success = request.getParameter("success");
        String error = request.getParameter("error");

        if ("info_updated".equals(success)) {
            request.setAttribute("message", "Cập nhật thông tin thành công!");
        } else if ("password_updated".equals(success)) {
            request.setAttribute("message", "Cập nhật mật khẩu thành công!");
        } else if ("avatar_updated".equals(success)) {
            request.setAttribute("message", "Cập nhật ảnh đại diện thành công!");
        }

        if (error != null) {
            request.setAttribute("error", error);
        }
        
        request.getRequestDispatcher("/pages/user/profile.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("/login");
            return;
        }

        User user = (User) session.getAttribute("user");
        String path = request.getServletPath();

        if ("/profile/update-info".equals(path)) {
            // Cập nhật thông tin cá nhân
            user.setFullName(request.getParameter("fullName"));
            user.setEmail(request.getParameter("email"));
            user.setPhone(request.getParameter("phone"));

            String birthdayStr = request.getParameter("birthday");
            if (birthdayStr != null && !birthdayStr.isEmpty()) {
                user.setBirthday(java.time.LocalDate.parse(birthdayStr).atStartOfDay());
            } else {
                user.setBirthday(null);
            }

            boolean updated = userService.updateProfileInfo(user);
            if (updated) {
                session.setAttribute("user", user);
                response.sendRedirect(request.getContextPath() + "/profile?success=info_updated");
            } else {
                response.sendRedirect(request.getContextPath() + "/profile?error=Error!");
            }
        } 
        else if ("/profile/update-password".equals(path)) {
            // Cập nhật mật khẩu
            String currentPassword = request.getParameter("currentPassword");
            String newPassword = request.getParameter("newPassword");
            String confirmPassword = request.getParameter("confirmPassword");

            // Kiểm tra confirm mật khẩu
            if (!newPassword.equals(confirmPassword)) {
                response.sendRedirect(request.getContextPath() + "/profile?error=Wrong confirm password");
                return;
            }

            // Kiểm tra độ mạnh mật khẩu (tùy chọn)
            if (!Password.isValidPassword(newPassword)) {
                response.sendRedirect(request.getContextPath() + "/profile?error=Not enough conditions");
                return;
            }

            // Hash và update
            String hashedPassword = Password.hashPassword(newPassword);
            user.setPassword(hashedPassword);

            boolean updated = userService.updatePassword(user);
            if (updated) {
                session.setAttribute("user", user);
                response.sendRedirect(request.getContextPath() + "/profile?success=password_updated");
            } else {
                response.sendRedirect(request.getContextPath() + "/profile?error=Error!");
            }
        } 
    }
}