package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.User;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
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
 * Do Leader (TV 1 - Dũng) phụ trách quản trị tổng quan hệ sinh thái rạp.
 */
@WebServlet(name = "AdminDashboardServlet", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardServlet extends HttpServlet {
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        int totalCinemas = screeningRoomService.getTotalCinemasCount();
        int totalRooms = screeningRoomService.getTotalRoomsCount();
        int totalSeats = screeningRoomService.getTotalSeatsCount();

        List<ScreeningRoom> recentRooms = screeningRoomService.getAllRoomsWithCinema(null, null, null);

        req.setAttribute("cinemas", cinemas);
        req.setAttribute("totalCinemas", totalCinemas > 0 ? totalCinemas : (cinemas != null ? cinemas.size() : 0));
        req.setAttribute("totalRooms", totalRooms > 0 ? totalRooms : (recentRooms != null ? recentRooms.size() : 0));
        req.setAttribute("totalSeats", totalSeats);
        req.setAttribute("recentRooms", recentRooms);

        req.getRequestDispatcher("/WEB-INF/views/admin/dashboard.jsp").forward(req, resp);
    }
}
