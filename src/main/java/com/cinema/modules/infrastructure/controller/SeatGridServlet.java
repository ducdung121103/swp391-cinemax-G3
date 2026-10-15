package com.cinema.modules.infrastructure.controller;

import com.cinema.model.Cinema;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Seat;
import com.cinema.model.SeatType;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller Thiết kế Sơ đồ Ghế tương tác trực quan (Seat Layout Designer - M-04.3).
 * Do Dũng (Leader - TV 1) phụ trách.
 */
@WebServlet(name = "SeatGridServlet", urlPatterns = {"/admin/infrastructure/seats", "/admin/seats"})
public class SeatGridServlet extends HttpServlet {
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String roomIdParam = req.getParameter("roomId");
        Long roomId = null;

        List<Cinema> cinemas = screeningRoomService.getAllCinemas();
        List<ScreeningRoom> allRooms = screeningRoomService.getAllRoomsWithCinema(null, null, null);

        if (roomIdParam != null && !roomIdParam.trim().isEmpty()) {
            try { roomId = Long.parseLong(roomIdParam.trim()); } catch (NumberFormatException ignored) {}
        }

        // Nếu chưa chọn phòng, tự động lấy phòng đầu tiên trong danh sách
        if (roomId == null && allRooms != null && !allRooms.isEmpty()) {
            roomId = allRooms.get(0).getId();
        }

        ScreeningRoom currentRoom = null;
        List<Seat> seats = new ArrayList<>();
        List<SeatType> seatTypes = screeningRoomService.getAllSeatTypes();

        int totalSeats = 0;
        int standardCount = 0;
        int vipCount = 0;
        int coupleCount = 0;
        int maintenanceCount = 0;

        if (roomId != null) {
            currentRoom = screeningRoomService.getRoomById(roomId);
            seats = screeningRoomService.getRoomSeatMatrix(roomId);

            if (seats != null) {
                totalSeats = seats.size();
                for (Seat s : seats) {
                    if ("MAINTENANCE".equalsIgnoreCase(s.getStatus()) || Boolean.FALSE.equals(s.getIsActive())) {
                        maintenanceCount++;
                    } else if (s.getSeatType() != null) {
                        String code = s.getSeatType().getTypeCode();
                        if ("VIP".equalsIgnoreCase(code)) vipCount++;
                        else if ("COUPLE".equalsIgnoreCase(code)) coupleCount++;
                        else standardCount++;
                    }
                }
            }
        }

        req.setAttribute("cinemas", cinemas);
        req.setAttribute("allRooms", allRooms);
        req.setAttribute("currentRoom", currentRoom);
        req.setAttribute("selectedRoomId", roomId);
        req.setAttribute("seats", seats);
        req.setAttribute("seatTypes", seatTypes);

        // Thống kê nhanh
        req.setAttribute("totalSeats", totalSeats);
        req.setAttribute("standardCount", standardCount);
        req.setAttribute("vipCount", vipCount);
        req.setAttribute("coupleCount", coupleCount);
        req.setAttribute("maintenanceCount", maintenanceCount);

        // Flash message
        String msg = req.getParameter("msg");
        String error = req.getParameter("error");
        if (msg != null) req.setAttribute("msg", msg);
        if (error != null) req.setAttribute("error", error);

        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/seat-designer.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");
        String roomIdParam = req.getParameter("roomId");
        Long roomId = (roomIdParam != null && !roomIdParam.trim().isEmpty()) ? Long.parseLong(roomIdParam.trim()) : null;

        try {
            if ("updateSelectedSeats".equalsIgnoreCase(action)) {
                String seatIdsRaw = req.getParameter("seatIds");
                String seatTypeIdStr = req.getParameter("seatTypeId");
                String status = req.getParameter("status");
                String isActiveStr = req.getParameter("isActive");

                Long seatTypeId = (seatTypeIdStr != null && !seatTypeIdStr.trim().isEmpty()) ? Long.parseLong(seatTypeIdStr.trim()) : null;
                Boolean isActive = (isActiveStr != null && !isActiveStr.trim().isEmpty()) ? Boolean.parseBoolean(isActiveStr.trim()) : null;

                List<Long> seatIds = new ArrayList<>();
                if (seatIdsRaw != null && !seatIdsRaw.trim().isEmpty()) {
                    String[] parts = seatIdsRaw.split(",");
                    for (String p : parts) {
                        if (!p.trim().isEmpty()) {
                            seatIds.add(Long.parseLong(p.trim()));
                        }
                    }
                }

                if (!seatIds.isEmpty()) {
                    screeningRoomService.updateBatchSeats(roomId, seatIds, seatTypeId, status, isActive);
                }
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seats?roomId=" + roomId + "&msg=seats_updated");
                return;

            } else if ("applyRowType".equalsIgnoreCase(action)) {
                String seatRow = req.getParameter("seatRow");
                Long seatTypeId = Long.parseLong(req.getParameter("seatTypeId"));

                screeningRoomService.updateRowSeatType(roomId, seatRow, seatTypeId);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seats?roomId=" + roomId + "&msg=row_updated");
                return;

            } else if ("regenerateMatrix".equalsIgnoreCase(action)) {
                int totalRows = Integer.parseInt(req.getParameter("totalRows"));
                int totalColumns = Integer.parseInt(req.getParameter("totalColumns"));
                int stdRows = Integer.parseInt(req.getParameter("stdRows"));
                int vipRows = Integer.parseInt(req.getParameter("vipRows"));

                screeningRoomService.regenerateRoomSeatMatrix(roomId, totalRows, totalColumns, stdRows, vipRows);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seats?roomId=" + roomId + "&msg=matrix_regenerated");
                return;

            } else if ("updateSeatType".equalsIgnoreCase(action)) {
                Long seatTypeId = Long.parseLong(req.getParameter("seatTypeId"));
                String surchargeStr = req.getParameter("surcharge");
                String colorHex = req.getParameter("colorHex");

                BigDecimal surcharge = BigDecimal.ZERO;
                if (surchargeStr != null && !surchargeStr.trim().isEmpty()) {
                    surcharge = new BigDecimal(surchargeStr.trim());
                }

                screeningRoomService.updateSeatTypeSurcharge(seatTypeId, surcharge, colorHex);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seats?roomId=" + roomId + "&msg=type_updated");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Đã có lỗi xảy ra", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seats?roomId=" + roomId + "&error=" + errorMsg);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/seats?roomId=" + roomId);
    }
}
