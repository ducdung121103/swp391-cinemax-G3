package com.cinema.modules.infrastructure.controller;

import com.cinema.common.dao.SystemSettingDAO;
import com.cinema.model.SystemSetting;
import com.cinema.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller Quản lý Tham số Quy tắc Vận hành Hệ thống (System Settings - M-11).
 * Phụ trách bởi Dũng (Leader - TV 1) - Tinh giản cho 3 tham số quy tắc vận hành rạp chiếu phim cốt lõi.
 */
@WebServlet(name = "SettingAdminServlet", urlPatterns = {"/admin/infrastructure/settings", "/admin/settings"})
public class SettingAdminServlet extends HttpServlet {
    private final SystemSettingDAO systemSettingDAO = new SystemSettingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<SystemSetting> settings = systemSettingDAO.findAllSettings();

        // Gom các giá trị vào Map để View settings.jsp dễ dàng bind dữ liệu
        Map<String, String> settingsMap = new HashMap<>();
        for (SystemSetting s : settings) {
            settingsMap.put(s.getSettingKey(), s.getSettingValue());
        }

        req.setAttribute("settings", settings);
        req.setAttribute("settingsMap", settingsMap);

        // Flash message
        String msg = req.getParameter("msg");
        String error = req.getParameter("error");
        if (msg != null) req.setAttribute("msg", msg);
        if (error != null) req.setAttribute("error", error);

        req.getRequestDispatcher("/WEB-INF/views/admin/infrastructure/settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String action = req.getParameter("action");

        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        Integer updatedBy = (currentUser != null && currentUser.getId() != null) ? currentUser.getId().intValue() : 1;

        try {
            if ("saveAll".equalsIgnoreCase(action) || action == null || action.isBlank()) {
                String holdDurationStr = req.getParameter("SEAT_HOLD_DURATION_SECONDS");
                String bufferMinutesStr = req.getParameter("CLEANING_BUFFER_MINUTES");
                String cancelDeadlineStr = req.getParameter("CANCELLATION_DEADLINE_HOURS");

                // Hỗ trợ cả tiền tố setting_ nếu form gửi lên
                if (holdDurationStr == null) holdDurationStr = req.getParameter("setting_SEAT_HOLD_DURATION_SECONDS");
                if (bufferMinutesStr == null) bufferMinutesStr = req.getParameter("setting_CLEANING_BUFFER_MINUTES");
                if (cancelDeadlineStr == null) cancelDeadlineStr = req.getParameter("setting_CANCELLATION_DEADLINE_HOURS");

                if (holdDurationStr == null || holdDurationStr.isBlank()
                        || bufferMinutesStr == null || bufferMinutesStr.isBlank()
                        || cancelDeadlineStr == null || cancelDeadlineStr.isBlank()) {
                    throw new IllegalArgumentException("Vui lòng nhập đầy đủ giá trị cho cả 3 tham số vận hành!");
                }

                int holdDuration = Integer.parseInt(holdDurationStr.trim());
                int bufferMinutes = Integer.parseInt(bufferMinutesStr.trim());
                int cancelDeadline = Integer.parseInt(cancelDeadlineStr.trim());

                if (holdDuration < 60 || holdDuration > 3600) {
                    throw new IllegalArgumentException("Thời gian giữ ghế tạm thời phải từ 60 đến 3600 giây (tối đa 60 phút)!");
                }
                if (bufferMinutes < 5 || bufferMinutes > 120) {
                    throw new IllegalArgumentException("Thời gian đệm vệ sinh phòng chiếu phải từ 5 đến 120 phút!");
                }
                if (cancelDeadline < 1 || cancelDeadline > 72) {
                    throw new IllegalArgumentException("Thời hạn hủy vé trực tuyến phải từ 1 đến 72 giờ trước giờ chiếu!");
                }

                Map<String, String> updateBatch = new HashMap<>();
                updateBatch.put("SEAT_HOLD_DURATION_SECONDS", String.valueOf(holdDuration));
                updateBatch.put("CLEANING_BUFFER_MINUTES", String.valueOf(bufferMinutes));
                updateBatch.put("CANCELLATION_DEADLINE_HOURS", String.valueOf(cancelDeadline));

                systemSettingDAO.batchUpdateSettings(updateBatch, updatedBy);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/settings?msg=success");
                return;
            }
        } catch (NumberFormatException e) {
            String errorMsg = URLEncoder.encode("Giá trị tham số phải là số nguyên dương hợp lệ!", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/settings?error=" + errorMsg);
            return;
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Đã có lỗi xảy ra", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/settings?error=" + errorMsg);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/settings");
    }
}
