<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản Lý Bảo Trì Ghế Phòng Chiếu - CineMax Administration</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .maint-container {
            display: grid;
            grid-template-columns: 1fr 340px;
            gap: 24px;
            align-items: start;
        }
        @media (max-width: 1024px) {
            .maint-container {
                grid-template-columns: 1fr;
            }
        }
        .seat-matrix-card {
            background: #fff;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            padding: 24px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
        }
        .screen-bar {
            background: linear-gradient(180deg, #3b82f6 0%, rgba(59, 130, 246, 0.1) 100%);
            color: #1e3a8a;
            font-weight: 800;
            font-size: 0.85rem;
            letter-spacing: 2px;
            text-align: center;
            padding: 8px 0;
            border-radius: 6px 6px 0 0;
            border-top: 3px solid #1d4ed8;
            margin-bottom: 28px;
            box-shadow: 0 4px 12px rgba(59, 130, 246, 0.15);
        }
        .seat-grid-scroll {
            overflow-x: auto;
            padding: 10px 0 20px 0;
            display: flex;
            flex-direction: column;
            gap: 8px;
            align-items: center;
        }
        .seat-grid-row {
            display: flex;
            gap: 6px;
            align-items: center;
        }
        .row-label {
            width: 24px;
            font-weight: 800;
            color: #64748b;
            font-size: 0.85rem;
            text-align: center;
        }
        .seat-node {
            width: 38px;
            height: 38px;
            border-radius: 6px;
            border: 1px solid #cbd5e1;
            background: #f8fafc;
            color: #334155;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            font-size: 0.72rem;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.15s ease-in-out;
            user-select: none;
            position: relative;
        }
        .seat-node:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
        }
        .seat-node.active-seat {
            background: #f0fdf4;
            border-color: #86efac;
            color: #166534;
        }
        .seat-node.maint-seat {
            background: #fef2f2;
            border-color: #fca5a5;
            color: #991b1b;
        }
        .seat-node.selected-seat {
            border: 2px solid #e50914 !important;
            background: #fef08a !important;
            color: #713f12 !important;
            box-shadow: 0 0 0 3px rgba(229, 9, 20, 0.2);
            transform: scale(1.05);
        }
        .action-panel {
            background: #fff;
            border-radius: 10px;
            border: 1px solid #e2e8f0;
            padding: 20px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.02);
        }
        .badge-legend {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 0.8rem;
            color: #475569;
        }
        .legend-dot {
            width: 14px;
            height: 14px;
            border-radius: 3px;
            display: inline-block;
        }
    </style>
