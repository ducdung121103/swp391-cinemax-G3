package com.cinema.modules.catalog.service;

import com.cinema.model.TicketPricing;
import com.cinema.model.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Public Service Interface tính toán giá vé động (TV 3).
 * TV 4 và TV 5 sẽ gọi qua Interface này để tính tiền vé chuẩn xác theo suất chiếu và loại ghế.
 */
public interface PricingService {
    BigDecimal calculateTicketPrice(Long showtimeId, Long seatTypeId, LocalDateTime showTime, String format);

    List<TicketPricing> listPrices();

    String savePrice(TicketPricing pricing, User actor);

    String deletePrice(Long id, User actor);

    int getStudentDiscountPercent();

    int getSeniorDiscountPercent();

    String saveDiscountPercents(int studentPercent, int seniorPercent, User actor);
}
