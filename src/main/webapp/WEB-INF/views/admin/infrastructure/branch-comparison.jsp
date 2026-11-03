<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>So Sánh Hiệu Suất Cụm Rạp - CineMax Enterprise BI</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js"></script>
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
            width: 48px;
            height: 48px;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.3rem;
            flex-shrink: 0;
        }
        .kpi-icon-red { background: #fee2e2; color: #dc2626; }
        .kpi-icon-blue { background: #e0f2fe; color: #0284c7; }
        .kpi-icon-green { background: #dcfce7; color: #16a34a; }
        .kpi-icon-amber { background: #fef3c7; color: #d97706; }

        .kpi-title {
            font-size: 0.78rem;
            font-weight: 600;
            color: #64748b;
            text-transform: uppercase;
            letter-spacing: 0.03em;
            margin-bottom: 4px;
        }
        .kpi-value {
            font-size: 1.35rem;
            font-weight: 700;
            color: #0f172a;
            line-height: 1.2;
        }
        .kpi-delta {
            font-size: 0.75rem;
            font-weight: 600;
            margin-top: 4px;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .delta-up { color: #16a34a; }
        .delta-down { color: #dc2626; }

        .card-surface {
            background: #fff;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            padding: 20px;
            margin-bottom: 20px;
        }
        .card-header-flex {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 16px;
        }
        .card-title-lg {
            font-size: 1rem;
            font-weight: 700;
            color: #1e293b;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .btn-export {
            padding: 7px 14px;
            border-radius: 6px;
            font-size: 0.85rem;
            font-weight: 600;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            text-decoration: none;
            transition: all 0.2s;
            cursor: pointer;
        }
        .btn-export-excel {
            background: #107c41;
            color: #fff;
            border: 1px solid #107c41;
        }
        .btn-export-excel:hover { background: #0c6133; }
        .btn-export-pdf {
            background: #b30b00;
            color: #fff;
            border: 1px solid #b30b00;
        }
        .btn-export-pdf:hover { background: #8a0800; }
        .btn-primary-red {
            background: #e50914;
            color: #fff;
            border: none;
            padding: 8px 16px;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
        }
        .btn-primary-red:hover { background: #b80710; }

        .table-custom {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.84rem;
        }
        .table-custom th {
            background: #f8fafc;
            color: #475569;
            font-weight: 600;
            padding: 10px 12px;
            text-align: left;
            border-bottom: 2px solid #e2e8f0;
            white-space: nowrap;
        }
        .table-custom td {
            padding: 10px 12px;
            border-bottom: 1px solid #f1f5f9;
            color: #1e293b;
            vertical-align: middle;
            white-space: nowrap;
        }
        .table-custom tbody tr:hover {
            background-color: #f8fafc;
        }
        .tier-badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 0.72rem;
            font-weight: 700;
            text-transform: uppercase;
        }
        .tier-top { background: #fef08a; color: #854d0e; border: 1px solid #fde047; }
        .tier-above { background: #dcfce7; color: #166534; border: 1px solid #bbf7d0; }
        .tier-below { background: #fed7aa; color: #9a3412; border: 1px solid #fdba74; }
        .tier-under { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }

        .rank-circle {
            width: 26px;
            height: 26px;
            border-radius: 50%;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 0.78rem;
        }
        .rank-1 { background: #fef08a; color: #854d0e; }
        .rank-2 { background: #e2e8f0; color: #334155; }
        .rank-3 { background: #fed7aa; color: #9a3412; }
        .rank-other { background: #f1f5f9; color: #64748b; }

        @media print {
            .app-sidebar, .app-header, .filter-toolbar, .btn-export, .export-group {
                display: none !important;
            }
            .app-main { margin-left: 0 !important; padding: 0 !important; }
            .card-surface { border: none !important; box-shadow: none !important; }
        }
    </style>
</head>
<body class="admin-body">
    <div class="app-layout">
        <jsp:include page="/WEB-INF/views/common/sidebar.jsp"/>

        <main class="app-main">
            <jsp:include page="/WEB-INF/views/common/header.jsp"/>

            <div class="app-content">
                <!-- Breadcrumbs -->
                <div class="content-header" style="margin-bottom: 16px;">
                    <div class="breadcrumb" style="font-size: 0.85rem; color: #64748b; margin-bottom: 4px;">
                        <span>Trang chủ</span> &gt;
                        <span>Báo cáo & Thống kê</span> &gt;
                        <span style="color: #0f172a; font-weight: 600;">So Sánh Hiệu Suất Cụm Rạp</span>
                    </div>
                    <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px;">
                        <div>
                            <h1 style="font-size: 1.45rem; font-weight: 700; color: #0f172a; margin: 0;">
                                <i class="fa-solid fa-code-compare" style="color: #e50914; margin-right: 8px;"></i>So Sánh Hiệu Suất Cụm Rạp
                            </h1>
                            <p style="font-size: 0.85rem; color: #64748b; margin: 4px 0 0 0;">
                                Phân tích đa chiều doanh thu, tỷ lệ lấp đầy, xếp hạng và phân tầng hiệu suất toàn chuỗi CineMax (M-10.2).
                            </p>
                        </div>
                        <div class="export-group" style="display: flex; gap: 8px;">
                            <a href="${pageContext.request.contextPath}/admin/reports/comparison/export?format=xlsx&from=${filter.from}&to=${filter.to}&compareMode=${filter.compareMode}&city=${filter.city}&includeInactive=${filter.includeInactive}"
                               class="btn-export btn-export-excel">
                                <i class="fa-solid fa-file-excel"></i> Xuất Excel
                            </a>
                            <a href="${pageContext.request.contextPath}/admin/reports/comparison/export?format=pdf&from=${filter.from}&to=${filter.to}&compareMode=${filter.compareMode}&city=${filter.city}&includeInactive=${filter.includeInactive}"
                               class="btn-export btn-export-pdf">
                                <i class="fa-solid fa-file-pdf"></i> Xuất PDF
                            </a>
                        </div>
                    </div>
                </div>

                <!-- Filter Toolbar -->
                <div class="filter-toolbar">
                    <form action="${pageContext.request.contextPath}/admin/reports/comparison" method="get" class="filter-group" style="margin: 0; width: 100%;">
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <label style="font-size: 0.82rem; font-weight: 600; color: #475569;">Từ ngày:</label>
                            <input type="date" name="from" value="${filter.from}" class="filter-input" required>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <label style="font-size: 0.82rem; font-weight: 600; color: #475569;">Đến ngày:</label>
                            <input type="date" name="to" value="${filter.to}" class="filter-input" required>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <label style="font-size: 0.82rem; font-weight: 600; color: #475569;">So sánh với:</label>
                            <select name="compareMode" class="filter-select">
                                <option value="PREVIOUS_PERIOD" ${filter.compareMode == 'PREVIOUS_PERIOD' ? 'selected' : ''}>Kỳ liền trước (MoM/DoD)</option>
                                <option value="SAME_PERIOD_LAST_YEAR" ${filter.compareMode == 'SAME_PERIOD_LAST_YEAR' ? 'selected' : ''}>Cùng kỳ năm trước (YoY)</option>
                            </select>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px;">
                            <label style="font-size: 0.82rem; font-weight: 600; color: #475569;">Thành phố:</label>
                            <select name="city" class="filter-select">
                                <option value="">Tất cả thành phố</option>
                                <c:forEach var="c" items="${cities}">
                                    <option value="${c}" ${filter.city == c ? 'selected' : ''}><c:out value="${c}"/></option>
                                </c:forEach>
                            </select>
                        </div>
                        <div style="display: flex; align-items: center; gap: 6px; margin-left: 6px;">
                            <input type="checkbox" id="includeInactive" name="includeInactive" value="true" ${filter.includeInactive ? 'checked' : ''}>
                            <label for="includeInactive" style="font-size: 0.82rem; color: #475569; cursor: pointer;">Hiện rạp tạm ngưng</label>
                        </div>
                        <button type="submit" class="btn-primary-red" style="margin-left: auto;">
                            <i class="fa-solid fa-filter" style="margin-right: 4px;"></i> Áp dụng
                        </button>
                    </form>
                </div>

                <!-- 4 Luxury Chain KPI Cards -->
                <div class="kpi-grid-4">
                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box kpi-icon-red">
                            <i class="fa-solid fa-coins"></i>
                        </div>
                        <div>
                            <div class="kpi-title">Tổng Doanh Thu Chuỗi</div>
                            <div class="kpi-value"><fmt:formatNumber value="${summary.totalRevenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
                            <div class="kpi-delta ${summary.chainGrowthPercent >= 0 ? 'delta-up' : 'delta-down'}">
                                <i class="fa-solid ${summary.chainGrowthPercent >= 0 ? 'fa-arrow-trend-up' : 'fa-arrow-trend-down'}"></i>
                                <fmt:formatNumber value="${summary.chainGrowthPercent}" maxFractionDigits="2"/>% vs kỳ trước
                            </div>
                        </div>
                    </div>

                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box kpi-icon-blue">
                            <i class="fa-solid fa-building"></i>
                        </div>
                        <div>
                            <div class="kpi-title">Doanh Thu TB / Rạp</div>
                            <div class="kpi-value"><fmt:formatNumber value="${summary.chainAverageRevenuePerCinema}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></div>
                            <div class="kpi-delta" style="color: #64748b;">
                                <i class="fa-solid fa-store"></i> ${summary.activeCinemasCount} / ${summary.totalCinemasCount} cụm rạp hoạt động
                            </div>
                        </div>
                    </div>

                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box kpi-icon-green">
                            <i class="fa-solid fa-chair"></i>
                        </div>
                        <div>
                            <div class="kpi-title">Tỷ Lệ Lấp Đầy TB Chuỗi</div>
                            <div class="kpi-value"><fmt:formatNumber value="${summary.chainAverageOccupancyRate}" maxFractionDigits="1"/>%</div>
                            <div class="kpi-delta" style="color: #64748b;">
                                <i class="fa-solid fa-ticket"></i> <fmt:formatNumber value="${summary.totalTicketsSold}" type="number"/> vé đã bán
                            </div>
                        </div>
                    </div>

                    <div class="kpi-card-luxury">
                        <div class="kpi-icon-box kpi-icon-amber">
                            <i class="fa-solid fa-trophy"></i>
                        </div>
                        <div>
                            <div class="kpi-title">Cụm Rạp Dẫn Đầu</div>
                            <div class="kpi-value" style="font-size: 1.15rem;"><c:out value="${summary.bestPerformerName}"/></div>
                            <div class="kpi-delta" style="color: #854d0e;">
                                <i class="fa-solid fa-arrow-down" style="color: #dc2626;"></i> Thấp nhất: <c:out value="${summary.worstPerformerName}"/>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Comparison Visual Charts -->
                <div style="display: grid; grid-template-columns: 2fr 1fr; gap: 16px; margin-bottom: 20px;">
                    <div class="card-surface" style="margin-bottom: 0;">
                        <div class="card-header-flex">
                            <div class="card-title-lg">
                                <i class="fa-solid fa-chart-column" style="color: #e50914;"></i> So Sánh Doanh Thu Theo Rạp (Kỳ Này vs Kỳ Trước)
                            </div>
                        </div>
                        <div style="height: 280px; position: relative;">
                            <canvas id="branchComparisonChart"></canvas>
                        </div>
                    </div>

                    <div class="card-surface" style="margin-bottom: 0;">
                        <div class="card-header-flex">
                            <div class="card-title-lg">
                                <i class="fa-solid fa-chart-pie" style="color: #0284c7;"></i> Thị Phần Doanh Thu Toàn Chuỗi
                            </div>
                        </div>
                        <div style="height: 280px; position: relative;">
                            <canvas id="marketShareChart"></canvas>
                        </div>
                    </div>
                </div>

                <!-- Enterprise Multi-Branch Comparison Table -->
                <div class="card-surface">
                    <div class="card-header-flex">
                        <div class="card-title-lg">
                            <i class="fa-solid fa-list-ol" style="color: #107c41;"></i> Bảng Xếp Hạng & Chỉ Số Chi Tiết Từng Cụm Rạp
                        </div>
                        <span style="font-size: 0.8rem; color: #64748b;">
                            Hiển thị <strong>${metrics.size()}</strong> cụm rạp
                        </span>
                    </div>

                    <div style="overflow-x: auto;">
                        <table class="table-custom">
                            <thead>
                                <tr>
                                    <th style="text-align: center; width: 45px;">Hạng</th>
                                    <th>Mã Rạp</th>
                                    <th>Tên Cụm Rạp</th>
                                    <th>Thành Phố</th>
                                    <th style="text-align: center;">Trạng Thái</th>
                                    <th style="text-align: right;">Doanh Thu Kỳ Này</th>
                                    <th style="text-align: right;">Doanh Thu Kỳ Trước</th>
                                    <th style="text-align: center;">Tăng Trưởng</th>
                                    <th style="text-align: center;">Thị Phần</th>
                                    <th style="text-align: right;">Vé Bán</th>
                                    <th style="text-align: right;">Giá Vé TB</th>
                                    <th style="text-align: right;">Doanh Thu F&B</th>
                                    <th style="text-align: center;">Phòng</th>
                                    <th style="text-align: center;">Suất</th>
                                    <th style="text-align: center;">Lấp Đầy</th>
                                    <th style="text-align: right;">DT / Phòng</th>
                                    <th>Phim Dẫn Đầu</th>
                                    <th style="text-align: center;">Phân Tầng</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:choose>
                                    <c:when test="${empty metrics}">
                                        <tr>
                                            <td colspan="18" style="text-align: center; padding: 40px; color: #64748b;">
                                                <i class="fa-regular fa-folder-open" style="font-size: 2.5rem; color: #cbd5e1; margin-bottom: 10px; display: block;"></i>
                                                Không có dữ liệu kinh doanh phù hợp cho giai đoạn lọc đã chọn.
                                            </td>
                                        </tr>
                                    </c:when>
                                    <c:otherwise>
                                        <c:forEach var="m" items="${metrics}">
                                            <tr>
                                                <td style="text-align: center;">
                                                    <span class="rank-circle ${m.rank == 1 ? 'rank-1' : (m.rank == 2 ? 'rank-2' : (m.rank == 3 ? 'rank-3' : 'rank-other'))}">
                                                        ${m.rank}
                                                    </span>
                                                </td>
                                                <td><span style="font-family: monospace; font-weight: 600; color: #0284c7;"><c:out value="${m.cinemaCode}"/></span></td>
                                                <td><strong style="color: #0f172a;"><c:out value="${m.cinemaName}"/></strong></td>
                                                <td><c:out value="${m.city != null ? m.city : '-'}"/></td>
                                                <td style="text-align: center;">
                                                    <c:choose>
                                                        <c:when test="${m.active}">
                                                            <span style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #16a34a; margin-right: 4px;"></span>
                                                            <span style="font-size: 0.78rem; color: #166534; font-weight: 600;">Hoạt động</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span style="display: inline-block; width: 8px; height: 8px; border-radius: 50%; background: #94a3b8; margin-right: 4px;"></span>
                                                            <span style="font-size: 0.78rem; color: #64748b; font-weight: 600;">Tạm ngưng</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: right; font-weight: 700; color: #0f172a;">
                                                    <fmt:formatNumber value="${m.revenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                </td>
                                                <td style="text-align: right; color: #64748b;">
                                                    <fmt:formatNumber value="${m.comparisonRevenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                </td>
                                                <td style="text-align: center;">
                                                    <c:choose>
                                                        <c:when test="${m.growthPercent > 0}">
                                                            <span style="color: #16a34a; font-weight: 600;">+<fmt:formatNumber value="${m.growthPercent}" maxFractionDigits="1"/>%</span>
                                                        </c:when>
                                                        <c:when test="${m.growthPercent < 0}">
                                                            <span style="color: #dc2626; font-weight: 600;"><fmt:formatNumber value="${m.growthPercent}" maxFractionDigits="1"/>%</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span style="color: #64748b;">0.0%</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td style="text-align: center; font-weight: 600; color: #0284c7;">
                                                    <fmt:formatNumber value="${m.revenueSharePercent}" maxFractionDigits="1"/>%
                                                </td>
                                                <td style="text-align: right;"><fmt:formatNumber value="${m.ticketsSold}" type="number"/></td>
                                                <td style="text-align: right; color: #475569;"><fmt:formatNumber value="${m.averageTicketPrice}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></td>
                                                <td style="text-align: right; color: #d97706;"><fmt:formatNumber value="${m.fnbRevenue}" type="currency" currencySymbol="₫" maxFractionDigits="0"/></td>
                                                <td style="text-align: center;">${m.roomsCount}</td>
                                                <td style="text-align: center;">${m.showtimesCount}</td>
                                                <td style="text-align: center; font-weight: 600;">
                                                    <span style="color: ${m.occupancyRate >= 70 ? '#16a34a' : (m.occupancyRate >= 40 ? '#0284c7' : '#dc2626')};">
                                                        <fmt:formatNumber value="${m.occupancyRate}" maxFractionDigits="1"/>%
                                                    </span>
                                                </td>
                                                <td style="text-align: right; color: #475569;">
                                                    <fmt:formatNumber value="${m.revenuePerRoom}" type="currency" currencySymbol="₫" maxFractionDigits="0"/>
                                                </td>
                                                <td><c:out value="${m.topMovieName}"/></td>
                                                <td style="text-align: center;">
                                                    <c:choose>
                                                        <c:when test="${m.performanceTier == 'TOP_TIER'}">
                                                            <span class="tier-badge tier-top"><i class="fa-solid fa-star"></i> Top Tier</span>
                                                        </c:when>
                                                        <c:when test="${m.performanceTier == 'ABOVE_AVERAGE'}">
                                                            <span class="tier-badge tier-above"><i class="fa-solid fa-arrow-up"></i> Trên TB</span>
                                                        </c:when>
                                                        <c:when test="${m.performanceTier == 'BELOW_AVERAGE'}">
                                                            <span class="tier-badge tier-below"><i class="fa-solid fa-minus"></i> Dưới TB</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="tier-badge tier-under"><i class="fa-solid fa-triangle-exclamation"></i> Cần Cải Thiện</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </main>
    </div>

    <!-- Chart.js Scripts -->
    <script>
        document.addEventListener('DOMContentLoaded', function () {
            const cinemaLabels = [
                <c:forEach var="m" items="${metrics}" varStatus="status">
                    "<c:out value="${m.cinemaName}"/>"<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ];
            const currentRevs = [
                <c:forEach var="m" items="${metrics}" varStatus="status">
                    ${m.revenue}<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ];
            const compRevs = [
                <c:forEach var="m" items="${metrics}" varStatus="status">
                    ${m.comparisonRevenue}<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ];
            const marketShares = [
                <c:forEach var="m" items="${metrics}" varStatus="status">
                    ${m.revenueSharePercent}<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ];

            // 1. Grouped Bar Chart
            const barCtx = document.getElementById('branchComparisonChart');
            if (barCtx && cinemaLabels.length > 0) {
                new Chart(barCtx, {
                    type: 'bar',
                    data: {
                        labels: cinemaLabels,
                        datasets: [
                            {
                                label: 'Kỳ này',
                                data: currentRevs,
                                backgroundColor: 'rgba(229, 9, 20, 0.85)',
                                borderColor: '#e50914',
                                borderWidth: 1,
                                borderRadius: 4
                            },
                            {
                                label: 'Kỳ so sánh',
                                data: compRevs,
                                backgroundColor: 'rgba(148, 163, 184, 0.6)',
                                borderColor: '#94a3b8',
                                borderWidth: 1,
                                borderRadius: 4
                            }
                        ]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: { position: 'top' },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        let val = context.parsed.y || 0;
                                        return context.dataset.label + ': ' + new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
                                    }
                                }
                            }
                        },
                        scales: {
                            y: {
                                beginAtZero: true,
                                ticks: {
                                    callback: function(value) {
                                        return (value / 1000000).toLocaleString('vi-VN') + ' tr';
                                    }
                                }
                            }
                        }
                    }
                });
            }

            // 2. Market Share Doughnut Chart
            const shareCtx = document.getElementById('marketShareChart');
            if (shareCtx && cinemaLabels.length > 0) {
                new Chart(shareCtx, {
                    type: 'doughnut',
                    data: {
                        labels: cinemaLabels,
                        datasets: [{
                            data: marketShares,
                            backgroundColor: [
                                '#e50914', '#0284c7', '#16a34a', '#d97706',
                                '#8b5cf6', '#ec4899', '#64748b', '#14b8a6'
                            ],
                            borderWidth: 2
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: { position: 'bottom', labels: { boxWidth: 12, font: { size: 11 } } },
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        return context.label + ': ' + context.parsed + '% thị phần';
                                    }
                                }
                            }
                        }
                    }
                });
            }
        });
    </script>
</body>
</html>
