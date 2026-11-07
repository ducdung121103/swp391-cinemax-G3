package com.cinema.modules.booking.controller;

import com.cinema.common.dto.ApiResponse;
import com.cinema.common.util.JsonUtil;
import com.cinema.model.User;
import com.cinema.modules.booking.dto.CustomerBookingHistoryDTO;
import com.cinema.modules.booking.service.BookingFlowService;
import com.cinema.modules.booking.service.impl.BookingFlowServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Controller xem Lịch sử mua vé & Vé điện tử cho Khách hàng.
 */
@WebServlet(name = "BookingHistoryServlet", urlPatterns = {"/customer/bookings", "/booking/history", "/profile/bookings"})
public class BookingHistoryServlet extends HttpServlet {

    private final BookingFlowService bookingFlowService = new BookingFlowServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (currentUser == null) {
            String redirectUrl = req.getRequestURI();
            if (req.getQueryString() != null) {
                redirectUrl += "?" + req.getQueryString();
            }
            resp.sendRedirect(req.getContextPath() + "/login?msg=login_required&redirect="
                    + URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8));
            return;
        }

        // 1. Nếu yêu cầu chi tiết 1 đơn hàng qua AJAX JSON (để hiển thị Modal vé điện tử)
        String bookingCode = req.getParameter("code");
        if (bookingCode == null || bookingCode.isBlank()) {
            bookingCode = req.getParameter("bookingCode");
        }
        if (bookingCode != null && !bookingCode.isBlank() && isAjaxRequest(req)) {
            resp.setContentType("application/json; charset=UTF-8");
            CustomerBookingHistoryDTO detail = bookingFlowService.getCustomerBookingDetail(bookingCode, currentUser.getId());
            if (detail != null) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(detail)));
            } else {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("NOT_FOUND", "Không tìm thấy thông tin vé.")));
            }
            return;
        }

        // 2. Tải toàn bộ danh sách lịch sử mua vé của khách hàng
        List<CustomerBookingHistoryDTO> history = bookingFlowService.getCustomerBookingHistory(currentUser.getId());
        req.setAttribute("historyList", history);
        req.setAttribute("totalBookings", history.size());
        req.setAttribute("activeMenu", "bookings");

        req.getRequestDispatcher("/WEB-INF/views/customer/booking/history.jsp").forward(req, resp);
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        String xrw = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xrw) || (accept != null && accept.contains("application/json"));
    }
}

