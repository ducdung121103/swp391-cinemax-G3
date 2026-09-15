package com.cinema.modules.operation.controller;

import com.cinema.common.dto.ApiResponse;
import com.cinema.common.util.JsonUtil;
import com.cinema.model.TicketCheckinLog;
import com.cinema.model.User;
import com.cinema.modules.operation.service.CheckinService;
import com.cinema.modules.operation.service.impl.CheckinServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/**
 * Controller kiểm tra soát vé qua Camera QR (TV 5).
 */
@WebServlet(name = "ScannerServlet", urlPatterns = {"/admin/operation/scanner"})
public class ScannerServlet extends HttpServlet {
    private final CheckinService checkinService = new CheckinServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/admin/operation/scanner.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json; charset=UTF-8");
        String barcode = req.getParameter("barcode");

        HttpSession session = req.getSession(false);
        User staff = (session != null) ? (User) session.getAttribute("currentUser") : null;
        Long staffId = (staff != null) ? staff.getId() : null;

        if (barcode == null || barcode.trim().isEmpty()) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("BAD_REQUEST", "Mã vé không được để trống!")));
            return;
        }

        TicketCheckinLog log = checkinService.checkinTicket(barcode.trim(), staffId);
        if ("SUCCESS".equals(log.getStatus())) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(log, "Vé hợp lệ! Mời quý khách vào phòng chiếu.")));
        } else if ("REJECTED_ALREADY_USED".equals(log.getStatus())) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("REJECTED_ALREADY_USED", "CẢNH BÁO: Vé này ĐÃ ĐƯỢC SỬ DỤNG trước đó!")));
        } else {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("INVALID_TICKET", "Vé không hợp lệ hoặc không tồn tại!")));
        }
    }
}
