package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Branch;
import com.cinema.model.User;
import com.cinema.modules.infrastructure.service.ScreeningHallService;
import com.cinema.modules.infrastructure.service.impl.ScreeningHallServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

/**
 * Controller hiển thị Bảng điều khiển Quản trị Trung tâm (Admin Dashboard).
 * Do Leader (TV 1) phụ trách quản trị tổng quan hệ thống.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {
    private final ScreeningHallService screeningHallService = new ScreeningHallServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        List<Branch> branches = screeningHallService.getAllBranches();
        req.setAttribute("branches", branches);
        req.setAttribute("totalBranches", branches != null ? branches.size() : 0);

        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
