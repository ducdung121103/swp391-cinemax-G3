package com.cinema.modules.booking.controller;

import com.cinema.model.User;
import com.cinema.modules.booking.dto.BookingPageData;
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
import java.sql.SQLException;

/**
 * Controller tiếp nhận yêu cầu mở màn hình Chọn ghế & Đặt vé Online của khách hàng.
 * Bắt buộc khách hàng phải đăng nhập tài khoản trước khi đặt vé để quản lý thông tin khách.
 */
@WebServlet(name = "SeatSelectionServlet", urlPatterns = {"/booking/seats"})
public class SeatSelectionServlet extends HttpServlet {

    private final BookingFlowService bookingFlowService = new BookingFlowServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String showtimeId = req.getParameter("showtimeId");

        // 1. Bắt buộc đăng nhập để lấy thông tin khách hàng đặt vé
        HttpSession session = req.getSession(true);
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            String returnUrl = req.getContextPath() + "/booking/seats?showtimeId="
                    + (showtimeId != null ? showtimeId.trim() : "");
            resp.sendRedirect(req.getContextPath() + "/login?msg=login_required&redirect="
                    + URLEncoder.encode(returnUrl, StandardCharsets.UTF_8));
            return;
        }

        try {
            BookingPageData pageData = bookingFlowService.loadBookingPage(showtimeId, session.getId());
            if (pageData == null || pageData.getShowtime() == null) {
                resp.sendRedirect(req.getContextPath() + "/showtimes");
                return;
            }

            req.setAttribute("currentUser", currentUser);
            req.setAttribute("pageData", pageData);
            req.setAttribute("showtime", pageData.getShowtime());
            req.setAttribute("showtimeText", pageData.getShowtimeText());
            req.setAttribute("currentCinema", pageData.getCurrentCinema());
            req.setAttribute("seatMap", pageData.getSeatMap());
            req.setAttribute("seatPrices", pageData.getSeatPrices());
            req.setAttribute("seatColumnCount", pageData.getSeatColumnCount());
            req.setAttribute("availableSeatCount", pageData.getAvailableSeatCount());
            req.setAttribute("fnbItems", pageData.getFnbItems());
            req.setAttribute("studentDiscount", pageData.getStudentDiscount());
            req.setAttribute("seniorDiscount", pageData.getSeniorDiscount());

            req.getRequestDispatcher("/WEB-INF/views/customer/booking/seats.jsp").forward(req, resp);
        } catch (SQLException e) {
            req.setAttribute("seatError", "Không tải được sơ đồ ghế của suất chiếu này.");
            req.getRequestDispatcher("/WEB-INF/views/customer/booking/seats.jsp").forward(req, resp);
        }
    }
}
