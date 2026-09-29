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
        if (seatTypeId != null) {
            BigDecimal surcharge = pricingDAO.findSeatTypeSurcharge(seatTypeId);
            if (surcharge != null && surcharge.compareTo(BigDecimal.ZERO) > 0) {
                basePrice = basePrice.add(surcharge);
            }
        }
        return basePrice;
    }
}
