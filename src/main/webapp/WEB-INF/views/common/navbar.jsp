<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="admin-navbar">
    <div class="navbar-left">
        <span class="system-title">Hệ Thống Quản Trị Trung Tâm</span>
    </div>
    <div class="navbar-right">
        <span class="admin-user">Tài khoản: <strong>${sessionScope.currentUser.fullName}</strong> (${sessionScope.currentUser.role.roleName})</span>
        <a href="${pageContext.request.contextPath}/" target="_blank" class="btn-portal">Xem trang Web Khách</a>
        <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Đăng xuất</a>
    </div>
</div>
