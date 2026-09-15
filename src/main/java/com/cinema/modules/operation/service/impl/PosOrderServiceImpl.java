package com.cinema.modules.operation.service.impl;

import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;
import com.cinema.modules.booking.service.BookingEngineService;
import com.cinema.modules.booking.service.impl.BookingEngineServiceImpl;
import com.cinema.modules.operation.service.PosOrderService;

/**
 * Cài đặt nghiệp vụ Bán vé tại quầy POS (TV 5).
 * Minh họa chuẩn Clean Architecture: Gọi Core Engine của TV 4 qua Interface công khai,
 * CẤM TUYỆT ĐỐI việc import BookingDAO hay OrderItemDAO của TV 4!
 */
public class PosOrderServiceImpl implements PosOrderService {
    // Ủy quyền nghiệp vụ xử lý Transaction cho BookingEngineService của TV 4
    private final BookingEngineService bookingEngineService;

    public PosOrderServiceImpl() {
        this.bookingEngineService = new BookingEngineServiceImpl();
    }

    public PosOrderServiceImpl(BookingEngineService bookingEngineService) {
        this.bookingEngineService = bookingEngineService;
    }

    @Override
    public BookingResult processCounterCheckout(CreateBookingDTO request) {
        request.setChannel("POS");
        return bookingEngineService.createBooking(request);
    }
}
