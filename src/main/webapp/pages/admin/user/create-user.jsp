<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tạo người dùng mới</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-layout.css">
</head>
<body>

<jsp:include page="/components/layout/dashboard/dashboard_header.jsp" />
<jsp:include page="/components/layout/dashboard/admin_sidebar.jsp">
    <jsp:param name="page" value="user"/>
</jsp:include>

<main>
    <div class="container-fluid" style="max-width: 900px;">
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/admin/manage-users" class="text-decoration-none text-secondary">Người dùng</a></li>
                <li class="breadcrumb-item active" style="color: #d96c2c;">Tạo mới</li>
            </ol>
        </nav>

        <form method="post" action="${pageContext.request.contextPath}/admin/create-user" id="createUserForm" class="needs-validation">
            <div class="card card-custom">
                <div class="card-header-custom">
                    <h5 class="mb-0 fw-bold">
                        <i class="fa fa-user-plus me-2" style="color: #d96c2c;"></i>
                        Thông tin người dùng mới
                    </h5>
                </div>

                <div class="card-body p-5">
                    <c:if test="${error != null}">
                        <div class="alert alert-danger mb-4"><i class="fa fa-exclamation-circle me-2"></i>${error}</div>
                    </c:if>

                    <div class="row g-4">
                        <div class="col-md-6">
                            <label class="form-label fw-bold">Username <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="username" value="${param.username}" required minlength="3"
                                   placeholder="Tên đăng nhập">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label fw-bold">Email <span class="text-danger">*</span></label>
                            <input type="email" class="form-control" name="email" value="${param.email}" required placeholder="name@example.com">
                        </div>

                        <div class="col-12">
                            <label class="form-label fw-bold">Họ và Tên <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" name="fullName" value="${param.fullName}" required placeholder="Nguyễn Văn A">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label fw-bold">Mật khẩu <span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="password" name="password" required minlength="6">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label fw-bold">Xác nhận mật khẩu <span class="text-danger">*</span></label>
                            <input type="password" class="form-control" id="confirmPassword" name="confirmPassword" required minlength="6">
                            <div class="invalid-feedback">Mật khẩu không khớp!</div>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label fw-bold">Số điện thoại</label>
                            <input type="tel" class="form-control" name="phone" value="${param.phone}" placeholder="09xxxxxxx">
                        </div>

                        <div class="col-md-6">
                            <label class="form-label fw-bold">Ngày sinh</label>
                            <input type="date" class="form-control" name="birthday" value="${param.birthday}">
                        </div>

                        <div class="col-12">
                            <label class="form-label fw-bold">Vai trò (Role) <span class="text-danger">*</span></label>
                            <select class="form-select" name="roleId" required>
                                <option value="">-- Chọn vai trò --</option>
                                <c:forEach var="role" items="${creatableRoles}">
                                    <option value="${role.roleId}" ${param.roleId == role.roleId.toString() ? 'selected' : ''}>
                                            ${role.roleName}
                                    </option>
                                </c:forEach>
                            </select>

                            <div class="alert alert-light mt-3 border border-secondary border-opacity-25">
                                <small class="text-muted">
                                    <i class="fa fa-info-circle me-1" style="color: #d96c2c;"></i>
                                    <strong>ADMIN:</strong> Quản trị hệ thống |
                                    <strong>BRANCH_MANAGER:</strong> Quản lý rạp |
                                    <strong>CINEMA_STAFF:</strong> Nhân viên bán vé
                                </small>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-footer bg-white p-4 d-flex justify-content-end gap-2">
                    <a href="${pageContext.request.contextPath}/admin/manage-users" class="btn btn-light px-4">Hủy bỏ</a>
                    <button type="submit" class="btn btn-orange px-5 fw-bold">
                        <i class="fa fa-save me-2"></i>Tạo người dùng
                    </button>
                </div>
            </div>
        </form>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
<script>
    // Validation match password đơn giản
    document.getElementById('createUserForm').addEventListener('submit', function(e) {
        const p1 = document.getElementById('password').value;
        const p2 = document.getElementById('confirmPassword').value;
        if(p1 !== p2) {
            e.preventDefault();
            alert("Mật khẩu xác nhận không khớp!");
        }
    });
</script>
</body>
</html>