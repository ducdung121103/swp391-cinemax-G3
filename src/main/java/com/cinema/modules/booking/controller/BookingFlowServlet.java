package com.cinema.modules.booking.controller;

import com.cinema.common.dto.ApiResponse;
import com.cinema.common.util.JsonUtil;
import com.cinema.model.User;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller điều hướng luồng Giữ ghế & Thanh toán trực tuyến (TV 4).
 */
@WebServlet(name = "BookingFlowServlet", urlPatterns = {"/booking/hold", "/booking/checkout"})
public class BookingFlowServlet extends HttpServlet {
    private final BookingEngineService bookingEngineService = new BookingEngineServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String showtimeId = req.getParameter("showtimeId");
        req.setAttribute("showtimeId", showtimeId);
        req.getRequestDispatcher("/WEB-INF/views/customer/booking/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json; charset=UTF-8");

        HttpSession session = req.getSession(true);
        User currentUser = (User) session.getAttribute("currentUser");

        if ("/booking/hold".equals(path)) {
            // Giữ ghế tạm thời 5 phút qua AJAX
            Long showtimeId = Long.parseLong(req.getParameter("showtimeId"));
            String[] seatIdArr = req.getParameterValues("seatIds");
            List<Long> seatIds = new ArrayList<>();
            if (seatIdArr != null) {
                for (String s : seatIdArr) seatIds.add(Long.parseLong(s));
            }

            try {
                boolean held = bookingEngineService.holdSeats(showtimeId, seatIds, session.getId());
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(held, "Giữ ghế thành công trong 5 phút")));
            } catch (Exception e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("SEAT_ALREADY_RESERVED", e.getMessage())));
            }
            return;
        }

        if ("/booking/checkout".equals(path)) {
            // Thanh toán đơn hàng
            Long showtimeId = Long.parseLong(req.getParameter("showtimeId"));
            String[] seatIdArr = req.getParameterValues("seatIds");
            List<Long> seatIds = new ArrayList<>();
            if (seatIdArr != null) {
                for (String s : seatIdArr) seatIds.add(Long.parseLong(s));
            }

            CreateBookingDTO dto = CreateBookingDTO.builder()
                    .userId(currentUser != null ? currentUser.getId() : null)
                    .showtimeId(showtimeId)
                    .seatIds(seatIds)
                    .channel("ONLINE")
                    .paymentMethod(req.getParameter("paymentMethod"))
                    .sessionId(session.getId())
                    .build();

            try {
                BookingResult result = bookingEngineService.createBooking(dto);
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(result, "Đặt vé thành công!")));
            } catch (Exception e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("BOOKING_FAILED", e.getMessage())));
            }
        }
    }
}
