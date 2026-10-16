package controllers.auth;

import services.AuthService;
import utils.Password;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "ResetPasswordController", urlPatterns = {"/reset-password"})
public class ResetPassword extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();

        Boolean canReset = (Boolean) session.getAttribute("canResetPassword");
        if (canReset == null || !canReset) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        request.getRequestDispatcher("/pages/auth/reset-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("resetEmail");

        Boolean canReset = (Boolean) session.getAttribute("canResetPassword");
        if (canReset == null || !canReset || email == null) {
            response.sendRedirect(request.getContextPath() + "/forgot-password");
            return;
        }

        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        // Kiểm tra mật khẩu nhập vào có khớp nhau không
        if (newPassword == null || !newPassword.equals(confirmPassword)) {
            request.setAttribute("error", "Mật khẩu xác nhận không khớp!");
            request.getRequestDispatcher("/pages/auth/reset-password.jsp").forward(request, response);
            return;
        }

        // Kiểm tra độ mạnh mật khẩu
        if (!Password.isValidPassword(newPassword)) {
            request.setAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ cái, số và ký tự đặc biệt (@$!%*#?&)!");
            request.getRequestDispatcher("/pages/auth/reset-password.jsp").forward(request, response);
            return;
        }

        // Gọi Service
        if (authService.resetPassword(email, newPassword)) {
            // Xóa session rác
            session.removeAttribute("resetEmail");
            session.removeAttribute("canResetPassword");

            // Gửi thông báo thành công sang trang login
            request.setAttribute("message", "Đổi mật khẩu thành công. Vui lòng đăng nhập lại!");
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
        } else {
            request.setAttribute("error", "Lỗi hệ thống, vui lòng thử lại sau.");
            request.getRequestDispatcher("/pages/auth/reset-password.jsp").forward(request, response);
        }
    }
}