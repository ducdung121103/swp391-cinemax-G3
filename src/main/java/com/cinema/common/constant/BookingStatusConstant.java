package com.cinema.common.constant;

/**
 * Định nghĩa trạng thái vòng đời của Đơn đặt vé (Bookings) và Khóa ghế.
 */
public final class BookingStatusConstant {
    private BookingStatusConstant() {}

    /** Đang tạm giữ ghế trong 5 phút */
    public static final String HOLDING = "HOLDING";

    /** Chờ xử lý thanh toán */
    public static final String PENDING = "PENDING";

    /** Đã thanh toán thành công và xác nhận vé */
    public static final String CONFIRMED = "CONFIRMED";

    /** Khách hủy giao dịch hoặc hoàn tiền */
    public static final String CANCELLED = "CANCELLED";

    /** Hết hạn thời gian giữ ghế hoặc thời gian thanh toán */
    public static final String EXPIRED = "EXPIRED";
}
