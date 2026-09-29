package com.cinema.modules.booking.service;

import com.cinema.model.Voucher;

import java.math.BigDecimal;
import java.sql.Connection;

/**
 * Service quản lý và áp dụng mã giảm giá Voucher (TV 4).
 */
public interface VoucherService {
    Voucher getVoucherById(Connection conn, Long voucherId);
    Voucher getVoucherByCode(String code);
    BigDecimal calculateAndValidateDiscount(Connection conn, Long voucherId, BigDecimal orderTotal);
    boolean applyVoucherUsage(Connection conn, Long voucherId);
}
