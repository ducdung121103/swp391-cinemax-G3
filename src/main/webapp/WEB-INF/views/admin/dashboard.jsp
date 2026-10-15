<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tổng quan Hệ Thống - Cinema Admin Dashboard</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: #fff;
            padding: 24px;
            border-radius: 10px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.06);
            display: flex;
            align-items: center;
            gap: 16px;
            border-left: 4px solid #e50914;
        }
        .stat-icon {
            font-size: 2.2rem;
            width: 55px;
            height: 55px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 12px;
            background: #f0f4f8;
        }
        .stat-info h3 {
            margin: 0 0 5px 0;
            font-size: 1.8rem;
            color: #1a202c;
        }
        .stat-info p {
            margin: 0;
            color: #718096;
            font-size: 0.9rem;
        }
        .quick-actions {
            display: flex;
            gap: 12px;
            flex-wrap: wrap;
            margin-top: 15px;
        }
        .btn-action {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 10px 18px;
            border-radius: 6px;
            font-weight: 500;
            text-decoration: none;
            color: white;
            background: #e50914;
            transition: background 0.2s;
        }
        .btn-action:hover {
            background: #b20710;
        }
        .btn-secondary-action {
            background: #2d3748;
        }
        .btn-secondary-action:hover {
            background: #1a202c;
        }
        .btn-outline-action {
            background: transparent;
            color: #2d3748;
            border: 1px solid #cbd5e0;
        }
        .btn-outline-action:hover {
            background: #f7fafc;
        }
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../common/navbar.jsp" />
            <div class="admin-content">
                <div class="admin-card" style="margin-bottom: 25px;">
                    <h2>Xin chào, ${sessionScope.currentUser.fullName != null ? sessionScope.currentUser.fullName : 'Quản Trị Viên'}!</h2>
                    <p style="color: #718096; margin-top: 5px;">
                        Chào mừng bạn đến với Hệ thống Quản trị Cụm rạp Chi nhánh Toàn quốc (CineMax - SWP391 Nhóm 3).
                        Vai trò hiện tại: <strong style="color: #e50914;">${sessionScope.currentUser.role.roleName != null ? sessionScope.currentUser.role.roleName : 'ADMIN'}</strong>
                    </p>
                    <div class="quick-actions">
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/cinemas" class="btn-action">
                            🏢 Quản lý Cụm Rạp
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms" class="btn-action btn-secondary-action">
                            🎥 Quản lý Phòng Chiếu
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/seats" class="btn-action btn-secondary-action">
                            💺 Thiết kế Sơ đồ Ghế
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/settings" class="btn-action btn-outline-action">
                            ⚙️ Cấu hình Hệ Thống
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/operation/pos" class="btn-action btn-outline-action">
                            🖥️ Quầy Bán Vé (POS)
                        </a>
                    </div>
                </div>

                <!-- Thẻ thống kê chỉ số thực tế -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon">🏢</div>
                        <div class="stat-info">
                            <h3>${totalCinemas}</h3>
                            <p>Cụm rạp chi nhánh</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">🎥</div>
                        <div class="stat-info">
                            <h3>${totalRooms}</h3>
                            <p>Phòng chiếu hoạt động</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">💺</div>
                        <div class="stat-info">
                            <h3>${totalSeats > 0 ? totalSeats : (totalRooms * 100)}</h3>
                            <p>Ghế ngồi sẵn sàng phục vụ</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">⚙️</div>
                        <div class="stat-info">
                            <h3>9+</h3>
                            <p>Tham số hệ thống toàn cục</p>
                        </div>
                    </div>
                </div>

                <!-- Danh sách cụm rạp tóm tắt -->
                <div class="admin-card">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                        <h3 style="margin: 0;">Danh sách Cụm Rạp Chi Nhánh</h3>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/cinemas" style="color: #e50914; font-size: 0.9rem; text-decoration: none; font-weight: 600;">Xem chi tiết & Quản lý &rarr;</a>
                    </div>
                    <table class="order-summary-table">
                        <thead>
                            <tr>
                                <th>Mã Rạp</th>
                                <th>Tên Cụm Rạp</th>
                                <th>Thành Phố</th>
                                <th>Địa Chỉ</th>
                                <th>Hotline</th>
                                <th>Số Phòng</th>
                                <th>Trạng Thái</th>
                                <th>Hành Động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${cinemas}" var="c">
                                <tr>
                                    <td><strong style="color: #e50914;">${c.cinemaCode}</strong></td>
                                    <td><strong>${c.name}</strong></td>
                                    <td><span style="background: #edf2f7; padding: 3px 8px; border-radius: 4px; font-size: 0.85rem;">${c.city}</span></td>
                                    <td>${c.address}</td>
                                    <td>${c.phone}</td>
                                    <td><strong>${c.totalRooms}</strong> phòng</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.isActive}">
                                                <span style="color: #38a169; font-weight: 600;">● Đang vận hành</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span style="color: #e53e3e; font-weight: 600;">● Tạm đóng cửa</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms?cinemaId=${c.id}" style="color: #3182ce; font-size: 0.85rem; text-decoration: none;">Xem phòng chiếu</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
