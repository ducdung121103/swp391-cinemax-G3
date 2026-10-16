<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>${not empty room.roomId ? 'Cập nhật' : 'Thêm'} Phòng chiếu</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/admin-layout.css">
</head>
<body>

<jsp:include page="/components/layout/dashboard/dashboard_header.jsp" />
<jsp:include page="/components/layout/dashboard/admin_sidebar.jsp">
    <jsp:param name="page" value="branch"/>
</jsp:include>

<main>
    <div class="container-fluid" style="max-width: 900px;">
        <nav aria-label="breadcrumb" class="mb-4">
            <ol class="breadcrumb">
                <li class="breadcrumb-item active" style="color: #d96c2c;">${not empty room.roomId ? 'Cập nhật' : 'Thêm mới'}</li>                <li class="breadcrumb-item active" style="color: #d96c2c;">
                    ${isViewMode ? 'Chi tiết' : (branch != null ? 'Cập nhật' : 'Thêm mới')}
                </li>
            </ol>
        </nav>

        <form action="branches" method="post">
            <input type="hidden" name="action" value="${not empty branch.branchId ? 'update' : 'create'}">
            <c:if test="${not empty branch.branchId}">
                <input type="hidden" name="branchId" value="${branch.branchId}">
            </c:if>

            <div class="card card-custom">
                <div class="card-header-custom">
                    <h5 class="mb-0 fw-bold">
                        <i class="fa ${isViewMode ? 'fa-eye' : (not empty branch.branchId ? 'fa-edit' : 'fa-plus-circle')} me-2" style="color: #d96c2c;"></i>
                        ${isViewMode ? 'Thông tin chi tiết' : (not empty branch.branchId ? 'Cập nhật thông tin' : 'Tạo chi nhánh mới')}
                    </h5>
                </div>

                <div class="card-body p-5">
                    <c:if test="${not empty error}">
                        <div class="alert alert-danger mb-4">${error}</div>
                    </c:if>

                    <c:set var="disabledAttr" value="${isViewMode ? 'disabled' : ''}" />

                    <div class="row g-4">
                        <div class="col-12">
                            <label class="form-label fw-bold">Tên Chi nhánh <span class="text-danger">*</span></label>
                            <input type="text" class="form-control form-control-lg" name="branchName"
                                   value="${branch.branchName}" required placeholder="Ví dụ: Hola Cinema" ${disabledAttr}>
                        </div>

                        <div class="col-12">
                            <label class="form-label fw-bold">Địa chỉ</label>
                            <input type="text" class="form-control" name="address"
                                   value="${branch.address}" placeholder="Số nhà, đường, quận..." ${disabledAttr}>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label fw-bold">Số điện thoại</label>
                            <input type="text" class="form-control" name="phone" value="${branch.phone}"
                                   pattern="^(84|0[3|5|7|8|9])+([0-9]{8})$" maxlength="10"
                                   title="Vui lòng nhập SĐT hợp lệ (VD: 0987654321)" placeholder="098..." ${disabledAttr}>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label fw-bold">Email</label>
                            <input type="email" class="form-control" name="email" value="${branch.email}" placeholder="admin@mycinema.com" ${disabledAttr}>
                        </div>

                        <div class="col-md-6">
                            <label class="form-label fw-bold">Quản lý (Manager)</label>
                            <select class="form-select" name="managerId" ${disabledAttr}>
                                <option value="">-- Chọn quản lý --</option>
                                <c:forEach var="mgr" items="${managers}">
                                    <option value="${mgr.userId}" ${branch.managerId == mgr.userId ? 'selected' : ''}>
                                            ${mgr.fullName} (${mgr.username})
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="col-md-6 d-flex align-items-center">
                            <div class="form-check form-switch ps-5 mt-4">
                                <input class="form-check-input" type="checkbox" name="isActive"
                                       style="width: 3em; height: 1.5em; background-color: #d96c2c; border-color: #d96c2c;"
                                ${branch == null || branch.active ? 'checked' : ''} ${disabledAttr}>
                                <label class="form-check-label fw-bold ms-2 pt-1">Kích hoạt hoạt động</label>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card-footer bg-white p-4 d-flex justify-content-between align-items-center">
                    <div>
                        <c:if test="${isViewMode}">
                            <a href="branches?action=edit&id=${branch.branchId}" class="btn btn-outline-warning fw-bold">
                                <i class="fa fa-pencil me-2"></i>Chỉnh sửa chi nhánh này
                            </a>
                        </c:if>
                    </div>
                    <div class="d-flex gap-2">
                        <a href="branches" class="btn btn-light px-4 border">${isViewMode ? 'Quay lại danh sách' : 'Hủy bỏ'}</a>

                        <c:if test="${!isViewMode}">
                            <button type="submit" class="btn btn-orange px-5 fw-bold">
                                <i class="fa fa-save me-2"></i>Lưu lại
                            </button>
                        </c:if>
                    </div>
                </div>
            </div>
        </form>
    </div>
</main>
<script src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
</body>
</html>