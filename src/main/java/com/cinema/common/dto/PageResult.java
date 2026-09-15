package com.cinema.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * Lớp chuẩn hóa kết quả phân trang danh sách (Pagination Result).
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    /** Danh sách phần tử trong trang hiện tại */
    @Builder.Default
    private List<T> content = Collections.emptyList();

    /** Số trang hiện tại (1-indexed) */
    private int pageNumber;

    /** Kích thước số bản ghi trên mỗi trang */
    private int pageSize;

    /** Tổng số bản ghi trên toàn bộ hệ thống */
    private long totalElements;

    /** Tổng số trang tính toán */
    private int totalPages;
}
