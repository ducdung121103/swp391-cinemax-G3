<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<aside class="admin-sidebar">
    <div class="sidebar-brand">
        <h2>🎬 CINEMA ADMIN</h2>
    </div>
    <ul class="sidebar-menu">
        <li><a href="${pageContext.request.contextPath}/admin/dashboard"><i class="icon">📊</i> Tổng quan Dashboard</a></li>

        <!-- TV 1: Infrastructure -->
        <li class="menu-header">HẠ TẦNG & RẠP (TV 1)</li>
        <li><a href="${pageContext.request.contextPath}/admin/infrastructure/branches"><i class="icon">🏢</i> Chi nhánh cụm rạp</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/infrastructure/halls"><i class="icon">🎥</i> Phòng chiếu & Sơ đồ ghế</a></li>

        <!-- TV 2: Identity -->
        <li class="menu-header">NGƯỜI DÙNG & HỘI VIÊN (TV 2)</li>
        <li><a href="${pageContext.request.contextPath}/admin/identity/users"><i class="icon">👥</i> Quản lý tài khoản</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/identity/tiers"><i class="icon">⭐</i> Hạng thành viên & Điểm</a></li>

        <!-- TV 3: Catalog -->
        <li class="menu-header">PHIM & LỊCH CHIẾU (TV 3)</li>
        <li><a href="${pageContext.request.contextPath}/admin/catalog/movies"><i class="icon">🎞️</i> Danh mục Phim</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/catalog/showtimes"><i class="icon">🕒</i> Xếp lịch chiếu</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/catalog/prices"><i class="icon">💵</i> Bảng giá vé</a></li>

        <!-- TV 4: Booking & Billing -->
        <li class="menu-header">HÓA ĐƠN & VOUCHER (TV 4)</li>
        <li><a href="${pageContext.request.contextPath}/admin/booking/transactions"><i class="icon">🧾</i> Lịch sử đặt vé & Hóa đơn</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/booking/vouchers"><i class="icon">🏷️</i> Quản lý Khuyến mãi</a></li>

        <!-- TV 5: Operation POS & FnB -->
        <li class="menu-header">VẬN HÀNH & BÁN LẺ (TV 5)</li>
        <li><a href="${pageContext.request.contextPath}/admin/operation/pos"><i class="icon">🖥️</i> Quầy Bán vé & F&B (POS)</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/operation/scanner"><i class="icon">📱</i> Soát vé qua Camera QR</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/operation/fnb"><i class="icon">🍿</i> Tồn kho Bắp Nước</a></li>
        <li><a href="${pageContext.request.contextPath}/admin/operation/drawers"><i class="icon">💰</i> Ca làm & Két tiền</a></li>
    </ul>
</aside>
