package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Cinema;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * Controller quản trị cụm rạp (TV 1).
 * Tuân thủ quy tắc: 100% dùng @WebServlet, KHÔNG khai báo trong web.xml!
 */
@WebServlet(name = "CinemaAdminServlet", urlPatterns = {"/admin/infrastructure/cinemas", "/admin/cinemas"})
public class CinemaAdminServlet extends HttpServlet {
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        req.setAttribute("cinemas", cinemas);
        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/cinemas.jsp").forward(req, resp);
    }
}
