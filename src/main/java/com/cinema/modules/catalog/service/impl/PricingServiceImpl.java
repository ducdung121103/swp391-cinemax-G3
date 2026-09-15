package com.cinema.modules.catalog.service.impl;

import com.cinema.modules.catalog.dao.TicketPricingDAO;
import com.cinema.modules.catalog.service.PricingService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Cài đặt nghiệp vụ Tính giá vé (TV 3).
 */
public class PricingServiceImpl implements PricingService {
    private final TicketPricingDAO pricingDAO;

    public PricingServiceImpl() {
        this.pricingDAO = new TicketPricingDAO();
    }

    public PricingServiceImpl(TicketPricingDAO pricingDAO) {
        this.pricingDAO = pricingDAO;
    }

    @Override
    public BigDecimal calculateTicketPrice(Long showtimeId, Long seatTypeId, LocalDateTime showTime, String format) {
        BigDecimal basePrice = pricingDAO.findBasePrice(showTime, format);
        // Nếu là ghế VIP cộng thêm 15k, Ghế Đôi (Sweetbox) cộng thêm 40k
        if (seatTypeId != null) {
            if (seatTypeId == 2L) {
                basePrice = basePrice.add(new BigDecimal("15000.00")); // VIP
            } else if (seatTypeId == 3L) {
                basePrice = basePrice.add(new BigDecimal("40000.00")); // COUPLE
            }
        }
        return basePrice;
    }
}
