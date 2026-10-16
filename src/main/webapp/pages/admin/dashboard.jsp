<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Admin Dashboard - MBCMS</title>
                <link href="${pageContext.request.contextPath}/css/bootstrap.min.css" rel="stylesheet">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-layout.css">
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

                <style>
                    /* Dashboard Specific Overrides */
                    :root {
                        --primary-color: #d96c2c;
                        --dark-bg: #141414;
                        --card-bg: #1f1f1f;
                        --text-color: #ffffff;
                        --text-muted: #b3b3b3;
                        --success-color: #46d369;
                        --danger-color: #e50914;
                    }

                    /* Override body background to keep dashboard dark */
                    body {
                        background-color: var(--dark-bg);
                        color: var(--text-color);
                    }

                    /* KPI Cards */
                    .kpi-grid {
                        display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
                        gap: 20px;
                        margin-bottom: 30px;
                    }

                    .kpi-card {
                        background-color: var(--card-bg);
                        padding: 25px;
                        border-radius: 12px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
                        display: flex;
                        flex-direction: column;
                        justify-content: space-between;
                        border-top: 4px solid transparent;
                        transition: transform 0.2s, box-shadow 0.2s;
                    }

                    .kpi-card:hover {
                        transform: translateY(-5px);
                        box-shadow: 0 8px 15px rgba(0, 0, 0, 0.3);
                        border-top-color: var(--primary-color);
                    }

                    .kpi-title {
                        color: var(--text-muted);
                        font-size: 0.85rem;
                        margin-bottom: 15px;
                        text-transform: uppercase;
                        letter-spacing: 1px;
                        font-weight: 600;
                    }

                    .kpi-value {
                        font-size: 2rem;
                        font-weight: 700;
                        margin-bottom: 5px;
                        color: #fff;
                    }

                    .kpi-trend {
                        font-size: 0.9rem;
                        display: flex;
                        align-items: center;
                        gap: 8px;
                        margin-top: 5px;
                    }

                    .trend-up {
                        color: var(--success-color);
                    }

                    .trend-down {
                        color: var(--danger-color);
                    }

                    /* Charts Section */
                    .charts-grid {
                        display: grid;
                        grid-template-columns: 2fr 1fr;
                        gap: 20px;
                        margin-bottom: 30px;
                    }

                    .chart-full-width {
                        grid-column: 1 / -1;
                    }

                    .chart-card {
                        background-color: var(--card-bg);
                        padding: 25px;
                        border-radius: 12px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
                        height: 450px;
                        /* Fixed height */
                        position: relative;
                        display: flex;
                        flex-direction: column;
                    }

                    .chart-card h3 {
                        margin-top: 0;
                        margin-bottom: 20px;
                        font-size: 1.2rem;
                        color: var(--text-color);
                        font-weight: 600;
                        flex-shrink: 0;
                    }

                    .chart-container {
                        flex-grow: 1;
                        position: relative;
                        min-height: 0;
                        width: 100%;
                    }

                    /* Lists Section */
                    .lists-grid {
                        display: grid;
                        grid-template-columns: 2fr 1fr;
                        gap: 20px;
                    }

                    .list-card {
                        background-color: var(--card-bg);
                        padding: 25px;
                        border-radius: 12px;
                        box-shadow: 0 4px 6px rgba(0, 0, 0, 0.2);
                    }

                    .list-card h3 {
                        margin-top: 0;
                        margin-bottom: 20px;
                        font-size: 1.2rem;
                        color: var(--text-color);
                        font-weight: 600;
                    }

                    /* Table Styles */
                    .table-responsive {
                        width: 100%;
                        overflow-x: auto;
                    }

                    table {
                        width: 100%;
                        border-collapse: collapse;
                        font-size: 0.95rem;
                        color: var(--text-color);
                    }

                    th,
                    td {
                        padding: 15px;
                        text-align: left;
                        border-bottom: 1px solid #333;
                    }

                    th {
                        color: var(--text-muted);
                        font-weight: 600;
                        text-transform: uppercase;
                        font-size: 0.8rem;
                        letter-spacing: 0.5px;
                    }

                    tr:last-child td {
                        border-bottom: none;
                    }

                    tr:hover {
                        background-color: rgba(255, 255, 255, 0.03);
                    }

                    /* Movie Status List */
                    .status-list {
                        list-style: none;
                        padding: 0;
                        margin: 0;
                    }

                    .status-item {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        padding: 15px 0;
                        border-bottom: 1px solid #333;
                    }

                    .status-item:last-child {
                        border-bottom: none;
                    }

                    .status-label {
                        display: flex;
                        align-items: center;
                        gap: 12px;
                        font-size: 1rem;
                    }

                    .status-badge {
                        width: 12px;
                        height: 12px;
                        border-radius: 50%;
                        display: inline-block;
                        box-shadow: 0 0 5px currentColor;
                    }

                    .badge-success {
                        background-color: var(--success-color);
                        color: var(--success-color);
                    }

                    .badge-warning {
                        background-color: #ffc107;
                        color: #ffc107;
                    }

                    .status-count {
                        font-weight: 700;
                        font-size: 1.3rem;
                    }

                    /* Responsive */
                    @media (max-width: 992px) {

                        .charts-grid,
                        .lists-grid {
                            grid-template-columns: 1fr;
                        }
                    }
                </style>
            </head>

            <body>

                <!-- Header -->
                <jsp:include page="/components/layout/dashboard/dashboard_header.jsp" />

                <!-- Sidebar -->
                <jsp:include page="/components/layout/dashboard/admin_sidebar.jsp">
                    <jsp:param name="page" value="dashboard" />
                </jsp:include>

                <!-- Main Content -->
                <main>
                    <div class="container-fluid">

                        <div class="d-flex justify-content-between align-items-center mb-4">
                            <div>
                                <h3 class="fw-bold text-white">Admin Dashboard</h3>
                                <p class="text-muted mb-0">Tổng quan tình hình kinh doanh tháng này:
                                    <fmt:formatDate value="<%= new java.util.Date() %>" pattern="MM/yyyy" />
                                </p>
                            </div>
                        </div>

                        <!-- 1. KPI Cards -->
                        <div class="kpi-grid">
                            <!-- Total Revenue -->
                            <div class="kpi-card">
                                <div class="kpi-title"><i class="fas fa-dollar-sign"></i> Tổng Doanh Thu</div>
                                <div class="kpi-value">
                                    <fmt:formatNumber value="${dashboard.kpi.totalRevenueMonth}" type="currency"
                                        currencySymbol="₫" maxFractionDigits="0" />
                                </div>
                                <div class="kpi-trend ${dashboard.kpi.revenueIncreased ? 'trend-up' : 'trend-down'}">
                                    <i class="fas fa-arrow-${dashboard.kpi.revenueIncreased ? 'up' : 'down'}"></i>
                                    <span>
                                        <fmt:formatNumber value="${dashboard.kpi.revenueChange}"
                                            maxFractionDigits="1" />% so với tháng trước
                                    </span>
                                </div>
                            </div>

                            <!-- Tickets Sold -->
                            <div class="kpi-card">
                                <div class="kpi-title"><i class="fas fa-ticket-alt"></i> Vé Đã Bán</div>
                                <div class="kpi-value">${dashboard.kpi.ticketsSoldMonth}</div>
                                <div class="kpi-trend ${dashboard.kpi.ticketsChange >= 0 ? 'trend-up' : 'trend-down'}">
                                    <i class="fas fa-arrow-${dashboard.kpi.ticketsChange >= 0 ? 'up' : 'down'}"></i>
                                    <span>${dashboard.kpi.ticketsChange >= 0 ? '+' : ''}${dashboard.kpi.ticketsChange}
                                        vé so với tháng trước</span>
                                </div>
                            </div>

                            <!-- New Customers -->
                            <div class="kpi-card">
                                <div class="kpi-title"><i class="fas fa-users"></i> Khách Hàng Mới</div>
                                <div class="kpi-value">${dashboard.kpi.newCustomersMonth}</div>
                                <div class="kpi-trend">
                                    <span class="text-muted">Trong tháng này</span>
                                </div>
                            </div>

                            <!-- Active Branches -->
                            <div class="kpi-card">
                                <div class="kpi-title"><i class="fas fa-store"></i> Rạp Đang Hoạt Động</div>
                                <div class="kpi-value">
                                    <span
                                        class="${dashboard.kpi.activeBranches < dashboard.kpi.totalBranches ? 'text-danger' : 'text-success'}">
                                        ${dashboard.kpi.activeBranches}/${dashboard.kpi.totalBranches}
                                    </span>
                                </div>
                                <div class="kpi-trend">
                                    <span class="text-muted">Chi nhánh</span>
                                </div>
                            </div>
                        </div>

                        <!-- 2. Analytical Charts -->
                        <div class="charts-grid">
                            <!-- Revenue by Branch -->
                            <div class="chart-card">
                                <h3>Doanh thu theo Chi nhánh</h3>
                                <div class="chart-container">
                                    <canvas id="revenueBranchChart"></canvas>
                                </div>
                            </div>

                            <!-- Top Movies -->
                            <div class="chart-card">
                                <h3>Top Phim ăn khách</h3>
                                <div class="chart-container">
                                    <canvas id="topMoviesChart"></canvas>
                                </div>
                            </div>

                            <!-- Trend Chart -->
                            <div class="chart-card chart-full-width">
                                <h3>Biểu đồ xu hướng 6 tháng gần nhất</h3>
                                <div class="chart-container">
                                    <canvas id="revenueTrendChart"></canvas>
                                </div>
                            </div>
                        </div>

                        <!-- 3. Detailed Lists -->
                        <div class="lists-grid">
                            <!-- Recent Transactions -->
                            <div class="list-card">
                                <h3>Giao dịch gần đây</h3>
                                <div class="table-responsive">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Mã vé</th>
                                                <th>Khách hàng</th>
                                                <th>Phim</th>
                                                <th>Rạp</th>
                                                <th>Số tiền</th>
                                                <th>Thời gian</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="item" items="${dashboard.recentTransactions}">
                                                <tr>
                                                    <td>${item.ticketCode}</td>
                                                    <td>${item.customerName}</td>
                                                    <td class="text-truncate" style="max-width: 150px;">
                                                        ${item.movieTitle}</td>
                                                    <td>${item.branchName}</td>
                                                    <td style="color: var(--success-color); font-weight: bold;">
                                                        <fmt:formatNumber value="${item.amount}" type="currency"
                                                            currencySymbol="₫" maxFractionDigits="0" />
                                                    </td>
                                                    <td>
                                                        ${item.time}
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                            <c:if test="${empty dashboard.recentTransactions}">
                                                <tr>
                                                    <td colspan="6"
                                                        style="text-align: center; color: var(--text-muted); padding: 30px;">
                                                        Chưa có giao dịch nào gần đây.</td>
                                                </tr>
                                            </c:if>
                                        </tbody>
                                    </table>
                                </div>
                            </div>

                            <!-- Movie Status -->
                            <div class="list-card">
                                <h3>Tình trạng phim</h3>
                                <ul class="status-list">
                                    <li class="status-item">
                                        <div class="status-label">
                                            <span class="status-badge badge-success"></span>
                                            Đang chiếu (Now Showing)
                                        </div>
                                        <div class="status-count">${dashboard.movieStatus.nowShowing}</div>
                                    </li>
                                    <li class="status-item">
                                        <div class="status-label">
                                            <span class="status-badge badge-warning"></span>
                                            Sắp chiếu (Coming Soon)
                                        </div>
                                        <div class="status-count">${dashboard.movieStatus.comingSoon}</div>
                                    </li>
                                </ul>
                                <div style="margin-top: 25px; text-align: center;">
                                    <a href="${pageContext.request.contextPath}/admin/manage-movie"
                                        style="color: var(--primary-color); text-decoration: none; font-weight: bold; border: 1px solid var(--primary-color); padding: 10px 20px; border-radius: 5px; transition: 0.3s; display: inline-block;">
                                        Quản lý kho phim <i class="fas fa-arrow-right ml-2"></i>
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>

                <!-- Scripts -->
                <script src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
                <script>
                    // Sidebar toggle & Init
                    document.addEventListener('DOMContentLoaded', function () {
                        const toggleBtn = document.getElementById('sidebarToggle');
                        const body = document.body;

                        if (toggleBtn) {
                            toggleBtn.addEventListener('click', function () {
                                body.classList.toggle('sidebar-collapsed');
                            });
                        }
                    });

                    // Common Chart Options
                    Chart.defaults.color = '#b3b3b3';
                    Chart.defaults.borderColor = '#333';
                    Chart.defaults.font.family = "'Helvetica Neue', 'Helvetica', 'Arial', sans-serif";

                    // Parse Data from Controller
                    const revenueByBranchData = ${ revenueByBranchJson }; // List<String> labels, List<BigDecimal> data
                    const topMoviesData = ${ topMoviesJson };
                    const revenueTrendData = ${ revenueTrendJson };

                    // 1. Revenue by Branch (Bar Chart)
                    const ctxBranch = document.getElementById('revenueBranchChart').getContext('2d');
                    new Chart(ctxBranch, {
                        type: 'bar',
                        data: {
                            labels: revenueByBranchData.labels,
                            datasets: [{
                                label: 'Doanh thu',
                                data: revenueByBranchData.data,
                                backgroundColor: '#d96c2c', // Primary color
                                borderRadius: 4
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: {
                                legend: { display: false }
                            },
                            scales: {
                                y: {
                                    beginAtZero: true,
                                    grid: { color: '#333' },
                                    ticks: { callback: function (value) { return value.toLocaleString() + ' ₫'; } }
                                },
                                x: {
                                    grid: { display: false }
                                }
                            }
                        }
                    });

                    // 2. Top Movies (Pie Chart -> Doughnut)
                    const ctxMovies = document.getElementById('topMoviesChart').getContext('2d');
                    new Chart(ctxMovies, {
                        type: 'doughnut',
                        data: {
                            labels: topMoviesData.labels,
                            datasets: [{
                                label: 'Doanh thu',
                                data: topMoviesData.data,
                                backgroundColor: [
                                    '#d96c2c',
                                    '#ffce56',
                                    '#4bc0c0',
                                    '#36a2eb',
                                    '#9966ff'
                                ],
                                borderWidth: 0
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: {
                                legend: {
                                    position: 'right',
                                    labels: { boxWidth: 12, padding: 20 }
                                },
                                tooltip: {
                                    callbacks: {
                                        label: function (context) {
                                            let label = context.label || '';
                                            if (label) {
                                                label += ': ';
                                            }
                                            if (context.parsed !== null) {
                                                label += context.parsed.toLocaleString() + ' ₫';
                                            }
                                            return label;
                                        }
                                    }
                                }
                            }
                        }
                    });

                    // 3. Trend (Line Chart)
                    const ctxTrend = document.getElementById('revenueTrendChart').getContext('2d');
                    new Chart(ctxTrend, {
                        type: 'line',
                        data: {
                            labels: revenueTrendData.labels,
                            datasets: [{
                                label: 'Doanh thu tổng',
                                data: revenueTrendData.data,
                                borderColor: '#46d369',
                                backgroundColor: 'rgba(70, 211, 105, 0.1)',
                                borderWidth: 2,
                                fill: true,
                                tension: 0.4, // Smooth curves
                                pointRadius: 4,
                                pointHoverRadius: 6
                            }]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: {
                                legend: { display: false },
                                tooltip: {
                                    mode: 'index',
                                    intersect: false,
                                    callbacks: {
                                        label: function (context) {
                                            let label = context.dataset.label || '';
                                            if (label) {
                                                label += ': ';
                                            }
                                            if (context.parsed.y !== null) {
                                                label += context.parsed.y.toLocaleString() + ' ₫';
                                            }
                                            return label;
                                        }
                                    }
                                }
                            },
                            scales: {
                                y: {
                                    beginAtZero: true,
                                    grid: { color: '#333' },
                                    ticks: { callback: function (value) { return value.toLocaleString() + ' ₫'; } }
                                },
                                x: {
                                    grid: { display: false }
                                }
                            },
                            interaction: {
                                mode: 'nearest',
                                axis: 'x',
                                intersect: false
                            }
                        }
                    });
                </script>
            </body>

            </html>