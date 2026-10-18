<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="currentUri" value="${pageContext.request.requestURI}" />
<aside class="admin-sidebar">
    <div class="sidebar-brand">
        <h2>
            <i class="fa-solid fa-clapperboard"></i> CINEMAX ADMIN
            <span class="sidebar-brand-subtitle">Core</span>
        </h2>
    </div>
    <div class="sidebar-nav">
        <ul class="sidebar-menu">
            <li class="menu-header">BẢNG ĐIỀU KHIỂN</li>
            <li class="${currentUri.endsWith('/dashboard') || currentUri.endsWith('/dashboard.jsp') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/dashboard">
                    <i class="fa-solid fa-chart-line"></i>
                    <span>Tổng quan Dashboard</span>
                </a>
            </li>

            <!-- CHỨC NĂNG QUẢN TRỊ VIÊN (ADMIN - DŨNG) -->
            <li class="menu-header">HẠ TẦNG & HỆ THỐNG (ADMIN)</li>
            <li class="${currentUri.contains('/infrastructure/cinemas') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/infrastructure/cinemas">
                    <i class="fa-solid fa-building"></i>
                    <span>Quản lý Cụm Rạp</span>
                </a>
            </li>
            <li class="${currentUri.contains('/infrastructure/rooms') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms">
                    <i class="fa-solid fa-video"></i>
                    <span>Quản lý Phòng Chiếu</span>
                </a>
            </li>
            <li class="${currentUri.contains('/infrastructure/seats') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/infrastructure/seats">
                    <i class="fa-solid fa-couch"></i>
                    <span>Thiết Kế Sơ Đồ Ghế</span>
                </a>
            </li>
            <li class="${currentUri.contains('/infrastructure/occupancy') || currentUri.contains('/reports/occupancy') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/infrastructure/occupancy">
                    <i class="fa-solid fa-chart-pie"></i>
                    <span>Báo Cáo Tỷ Lệ Lấp Đầy</span>
                </a>
            </li>
            <li class="${currentUri.contains('/infrastructure/settings') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/infrastructure/settings">
                    <i class="fa-solid fa-sliders"></i>
                    <span>Cấu Hình Hệ Thống</span>
                </a>
            </li>

            <!-- VẬN HÀNH & BÁN LẺ (OPERATION - TUYỂN) -->
            <li class="menu-header">VẬN HÀNH & POS (STAFF/MANAGER)</li>
            <li class="${currentUri.contains('/operation/pos') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/operation/pos">
                    <i class="fa-solid fa-cash-register"></i>
                    <span>Quầy Bán Vé & POS</span>
                </a>
            </li>
            <li class="${currentUri.contains('/operation/fnb') && !currentUri.contains('/fnb-catalog') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/operation/fnb">
                    <i class="fa-solid fa-boxes-stacked"></i>
                    <span>Tồn Kho Bắp Nước</span>
                </a>
            </li>
            <li class="${currentUri.contains('/operation/fnb-catalog') ? 'active' : ''}">
                <a href="${pageContext.request.contextPath}/admin/operation/fnb-catalog">
                    <i class="fa-solid fa-burger"></i>
                    <span>Danh Mục F&B</span>
                </a>
            </li>
        </ul>
    </div>
    <div class="sidebar-footer">
        <span><i class="fa-solid fa-shield-halved"></i> Quản Trị Hệ Thống</span>
        <span style="color: #10b981;">● Online</span>
    </div>
</aside>