</head>
<body class="admin-body">
<div class="admin-wrapper">
    <%-- Sidebar chung --%>
    <jsp:include page="/WEB-INF/views/common/sidebar.jsp" />

    <div class="admin-main">
        <%-- Topbar --%>
        <header class="admin-navbar">
            <div class="navbar-left">
                <button class="btn-sidebar-toggle" id="sidebarToggle"><i class="fa-solid fa-bars"></i></button>
                <div class="breadcrumb">
                    <span>Quản Trị</span> <i class="fa-solid fa-chevron-right"></i>
                    <span>Hạ Tầng & Hệ Thống</span> <i class="fa-solid fa-chevron-right"></i>
                    <span class="active">Quản Lý Bảo Trì Ghế (M-04.4)</span>
                </div>
            </div>
            <div class="navbar-right">
                <a href="${pageContext.request.contextPath}/admin/infrastructure/seats?roomId=${selectedRoomId}" class="btn-back-dashboard" style="background: #f1f5f9; color: #475569; text-decoration: none; padding: 6px 14px; border-radius: 6px; font-weight: 600; font-size: 0.85rem; border: 1px solid #cbd5e1;">
                    <i class="fa-solid fa-couch"></i> Mở Màn Thiết Kế Sơ Đồ
                </a>
            </div>
        </header>

        <%-- Nội dung chính --%>
        <div class="admin-content">

            <%-- Flash Notifications --%>
            <c:if test="${not empty error}">
                <div class="alert alert-danger" style="background:#fee2e2; color:#b91c1c; padding:12px 16px; border-radius:6px; margin-bottom:16px;">
                    <i class="fa-solid fa-circle-exclamation"></i> <c:out value="${error}" />
                </div>
            </c:if>
            <c:if test="${not empty msg}">
                <div class="alert alert-success" style="background:#dcfce7; color:#15803d; padding:12px 16px; border-radius:6px; margin-bottom:16px;">
                    <i class="fa-solid fa-circle-check"></i>
                    <c:choose>
                        <c:when test="${msg == 'maintenance_applied'}">Đã chuyển danh sách ghế được chọn sang trạng thái BẢO TRÌ thành công!</c:when>
                        <c:when test="${msg == 'maintenance_released'}">Đã mở lại danh sách ghế được chọn về trạng thái HOẠT ĐỘNG thành công!</c:when>
                        <c:when test="${msg == 'all_restored'}">Đã khôi phục TOÀN BỘ ghế bảo trì trong phòng về trạng thái HOẠT ĐỘNG!</c:when>
                        <c:otherwise><c:out value="${msg}" /></c:otherwise>
                    </c:choose>
                </div>
            </c:if>

            <%-- Thanh chọn phòng chiếu --%>
            <div class="filter-toolbar" style="background: #fff; padding: 16px 20px; border-radius: 8px; border: 1px solid #e2e8f0; display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 12px; margin-bottom: 20px;">
                <form action="${pageContext.request.contextPath}/admin/infrastructure/seat-maintenance" method="GET" style="display: flex; align-items: center; gap: 12px; flex-wrap: wrap;">
                    <label style="font-weight: 700; font-size: 0.85rem; color: #475569;">
                        <i class="fa-solid fa-video" style="color: #e50914;"></i> CHỌN PHÒNG CHIẾU:
                    </label>
                    <select name="roomId" class="filter-select" onchange="this.form.submit()" style="padding: 8px 14px; border: 1px solid #cbd5e1; border-radius: 6px; font-weight: 600; min-width: 280px;">
                        <c:forEach items="${allRooms}" var="r">
                            <option value="${r.id}" ${r.id == selectedRoomId ? 'selected' : ''}>
                                ${r.name} - ${r.cinema.name} (${r.roomType})
                            </option>
                        </c:forEach>
                    </select>
                </form>

                <div style="font-size: 0.85rem; color: #64748b;">
                    Phòng hiện tại: <strong style="color: #0f172a;">${currentRoom.name}</strong> (${currentRoom.cinema.name})
                </div>
            </div>

            <%-- 3 Thẻ KPI Thống Kê Nhanh --%>
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(260px, 1fr)); gap: 16px; margin-bottom: 24px;">
                <div class="kpi-card-luxury" style="background:#fff; border-radius:10px; padding:16px 18px; border:1px solid #e2e8f0; display:flex; align-items:center; gap:14px;">
                    <div style="width:46px; height:46px; border-radius:10px; background:linear-gradient(135deg, #475569, #1e293b); color:#fff; display:flex; align-items:center; justify-content:center; font-size:1.25rem;">
                        <i class="fa-solid fa-couch"></i>
                    </div>
                    <div>
                        <h3 style="margin:0; font-size:1.3rem; font-weight:800; color:#0f172a;">${totalSeats}</h3>
                        <p style="margin:2px 0 0 0; font-size:0.75rem; color:#64748b; font-weight:700; text-transform:uppercase;">Tổng Sức Chứa Phòng</p>
                    </div>
                </div>

                <div class="kpi-card-luxury" style="background:#fff; border-radius:10px; padding:16px 18px; border:1px solid #e2e8f0; display:flex; align-items:center; gap:14px;">
                    <div style="width:46px; height:46px; border-radius:10px; background:linear-gradient(135deg, #10b981, #059669); color:#fff; display:flex; align-items:center; justify-content:center; font-size:1.25rem;">
                        <i class="fa-solid fa-circle-check"></i>
                    </div>
                    <div>
                        <h3 style="margin:0; font-size:1.3rem; font-weight:800; color:#15803d;">${activeCount}</h3>
                        <p style="margin:2px 0 0 0; font-size:0.75rem; color:#64748b; font-weight:700; text-transform:uppercase;">Ghế Đang Hoạt Động (Bán Vé)</p>
                    </div>
                </div>

                <div class="kpi-card-luxury" style="background:#fff; border-radius:10px; padding:16px 18px; border:1px solid #e2e8f0; display:flex; align-items:center; gap:14px;">
                    <div style="width:46px; height:46px; border-radius:10px; background:linear-gradient(135deg, #f59e0b, #d97706); color:#fff; display:flex; align-items:center; justify-content:center; font-size:1.25rem;">
                        <i class="fa-solid fa-wrench"></i>
                    </div>
                    <div>
                        <h3 style="margin:0; font-size:1.3rem; font-weight:800; color:#b45309;">${maintenanceCount}</h3>
                        <p style="margin:2px 0 0 0; font-size:0.75rem; color:#64748b; font-weight:700; text-transform:uppercase;">Ghế Đang Bảo Trì / Tạm Khóa</p>
                    </div>
                </div>
            </div>

            <%-- Bố cục 2 Cột: Ma trận ghế & Bảng điều khiển bảo trì --%>
            <div class="maint-container">
                <%-- Cột 1: Ma trận ghế --%>
                <div class="seat-matrix-card">
                    <div class="screen-bar">MÀN HÌNH CHIẾU PHÒNG (${currentRoom.name})</div>

                    <div class="seat-grid-scroll">
                        <c:choose>
                            <c:when test="${not empty seats}">
                                <%-- Nhóm theo hàng ghế --%>
                                <c:set var="lastRow" value="" />
                                <c:forEach items="${seats}" var="s" varStatus="loop">
                                    <c:if test="${s.seatRow != lastRow}">
                                        <c:if test="${not empty lastRow}">
                                            </div>
                                        </c:if>
                                        <div class="seat-grid-row">
                                            <div class="row-label">${s.seatRow}</div>
                                        <c:set var="lastRow" value="${s.seatRow}" />
                                    </c:if>

                                    <c:set var="isMaint" value="${s.status == 'MAINTENANCE' || s.isActive == false}" />
                                    <div class="seat-node ${isMaint ? 'maint-seat' : 'active-seat'}" 
                                         data-seat-id="${s.id}" 
                                         data-seat-code="${s.seatCode}" 
                                         data-is-maint="${isMaint}" 
                                         onclick="toggleSeatSelection(this)">
                                        <i class="fa-solid ${isMaint ? 'fa-wrench' : 'fa-couch'}" style="font-size: 0.65rem; margin-bottom: 2px;"></i>
                                        <span>${s.seatNumber}</span>
                                    </div>

                                    <c:if test="${loop.last}">
                                        </div>
                                    </c:if>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div style="padding: 40px; text-align: center; color: #94a3b8;">
                                    <i class="fa-solid fa-couch" style="font-size: 2.5rem; margin-bottom: 12px; display: block;"></i>
                                    Phòng chiếu này chưa có ma trận ghế. Vui lòng vào màn Thiết Kế Sơ Đồ để tạo ghế!
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <%-- Chú giải màu sắc --%>
                    <div style="display: flex; gap: 20px; justify-content: center; margin-top: 24px; padding-top: 16px; border-top: 1px solid #e2e8f0; flex-wrap: wrap;">
                        <div class="badge-legend">
                            <span class="legend-dot" style="background:#f0fdf4; border:1px solid #86efac;"></span> Ghế Hoạt Động (Sẵn sàng bán)
                        </div>
                        <div class="badge-legend">
                            <span class="legend-dot" style="background:#fef2f2; border:1px solid #fca5a5;"></span> Ghế Bảo Trì / Hỏng (Khóa bán)
                        </div>
                        <div class="badge-legend">
                            <span class="legend-dot" style="background:#fef08a; border:2px solid #e50914;"></span> Ghế Đang Chọn
                        </div>
                    </div>
                </div>

                <%-- Cột 2: Bảng Điều Khiển Tác Vụ Bảo Trì --%>
                <div class="action-panel">
                    <h3 style="margin: 0 0 16px 0; font-size: 1.05rem; color: #1e293b; display: flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-sliders" style="color: #e50914;"></i> Bảng Tác Vụ Bảo Trì
                    </h3>

                    <div style="background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px; margin-bottom: 18px;">
                        <div style="font-size: 0.8rem; color: #64748b; margin-bottom: 4px;">Trạng thái lựa chọn:</div>
                        <div style="font-size: 1.1rem; font-weight: 800; color: #0f172a;" id="selectedCountText">
                            Đang chọn: 0 ghế
                        </div>
                        <div style="font-size: 0.75rem; color: #64748b; margin-top: 4px;" id="selectedCodesText">
                            (Click vào từng ghế trên sơ đồ để chọn)
                        </div>
                    </div>

                    <%-- Thao tác 1: Chuyển sang bảo trì --%>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seat-maintenance" style="margin-bottom: 10px;">
                        <input type="hidden" name="action" value="toggleMaintenance">
                        <input type="hidden" name="roomId" value="${selectedRoomId}">
                        <input type="hidden" name="underMaintenance" value="true">
                        <input type="hidden" name="seatIds" class="hidden-seat-ids" value="">
                        <button type="submit" class="btn-maint-submit" id="btnSetMaint" disabled
                                style="width: 100%; padding: 12px; border-radius: 6px; font-weight: 700; font-size: 0.85rem; background: #ea580c; color: #fff; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px;">
                            <i class="fa-solid fa-wrench"></i> Khóa Ghế Sang BẢO TRÌ
                        </button>
                    </form>

                    <%-- Thao tác 2: Mở lại ghế --%>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seat-maintenance" style="margin-bottom: 18px;">
                        <input type="hidden" name="action" value="toggleMaintenance">
                        <input type="hidden" name="roomId" value="${selectedRoomId}">
                        <input type="hidden" name="underMaintenance" value="false">
                        <input type="hidden" name="seatIds" class="hidden-seat-ids" value="">
                        <button type="submit" class="btn-maint-submit" id="btnReleaseMaint" disabled
                                style="width: 100%; padding: 12px; border-radius: 6px; font-weight: 700; font-size: 0.85rem; background: #16a34a; color: #fff; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px;">
                            <i class="fa-solid fa-circle-check"></i> Mở Lại Ghế HOẠT ĐỘNG
                        </button>
                    </form>

                    <hr style="border: 0; border-top: 1px dashed #cbd5e1; margin: 18px 0;">

                    <%-- Thao tác 3: Khôi phục toàn bộ phòng --%>
                    <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seat-maintenance"
                          onsubmit="return confirm('Bạn có chắc chắn muốn mở lại TOÀN BỘ ghế bảo trì trong phòng này về trạng thái HOẠT ĐỘNG?')" style="margin-bottom: 12px;">
                        <input type="hidden" name="action" value="restoreAll">
                        <input type="hidden" name="roomId" value="${selectedRoomId}">
                        <button type="submit"
                                style="width: 100%; padding: 10px; border-radius: 6px; font-weight: 700; font-size: 0.82rem; background: #475569; color: #fff; border: none; cursor: pointer; display: flex; align-items: center; justify-content: center; gap: 8px;">
                            <i class="fa-solid fa-arrows-rotate"></i> Khôi Phục Toàn Phòng
                        </button>
                    </form>

                    <button type="button" onclick="clearSeatSelection()"
                            style="width: 100%; padding: 8px; border-radius: 6px; font-weight: 600; font-size: 0.8rem; background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; cursor: pointer;">
                        <i class="fa-solid fa-xmark"></i> Bỏ chọn tất cả ghế
                    </button>
                </div>
            </div>

        </div>
    </div>
