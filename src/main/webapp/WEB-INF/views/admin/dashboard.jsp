<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Tổng quan Quản trị Hệ Thống - Cinema Admin Dashboard</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../common/navbar.jsp" />
            <div class="admin-content">

                <!-- Welcome Banner & Core Quick Actions -->
                <div class="admin-card" style="margin-bottom: 24px;">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 15px;">
                        <div>
                            <h2 style="margin: 0 0 6px 0; font-size: 1.45rem; font-weight: 800; color: #0f172a;">
                                Xin chào, ${not empty sessionScope.currentUser.fullName ? sessionScope.currentUser.fullName : 'Hệ Thống Quản Trị Viên'}!
                            </h2>
                            <p style="margin: 0; color: #64748b; font-size: 0.92rem;">
                                Hệ thống Quản trị Cụm rạp Chi nhánh Toàn quốc (CineMax - SWP391 Nhóm 3). 
                                Phân hệ: <strong style="color: #e50914;">HẠ TẦNG & THAM SỐ HỆ THỐNG</strong>
                            </p>
                        </div>
                        <span class="role-tag" style="padding: 6px 14px; font-size: 0.82rem;">
                            <i class="fa-solid fa-crown" style="margin-right: 5px;"></i> ${not empty sessionScope.currentUser.role.roleName ? sessionScope.currentUser.role.roleName : 'ADMIN'}
                        </span>
                    </div>

                    <div class="quick-actions">
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/cinemas" class="btn-action">
                            <i class="fa-solid fa-building"></i> Quản lý Cụm Rạp
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms" class="btn-action btn-action-secondary">
                            <i class="fa-solid fa-video"></i> Quản lý Phòng Chiếu
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/seats" class="btn-action btn-action-secondary">
                            <i class="fa-solid fa-couch"></i> Thiết kế Sơ đồ Ghế
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/settings" class="btn-action btn-action-outline">
                            <i class="fa-solid fa-sliders"></i> Cấu hình Hệ Thống
                        </a>
                    </div>
                </div>

                <!-- Thống kê chỉ số thực tế (Clean SVG/FontAwesome Vectors) -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon-wrapper stat-icon-red">
                            <i class="fa-solid fa-building"></i>
                        </div>
                        <div class="stat-info">
                            <h3>${totalCinemas}</h3>
                            <p>Cụm rạp chi nhánh</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon-wrapper stat-icon-blue">
                            <i class="fa-solid fa-film"></i>
                        </div>
                        <div class="stat-info">
                            <h3>${totalRooms}</h3>
                            <p>Phòng chiếu hoạt động</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon-wrapper stat-icon-purple">
                            <i class="fa-solid fa-couch"></i>
                        </div>
                        <div class="stat-info">
                            <h3>${totalSeats > 0 ? totalSeats : (totalRooms * 100)}</h3>
                            <p>Ghế ngồi phục vụ</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon-wrapper stat-icon-orange">
                            <i class="fa-solid fa-sliders"></i>
                        </div>
                        <div class="stat-info">
                            <h3>9+</h3>
                            <p>Tham số toàn cục</p>
                        </div>
                    </div>
                </div>

                <!-- Danh sách cụm rạp tóm tắt với Enterprise Table Layout -->
                <div class="admin-card">
                    <div class="admin-card-header">
                        <h3 class="admin-card-title">
                            <i class="fa-solid fa-city"></i>
                            Danh sách Cụm Rạp Chi Nhánh
                        </h3>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/cinemas" class="action-link">
                            <span>Quản lý chi tiết</span>
                            <i class="fa-solid fa-arrow-right"></i>
                        </a>
                    </div>

                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="width: 100px;">Mã Rạp</th>
                                    <th>Tên Cụm Rạp</th>
                                    <th style="width: 140px;">Thành Phố</th>
                                    <th>Địa Chỉ</th>
                                    <th style="width: 140px;">Hotline</th>
                                    <th style="width: 130px;">Quy Mô</th>
                                    <th style="width: 150px;">Trạng Thái</th>
                                    <th style="width: 140px; text-align: right;">Hành Động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${cinemas}" var="c">
                                    <tr>
                                        <td>
                                            <span class="badge-code">${c.cinemaCode}</span>
                                        </td>
                                        <td>
                                            <strong style="font-size: 0.95rem; color: #0f172a;">${c.name}</strong>
                                        </td>
                                        <td>
                                            <span class="badge-city">
                                                <i class="fa-solid fa-location-dot" style="margin-right: 4px; color: #94a3b8;"></i>${c.city}
                                            </span>
                                        </td>
                                        <td>
                                            <span style="color: #475569; font-size: 0.88rem;">${c.address}</span>
                                        </td>
                                        <td>
                                            <span style="font-family: ui-monospace, monospace; font-size: 0.88rem; color: #334155; font-weight: 600;">
                                                <i class="fa-solid fa-phone" style="font-size: 0.75rem; color: #94a3b8; margin-right: 4px;"></i>${c.phone}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge-rooms">
                                                <i class="fa-solid fa-door-open"></i> ${c.totalRooms} phòng
                                            </span>
                                        </td>
                                        <td>
                                            <span class="status-indicator ${c.isActive ? 'status-active' : 'status-inactive'}">
                                                ${c.isActive ? 'Đang vận hành' : 'Tạm đóng cửa'}
                                            </span>
                                        </td>
                                        <td style="text-align: right;">
                                            <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms?cinemaId=${c.id}" class="action-link">
                                                <span>Xem phòng</span>
                                                <i class="fa-solid fa-arrow-right"></i>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
