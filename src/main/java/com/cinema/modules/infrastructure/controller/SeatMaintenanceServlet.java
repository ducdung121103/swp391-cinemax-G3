package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Controller Quản Lý Bảo Trì Ghế Phòng Chiếu (M-04.4 - Seat Maintenance Management).
 * Tách biệt hoàn toàn khỏi màn hình Thiết Kế Sơ Đồ Ghế (M-04.3) nhằm phân định rõ:
 * - M-04.3: Thiết lập hạ tầng ban đầu (Setup Layout, phân loại Standard/VIP/Couple).
 * - M-04.4: Tác vụ vận hành bảo trì cơ sở vật chất thường nhật (Khóa ghế hỏng / Mở lại ghế).
 *
 * LUỒNG DỮ LIỆU:
 * 1. GET /admin/infrastructure/seat-maintenance -> doGet -> load room matrix -> forward seat-maintenance.jsp
 * 2. POST action=toggleMaintenance -> ScreeningRoomService.toggleSeatsMaintenance -> ScreeningRoomDAO -> DB seats
 * 3. POST action=restoreAll -> ScreeningRoomService.restoreAllMaintenanceSeatsInRoom -> ScreeningRoomDAO -> DB seats
 */
@WebServlet(name = "SeatMaintenanceServlet", urlPatterns = {
        "/admin/infrastructure/seat-maintenance",
        "/admin/seats/maintenance"
})
public class SeatMaintenanceServlet extends HttpServlet {

    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = req.getSession(false);
        com.cinema.model.User currentUser = (session != null) ? (com.cinema.model.User) session.getAttribute("currentUser") : null;
        if (currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error=" + URLEncoder.encode("Chỉ Quản trị viên (Admin) mới có quyền truy cập Quản lý bảo trì ghế!", StandardCharsets.UTF_8));
            return;
        }

        String roomIdParam = req.getParameter("roomId");
        Long roomId = null;

        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        List<ScreeningRoom> allRooms = screeningRoomService.getAllRoomsWithCinema(null, null, null);

        if (roomIdParam != null && !roomIdParam.trim().isEmpty()) {
            try {
                roomId = Long.parseLong(roomIdParam.trim());
            } catch (NumberFormatException ignored) {}
        }

        // Tự động chọn phòng chiếu đầu tiên nếu chưa chọn
        if (roomId == null && allRooms != null && !allRooms.isEmpty()) {
            roomId = allRooms.get(0).getId();
        }

        ScreeningRoom currentRoom = null;
        List<Seat> seats = new ArrayList<>();
        int totalSeats = 0;
        int activeCount = 0;
        int maintenanceCount = 0;

        if (roomId != null) {
            currentRoom = screeningRoomService.getRoomById(roomId);
            seats = screeningRoomService.getRoomSeatMatrix(roomId);

            if (seats != null) {
                totalSeats = seats.size();
                for (Seat s : seats) {
                    if ("MAINTENANCE".equalsIgnoreCase(s.getStatus()) || Boolean.FALSE.equals(s.getIsActive())) {
                        maintenanceCount++;
                    } else {
                        activeCount++;
                    }
                }
            }
        }

        req.setAttribute("cinemas", cinemas);
        req.setAttribute("allRooms", allRooms);
        req.setAttribute("currentRoom", currentRoom);
        req.setAttribute("selectedRoomId", roomId);
        req.setAttribute("seats", seats);

        // Thống kê 3 chỉ số phục vụ bảo trì
        req.setAttribute("totalSeats", totalSeats);
        req.setAttribute("activeCount", activeCount);
        req.setAttribute("maintenanceCount", maintenanceCount);

        // Flash message
        String msg = req.getParameter("msg");
        String error = req.getParameter("error");
        if (msg != null) req.setAttribute("msg", msg);
        if (error != null) req.setAttribute("error", error);

        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/seat-maintenance.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        jakarta.servlet.http.HttpSession session = req.getSession(false);
        com.cinema.model.User currentUser = (session != null) ? (com.cinema.model.User) session.getAttribute("currentUser") : null;
        if (currentUser != null && currentUser.getRole() != null && "MANAGER".equalsIgnoreCase(currentUser.getRole().getRoleName())) {
            resp.sendRedirect(req.getContextPath() + "/admin/dashboard?error=" + URLEncoder.encode("Chỉ Quản trị viên (Admin) mới có quyền chỉnh sửa trạng thái bảo trì ghế!", StandardCharsets.UTF_8));
            return;
        }

        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        String roomIdParam = req.getParameter("roomId");
        Long roomId = (roomIdParam != null && !roomIdParam.trim().isEmpty()) ? Long.parseLong(roomIdParam.trim()) : null;

        try {
            if ("toggleMaintenance".equalsIgnoreCase(action)) {
                // Thao tác 1 & 2: Chuyển ghế sang Bảo trì hoặc Mở lại về Hoạt động
                String seatIdsRaw = req.getParameter("seatIds");
                String underMaintStr = req.getParameter("underMaintenance");
                boolean underMaintenance = underMaintStr == null || Boolean.parseBoolean(underMaintStr);

                List<Long> seatIds = parseSeatIds(seatIdsRaw);
                if (!seatIds.isEmpty()) {
                    screeningRoomService.toggleSeatsMaintenance(roomId, seatIds, underMaintenance);
                }
                String msgKey = underMaintenance ? "maintenance_applied" : "maintenance_released";
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seat-maintenance?roomId=" + roomId + "&msg=" + msgKey);
                return;

            } else if ("restoreAll".equalsIgnoreCase(action)) {
                // Thao tác 3: Khôi phục toàn bộ ghế bảo trì trong phòng về Hoạt động
                if (roomId != null) {
                    screeningRoomService.restoreAllMaintenanceSeatsInRoom(roomId);
                }
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seat-maintenance?roomId=" + roomId + "&msg=all_restored");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Đã có lỗi xảy ra", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seat-maintenance?roomId=" + roomId + "&error=" + errorMsg);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seat-maintenance?roomId=" + roomId);
    }

    private List<Long> parseSeatIds(String seatIdsRaw) {
        List<Long> list = new ArrayList<>();
        if (seatIdsRaw != null && !seatIdsRaw.trim().isEmpty()) {
            for (String p : seatIdsRaw.split(",")) {
                if (!p.trim().isEmpty()) {
                    try {
                        list.add(Long.parseLong(p.trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return list;
    }
}
