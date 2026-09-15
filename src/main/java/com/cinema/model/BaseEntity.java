package com.cinema.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Lớp thực thể cơ sở (Base Entity) chuẩn hóa cho toàn bộ 28 bảng CSDL.
 * Chứa các trường vết hệ thống (Audit Fields) và cờ trạng thái Soft Delete.
 */
@Getter
@Setter
@ToString
public abstract class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Khóa chính định danh duy nhất */
    protected Long id;

    /** Thời điểm khởi tạo bản ghi */
    protected LocalDateTime createdAt;

    /** Thời điểm cập nhật cuối cùng */
    protected LocalDateTime updatedAt;

    /** Trạng thái kích hoạt (1: Hoạt động, 0: Tạm ngưng) */
    protected Boolean isActive = true;

    /** Trạng thái xóa mềm (1: Đã xóa, 0: Bình thường) */
    protected Boolean isDeleted = false;
}
