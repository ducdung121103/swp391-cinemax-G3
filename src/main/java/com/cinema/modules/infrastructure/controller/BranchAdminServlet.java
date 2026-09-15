package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Branch;
import com.cinema.modules.infrastructure.service.ScreeningHallService;
import com.cinema.modules.infrastructure.service.impl.ScreeningHallServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller quản trị chi nhánh cụm rạp (TV 1).
 * Tuân thủ quy tắc: 100% dùng @WebServlet, KHÔNG khai báo trong web.xml!
 */
@WebServlet(name = "BranchAdminServlet", urlPatterns = {"/admin/infrastructure/branches"})
public class BranchAdminServlet extends HttpServlet {
    private final ScreeningHallService screeningHallService = new ScreeningHallServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Branch> branches = screeningHallService.getAllBranches();
        req.setAttribute("branches", branches);
        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/branches.jsp").forward(req, resp);
    }
}
