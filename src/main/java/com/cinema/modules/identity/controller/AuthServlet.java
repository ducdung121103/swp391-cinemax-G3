package com.cinema.modules.identity.controller;

import com.cinema.common.constant.RoleConstant;
import com.cinema.model.User;
import com.cinema.modules.identity.service.UserService;
import com.cinema.modules.identity.service.impl.UserServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller xử lý Đăng nhập, Đăng xuất, Đăng ký thành viên (TV 2).
 */
@WebServlet(name = "AuthServlet", urlPatterns = {"/login", "/logout", "/register"})
public class AuthServlet extends HttpServlet {
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/logout".equals(path)) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            resp.sendRedirect(req.getContextPath() + "/login?msg=logged_out");
            return;
        }

        if ("/register".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            return;
        }

        // Mặc định là /login
        req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();

        if ("/login".equals(path)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            User user = userService.authenticate(email, password);

            if (user != null) {
                HttpSession session = req.getSession(true);
                session.setAttribute("currentUser", user);

                // Điều hướng theo Role
                String role = (user.getRole() != null) ? user.getRole().getRoleName() : RoleConstant.CUSTOMER;
                if (RoleConstant.ADMIN.equalsIgnoreCase(role) || RoleConstant.MANAGER.equalsIgnoreCase(role) || RoleConstant.STAFF.equalsIgnoreCase(role)) {
                    resp.sendRedirect(req.getContextPath() + "/admin/dashboard");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/");
                }
            } else {
                req.setAttribute("errorMessage", "Email hoặc mật khẩu không chính xác!");
                req.getRequestDispatcher("/WEB-INF/views/customer/account/login.jsp").forward(req, resp);
            }
            return;
        }

        if ("/register".equals(path)) {
            String email = req.getParameter("email");
            String password = req.getParameter("password");
            String fullName = req.getParameter("fullName");
            String phone = req.getParameter("phone");

            User created = userService.registerCustomer(email, password, fullName, phone);
            if (created != null) {
                resp.sendRedirect(req.getContextPath() + "/login?msg=register_success");
            } else {
                req.setAttribute("errorMessage", "Đăng ký thất bại. Email có thể đã tồn tại!");
                req.getRequestDispatcher("/WEB-INF/views/customer/account/register.jsp").forward(req, resp);
            }
        }
    }
}
