<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="admin-navbar">
    <div class="navbar-left">
        <span class="system-title">
            <i class="fa-solid fa-server" style="color: #e50914;"></i>
            Hệ Thống Quản Trị Trung Tâm CineMax
        </span>
    </div>
    <div class="navbar-right">
        <div class="admin-user-badge">
            <i class="fa-solid fa-user-shield"></i>
            <span>${not empty sessionScope.currentUser.fullName ? sessionScope.currentUser.fullName : 'Hệ Thống Quản Trị Viên'}</span>
            <span class="role-tag">${not empty sessionScope.currentUser.role.roleName ? sessionScope.currentUser.role.roleName : 'ADMIN'}</span>
        </div>
        <a href="${pageContext.request.contextPath}/" target="_blank" class="btn-nav btn-nav-outline" title="Mở trang chủ khách hàng">
            <i class="fa-solid fa-arrow-up-right-from-square"></i>
            <span>Xem Web Khách</span>
        </a>
        <a href="${pageContext.request.contextPath}/logout" class="btn-nav btn-nav-logout" title="Đăng xuất khỏi phiên làm việc">
            <i class="fa-solid fa-right-from-bracket"></i>
            <span>Đăng xuất</span>
        </a>
    </div>
</div>
