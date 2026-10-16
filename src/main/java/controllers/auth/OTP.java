package controllers.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "OtpController", urlPatterns = {"/otp-confirm"})
public class OTP extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/pages/auth/otp-confirm.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        String sessionOtp = (String) session.getAttribute("resetOTP");
        String inputOtp = request.getParameter("otp");

        if (sessionOtp != null && sessionOtp.equals(inputOtp)) {
            session.removeAttribute("resetOTP");
            session.setAttribute("canResetPassword", true);

            response.sendRedirect(request.getContextPath() + "/reset-password");
        } else {
            request.setAttribute("error", "Mã OTP không chính xác!");
            request.getRequestDispatcher("/pages/auth/otp-confirm.jsp").forward(request, response);
        }
    }
}