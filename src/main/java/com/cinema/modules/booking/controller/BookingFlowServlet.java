package com.cinema.modules.booking.controller;

import com.cinema.common.dto.ApiResponse;
import com.cinema.common.util.JsonUtil;
import com.cinema.common.util.ValidationUtil;
import com.cinema.model.Seat;
import com.cinema.model.Showtime;
import com.cinema.model.User;
import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.VNPayService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import com.cinema.modules.booking.service.impl.VNPayServiceImpl;
import com.cinema.modules.catalog.service.PricingService;
import com.cinema.modules.catalog.service.ShowtimeService;
import com.cinema.modules.catalog.service.impl.PricingServiceImpl;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller Ä‘iá»u hÆ°á»›ng luá»“ng Giá»¯ gháº¿ & Thanh toÃ¡n trá»±c tuyáº¿n (TV 4).
 
 * Refactor update: Lam ro trach nhiem lop cho de bao tri.*/
@WebServlet(name = "BookingFlowServlet", urlPatterns = {"/booking/hold", "/booking/checkout"})
public class BookingFlowServlet extends HttpServlet {
    private final BookingEngineService bookingEngineService = new BookingEngineServiceImpl();
    private final ScreeningRoomService screeningRoomService = new ScreeningRoomServiceImpl();
    private final ShowtimeService showtimeService = new ShowtimeServiceImpl();
    private final PricingService pricingService = new PricingServiceImpl();
    private final VNPayService vnpayService = new VNPayServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String showtimeId = req.getParameter("showtimeId");
        req.setAttribute("showtimeId", showtimeId);
        String[] seatIdArr = req.getParameterValues("seatIds");
        if (seatIdArr != null && seatIdArr.length > 0) {
            List<String> codes = new ArrayList<>();
            for (String raw : seatIdArr) {
                try {
                    Seat seat = screeningRoomService.getSeatById(Long.parseLong(raw));
                    if (seat != null && seat.getSeatCode() != null) {
                        codes.add(seat.getSeatCode());
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            if (!codes.isEmpty()) {
                req.setAttribute("seatLabel", String.join(", ", codes));
            }
        }
        req.getRequestDispatcher("/WEB-INF/views/customer/booking/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        resp.setContentType("application/json; charset=UTF-8");

        HttpSession session = req.getSession(true);
        User currentUser = (User) session.getAttribute("currentUser");

        if ("/booking/hold".equals(path)) {
            // Giá»¯ gháº¿ táº¡m thá»i 5 phÃºt qua AJAX
            Long showtimeId = Long.parseLong(req.getParameter("showtimeId"));
            String[] seatIdArr = req.getParameterValues("seatIds");
            List<Long> seatIds = new ArrayList<>();
            if (seatIdArr != null) {
                for (String s : seatIdArr) seatIds.add(Long.parseLong(s));
            }

            try {
                // Kiểm tra nghiệp vụ chọn ghế đôi Sweetbox
                List<Seat> seats = screeningRoomService.getSeatsByIds(seatIds);
                ValidationUtil.validateCoupleSeats(seats);

                boolean held = bookingEngineService.holdSeats(showtimeId, seatIds, session.getId());
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(held, "Giữ ghế thành công trong 5 phút")));
            } catch (IllegalArgumentException e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("INVALID_SEAT_SELECTION", e.getMessage())));
            } catch (Exception e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("SEAT_ALREADY_RESERVED", e.getMessage())));
            }
            return;
        }

        if ("/booking/checkout".equals(path)) {
            // Thanh toÃ¡n Ä‘Æ¡n hÃ ng
            Long showtimeId = Long.parseLong(req.getParameter("showtimeId"));
            String[] seatIdArr = req.getParameterValues("seatIds");
            List<Long> seatIds = new ArrayList<>();
            if (seatIdArr != null) {
                for (String s : seatIdArr) seatIds.add(Long.parseLong(s));
            }

            Showtime showtime = showtimeService.getShowtimeById(showtimeId);
            if (showtime == null) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("SHOWTIME_NOT_FOUND", "KhÃ´ng tÃ¬m tháº¥y thÃ´ng tin suáº¥t chiáº¿u")));
                return;
            }

            // Kiểm tra nghiệp vụ chọn ghế đôi Sweetbox
            List<Seat> seats = screeningRoomService.getSeatsByIds(seatIds);
            try {
                ValidationUtil.validateCoupleSeats(seats);
            } catch (IllegalArgumentException e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("INVALID_SEAT_SELECTION", e.getMessage())));
                return;
            }

            // Tính giá vé động cho từng ghế dựa trên loại ghế (surcharge từ DB) và suất chiếu
            List<BigDecimal> ticketPrices = new ArrayList<>();
            for (Long seatId : seatIds) {
                Seat seat = screeningRoomService.getSeatById(seatId);
                Long seatTypeId = (seat != null) ? seat.getSeatTypeId() : 1L;
                BigDecimal price = pricingService.calculateTicketPrice(showtimeId, seatTypeId, showtime.getStartTime(), showtime.getExperienceFormat());
                ticketPrices.add(price);
            }

            String voucherIdParam = req.getParameter("voucherId");
            Long voucherId = (voucherIdParam != null && !voucherIdParam.trim().isEmpty()) ? Long.parseLong(voucherIdParam.trim()) : null;

            String paymentMethod = req.getParameter("paymentMethod");
            if (paymentMethod == null || paymentMethod.isBlank()) {
                paymentMethod = "VNPAY";
            }

            CreateBookingDTO dto = CreateBookingDTO.builder()
                    .userId(currentUser != null ? currentUser.getId() : null)
                    .showtimeId(showtimeId)
                    .seatIds(seatIds)
                    .ticketPrices(ticketPrices)
                    .voucherId(voucherId)
                    .channel("ONLINE")
                    .paymentMethod(paymentMethod)
                    .sessionId(session.getId())
                    .build();

            try {
                BookingResult result = bookingEngineService.createBooking(dto);
                if ("VNPAY".equalsIgnoreCase(dto.getPaymentMethod())) {
                    String payUrl = vnpayService.createPaymentUrl(req, result.getBookingCode(),
                            result.getFinalAmount(), "Thanh toan ve CineMax " + result.getBookingCode());
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("success", true);
                    responseData.put("paymentUrl", payUrl);
                    responseData.put("bookingCode", result.getBookingCode());
                    responseData.put("data", result);
                    resp.getWriter().write(JsonUtil.toJson(responseData));
                    return;
                }
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.success(result, "Đặt vé thành công!")));
            } catch (Exception e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("BOOKING_FAILED", e.getMessage())));
            }
        }
    }
}

