package controllers.auth;

import jakarta.servlet.http.HttpSession;
import services.AuthService;
import utils.Password;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "register", urlPatterns = {"/register"})
public class Register extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Kiểm tra nếu đã login
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Get Params
        String username = request.getParameter("username");
        String fullName = request.getParameter("fullName");
        String email = request.getParameter("email");
        String phone = request.getParameter("phone");
        String password = request.getParameter("password");
        String birthdayStr = request.getParameter("birthday");

        // Basic Validation
        if (isBlank(username) || isBlank(fullName) || isBlank(email) || isBlank(phone) || isBlank(password)) {
            returnError(request, response, "Please fill in all fields.");
            return;
        }

        if (!Password.isValidPassword(password)) {
            returnError(request, response, "Password must be 8+ chars, contain letter, number & special char.");
            return;
        }

        // Call Service
        try {
            authService.registerUser(username, fullName, email, phone, password, birthdayStr);
            response.sendRedirect(request.getContextPath() + "/login?success=true");

        } catch (IllegalArgumentException e) {
            returnError(request, response, e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            returnError(request, response, "System error. Please try again later.");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void returnError(HttpServletRequest request, HttpServletResponse response, String msg) throws ServletException, IOException {
        request.setAttribute("error", msg);
        request.setAttribute("username", request.getParameter("username"));
        request.setAttribute("fullName", request.getParameter("fullName"));
        request.setAttribute("email", request.getParameter("email"));
        request.setAttribute("phone", request.getParameter("phone"));
        request.setAttribute("birthday", request.getParameter("birthday"));
        request.getRequestDispatcher("/pages/auth/register.jsp").forward(request, response);
    }
}