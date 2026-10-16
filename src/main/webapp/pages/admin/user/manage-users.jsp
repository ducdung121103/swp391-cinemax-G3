<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="jakarta.tags.core" %>
        <%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
            <!DOCTYPE html>
            <html lang="vi">

            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Quản lý Người dùng</title>
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
                <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
                <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-layout.css">
            </head>

            <body>

                <jsp:include page="/components/layout/dashboard/dashboard_header.jsp" />
                <jsp:include page="/components/layout/dashboard/admin_sidebar.jsp">
                    <jsp:param name="page" value="user" />
                </jsp:include>

                <main>
                    <div class="container-fluid">
                        <div class="d-flex justify-content-between align-items-center mb-4">
                            <div>
                                <h3 class="fw-bold" style="color: #212529;">Quản lý Người dùng</h3>
                                <nav aria-label="breadcrumb">
                                    <ol class="breadcrumb">
                                        <li class="breadcrumb-item"><a href="#"
                                                class="text-decoration-none text-secondary">Admin</a></li>
                                        <li class="breadcrumb-item active" style="color: #d96c2c;">Người dùng</li>
                                    </ol>
                                </nav>
                            </div>
                            <a href="${pageContext.request.contextPath}/admin/create-user"
                                class="btn btn-orange shadow-sm px-4">
                                <i class="fa fa-user-plus me-2"></i>Thêm người dùng
                            </a>
                        </div>

                        <c:if test="${not empty param.success}">
                            <div class="alert alert-success border-0 shadow-sm"
                                style="border-left: 4px solid #d96c2c !important;">
                                <i class="fa fa-check-circle me-2"></i> ${param.success}
                            </div>
                        </c:if>
                        <c:if test="${not empty error}">
                            <div class="alert alert-danger border-0 shadow-sm">
                                <i class="fa fa-exclamation-triangle me-2"></i> ${error}
                            </div>
                        </c:if>

                        <div class="card card-custom">
                            <div class="card-body p-4">

                                <form method="get" action="${pageContext.request.contextPath}/admin/manage-users">
                                    <div class="row mb-4 g-3">
                                        <div class="col-md-3">
                                            <select class="form-select" name="roleFilter">
                                                <option value="">Tất cả Vai trò</option>
                                                <c:forEach var="role" items="${allRoles}">
                                                    <option value="${role.roleId}" ${roleFilter==role.roleId.toString()
                                                        ? 'selected' : '' }>
                                                        ${role.roleName}
                                                    </option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div class="col-md-3">
                                            <select class="form-select" name="statusFilter">
                                                <option value="">Tất cả Trạng thái</option>
                                                <option value="ACTIVE" ${statusFilter=='ACTIVE' ? 'selected' : '' }>
                                                    Active</option>
                                                <option value="LOCKED" ${statusFilter=='LOCKED' ? 'selected' : '' }>
                                                    Locked</option>
                                                <option value="INACTIVE" ${statusFilter=='INACTIVE' ? 'selected' : '' }>
                                                    Inactive</option>
                                            </select>
                                        </div>
                                        <div class="col-md-4">
                                            <div class="input-group">
                                                <span class="input-group-text bg-white border-end-0"><i
                                                        class="fa fa-search text-muted"></i></span>
                                                <input type="text" class="form-control border-start-0" name="search"
                                                    placeholder="Tìm theo tên, email..." value="${searchKeyword}">
                                            </div>
                                        </div>
                                        <div class="col-md-2">
                                            <button type="submit" class="btn btn-dark w-100">Lọc</button>
                                        </div>
                                    </div>
                                </form>

                                <div class="table-responsive">
                                    <table class="table table-custom table-hover align-middle">
                                        <thead>
                                            <tr>
                                                <th class="ps-4">ID</th>
                                                <th>Tài khoản / Email</th>
                                                <th>Họ tên</th>
                                                <th>Vai trò</th>
                                                <th>Trạng thái</th>
                                                <th>Đăng nhập cuối</th>
                                                <th class="text-end pe-4">Hành động</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:choose>
                                                <c:when test="${empty users}">
                                                    <tr>
                                                        <td colspan="7" class="text-center py-5 text-muted">Không tìm
                                                            thấy người dùng nào.</td>
                                                    </tr>
                                                </c:when>
                                                <c:otherwise>
                                                    <c:forEach var="user" items="${users}">
                                                        <tr>
                                                            <td class="ps-4 fw-bold" style="color: #d96c2c;">
                                                                #${user.userId}</td>
                                                            <td>
                                                                <div class="fw-bold text-dark">${user.username}</div>
                                                                <small class="text-muted">${user.email}</small>
                                                            </td>
                                                            <td>${user.fullName != null ? user.fullName : '<span
                                                                    class="text-muted">--</span>'}</td>
                                                            <td>
                                                                <span
                                                                    class="badge bg-light text-dark border border-secondary">
                                                                    ${roleMap[user.roleId]}
                                                                </span>
                                                            </td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${user.status == 'ACTIVE'}">
                                                                        <span
                                                                            class="badge bg-success rounded-pill px-3">Active</span>
                                                                    </c:when>
                                                                    <c:when test="${user.status == 'LOCKED'}">
                                                                        <span
                                                                            class="badge bg-danger rounded-pill px-3">Locked</span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <span
                                                                            class="badge bg-secondary rounded-pill px-3">Inactive</span>
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                            <td class="text-secondary small">
                                                                ${user.lastLogin != null ? user.lastLoginFormatted :
                                                                'Chưa từng'}
                                                            </td>
                                                            <td class="text-end pe-4">
                                                                <c:choose>
                                                                    <c:when test="${roleMap[user.roleId] == 'ADMIN'}">
                                                                        <span class="badge bg-secondary text-white px-2 py-1"
                                                                            title="Tài khoản Admin không thể bị khóa hoặc xóa">
                                                                            <i class="fa fa-shield-halved me-1"></i>Protected
                                                                        </span>
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        <div class="btn-group">
                                                                            <c:if test="${user.status == 'ACTIVE'}">
                                                                                <form method="post"
                                                                                    onsubmit="return confirm('Khóa tài khoản này?');">
                                                                                    <input type="hidden" name="action"
                                                                                        value="lock">
                                                                                    <input type="hidden" name="userId"
                                                                                        value="${user.userId}">
                                                                                    <button
                                                                                        class="btn btn-outline-warning btn-sm"
                                                                                        title="Khóa">
                                                                                        <i class="fa fa-lock"></i>
                                                                                    </button>
                                                                                </form>
                                                                            </c:if>
                                                                            <c:if test="${user.status == 'LOCKED'}">
                                                                                <form method="post"
                                                                                    onsubmit="return confirm('Mở khóa tài khoản này?');">
                                                                                    <input type="hidden" name="action"
                                                                                        value="unlock">
                                                                                    <input type="hidden" name="userId"
                                                                                        value="${user.userId}">
                                                                                    <button
                                                                                        class="btn btn-outline-success btn-sm"
                                                                                        title="Mở khóa">
                                                                                        <i class="fa fa-unlock"></i>
                                                                                    </button>
                                                                                </form>
                                                                            </c:if>

                                                                            <form method="post" class="ms-1"
                                                                                onsubmit="return confirm('Xóa vĩnh viễn user này?');">
                                                                                <input type="hidden" name="action"
                                                                                    value="delete">
                                                                                <input type="hidden" name="userId"
                                                                                    value="${user.userId}">
                                                                                <button class="btn btn-outline-danger btn-sm"
                                                                                    title="Xóa">
                                                                                    <i class="fa fa-trash"></i>
                                                                                </button>
                                                                            </form>
                                                                        </div>
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

                                <div class="d-flex justify-content-between align-items-center mt-3">
                                    <span class="text-muted small">
                                        Tổng số: <strong>${totalUsers}</strong> người dùng &nbsp;|&nbsp;
                                        Trang <strong>${currentPage}</strong> / <strong>${totalPages}</strong>
                                    </span>

                                    <c:if test="${totalPages > 1}">
                                        <nav aria-label="Phân trang người dùng">
                                            <ul class="pagination pagination-sm mb-0">
                                                <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                                    <a class="page-link"
                                                        href="${pageContext.request.contextPath}/admin/manage-users?page=${currentPage - 1}&roleFilter=${roleFilter}&statusFilter=${statusFilter}&search=${searchKeyword}">
                                                        &laquo;
                                                    </a>
                                                </li>
                                                <c:forEach begin="1" end="${totalPages}" var="p">
                                                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                                                        <a class="page-link"
                                                            href="${pageContext.request.contextPath}/admin/manage-users?page=${p}&roleFilter=${roleFilter}&statusFilter=${statusFilter}&search=${searchKeyword}">
                                                            ${p}
                                                        </a>
                                                    </li>
                                                </c:forEach>
                                                <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                                    <a class="page-link"
                                                        href="${pageContext.request.contextPath}/admin/manage-users?page=${currentPage + 1}&roleFilter=${roleFilter}&statusFilter=${statusFilter}&search=${searchKeyword}">
                                                        &raquo;
                                                    </a>
                                                </li>
                                            </ul>
                                        </nav>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </div>
                </main>

                <script src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
            </body>

            </html>