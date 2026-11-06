<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý tài khoản - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .filter-bar { display: flex; gap: 12px; flex-wrap: wrap; margin: 16px 0; }
        .filter-input, .filter-select { padding: 8px 12px; border: 1px solid #cbd5e0; border-radius: 6px; }
        .alert { padding: 12px 16px; border-radius: 6px; margin-bottom: 16px; }
        .alert-success { background: #c6f6d5; color: #22543d; }
        .alert-danger { background: #fed7d7; color: #742a2a; }
        table { width: 100%; border-collapse: collapse; background: #fff; }
        th, td { padding: 10px; border-bottom: 1px solid #e2e8f0; vertical-align: top; text-align: left; }
        td form { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <div class="admin-content">
            <h2>Quản lý tài khoản</h2>

            <c:if test="${msg == 'status_updated'}"><div class="alert alert-success">Đã cập nhật trạng thái.</div></c:if>
            <c:if test="${msg == 'assigned'}"><div class="alert alert-success">Đã gán vai trò và chi nhánh.</div></c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <c:choose>
                        <c:when test="${error == 'SELF'}">Không thao tác trên chính tài khoản đang đăng nhập.</c:when>
                        <c:when test="${error == 'LAST_ADMIN'}">Không khóa admin đang hoạt động cuối cùng.</c:when>
                        <c:when test="${error == 'ADMIN'}">Không đổi vai trò của tài khoản ADMIN.</c:when>
                        <c:when test="${error == 'ROLE'}">Chỉ gán được STAFF hoặc MANAGER.</c:when>
                        <c:when test="${error == 'CUSTOMER'}">Tài khoản khách không gán chi nhánh.</c:when>
                        <c:when test="${error == 'CINEMA'}">Staff và Manager phải thuộc một rạp đang hoạt động.</c:when>
                        <c:otherwise>Không thực hiện được.</c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <form method="get" action="${pageContext.request.contextPath}/admin/identity/users" class="filter-bar">
                <input class="filter-input" type="text" name="keyword" value="${keyword}" placeholder="Tên hoặc email" />
                <select class="filter-select" name="role">
                    <option value="">Mọi vai trò</option>
                    <c:forEach var="roleName" items="${['ADMIN','MANAGER','STAFF','CUSTOMER']}">
                        <option value="${roleName}" ${selectedRole == roleName ? 'selected' : ''}>${roleName}</option>
                    </c:forEach>
                </select>
                <select class="filter-select" name="status">
                    <option value="">Mọi trạng thái</option>
                    <c:forEach var="statusName" items="${['ACTIVE','LOCKED','UNVERIFIED']}">
                        <option value="${statusName}" ${selectedStatus == statusName ? 'selected' : ''}>${statusName}</option>
                    </c:forEach>
                </select>
                <button class="btn btn-primary" type="submit">Lọc</button>
            </form>

            <table class="table">
                <thead>
                <tr>
                    <th>STT</th>
                    <th>Họ tên</th>
                    <th>Email</th>
                    <th>Vai trò</th>
                    <th>Rạp</th>
                    <th>Trạng thái</th>
                    <th>Khóa / mở</th>
                    <th>Gán chi nhánh</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="user" items="${users}" varStatus="st">
                    <tr>
                        <td>${(page - 1) * pageSize + st.index + 1}</td>
                        <td>${user.fullName}</td>
                        <td>${user.email}</td>
                        <td>${user.role.roleName}</td>
                        <td>${empty user.cinema ? '—' : user.cinema.name}</td>
                        <td>${user.status}</td>
                        <td>
                            <c:if test="${user.status == 'ACTIVE' || user.status == 'LOCKED'}">
                                <form method="post" action="${pageContext.request.contextPath}/admin/identity/users">
                                    <input type="hidden" name="action" value="toggleStatus" />
                                    <input type="hidden" name="userId" value="${user.id}" />
                                    <input type="hidden" name="keyword" value="${keyword}" />
                                    <input type="hidden" name="role" value="${selectedRole}" />
                                    <input type="hidden" name="status" value="${selectedStatus}" />
                                    <input type="hidden" name="page" value="${page}" />
                                    <button class="btn btn-sm btn-outline-secondary" type="submit">
                                        ${user.status == 'ACTIVE' ? 'Khóa' : 'Mở'}
                                    </button>
                                </form>
                            </c:if>
                        </td>
                        <td>
                            <c:choose>
                                <c:when test="${user.role.roleName == 'STAFF' || user.role.roleName == 'MANAGER'}">
                                    <form method="post" action="${pageContext.request.contextPath}/admin/identity/users">
                                        <input type="hidden" name="action" value="assign" />
                                        <input type="hidden" name="userId" value="${user.id}" />
                                        <input type="hidden" name="keyword" value="${keyword}" />
                                        <input type="hidden" name="role" value="${selectedRole}" />
                                        <input type="hidden" name="status" value="${selectedStatus}" />
                                        <input type="hidden" name="page" value="${page}" />
                                        <select name="roleName">
                                            <option value="STAFF" ${user.role.roleName == 'STAFF' ? 'selected' : ''}>STAFF</option>
                                            <option value="MANAGER" ${user.role.roleName == 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                                        </select>
                                        <select name="cinemaId" required>
                                            <option value="">Chọn rạp</option>
                                            <c:forEach var="cinema" items="${cinemas}">
                                                <option value="${cinema.id}" ${user.cinemaId == cinema.id ? 'selected' : ''}>${cinema.name}</option>
                                            </c:forEach>
                                        </select>
                                        <button class="btn btn-sm btn-primary" type="submit">Gán</button>
                                    </form>
                                </c:when>
                                <c:otherwise>—</c:otherwise>
                            </c:choose>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <div>
                <c:if test="${page > 1}">
                    <c:url var="prevUrl" value="/admin/identity/users">
                        <c:param name="page" value="${page - 1}" />
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="role" value="${selectedRole}" />
                        <c:param name="status" value="${selectedStatus}" />
                    </c:url>
                    <a href="${prevUrl}">Trước</a>
                </c:if>
                <span>Trang ${page} / ${totalPages}</span>
                <c:if test="${page < totalPages}">
                    <c:url var="nextUrl" value="/admin/identity/users">
                        <c:param name="page" value="${page + 1}" />
                        <c:param name="keyword" value="${keyword}" />
                        <c:param name="role" value="${selectedRole}" />
                        <c:param name="status" value="${selectedStatus}" />
                    </c:url>
                    <a href="${nextUrl}">Sau</a>
                </c:if>
            </div>
        </div>
    </div>
</div>
</body>
</html>
