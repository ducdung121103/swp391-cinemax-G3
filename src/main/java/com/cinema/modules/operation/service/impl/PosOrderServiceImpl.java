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
        if (request == null) {
            throw new IllegalArgumentException("Thiếu thông tin đơn bán tại quầy.");
        }
        if (request.getStaffId() == null) {
            throw new IllegalArgumentException("Không xác định được nhân viên đang bán.");
        }
        if (request.getPaymentMethod() == null
                || !"CASH".equals(request.getPaymentMethod())) {
            throw new IllegalArgumentException("Phương thức thanh toán tại quầy không hợp lệ.");
        }

        request.setChannel("POS");
        request.setUserId(null);
        return bookingEngineService.createBooking(request);
    }
}
