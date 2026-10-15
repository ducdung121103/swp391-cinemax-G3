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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller Quản lý Chi nhánh Cụm Rạp (Branch Management - M-04.1).
 * Do Dũng (Leader - TV 1) phụ trách.
 */
@WebServlet(name = "CinemaAdminServlet", urlPatterns = {"/admin/infrastructure/cinemas", "/admin/cinemas", "/admin/branches"})
public class CinemaAdminServlet extends HttpServlet {
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String keyword = req.getParameter("keyword");
        String city = req.getParameter("city");
        String statusParam = req.getParameter("status");

        Boolean isActive = null;
        if ("ACTIVE".equalsIgnoreCase(statusParam)) {
            isActive = true;
        } else if ("INACTIVE".equalsIgnoreCase(statusParam)) {
            isActive = false;
        }

        List<Cinema> cinemas = screeningRoomService.getAllCinemasForAdmin(keyword, city, isActive);
        List<String> cities = screeningRoomService.getAllCities();

        req.setAttribute("cinemas", cinemas);
        req.setAttribute("cities", cities);
        req.setAttribute("keyword", keyword);
        req.setAttribute("selectedCity", city);
        req.setAttribute("selectedStatus", statusParam);

        // Flash message handling
        String msg = req.getParameter("msg");
        String error = req.getParameter("error");
        if (msg != null) req.setAttribute("msg", msg);
        if (error != null) req.setAttribute("error", error);

        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/cinemas.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        try {
            if ("create".equalsIgnoreCase(action)) {
                String cinemaCode = req.getParameter("cinemaCode");
                String name = req.getParameter("name");
                String city = req.getParameter("city");
                String address = req.getParameter("address");
                String phone = req.getParameter("phone");
                String email = req.getParameter("email");
                String totalRoomsStr = req.getParameter("totalRooms");
                boolean isActive = "true".equalsIgnoreCase(req.getParameter("isActive")) || "1".equals(req.getParameter("isActive")) || "on".equalsIgnoreCase(req.getParameter("isActive"));

                int totalRooms = 0;
                if (totalRoomsStr != null && !totalRoomsStr.trim().isEmpty()) {
                    try { totalRooms = Integer.parseInt(totalRoomsStr.trim()); } catch (NumberFormatException ignored) {}
                }

                Cinema c = Cinema.builder()
                        .cinemaCode(cinemaCode)
                        .name(name)
                        .city(city)
                        .address(address)
                        .phone(phone)
                        .email(email)
                        .totalRooms(totalRooms)
                        .build();
                c.setIsActive(isActive);

                screeningRoomService.createCinema(c);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/cinemas?msg=created");
                return;

            } else if ("update".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                String cinemaCode = req.getParameter("cinemaCode");
                String name = req.getParameter("name");
                String city = req.getParameter("city");
                String address = req.getParameter("address");
                String phone = req.getParameter("phone");
                String email = req.getParameter("email");
                boolean isActive = "true".equalsIgnoreCase(req.getParameter("isActive")) || "1".equals(req.getParameter("isActive")) || "on".equalsIgnoreCase(req.getParameter("isActive"));

                Cinema c = Cinema.builder()
                        .cinemaCode(cinemaCode)
                        .name(name)
                        .city(city)
                        .address(address)
                        .phone(phone)
                        .email(email)
                        .build();
                c.setId(id);
                c.setIsActive(isActive);

                screeningRoomService.updateCinema(c);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/cinemas?msg=updated");
                return;

            } else if ("toggleStatus".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                boolean currentActive = Boolean.parseBoolean(req.getParameter("currentActive"));
                screeningRoomService.toggleCinemaStatus(id, !currentActive);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/cinemas?msg=status_updated");
                return;

            } else if ("delete".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                screeningRoomService.deleteCinema(id);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/cinemas?msg=deleted");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Đã có lỗi xảy ra", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/cinemas?error=" + errorMsg);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/cinemas");
    }
}
