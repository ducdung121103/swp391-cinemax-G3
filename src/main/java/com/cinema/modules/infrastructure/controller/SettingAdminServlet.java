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
 * Controller Quản lý Tham số Cấu hình Hệ thống (System Settings - M-11).
 * Do Dũng (Leader - TV 1) phụ trách.
 */
@WebServlet(name = "SettingAdminServlet", urlPatterns = {"/admin/infrastructure/settings", "/admin/settings"})
public class SettingAdminServlet extends HttpServlet {
    private final SystemSettingDAO systemSettingDAO = new SystemSettingDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String group = req.getParameter("group");

        List<SystemSetting> settings = systemSettingDAO.findAllSettings(group);
        List<String> groups = systemSettingDAO.findAllGroups();

        // Gom nhóm theo Map để dễ dàng hiển thị theo từng Tab trên giao diện
        Map<String, String> settingsMap = new HashMap<>();
        for (SystemSetting s : settings) {
            settingsMap.put(s.getSettingKey(), s.getSettingValue());
        }

        req.setAttribute("settings", settings);
        req.setAttribute("settingsMap", settingsMap);
        req.setAttribute("groups", groups);
        req.setAttribute("selectedGroup", group);

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
            if ("saveAll".equalsIgnoreCase(action)) {
                Map<String, String[]> paramMap = req.getParameterMap();
                Map<String, String> updateBatch = new HashMap<>();

                for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                    String paramName = entry.getKey();
                    if (paramName.startsWith("setting_")) {
                        String key = paramName.substring("setting_".length()).toUpperCase();
                        String val = entry.getValue() != null && entry.getValue().length > 0 ? entry.getValue()[0].trim() : "";

                        // Validation số nguyên dương cho các tham số trọng yếu
                        if ("SEAT_HOLD_DURATION_SECONDS".equals(key)) {
                            int sec = Integer.parseInt(val);
                            if (sec < 60 || sec > 3600) {
                                throw new IllegalArgumentException("Thời gian giữ ghế tạm thời phải từ 60 giây đến 3600 giây (1h)!");
                            }
                        } else if ("CLEANING_BUFFER_MINUTES".equals(key)) {
                            int min = Integer.parseInt(val);
                            if (min < 0 || min > 120) {
                                throw new IllegalArgumentException("Thời gian đệm vệ sinh phòng phải từ 0 đến 120 phút!");
                            }
                        } else if ("OTP_EXPIRY_SECONDS".equals(key)) {
                            int otpSec = Integer.parseInt(val);
                            if (otpSec < 30 || otpSec > 1800) {
                                throw new IllegalArgumentException("Thời hạn mã OTP phải từ 30 giây đến 1800 giây!");
                            }
                        }

                        updateBatch.put(key, val);
                    }
                }

                systemSettingDAO.batchUpdateSettings(updateBatch, updatedBy);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/settings?msg=all_saved");
                return;

            } else if ("saveSingle".equalsIgnoreCase(action)) {
                String key = req.getParameter("settingKey");
                String value = req.getParameter("settingValue");
                String description = req.getParameter("description");
                String groupName = req.getParameter("groupName");

                if (key == null || key.trim().isEmpty()) {
                    throw new IllegalArgumentException("Khóa tham số (Setting Key) không được để trống!");
                }

                systemSettingDAO.upsertSetting(key, value, description, groupName, updatedBy);
                resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/settings?msg=single_saved");
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = URLEncoder.encode(e.getMessage() != null ? e.getMessage() : "Đã có lỗi xảy ra", StandardCharsets.UTF_8);
            resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/settings?error=" + errorMsg);
            return;
        }

        resp.sendRedirect(req.getContextPath() + "/admin/infrastructure/settings");
    }
}
