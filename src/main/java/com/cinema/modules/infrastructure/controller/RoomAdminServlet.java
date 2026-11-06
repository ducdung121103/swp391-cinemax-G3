package com.cinema.modules.infrastructure.controller;

import com.cinema.common.constant.RoleConstant;
import com.cinema.model.Cinema;
import com.cinema.model.User;
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

    // LUỒNG DỮ LIỆU (SWP391 Architecture Tracing):
    // 1. HTTP Request: GET /admin/infrastructure/rooms
    // 2. Controller: RoomAdminServlet.doGet -> lọc cinemaId, roomType, status
    // 3. Service: ScreeningRoomService.getAllRoomsWithCinema -> ScreeningRoomDAO.findAllRooms
    // 4. Database: Bảng screening_rooms, cinemas -> Map ResultSet sang List<ScreeningRoom>
    // 5. Response: req.setAttribute("rooms") -> Forward /WEB-INF/views/admin/infrastructure/rooms.jsp
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
        req.setAttribute("actor", currentUser(req));

        // Flash message
        String msg = req.getParameter("msg");
        String error = req.getParameter("error");
        if (msg != null) req.setAttribute("msg", msg);
        if (error != null) req.setAttribute("error", error);

        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/rooms.jsp").forward(req, resp);
    }

    // LUỒNG DỮ LIỆU (SWP391 Architecture Tracing):
    // 1. HTTP Request: POST /admin/infrastructure/rooms (action: create, update, toggleStatus, delete)
    // 2. Controller: RoomAdminServlet.doPost -> RBAC Branch Guard (Manager chỉ sửa phòng rạp mình)
    // 3. Service: ScreeningRoomService (createRoom + auto seats, updateRoom, deleteRoom)
    // 4. Database: Bảng screening_rooms, seats (is_deleted=1, kiểm tra future showtimes)
    // 5. Response: resp.sendRedirect -> /admin/infrastructure/rooms?msg={action_result}
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
                User actor = currentUser(req);
                Long id = Long.parseLong(req.getParameter("id"));
                String code = screeningRoomService.deleteRoom(actor, id);
                if ("OK".equals(code)) {
                    resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?msg=deleted");
                } else if ("ACTIVE_SHOWTIMES".equals(code)) {
                    resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?error=active_showtimes");
                } else if ("FORBIDDEN_BRANCH".equals(code)) {
                    resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?error=forbidden_branch");
                } else if ("UNAUTHORIZED".equals(code)) {
                    resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?error=forbidden");
                } else {
                    resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/rooms?error=failed");
                }
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
    private User currentUser(HttpServletRequest req) {
        jakarta.servlet.http.HttpSession session = req.getSession(false);
        if (session == null) return null;
        Object val = session.getAttribute("currentUser");
        return val instanceof User ? (User) val : null;
    }
}
