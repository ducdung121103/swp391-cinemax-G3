<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>In Vé Tại Quầy | CineMax</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/operation.css?v=9">
    <style>
        .lookup-card {
            background: #fff;
            border-radius: 12px;
            padding: 24px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 4px 12px rgba(0,0,0,0.03);
            margin-bottom: 24px;
        }
        .lookup-input-group {
            display: flex;
            gap: 12px;
            align-items: stretch;
            max-width: 760px;
        }
        .lookup-input {
            flex: 1;
            padding: 12px 18px;
            font-size: 15px;
            font-weight: 600;
            border: 2px solid #cbd5e1;
            border-radius: 8px;
            outline: none;
            transition: border-color 0.2s;
        }
        .lookup-input:focus {
            border-color: #e50914;
            box-shadow: 0 0 0 3px rgba(229, 9, 20, 0.15);
        }
        .recent-table-card {
            background: #fff;
            border-radius: 12px;
            border: 1px solid #e2e8f0;
            box-shadow: 0 2px 6px rgba(0,0,0,0.02);
            overflow: hidden;
            margin-top: 24px;
        }
        .recent-table-header {
            padding: 16px 20px;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .customer-pill {
            display: inline-flex;
            align-items: center;
            gap: 6px;
            background: #f1f5f9;
            padding: 4px 10px;
            border-radius: 6px;
            font-size: 12px;
            font-weight: 600;
            color: #334155;
        }
        @media print {
            .lookup-card, .recent-table-card, .search-results-card, .operation-heading, .admin-sidebar, .admin-navbar, .no-print {
                display: none !important;
            }
            .admin-wrapper, .admin-main, .operation-page {
                display: block !important;
                padding: 0 !important;
                margin: 0 !important;
                background: #fff !important;
            }
            .receipt-card, .receipt-detail-card {
                border: 1px dashed #64748b !important;
                box-shadow: none !important;
                margin-bottom: 20px !important;
            }
        }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <main class="admin-content operation-page">

            <!-- HEADER -->
            <header class="operation-heading no-print">
                <div>
                    <span class="operation-eyebrow">TÁC NGHIỆP TẠI RẠP · QUẦY SOÁT & IN VÉ</span>
                    <h1>Tra Cứu & In Vé Khách Hàng</h1>
                    <p>Nhập mã đơn hàng, tên khách hoặc số điện thoại (chỉ cần vài ký tự) để tìm kiếm và in vé cứng.</p>
                </div>
                <div class="header-chips">
                    <div class="pos-cinema-chip">
                        <span class="cinema-dot"></span>
                        <div>
                            <small>Chi nhánh trực</small>
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty staff.cinema}">${staff.cinema.name}</c:when>
                                    <c:otherwise>Toàn Hệ Thống CineMax</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                    </div>
                    <div class="operator-chip">
                        <span class="operator-dot"></span>
                        <div>
                            <small>Nhân viên thực hiện</small>
                            <strong>${staff.fullName}</strong>
                        </div>
                    </div>
                </div>
            </header>

            <!-- HỘP TÌM KIẾM THEO KÝ TỰ (MÃ ĐƠN, TÊN KHÁCH, SĐT) -->
            <div class="lookup-card no-print">
                <h3 style="font-size: 15px; font-weight: 700; color: #1e293b; margin-bottom: 6px;">
                    <i class="fa-solid fa-magnifying-glass text-danger me-2"></i> Tra Cứu Đơn Hàng Để In Vé:
                </h3>
                <p style="font-size: 13px; color: #64748b; margin-bottom: 14px;">
                    Tìm kiếm linh hoạt: bạn có thể nhập <strong>vài ký tự mã đơn</strong> (ví dụ: <em>3E15</em>), <strong>tên khách hàng</strong> (ví dụ: <em>Khách Hàng</em>, <em>Nguyễn</em>) hoặc <strong>số điện thoại</strong>.
                </p>

                <form method="get" action="${pageContext.request.contextPath}/operation/ticket-print">
                    <div class="lookup-input-group">
                        <input type="text" name="keyword" class="lookup-input" value="${searchedCode}"
                               placeholder="Gõ mã đơn (3E15...), tên khách (Nguyễn...), hoặc SĐT (0987...)" autofocus required>
                        <button type="submit" class="button button-primary" style="padding: 12px 24px; font-weight: 700; font-size: 14px;">
                            <i class="fa-solid fa-magnifying-glass me-1"></i> Tìm Kiếm & In Vé
                        </button>
                        <c:if test="${not empty searchedCode}">
                            <a href="${pageContext.request.contextPath}/operation/ticket-print" class="button button-secondary"
                               style="padding: 12px 18px; font-weight: 600; text-decoration: none; display: flex; align-items: center;"
                               title="Xem tất cả đơn gần đây">
                                <i class="fa-solid fa-xmark me-1"></i> Xóa tìm
                            </a>
                        </c:if>
                    </div>
                </form>

                <c:if test="${not empty errorMessage}">
                    <div class="operation-alert alert-error" role="alert" style="margin-top: 16px; margin-bottom: 0;">
                        <span class="alert-icon">!</span>
                        <span><c:out value="${errorMessage}" /></span>
                    </div>
                </c:if>

                <c:if test="${not empty infoMessage}">
                    <div class="operation-alert" role="alert" style="margin-top: 16px; margin-bottom: 0; background: #f0fdf4; border-color: #86efac; color: #166534;">
                        <span class="alert-icon" style="color: #16a34a;">✓</span>
                        <span><c:out value="${infoMessage}" /></span>
                    </div>
                </c:if>
            </div>

            <!-- DANH SÁCH KẾT QUẢ TÌM KIẾM (KHI TÌM THẤY NHIỀU ĐƠN PHÙ HỢP) -->
            <c:if test="${not empty searchResults}">
                <div class="recent-table-card search-results-card no-print" style="margin-top: 0; margin-bottom: 24px; border: 2px solid #93c5fd;">
                    <div class="recent-table-header" style="background: #eff6ff;">
                        <div>
                            <h3 style="font-size: 15px; font-weight: 700; color: #1e40af; margin: 0;">
                                <i class="fa-solid fa-list-check text-primary me-2"></i> Kết Quả Tìm Kiếm Khớp Với: "${searchedCode}"
                            </h3>
                            <small style="color: #475569;">Có <strong>${fn:length(searchResults)}</strong> đơn hàng phù hợp. Vui lòng nhấn "Chọn & In Vé" tại dòng của khách:</small>
                        </div>
                        <span class="badge" style="background:#2563eb; color:#fff; padding:6px 14px; border-radius:6px; font-weight:700;">
                            ${fn:length(searchResults)} kết quả
                        </span>
                    </div>

                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="width: 150px;">Mã Đơn</th>
                                    <th style="width: 180px;">Khách Hàng</th>
                                    <th>Phim Điện Ảnh</th>
                                    <th>Suất Chiếu & Rạp</th>
                                    <th style="text-align: center;">Kênh</th>
                                    <th style="text-align: center;">Số Ghế</th>
                                    <th style="text-align: right;">Tổng Tiền</th>
                                    <th style="text-align: center; width: 140px;">Hành Động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${searchResults}" var="sr">
                                    <tr>
                                        <td>
                                            <strong class="font-monospace" style="color: #e50914;">${sr.bookingCode}</strong>
                                            <small class="d-block text-muted" style="font-size:11px;">${sr.formattedCreatedAt}</small>
                                        </td>
                                        <td>
                                            <div style="font-weight: 700; color: #0f172a;">${sr.displayCustomerName}</div>
                                            <c:if test="${not empty sr.customerPhone}">
                                                <small class="d-block text-muted" style="font-size:11px;">
                                                    <i class="fa-solid fa-phone me-1 text-success"></i>${sr.customerPhone}
                                                </small>
                                            </c:if>
                                            <c:if test="${not empty sr.customerEmail}">
                                                <small class="d-block text-muted" style="font-size:11px;">
                                                    <i class="fa-solid fa-envelope me-1"></i>${sr.customerEmail}
                                                </small>
                                            </c:if>
                                        </td>
                                        <td><strong>${sr.movieTitle}</strong></td>
                                        <td>
                                            <span style="font-size: 13px; color: #334155;">
                                                ${sr.formattedShowtime} · <strong>${sr.roomName}</strong>
                                            </span>
                                            <small class="d-block text-muted" style="font-size:11px;">${sr.cinemaName}</small>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="${sr.channel eq 'POS' ? 'channel-badge-pos' : 'channel-badge-online'}" style="padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: 700;">
                                                ${sr.channel}
                                            </span>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="badge" style="background:#f1f5f9; color:#1e293b; font-weight:700; border:1px solid #cbd5e1;">
                                                ${sr.seatCodesString}
                                            </span>
                                        </td>
                                        <td style="text-align: right; font-weight: 700; color: #e50914;">
                                            <fmt:formatNumber value="${sr.finalAmount}" pattern="#,##0" groupingUsed="true" /> đ
                                        </td>
                                        <td style="text-align: center;">
                                            <a href="${pageContext.request.contextPath}/operation/ticket-print?code=${sr.bookingCode}"
                                               class="button button-primary"
                                               style="padding: 6px 12px; font-size: 12px; font-weight: 700; text-decoration: none; display: inline-flex; align-items: center; gap: 4px;">
                                                <i class="fa-solid fa-print"></i> Chọn & In Vé
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </c:if>

            <!-- HIỂN THỊ VÉ VÀ HÓA ĐƠN KHI TRA CỨU THÀNH CÔNG (GIỐNG BÊN POS) -->
            <c:if test="${not empty booking}">
                <section class="receipt-card" id="receipt" aria-labelledby="receipt-title">
                    <div class="receipt-mark">✓</div>
                    <div class="receipt-copy">
                        <span class="operation-eyebrow">THÔNG TIN VÉ XÁC NHẬN</span>
                        <h2 id="receipt-title">Đơn hàng đã được xác nhận</h2>
                        <p>
                            Mã đơn: <strong class="text-danger font-monospace">${booking.bookingCode}</strong>
                            <span class="receipt-divider">·</span>
                            Kênh mua: <strong>${booking.channel}</strong>
                            <span class="receipt-divider">·</span>
                            Trạng thái: <strong style="color: #10b981;">${booking.status}</strong>
                        </p>
                        <p class="receipt-showtime" style="background: rgba(255,255,255,0.06); padding: 6px 12px; border-radius: 6px; margin: 6px 0;">
                            <i class="fa-solid fa-user me-1 text-primary"></i> Khách hàng: <strong style="font-size: 15px; color: #fff;">${booking.displayCustomerName}</strong>
                            <c:if test="${not empty booking.customerPhone}">
                                <span class="receipt-divider">·</span>
                                <i class="fa-solid fa-phone me-1 text-success"></i> SĐT: <strong>${booking.customerPhone}</strong>
                            </c:if>
                            <c:if test="${not empty booking.customerEmail}">
                                <span class="receipt-divider">·</span>
                                <i class="fa-solid fa-envelope me-1 text-muted"></i> <strong>${booking.customerEmail}</strong>
                            </c:if>
                        </p>
                        <p class="receipt-showtime">
                            Rạp: <strong>${booking.cinemaName}</strong> ·
                            <strong>${booking.movieTitle}</strong> ·
                            ${booking.formattedShowtime} ·
                            Phòng: <strong>${booking.roomName} (${booking.experienceFormat})</strong>
                        </p>
                        <p class="receipt-showtime">
                            Thanh toán: <strong><c:choose><c:when test="${booking.paymentMethod eq 'CASH'}">Tiền mặt</c:when><c:when test="${booking.paymentMethod eq 'VNPAY_POS' or booking.paymentMethod eq 'VNPAY'}">Cổng VNPAY</c:when><c:otherwise><c:out value="${booking.paymentMethod}" /></c:otherwise></c:choose></strong>
                        </p>
                    </div>
                    <div class="receipt-total">
                        <small>Tổng thanh toán</small>
                        <strong><fmt:formatNumber value="${booking.finalAmount}" pattern="#,##0" groupingUsed="true" /><span>đ</span></strong>
                    </div>
                    <div style="display: flex; gap: 10px; align-items: center;" class="no-print">
                        <button class="button button-secondary receipt-print" type="button" onclick="window.print()">
                            <i class="fa-solid fa-print me-1"></i> In Vé Cứng
                        </button>
                        <a href="${pageContext.request.contextPath}/operation/ticket-print" class="button button-primary">
                            Tra Cứu Khách Khác →
                        </a>
                    </div>
                </section>

                <section class="receipt-detail-card" aria-label="Chi tiết vé in cứng" style="margin-bottom: 24px;">
                    <h3>Chi tiết vé ghế ngồi</h3>
                    <div class="receipt-detail-line" style="background: #f8fafc; padding: 10px 14px; border-radius: 6px; margin-bottom: 12px; border: 1px solid #e2e8f0;">
                        <span><i class="fa-solid fa-user text-danger me-1"></i> Khách hàng</span>
                        <strong style="color: #0f172a; font-size: 14px;">
                            ${booking.displayCustomerName}
                            <c:if test="${not empty booking.customerPhone}"> · SĐT: ${booking.customerPhone}</c:if>
                        </strong>
                    </div>
                    <div class="receipt-detail-line">
                        <span>Mã đơn hàng</span>
                        <strong class="font-monospace" style="color: #e50914;">${booking.bookingCode}</strong>
                    </div>
                    <div class="receipt-detail-line">
                        <span>Suất chiếu & Phòng</span>
                        <strong>${booking.formattedShowtime} · ${booking.roomName} (${booking.experienceFormat})</strong>
                    </div>
                    <div class="receipt-detail-line">
                        <span>Số lượng vé</span>
                        <strong>${booking.ticketCount} ghế (${booking.seatCodesString})</strong>
                    </div>
                    <c:forEach items="${booking.tickets}" var="ticket">
                        <div class="receipt-detail-line receipt-detail-subline">
                            <span><i class="fa-solid fa-chair text-danger me-1"></i> Ghế <strong>${ticket.seatCode}</strong></span>
                            <strong><fmt:formatNumber value="${ticket.ticketPrice}" pattern="#,##0" groupingUsed="true" /> đ</strong>
                        </div>
                    </c:forEach>
                    <div class="receipt-detail-line">
                        <span>Tổng tiền vé</span>
                        <strong><fmt:formatNumber value="${booking.totalTicketsAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong>
                    </div>

                    <c:if test="${not empty booking.fnbItems}">
                        <div class="receipt-detail-line" style="margin-top: 10px; border-top: 1px dashed #cbd5e1; padding-top: 10px;">
                            <span>Bắp nước & combo kèm theo (giao cho khách)</span>
                            <strong><fmt:formatNumber value="${booking.totalFnbAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong>
                        </div>
                        <c:forEach items="${booking.fnbItems}" var="fnbItem">
                            <div class="receipt-detail-line receipt-detail-subline">
                                <span><i class="fa-solid fa-bowl-food text-warning me-1"></i> ${fnbItem.itemName} × ${fnbItem.quantity}</span>
                                <strong><fmt:formatNumber value="${fnbItem.subtotal}" pattern="#,##0" groupingUsed="true" /> đ</strong>
                            </div>
                        </c:forEach>
                    </c:if>

                    <div class="receipt-detail-line receipt-detail-grand-total">
                        <span>Tổng hóa đơn</span>
                        <strong><fmt:formatNumber value="${booking.finalAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong>
                    </div>
                </section>
            </c:if>

            <!-- BẢNG ĐƠN ĐẶT VÉ GẦN ĐÂY CỦA RẠP -->
            <div class="recent-table-card no-print">
                <div class="recent-table-header">
                    <div>
                        <h3 style="font-size: 15px; font-weight: 700; color: #1e293b; margin: 0;">
                            <i class="fa-solid fa-clock-rotate-left text-warning me-2"></i> Đơn Đặt Vé Gần Đây Cần In Vé Tại Quầy
                        </h3>
                        <small style="color: #64748b;">Bao gồm các đơn đặt vé Online và POS đã thanh toán thành công gần nhất.</small>
                    </div>
                    <span class="badge" style="background:#e0f2fe; color:#0369a1; padding:6px 12px; border-radius:6px; font-weight:700;">
                        ${fn:length(recentBookings)} đơn gần nhất
                    </span>
                </div>

                <div class="table-responsive">
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th style="width: 140px;">Mã Đơn</th>
                                <th style="width: 170px;">Khách Hàng</th>
                                <th>Phim Điện Ảnh</th>
                                <th>Suất Chiếu & Phòng</th>
                                <th style="text-align: center;">Kênh</th>
                                <th style="text-align: center;">Số Ghế</th>
                                <th style="text-align: right;">Tổng Tiền</th>
                                <th style="text-align: center; width: 130px;">Hành Động</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${not empty recentBookings}">
                                    <c:forEach items="${recentBookings}" var="rb">
                                        <tr style="${rb.bookingCode eq searchedCode ? 'background:#fef2f2;' : ''}">
                                            <td>
                                                <strong class="font-monospace" style="color: #e50914;">${rb.bookingCode}</strong>
                                                <small class="d-block text-muted" style="font-size:11px;">${rb.formattedCreatedAt}</small>
                                            </td>
                                            <td>
                                                <div style="font-weight: 700; color: #1e293b;">${rb.displayCustomerName}</div>
                                                <c:if test="${not empty rb.customerPhone}">
                                                    <small class="d-block text-muted" style="font-size:11px;">
                                                        <i class="fa-solid fa-phone me-1 text-success"></i>${rb.customerPhone}
                                                    </small>
                                                </c:if>
                                            </td>
                                            <td><strong>${rb.movieTitle}</strong></td>
                                            <td>
                                                <span style="font-size: 13px; color: #334155;">
                                                    ${rb.formattedShowtime} · <strong>${rb.roomName}</strong>
                                                </span>
                                            </td>
                                            <td style="text-align: center;">
                                                <span class="${rb.channel eq 'POS' ? 'channel-badge-pos' : 'channel-badge-online'}" style="padding: 3px 8px; border-radius: 4px; font-size: 11px; font-weight: 700;">
                                                    ${rb.channel}
                                                </span>
                                            </td>
                                            <td style="text-align: center;">
                                                <span class="badge" style="background:#f1f5f9; color:#1e293b; font-weight:700; border:1px solid #cbd5e1;">
                                                    ${rb.seatCodesString}
                                                </span>
                                            </td>
                                            <td style="text-align: right; font-weight: 700; color: #e50914;">
                                                <fmt:formatNumber value="${rb.finalAmount}" pattern="#,##0" groupingUsed="true" /> đ
                                            </td>
                                            <td style="text-align: center;">
                                                <a href="${pageContext.request.contextPath}/operation/ticket-print?code=${rb.bookingCode}"
                                                   class="button button-secondary"
                                                   style="padding: 6px 12px; font-size: 12px; font-weight: 700; text-decoration: none; display: inline-flex; align-items: center; gap: 4px;">
                                                    <i class="fa-solid fa-print text-warning"></i> In Vé
                                                </a>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <tr>
                                        <td colspan="8" style="text-align: center; padding: 24px; color: #94a3b8;">
                                            Chưa có đơn hàng nào được ghi nhận tại rạp.
                                        </td>
                                    </tr>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>

        </main>
    </div>
</div>
</body>
</html>
