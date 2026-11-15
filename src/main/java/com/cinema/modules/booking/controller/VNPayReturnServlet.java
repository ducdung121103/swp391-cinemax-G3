package com.cinema.modules.booking.controller;

import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
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
    private final BookingEngineService bookingEngineService = new BookingEngineServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> returnData = vnpayService.processReturn(request);

        boolean gatewaySuccess = Boolean.TRUE.equals(returnData.get("success"));
        boolean signatureValid = Boolean.TRUE.equals(returnData.get("signatureValid"));
        String orderCode = (String) returnData.get("orderCode");
        String transactionNo = (String) returnData.get("transactionNo");
        Object rawAmount = returnData.get("amount");
        java.math.BigDecimal amount = rawAmount instanceof java.math.BigDecimal
                ? (java.math.BigDecimal) rawAmount : null;

        boolean isSuccess = false;
        if (signatureValid && orderCode != null && !orderCode.isBlank() && amount != null) {
            try {
                boolean finalized = bookingEngineService.finalizeVnpayPayment(
                        orderCode, amount, transactionNo, gatewaySuccess);
                isSuccess = gatewaySuccess && finalized;
                if (gatewaySuccess && !finalized) {
                    returnData.put("message",
                            "Không thể xác nhận thanh toán: mã đơn, số tiền hoặc trạng thái giao dịch không khớp.");
                }
            } catch (RuntimeException e) {
                throw new ServletException("Không thể cập nhật kết quả thanh toán VNPay.", e);
            }
        } else if (gatewaySuccess) {
            returnData.put("message", "Phản hồi thanh toán VNPay không hợp lệ.");
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
