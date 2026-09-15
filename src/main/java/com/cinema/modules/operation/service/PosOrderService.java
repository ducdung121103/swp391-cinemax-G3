package com.cinema.modules.operation.service;

import com.cinema.modules.booking.dto.BookingResult;
import com.cinema.modules.booking.dto.CreateBookingDTO;

/**
 * Public Service Interface quản lý bán vé và bắp nước tại quầy POS (TV 5).
 * POS chỉ đóng vai trò Client; ủy quyền lưu trữ đơn hàng sang BookingEngineService của TV 4!
 */
public interface PosOrderService {
    BookingResult processCounterCheckout(CreateBookingDTO request);
}
