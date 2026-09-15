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
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../common/navbar.jsp" />
            <div class="admin-content">
                <div class="admin-card" style="margin-bottom: 25px;">
                    <h2>Xin chào, ${sessionScope.currentUser.fullName}!</h2>
                    <p style="color: #718096; margin-top: 5px;">
                        Chào mừng bạn đến với Hệ thống Quản trị Cụm rạp Chi nhánh Toàn quốc (SWP391 - Nhóm 3).
                        Vai trò hiện tại: <strong style="color: #e50914;">${sessionScope.currentUser.role.roleName}</strong>
                    </p>
                    <div class="quick-actions">
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/branches" class="btn-action">
                            🏢 Quản lý Cụm Rạp
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/operation/pos" class="btn-action btn-secondary-action">
                            🖥️ Mở Quầy Bán Vé (POS)
                        </a>
                        <a href="${pageContext.request.contextPath}/admin/operation/scanner" class="btn-action btn-secondary-action">
                            📱 Mở Soát Vé (QR Scanner)
                        </a>
                    </div>
                </div>

                <!-- Thẻ thống kê chỉ số nhanh -->
                <div class="stats-grid">
                    <div class="stat-card">
                        <div class="stat-icon">🏢</div>
                        <div class="stat-info">
                            <h3>${totalBranches}</h3>
                            <p>Cụm rạp chi nhánh</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">🎥</div>
                        <div class="stat-info">
                            <h3>4</h3>
                            <p>Phòng chiếu hoạt động</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">💺</div>
                        <div class="stat-info">
                            <h3>400</h3>
                            <p>Tổng số ghế ngồi chuẩn</p>
                        </div>
                    </div>
                    <div class="stat-card">
                        <div class="stat-icon">🎞️</div>
                        <div class="stat-info">
                            <h3>5</h3>
                            <p>Phim đang chiếu & sắp chiếu</p>
                        </div>
                    </div>
                </div>

                <!-- Danh sách cụm rạp tóm tắt -->
                <div class="admin-card">
                    <h3>Cụm Rạp Đang Hoạt Động (Zone 1)</h3>
                    <table class="order-summary-table" style="margin-top: 15px;">
                        <thead>
                            <tr>
                                <th>Mã Chi Nhánh</th>
                                <th>Tên Cụm Rạp</th>
                                <th>Thành Phố</th>
                                <th>Địa Chỉ</th>
                                <th>Hotline</th>
                                <th>Số Phòng</th>
                                <th>Trạng Thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${branches}" var="b">
                                <tr>
                                    <td><strong>${b.branchCode}</strong></td>
                                    <td>${b.name}</td>
                                    <td>${b.city}</td>
                                    <td>${b.address}</td>
                                    <td>${b.phone}</td>
                                    <td>${b.totalHalls} phòng</td>
                                    <td><span style="color: #38a169;">● Đang vận hành</span></td>
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
