<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Cụm Rạp Chi Nhánh - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .filter-bar {
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
            margin-bottom: 20px;
            background: #fff;
            padding: 16px 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }
        .filter-input {
            padding: 8px 12px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.9rem;
            min-width: 220px;
        }
        .filter-select {
            padding: 8px 12px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.9rem;
            background-color: #fff;
        }
        .btn-primary {
            background: #e50914;
            color: #fff;
            padding: 9px 18px;
            border: none;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: background 0.2s;
        }
        .btn-primary:hover { background: #b20710; }
        .btn-secondary {
            background: #4a5568;
            color: #fff;
            padding: 8px 14px;
            border: none;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .btn-secondary:hover { background: #2d3748; }
        .btn-sm {
            padding: 5px 10px;
            font-size: 0.8rem;
            border-radius: 4px;
            cursor: pointer;
            border: none;
            text-decoration: none;
            display: inline-block;
        }
        .badge {
            padding: 4px 10px;
            border-radius: 20px;
            font-size: 0.8rem;
            font-weight: 600;
            display: inline-block;
        }
        .badge-success { background: #c6f6d5; color: #22543d; }
        .badge-danger { background: #fed7d7; color: #742a2a; }
        .badge-code { background: #edf2f7; color: #2d3748; font-family: monospace; font-size: 0.9rem; border: 1px solid #e2e8f0; }

        /* Modal styling */
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0, 0, 0, 0.55);
            z-index: 999;
            align-items: center;
            justify-content: center;
        }
        .modal-overlay.active { display: flex; }
        .modal-content {
            background: #fff;
            width: 100%;
            max-width: 580px;
            border-radius: 10px;
            padding: 24px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
            max-height: 90vh;
            overflow-y: auto;
        }
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 18px;
            border-bottom: 1px solid #e2e8f0;
            padding-bottom: 12px;
        }
        .modal-header h3 { margin: 0; color: #1a202c; font-size: 1.3rem; }
        .close-modal { font-size: 1.5rem; cursor: pointer; color: #a0aec0; border: none; background: none; }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 0.85rem; color: #4a5568; }
        .form-control {
            width: 100%;
            padding: 9px 12px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.9rem;
            box-sizing: border-box;
        }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
        .alert-toast {
            padding: 12px 18px;
            border-radius: 6px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .alert-success { background: #c6f6d5; color: #22543d; border-left: 5px solid #38a169; }
        .alert-error { background: #fed7d7; color: #742a2a; border-left: 5px solid #e53e3e; }
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../../common/navbar.jsp" />
            <div class="admin-content">

                <!-- Alert Message -->
                <c:if test="${not empty msg}">
                    <div class="alert-toast alert-success">
                        <span>
                            <i class="fa-solid fa-circle-check" style="margin-right: 6px;"></i>
                            <c:choose>
                                <c:when test="${msg == 'created'}">Đã thêm mới cụm rạp thành công!</c:when>
                                <c:when test="${msg == 'updated'}">Đã cập nhật thông tin cụm rạp thành công!</c:when>
                                <c:when test="${msg == 'status_updated'}">Đã thay đổi trạng thái hoạt động của rạp!</c:when>
                                <c:when test="${msg == 'deleted'}">Đã xóa cụm rạp khỏi hệ thống!</c:when>
                                <c:otherwise>Thao tác thành công!</c:otherwise>
                            </c:choose>
                        </span>
                        <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem;">&times;</button>
                    </div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert-toast alert-error">
                        <span>
                            <i class="fa-solid fa-triangle-exclamation" style="margin-right: 6px;"></i>
                            ${error}
                        </span>
                        <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem;">&times;</button>
                    </div>
                </c:if>

                <!-- Thanh công cụ Tìm kiếm, Lọc và Thêm mới -->
                <form method="GET" action="${pageContext.request.contextPath}/admin/infrastructure/cinemas" class="filter-bar">
                    <input type="text" name="keyword" class="filter-input" placeholder="Tìm theo tên rạp, mã rạp, địa chỉ..." value="${keyword}">
                    
                    <select name="city" class="filter-select">
                        <option value="ALL">-- Tất cả thành phố --</option>
                        <c:forEach items="${cities}" var="ct">
                            <option value="${ct}" ${selectedCity == ct ? 'selected' : ''}>${ct}</option>
                        </c:forEach>
                    </select>

                    <select name="status" class="filter-select">
                        <option value="ALL">-- Tất cả trạng thái --</option>
                        <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Đang hoạt động</option>
                        <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>Tạm ngưng/Đóng cửa</option>
                    </select>

                    <button type="submit" class="btn-secondary">
                        <i class="fa-solid fa-filter"></i> Lọc
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/infrastructure/cinemas" class="btn-secondary" style="background:#718096;">
                        <i class="fa-solid fa-rotate-left"></i> Đặt lại
                    </a>

                    <div style="margin-left: auto;">
                        <button type="button" class="btn-primary" onclick="openCreateModal()">
                            <i class="fa-solid fa-plus"></i> Thêm Cụm Rạp Mới
                        </button>
                    </div>
                </form>

                <!-- Danh sách cụm rạp -->
                <div class="admin-card">
                    <div class="admin-card-header">
                        <h3 class="admin-card-title">
                            <i class="fa-solid fa-building"></i>
                            Quản lý Chi Nhánh Cụm Rạp (M-04.1)
                        </h3>
                        <span style="font-size: 0.85rem; color: #64748b; font-weight: 600;">Tổng số: <strong>${cinemas.size()}</strong> cụm rạp</span>
                    </div>

                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="width: 100px;">Mã Rạp</th>
                                    <th>Tên Cụm Rạp</th>
                                    <th style="width: 130px;">Thành Phố</th>
                                    <th>Địa Chỉ</th>
                                    <th style="width: 140px;">Hotline</th>
                                    <th style="width: 120px;">Phòng Chiếu</th>
                                    <th style="width: 140px;">Trạng Thái</th>
                                    <th style="width: 190px; text-align: center;">Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${cinemas}" var="c">
                                    <tr>
                                        <td><span class="badge-code">${c.cinemaCode}</span></td>
                                        <td><strong style="color: #0f172a; font-size: 0.95rem;">${c.name}</strong></td>
                                        <td>
                                            <span class="badge-city">
                                                <i class="fa-solid fa-location-dot" style="margin-right: 4px; color: #94a3b8;"></i>${c.city}
                                            </span>
                                        </td>
                                        <td style="font-size: 0.88rem; color: #475569;">${c.address}</td>
                                        <td style="font-size: 0.88rem; font-family: ui-monospace, monospace; font-weight: 600; color: #334155;">
                                            <i class="fa-solid fa-phone" style="font-size: 0.75rem; color: #94a3b8; margin-right: 4px;"></i>${c.phone}
                                        </td>
                                        <td>
                                            <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms?cinemaId=${c.id}" 
                                               class="badge-rooms" style="text-decoration: none;" title="Xem các phòng chiếu của rạp này">
                                                <i class="fa-solid fa-door-open"></i> ${c.totalRooms} phòng
                                            </a>
                                        </td>
                                        <td>
                                            <span class="status-indicator ${c.isActive ? 'status-active' : 'status-inactive'}">
                                                ${c.isActive ? 'Đang hoạt động' : 'Tạm đóng cửa'}
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <button type="button" class="btn-sm btn-secondary" 
                                                    onclick="openEditModal('${c.id}', '${c.cinemaCode}', '${c.name}', '${c.city}', '${c.address}', '${c.phone}', '${c.email}', ${c.isActive})">
                                                <i class="fa-solid fa-pen-to-square"></i> Sửa
                                            </button>

                                            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/cinemas" style="display:inline;" onsubmit="return confirm('Bạn có chắc muốn đổi trạng thái rạp này?')">
                                                <input type="hidden" name="action" value="toggleStatus">
                                                <input type="hidden" name="id" value="${c.id}">
                                                <input type="hidden" name="currentActive" value="${c.isActive}">
                                                <button type="submit" class="btn-sm ${c.isActive ? 'btn-secondary' : 'btn-primary'}" style="${c.isActive ? 'background:#ef4444;' : 'background:#10b981;'}">
                                                    <i class="fa-solid ${c.isActive ? 'fa-lock' : 'fa-lock-open'}"></i> ${c.isActive ? 'Khóa' : 'Mở'}
                                                </button>
                                            </form>

                                            <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms?cinemaId=${c.id}" class="btn-sm btn-secondary" style="background:#0284c7;" title="Xem phòng">
                                                <i class="fa-solid fa-video"></i> Phòng
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty cinemas}">
                                    <tr>
                                        <td colspan="8" style="text-align: center; color: #a0aec0; padding: 30px;">
                                            Không tìm thấy cụm rạp nào phù hợp với bộ lọc!
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <!-- MODAL THÊM CỤM RẠP MỚI -->
    <div id="createModal" class="modal-overlay">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Thêm Cụm Rạp Mới</h3>
                <button type="button" class="close-modal" onclick="closeCreateModal()">&times;</button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/cinemas">
                <input type="hidden" name="action" value="create">
                
                <div class="form-row">
                    <div class="form-group">
                        <label>Mã Rạp (*):</label>
                        <input type="text" name="cinemaCode" class="form-control" required placeholder="VD: C-HN03, C-DN01" pattern="[A-Za-z0-9\-_]{3,20}">
                    </div>
                    <div class="form-group">
                        <label>Thành Phố (*):</label>
                        <input type="text" name="city" class="form-control" required placeholder="VD: Hà Nội, TP.HCM, Đà Nẵng">
                    </div>
                </div>

                <div class="form-group">
                    <label>Tên Cụm Rạp (*):</label>
                    <input type="text" name="name" class="form-control" required placeholder="VD: CineMax Landmark 81">
                </div>

                <div class="form-group">
                    <label>Địa Chỉ Chi Tiết (*):</label>
                    <input type="text" name="address" class="form-control" required placeholder="VD: Tầng B1, TTTM Landmark 81, 720A Điện Biên Phủ, P.22, Bình Thạnh">
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Hotline Hỗ Trợ:</label>
                        <input type="text" name="phone" class="form-control" placeholder="VD: 028 7300 8888">
                    </div>
                    <div class="form-group">
                        <label>Email Liên Hệ:</label>
                        <input type="email" name="email" class="form-control" placeholder="VD: c-sg02@cinemax.vn">
                    </div>
                </div>

                <div class="form-group" style="display: flex; align-items: center; gap: 8px; margin-top: 10px;">
                    <input type="checkbox" id="createIsActive" name="isActive" value="true" checked style="width: 18px; height: 18px;">
                    <label for="createIsActive" style="margin-bottom: 0; cursor: pointer;">Kích hoạt rạp hoạt động ngay</label>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                    <button type="button" class="btn-secondary" onclick="closeCreateModal()">Hủy bỏ</button>
                    <button type="submit" class="btn-primary">💾 Lưu Cụm Rạp</button>
                </div>
            </form>
        </div>
    </div>

    <!-- MODAL SỬA CỤM RẠP -->
    <div id="editModal" class="modal-overlay">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Cập Nhật Thông Tin Cụm Rạp</h3>
                <button type="button" class="close-modal" onclick="closeEditModal()">&times;</button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/cinemas">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="editId" name="id" value="">
                
                <div class="form-row">
                    <div class="form-group">
                        <label>Mã Rạp (*):</label>
                        <input type="text" id="editCode" name="cinemaCode" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>Thành Phố (*):</label>
                        <input type="text" id="editCity" name="city" class="form-control" required>
                    </div>
                </div>

                <div class="form-group">
                    <label>Tên Cụm Rạp (*):</label>
                    <input type="text" id="editName" name="name" class="form-control" required>
                </div>

                <div class="form-group">
                    <label>Địa Chỉ Chi Tiết (*):</label>
                    <input type="text" id="editAddress" name="address" class="form-control" required>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Hotline Hỗ Trợ:</label>
                        <input type="text" id="editPhone" name="phone" class="form-control">
                    </div>
                    <div class="form-group">
                        <label>Email Liên Hệ:</label>
                        <input type="email" id="editEmail" name="email" class="form-control">
                    </div>
                </div>

                <div class="form-group" style="display: flex; align-items: center; gap: 8px; margin-top: 10px;">
                    <input type="checkbox" id="editIsActive" name="isActive" value="true" style="width: 18px; height: 18px;">
                    <label for="editIsActive" style="margin-bottom: 0; cursor: pointer;">Trạng thái hoạt động</label>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                    <button type="button" class="btn-secondary" onclick="closeEditModal()">Hủy bỏ</button>
                    <button type="submit" class="btn-primary">💾 Cập Nhật</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openCreateModal() {
            document.getElementById('createModal').classList.add('active');
        }
        function closeCreateModal() {
            document.getElementById('createModal').classList.remove('active');
        }
        function openEditModal(id, code, name, city, address, phone, email, isActive) {
            document.getElementById('editId').value = id;
            document.getElementById('editCode').value = code;
            document.getElementById('editName').value = name;
            document.getElementById('editCity').value = city;
            document.getElementById('editAddress').value = address;
            document.getElementById('editPhone').value = phone;
            document.getElementById('editEmail').value = email;
            document.getElementById('editIsActive').checked = (isActive === true || isActive === 'true');
            document.getElementById('editModal').classList.add('active');
        }
        function closeEditModal() {
            document.getElementById('editModal').classList.remove('active');
        }
        window.onclick = function(event) {
            if (event.target.classList.contains('modal-overlay')) {
                event.target.classList.remove('active');
            }
        }
    </script>
</body>
</html>
