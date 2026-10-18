<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thiết Kế Sơ Đồ Ghế - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .designer-layout {
            display: grid;
            grid-template-columns: 1fr 340px;
            gap: 24px;
            align-items: start;
        }
        @media (max-width: 1200px) {
            .designer-layout { grid-template-columns: 1fr; }
        }

        /* Thanh chọn phòng & Thống kê */
        .room-selector-card {
            background: #fff;
            padding: 16px 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            flex-wrap: wrap;
            gap: 15px;
        }
        .stats-badges {
            display: flex;
            gap: 10px;
            flex-wrap: wrap;
        }
        .stat-badge {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 0.8rem;
            font-weight: 600;
            background: #f7fafc;
            border: 1px solid #e2e8f0;
        }
        .dot { width: 10px; height: 10px; border-radius: 50%; display: inline-block; }

        /* Khung sơ đồ ghế rạp chiếu */
        .screen-container {
            background: #1a1a24;
            border-radius: 12px;
            padding: 30px 20px;
            box-shadow: inset 0 0 30px rgba(0,0,0,0.5);
            text-align: center;
            overflow-x: auto;
        }
        .screen-arc {
            width: 70%;
            height: 18px;
            margin: 0 auto 35px auto;
            border-top: 5px solid #e50914;
            border-radius: 50% 50% 0 0;
            box-shadow: 0 -8px 25px rgba(229, 9, 20, 0.4);
            position: relative;
        }
        .screen-text {
            color: #718096;
            font-size: 0.75rem;
            font-weight: 700;
            letter-spacing: 4px;
            text-transform: uppercase;
            margin-top: 6px;
        }

        /* Lưới ma trận ghế */
        .seat-matrix-wrapper {
            display: inline-block;
            margin: 0 auto;
            user-select: none;
        }
        .seat-row-wrapper {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 8px;
            margin-bottom: 8px;
        }
        .row-header-label {
            width: 28px;
            height: 28px;
            color: #a0aec0;
            font-weight: bold;
            font-size: 0.85rem;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            border-radius: 4px;
            transition: all 0.2s;
        }
        .row-header-label:hover {
            background: rgba(255, 255, 255, 0.15);
            color: #fff;
        }

        /* Ghế cá nhân */
        .seat-cell {
            width: 36px;
            height: 36px;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 10px;
            font-weight: 700;
            cursor: pointer;
            color: #fff;
            transition: all 0.15s ease-in-out;
            border: 2px solid transparent;
            box-shadow: 0 2px 4px rgba(0,0,0,0.3);
        }
        .seat-cell:hover {
            transform: scale(1.15);
            z-index: 10;
        }
        .seat-cell.selected {
            border-color: #00f2fe !important;
            box-shadow: 0 0 12px #00f2fe !important;
            transform: scale(1.18);
            z-index: 20;
        }
        .seat-cell.is-maintenance {
            background: #2d3748 !important;
            color: #a0aec0 !important;
            opacity: 0.45;
            text-decoration: line-through;
        }

        /* Inspector Panel bên phải */
        .inspector-card {
            background: #fff;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
            margin-bottom: 20px;
        }
        .inspector-card h4 {
            margin: 0 0 14px 0;
            font-size: 1.05rem;
            color: #1a202c;
            border-bottom: 1px solid #edf2f7;
            padding-bottom: 8px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .selected-pill-box {
            max-height: 85px;
            overflow-y: auto;
            background: #f7fafc;
            border: 1px solid #e2e8f0;
            border-radius: 6px;
            padding: 8px;
            font-size: 0.8rem;
            color: #2d3748;
            margin-bottom: 14px;
            word-break: break-all;
        }

        .btn-action-primary {
            background: #e50914;
            color: #fff;
            padding: 9px 14px;
            border: none;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            width: 100%;
            text-align: center;
            transition: background 0.2s;
        }
        .btn-action-primary:hover { background: #b20710; }
        .btn-action-secondary {
            background: #4a5568;
            color: #fff;
            padding: 8px 12px;
            border: none;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
        }
        .form-select, .form-input {
            width: 100%;
            padding: 8px 10px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.85rem;
            box-sizing: border-box;
            margin-bottom: 12px;
        }
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
                                <c:when test="${msg == 'seats_updated'}">Đã cập nhật thành công các ghế được chọn!</c:when>
                                <c:when test="${msg == 'row_updated'}">Đã cập nhật loại ghế cho cả hàng ghế!</c:when>
                                <c:when test="${msg == 'matrix_regenerated'}">Đã khởi tạo lại toàn bộ ma trận ghế cho phòng chiếu!</c:when>
                                <c:when test="${msg == 'type_updated'}">Đã cập nhật thông số phụ thu loại ghế!</c:when>
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

                <!-- Thanh Chọn Phòng Chiếu & Thống Kê Nhanh -->
                <div class="room-selector-card">
                    <form method="GET" action="${pageContext.request.contextPath}/admin/infrastructure/seats" style="display:flex; align-items:center; gap:10px;">
                        <label style="font-weight: 700; color: #2d3748;">
                            <i class="fa-solid fa-video" style="color: #e50914; margin-right: 6px;"></i>Chọn Phòng Chiếu:
                        </label>
                        <select name="roomId" class="form-select" style="margin-bottom:0; width: 280px;" onchange="this.form.submit()">
                            <c:forEach items="${allRooms}" var="r">
                                <option value="${r.id}" ${selectedRoomId == r.id ? 'selected' : ''}>
                                    ${r.cinema.name} - ${r.name} (${r.roomType})
                                </option>
                            </c:forEach>
                        </select>
                    </form>

                    <div class="stats-badges">
                        <span class="stat-badge">
                            Tổng ghế: <strong>${totalSeats}</strong>
                        </span>
                        <span class="stat-badge">
                            <span class="dot" style="background: #6c757d;"></span> Standard: <strong>${standardCount}</strong>
                        </span>
                        <span class="stat-badge">
                            <span class="dot" style="background: #fd7e14;"></span> VIP: <strong>${vipCount}</strong>
                        </span>
                        <span class="stat-badge">
                            <span class="dot" style="background: #e83e8c;"></span> Couple: <strong>${coupleCount}</strong>
                        </span>
                        <c:if test="${maintenanceCount > 0}">
                            <span class="stat-badge" style="background: #fed7d7; color: #9b2c2c;">
                                <i class="fa-solid fa-triangle-exclamation" style="margin-right: 4px;"></i>Bảo trì: <strong>${maintenanceCount}</strong>
                            </span>
                        </c:if>
                    </div>
                </div>

                <!-- BỐ CỤC THIẾT KẾ: TRÁI LƯỚI GHẾ - PHẢI BẢNG ĐIỀU KHIỂN INSPECTOR -->
                <div class="designer-layout">

                    <!-- CỘT TRÁI: MÀN HÌNH CHIẾU & LƯỚI MA TRẬN GHẾ -->
                    <div class="admin-card" style="padding: 24px;">
                        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px;">
                            <div>
                                <h3 style="margin: 0;">Sơ Đồ Ghế: ${currentRoom.name}</h3>
                                <p style="margin: 4px 0 0 0; font-size: 0.85rem; color: #718096;">
                                    Định dạng: <strong>${currentRoom.roomType}</strong> | Kích thước: <strong>${currentRoom.totalRows} hàng &times; ${currentRoom.totalColumns} cột</strong>
                                </p>
                            </div>
                            <div style="display: flex; gap: 8px;">
                                <button type="button" class="btn-action-secondary" onclick="selectAllSeats()">Chọn tất cả</button>
                                <button type="button" class="btn-action-secondary" onclick="clearSeatSelection()" style="background:#a0aec0;">Bỏ chọn</button>
                            </div>
                        </div>

                        <!-- Khung Màn Hình Chiếu Rạp Cinema -->
                        <div class="screen-container">
                            <div class="screen-arc">
                                <div class="screen-text">MÀN HÌNH CHIẾU (SCREEN)</div>
                            </div>

                            <!-- Lưới Ghế -->
                            <div class="seat-matrix-wrapper" id="seatMatrix">
                                <c:set var="lastRow" value="" />
                                <c:forEach items="${seats}" var="s" varStatus="loop">
                                    <c:if test="${s.seatRow != lastRow}">
                                        <c:if test="${not loop.first}">
                                            </div> <!-- Đóng seat-row-wrapper trước -->
                                        </c:if>
                                        <div class="seat-row-wrapper" data-row-letter="${s.seatRow}">
                                            <div class="row-header-label" onclick="selectEntireRow('${s.seatRow}')" title="Click để chọn cả hàng ${s.seatRow}">
                                                ${s.seatRow}
                                            </div>
                                        <c:set var="lastRow" value="${s.seatRow}" />
                                    </c:if>

                                    <!-- Ô Ghế Ngồi -->
                                    <div class="seat-cell ${s.status == 'MAINTENANCE' || !s.isActive ? 'is-maintenance' : ''}"
                                         id="seat-${s.id}"
                                         data-id="${s.id}"
                                         data-code="${s.seatCode}"
                                         data-row="${s.seatRow}"
                                         data-number="${s.seatNumber}"
                                         data-type-id="${s.seatTypeId}"
                                         data-type-code="${s.seatType.typeCode}"
                                         data-status="${s.status}"
                                         data-active="${s.isActive}"
                                         style="background-color: ${s.seatType.colorHex != null ? s.seatType.colorHex : '#6c757d'};"
                                         onclick="toggleSeatSelection(this)"
                                         title="Ghế ${s.seatCode} (${s.seatType.name}) - ${s.status}">
                                        ${s.seatCode}
                                    </div>

                                    <c:if test="${loop.last}">
                                        </div> <!-- Đóng hàng cuối cùng -->
                                    </c:if>
                                </c:forEach>
                            </div>
                        </div>

                        <!-- Chú thích loại ghế bên dưới -->
                        <div style="display: flex; justify-content: center; gap: 24px; margin-top: 20px; flex-wrap: wrap;">
                            <c:forEach items="${seatTypes}" var="st">
                                <div style="display: flex; align-items: center; gap: 8px; font-size: 0.85rem;">
                                    <span style="width: 18px; height: 18px; border-radius: 4px; background: ${st.colorHex}; display: inline-block;"></span>
                                    <span><strong>${st.name}</strong> (+<fmt:formatNumber value="${st.surcharge}" pattern="#,##0"/>đ)</span>
                                </div>
                            </c:forEach>
                            <div style="display: flex; align-items: center; gap: 8px; font-size: 0.85rem; color: #a0aec0;">
                                <span style="width: 18px; height: 18px; border-radius: 4px; background: #2d3748; opacity: 0.45; text-decoration: line-through; display: inline-block;"></span>
                                <span>Ghế Bảo Trì / Khóa</span>
                            </div>
                        </div>
                    </div>

                    <!-- CỘT PHẢI: BẢNG ĐIỀU KHIỂN THIẾT KẾ (INSPECTOR PANEL) -->
                    <div>

                        <!-- 1. Bảng áp dụng cho các ghế đang chọn -->
                        <div class="inspector-card">
                            <h4>
                                <span><i class="fa-solid fa-crosshairs" style="color: #e50914; margin-right: 6px;"></i>Ghế Đang Chọn</span>
                                <span id="selectedCountBadge" style="background: #e50914; color:#fff; padding: 2px 8px; border-radius: 10px; font-size: 0.75rem;">0 ghế</span>
                            </h4>

                            <div class="selected-pill-box" id="selectedSeatsDisplay">
                                <em>Chưa chọn ghế nào. Nhấp vào ghế hoặc chữ cái đầu hàng trên sơ đồ để chọn.</em>
                            </div>

                            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seats" id="batchUpdateForm">
                                <input type="hidden" name="action" value="updateSelectedSeats">
                                <input type="hidden" name="roomId" value="${selectedRoomId}">
                                <input type="hidden" name="seatIds" id="hiddenSeatIds" value="">

                                <label style="font-size: 0.8rem; font-weight: 600; color: #4a5568;">Đổi Loại Ghế:</label>
                                <select name="seatTypeId" class="form-select">
                                    <c:forEach items="${seatTypes}" var="st">
                                        <option value="${st.id}">${st.name} (${st.typeCode})</option>
                                    </c:forEach>
                                </select>

                                <label style="font-size: 0.8rem; font-weight: 600; color: #4a5568;">Trạng Thái Vận Hành:</label>
                                <select name="status" class="form-select">
                                    <option value="ACTIVE">Hoạt động bình thường (ACTIVE)</option>
                                    <option value="MAINTENANCE">Bảo trì / Hỏng hóc (MAINTENANCE)</option>
                                    <option value="BLOCKED">Khóa ghế / Tạo lối đi (BLOCKED)</option>
                                </select>

                                <button type="submit" class="btn-action-primary" id="btnApplySelected" disabled>
                                    <i class="fa-solid fa-floppy-disk"></i> Áp Dụng Thay Đổi
                                </button>
                            </form>

                            <!-- Quick Maintenance Toggles (Function 5 - Iter 2) -->
                            <div style="margin-top: 15px; padding-top: 15px; border-top: 1px dashed #e2e8f0;">
                                <div style="font-size: 0.8rem; font-weight: 700; color: #4a5568; margin-bottom: 8px;">
                                    <i class="fa-solid fa-wrench" style="color: #ed8936;"></i> THAO TÁC BẢO TRÌ NHANH
                                </div>
                                <div style="display: flex; flex-direction: column; gap: 8px;">
                                    <button type="button" class="btn-action-secondary" id="btnQuickMaintToggle" style="background: #ed8936; color: #fff; width: 100%; border: none;" onclick="quickToggleMaint(true)" disabled>
                                        <i class="fa-solid fa-tools"></i> Chuyển Ghế Đang Chọn sang BẢO TRÌ
                                    </button>
                                    <button type="button" class="btn-action-secondary" id="btnQuickMaintRestore" style="background: #319795; color: #fff; width: 100%; border: none;" onclick="quickToggleMaint(false)" disabled>
                                        <i class="fa-solid fa-check-circle"></i> Mở Lại Ghế Đang Chọn (ACTIVE)
                                    </button>
                                    <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seats" onsubmit="return confirm('Bạn có chắc chắn muốn mở lại TOÀN BỘ ghế bảo trì trong phòng này về trạng thái HOẠT ĐỘNG?')">
                                        <input type="hidden" name="action" value="restoreAllRoomSeats">
                                        <input type="hidden" name="roomId" value="${selectedRoomId}">
                                        <button type="submit" class="btn-action-secondary" style="background: #4a5568; color: #fff; width: 100%; border: none;">
                                            <i class="fa-solid fa-arrows-rotate"></i> Khôi Phục Toàn Bộ Ghế Phòng
                                        </button>
                                    </form>
                                </div>
                            </div>
                        </div>

                        <!-- 2. Đổi nhanh cả hàng ghế -->
                        <div class="inspector-card">
                            <h4>
                                <i class="fa-solid fa-bolt" style="color: #f59e0b; margin-right: 6px;"></i>Đổi Nhanh Theo Hàng
                            </h4>
                            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seats">
                                <input type="hidden" name="action" value="applyRowType">
                                <input type="hidden" name="roomId" value="${selectedRoomId}">

                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                                    <div>
                                        <label style="font-size: 0.8rem; font-weight: 600;">Hàng ghế:</label>
                                        <input type="text" name="seatRow" class="form-input" placeholder="VD: D" maxlength="2" required style="text-transform: uppercase;">
                                    </div>
                                    <div>
                                        <label style="font-size: 0.8rem; font-weight: 600;">Loại ghế mới:</label>
                                        <select name="seatTypeId" class="form-select">
                                            <c:forEach items="${seatTypes}" var="st">
                                                <option value="${st.id}">${st.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </div>
                                <button type="submit" class="btn-action-secondary" style="width: 100%;">
                                    <i class="fa-solid fa-arrows-split-up-and-left"></i> Áp Dụng Cho Cả Hàng
                                </button>
                            </form>
                        </div>

                        <!-- 3. Công cụ Sinh lại Ma trận Tự Động (Auto-Generator) -->
                        <div class="inspector-card">
                            <h4 style="color: #c53030;">
                                <i class="fa-solid fa-wand-magic-sparkles" style="color: #e50914; margin-right: 6px;"></i>Tái Tạo Ma Trận Tự Động
                            </h4>
                            <p style="font-size: 0.75rem; color: #718096; margin-top: -5px; margin-bottom: 10px;">
                                Khởi tạo lại kích thước lưới và phân bổ ghế mới theo quy tắc.
                            </p>
                            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seats" onsubmit="return confirm('CẢNH BÁO: Thao tác này sẽ xóa toàn bộ sơ đồ ghế cũ và sinh lại ma trận ghế mới! Bạn có chắc chắn?')">
                                <input type="hidden" name="action" value="regenerateMatrix">
                                <input type="hidden" name="roomId" value="${selectedRoomId}">

                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                                    <div>
                                        <label style="font-size: 0.75rem;">Số hàng (A-Z):</label>
                                        <input type="number" name="totalRows" class="form-input" value="${currentRoom.totalRows != null ? currentRoom.totalRows : 10}" min="4" max="26" required>
                                    </div>
                                    <div>
                                        <label style="font-size: 0.75rem;">Số cột (1-N):</label>
                                        <input type="number" name="totalColumns" class="form-input" value="${currentRoom.totalColumns != null ? currentRoom.totalColumns : 10}" min="4" max="30" required>
                                    </div>
                                </div>

                                <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">
                                    <div>
                                        <label style="font-size: 0.75rem;">Số hàng Standard:</label>
                                        <input type="number" name="stdRows" class="form-input" value="3" min="1" max="20" required>
                                    </div>
                                    <div>
                                        <label style="font-size: 0.75rem;">Số hàng VIP:</label>
                                        <input type="number" name="vipRows" class="form-input" value="5" min="1" max="20" required>
                                    </div>
                                </div>
                                <small style="display: block; font-size: 0.7rem; color: #a0aec0; margin-bottom: 10px;">* Các hàng còn lại ở cuối sẽ tự động gán loại COUPLE.</small>

                                <button type="submit" class="btn-action-primary" style="background: #2d3748;">
                                    <i class="fa-solid fa-rotate"></i> Tái Tạo Sơ Đồ Mới
                                </button>
                            </form>
                        </div>

                    </div>
                </div>

                <!-- KHỐI QUẢN LÝ LỊCH BẢO TRÌ PHÒNG CHIẾU (MAINTENANCE SCHEDULES - Function 5) -->
                <div class="card" style="margin-top: 24px; border-radius: 8px; border: 1px solid #e2e8f0; background: #fff; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden;">
                    <div style="background: #1a1a24; padding: 16px 20px; display: flex; justify-content: space-between; align-items: center;">
                        <h3 style="margin: 0; color: #fff; font-size: 1.05rem; display: flex; align-items: center; gap: 10px;">
                            <i class="fa-solid fa-calendar-check" style="color: #e50914;"></i>
                            <span>Lịch Bảo Trì Phòng Chiếu: <strong>${currentRoom.name}</strong> (${currentRoom.cinema.name})</span>
                        </h3>
                        <span style="background: rgba(229, 9, 20, 0.2); color: #ff6b6b; padding: 4px 10px; border-radius: 20px; font-size: 0.8rem; font-weight: 600;">
                            Bảng maintenance_schedules
                        </span>
                    </div>

                    <div style="padding: 20px; display: grid; grid-template-columns: 1fr 2fr; gap: 24px;">
                        <!-- Form Đặt Lịch Bảo Trì Mới -->
                        <div style="background: #f8fafc; padding: 18px; border-radius: 8px; border: 1px solid #e2e8f0;">
                            <h4 style="margin: 0 0 14px 0; color: #2d3748; font-size: 0.95rem; display: flex; align-items: center; gap: 6px;">
                                <i class="fa-solid fa-calendar-plus" style="color: #e50914;"></i> Lên Lịch Bảo Trì Mới
                            </h4>
                            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seats">
                                <input type="hidden" name="action" value="createMaintenanceSchedule">
                                <input type="hidden" name="roomId" value="${selectedRoomId}">
                                <input type="hidden" name="seatIds" id="schedSeatIds" value="">

                                <div style="margin-bottom: 12px;">
                                    <label style="display: block; font-size: 0.8rem; font-weight: 600; color: #4a5568; margin-bottom: 4px;">Thời gian bắt đầu:</label>
                                    <input type="datetime-local" name="startTime" class="form-input" required id="schedStartTime">
                                </div>
                                <div style="margin-bottom: 12px;">
                                    <label style="display: block; font-size: 0.8rem; font-weight: 600; color: #4a5568; margin-bottom: 4px;">Thời gian kết thúc:</label>
                                    <input type="datetime-local" name="endTime" class="form-input" required id="schedEndTime">
                                </div>
                                <div style="margin-bottom: 12px;">
                                    <label style="display: block; font-size: 0.8rem; font-weight: 600; color: #4a5568; margin-bottom: 4px;">Lý do bảo trì:</label>
                                    <input type="text" name="reason" class="form-input" placeholder="VD: Thay đèn chiếu laser, sửa các ghế hỏng" required>
                                </div>
                                <div style="margin-bottom: 14px; background: #edf2f7; padding: 10px; border-radius: 6px; font-size: 0.75rem; color: #4a5568;">
                                    <i class="fa-solid fa-info-circle"></i> Ghế đính kèm: 
                                    <strong id="schedSelectedSeatsText">Toàn phòng (Không đính kèm ghế cụ thể)</strong>
                                </div>

                                <button type="submit" class="btn-action-primary" style="width: 100%;">
                                    <i class="fa-solid fa-plus"></i> Tạo Phiếu Bảo Trì
                                </button>
                            </form>
                        </div>

                        <!-- Danh sách Lịch Bảo Trì của Phòng -->
                        <div>
                            <h4 style="margin: 0 0 14px 0; color: #2d3748; font-size: 0.95rem; display: flex; align-items: center; justify-content: space-between;">
                                <span><i class="fa-solid fa-clock-rotate-left" style="color: #4a5568;"></i> Danh Sách Phiếu Bảo Trì (${maintenanceSchedules != null ? maintenanceSchedules.size() : 0})</span>
                            </h4>

                            <div style="max-height: 380px; overflow-y: auto; border: 1px solid #e2e8f0; border-radius: 8px;">
                                <table style="width: 100%; border-collapse: collapse; font-size: 0.85rem; text-align: left;">
                                    <thead style="background: #edf2f7; color: #4a5568; font-weight: 700; position: sticky; top: 0;">
                                        <tr>
                                            <th style="padding: 10px 12px;">Mã #</th>
                                            <th style="padding: 10px 12px;">Bắt đầu</th>
                                            <th style="padding: 10px 12px;">Kết thúc</th>
                                            <th style="padding: 10px 12px;">Lý do</th>
                                            <th style="padding: 10px 12px;">Người tạo</th>
                                            <th style="padding: 10px 12px;">Trạng thái</th>
                                            <th style="padding: 10px 12px; text-align: center;">Thao tác</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${not empty maintenanceSchedules}">
                                                <c:forEach items="${maintenanceSchedules}" var="ms">
                                                    <tr style="border-bottom: 1px solid #edf2f7;">
                                                        <td style="padding: 10px 12px; font-weight: 700; color: #e50914;">#${ms.id}</td>
                                                        <td style="padding: 10px 12px; white-space: nowrap;">${ms.startTime}</td>
                                                        <td style="padding: 10px 12px; white-space: nowrap;">${ms.endTime}</td>
                                                        <td style="padding: 10px 12px;">${ms.reason}</td>
                                                        <td style="padding: 10px 12px;">${ms.createdByName != null ? ms.createdByName : 'Quản trị viên'}</td>
                                                        <td style="padding: 10px 12px;">
                                                            <span style="background: #feebc8; color: #c05621; padding: 2px 8px; border-radius: 4px; font-weight: 600; font-size: 0.75rem;">
                                                                ĐANG XỬ LÝ
                                                            </span>
                                                        </td>
                                                        <td style="padding: 10px 12px; text-align: center;">
                                                            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/seats" style="display: inline;" onsubmit="return confirm('Xác nhận hoàn tất bảo trì và mở khóa ghế?')">
                                                                <input type="hidden" name="action" value="resolveMaintenanceSchedule">
                                                                <input type="hidden" name="scheduleId" value="${ms.id}">
                                                                <input type="hidden" name="roomId" value="${selectedRoomId}">
                                                                <input type="hidden" name="restoreSeats" value="true">
                                                                <button type="submit" style="background: #38a169; color: #fff; border: none; padding: 4px 10px; border-radius: 4px; cursor: pointer; font-size: 0.75rem; font-weight: 600;">
                                                                    <i class="fa-solid fa-check"></i> Hoàn Tất
                                                                </button>
                                                            </form>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </c:when>
                                            <c:otherwise>
                                                <tr>
                                                    <td colspan="7" style="padding: 24px; text-align: center; color: #a0aec0;">
                                                        <em>Phòng chiếu này hiện không có lịch bảo trì nào. Toàn bộ cơ sở vật chất sẵn sàng phục vụ!</em>
                                                    </td>
                                                </tr>
                                            </c:otherwise>
                                        </c:choose>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <!-- JAVASCRIPT XỬ LÝ CHỌN GHẾ TƯƠNG TÁC CAO -->
    <script>
        let selectedSeatIds = new Set();
        let selectedSeatCodes = new Set();

        function toggleSeatSelection(cell) {
            const seatId = cell.getAttribute('data-id');
            const seatCode = cell.getAttribute('data-code');

            if (selectedSeatIds.has(seatId)) {
                selectedSeatIds.delete(seatId);
                selectedSeatCodes.delete(seatCode);
                cell.classList.remove('selected');
            } else {
                selectedSeatIds.add(seatId);
                selectedSeatCodes.add(seatCode);
                cell.classList.add('selected');
            }
            updateInspector();
        }

        function selectEntireRow(rowLetter) {
            const rowWrapper = document.querySelector(`.seat-row-wrapper[data-row-letter="${rowLetter}"]`);
            if (!rowWrapper) return;

            const cells = rowWrapper.querySelectorAll('.seat-cell');
            let allSelected = true;
            cells.forEach(c => {
                if (!selectedSeatIds.has(c.getAttribute('data-id'))) {
                    allSelected = false;
                }
            });

            cells.forEach(c => {
                const sId = c.getAttribute('data-id');
                const sCode = c.getAttribute('data-code');
                if (allSelected) {
                    selectedSeatIds.delete(sId);
                    selectedSeatCodes.delete(sCode);
                    c.classList.remove('selected');
                } else {
                    selectedSeatIds.add(sId);
                    selectedSeatCodes.add(sCode);
                    c.classList.add('selected');
                }
            });
            updateInspector();
        }

        function selectAllSeats() {
            document.querySelectorAll('.seat-cell').forEach(c => {
                selectedSeatIds.add(c.getAttribute('data-id'));
                selectedSeatCodes.add(c.getAttribute('data-code'));
                c.classList.add('selected');
            });
            updateInspector();
        }

        function clearSeatSelection() {
            selectedSeatIds.clear();
            selectedSeatCodes.clear();
            document.querySelectorAll('.seat-cell').forEach(c => c.classList.remove('selected'));
            updateInspector();
        }

        function updateInspector() {
            const count = selectedSeatIds.size;
            document.getElementById('selectedCountBadge').innerText = count + ' ghế';
            document.getElementById('hiddenSeatIds').value = Array.from(selectedSeatIds).join(',');

            const displayBox = document.getElementById('selectedSeatsDisplay');
            const btnApply = document.getElementById('btnApplySelected');
            const btnMaintToggle = document.getElementById('btnQuickMaintToggle');
            const btnMaintRestore = document.getElementById('btnQuickMaintRestore');
            const schedSeatIds = document.getElementById('schedSeatIds');
            const schedSelectedSeatsText = document.getElementById('schedSelectedSeatsText');

            if (count === 0) {
                displayBox.innerHTML = '<em>Chưa chọn ghế nào. Nhấp vào ghế hoặc chữ cái đầu hàng trên sơ đồ để chọn.</em>';
                btnApply.disabled = true;
                if (btnMaintToggle) btnMaintToggle.disabled = true;
                if (btnMaintRestore) btnMaintRestore.disabled = true;
                if (schedSeatIds) schedSeatIds.value = '';
                if (schedSelectedSeatsText) schedSelectedSeatsText.innerText = 'Toàn phòng (Không đính kèm ghế cụ thể)';
            } else {
                displayBox.innerText = Array.from(selectedSeatCodes).sort().join(', ');
                btnApply.disabled = false;
                if (btnMaintToggle) btnMaintToggle.disabled = false;
                if (btnMaintRestore) btnMaintRestore.disabled = false;
                if (schedSeatIds) schedSeatIds.value = Array.from(selectedSeatIds).join(',');
                if (schedSelectedSeatsText) schedSelectedSeatsText.innerText = count + ' ghế: ' + Array.from(selectedSeatCodes).sort().join(', ');
            }
        }

        function quickToggleMaint(underMaintenance) {
            if (selectedSeatIds.size === 0) {
                alert('Vui lòng chọn ít nhất một ghế trên sơ đồ!');
                return;
            }
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '${pageContext.request.contextPath}/admin/infrastructure/seats';

            const actionInp = document.createElement('input');
            actionInp.type = 'hidden';
            actionInp.name = 'action';
            actionInp.value = 'quickToggleMaintenance';
            form.appendChild(actionInp);

            const roomInp = document.createElement('input');
            roomInp.type = 'hidden';
            roomInp.name = 'roomId';
            roomInp.value = '${selectedRoomId}';
            form.appendChild(roomInp);

            const seatInp = document.createElement('input');
            seatInp.type = 'hidden';
            seatInp.name = 'seatIds';
            seatInp.value = Array.from(selectedSeatIds).join(',');
            form.appendChild(seatInp);

            const underInp = document.createElement('input');
            underInp.type = 'hidden';
            underInp.name = 'underMaintenance';
            underInp.value = underMaintenance ? 'true' : 'false';
            form.appendChild(underInp);

            document.body.appendChild(form);
            form.submit();
        }

        window.addEventListener('DOMContentLoaded', () => {
            const now = new Date();
            const tomorrow = new Date(Date.now() + 24 * 60 * 60 * 1000);
            const formatDT = d => {
                const pad = n => String(n).padStart(2, '0');
                return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate()) + 'T' + pad(d.getHours()) + ':' + pad(d.getMinutes());
            };
            const startInput = document.getElementById('schedStartTime');
            const endInput = document.getElementById('schedEndTime');
            if (startInput && !startInput.value) startInput.value = formatDT(now);
            if (endInput && !endInput.value) endInput.value = formatDT(tomorrow);
        });
    </script>
</body>
</html>
