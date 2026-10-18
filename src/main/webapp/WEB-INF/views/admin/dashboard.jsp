<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bảng Điều Khiển Quản Trị Hệ Thống - Cinema Admin Dashboard</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .kpi-grid-6 {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
            margin-bottom: 24px;
        }
        .kpi-card-luxury {
            background: #fff;
            border-radius: 10px;
            padding: 18px 20px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 2px 4px rgba(0,0,0,0.02);
            display: flex;
            align-items: center;
            gap: 16px;
            transition: all 0.2s ease;
        }
        .kpi-card-luxury:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 12px rgba(0,0,0,0.06);
        }
        .kpi-icon-box {
            width: 48px;
            height: 48px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.3rem;
            flex-shrink: 0;
        }
        .bg-gradient-red { background: linear-gradient(135deg, #e50914, #b20710); color: #fff; }
        .bg-gradient-green { background: linear-gradient(135deg, #10b981, #059669); color: #fff; }
        .bg-gradient-blue { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: #fff; }
        .bg-gradient-purple { background: linear-gradient(135deg, #8b5cf6, #6d28d9); color: #fff; }
        .bg-gradient-amber { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; }
        .bg-gradient-slate { background: linear-gradient(135deg, #475569, #1e293b); color: #fff; }
        .kpi-content h3 {
            margin: 0;
            font-size: 1.35rem;
            font-weight: 800;
            color: #0f172a;
        }
        .kpi-content p {
            margin: 2px 0 0 0;
            font-size: 0.8rem;
            color: #64748b;
            font-weight: 600;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        .progress-bar-container {
            width: 100%;
            background: #e2e8f0;
            border-radius: 10px;
            height: 8px;
            overflow: hidden;
            display: inline-block;
            vertical-align: middle;
        }
        .progress-bar-fill {
            height: 100%;
            background: linear-gradient(90deg, #e50914, #ff6b6b);
            border-radius: 10px;
        }
        .channel-badge-online {
            background: #dbeafe;
            color: #1e40af;
            font-size: 0.75rem;
            padding: 3px 8px;
            border-radius: 4px;
            font-weight: 700;
        }
        .channel-badge-pos {
            background: #f3e8ff;
            color: #6b21a8;
            font-size: 0.75rem;
            padding: 3px 8px;
            border-radius: 4px;
            font-weight: 700;
        }
        .pay-badge-vnpay {
            background: #fee2e2;
            color: #991b1b;
            font-size: 0.75rem;
            padding: 3px 8px;
            border-radius: 4px;
            font-weight: 700;
        }
        .pay-badge-cash {
            background: #dcfce7;
            color: #166534;
            font-size: 0.75rem;
            padding: 3px 8px;
            border-radius: 4px;
            font-weight: 700;
        }
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../common/navbar.jsp" />
            <div class="admin-content">

                <!-- Welcome Banner & Core Quick Actions -->
                <div class="admin-card" style="margin-bottom: 20px;">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 15px;">
                        <div>
                            <h2 style="margin: 0 0 6px 0; font-size: 1.45rem; font-weight: 800; color: #0f172a;">
                                Trung Tâm Điều Hành Quản Trị - CineMax Master Dashboard
                            </h2>
                            <p style="margin: 0; color: #64748b; font-size: 0.92rem;">
                                Hệ sinh thái Cụm rạp Chiếu phim Toàn quốc. Phân hệ: <strong style="color: #e50914;">HẠ TẦNG & BÁO CÁO KINH DOANH</strong>
                            </p>
                        </div>
                        <span class="role-tag" style="padding: 6px 14px; font-size: 0.82rem;">
                            <i class="fa-solid fa-crown" style="margin-right: 5px;"></i> ${not empty sessionScope.currentUser.role.roleName ? sessionScope.currentUser.role.roleName : 'ADMINISTRATOR'}
                        </span>
                    </div>

                    <div class="quick-actions" style="margin-top: 18px;">
                        <c:set var="userRole" value="${sessionScope.currentUser.role.roleName}" />
                        <c:choose>
                            <c:when test="${userRole == 'MANAGER'}">
                                <a href="${pageContext.request.contextPath}/manager/showtimes" class="btn-action">
                                    <i class="fa-solid fa-calendar-days"></i> Quản lý Lịch Chiếu
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms" class="btn-action btn-action-secondary">
                                    <i class="fa-solid fa-video"></i> Quản lý Phòng Chiếu
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/operation/fnb" class="btn-action" style="background: #1e293b; color: #fff;">
                                    <i class="fa-solid fa-boxes-stacked"></i> Tồn Kho Bắp Nước
                                </a>
                            </c:when>
                            <c:otherwise>
                                <a href="${pageContext.request.contextPath}/admin/infrastructure/cinemas" class="btn-action">
                                    <i class="fa-solid fa-building"></i> Quản lý Cụm Rạp
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms" class="btn-action btn-action-secondary">
                                    <i class="fa-solid fa-video"></i> Quản lý Phòng Chiếu
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/infrastructure/seats" class="btn-action btn-action-secondary">
                                    <i class="fa-solid fa-couch"></i> Thiết Kế Sơ Đồ Ghế
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/infrastructure/occupancy" class="btn-action" style="background: #1e293b; color: #fff;">
                                    <i class="fa-solid fa-chart-pie"></i> Báo Cáo Tỷ Lệ Lấp Đầy
                                </a>
                                <a href="${pageContext.request.contextPath}/admin/infrastructure/settings" class="btn-action btn-action-outline">
                                    <i class="fa-solid fa-sliders"></i> Cấu Hình Hệ Thống
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>

                <!-- 6 THẺ KPI TỔNG QUAN HỆ THỐNG (FUNCTION 6 - ITERATION 2) -->
                <div class="kpi-grid-6">
                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box bg-gradient-green">
                            <i class="fa-solid fa-sack-dollar"></i>
                        </div>
                        <div class="kpi-content">
                            <h3><fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/> <small style="font-size: 0.75rem;">đ</small></h3>
                            <p>Tổng Doanh Thu</p>
                        </div>
                    </div>

                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box bg-gradient-red">
                            <i class="fa-solid fa-ticket"></i>
                        </div>
                        <div class="kpi-content">
                            <h3>${totalValidTickets}</h3>
                            <p>Vé Đã Phát Hành</p>
                        </div>
                    </div>

                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box bg-gradient-blue">
                            <i class="fa-solid fa-cart-shopping"></i>
                        </div>
                        <div class="kpi-content">
                            <h3>${totalConfirmedBookings}</h3>
                            <p>Đơn Đặt Vé Hoàn Tất</p>
                        </div>
                    </div>

                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box bg-gradient-purple">
                            <i class="fa-solid fa-chart-line"></i>
                        </div>
                        <div class="kpi-content">
                            <h3>${avgOccupancyRate}%</h3>
                            <p>Lấp Đầy Trung Bình</p>
                        </div>
                    </div>

                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box bg-gradient-amber">
                            <i class="fa-solid fa-building"></i>
                        </div>
                        <div class="kpi-content">
                            <h3>${totalCinemas} <small style="font-size: 0.75rem; color:#64748b;">rạp</small> / ${totalRooms} <small style="font-size: 0.75rem; color:#64748b;">phòng</small></h3>
                            <p>Quy Mô Mạng Lưới</p>
                        </div>
                    </div>

                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box bg-gradient-slate">
                            <i class="fa-solid fa-couch"></i>
                        </div>
                        <div class="kpi-content">
                            <h3>${totalSeats} <small style="font-size: 0.75rem; color:#ef4444;">(${maintenanceSeats} bảo trì)</small></h3>
                            <p>Ghế Ngồi Phục Vụ</p>
                        </div>
                    </div>
                </div>

                <!-- BẢNG 1: HIỆU SUẤT KINH DOANH THEO TỪNG CỤM RẠP CHI NHÁNH -->
                <div class="admin-card" style="margin-bottom: 24px;">
                    <div class="admin-card-header">
                        <h3 class="admin-card-title">
                            <i class="fa-solid fa-chart-column" style="color: #e50914;"></i>
                            Hiệu Suất Kinh Doanh & Đóng Góp Doanh Thu Theo Chi Nhánh
                        </h3>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/occupancy" class="action-link">
                            <span>Xem phân tích lấp đầy</span>
                            <i class="fa-solid fa-arrow-right"></i>
                        </a>
                    </div>

                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="width: 100px;">Mã Rạp</th>
                                    <th>Cụm Rạp Chi Nhánh</th>
                                    <th style="width: 140px;">Thành Phố</th>
                                    <th style="width: 110px; text-align: center;">Quy Mô</th>
                                    <th style="width: 110px; text-align: center;">Suất Chiếu</th>
                                    <th style="width: 110px; text-align: center;">Tổng Đơn</th>
                                    <th style="width: 160px; text-align: right;">Doanh Thu (VNĐ)</th>
                                    <th style="width: 180px;">Tỷ Trọng Đóng Góp</th>
                                    <th style="width: 130px; text-align: right;">Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${branchPerformances}" var="bp">
                                    <tr>
                                        <td><span class="badge-code">${bp.cinemaCode}</span></td>
                                        <td>
                                            <strong style="color: #0f172a; font-size: 0.95rem;">${bp.cinemaName}</strong>
                                        </td>
                                        <td>
                                            <span class="badge-city">
                                                <i class="fa-solid fa-location-dot" style="margin-right: 4px; color: #94a3b8;"></i>${bp.city}
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="badge-rooms"><i class="fa-solid fa-video"></i> ${bp.totalRooms} phòng</span>
                                        </td>
                                        <td style="text-align: center;"><strong>${bp.totalShowtimes}</strong></td>
                                        <td style="text-align: center;">
                                            <span style="font-weight: 700; color: #2563eb;">${bp.totalBookings}</span>
                                        </td>
                                        <td style="text-align: right; font-weight: 800; color: #16a34a; font-size: 0.95rem;">
                                            <fmt:formatNumber value="${bp.totalRevenue}" pattern="#,##0"/> đ
                                        </td>
                                        <td>
                                            <div style="display: flex; align-items: center; gap: 8px;">
                                                <div class="progress-bar-container" style="flex: 1;">
                                                    <div class="progress-bar-fill" style="width: ${bp.revenueSharePercent}%;"></div>
                                                </div>
                                                <span style="font-size: 0.75rem; font-weight: 700; width: 45px; text-align: right;">${bp.revenueSharePercent}%</span>
                                            </div>
                                        </td>
                                        <td style="text-align: right;">
                                            <a href="${pageContext.request.contextPath}/admin/infrastructure/occupancy?cinemaId=${bp.cinemaId}" class="action-link" title="Xem tỷ lệ lấp đầy rạp">
                                                <span>Lấp đầy</span>
                                                <i class="fa-solid fa-chart-pie"></i>
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- BẢNG 2: GIAO DỊCH ĐẶT VÉ & THANH TOÁN GẦN ĐÂY -->
                <div class="admin-card">
                    <div class="admin-card-header">
                        <h3 class="admin-card-title">
                            <i class="fa-solid fa-receipt" style="color: #3b82f6;"></i>
                            Giao Dịch Đặt Vé & Thanh Toán Gần Đây (Real-time Feed)
                        </h3>
                        <span style="font-size: 0.8rem; color: #64748b;">
                            Cập nhật theo dữ liệu bảng <code>bookings</code> & <code>payments</code>
                        </span>
                    </div>

                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="width: 140px;">Mã Đơn</th>
                                    <th>Khách Hàng</th>
                                    <th>Phim Điện Ảnh</th>
                                    <th>Cụm Rạp & Phòng</th>
                                    <th style="width: 100px; text-align: center;">Kênh</th>
                                    <th style="width: 120px; text-align: center;">Phương Thức</th>
                                    <th style="width: 140px; text-align: right;">Thành Tiền</th>
                                    <th style="width: 120px; text-align: center;">Trạng Thái</th>
                                    <th style="width: 150px; text-align: right;">Thời Gian</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty recentTransactions}">
                                        <c:forEach items="${recentTransactions}" var="tx">
                                            <tr>
                                                <td><span class="badge-code" style="background:#e0f2fe; color:#0369a1;">${tx.bookingCode}</span></td>
                                                <td><strong>${tx.customerName}</strong></td>
                                                <td style="color: #0f172a;">${tx.movieTitle}</td>
                                                <td>
                                                    <span style="font-size: 0.85rem; color: #475569;">
                                                        ${tx.cinemaName} - <strong>${tx.roomName}</strong>
                                                    </span>
                                                </td>
                                                <td style="text-align: center;">
                                                    <span class="${tx.channel == 'POS' ? 'channel-badge-pos' : 'channel-badge-online'}">
                                                        <i class="${tx.channel == 'POS' ? 'fa-solid fa-cash-register' : 'fa-solid fa-globe'}"></i> ${tx.channel}
                                                    </span>
                                                </td>
                                                <td style="text-align: center;">
                                                    <span class="${tx.paymentMethod == 'VNPAY' ? 'pay-badge-vnpay' : 'pay-badge-cash'}">
                                                        ${tx.paymentMethod}
                                                    </span>
                                                </td>
                                                <td style="text-align: right; font-weight: 700; color: #0f172a;">
                                                    <fmt:formatNumber value="${tx.finalAmount}" pattern="#,##0"/> đ
                                                </td>
                                                <td style="text-align: center;">
                                                    <span class="status-indicator status-active" style="padding: 2px 8px; font-size: 0.75rem;">
                                                        ${tx.status}
                                                    </span>
                                                </td>
                                                <td style="text-align: right; color: #64748b; font-size: 0.8rem; white-space: nowrap;">
                                                    ${tx.createdAt}
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="9" style="text-align: center; padding: 30px; color: #94a3b8;">
                                                <em>Chưa có giao dịch đặt vé nào được ghi nhận.</em>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>
    </div>
</body>
</html>
