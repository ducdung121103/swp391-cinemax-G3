package controllers.auth;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import models.User;
import services.AuthService;

@WebServlet(name = "login", urlPatterns = { "/login" })
public class Login extends HttpServlet {

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Kiểm tra nếu user đã login rồi
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            // Đã login, redirect về trang tương ứng với role
            String roleName = (String) session.getAttribute("role");
            redirectByRole(request, response, roleName);
            return;
        }

        // Chưa login, hiển thị trang login
        if ("true".equals(request.getParameter("success"))) {
            request.setAttribute("message", "Registration successful! Please login.");
        }
        request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
    }

    private void redirectByRole(HttpServletRequest request, HttpServletResponse response, String roleName)
            throws IOException {
        String contextPath = request.getContextPath();

        switch (roleName) {
            case "ADMIN":
                response.sendRedirect(contextPath + "/admin/dashboard");
                break;
            case "BRANCH_MANAGER":
                response.sendRedirect(contextPath + "/branch-manager/dashboard");
                break;
            case "CINEMA_STAFF":
                response.sendRedirect(contextPath + "/staff/dashboard");
                break;
            case "CUSTOMER":
            default:
                response.sendRedirect(contextPath + "/home");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("error", "Username and Password are required.");
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
            return;
        }

        try {
            User user = authService.loginUser(username, password);
            HttpSession session = request.getSession();
            String roleName = authService.getRoleName(user.getRoleId());

            session.setAttribute("user", user);
            session.setAttribute("role", roleName);

            // Check redirectUrl
            String redirectUrl = (String) session.getAttribute("redirectUrl");
            if (redirectUrl != null) {
                session.removeAttribute("redirectUrl");
                response.sendRedirect(redirectUrl);
                return;
            }

            // Redirect by role
            redirectByRole(request, response, roleName);

        } catch (IllegalArgumentException e) {
            request.setAttribute("error", e.getMessage());
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "System error occurred.");
            request.getRequestDispatcher("/pages/auth/login.jsp").forward(request, response);
        }
    }
}