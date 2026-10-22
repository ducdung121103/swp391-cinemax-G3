<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="currentUri" value="${pageContext.request.requestURI}" />
<c:set var="roleName" value="${not empty sessionScope.currentUser.role.roleName ? sessionScope.currentUser.role.roleName : (sessionScope.currentUser.roleId == 1 ? 'ADMIN' : (sessionScope.currentUser.roleId == 2 ? 'MANAGER' : (sessionScope.currentUser.roleId == 3 ? 'STAFF' : 'CUSTOMER')))}" />
<c:set var="isAdmin" value="${roleName == 'ADMIN' || sessionScope.currentUser.roleId == 1}" />
<c:set var="isManager" value="${roleName == 'MANAGER' || sessionScope.currentUser.roleId == 2}" />
<c:set var="isStaff" value="${roleName == 'STAFF' || sessionScope.currentUser.roleId == 3}" />

<aside class="admin-sidebar">
    <div class="sidebar-brand">
        <h2>
            <i class="fa-solid fa-clapperboard"></i> CINEMAX
            <c:choose>
                <c:when test="${isAdmin}"><span class="sidebar-brand-subtitle">Admin Core</span></c:when>
                <c:when test="${isManager}"><span class="sidebar-brand-subtitle">Branch Hub</span></c:when>
                <c:when test="${isStaff}"><span class="sidebar-brand-subtitle">POS Station</span></c:when>
                <c:otherwise><span class="sidebar-brand-subtitle">Portal</span></c:otherwise>
            </c:choose>
        </h2>
    </div>

    <div class="sidebar-nav">
        <ul class="sidebar-menu">

            <%-- ==================== 1. PHÂN HỆ ADMIN (QUẢN TRỊ VIÊN TỐI CAO - DŨNG) ==================== --%>
            <c:if test="${isAdmin}">
                <li class="menu-header">BẢNG ĐIỀU KHIỂN</li>
                <li class="${currentUri.endsWith('/dashboard') || currentUri.endsWith('/dashboard.jsp') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard">
                        <i class="fa-solid fa-chart-line"></i>
                        <span>Tổng quan Dashboard</span>
                    </a>
                </li>

                <li class="menu-header">HẠ TẦNG & HỆ THỐNG</li>
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

                <li class="menu-header">DANH MỤC DỊCH VỤ</li>
            </c:if>

            <%-- ==================== 2. PHÂN HỆ MANAGER (QUẢN LÝ CỤM RẠP CHI NHÁNH) ==================== --%>
            <c:if test="${isManager}">
                <li class="menu-header">BẢNG ĐIỀU KHIỂN</li>
                <li class="${currentUri.endsWith('/dashboard') || currentUri.endsWith('/dashboard.jsp') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/admin/dashboard">
                        <i class="fa-solid fa-chart-line"></i>
                        <span>Tổng quan Chi Nhánh</span>
                    </a>
                </li>

                <li class="menu-header">VẬN HÀNH CHI NHÁNH</li>
                <li class="${currentUri.contains('/showtimes') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/manager/showtimes">
                        <i class="fa-solid fa-calendar-days"></i>
                        <span>Quản lý Lịch Chiếu</span>
                    </a>
                </li>
                <li class="${currentUri.contains('/infrastructure/rooms') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms">
                        <i class="fa-solid fa-video"></i>
                        <span>Quản lý Phòng Chiếu</span>
                    </a>
                </li>
                <li class="${currentUri.contains('/operation/fnb') && !currentUri.contains('/fnb-catalog') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/manager/operation/fnb">
                        <i class="fa-solid fa-boxes-stacked"></i>
                        <span>Tồn Kho Bắp Nước</span>
                    </a>
                </li>
                <li class="${currentUri.contains('/operation/fnb-catalog') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/manager/operation/fnb-catalog">
                        <i class="fa-solid fa-burger"></i>
                        <span>Danh Mục F&B</span>
                    </a>
                </li>
                <li class="${currentUri.contains('/operation/pos') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/operation/pos">
                        <i class="fa-solid fa-cash-register"></i>
                        <span>Quầy Bán Vé & POS</span>
                    </a>
                </li>
            </c:if>

            <%-- ==================== 3. PHÂN HỆ STAFF (NHÂN VIÊN THU NGÂN / SOÁT VÉ - TUYỂN) ==================== --%>
            <c:if test="${isStaff}">
                <li class="menu-header">TÁC NGHIỆP TẠI RẠP</li>
                <li class="${currentUri.contains('/operation/pos') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/operation/pos">
                        <i class="fa-solid fa-cash-register"></i>
                        <span>Quầy Bán Vé & POS</span>
                    </a>
                </li>
                <li class="${currentUri.contains('/operation/scanner') ? 'active' : ''}">
                    <a href="${pageContext.request.contextPath}/admin/operation/scanner">
                        <i class="fa-solid fa-qrcode"></i>
                        <span>Soát Vé QR Scanner</span>
                    </a>
                </li>
            </c:if>

        </ul>
    </div>

    <div class="sidebar-footer">
        <c:choose>
            <c:when test="${isAdmin}">
                <span><i class="fa-solid fa-shield-halved"></i> Quản Trị Hệ Thống</span>
            </c:when>
            <c:when test="${isManager}">
                <span><i class="fa-solid fa-building-user"></i> Quản Lý Chi Nhánh</span>
            </c:when>
            <c:when test="${isStaff}">
                <span><i class="fa-solid fa-user-tag"></i> Nhân Viên Thu Ngân</span>
            </c:when>
            <c:otherwise>
                <span><i class="fa-solid fa-user"></i> Người Dùng</span>
            </c:otherwise>
        </c:choose>
        <span style="color: #10b981;">● Online</span>
    </div>
</aside>
