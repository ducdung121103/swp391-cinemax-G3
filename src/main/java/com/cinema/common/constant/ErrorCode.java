package com.cinema.common.constant;

/**
 * Mã lỗi hệ thống chuẩn hóa giúp Frontend/Client nhận diện chính xác nguyên nhân lỗi.
 */
public final class ErrorCode {
    private ErrorCode() {}

    public static final String SUCCESS = "SUCCESS";
    public static final String BAD_REQUEST = "BAD_REQUEST";
    public static final String UNAUTHORIZED = "UNAUTHORIZED";
    public static final String FORBIDDEN = "FORBIDDEN";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String SEAT_ALREADY_RESERVED = "SEAT_ALREADY_RESERVED";
    public static final String SEAT_HOLDING_EXPIRED = "SEAT_HOLDING_EXPIRED";
    public static final String TRANSACTION_FAILED = "TRANSACTION_FAILED";
    public static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
}
