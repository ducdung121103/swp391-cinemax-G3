<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Báo Cáo Doanh Thu & Doanh Số - CineMax Executive Reports</title>
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
        .filter-input, .filter-select {
            padding: 8px 12px;
            border: 1px solid #cbd5e1;
            border-radius: 6px;
            font-size: 0.85rem;
            color: #1e293b;
            background: #f8fafc;
            outline: none;
        }
        .filter-input:focus, .filter-select:focus {
            border-color: #e50914;
            background: #fff;
        }
        .kpi-grid-4 {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
            gap: 16px;
            margin-bottom: 20px;
        }
        .kpi-card-luxury {
            background: #fff;
            border-radius: 10px;
            padding: 16px 18px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
            display: flex;
            align-items: center;
            gap: 14px;
        }
        .kpi-icon-box {
            width: 46px;
            height: 46px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.25rem;
            flex-shrink: 0;
        }
        .bg-red { background: linear-gradient(135deg, #e50914, #b20710); color: #fff; }
        .bg-green { background: linear-gradient(135deg, #10b981, #059669); color: #fff; }
        .bg-blue { background: linear-gradient(135deg, #3b82f6, #1d4ed8); color: #fff; }
        .bg-amber { background: linear-gradient(135deg, #f59e0b, #d97706); color: #fff; }
        .bg-purple { background: linear-gradient(135deg, #8b5cf6, #6d28d9); color: #fff; }
        .bg-slate { background: linear-gradient(135deg, #475569, #1e293b); color: #fff; }
        .kpi-content h3 {
            margin: 0;
            font-size: 1.25rem;
            font-weight: 800;
            color: #0f172a;
        }
        .kpi-content p {
            margin: 2px 0 0 0;
            font-size: 0.78rem;
            color: #64748b;
            text-transform: uppercase;
            font-weight: 700;
        }
        .growth-badge {
            font-size: 0.75rem;
            padding: 2px 6px;
            border-radius: 4px;
            font-weight: 700;
            display: inline-flex;
            align-items: center;
            gap: 3px;
            margin-top: 4px;
        }
        .growth-pos { background: #dcfce7; color: #15803d; }
        .growth-neg { background: #fee2e2; color: #b91c1c; }
        .growth-neutral { background: #f1f5f9; color: #64748b; }
        .charts-grid-2 {
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 20px;
            margin-bottom: 24px;
        }
        @media (max-width: 1024px) {
            .charts-grid-2 { grid-template-columns: 1fr; }
        }
        .chart-card {
            background: #fff;
            border-radius: 10px;
            padding: 20px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
        }
        .chart-card h4 {
            margin: 0 0 16px 0;
            font-size: 0.95rem;
            font-weight: 700;
            color: #1e293b;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .tables-grid-2 {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 24px;
        }
        @media (max-width: 900px) {
            .tables-grid-2 { grid-template-columns: 1fr; }
        }
        .btn-export {
            background: #fff;
            color: #0f172a;
            border: 1px solid #cbd5e1;
            padding: 8px 14px;
            border-radius: 6px;
            font-size: 0.82rem;
            font-weight: 600;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            text-decoration: none;
            transition: all 0.2s;
        }
        .btn-export:hover {
            background: #f8fafc;
            border-color: #94a3b8;
        }
        .btn-export.excel:hover {
            color: #107c41;
            border-color: #107c41;
        }
        .btn-export.pdf:hover {
            color: #e50914;
            border-color: #e50914;
        }
        .empty-state {
            padding: 40px;
            text-align: center;
            background: #fff;
            border-radius: 10px;
            border: 1px dashed #cbd5e1;
            color: #64748b;
        }
        .empty-state i {
            font-size: 2.5rem;
            color: #cbd5e1;
            margin-bottom: 12px;
        }
        @media print {
            * {
                -webkit-print-color-adjust: exact !important;
                print-color-adjust: exact !important;
            }
            @page {
                size: A4 portrait;
                margin: 10mm;
            }
            .admin-sidebar, .admin-navbar, .filter-toolbar, .filter-group, .pagination-box, .btn-export, .btn-back-dashboard, .breadcrumb, .btn-sidebar-toggle {
                display: none !important;
            }
            .admin-main { margin: 0 !important; padding: 0 !important; width: 100% !important; }
            .admin-content { padding: 0 !important; }
            .chart-card, .kpi-card-luxury { break-inside: avoid; page-break-inside: avoid; box-shadow: none !important; border: 1px solid #cbd5e1 !important; }
        }
    </style>
</head>
<body class="admin-body">
<div class="admin-wrapper">

    <%-- Include Sidebar --%>
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="admin-main">
        <%-- Top Navbar --%>
        <header class="admin-navbar">
            <div class="navbar-left">
                <button class="btn-sidebar-toggle" id="sidebarToggle"><i class="fa-solid fa-bars"></i></button>
                <div class="breadcrumb">
                    <span>Quản Trị</span> <i class="fa-solid fa-chevron-right"></i>
                    <span>Báo Cáo & Thống Kê</span> <i class="fa-solid fa-chevron-right"></i>
                    <span class="active">Báo Cáo Doanh Thu (M-10.1)</span>
                </div>
            </div>
            <div class="navbar-right">
                <a href="${pageContext.request.contextPath}/admin/dashboard" class="btn-back-dashboard">
                    <i class="fa-solid fa-gauge-high"></i> Dashboard
                </a>
            </div>
        </header>

        <%-- Content Container --%>
        <div class="admin-content">

            <%-- Alerts --%>
            <c:if test="${not empty error}">
                <div class="alert alert-danger" style="background:#fee2e2; color:#b91c1c; padding:12px 16px; border-radius:6px; margin-bottom:16px;">
                    <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${error}" />
                </div>
            </c:if>
            <c:if test="${not empty msg}">
                <div class="alert alert-success" style="background:#dcfce7; color:#15803d; padding:12px 16px; border-radius:6px; margin-bottom:16px;">
                    <i class="fa-solid fa-circle-check"></i> <c:out value="${msg}" />
                </div>
            </c:if>

            <%-- Filter Toolbar --%>
            <div class="filter-toolbar">
                <form action="${pageContext.request.contextPath}/admin/reports/revenue" method="GET" class="filter-group">
                    <div>
                        <label style="font-size:0.75rem; color:#64748b; font-weight:700; display:block; margin-bottom:2px;">TỪ NGÀY</label>
                        <input type="date" name="from" value="${filter.from}" class="filter-input" required />
                    </div>
                    <div>
                        <label style="font-size:0.75rem; color:#64748b; font-weight:700; display:block; margin-bottom:2px;">ĐẾN NGÀY</label>
                        <input type="date" name="to" value="${filter.to}" class="filter-input" required />
                    </div>
                    <div>
                        <label style="font-size:0.75rem; color:#64748b; font-weight:700; display:block; margin-bottom:2px;">CỤM RẠP</label>
                        <select name="cinemaId" class="filter-select">
                            <option value="ALL">-- Tất cả cụm rạp --</option>
                            <c:forEach items="${cinemas}" var="c">
                                <option value="${c.id}" ${filter.cinemaId == c.id ? 'selected' : ''}>
                                    <c:out value="${c.name}" /> (<c:out value="${c.cinemaCode}" />)
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div style="align-self:flex-end;">
                        <button type="submit" class="btn btn-primary" style="padding:8px 16px; border-radius:6px; font-size:0.85rem; font-weight:700; background:#e50914; border:none; color:#fff; cursor:pointer;">
                            <i class="fa-solid fa-filter"></i> Lọc
                        </button>
                        <a href="${pageContext.request.contextPath}/admin/reports/revenue" class="btn btn-secondary" style="padding:8px 12px; border-radius:6px; font-size:0.85rem; background:#f1f5f9; border:1px solid #cbd5e1; color:#475569; text-decoration:none; margin-left:4px;">
                            <i class="fa-solid fa-rotate-left"></i>
                        </a>
                    </div>
                </form>

                <%-- Export Action --%>
                <div class="filter-group" style="align-self:flex-end;">
                    <button type="button" class="btn-export pdf" onclick="window.print()" title="Xuất báo cáo dưới dạng PDF hoặc in trực tiếp" style="background:#1e293b; color:#fff; border:none; cursor:pointer;">
                        <i class="fa-solid fa-print"></i> Xuất Báo Cáo / In PDF
                    </button>
                </div>
            </div>

            <%-- KPI Cards Row: 3 Core Metrics --%>
            <div class="kpi-grid-4" style="grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));">
                <div class="kpi-card-luxury">
                    <div class="kpi-icon-box bg-red"><i class="fa-solid fa-sack-dollar"></i></div>
                    <div class="kpi-content">
                        <h3><fmt:formatNumber value="${kpi.totalRevenue}" pattern="#,##0"/> ₫</h3>
                        <p>Tổng Doanh Thu Toàn Hệ Thống</p>
                    </div>
                </div>

                <div class="kpi-card-luxury">
                    <div class="kpi-icon-box bg-green"><i class="fa-solid fa-receipt"></i></div>
                    <div class="kpi-content">
                        <h3><fmt:formatNumber value="${kpi.totalConfirmedBookings}" pattern="#,##0"/></h3>
                        <p>Số Đơn Đặt Vé Xác Nhận</p>
                    </div>
                </div>

                <div class="kpi-card-luxury">
                    <div class="kpi-icon-box bg-blue"><i class="fa-solid fa-ticket"></i></div>
                    <div class="kpi-content">
                        <h3><fmt:formatNumber value="${kpi.totalTicketsSold}" pattern="#,##0"/></h3>
                        <p>Tổng Số Vé Đã Bán Ra</p>
                    </div>
                </div>
            </div>

            <%-- Tables Section: Top Movies & Branch Performance --%>
            <div class="tables-grid-2">
                <%-- Top Movies --%>
                <div class="chart-card">
                    <h4><span><i class="fa-solid fa-film" style="color:#8b5cf6;"></i> Top Phim Doanh Thu Cao Nhất</span></h4>
                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                            <tr>
                                <th>#</th>
                                <th>Tên Phim</th>
                                <th style="text-align:right;">Doanh Thu</th>
                                <th style="text-align:center;">Tỷ Trọng</th>
                                <th style="text-align:right;">Số Vé</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${byMovie}" var="m" varStatus="loop">
                                <tr>
                                    <td><span class="badge ${loop.index < 3 ? 'badge-primary' : 'badge-outline'}">${loop.index + 1}</span></td>
                                    <td><strong><c:out value="${m.categoryName}" /></strong></td>
                                    <td style="text-align:right; font-weight:700;"><fmt:formatNumber value="${m.revenue}" pattern="#,##0"/> ₫</td>
                                    <td style="text-align:center;">
                                        <div style="display:flex; align-items:center; gap:6px; justify-content:center;">
                                            <div style="width:50px; background:#e2e8f0; height:6px; border-radius:3px; overflow:hidden;">
                                                <div style="width:${m.sharePercent}%; background:#8b5cf6; height:100%;"></div>
                                            </div>
                                            <span style="font-size:0.75rem;">${m.sharePercent}%</span>
                                        </div>
                                    </td>
                                    <td style="text-align:right;"><fmt:formatNumber value="${m.ticketsSold}" pattern="#,##0"/></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty byMovie}">
                                <tr><td colspan="5" style="text-align:center; color:#94a3b8; padding:20px;">Không có dữ liệu phim</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

                <%-- Cinema Breakdown --%>
                <div class="chart-card">
                    <h4><span><i class="fa-solid fa-building" style="color:#10b981;"></i> Doanh Thu Theo Cụm Rạp Chi Nhánh</span></h4>
                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                            <tr>
                                <th>Cụm Rạp</th>
                                <th style="text-align:right;">Doanh Thu</th>
                                <th style="text-align:center;">Tỷ Trọng</th>
                                <th style="text-align:right;">Số Đơn</th>
                                <th style="text-align:right;">Số Vé</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${byCinema}" var="c">
                                <tr>
                                    <td><strong><c:out value="${c.categoryName}" /></strong></td>
                                    <td style="text-align:right; font-weight:700;"><fmt:formatNumber value="${c.revenue}" pattern="#,##0"/> ₫</td>
                                    <td style="text-align:center;">
                                        <div style="display:flex; align-items:center; gap:6px; justify-content:center;">
                                            <div style="width:50px; background:#e2e8f0; height:6px; border-radius:3px; overflow:hidden;">
                                                <div style="width:${c.sharePercent}%; background:#10b981; height:100%;"></div>
                                            </div>
                                            <span style="font-size:0.75rem;">${c.sharePercent}%</span>
                                        </div>
                                    </td>
                                    <td style="text-align:right;"><fmt:formatNumber value="${c.bookingsCount}" pattern="#,##0"/></td>
                                    <td style="text-align:right;"><fmt:formatNumber value="${c.ticketsSold}" pattern="#,##0"/></td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty byCinema}">
                                <tr><td colspan="5" style="text-align:center; color:#94a3b8; padding:20px;">Không có dữ liệu cụm rạp</td></tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <%-- Drill-Down Detail Table --%>
            <div class="chart-card">
                <h4>
                    <span><i class="fa-solid fa-list-check" style="color:#e50914;"></i> Chi Tiết Giao Dịch Đặt Vé & Thanh Toán (${bookingsPaged.totalElements} bản ghi)</span>
                </h4>
                <div class="table-responsive">
                    <table class="admin-table">
                        <thead>
                        <tr>
                            <th>Mã Đơn</th>
                            <th>Thời Gian</th>
                            <th>Phim</th>
                            <th>Cụm Rạp</th>
                            <th>Kênh</th>
                            <th>Phương Thức</th>
                            <th style="text-align:right;">Tiền Vé</th>
                            <th style="text-align:right;">Tiền F&B</th>
                            <th style="text-align:right;">Giảm Giá</th>
                            <th style="text-align:right;">Tổng Tiền</th>
                            <th>Trạng Thái</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${bookingsPaged.content}" var="b">
                            <tr>
                                <td><code><c:out value="${b.bookingCode}" /></code></td>
                                <td><fmt:parseDate value="${b.paymentTime}" pattern="yyyy-MM-dd'T'HH:mm" var="parsedDate" type="both" /><fmt:formatDate value="${parsedDate}" pattern="dd/MM/yyyy HH:mm" /></td>
                                <td><c:out value="${b.movieTitle}" /></td>
                                <td><c:out value="${b.cinemaName}" /></td>
                                <td>
                                    <span class="badge ${b.channel == 'ONLINE' ? 'badge-primary' : 'badge-warning'}">
                                        <c:out value="${b.channel}" />
                                    </span>
                                </td>
                                <td><span class="badge badge-outline"><c:out value="${b.paymentMethod}" /></span></td>
                                <td style="text-align:right;"><fmt:formatNumber value="${b.ticketsAmount}" pattern="#,##0"/> ₫</td>
                                <td style="text-align:right;"><fmt:formatNumber value="${b.fnbAmount}" pattern="#,##0"/> ₫</td>
                                <td style="text-align:right; color:#b91c1c;">-<fmt:formatNumber value="${b.discountAmount}" pattern="#,##0"/> ₫</td>
                                <td style="text-align:right; font-weight:700;"><fmt:formatNumber value="${b.finalAmount}" pattern="#,##0"/> ₫</td>
                                <td><span class="badge badge-success"><c:out value="${b.status}" /></span></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty bookingsPaged.content}">
                            <tr><td colspan="11" style="text-align:center; color:#94a3b8; padding:30px;">Không tìm thấy giao dịch nào phù hợp với bộ lọc</td></tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>

                <%-- Pagination --%>
                <c:if test="${bookingsPaged.totalPages > 1}">
                    <div style="display:flex; justify-content:space-between; align-items:center; margin-top:16px; padding-top:16px; border-top:1px solid #e2e8f0;">
                        <span style="font-size:0.85rem; color:#64748b;">
                            Trang <strong>${bookingsPaged.pageNumber}</strong> / <strong>${bookingsPaged.totalPages}</strong>
                        </span>
                        <div style="display:flex; gap:6px;">
                            <c:if test="${bookingsPaged.pageNumber > 1}">
                                <a href="?from=${filter.from}&to=${filter.to}&cinemaId=${filter.cinemaId}&movieId=${filter.movieId}&channel=${filter.channel}&groupBy=${filter.groupBy}&page=${bookingsPaged.pageNumber - 1}" class="btn-export">
                                    <i class="fa-solid fa-chevron-left"></i> Trước
                                </a>
                            </c:if>
                            <c:if test="${bookingsPaged.pageNumber < bookingsPaged.totalPages}">
                                <a href="?from=${filter.from}&to=${filter.to}&cinemaId=${filter.cinemaId}&movieId=${filter.movieId}&channel=${filter.channel}&groupBy=${filter.groupBy}&page=${bookingsPaged.pageNumber + 1}" class="btn-export">
                                    Sau <i class="fa-solid fa-chevron-right"></i>
                                </a>
                            </c:if>
                        </div>
                    </div>
                </c:if>
            </div>

        </div>
    </div>
</div>

<%-- Script Render Chart.js --%>
</body>
</html>
