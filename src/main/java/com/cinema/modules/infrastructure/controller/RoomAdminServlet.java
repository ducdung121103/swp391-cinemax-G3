package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
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
 * Controller Quản lý Phòng Chiếu Phim (Screening Room Management - M-04.2).
 * Do Dũng (Leader - TV 1) phụ trách.
 */
@WebServlet(name = "RoomAdminServlet", urlPatterns = {"/admin/infrastructure/rooms", "/admin/rooms"})
public class RoomAdminServlet extends HttpServlet {
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String cinemaIdParam = req.getParameter("cinemaId");
        String roomType = req.getParameter("roomType");
        String status = req.getParameter("status");

        Long cinemaId = null;
        if (cinemaIdParam != null && !cinemaIdParam.trim().isEmpty() && !"ALL".equalsIgnoreCase(cinemaIdParam.trim())) {
            try { cinemaId = Long.parseLong(cinemaIdParam.trim()); } catch (NumberFormatException ignored) {}
        }

        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        List<ScreeningRoom> rooms = screeningRoomService.getAllRoomsWithCinema(cinemaId, roomType, status);

        req.setAttribute("cinemas", cinemas);
        req.setAttribute("rooms", rooms);
        req.setAttribute("selectedCinemaId", cinemaId);
        req.setAttribute("selectedRoomType", roomType);
        req.setAttribute("selectedStatus", status);

        // Flash message
        String msg = req.getParameter("msg");
        String error = req.getParameter("error");
        if (msg != null) req.setAttribute("msg", msg);
        if (error != null) req.setAttribute("error", error);

        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/rooms.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        try {
            if ("create".equalsIgnoreCase(action)) {
                Long cinemaId = Long.parseLong(req.getParameter("cinemaId"));
                String name = req.getParameter("name");
                String roomType = req.getParameter("roomType");
                String rowsStr = req.getParameter("totalRows");
                String colsStr = req.getParameter("totalColumns");
                String status = req.getParameter("status");

                int totalRows = 10;
                int totalCols = 10;
                if (rowsStr != null && !rowsStr.trim().isEmpty()) {
                    try { totalRows = Integer.parseInt(rowsStr.trim()); } catch (NumberFormatException ignored) {}
                }
                if (colsStr != null && !colsStr.trim().isEmpty()) {
                    try { totalCols = Integer.parseInt(colsStr.trim()); } catch (NumberFormatException ignored) {}
                }

                ScreeningRoom room = ScreeningRoom.builder()
                        .cinemaId(cinemaId)
                        .name(name)
                        .roomType(roomType != null ? roomType : "STANDARD_2D")
                        .totalRows(totalRows)
                        .totalColumns(totalCols)
                        .status(status != null ? status : "ACTIVE")
                        .build();

                Long newRoomId = screeningRoomService.createRoom(room);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?cinemaId=" + cinemaId + "&msg=created");
                return;

            } else if ("update".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                String name = req.getParameter("name");
                String roomType = req.getParameter("roomType");
                String status = req.getParameter("status");

                ScreeningRoom room = ScreeningRoom.builder()
                        .name(name)
                        .roomType(roomType)
                        .status(status)
                        .build();
                room.setId(id);

                screeningRoomService.updateRoom(room);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?msg=updated");
                return;

            } else if ("changeStatus".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                String status = req.getParameter("status");
                screeningRoomService.updateRoomStatus(id, status);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?msg=status_updated");
                return;

            } else if ("delete".equalsIgnoreCase(action)) {
                Long id = Long.parseLong(req.getParameter("id"));
                screeningRoomService.deleteRoom(id);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?msg=deleted");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Đã có lỗi xảy ra", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?error=" + errorMsg);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms");
    }
}
