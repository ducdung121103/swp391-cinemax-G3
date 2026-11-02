package com.cinema.modules.booking.service.impl;

import com.cinema.common.config.VNPayConfig;
import com.cinema.modules.booking.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Cài đặt chuẩn dịch vụ tích hợp Cổng thanh toán trực tuyến VNPay 2.1.0 (M-06.1).
 */
public class VNPayServiceImpl implements VNPayService {

    private static final DateTimeFormatter VNPAY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    public String createPaymentUrl(HttpServletRequest request, String orderCode, BigDecimal amount, String orderInfo) {
        if (orderCode == null || orderCode.isBlank()) {
            throw new IllegalArgumentException("Mã đơn hàng không được để trống.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }

        // VNPay quy định số tiền phải nhân với 100 (đơn vị xu/đồng không có phần thập phân)
        long vnpAmount = amount.multiply(BigDecimal.valueOf(100)).longValue();

        LocalDateTime now = LocalDateTime.now();
        String createDate = VNPAY_DATE_FORMAT.format(now);
        String expireDate = VNPAY_DATE_FORMAT.format(now.plusMinutes(15));

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", VNPayConfig.getVersion());
        vnpParams.put("vnp_Command", VNPayConfig.getCommand());
        vnpParams.put("vnp_TmnCode", VNPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(vnpAmount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderCode);
        vnpParams.put("vnp_OrderInfo", (orderInfo != null && !orderInfo.isBlank())
                ? orderInfo : "Thanh toan don ve CineMax " + orderCode);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", VNPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", VNPayConfig.getIpAddress(request));
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

        // Sắp xếp tham số theo alphabet A-Z
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                // Build hash data
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
                     .append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String secureHash = VNPayConfig.hmacSHA512(VNPayConfig.getHashSecret(), hashData.toString());
        return VNPayConfig.getPayUrl() + "?" + query.toString() + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public Map<String, Object> processReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String fieldName = params.nextElement();
            String fieldValue = request.getParameter(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                fields.put(fieldName, fieldValue);
            }
        }

        String vnpSecureHash = fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        // Băm lại toàn bộ dữ liệu trả về theo đúng thuật toán VNPay
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        String calculatedHash = VNPayConfig.hmacSHA512(VNPayConfig.getHashSecret(), hashData.toString());
        boolean isSignatureValid = calculatedHash.equalsIgnoreCase(vnpSecureHash);

        String responseCode = fields.get("vnp_ResponseCode");
        String transactionNo = fields.get("vnp_TransactionNo");
        String orderCode = fields.get("vnp_TxnRef");
        String amountStr = fields.get("vnp_Amount");
        String bankCode = fields.get("vnp_BankCode");
        String payDate = fields.get("vnp_PayDate");

        BigDecimal amount = BigDecimal.ZERO;
        if (amountStr != null) {
            try {
                amount = new BigDecimal(amountStr).divide(BigDecimal.valueOf(100));
            } catch (NumberFormatException ignored) {}
        }

        boolean isSuccess = isSignatureValid && "00".equals(responseCode);

        Map<String, Object> result = new HashMap<>();
        result.put("success", isSuccess);
        result.put("signatureValid", isSignatureValid);
        result.put("responseCode", responseCode != null ? responseCode : "99");
        result.put("transactionNo", transactionNo);
        result.put("orderCode", orderCode);
        result.put("amount", amount);
        result.put("bankCode", bankCode);
        result.put("payDate", payDate);
        result.put("message", resolveMessage(responseCode, isSignatureValid));
        return result;
    }

    private String resolveMessage(String responseCode, boolean isSignatureValid) {
        if (!isSignatureValid) {
            return "Chữ ký số không hợp lệ. Giao dịch có thể bị can thiệp trái phép.";
        }
        if (responseCode == null) {
            return "Không nhận được phản hồi từ cổng VNPay.";
        }
        return switch (responseCode) {
            case "00" -> "Giao dịch thanh toán thành công!";
            case "07" -> "Trừ tiền thành công. Giao dịch bị nghi ngờ gian lận.";
            case "09" -> "Thẻ hoặc tài khoản chưa đăng ký Internet Banking.";
            case "10" -> "Xác thực thông tin thẻ/tài khoản không đúng quá 3 lần.";
            case "11" -> "Đã hết hạn chờ thanh toán.";
            case "12" -> "Thẻ hoặc tài khoản của quý khách đang bị khóa.";
            case "24" -> "Giao dịch bị hủy bởi khách hàng.";
            case "51" -> "Tài khoản của quý khách không đủ số dư để thanh toán.";
            case "65" -> "Tài khoản đã vượt quá hạn mức giao dịch trong ngày.";
            case "75" -> "Ngân hàng thanh toán đang bảo trì.";
            default -> "Giao dịch không thành công (Mã phản hồi: " + responseCode + ").";
        };
    }
}

