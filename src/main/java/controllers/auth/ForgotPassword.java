package controllers.auth;

import services.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "ForgotPasswordController", urlPatterns = {"/forgot-password"})
public class ForgotPassword extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String email = request.getParameter("email");

        // Gọi Service
        String otp = authService.initiateForgotPassword(email);

        if (otp != null) {
            // Lưu vào Session để dùng ở bước sau
            HttpSession session = request.getSession();
            session.setAttribute("resetEmail", email);
            session.setAttribute("resetOTP", otp);
            session.setAttribute("otpCreationTime", System.currentTimeMillis());

            // Chuyển hướng sang trang nhập OTP
            response.sendRedirect(request.getContextPath() + "/otp-confirm");
        } else {
            // Lỗi: Email không tồn tại hoặc không gửi được mail
            request.setAttribute("error", "Email không tồn tại hoặc lỗi hệ thống.");
            request.getRequestDispatcher("/pages/auth/forgot-password.jsp").forward(request, response);
        }
    }
}