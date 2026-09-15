<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<header class="main-header">
    <div class="header-container">
        <a href="${pageContext.request.contextPath}/" class="logo">
            <span class="cinema-icon">🎬</span> CINEMA CHAIN
        </a>
        <nav class="nav-menu">
            <a href="${pageContext.request.contextPath}/movies">Phim Đang Chiếu</a>
            <a href="${pageContext.request.contextPath}/showtimes">Lịch Chiếu</a>
            <a href="${pageContext.request.contextPath}/vouchers">Khuyến Mãi</a>
        </nav>
        <div class="user-action">
            <c:choose>
                <c:when test="${not empty sessionScope.currentUser}">
                    <span class="user-greeting">Xin chào, <strong>${sessionScope.currentUser.fullName}</strong></span>
                    <c:if test="${sessionScope.currentUser.role.roleName != 'CUSTOMER'}">
                        <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-admin">Quản trị</a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/logout" class="btn-logout">Đăng xuất</a>
                </c:when>
                <c:otherwise>
                    <a href="${pageContext.request.contextPath}/login" class="btn-login">Đăng nhập</a>
                    <a href="${pageContext.request.contextPath}/register" class="btn-register">Đăng ký</a>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</header>
