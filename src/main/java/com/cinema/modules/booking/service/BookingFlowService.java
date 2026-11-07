package com.cinema.modules.booking.service;

import com.cinema.modules.booking.dto.BookingCheckoutCommand;
import com.cinema.modules.booking.dto.BookingCheckoutResult;
import com.cinema.modules.booking.dto.BookingPageData;
import com.cinema.modules.booking.dto.BookingQuoteCommand;
import com.cinema.modules.booking.dto.BookingQuoteResult;
import com.cinema.modules.booking.dto.CustomerBookingHistoryDTO;

import java.sql.SQLException;
import java.util.List;

/**
 * Service điều phối toàn bộ nghiệp vụ luồng Đặt vé Online của khách hàng (3-tier architecture).
 * Tách biệt hoàn toàn business logic khỏi Servlet.
 */
public interface BookingFlowService {

    /**
     * Tải đầy đủ dữ liệu trang đặt vé: suất chiếu, rạp, sơ đồ ghế, bảng giá động, bắp nước, % ưu đãi.
     */
    BookingPageData loadBookingPage(String showtimeId, String sessionId) throws SQLException;

    /**
     * Giữ ghế tạm thời 5 phút sau khi kiểm tra quy tắc ghế đôi Sweetbox.
     */
    boolean holdSeats(String showtimeId, List<Long> seatIds, String sessionId);

    /**
     * Tính toán báo giá thời gian thực (vé, ưu đãi loại vé, bắp nước, tổng tiền).
     */
    BookingQuoteResult quote(BookingQuoteCommand command);

    /**
     * Thực hiện thanh toán và tạo booking, sinh URL cổng thanh toán VNPay nếu là VNPAY.
     */
    BookingCheckoutResult checkout(BookingCheckoutCommand command);

    /**
     * Lấy lịch sử đặt vé xem phim của khách hàng.
     */
    List<CustomerBookingHistoryDTO> getCustomerBookingHistory(Long userId);

    /**
     * Lấy thông tin chi tiết của một mã vé / đơn hàng.
     */
    CustomerBookingHistoryDTO getCustomerBookingDetail(String bookingCode, Long userId);
}

