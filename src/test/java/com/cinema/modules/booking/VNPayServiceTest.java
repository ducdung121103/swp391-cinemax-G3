package com.cinema.modules.booking;

import com.cinema.common.config.VNPayConfig;
import com.cinema.modules.booking.service.VNPayService;
import com.cinema.modules.booking.service.impl.VNPayServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Unit Tests for VNPayService (VNPay Gateway Integration)")
class VNPayServiceTest {

    @Mock
    private HttpServletRequest request;

    private VNPayService vnpayService;

    @BeforeEach
    void setUp() {
        vnpayService = new VNPayServiceImpl();
    }

    @Test
    @DisplayName("Should build valid VNPay payment URL with required parameters and SHA-512 hash")
    void testCreatePaymentUrl() {
        when(request.getHeader(anyString())).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");

        String orderCode = "BK-TEST-1234";
        BigDecimal amount = new BigDecimal("250000");

        String payUrl = vnpayService.createPaymentUrl(request, orderCode, amount, "Test Payment");

        assertNotNull(payUrl);
        assertTrue(payUrl.startsWith("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?"));
        assertTrue(payUrl.contains("vnp_Amount=25000000")); // 250000 * 100
        assertTrue(payUrl.contains("vnp_TxnRef=BK-TEST-1234"));
        assertTrue(payUrl.contains("vnp_SecureHash="));
    }

    @Test
    @DisplayName("Should successfully verify valid VNPay return response")
    void testProcessReturnSuccess() {
        Map<String, String> returnParams = new HashMap<>();
        returnParams.put("vnp_Amount", "25000000");
        returnParams.put("vnp_BankCode", "NCB");
        returnParams.put("vnp_CardType", "ATM");
        returnParams.put("vnp_OrderInfo", "Test");
        returnParams.put("vnp_PayDate", "20261102120000");
        returnParams.put("vnp_ResponseCode", "00");
        returnParams.put("vnp_TmnCode", VNPayConfig.getTmnCode());
        returnParams.put("vnp_TransactionNo", "14567890");
        returnParams.put("vnp_TxnRef", "BK-TEST-1234");

        // Calculate expected hash
        String secureHash = VNPayConfig.hashAllFields(returnParams);

        Vector<String> paramNames = new Vector<>(returnParams.keySet());
        paramNames.add("vnp_SecureHash");

        when(request.getParameterNames()).thenReturn(paramNames.elements());
        for (Map.Entry<String, String> entry : returnParams.entrySet()) {
            when(request.getParameter(entry.getKey())).thenReturn(entry.getValue());
        }
        when(request.getParameter("vnp_SecureHash")).thenReturn(secureHash);

        Map<String, Object> result = vnpayService.processReturn(request);

        assertNotNull(result);
        assertEquals(true, result.get("success"));
        assertEquals(true, result.get("signatureValid"));
        assertEquals("00", result.get("responseCode"));
        assertEquals(new BigDecimal("250000"), result.get("amount"));
        assertEquals("BK-TEST-1234", result.get("orderCode"));
        assertEquals("14567890", result.get("transactionNo"));
    }

    @Test
    @DisplayName("Should detect invalid signature when checksum is tampered")
    void testProcessReturnInvalidSignature() {
        Map<String, String> returnParams = new HashMap<>();
        returnParams.put("vnp_Amount", "25000000");
        returnParams.put("vnp_ResponseCode", "00");
        returnParams.put("vnp_TxnRef", "BK-TEST-1234");

        Vector<String> paramNames = new Vector<>(returnParams.keySet());
        paramNames.add("vnp_SecureHash");

        when(request.getParameterNames()).thenReturn(paramNames.elements());
        for (Map.Entry<String, String> entry : returnParams.entrySet()) {
            when(request.getParameter(entry.getKey())).thenReturn(entry.getValue());
        }
        when(request.getParameter("vnp_SecureHash")).thenReturn("INVALID_HASH_123456789");

        Map<String, Object> result = vnpayService.processReturn(request);

        assertNotNull(result);
        assertEquals(false, result.get("success"));
        assertEquals(false, result.get("signatureValid"));
    }
}

