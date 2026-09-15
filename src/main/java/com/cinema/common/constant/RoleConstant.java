package com.cinema.common.constant;

/**
 * Định nghĩa chuẩn các Vai trò (Roles) trong toàn bộ hệ thống.
 */
public final class RoleConstant {
    private RoleConstant() {}

    /** Quản trị viên tối cao (Toàn quyền hệ thống) */
    public static final String ADMIN = "ADMIN";

    /** Quản lý chi nhánh cụm rạp */
    public static final String MANAGER = "MANAGER";

    /** Nhân viên quầy vé, bắp nước, soát vé */
    public static final String STAFF = "STAFF";

    /** Khách hàng thành viên */
    public static final String CUSTOMER = "CUSTOMER";
}
