package com.cinema.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Lớp chuẩn hóa kết quả phản hồi JSON (API Response Wrapper) cho các yêu cầu AJAX.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    /** Trạng thái xử lý (SUCCESS hoặc FAIL) */
    private String status;

    /** Mã lỗi hoặc mã trạng thái */
    private String code;

    /** Thông điệp mô tả cho Client / Người dùng */
    private String message;

    /** Dữ liệu trả về (Generic Data Payload) */
    private T data;

    /** Thời điểm sinh phản hồi */
    @Builder.Default
    private String timestamp = LocalDateTime.now().toString();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .status("SUCCESS")
                .code("200")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Thao tác thành công");
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .status("FAIL")
                .code(code)
                .message(message)
                .data(null)
                .timestamp(LocalDateTime.now().toString())
                .build();
    }
}
