package com.cinema.modules.catalog.controller;

import com.cinema.common.constant.RoleConstant;
import com.cinema.common.dto.ApiResponse;
import com.cinema.common.util.JsonUtil;
import com.cinema.model.ScreeningRoom;
import com.cinema.model.Showtime;
import com.cinema.model.User;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.ShowtimeServiceImpl;
import com.cinema.modules.infrastructure.service.ScreeningRoomService;
import com.cinema.modules.infrastructure.service.impl.ScreeningRoomServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller Quản lý Lịch chiếu phim dành cho Quản lý cụm rạp và Admin (TV 3).
 * urlPatterns: /manager/showtimes, /admin/catalog/showtimes
 */
@WebServlet(name = "ShowtimeManagementServlet", urlPatterns = {"/manager/showtimes", "/admin/catalog/showtimes"})
public class ShowtimeManagementServlet extends HttpServlet {
    private final ShowtimeService showtimeService = new ShowtimeServiceImpl();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("UNAUTHORIZED", "Vui lòng đăng nhập!")));
            return;
        }

        String role = (currentUser.getRole() != null) ? currentUser.getRole().getRoleName() : null;
        if (!RoleConstant.ADMIN.equalsIgnoreCase(role) && !RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("FORBIDDEN", "Không có quyền truy cập lịch chiếu!")));
            return;
        }

        String cinemaIdParam = req.getParameter("cinemaId");
        Long cinemaId = null;
        if (cinemaIdParam != null && !cinemaIdParam.trim().isEmpty()) {
            cinemaId = Long.parseLong(cinemaIdParam.trim());
        } else if (currentUser.getCinemaId() != null) {
            cinemaId = currentUser.getCinemaId();
        }

        if (cinemaId == null) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("INVALID_PARAM", "Vui lòng cung cấp mã cụm rạp (cinemaId)")));
            return;
        }

        String dateParam = req.getParameter("date");
        LocalDate date = (dateParam != null && !dateParam.trim().isEmpty()) ? LocalDate.parse(dateParam.trim()) : LocalDate.now();

        List<Showtime> showtimes = showtimeService.getShowtimesByCinemaAndDate(cinemaId, date);
        resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(showtimes, "Lấy danh sách suất chiếu thành công")));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (currentUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("UNAUTHORIZED", "Vui lòng đăng nhập!")));
            return;
        }

        String role = (currentUser.getRole() != null) ? currentUser.getRole().getRoleName() : null;
        if (!RoleConstant.ADMIN.equalsIgnoreCase(role) && !RoleConstant.MANAGER.equalsIgnoreCase(role)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("FORBIDDEN", "Chỉ Quản lý rạp hoặc Admin mới có quyền tạo suất chiếu!")));
            return;
        }

        try {
            Long movieId = Long.parseLong(req.getParameter("movieId"));
            Long screeningRoomId = Long.parseLong(req.getParameter("screeningRoomId"));
            String startTimeStr = req.getParameter("startTime");
            LocalDateTime startTime = LocalDateTime.parse(startTimeStr);
            String experienceFormat = req.getParameter("experienceFormat");
            if (experienceFormat == null || experienceFormat.trim().isEmpty()) {
                experienceFormat = "2D";
            }

            ScreeningRoom room = screeningRoomService.getRoomById(screeningRoomId);
            if (room == null) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("ROOM_NOT_FOUND", "Không tìm thấy phòng chiếu!")));
                return;
            }

            // Kiểm tra phân quyền: Quản lý rạp chỉ được tạo suất chiếu cho cụm rạp mình phụ trách
            if (RoleConstant.MANAGER.equalsIgnoreCase(role) && currentUser.getCinemaId() != null
                    && !currentUser.getCinemaId().equals(room.getCinemaId())) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("FORBIDDEN", "Quản lý chỉ được phép tạo suất chiếu cho cụm rạp của mình!")));
                return;
            }

            Showtime showtime = Showtime.builder()
                    .movieId(movieId)
                    .screeningRoomId(screeningRoomId)
                    .startTime(startTime)
                    .experienceFormat(experienceFormat)
                    .status("SCHEDULED")
                    .build();

            boolean created = showtimeService.createShowtime(showtime);
            if (created) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(showtime, "Tạo suất chiếu mới thành công!")));
            } else {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("CREATE_FAILED", "Không thể tạo suất chiếu!")));
            }
        } catch (Exception e) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("BAD_REQUEST", e.getMessage())));
        }
    }
}
