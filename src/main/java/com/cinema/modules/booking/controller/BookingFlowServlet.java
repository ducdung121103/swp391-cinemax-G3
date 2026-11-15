package com.cinema.modules.booking.controller;

import com.cinema.common.dto.ApiResponse;
import com.cinema.common.util.JsonUtil;
import com.cinema.model.User;
import com.cinema.modules.booking.dto.BookingCheckoutCommand;
import com.cinema.modules.booking.dto.BookingCheckoutResult;
import com.cinema.modules.booking.dto.BookingQuoteCommand;
import com.cinema.modules.booking.dto.BookingQuoteResult;
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
import java.util.*;

/**
 * Controller điều hướng Giữ ghế & Thanh toán trực tuyến (TV 4 & Online Booking).
 * Bắt buộc đăng nhập để lấy thông tin khách và gắn tài khoản vào vé đã mua.
 */
@WebServlet(name = "BookingFlowServlet", urlPatterns = {
        "/booking/hold", "/booking/checkout", "/booking/retry-payment", "/booking/cancel-payment"
})
public class BookingFlowServlet extends HttpServlet {

    private final BookingFlowService bookingFlowService = new BookingFlowServiceImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String showtimeId = req.getParameter("showtimeId");
        if (showtimeId != null && !showtimeId.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/booking/seats?showtimeId="
                    + URLEncoder.encode(showtimeId.trim(), StandardCharsets.UTF_8));
            return;
        }
        resp.sendRedirect(req.getContextPath() + "/showtimes");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getServletPath();
        resp.setContentType("application/json; charset=UTF-8");

        HttpSession session = req.getSession(true);
        User currentUser = (User) session.getAttribute("currentUser");

        // Bắt buộc khách hàng phải đăng nhập
        if (currentUser == null) {
            if (isAjaxRequest(req)) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("LOGIN_REQUIRED",
                        "Vui lòng đăng nhập tài khoản để tiếp tục đặt vé.")));
            } else {
                resp.sendRedirect(req.getContextPath() + "/login?msg=login_required");
            }
            return;
        }

        if ("/booking/retry-payment".equals(path)) {
            try {
                String paymentUrl = bookingFlowService.retryVnpayPayment(
                        req.getParameter("bookingCode"), currentUser.getId(), req);
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("paymentUrl", paymentUrl);
                resp.getWriter().write(JsonUtil.toJson(responseData));
            } catch (Exception e) {
                resp.getWriter().write(JsonUtil.toJson(
                        ApiResponse.error("PAYMENT_RETRY_FAILED", e.getMessage())));
            }
            return;
        }

        if ("/booking/cancel-payment".equals(path)) {
            try {
                boolean cancelled = bookingFlowService.cancelPendingVnpayBooking(
                        req.getParameter("bookingCode"), currentUser.getId());
                if (!cancelled) {
                    resp.getWriter().write(JsonUtil.toJson(
                            ApiResponse.error("PAYMENT_CANCEL_FAILED",
                                    "Đơn không còn chờ thanh toán hoặc không thuộc tài khoản của bạn.")));
                    return;
                }
                Map<String, Object> responseData = new HashMap<>();
                responseData.put("success", true);
                responseData.put("message", "Đã hủy đơn và giải phóng ghế.");
                resp.getWriter().write(JsonUtil.toJson(responseData));
            } catch (Exception e) {
                resp.getWriter().write(JsonUtil.toJson(
                        ApiResponse.error("PAYMENT_CANCEL_FAILED", e.getMessage())));
            }
            return;
        }

        if ("/booking/hold".equals(path)) {
            try {
                boolean held = bookingFlowService.holdSeats(
                        req.getParameter("showtimeId"),
                        parseSeatIds(req.getParameterValues("seatIds")),
                        session.getId());
                Map<String, Object> respData = new HashMap<>();
                respData.put("success", held);
                respData.put("status", "SUCCESS");
                respData.put("code", "200");
                respData.put("data", held);
                respData.put("message", "Ghế đã được giữ thành công trong 5 phút");
                resp.getWriter().write(JsonUtil.toJson(respData));
            } catch (IllegalArgumentException e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("INVALID_SEAT_SELECTION", e.getMessage())));
            } catch (Exception e) {
                resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("SEAT_ALREADY_RESERVED", e.getMessage())));
            }
            return;
        }

        if ("/booking/checkout".equals(path)) {
            boolean isAjax = isAjaxRequest(req);
            String showtimeId = req.getParameter("showtimeId");

            // Xử lý báo giá thời gian thực
            if ("quote".equalsIgnoreCase(req.getParameter("mode"))) {
                try {
                    BookingQuoteCommand command = BookingQuoteCommand.builder()
                            .showtimeId(showtimeId)
                            .seatIds(parseSeatIds(req.getParameterValues("seatIds")))
                            .ticketClass(req.getParameter("ticketClass"))
                            .fnbQuantities(parseFnbItems(req.getParameterMap()))
                            .build();
                    BookingQuoteResult quote = bookingFlowService.quote(command);
                    resp.getWriter().write(JsonUtil.toJson(quote));
                } catch (Exception e) {
                    resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("QUOTE_FAILED", e.getMessage())));
                }
                return;
            }

            // Xử lý thanh toán và tạo đơn hàng
            try {
                BookingCheckoutCommand command = BookingCheckoutCommand.builder()
                        .user(currentUser)
                        .showtimeId(showtimeId)
                        .seatIds(parseSeatIds(req.getParameterValues("seatIds")))
                        .ticketClass(req.getParameter("ticketClass"))
                        .fnbQuantities(parseFnbItems(req.getParameterMap()))
                        .voucherId(req.getParameter("voucherId"))
                        .paymentMethod(req.getParameter("paymentMethod"))
                        .sessionId(session.getId())
                        .request(req)
                        .build();

                BookingCheckoutResult result = bookingFlowService.checkout(command);

                if ("VNPAY".equalsIgnoreCase(result.getPaymentMethod()) && result.getPaymentUrl() != null) {
                    if (isAjax) {
                        Map<String, Object> responseData = new HashMap<>();
                        responseData.put("success", true);
                        responseData.put("status", "SUCCESS");
                        responseData.put("paymentUrl", result.getPaymentUrl());
                        responseData.put("bookingCode", result.getBookingCode());
                        responseData.put("data", result.getBookingResult());
                        resp.getWriter().write(JsonUtil.toJson(responseData));
                    } else {
                        resp.sendRedirect(result.getPaymentUrl());
                    }
                    return;
                }

                if (isAjax) {
                    Map<String, Object> responseData = new HashMap<>();
                    responseData.put("success", true);
                    responseData.put("status", "SUCCESS");
                    responseData.put("bookingCode", result.getBookingCode());
                    responseData.put("data", result.getBookingResult());
                    responseData.put("message", "Đặt vé và thanh toán thành công!");
                    resp.getWriter().write(JsonUtil.toJson(responseData));
                } else {
                    resp.sendRedirect(req.getContextPath() + "/customer/bookings?code=" + result.getBookingCode());
                }
            } catch (Exception e) {
                sendError(req, resp, isAjax, showtimeId, e.getMessage());
            }
        }
    }

    private List<Long> parseSeatIds(String[] seatIdArr) {
        List<Long> seatIds = new ArrayList<>();
        if (seatIdArr != null) {
            for (String raw : seatIdArr) {
                if (raw != null && !raw.isBlank()) {
                    try {
                        seatIds.add(Long.parseLong(raw.trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return seatIds;
    }

    private Map<Long, Integer> parseFnbItems(Map<String, String[]> params) {
        Map<Long, Integer> fnbItems = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : params.entrySet()) {
            if (entry.getKey().startsWith("fnb_")) {
                String itemIdStr = entry.getKey().substring(4);
                String val = (entry.getValue() != null && entry.getValue().length > 0) ? entry.getValue()[0] : null;
                if (val != null && !val.isBlank()) {
                    try {
                        int qty = Integer.parseInt(val.trim());
                        if (qty > 0) {
                            fnbItems.put(Long.parseLong(itemIdStr), qty);
                        }
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
        return fnbItems;
    }

    private boolean isAjaxRequest(HttpServletRequest req) {
        String requestedWith = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (accept != null && accept.contains("application/json"))
                || "true".equalsIgnoreCase(req.getParameter("ajax"));
    }

    private void sendError(HttpServletRequest req, HttpServletResponse resp, boolean isAjax, String showtimeId, String error) throws IOException {
        if (isAjax) {
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.error("BOOKING_FAILED", error != null ? error : "Lỗi xử lý đặt vé.")));
        } else {
            String redirectUrl = req.getContextPath() + "/booking/seats?";
            if (showtimeId != null) {
                redirectUrl += "showtimeId=" + URLEncoder.encode(showtimeId, StandardCharsets.UTF_8) + "&";
            }
            redirectUrl += "error=" + URLEncoder.encode(error != null ? error : "Lỗi xử lý đặt vé", StandardCharsets.UTF_8);
            resp.sendRedirect(redirectUrl);
        }
    }
}
