<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<div id="sidebarMenu" class="d-flex flex-column p-3">

    <a href="${pageContext.request.contextPath}/admin/dashboard" class="d-flex align-items-center mb-4 text-white text-decoration-none">
        <span class="fs-4 fw-bold" style="letter-spacing: 1px;">
            <i class="fa fa-film me-2" style="color: #d96c2c;"></i>MyCinema
        </span>
    </a>
    <hr class="text-secondary mt-0">

    <ul class="nav nav-pills flex-column mb-auto">
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/dashboard"
               class="nav-link ${param.page == 'dashboard' ? 'active' : ''}">
                <i class="fa fa-home me-3"></i>Dashboard
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/manage-users"
               class="nav-link ${param.page == 'user' ? 'active' : ''}">
                <i class="fa fa-users me-3"></i>Quản lý người dùng
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/branches"
               class="nav-link ${param.page == 'branch' ? 'active' : ''}">
                <i class="fa fa-building me-3"></i>Quản lý Chi nhánh
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/movies" class="nav-link">
                <i class="fa fa-film me-3"></i>Quản lý Phim
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/manage-genres" class="nav-link">
                <i class="fa fa-tags me-3"></i>Quản lý thể loại
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/concessions" class="nav-link">
                <i class="fa fa-coffee me-3"></i>Quản lý đồ ăn & nước uống
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/assign-staff"
               class="nav-link ${param.page == 'assign-staff' ? 'active' : ''}">
                <i class="fa fa-user-plus me-3"></i>Phân công nhân viên
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/staff-schedule"
               class="nav-link ${param.page == 'staff-schedule' ? 'active' : ''}">
                <i class="fas fa-calendar-check me-3"></i>Lịch làm việc NV
            </a>
        </li>
        <li class="nav-item mt-3">
            <span class="nav-link text-muted fw-bold ps-3" style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em;">Hệ Thống Loyalty</span>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/loyalty-config" class="nav-link">
                <i class="fa fa-cogs me-3"></i>Cấu hình Tích Điểm
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/manage-tiers" class="nav-link">
                <i class="fa fa-trophy me-3"></i>Hạng Thành Viên
            </a>
        </li>
        <li class="nav-item">
            <a href="${pageContext.request.contextPath}/admin/manage-vouchers" class="nav-link">
                <i class="fa fa-tag me-3"></i>Quản lý Vouchers
            </a>
        </li>
    </ul>

    <div class="mt-auto pt-3 border-top border-secondary">

        <a href="${pageContext.request.contextPath}/logout" class="nav-link text-white-50 hover-white">
            <i class="fa fa-sign-out me-2 text-danger"></i> Đăng xuất
        </a>
    </div>
</div>