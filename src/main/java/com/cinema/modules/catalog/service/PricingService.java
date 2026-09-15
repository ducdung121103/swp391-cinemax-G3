package com.cinema.modules.catalog.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Public Service Interface tính toán giá vé động (TV 3).
 * TV 4 và TV 5 sẽ gọi qua Interface này để tính tiền vé chuẩn xác theo suất chiếu và loại ghế.
 */
public interface PricingService {
    BigDecimal calculateTicketPrice(Long showtimeId, Long seatTypeId, LocalDateTime showTime, String format);
}
