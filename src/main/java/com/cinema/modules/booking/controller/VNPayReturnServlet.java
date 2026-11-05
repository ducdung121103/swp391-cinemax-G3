package com.cinema.modules.booking.controller;

import com.cinema.common.context.DBContext;
import com.cinema.common.transaction.TransactionManager;
import com.cinema.modules.booking.dao.BookingDAO;
import com.cinema.modules.booking.service.VNPayService;
import com.cinema.modules.booking.service.impl.VNPayServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Servlet tiếp nhận phản hồi từ Cổng thanh toán VNPay (URL Return - M-06.1).
 */
@WebServlet(name = "VNPayReturnServlet", urlPatterns = {"/vnpay-return"})
public class VNPayReturnServlet extends HttpServlet {

    private final VNPayService vnpayService = new VNPayServiceImpl();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> returnData = vnpayService.processReturn(request);

        boolean isSuccess = Boolean.TRUE.equals(returnData.get("success"));
        String orderCode = (String) returnData.get("orderCode");

        if (isSuccess && orderCode != null && !orderCode.isBlank()) {
            try {
                TransactionManager.executeInTransaction(conn -> {
                    bookingDAO.updateStatusByCode(conn, orderCode, "CONFIRMED");
                    return true;
                });
            } catch (Exception e) {
                System.err.println("[VNPayReturnServlet] Lỗi cập nhật trạng thái đơn hàng: " + e.getMessage());
            }
        }

        request.setAttribute("isSuccess", isSuccess);
        request.setAttribute("orderCode", orderCode);
        request.setAttribute("amount", returnData.get("amount"));
        request.setAttribute("transactionNo", returnData.get("transactionNo"));
        request.setAttribute("bankCode", returnData.get("bankCode"));
        request.setAttribute("message", returnData.get("message"));
        request.setAttribute("responseCode", returnData.get("responseCode"));

        request.getRequestDispatcher("/WEB-INF/views/customer/booking/vnpay-result.jsp")
                .forward(request, response);
    }
}

