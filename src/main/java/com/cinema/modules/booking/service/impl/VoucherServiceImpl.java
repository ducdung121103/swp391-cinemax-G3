package com.cinema.modules.booking.service.impl;

import com.cinema.model.Voucher;
import com.cinema.modules.booking.dao.VoucherDAO;
import com.cinema.modules.booking.service.VoucherService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.time.LocalDateTime;

/**
 * Cài đặt nghiệp vụ Voucher (TV 4).
 */
public class VoucherServiceImpl implements VoucherService {
    private final VoucherDAO voucherDAO;

    public VoucherServiceImpl() {
        this.voucherDAO = new VoucherDAO();
    }

    public VoucherServiceImpl(VoucherDAO voucherDAO) {
        this.voucherDAO = voucherDAO;
    }

    @Override
    public Voucher getVoucherById(Connection conn, Long voucherId) {
        return voucherDAO.findById(conn, voucherId);
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherDAO.findByCode(code);
    }

    @Override
    public BigDecimal calculateAndValidateDiscount(Connection conn, Long voucherId, BigDecimal orderTotal) {
        if (voucherId == null) {
            return BigDecimal.ZERO;
        }

        Voucher voucher = voucherDAO.findById(conn, voucherId);
        if (voucher == null) {
            throw new IllegalArgumentException("Mã khuyến mãi (Voucher) không tồn tại!");
        }

        if (voucher.getIsActive() != null && !voucher.getIsActive()) {
            throw new IllegalStateException("Mã khuyến mãi hiện không còn hiệu lực!");
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getValidFrom() != null && now.isBefore(voucher.getValidFrom())) {
            throw new IllegalStateException("Mã khuyến mãi chưa đến thời gian áp dụng!");
        }
        if (voucher.getValidTo() != null && now.isAfter(voucher.getValidTo())) {
            throw new IllegalStateException("Mã khuyến mãi đã hết hạn sử dụng!");
        }

        if (voucher.getUsageLimit() != null && voucher.getUsedCount() != null
                && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalStateException("Mã khuyến mãi đã hết số lượt sử dụng!");
        }

        if (voucher.getMinOrderAmount() != null && orderTotal.compareTo(voucher.getMinOrderAmount()) < 0) {
            throw new IllegalStateException("Đơn hàng chưa đạt giá trị tối thiểu " + voucher.getMinOrderAmount() + " để áp dụng voucher!");
        }

        BigDecimal discount = BigDecimal.ZERO;
        if ("PERCENT".equalsIgnoreCase(voucher.getDiscountType())) {
            BigDecimal rate = voucher.getDiscountVal().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            discount = orderTotal.multiply(rate).setScale(2, RoundingMode.HALF_UP);
            if (voucher.getMaxDiscount() != null && discount.compareTo(voucher.getMaxDiscount()) > 0) {
                discount = voucher.getMaxDiscount();
            }
        } else if ("FIXED_AMOUNT".equalsIgnoreCase(voucher.getDiscountType())) {
            discount = voucher.getDiscountVal();
        }

        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }

        return discount;
    }

    @Override
    public boolean applyVoucherUsage(Connection conn, Long voucherId) {
        if (voucherId == null) return true;
        boolean updated = voucherDAO.incrementUsedCount(conn, voucherId);
        if (!updated) {
            throw new IllegalStateException("Không thể áp dụng voucher do đã hết lượt sử dụng hoặc có xung đột!");
        }
        return true;
    }
}
