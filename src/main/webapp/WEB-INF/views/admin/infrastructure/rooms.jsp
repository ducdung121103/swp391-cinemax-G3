<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Phòng Chiếu Phim - Cinema Admin</title>
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
        .badge-type-2d { background: #e2e8f0; color: #2d3748; }
        .badge-type-vip { background: #feebc8; color: #7b341e; }
        .badge-type-imax { background: #e9d8fd; color: #553c9a; font-weight: bold; }
        .badge-type-4dx { background: #fed7d7; color: #9b2c2c; font-weight: bold; }
        .badge-status-active { background: #c6f6d5; color: #22543d; }
        .badge-status-maint { background: #feebc8; color: #9c4221; }
        .badge-status-inact { background: #edf2f7; color: #718096; }

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
            max-width: 550px;
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
                            <c:choose>
                                <c:when test="${msg == 'created'}">✅ Đã thêm mới phòng chiếu và tự động sinh ma trận ghế thành công!</c:when>
                                <c:when test="${msg == 'updated'}">✅ Đã cập nhật thông tin phòng chiếu!</c:when>
                                <c:when test="${msg == 'status_updated'}">✅ Đã thay đổi trạng thái vận hành của phòng chiếu!</c:when>
                                <c:when test="${msg == 'deleted'}">✅ Đã đưa phòng chiếu vào trạng thái lưu trữ!</c:when>
                                <c:otherwise>✅ Thao tác thành công!</c:otherwise>
                            </c:choose>
                        </span>
                        <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer;">&times;</button>
                    </div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert-toast alert-error">
                        <span>⚠️ ${error}</span>
                        <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer;">&times;</button>
                    </div>
                </c:if>

                <!-- Thanh công cụ Lọc & Thêm mới -->
                <form method="GET" action="${pageContext.request.contextPath}/admin/infrastructure/rooms" class="filter-bar">
                    <label style="font-weight: 600; font-size: 0.9rem; color: #4a5568;">🏢 Cụm Rạp:</label>
                    <select name="cinemaId" class="filter-select" onchange="this.form.submit()">
                        <option value="ALL">-- Tất cả cụm rạp --</option>
                        <c:forEach items="${cinemas}" var="c">
                            <option value="${c.id}" ${selectedCinemaId == c.id ? 'selected' : ''}>${c.name} (${c.city})</option>
                        </c:forEach>
                    </select>

                    <label style="font-weight: 600; font-size: 0.9rem; color: #4a5568;">Định dạng:</label>
                    <select name="roomType" class="filter-select" onchange="this.form.submit()">
                        <option value="ALL">-- Tất cả định dạng --</option>
                        <option value="STANDARD_2D" ${selectedRoomType == 'STANDARD_2D' ? 'selected' : ''}>STANDARD 2D</option>
                        <option value="VIP_2D" ${selectedRoomType == 'VIP_2D' ? 'selected' : ''}>VIP LUXURY 2D</option>
                        <option value="IMAX_3D" ${selectedRoomType == 'IMAX_3D' ? 'selected' : ''}>IMAX 3D LASER</option>
                        <option value="4DX" ${selectedRoomType == '4DX' ? 'selected' : ''}>4DX DYNAMIC</option>
                    </select>

                    <label style="font-weight: 600; font-size: 0.9rem; color: #4a5568;">Trạng thái:</label>
                    <select name="status" class="filter-select" onchange="this.form.submit()">
                        <option value="ALL">-- Tất cả trạng thái --</option>
                        <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Đang chiếu (ACTIVE)</option>
                        <option value="MAINTENANCE" ${selectedStatus == 'MAINTENANCE' ? 'selected' : ''}>Đang bảo trì (MAINTENANCE)</option>
                        <option value="INACTIVE" ${selectedStatus == 'INACTIVE' ? 'selected' : ''}>Ngưng hoạt động (INACTIVE)</option>
                    </select>

                    <a href="${pageContext.request.contextPath}/admin/infrastructure/rooms" class="btn-secondary" style="background:#718096;">Đặt lại</a>

                    <div style="margin-left: auto;">
                        <button type="button" class="btn-primary" onclick="openCreateModal()">
                            ➕ Thêm Phòng Chiếu
                        </button>
                    </div>
                </form>

                <!-- Bảng danh sách phòng chiếu -->
                <div class="admin-card">
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
                        <h2 style="margin: 0; font-size: 1.4rem;">Danh Sách Phòng Chiếu Phim (M-04.2)</h2>
                        <span style="font-size: 0.85rem; color: #718096;">Tìm thấy: <strong>${rooms.size()}</strong> phòng chiếu</span>
                    </div>

                    <table class="order-summary-table">
                        <thead>
                            <tr>
                                <th style="width: 70px;">ID</th>
                                <th>Cụm Rạp Chi Nhánh</th>
                                <th>Tên Phòng Chiếu</th>
                                <th style="width: 140px;">Định Dạng</th>
                                <th style="width: 120px; text-align: center;">Kích Thước</th>
                                <th style="width: 110px; text-align: center;">Sức Chứa</th>
                                <th style="width: 140px;">Trạng Thái</th>
                                <th style="width: 240px; text-align: center;">Thao Tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${rooms}" var="r">
                                <tr>
                                    <td><strong>#${r.id}</strong></td>
                                    <td>
                                        <strong>${r.cinema.name}</strong>
                                        <div style="font-size: 0.8rem; color: #718096;">${r.cinema.city}</div>
                                    </td>
                                    <td><strong style="color: #2b6cb0;">${r.name}</strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${r.roomType == 'IMAX_3D'}"><span class="badge badge-type-imax">IMAX 3D</span></c:when>
                                            <c:when test="${r.roomType == 'VIP_2D'}"><span class="badge badge-type-vip">VIP 2D</span></c:when>
                                            <c:when test="${r.roomType == '4DX'}"><span class="badge badge-type-4dx">4DX</span></c:when>
                                            <c:otherwise><span class="badge badge-type-2d">STANDARD 2D</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: center; font-family: monospace;">
                                        ${r.totalRows} hàng &times; ${r.totalColumns} cột
                                    </td>
                                    <td style="text-align: center;">
                                        <strong style="color: #e50914;">${r.totalCapacity}</strong> ghế
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${r.status == 'ACTIVE'}">
                                                <span class="badge badge-status-active">● Đang hoạt động</span>
                                            </c:when>
                                            <c:when test="${r.status == 'MAINTENANCE'}">
                                                <span class="badge badge-status-maint">● Đang bảo trì</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-status-inact">● Tạm ngưng</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="text-align: center;">
                                        <a href="${pageContext.request.contextPath}/admin/infrastructure/seats?roomId=${r.id}" 
                                           class="btn-sm btn-primary" style="background: #2b6cb0;" title="Thiết kế và xem ma trận ghế">
                                            💺 Sơ Đồ Ghế
                                        </a>

                                        <button type="button" class="btn-sm btn-secondary" 
                                                onclick="openEditModal('${r.id}', '${r.name}', '${r.roomType}', '${r.status}')">
                                            ✏️ Sửa
                                        </button>

                                        <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/rooms" style="display:inline;">
                                            <input type="hidden" name="action" value="changeStatus">
                                            <input type="hidden" name="id" value="${r.id}">
                                            <c:choose>
                                                <c:when test="${r.status == 'ACTIVE'}">
                                                    <input type="hidden" name="status" value="MAINTENANCE">
                                                    <button type="submit" class="btn-sm btn-secondary" style="background: #dd6b20;" title="Chuyển sang bảo trì phòng">
                                                        🔧 Bảo Trì
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <input type="hidden" name="status" value="ACTIVE">
                                                    <button type="submit" class="btn-sm btn-secondary" style="background: #38a169;" title="Mở lại phòng chiếu">
                                                        ▶️ Mở Lại
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty rooms}">
                                <tr>
                                    <td colspan="8" style="text-align: center; color: #a0aec0; padding: 30px;">
                                        Không có phòng chiếu nào phù hợp với bộ lọc!
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>

            </div>
        </div>
    </div>

    <!-- MODAL THÊM PHÒNG CHIẾU -->
    <div id="createModal" class="modal-overlay">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Thêm Phòng Chiếu Mới</h3>
                <button type="button" class="close-modal" onclick="closeCreateModal()">&times;</button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/rooms">
                <input type="hidden" name="action" value="create">

                <div class="form-group">
                    <label>Chi Nhánh Cụm Rạp (*):</label>
                    <select name="cinemaId" class="form-control" required>
                        <c:forEach items="${cinemas}" var="c">
                            <option value="${c.id}" ${selectedCinemaId == c.id ? 'selected' : ''}>${c.name} (${c.city})</option>
                        </c:forEach>
                    </select>
                </div>

                <div class="form-group">
                    <label>Tên Phòng Chiếu (*):</label>
                    <input type="text" name="name" class="form-control" required placeholder="VD: Cinema Room 03 (2D Digital), IMAX Laser Room 01">
                </div>

                <div class="form-group">
                    <label>Định Dạng Phòng Chiếu (*):</label>
                    <select name="roomType" class="form-control" required>
                        <option value="STANDARD_2D">STANDARD 2D (Tiêu chuẩn kỹ thuật số)</option>
                        <option value="VIP_2D">VIP 2D (Ghế da cao cấp, phục vụ tại chỗ)</option>
                        <option value="IMAX_3D">IMAX 3D LASER (Màn chiếu cực đại, âm thanh 12 kênh)</option>
                        <option value="4DX">4DX DYNAMIC (Ghế chuyển động & hiệu ứng môi trường)</option>
                    </select>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Số Hàng Ghế (Rows A-Z):</label>
                        <input type="number" name="totalRows" class="form-control" value="10" min="4" max="26" required>
                        <small style="color: #718096;">Tối đa 26 hàng (A đến Z)</small>
                    </div>
                    <div class="form-group">
                        <label>Số Cột Ghế (Cols 1-N):</label>
                        <input type="number" name="totalColumns" class="form-control" value="10" min="4" max="30" required>
                        <small style="color: #718096;">Tối đa 30 ghế mỗi hàng</small>
                    </div>
                </div>

                <div class="form-group">
                    <label>Trạng Thái Ban Đầu:</label>
                    <select name="status" class="form-control">
                        <option value="ACTIVE">Hoạt động ngay (ACTIVE)</option>
                        <option value="MAINTENANCE">Đang lắp đặt / Bảo trì (MAINTENANCE)</option>
                        <option value="INACTIVE">Tạm ngưng (INACTIVE)</option>
                    </select>
                </div>

                <div style="background: #f7fafc; padding: 12px; border-radius: 6px; font-size: 0.85rem; color: #4a5568; margin-bottom: 15px; border-left: 3px solid #3182ce;">
                    💡 <strong>Tự động sinh sơ đồ:</strong> Hệ thống sẽ tự động khởi tạo ma trận ghế ban đầu (Hàng 1..3: Standard, Hàng 4..8: VIP, Hàng 9..10: Couple). Bạn có thể tùy chỉnh lại ở màn hình <strong>Thiết kế Sơ đồ Ghế</strong> sau khi tạo.
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                    <button type="button" class="btn-secondary" onclick="closeCreateModal()">Hủy bỏ</button>
                    <button type="submit" class="btn-primary">💾 Khởi Tạo Phòng Chiếu</button>
                </div>
            </form>
        </div>
    </div>

    <!-- MODAL SỬA PHÒNG CHIẾU -->
    <div id="editModal" class="modal-overlay">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Cập Nhật Phòng Chiếu</h3>
                <button type="button" class="close-modal" onclick="closeEditModal()">&times;</button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/rooms">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="editRoomId" name="id" value="">

                <div class="form-group">
                    <label>Tên Phòng Chiếu (*):</label>
                    <input type="text" id="editRoomName" name="name" class="form-control" required>
                </div>

                <div class="form-group">
                    <label>Định Dạng Phòng Chiếu (*):</label>
                    <select id="editRoomType" name="roomType" class="form-control" required>
                        <option value="STANDARD_2D">STANDARD 2D</option>
                        <option value="VIP_2D">VIP 2D</option>
                        <option value="IMAX_3D">IMAX 3D</option>
                        <option value="4DX">4DX</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>Trạng Thái:</label>
                    <select id="editRoomStatus" name="status" class="form-control" required>
                        <option value="ACTIVE">Hoạt động (ACTIVE)</option>
                        <option value="MAINTENANCE">Bảo trì (MAINTENANCE)</option>
                        <option value="INACTIVE">Ngưng hoạt động (INACTIVE)</option>
                    </select>
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
        function openEditModal(id, name, type, status) {
            document.getElementById('editRoomId').value = id;
            document.getElementById('editRoomName').value = name;
            document.getElementById('editRoomType').value = type;
            document.getElementById('editRoomStatus').value = status;
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
