package com.cinema.modules.identity.controller;

import com.cinema.common.util.ValidationUtil;
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
 * Hồ sơ khách và đổi mật khẩu (M-01.3).
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/customer/account/profile"})
public class ProfileServlet extends HttpServlet {
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = currentUser(req);
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        User fresh = userService.getUserById(current.getId());
        if (fresh != null) {
            req.getSession().setAttribute("currentUser", fresh);
            req.setAttribute("profileUser", fresh);
        } else {
            req.setAttribute("profileUser", current);
        }
        req.getRequestDispatcher("/WEB-INF/views/customer/account/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = currentUser(req);
        if (current == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        String action = req.getParameter("action");
        if ("password".equals(action)) {
            changePassword(req, current);
        } else {
            updateProfile(req, current);
        }
        resp.sendRedirect(req.getContextPath() + "/customer/account/profile");
    }

    private void updateProfile(HttpServletRequest req, User current) {
        String code = userService.updateProfile(
                current.getId(),
                req.getParameter("fullName"),
                req.getParameter("phone"),
                req.getParameter("avatarUrl"));
        if ("OK".equals(code)) {
            User fresh = userService.getUserById(current.getId());
            if (fresh != null) {
                req.getSession().setAttribute("currentUser", fresh);
            }
            flash(req, "msg", "profile_saved");
        } else {
            flash(req, "error", code);
        }
    }

    private void changePassword(HttpServletRequest req, User current) {
        String newPassword = req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword");
        if (!ValidationUtil.isValidPassword(newPassword)) {
            flash(req, "error", "WEAK_PASSWORD");
            return;
        }
        if (confirm == null || !confirm.equals(newPassword)) {
            flash(req, "error", "PASSWORD_MISMATCH");
            return;
        }
        boolean changed = userService.changePassword(current.getId(), req.getParameter("oldPassword"), newPassword);
        flash(req, changed ? "msg" : "error", changed ? "password_changed" : "BAD_PASSWORD");
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("currentUser");
        return value instanceof User ? (User) value : null;
    }

    private void flash(HttpServletRequest req, String key, String value) {
        req.getSession().setAttribute("profileFlashKey", key);
        req.getSession().setAttribute("profileFlashValue", value);
    }
}
