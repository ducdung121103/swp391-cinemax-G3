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
        .filter-bar { display: flex; gap: 12px; flex-wrap: wrap; margin: 16px 0; align-items: center; }
        .filter-input, .filter-select { padding: 8px 12px; border: 1px solid #cbd5e0; border-radius: 6px; }
        .alert { padding: 12px 16px; border-radius: 8px; margin-bottom: 16px; display: flex; align-items: center; gap: 10px; font-size: 14px; }
        .alert-success { background: #c6f6d5; color: #22543d; border: 1px solid #9ae6b4; }
        .alert-danger { background: #fed7d7; color: #742a2a; border: 1px solid #feb2b2; }
        table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
        th, td { padding: 12px 10px; border-bottom: 1px solid #e2e8f0; vertical-align: middle; text-align: left; font-size: 13.5px; }
        th { background: #f8fafc; font-weight: 700; color: #4a5568; }
        td form { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin: 0; }
        .badge-status { padding: 4px 8px; border-radius: 999px; font-weight: 700; font-size: 11px; text-transform: uppercase; }
        .badge-active { background: #c6f6d5; color: #22543d; }
        .badge-locked { background: #fed7d7; color: #742a2a; }
        .badge-unverified { background: #feebc8; color: #744210; }
        .btn-create-user {
            display: inline-flex; align-items: center; gap: 8px;
            padding: 10px 18px; border-radius: 999px;
            background: #e50914; color: #fff; font-weight: 700; font-size: 13.5px;
            border: none; cursor: pointer;
            box-shadow: 0 4px 14px rgba(229, 9, 20, 0.28);
            transition: all 0.2s ease;
        }
        .btn-create-user:hover { background: #b20710; transform: translateY(-1px); }
        @keyframes fadeInModal {
            from { opacity: 0; transform: scale(0.96); }
            to { opacity: 1; transform: scale(1); }
        }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <div class="admin-content">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px; margin-bottom: 20px;">
                <div>
                    <h2 style="margin: 0; font-size: 1.4rem; color: #1a202c;">Quản lý tài khoản nhân sự & người dùng</h2>
                    <p style="margin: 4px 0 0; color: #718096; font-size: 13px;">Khởi tạo nhân viên, quản lý quyền hạn chi nhánh và xóa mềm tài khoản an toàn.</p>
                </div>
                <button type="button" class="btn-create-user" onclick="openCreateUserModal()">
                    <i class="fa-solid fa-user-plus"></i> Thêm nhân sự mới
                </button>
            </div>

            <c:if test="${msg == 'created'}"><div class="alert alert-success"><i class="fa-solid fa-check-circle"></i> Đã tạo tài khoản nhân sự mới thành công!</div></c:if>
            <c:if test="${msg == 'deleted'}"><div class="alert alert-success"><i class="fa-solid fa-check-circle"></i> Đã xóa mềm tài khoản thành công.</div></c:if>
            <c:if test="${msg == 'status_updated'}"><div class="alert alert-success"><i class="fa-solid fa-check-circle"></i> Đã cập nhật trạng thái hoạt động.</div></c:if>
            <c:if test="${msg == 'assigned'}"><div class="alert alert-success"><i class="fa-solid fa-check-circle"></i> Đã gán vai trò và chi nhánh cụm rạp.</div></c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">
                    <i class="fa-solid fa-triangle-exclamation"></i>
                    <c:choose>
                        <c:when test="${error == 'SELF'}">Không thể thao tác hoặc xóa chính tài khoản đang đăng nhập.</c:when>
                        <c:when test="${error == 'LAST_ADMIN'}">Không thể xóa hoặc khóa Quản trị viên (Admin) duy nhất đang hoạt động.</c:when>
                        <c:when test="${error == 'ADMIN'}">Không đổi vai trò của tài khoản ADMIN.</c:when>
                        <c:when test="${error == 'ROLE' || error == 'INVALID_ROLE'}">Chỉ gán hoặc tạo được vai trò STAFF hoặc MANAGER.</c:when>
                        <c:when test="${error == 'CUSTOMER'}">Tài khoản khách không thể gán chi nhánh cụm rạp.</c:when>
                        <c:when test="${error == 'CINEMA' || error == 'CINEMA_REQUIRED'}">Staff và Manager bắt buộc phải thuộc một cụm rạp đang hoạt động.</c:when>
                        <c:when test="${error == 'EMAIL_TAKEN'}">Email này đã được sử dụng bởi một tài khoản khác trong hệ thống!</c:when>
                        <c:when test="${error == 'PHONE_TAKEN'}">Số điện thoại này đã được sử dụng trong hệ thống!</c:when>
                        <c:when test="${error == 'INVALID_EMAIL'}">Định dạng email không hợp lệ!</c:when>
                        <c:when test="${error == 'INVALID_PHONE'}">Số điện thoại không đúng định dạng (10 số, bắt đầu bằng 03, 05, 07, 08, 09)!</c:when>
                        <c:when test="${error == 'INVALID_PASSWORD'}">Mật khẩu phải từ 8 ký tự, gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt!</c:when>
                        <c:when test="${error == 'INVALID_NAME'}">Họ tên nhân viên không được để trống!</c:when>
                        <c:otherwise>Không thực hiện được (${error}).</c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <form method="get" action="${pageContext.request.contextPath}/admin/identity/users" class="filter-bar">
                <input class="filter-input" type="text" name="keyword" value="${keyword}" placeholder="Tên hoặc email..." style="min-width: 220px;" />
                <select class="filter-select" name="role">
                    <option value="">Tất cả vai trò</option>
                    <c:forEach var="roleName" items="${['ADMIN','MANAGER','STAFF','CUSTOMER']}">
                        <option value="${roleName}" ${selectedRole == roleName ? 'selected' : ''}>${roleName}</option>
                    </c:forEach>
                </select>
                <select class="filter-select" name="status">
                    <option value="">Tất cả trạng thái</option>
                    <c:forEach var="statusName" items="${['ACTIVE','LOCKED','UNVERIFIED']}">
                        <option value="${statusName}" ${selectedStatus == statusName ? 'selected' : ''}>${statusName}</option>
                    </c:forEach>
                </select>
                <button class="btn btn-primary" type="submit" style="padding: 8px 16px; border-radius: 6px;">Lọc</button>
            </form>

            <div style="overflow-x: auto;">
                <table class="table">
                    <thead>
                    <tr>
                        <th style="width: 45px;">STT</th>
                        <th>Họ tên</th>
                        <th>Email / SĐT</th>
                        <th>Vai trò</th>
                        <th>Rạp chi nhánh</th>
                        <th>Trạng thái</th>
                        <th>Khóa / Mở</th>
                        <th>Gán chi nhánh</th>
                        <th style="text-align: center; width: 80px;">Xóa</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="user" items="${users}" varStatus="st">
                        <tr>
                            <td>${(page - 1) * pageSize + st.index + 1}</td>
                            <td style="font-weight: 600; color: #1a202c;">${user.fullName}</td>
                            <td>
                                <div>${user.email}</div>
                                <c:if test="${not empty user.phone}"><small style="color: #718096;">${user.phone}</small></c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.role.roleName == 'STAFF' || user.role.roleName == 'MANAGER'}">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/identity/users"
                                              style="display: flex; gap: 6px; align-items: center; flex-wrap: wrap;">
                                            <input type="hidden" name="action" value="assign" />
                                            <input type="hidden" name="userId" value="${user.id}" />
                                            <input type="hidden" name="cinemaId" value="${user.cinemaId}" />
                                            <input type="hidden" name="keyword" value="${keyword}" />
                                            <input type="hidden" name="role" value="${selectedRole}" />
                                            <input type="hidden" name="status" value="${selectedStatus}" />
                                            <input type="hidden" name="page" value="${page}" />
                                            <select name="roleName" style="padding: 4px 8px; border-radius: 6px; border: 1px solid #cbd5e0; font-size: 12px;">
                                                <option value="STAFF" ${user.role.roleName == 'STAFF' ? 'selected' : ''}>STAFF</option>
                                                <option value="MANAGER" ${user.role.roleName == 'MANAGER' ? 'selected' : ''}>MANAGER</option>
                                            </select>
                                            <button class="btn btn-sm btn-primary" type="submit" style="border-radius: 6px; padding: 4px 10px; font-size: 12px;">Lưu</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise>
                                        <span style="font-weight: 700; color: #2d3748;">${user.role.roleName}</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>${empty user.cinema ? '<span style="color:#a0aec0;">—</span>' : user.cinema.name}</td>
                            <td>
                                <span class="badge-status ${user.status == 'ACTIVE' ? 'badge-active' : (user.status == 'LOCKED' ? 'badge-locked' : 'badge-unverified')}">
                                    ${user.status}
                                </span>
                            </td>
                            <td>
                                <c:if test="${user.status == 'ACTIVE' || user.status == 'LOCKED'}">
                                    <form method="post" action="${pageContext.request.contextPath}/admin/identity/users">
                                        <input type="hidden" name="action" value="toggleStatus" />
                                        <input type="hidden" name="userId" value="${user.id}" />
                                        <input type="hidden" name="keyword" value="${keyword}" />
                                        <input type="hidden" name="role" value="${selectedRole}" />
                                        <input type="hidden" name="status" value="${selectedStatus}" />
                                        <input type="hidden" name="page" value="${page}" />
                                        <button class="btn btn-sm btn-outline-secondary" type="submit" style="border-radius: 6px; padding: 4px 10px; font-size: 12px;">
                                            ${user.status == 'ACTIVE' ? '<i class="fa-solid fa-lock"></i> Khóa' : '<i class="fa-solid fa-lock-open"></i> Mở'}
                                        </button>
                                    </form>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${user.role.roleName == 'STAFF' || user.role.roleName == 'MANAGER'}">
                                        <form method="post" action="${pageContext.request.contextPath}/admin/identity/users"
                                              style="display: flex; gap: 6px; align-items: center; flex-wrap: wrap;">
                                            <input type="hidden" name="action" value="assign" />
                                            <input type="hidden" name="userId" value="${user.id}" />
                                            <input type="hidden" name="roleName" value="${user.role.roleName}" />
                                            <input type="hidden" name="keyword" value="${keyword}" />
                                            <input type="hidden" name="role" value="${selectedRole}" />
                                            <input type="hidden" name="status" value="${selectedStatus}" />
                                            <input type="hidden" name="page" value="${page}" />
                                            <select name="cinemaId" required style="padding: 4px 8px; border-radius: 6px; border: 1px solid #cbd5e0; font-size: 12px; max-width: 160px;">
                                                <option value="">Chọn rạp</option>
                                                <c:forEach var="cinema" items="${cinemas}">
                                                    <option value="${cinema.id}" ${user.cinemaId == cinema.id ? 'selected' : ''}>${cinema.name}</option>
                                                </c:forEach>
                                            </select>
                                            <button class="btn btn-sm btn-primary" type="submit" style="border-radius: 6px; padding: 4px 10px; font-size: 12px;">Gán</button>
                                        </form>
                                    </c:when>
                                    <c:otherwise><span style="color: #a0aec0;">—</span></c:otherwise>
                                </c:choose>
                            </td>
                            <td style="text-align: center;">
                                <form method="post" action="${pageContext.request.contextPath}/admin/identity/users"
                                      onsubmit="return confirm('Bạn có chắc chắn muốn xóa tài khoản [${user.fullName} (${user.email})] không? Tài khoản sẽ chuyển sang trạng thái đã xóa.')">
                                    <input type="hidden" name="action" value="delete" />
                                    <input type="hidden" name="userId" value="${user.id}" />
                                    <input type="hidden" name="keyword" value="${keyword}" />
                                    <input type="hidden" name="role" value="${selectedRole}" />
                                    <input type="hidden" name="status" value="${selectedStatus}" />
                                    <input type="hidden" name="page" value="${page}" />
                                    <button type="submit" style="color: #e50914; border: 1px solid #fed7d7; background: #fff5f5; border-radius: 6px; padding: 5px 9px; cursor: pointer; font-size: 12px; font-weight: 600;" title="Xóa mềm">
                                        <i class="fa-solid fa-trash-can"></i> Xóa
                                    </button>
                                </form>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 16px;">
                <div>
                    <c:if test="${page > 1}">
                        <c:url var="prevUrl" value="/admin/identity/users">
                            <c:param name="page" value="${page - 1}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="role" value="${selectedRole}" />
                            <c:param name="status" value="${selectedStatus}" />
                        </c:url>
                        <a href="${prevUrl}" class="btn btn-sm btn-outline-secondary" style="border-radius: 6px; text-decoration: none; padding: 5px 12px;">« Trang trước</a>
                    </c:if>
                </div>
                <span style="color: #718096; font-size: 13px;">Trang <b>${page}</b> / <b>${totalPages}</b></span>
                <div>
                    <c:if test="${page < totalPages}">
                        <c:url var="nextUrl" value="/admin/identity/users">
                            <c:param name="page" value="${page + 1}" />
                            <c:param name="keyword" value="${keyword}" />
                            <c:param name="role" value="${selectedRole}" />
                            <c:param name="status" value="${selectedStatus}" />
                        </c:url>
                        <a href="${nextUrl}" class="btn btn-sm btn-outline-secondary" style="border-radius: 6px; text-decoration: none; padding: 5px 12px;">Trang sau »</a>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Modal Thêm Nhân Sự Mới -->
<div id="createUserModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.55); z-index: 9999; justify-content: center; align-items: center;">
    <div style="background: #fff; width: 100%; max-width: 520px; border-radius: 14px; box-shadow: 0 10px 25px rgba(0,0,0,0.25); overflow: hidden; animation: fadeInModal 0.2s ease-out; margin: 20px;">
        <div style="display: flex; justify-content: space-between; align-items: center; padding: 18px 24px; border-bottom: 1px solid #edf2f7; background: #f8fafc;">
            <h3 style="margin: 0; font-size: 1.15rem; color: #1a202c; display: flex; align-items: center; gap: 8px;">
                <i class="fa-solid fa-user-plus" style="color: #e50914;"></i> Thêm tài khoản nhân sự
            </h3>
            <button type="button" onclick="closeCreateUserModal()" style="border: none; background: transparent; font-size: 22px; color: #a0aec0; cursor: pointer; line-height: 1;">&times;</button>
        </div>
        <form method="post" action="${pageContext.request.contextPath}/admin/identity/users" style="padding: 24px;">
            <input type="hidden" name="action" value="create" />
            <input type="hidden" name="keyword" value="${keyword}" />
            <input type="hidden" name="role" value="${selectedRole}" />
            <input type="hidden" name="status" value="${selectedStatus}" />
            <input type="hidden" name="page" value="${page}" />

            <div style="margin-bottom: 14px;">
                <label style="display: block; font-weight: 600; font-size: 13px; color: #4a5568; margin-bottom: 5px;">Họ và tên <span style="color: #e50914;">*</span></label>
                <input type="text" name="fullName" required placeholder="Ví dụ: Nguyễn Văn A" style="width: 100%; padding: 9px 12px; border: 1px solid #cbd5e0; border-radius: 8px; box-sizing: border-box;" />
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 14px;">
                <div>
                    <label style="display: block; font-weight: 600; font-size: 13px; color: #4a5568; margin-bottom: 5px;">Email đăng nhập <span style="color: #e50914;">*</span></label>
                    <input type="email" name="email" required placeholder="nhanvien@cinema.com" style="width: 100%; padding: 9px 12px; border: 1px solid #cbd5e0; border-radius: 8px; box-sizing: border-box;" />
                </div>
                <div>
                    <label style="display: block; font-weight: 600; font-size: 13px; color: #4a5568; margin-bottom: 5px;">Số điện thoại</label>
                    <input type="tel" name="phone" placeholder="0912345678" pattern="^(0[3|5|7|8|9])[0-9]{8}$" title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 03, 05, 07, 08, 09" style="width: 100%; padding: 9px 12px; border: 1px solid #cbd5e0; border-radius: 8px; box-sizing: border-box;" />
                </div>
            </div>

            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 12px; margin-bottom: 14px;">
                <div>
                    <label style="display: block; font-weight: 600; font-size: 13px; color: #4a5568; margin-bottom: 5px;">Vai trò <span style="color: #e50914;">*</span></label>
                    <select name="roleName" required style="width: 100%; padding: 9px 12px; border: 1px solid #cbd5e0; border-radius: 8px; box-sizing: border-box; background: #fff;">
                        <option value="STAFF">STAFF (Nhân viên)</option>
                        <option value="MANAGER">MANAGER (Quản lý cụm)</option>
                    </select>
                </div>
                <div>
                    <label style="display: block; font-weight: 600; font-size: 13px; color: #4a5568; margin-bottom: 5px;">Cụm rạp chi nhánh <span style="color: #e50914;">*</span></label>
                    <select name="cinemaId" required style="width: 100%; padding: 9px 12px; border: 1px solid #cbd5e0; border-radius: 8px; box-sizing: border-box; background: #fff;">
                        <option value="">-- Chọn cụm rạp --</option>
                        <c:forEach var="c" items="${cinemas}">
                            <option value="${c.id}">${c.name}</option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div style="margin-bottom: 20px;">
                <label style="display: block; font-weight: 600; font-size: 13px; color: #4a5568; margin-bottom: 5px;">Mật khẩu khởi tạo <span style="color: #e50914;">*</span></label>
                <input type="text" name="password" required value="Cinemax@123" placeholder="Tối thiểu 8 ký tự, gồm Hoa, Thường, Số, Ký tự đặc biệt" style="width: 100%; padding: 9px 12px; border: 1px solid #cbd5e0; border-radius: 8px; box-sizing: border-box;" />
                <small style="color: #718096; font-size: 11px; margin-top: 4px; display: block;">Mật khẩu mặc định: <b>Cinemax@123</b> (Đạt chuẩn bảo mật, nhân viên có thể đổi sau).</small>
            </div>

            <div style="display: flex; justify-content: flex-end; gap: 10px; border-top: 1px solid #edf2f7; padding-top: 16px;">
                <button type="button" onclick="closeCreateUserModal()" style="padding: 9px 16px; border: 1px solid #cbd5e0; background: #fff; border-radius: 8px; font-weight: 600; color: #4a5568; cursor: pointer;">Hủy</button>
                <button type="submit" style="padding: 9px 20px; border: none; background: #e50914; border-radius: 8px; font-weight: 700; color: #fff; cursor: pointer; box-shadow: 0 4px 10px rgba(229,9,20,0.3);">
                    <i class="fa-solid fa-check"></i> Tạo tài khoản
                </button>
            </div>
        </form>
    </div>
</div>

<script>
    function openCreateUserModal() {
        var modal = document.getElementById('createUserModal');
        modal.style.display = 'flex';
    }
    function closeCreateUserModal() {
        var modal = document.getElementById('createUserModal');
        modal.style.display = 'none';
    }
    window.addEventListener('click', function(e) {
        var modal = document.getElementById('createUserModal');
        if (e.target === modal) {
            closeCreateUserModal();
        }
    });
</script>
</body>
</html>
