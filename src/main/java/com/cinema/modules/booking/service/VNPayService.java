package com.cinema.modules.booking.service;

import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Service xử lý tích hợp Cổng thanh toán trực tuyến VNPay (M-06.1).
 */
public interface VNPayService {

    /**
     * Tạo URL chuyển hướng sang cổng thanh toán VNPay Sandbox với chữ ký bảo mật SHA-512.
     *
     * @param request   HttpServletRequest hiện tại (lấy IP client và context path)
     * @param orderCode Mã đơn hàng đặt vé (vnp_TxnRef)
     * @param amount    Số tiền cần thanh toán
     * @param orderInfo Thông tin mô tả đơn hàng
     * @return URL thanh toán VNPay đầy đủ
     */
    String createPaymentUrl(HttpServletRequest request, String orderCode, BigDecimal amount, String orderInfo);

    /**
     * Xác thực chữ ký số và kết quả phản hồi trả về từ VNPay qua URL Return hoặc IPN.
     *
     * @param request HttpServletRequest chứa các tham số vnp_*
     * @return Map chứa kết quả: success (boolean), responseCode, transactionNo, orderCode, message
     */
    Map<String, Object> processReturn(HttpServletRequest request);
}

