package com.cinema.modules.identity.controller;

import com.cinema.common.constant.RoleConstant;
import com.cinema.model.Cinema;
import com.cinema.model.User;
import com.cinema.modules.identity.service.UserService;
import com.cinema.modules.identity.service.impl.UserServiceImpl;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Quản lý tài khoản và gán Staff/Manager về chi nhánh (M-01.4).
 */
@WebServlet(name = "UserAdminServlet", urlPatterns = {"/admin/identity/users"})
public class UserAdminServlet extends HttpServlet {
    private static final int PAGE_SIZE = 20;

    private final UserService userService = new UserServiceImpl();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String keyword = trimToNull(req.getParameter("keyword"));
        String role = trimToNull(req.getParameter("role"));
        String status = trimToNull(req.getParameter("status"));
        int page = parsePage(req.getParameter("page"));

        List<User> users = userService.searchUsers(keyword, role, status, page, PAGE_SIZE);
        int total = userService.countUsers(keyword, role, status);
        List<Cinema> cinemas = screeningRoomService.getAllCinemas();

        req.setAttribute("users", users);
        req.setAttribute("cinemas", cinemas);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedRole", role);
        req.setAttribute("selectedStatus", status);
        req.setAttribute("page", page);
        req.setAttribute("pageSize", PAGE_SIZE);
        req.setAttribute("totalPages", Math.max(1, (int) Math.ceil(total / (double) PAGE_SIZE)));
        req.setAttribute("msg", req.getParameter("msg"));
        req.setAttribute("error", req.getParameter("error"));
        req.getRequestDispatcher("/WEB-INF/views/admin/identity/users.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!isAdmin(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        User actor = currentUser(req);
        String action = req.getParameter("action");
        Long targetId = parseLong(req.getParameter("userId"));
        String code;
        if ("assign".equals(action)) {
            code = userService.assignRoleAndBranch(actor.getId(), targetId, req.getParameter("roleName"), parseLong(req.getParameter("cinemaId")));
        } else if ("toggleStatus".equals(action)) {
            code = userService.toggleAccountStatus(actor.getId(), targetId);
        } else {
            code = "FAILED";
        }
        String key = "OK".equals(code) ? "msg" : "error";
        String value = "OK".equals(code) ? ("assign".equals(action) ? "assigned" : "status_updated") : code;
        StringBuilder back = new StringBuilder(req.getContextPath())
                .append("/admin/identity/users?")
                .append(key).append("=").append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        appendFilter(back, "keyword", req.getParameter("keyword"));
        appendFilter(back, "role", req.getParameter("role"));
        appendFilter(back, "status", req.getParameter("status"));
        appendFilter(back, "page", req.getParameter("page"));
        resp.sendRedirect(back.toString());
    }

    private boolean isAdmin(HttpServletRequest req) {
        User user = currentUser(req);
        return user != null && user.getRole() != null && RoleConstant.ADMIN.equalsIgnoreCase(user.getRole().getRoleName());
    }

    private User currentUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object value = session.getAttribute("currentUser");
        return value instanceof User ? (User) value : null;
    }

    private int parsePage(String raw) {
        try {
            int page = Integer.parseInt(raw);
            return Math.max(page, 1);
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private Long parseLong(String raw) {
        try {
            return raw == null || raw.isBlank() ? null : Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void appendFilter(StringBuilder back, String name, String raw) {
        if (raw == null || raw.isBlank()) return;
        back.append("&").append(name).append("=").append(URLEncoder.encode(raw.trim(), StandardCharsets.UTF_8));
    }

    private String trimToNull(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return raw.trim();
    }
}