</div>

<script>
    const selectedSeatIds = new Set();
    const selectedSeatCodes = [];

    function toggleSeatSelection(el) {
        const seatId = el.getAttribute('data-seat-id');
        const seatCode = el.getAttribute('data-seat-code');

        if (selectedSeatIds.has(seatId)) {
            selectedSeatIds.delete(seatId);
            el.classList.remove('selected-seat');
            const idx = selectedSeatCodes.indexOf(seatCode);
            if (idx > -1) selectedSeatCodes.splice(idx, 1);
        } else {
            selectedSeatIds.add(seatId);
            el.classList.add('selected-seat');
            selectedSeatCodes.push(seatCode);
        }

        updateSelectionUI();
    }

    function clearSeatSelection() {
        selectedSeatIds.clear();
        selectedSeatCodes.length = 0;
        document.querySelectorAll('.seat-node.selected-seat').forEach(el => el.classList.remove('selected-seat'));
        updateSelectionUI();
    }

    function updateSelectionUI() {
        const count = selectedSeatIds.size;
        document.getElementById('selectedCountText').textContent = 'Đang chọn: ' + count + ' ghế';
        document.getElementById('selectedCodesText').textContent = count > 0 ? selectedSeatCodes.join(', ') : '(Click vào từng ghế trên sơ đồ để chọn)';

        const idsArray = Array.from(selectedSeatIds).join(',');
        document.querySelectorAll('.hidden-seat-ids').forEach(input => input.value = idsArray);

        const btnSet = document.getElementById('btnSetMaint');
        const btnRelease = document.getElementById('btnReleaseMaint');

        if (count > 0) {
            btnSet.disabled = false;
            btnSet.style.opacity = '1';
            btnSet.style.cursor = 'pointer';

            btnRelease.disabled = false;
            btnRelease.style.opacity = '1';
            btnRelease.style.cursor = 'pointer';
        } else {
            btnSet.disabled = true;
            btnSet.style.opacity = '0.5';
            btnSet.style.cursor = 'not-allowed';

            btnRelease.disabled = true;
            btnRelease.style.opacity = '0.5';
            btnRelease.style.cursor = 'not-allowed';
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        updateSelectionUI();
    });
</script>
</body>
</html>
