<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <title>Quản lý Chi nhánh</title>
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
    <div class="container-fluid">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <div>
                <h3 class="fw-bold" style="color: #212529;">Quản lý Chi nhánh</h3>
                <nav aria-label="breadcrumb">
                    <ol class="breadcrumb">
                        <li class="breadcrumb-item"><a href="#" class="text-decoration-none text-secondary">Admin</a></li>
                        <li class="breadcrumb-item active" style="color: #d96c2c;">Chi nhánh</li>
                    </ol>
                </nav>
            </div>
            <a href="branches?action=create" class="btn btn-orange shadow-sm px-4">
                <i class="fa fa-plus me-2"></i>Thêm mới
            </a>
        </div>

        <c:if test="${not empty param.message}">
            <div class="alert alert-success border-0 shadow-sm d-flex align-items-center" role="alert" style="border-left: 4px solid #d96c2c !important;">
                <i class="fa fa-check-circle me-2 text-success"></i> Thao tác thành công!
                <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <div class="card card-custom">
            <div class="card-body p-4">

                <form action="branches" method="get" id="filterForm">
                    <div class="row mb-4 g-3">
                        <div class="col-md-5">
                            <div class="input-group">
                                <span class="input-group-text bg-white border-end-0"><i class="fa fa-search text-muted"></i></span>
                                <input type="text" class="form-control border-start-0"
                                       name="keyword" value="${keyword}"
                                       placeholder="Tìm kiếm tên chi nhánh...">
                            </div>
                        </div>
                        <div class="col-md-3 ms-auto">
                            <select class="form-select" name="status" onchange="document.getElementById('filterForm').submit()">
                                <option value="">Tất cả trạng thái</option>
                                <option value="active" ${status == 'active' ? 'selected' : ''}>Đang hoạt động</option>
                                <option value="inactive" ${status == 'inactive' ? 'selected' : ''}>Ngừng hoạt động</option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <div class="d-flex gap-2">
                                <button type="submit" class="btn btn-dark flex-grow-1"><i class="fa fa-filter me-1"></i> Lọc</button>
                                <a href="branches" class="btn btn-light border flex-grow-1"><i class="fa fa-undo me-1"></i> Reset</a>
                            </div>
                        </div>
                    </div>
                </form>

                <div class="table-responsive">
                    <table class="table table-custom table-hover align-middle">
                        <thead>
                        <tr>
                            <th class="ps-4">ID</th>
                            <th>Tên Chi nhánh</th>
                            <th>Liên hệ</th>
                            <th>Quản lý</th>
                            <th>Trạng thái</th>
                            <th class="text-end pe-4">Hành động</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach var="b" items="${branches}">
                            <tr>
                                <td class="ps-4 fw-bold" style="color: #d96c2c;">#${b.branchId}</td>
                                <td>
                                    <div class="fw-bold text-dark">${b.branchName}</div>
                                    <small class="text-muted"><i class="fa fa-clock-o me-1"></i>${b.createdAt.toLocalDate()}</small>
                                </td>
                                <td>
                                    <div class="d-flex align-items-center mb-1 text-secondary">
                                        <i class="fa fa-map-marker me-2 text-danger" style="width:15px"></i>${b.address}
                                    </div>
                                    <div class="d-flex align-items-center text-secondary">
                                        <i class="fa fa-phone me-2 text-success" style="width:15px"></i>${b.phone}
                                    </div>
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${not empty b.managerName}">
                                            <div class="badge bg-light text-dark border">
                                                <i class="fa fa-user me-1" style="color: #d96c2c;"></i> ${b.managerName}
                                            </div>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-muted fst-italic">-- Chưa có --</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <span class="badge ${b.active ? 'bg-success' : 'bg-secondary'} rounded-pill px-3">
                                            ${b.active ? 'Active' : 'Inactive'}
                                    </span>
                                </td>
                                <td class="text-end pe-4">
                                    <a href="branches?action=view&id=${b.branchId}" class="btn btn-outline-info btn-sm me-1" title="Chi tiết">
                                        <i class="fa fa-eye"></i>
                                    </a>
                                    <a href="branches?action=edit&id=${b.branchId}" class="btn btn-outline-warning btn-sm me-1" title="Sửa">
                                        <i class="fa fa-pencil"></i>
                                    </a>
                                    <button onclick="confirmDelete(${b.branchId})" class="btn btn-outline-danger btn-sm" title="Xóa">
                                        <i class="fa fa-trash"></i>
                                    </button>
                                </td>
                            </tr>
                        </c:forEach>
                        </tbody>
                    </table>
                </div>

                <c:if test="${totalPages > 1}">
                    <nav class="d-flex justify-content-end mt-4">
                        <ul class="pagination">
                            <c:set var="queryParams" value="&keyword=${keyword}&status=${status}" />
                            <c:forEach begin="1" end="${totalPages}" var="i">
                                <li class="page-item ${currentPage == i ? 'active' : ''}">
                                    <a class="page-link" href="branches?page=${i}${queryParams}">${i}</a>
                                </li>
                            </c:forEach>
                        </ul>
                    </nav>
                </c:if>
            </div>
        </div>
    </div>
</main>
<script src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
<script>
    function confirmDelete(id) {
        if(confirm('Xác nhận xóa chi nhánh #' + id + '?')) {
            window.location.href = 'branches?action=delete&id=' + id;
        }
    }
</script>
</body>
</html>