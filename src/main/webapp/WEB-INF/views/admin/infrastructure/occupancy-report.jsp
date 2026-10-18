<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo Cáo Tỷ Lệ Lấp Đầy Phòng Chiếu & Suất Chiếu - CineMax Analytics</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .filter-toolbar {
            background: #fff;
            padding: 16px 20px;
            border-radius: 8px;
            border: 1px solid #e2e8f0;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 12px;
            margin-bottom: 20px;
        }
        .filter-group {
            display: flex;
            align-items: center;
            gap: 10px;
            flex-wrap: wrap;
        }
        .filter-select {
            padding: 8px 12px;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font-size: 0.85rem;
            color: #1e293b;
            background: #f8fafc;
            outline: none;
        }
        .kpi-row-4 {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 16px;
            margin-bottom: 24px;
        }
        .kpi-stat-card {
            background: #fff;
            border-radius: 8px;
            padding: 16px 20px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
        }
        .kpi-stat-card h4 {
            margin: 0;
            font-size: 0.8rem;
            text-transform: uppercase;
            color: #64748b;
            font-weight: 700;
            letter-spacing: 0.5px;
        }
        .kpi-stat-card .kpi-value {
            margin: 8px 0 0 0;
            font-size: 1.55rem;
            font-weight: 800;
            color: #0f172a;
        }
        .progress-bar-smart {
            width: 100%;
            height: 8px;
            background: #e2e8f0;
            border-radius: 10px;
            overflow: hidden;
            display: inline-block;
            vertical-align: middle;
        }
        .bar-green { background: #10b981; }
        .bar-orange { background: #f59e0b; }
        .bar-red { background: #ef4444; }

        .time-badge-prime {
            background: #fef3c7;
            color: #b45309;
            font-size: 0.75rem;
            padding: 3px 8px;
            border-radius: 4px;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .time-badge-offpeak {
            background: #e0f2fe;
            color: #0369a1;
            font-size: 0.75rem;
            padding: 3px 8px;
            border-radius: 4px;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }

        @media print {
            .admin-sidebar, .admin-navbar, .filter-toolbar, .btn-print, .action-link {
                display: none !important;
            }
            .admin-main {
                margin: 0 !important;
                padding: 0 !important;
            }
        }
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../../common/navbar.jsp" />
            <div class="admin-content">

                <!-- Header Tiêu Đề -->
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; flex-wrap: wrap; gap: 15px;">
                    <div>
                        <h2 style="margin: 0 0 4px 0; font-size: 1.45rem; font-weight: 800; color: #0f172a; display: flex; align-items: center; gap: 10px;">
                            <i class="fa-solid fa-chart-pie" style="color: #e50914;"></i>
                            Báo Cáo Tỷ Lệ Lấp Đầy Phòng Chiếu & Suất Chiếu (Branch Occupancy)
                        </h2>
                        <p style="margin: 0; color: #64748b; font-size: 0.9rem;">
                            Phân tích chi tiết hiệu suất khai thác phòng và ghế ngồi theo chi nhánh (SRS: M-10.3).
                        </p>
                    </div>
                    <div style="display: flex; gap: 10px;">
                        <button type="button" class="btn-action btn-print" onclick="window.print()" style="background: #1e293b; color: #fff;">
                            <i class="fa-solid fa-print"></i> Xuất Báo Cáo / In PDF
                        </button>
                    </div>
                </div>

                <!-- THANH BỘ LỌC ĐA CHIỀU (MULTI-DIMENSIONAL FILTER TOOLBAR) -->
                <form method="GET" action="${pageContext.request.contextPath}/admin/infrastructure/occupancy" class="filter-toolbar" id="filterForm">
                    <div class="filter-group">
                        <label style="font-size: 0.85rem; font-weight: 600; color: #475569;">
                            <i class="fa-solid fa-filter"></i> Bộ Lọc:
                        </label>

                        <!-- Lọc Cụm Rạp -->
                        <select name="cinemaId" class="filter-select" onchange="document.getElementById('filterForm').submit()">
                            <option value="ALL">-- Tất cả cụm rạp chi nhánh --</option>
                            <c:forEach items="${cinemas}" var="c">
                                <option value="${c.id}" ${selectedCinemaId == c.id ? 'selected' : ''}>${c.name} (${c.city})</option>
                            </c:forEach>
                        </select>

                        <!-- Lọc Phòng Chiếu -->
                        <select name="roomId" class="filter-select" onchange="document.getElementById('filterForm').submit()">
                            <option value="ALL">-- Tất cả phòng chiếu --</option>
                            <c:forEach items="${rooms}" var="r">
                                <option value="${r.id}" ${selectedRoomId == r.id ? 'selected' : ''}>${r.name} (${r.roomType})</option>
                            </c:forEach>
                        </select>

                        <!-- Lọc Định Dạng Phòng -->
                        <select name="roomType" class="filter-select" onchange="document.getElementById('filterForm').submit()">
                            <option value="ALL" ${selectedRoomType == 'ALL' ? 'selected' : ''}>-- Định dạng phòng --</option>
                            <option value="STANDARD_2D" ${selectedRoomType == 'STANDARD_2D' ? 'selected' : ''}>Standard 2D</option>
                            <option value="VIP_2D" ${selectedRoomType == 'VIP_2D' ? 'selected' : ''}>VIP 2D</option>
                            <option value="IMAX_3D" ${selectedRoomType == 'IMAX_3D' ? 'selected' : ''}>IMAX 3D</option>
                            <option value="4DX" ${selectedRoomType == '4DX' ? 'selected' : ''}>4DX</option>
                        </select>

                        <!-- Lọc Khung Giờ Chiếu -->
                        <select name="timeSlot" class="filter-select" onchange="document.getElementById('filterForm').submit()">
                            <option value="ALL" ${selectedTimeSlot == 'ALL' ? 'selected' : ''}>-- Khung giờ chiếu --</option>
                            <option value="PRIME_TIME" ${selectedTimeSlot == 'PRIME_TIME' ? 'selected' : ''}>🔥 Giờ Vàng (18:00 - 23:00)</option>
                            <option value="OFF_PEAK" ${selectedTimeSlot == 'OFF_PEAK' ? 'selected' : ''}>☀️ Giờ Thường (Trước 18:00)</option>
                        </select>
                    </div>

                    <div>
                        <a href="${pageContext.request.contextPath}/admin/infrastructure/occupancy" class="action-link" style="color: #64748b; font-size: 0.85rem;">
                            <i class="fa-solid fa-arrows-rotate"></i> Đặt lại bộ lọc
                        </a>
                    </div>
                </form>

                <!-- 4 KPI CARDS ĐẦU TRANG -->
                <div class="kpi-row-4">
                    <div class="kpi-stat-card">
                        <h4>Lấp Đầy Trung Bình Toàn Chuỗi</h4>
                        <div class="kpi-value" style="color: #2563eb;">${overallOccupancyRate}%</div>
                        <small style="color: #64748b; font-size: 0.75rem;">Trên tổng số suất chiếu đã kích hoạt</small>
                    </div>

                    <div class="kpi-stat-card">
                        <h4>Hiệu Suất Khung Giờ Vàng</h4>
                        <div class="kpi-value" style="color: #d97706;">${primeTimeOccupancyRate}%</div>
                        <small style="color: #64748b; font-size: 0.75rem;">18:00 - 23:00 hàng ngày (Prime Time)</small>
                    </div>

                    <div class="kpi-stat-card">
                        <h4>Ghế Đã Bán / Ghế Cung Ứng</h4>
                        <div class="kpi-value" style="color: #16a34a;">
                            <fmt:formatNumber value="${totalSoldSeats}" pattern="#,##0"/> 
                            <small style="font-size: 0.9rem; color: #64748b;">/ <fmt:formatNumber value="${totalSeatSupply}" pattern="#,##0"/></small>
                        </div>
                        <small style="color: #64748b; font-size: 0.75rem;">Tổng số vé giao dịch thành công</small>
                    </div>

                    <div class="kpi-stat-card">
                        <h4>Tổng Suất Chiếu Phân Tích</h4>
                        <div class="kpi-value" style="color: #7c3aed;">${totalShowtimesCount} <small style="font-size: 0.85rem; color: #64748b;">suất</small></div>
                        <small style="color: #64748b; font-size: 0.75rem;">Dữ liệu từ bảng <code>showtimes</code></small>
                    </div>
                </div>

                <!-- BẢNG 1: XẾP HẠNG TỶ LỆ LẤP ĐẦY THEO PHÒNG CHIẾU (HALL OCCUPANCY RANKING) -->
                <div class="admin-card" style="margin-bottom: 24px;">
                    <div class="admin-card-header">
                        <h3 class="admin-card-title">
                            <i class="fa-solid fa-door-open" style="color: #e50914;"></i>
                            Bảng 1: Hiệu Suất Tỷ Lệ Lấp Đầy Theo Phòng Chiếu & Chi Nhánh
                        </h3>
                        <span style="font-size: 0.8rem; color: #64748b;">
                            Thống kê theo bảng <code>screening_rooms</code> JOIN <code>cinemas</code>
                        </span>
                    </div>

                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th>Cụm Rạp Chi Nhánh</th>
                                    <th>Phòng Chiếu</th>
                                    <th style="width: 130px;">Định Dạng</th>
                                    <th style="width: 100px; text-align: center;">Sức Chứa</th>
                                    <th style="width: 110px; text-align: center;">Bảo Trì</th>
                                    <th style="width: 110px; text-align: center;">Suất Chiếu</th>
                                    <th style="width: 110px; text-align: center;">Vé Đã Bán</th>
                                    <th style="width: 180px;">Tỷ Lệ Lấp Đầy</th>
                                    <th style="width: 150px; text-align: right;">Doanh Thu Vé</th>
                                    <th style="width: 110px; text-align: right;">Sơ Đồ Ghế</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty roomSummaries}">
                                        <c:forEach items="${roomSummaries}" var="rs">
                                            <tr>
                                                <td><strong>${rs.cinemaName}</strong></td>
                                                <td><span style="font-weight: 700; color: #0f172a;">${rs.roomName}</span></td>
                                                <td><span class="badge-code">${rs.roomType}</span></td>
                                                <td style="text-align: center;">${rs.totalCapacity} ghế</td>
                                                <td style="text-align: center;">
                                                    <c:choose>
                                                        <c:when test="${rs.maintenanceSeatsCount > 0}">
                                                            <span style="background: #fee2e2; color: #991b1b; padding: 2px 8px; border-radius: 4px; font-weight: 700; font-size: 0.75rem;">
                                                                ${rs.maintenanceSeatsCount} ghế
                                                            </span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span style="color: #10b981; font-size: 0.75rem;"><i class="fa-solid fa-circle-check"></i> 0</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: center; font-weight: 600;">${rs.totalShowtimes}</td>
                                                <td style="text-align: center; font-weight: 700; color: #2563eb;">${rs.totalSoldTickets}</td>
                                                <td>
                                                    <div style="display: flex; align-items: center; gap: 8px;">
                                                        <div class="progress-bar-smart" style="flex: 1;">
                                                            <div class="${rs.occupancyRate >= 60.0 ? 'bar-green' : (rs.occupancyRate >= 30.0 ? 'bar-orange' : 'bar-red')}"
                                                                 style="width: ${rs.occupancyRate}%; height: 100%; border-radius: 10px;"></div>
                                                        </div>
                                                        <span style="font-size: 0.8rem; font-weight: 700; width: 45px; text-align: right;">${rs.occupancyRate}%</span>
                                                    </div>
                                                </td>
                                                <td style="text-align: right; font-weight: 700; color: #16a34a;">
                                                    <fmt:formatNumber value="${rs.roomTicketRevenue}" pattern="#,##0"/> đ
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${pageContext.request.contextPath}/admin/infrastructure/seats?roomId=${rs.roomId}" class="action-link" title="Xem sơ đồ ghế">
                                                        <span>Ghế</span>
                                                        <i class="fa-solid fa-couch"></i>
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="10" style="text-align: center; padding: 30px; color: #94a3b8;">
                                                <em>Không tìm thấy dữ liệu phòng chiếu nào phù hợp với bộ lọc.</em>
                                            </td>
                                        </tr>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- BẢNG 2: CHI TIẾT TỶ LỆ LẤP ĐẦY THEO TỪNG SUẤT CHIẾU (SHOWTIME DETAILS) -->
                <div class="admin-card">
                    <div class="admin-card-header">
                        <h3 class="admin-card-title">
                            <i class="fa-solid fa-clock" style="color: #2563eb;"></i>
                            Bảng 2: Chi Tiết Tỷ Lệ Lấp Đầy Từng Suất Chiếu & Khung Giờ (Showtime Feed)
                        </h3>
                        <span style="font-size: 0.8rem; color: #64748b;">
                            Thống kê theo bảng <code>showtimes</code> JOIN <code>movies</code>, <code>tickets</code>
                        </span>
                    </div>

                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="width: 70px;">Suất #</th>
                                    <th>Phim Điện Ảnh</th>
                                    <th>Cụm Rạp & Phòng</th>
                                    <th style="width: 140px;">Giờ Chiếu</th>
                                    <th style="width: 130px; text-align: center;">Khung Giờ</th>
                                    <th style="width: 100px; text-align: center;">Sức Chứa</th>
                                    <th style="width: 100px; text-align: center;">Đã Bán</th>
                                    <th style="width: 90px; text-align: center;">Còn Trống</th>
                                    <th style="width: 170px;">Tỷ Lệ Lấp Đầy</th>
                                    <th style="width: 140px; text-align: right;">Doanh Thu Vé</th>
                                    <th style="width: 90px; text-align: right;">Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${not empty showtimeDetails}">
                                        <c:forEach items="${showtimeDetails}" var="st">
                                            <tr>
                                                <td><span class="badge-code">#${st.showtimeId}</span></td>
                                                <td>
                                                    <div style="display: flex; align-items: center; gap: 10px;">
                                                        <c:if test="${not empty st.posterUrl}">
                                                            <img src="${st.posterUrl}" alt="" style="width: 32px; height: 46px; object-fit: cover; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.2);">
                                                        </c:if>
                                                        <div>
                                                            <strong style="color: #0f172a; font-size: 0.9rem;">${st.movieTitle}</strong>
                                                            <div style="font-size: 0.75rem; color: #64748b;">Định dạng: <strong>${st.experienceFormat}</strong></div>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div style="font-size: 0.85rem; font-weight: 600; color: #334155;">${st.cinemaName}</div>
                                                    <small style="color: #64748b;">${st.roomName}</small>
                                                </td>
                                                <td style="white-space: nowrap; font-size: 0.85rem;">
                                                    <div><strong>${st.startTime}</strong></div>
                                                    <small style="color: #64748b;">đến ${st.endTime}</small>
                                                </td>
                                                <td style="text-align: center;">
                                                    <c:choose>
                                                        <c:when test="${st.timeSlot == 'PRIME_TIME'}">
                                                            <span class="time-badge-prime"><i class="fa-solid fa-fire"></i> Giờ Vàng</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="time-badge-offpeak"><i class="fa-solid fa-sun"></i> Giờ Thường</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: center;">${st.capacity}</td>
                                                <td style="text-align: center; font-weight: 700; color: #16a34a;">${st.soldSeats}</td>
                                                <td style="text-align: center; color: #64748b;">${st.availableSeats}</td>
                                                <td>
                                                    <div style="display: flex; align-items: center; gap: 8px;">
                                                        <div class="progress-bar-smart" style="flex: 1;">
                                                            <div class="${st.occupancyRate >= 60.0 ? 'bar-green' : (st.occupancyRate >= 30.0 ? 'bar-orange' : 'bar-red')}"
                                                                 style="width: ${st.occupancyRate}%; height: 100%; border-radius: 10px;"></div>
                                                        </div>
                                                        <span style="font-size: 0.8rem; font-weight: 700; width: 45px; text-align: right;">${st.occupancyRate}%</span>
                                                    </div>
                                                </td>
                                                <td style="text-align: right; font-weight: 700; color: #0f172a;">
                                                    <fmt:formatNumber value="${st.revenue}" pattern="#,##0"/> đ
                                                </td>
                                                <td style="text-align: right;">
                                                    <a href="${pageContext.request.contextPath}/admin/infrastructure/seats?roomId=${st.roomId}" class="action-link" title="Xem sơ đồ ghế phòng">
                                                        <i class="fa-solid fa-arrow-up-right-from-square"></i>
                                                    </a>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:when>
                                    <c:otherwise>
                                        <tr>
                                            <td colspan="11" style="text-align: center; padding: 30px; color: #94a3b8;">
                                                <em>Không tìm thấy suất chiếu nào phù hợp với điều kiện lọc hiện tại.</em>
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
