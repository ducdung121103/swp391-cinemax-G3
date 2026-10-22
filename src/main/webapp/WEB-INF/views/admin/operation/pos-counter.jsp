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
    <title>Quầy bán vé | CineMax</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/operation.css?v=6">
</head>
<body>
<c:url var="fnbFallbackUrl" value="/assets/images/fnb/default.svg" />
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <main class="admin-content operation-page pos-page">
            <header class="operation-heading">
                <div>
                    <span class="operation-eyebrow">VẬN HÀNH · POS</span>
                    <h1>Quầy bán vé</h1>
                    <p>Chọn suất chiếu, ghế và bắp nước để tạo giao dịch tại quầy.</p>
                </div>
                <div class="operator-chip">
                    <span class="operator-dot"></span>
                    <span><strong><c:out value="${staff.fullName}" /></strong><small>Nhân viên đang trực</small></span>
                </div>
            </header>

            <c:if test="${not empty posError}">
                <div class="operation-alert alert-error" role="alert"><span class="alert-icon">!</span><c:out value="${posError}" /></div>
            </c:if>
            <c:if test="${not empty posSetupMessage}">
                <div class="operation-alert alert-info" role="status"><span class="alert-icon">i</span><c:out value="${posSetupMessage}" /></div>
            </c:if>

            <c:if test="${not empty checkoutResult}">
                <section class="receipt-card" id="receipt" aria-labelledby="receipt-title">
                    <div class="receipt-mark">✓</div>
                    <div class="receipt-copy">
                        <span class="operation-eyebrow">GIAO DỊCH HOÀN TẤT</span>
                        <h2 id="receipt-title">Đơn hàng đã được tạo và thanh toán</h2>
                        <p>Mã đơn <strong><c:out value="${checkoutResult.bookingCode}" /></strong><span class="receipt-divider">·</span>Trạng thái <strong><c:out value="${checkoutResult.status}" /></strong></p>
                        <p class="receipt-showtime"><c:out value="${checkoutShowtime.movie.title}" /> · <c:out value="${checkoutShowtime.startTime}" /> · <c:out value="${checkoutShowtime.screeningRoom.name}" /></p>
                        <p class="receipt-showtime">Thanh toán: <strong><c:choose><c:when test="${checkoutResult.paymentMethod eq 'CASH'}">Tiền mặt</c:when><c:otherwise><c:out value="${checkoutResult.paymentMethod}" /></c:otherwise></c:choose></strong></p>
                    </div>
                    <div class="receipt-total"><small>Tổng thanh toán</small><strong><fmt:formatNumber value="${checkoutResult.finalAmount}" pattern="#,##0" groupingUsed="true" /><span>đ</span></strong></div>
                    <button class="button button-secondary receipt-print" type="button" onclick="window.print()">In hóa đơn</button>
                </section>
            </c:if>
            <c:if test="${not empty checkoutResult}">
                <section class="receipt-detail-card" aria-label="Chi tiết xác nhận thanh toán">
                    <h3>Chi tiết đơn hàng đã thanh toán</h3>
                    <div class="receipt-detail-line"><span>Số ghế</span><strong><c:out value="${fn:length(checkoutResult.tickets)}" /> ghế</strong></div>
                    <c:forEach items="${checkoutResult.tickets}" var="ticket">
                        <div class="receipt-detail-line receipt-detail-subline"><span>Ghế <c:out value="${ticket.seatCode}" /></span><strong><fmt:formatNumber value="${ticket.ticketPrice}" pattern="#,##0" groupingUsed="true" /> đ</strong></div>
                    </c:forEach>
                    <div class="receipt-detail-line"><span>Tổng giá vé</span><strong><fmt:formatNumber value="${checkoutResult.totalTicketsAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong></div>
                    <div class="receipt-detail-line"><span>Ưu đãi giá vé</span><strong><c:out value="${empty checkoutResult.ticketDiscountPercent ? 0 : checkoutResult.ticketDiscountPercent}" />% · tiết kiệm <fmt:formatNumber value="${checkoutResult.ticketDiscountAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong></div>
                    <c:if test="${checkoutResult.discountAmount gt 0}">
                        <div class="receipt-detail-line"><span>Giảm thêm từ voucher</span><strong>-<fmt:formatNumber value="${checkoutResult.discountAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong></div>
                    </c:if>
                    <div class="receipt-detail-line"><span>Bắp nước & combo</span><strong><fmt:formatNumber value="${checkoutResult.totalFnbAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong></div>
                    <c:forEach items="${checkoutResult.fnbItems}" var="fnbItem">
                        <div class="receipt-detail-line receipt-detail-subline"><span><c:out value="${fnbItem.itemName}" /> × <c:out value="${fnbItem.quantity}" /> · <fmt:formatNumber value="${fnbItem.unitPrice}" pattern="#,##0" groupingUsed="true" /> đ/phần</span><strong><fmt:formatNumber value="${fnbItem.subtotal}" pattern="#,##0" groupingUsed="true" /> đ</strong></div>
                    </c:forEach>
                    <div class="receipt-detail-line receipt-detail-grand-total"><span>Tổng thanh toán</span><strong><fmt:formatNumber value="${checkoutResult.finalAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong></div>
                </section>
            </c:if>
            <c:if test="${not empty checkoutResult.tickets}">
                <section class="receipt-ticket-card" aria-label="Vé vừa tạo">
                    <h3>Vé đã phát hành</h3>
                    <c:forEach items="${checkoutResult.tickets}" var="ticket">
                        <div class="receipt-ticket-row">
                            <strong>Ghế <c:out value="${ticket.seatCode}" /></strong>
                            <code><c:out value="${ticket.barcode}" /></code>
                            <span><fmt:formatNumber value="${ticket.ticketPrice}" pattern="#,##0" groupingUsed="true" /> đ</span>
                        </div>
                    </c:forEach>
                </section>
            </c:if>

            <section class="admin-card pos-picker-card" aria-labelledby="pos-picker-title">
                <div class="section-heading compact-heading">
                    <div class="section-number">01</div>
                    <div><h2 id="pos-picker-title">Chọn suất chiếu</h2><p>Danh sách suất chiếu trong ngày tại chi nhánh.</p></div>
                </div>
                <form class="pos-picker-form" method="get" action="${pageContext.request.contextPath}/operation/pos">
                    <label class="field-group date-field">Ngày chiếu
                        <input type="date" name="date" value="${selectedDate}" min="${today}"
                               onchange="this.form.elements['showtimeId'].value='';this.form.submit()">
                    </label>
                    <c:choose>
                        <c:when test="${not empty cinemas}">
                            <label class="field-group branch-field">Chi nhánh
                                <select name="cinemaId" onchange="this.form.elements['showtimeId'].value='';this.form.submit()">
                                    <c:forEach items="${cinemas}" var="cinema">
                                        <option value="${cinema.id}" ${cinema.id eq cinemaId ? 'selected' : ''}><c:out value="${cinema.name}" /></option>
                                    </c:forEach>
                                </select>
                            </label>
                        </c:when>
                        <c:otherwise><input type="hidden" name="cinemaId" value="${cinemaId}"></c:otherwise>
                    </c:choose>
                    <label class="field-group showtime-field">Suất chiếu
                        <select name="showtimeId" required>
                            <option value="">Chọn phim và giờ chiếu</option>
                            <c:forEach items="${showtimes}" var="st">
                                <option value="${st.id}" ${st.id eq selectedShowtime ? 'selected' : ''}>
                                    <c:out value="${st.startTime}" /> · <c:out value="${st.movie.title}" /> · <c:out value="${st.screeningRoom.name}" />
                                </option>
                            </c:forEach>
                        </select>
                    </label>
                    <button class="button button-primary picker-submit" type="submit">Tải sơ đồ ghế <span aria-hidden="true">→</span></button>
                </form>
                <c:if test="${empty showtimes and empty posSetupMessage}">
                    <div class="empty-inline">Không có suất chiếu còn bán trong ngày đã chọn.</div>
                </c:if>
            </section>

            <c:if test="${not empty selectedShowtime}">
                <form class="pos-order-form" id="pos-order-form" method="post" action="${pageContext.request.contextPath}/operation/pos">
                    <input type="hidden" name="checkoutConfirmed" value="false">
                    <input type="hidden" name="cinemaId" value="${cinemaId}">
                    <input type="hidden" name="date" value="${selectedDate}">
                    <input type="hidden" name="showtimeId" value="${selectedShowtime}">

                    <div class="pos-main-column">
                        <section class="admin-card operation-card seat-card" aria-labelledby="seat-title">
                            <div class="section-heading">
                                <div class="section-number">02</div>
                                <div><h2 id="seat-title">Chọn ghế</h2><p>Chạm vào ghế còn trống để thêm vào đơn.</p></div>
                            </div>
                            <div class="seat-legend" aria-label="Chú thích trạng thái ghế">
                                <span><i class="legend-seat available"></i>Còn trống</span>
                                <span><i class="legend-seat selected"></i>Đang chọn</span>
                                <span><i class="legend-seat sold"></i>Đã bán</span>
                                <span><i class="legend-seat held"></i>Đang giữ</span>
                                <span><i class="legend-seat unavailable"></i>Không bán</span>
                            </div>
                            <div class="selected-showtime-info">
                                <strong><c:out value="${selectedShowtimeDetails.movie.title}" /></strong>
                                <span><c:out value="${selectedShowtimeDetails.startTime}" /> ·
                                    <c:out value="${selectedShowtimeDetails.screeningRoom.name}" /></span>
                            </div>
                            <div class="screen-label"><span>MÀN HÌNH</span></div>
                            <c:choose>
                                <c:when test="${not empty seatMap}">
                                    <div class="seat-grid-scroll">
                                        <div class="seat-grid" style="grid-template-columns: repeat(${seatColumnCount}, 36px);">
                                            <c:forEach items="${seatMap}" var="seat">
                                                <label class="seat-option ${seat.status eq 'AVAILABLE' ? 'is-available' : seat.status eq 'SOLD' ? 'is-sold' : seat.status eq 'HELD' ? 'is-held' : 'is-unavailable'}"
                                                       style="grid-row: ${seat.gridRowIndex}; grid-column: ${seat.gridColIndex};">
                                                    <c:choose>
                                                        <c:when test="${seat.status eq 'AVAILABLE'}">
                                                            <input type="checkbox" name="seatIds" value="${seat.id}"
                                                                   aria-label="Ghế ${seat.seatCode}, còn trống"
                                                                   data-seat-code="${seat.seatCode}" data-price="${seatPrices[seat.id]}">
                                                        </c:when>
                                                        <c:otherwise>
                                                            <input type="checkbox" disabled aria-label="Ghế ${seat.seatCode}, ${seat.status}">
                                                        </c:otherwise>
                                                    </c:choose>
                                                    <span><c:out value="${seat.seatCode}" /></span>
                                                </label>
                                            </c:forEach>
                                        </div>
                                    </div>
                                    <c:if test="${availableSeatCount eq 0}">
                                        <div class="empty-inline">Suất chiếu đã hết ghế bán được.</div>
                                    </c:if>
                                </c:when>
                                <c:otherwise><div class="empty-state"><span>○</span><strong>Phòng chưa có sơ đồ ghế</strong><small>Hãy cấu hình sơ đồ ghế cho phòng chiếu trước khi bán.</small></div></c:otherwise>
                            </c:choose>
                        </section>

                        <section class="admin-card operation-card ticket-class-card" aria-labelledby="ticket-class-title">
                            <div class="section-heading">
                                <div class="section-number">03</div>
                                <div><h2 id="ticket-class-title">Loại giá vé</h2><p>Nhân viên xác minh giấy tờ ưu đãi tại quầy.</p></div>
                            </div>
                            <div class="ticket-class-options">
                                <label class="choice-card is-selected"><input type="radio" name="ticketClass" value="STANDARD" data-discount-percent="0" checked><span class="choice-radio"></span><span class="choice-copy"><strong>Tiêu chuẩn</strong><small>Giá vé thông thường</small></span><span class="choice-tag">STANDARD</span></label>
                                <label class="choice-card"><input type="radio" name="ticketClass" value="STUDENT" data-discount-percent="${studentDiscount}"><span class="choice-radio"></span><span class="choice-copy"><strong>Học sinh / Sinh viên</strong><small>Giảm <c:out value="${studentDiscount}" />% · cần thẻ hợp lệ</small></span><span class="choice-tag">STUDENT</span></label>
                                <label class="choice-card"><input type="radio" name="ticketClass" value="SENIOR" data-discount-percent="${seniorDiscount}"><span class="choice-radio"></span><span class="choice-copy"><strong>Người cao tuổi</strong><small>Giảm <c:out value="${seniorDiscount}" />% · cần giấy tờ hợp lệ</small></span><span class="choice-tag">SENIOR</span></label>
                            </div>
                        </section>

                        <section class="admin-card operation-card concessions-card" aria-labelledby="concessions-title">
                            <div class="section-heading">
                                <div class="section-number">04</div>
                                <div><h2 id="concessions-title">Bắp nước & combo</h2><p>Chạm vào thẻ món để thêm một phần, hoặc chỉnh số lượng.</p></div>
                                <span class="section-count"><c:out value="${empty fnbItems ? 0 : fnbItems.size()}" /> sản phẩm</span>
                            </div>
                            <c:choose>
                                <c:when test="${not empty fnbItems}">
                                    <div class="pos-product-grid">
                                        <c:forEach items="${fnbItems}" var="item">
                                            <article class="pos-product-card ${item.stockQuantity le 0 ? 'is-out' : ''}" ${item.stockQuantity gt 0 ? 'data-fnb-card' : ''}>
                                                <div class="product-card-top">
                                                    <c:choose>
                                                        <c:when test="${empty item.imageUrl}"><c:set var="itemImageUrl" value="${fnbFallbackUrl}" /></c:when>
                                                        <c:when test="${fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), 'http://') or fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), 'https://')}"><c:set var="itemImageUrl" value="${fn:trim(item.imageUrl)}" /></c:when>
                                                        <c:when test="${fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), '/assets/images/fnb/') and fn:endsWith(fn:toLowerCase(fn:trim(item.imageUrl)), '.png')}"><c:url var="itemImageUrl" value="${fn:replace(fn:trim(item.imageUrl), '.png', '.svg')}" /></c:when>
                                                        <c:when test="${fn:startsWith(fn:trim(item.imageUrl), '/')}"><c:url var="itemImageUrl" value="${fn:trim(item.imageUrl)}" /></c:when>
                                                        <c:otherwise><c:set var="relativeItemImageUrl">/${fn:trim(item.imageUrl)}</c:set><c:url var="itemImageUrl" value="${relativeItemImageUrl}" /></c:otherwise>
                                                    </c:choose>
                                                    <img class="product-image" src="<c:out value='${itemImageUrl}' />" data-fallback="<c:out value='${fnbFallbackUrl}' />" onerror="this.onerror=null;this.src=this.dataset.fallback" alt="<c:out value='${item.name}' />" loading="lazy">
                                                    <c:if test="${item.isCombo}"><span class="combo-tag">COMBO</span></c:if>
                                                </div>
                                                <span class="product-category"><c:out value="${item.category.name}" /></span>
                                                <h3><c:out value="${item.name}" /></h3>
                                                <div class="product-price"><fmt:formatNumber value="${item.price}" pattern="#,##0" groupingUsed="true" /><span>đ</span></div>
                                                <c:choose>
                                                    <c:when test="${item.stockQuantity le 0}"><span class="stock-label stock-empty">Hết hàng</span></c:when>
                                                    <c:otherwise>
                                                        <div class="product-quantity"><label for="fnb-${item.id}">Số lượng</label><input id="fnb-${item.id}" type="number" name="fnb_${item.id}" min="0" max="${item.stockQuantity le 99 ? item.stockQuantity : 99}" value="0" data-fnb-quantity data-price="${item.price}" data-name="<c:out value='${item.name}' />"></div>
                                                        <span class="stock-label">Còn <c:out value="${item.stockQuantity}" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </article>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise><div class="empty-state"><span>◌</span><strong>Chưa có sản phẩm còn hàng</strong><small>Kiểm tra tồn kho của chi nhánh rồi thử lại.</small></div></c:otherwise>
                            </c:choose>
                        </section>
                    </div>

                    <aside class="pos-summary-column">
                        <section class="admin-card operation-card order-summary-card" aria-labelledby="summary-title">
                            <div class="section-heading summary-heading">
                                <div class="section-number">05</div>
                                <div><h2 id="summary-title">Thanh toán</h2><p>Kiểm tra thông tin trước khi xác nhận.</p></div>
                            </div>
                            <div class="summary-line"><span>Ghế đã chọn</span><strong><span id="selected-seat-count">0</span> ghế</strong></div>
                            <div class="summary-detail-list" id="selected-seat-list" aria-live="polite"><span class="summary-empty">Chưa chọn ghế.</span></div>
                            <div class="summary-line"><span>Tổng giá vé</span><strong id="ticket-subtotal">0đ</strong></div>
                            <div class="summary-line"><span>Ưu đãi vé</span><strong id="ticket-discount">0%</strong></div>
                            <div class="summary-line"><span>Bắp nước</span><strong id="selected-fnb-count">0 món</strong></div>
                            <div class="summary-detail-list" id="selected-fnb-list"><span class="summary-empty">Chưa chọn bắp nước.</span></div>
                            <div class="summary-line summary-fnb-total"><span>Tạm tính bắp nước</span><strong id="fnb-subtotal">0đ</strong></div>
                            <div class="summary-line summary-grand-total"><span>Tổng thanh toán</span><strong id="grand-total">0đ</strong></div>
                            <p class="summary-help">Giá vé từng ghế được tính theo suất chiếu, loại ghế và ưu đãi đã chọn.</p>
                            <label class="field-group payment-field">Phương thức thanh toán
                                <select name="paymentMethod" required>
                                    <option value="CASH">Tiền mặt</option>
                                    <option value="VNPAY_POS" disabled>Quét QR VNPay POS (Bảo trì)</option>
                                </select>
                            </label>
                            <button class="button button-primary checkout-button" type="submit" ${availableSeatCount eq 0 ? 'disabled' : ''}>Tạo đơn & thanh toán <span aria-hidden="true">→</span></button>
                            <p class="checkout-note">Đơn hàng sẽ được ghi nhận sau khi xác nhận thanh toán tại quầy.</p>
                        </section>
                    </aside>
                </form>
                <div class="pos-confirm-backdrop" id="pos-confirmation" hidden>
                    <section class="pos-confirm-dialog" role="dialog" aria-modal="true" aria-labelledby="pos-confirm-title" aria-describedby="pos-confirm-description" tabindex="-1">
                        <div class="pos-confirm-heading">
                            <span class="operation-eyebrow">KIỂM TRA ĐƠN HÀNG</span>
                            <h2 id="pos-confirm-title">Xác nhận tạo đơn & thanh toán</h2>
                            <p id="pos-confirm-description">Đơn chỉ được tạo sau khi bạn xác nhận các thông tin bên dưới.</p>
                        </div>
                        <p class="pos-confirm-showtime" id="confirm-showtime"></p>
                        <div class="pos-confirm-details">
                            <div class="pos-confirm-line"><span>Ghế đã chọn</span><strong id="confirm-seat-count"></strong></div>
                            <div class="pos-confirm-list" id="confirm-seat-list"></div>
                            <div class="pos-confirm-line"><span>Tổng giá vé sau ưu đãi</span><strong id="confirm-ticket-total"></strong></div>
                            <div class="pos-confirm-line"><span>Ưu đãi vé</span><strong id="confirm-ticket-discount"></strong></div>
                            <div class="pos-confirm-line"><span>Bắp nước & combo</span><strong id="confirm-fnb-count"></strong></div>
                            <div class="pos-confirm-list" id="confirm-fnb-list"></div>
                            <div class="pos-confirm-line"><span>Tổng bắp nước</span><strong id="confirm-fnb-total"></strong></div>
                            <div class="pos-confirm-line"><span>Thanh toán bằng</span><strong id="confirm-payment"></strong></div>
                            <div class="pos-confirm-line pos-confirm-grand-total"><span>Tổng thanh toán</span><strong id="confirm-grand-total"></strong></div>
                        </div>
                        <div class="pos-confirm-actions">
                            <button class="button button-secondary" id="cancel-pos-confirmation" type="button">Quay lại chỉnh sửa</button>
                            <button class="button button-primary" id="confirm-pos-checkout" type="button">Xác nhận thanh toán</button>
                        </div>
                    </section>
                </div>
            </c:if>
        </main>
    </div>
</div>
<script>
    (() => {
        const form = document.querySelector('.pos-order-form');
        const seatCount = document.getElementById('selected-seat-count');
        const selectedSeatList = document.getElementById('selected-seat-list');
        const ticketSubtotal = document.getElementById('ticket-subtotal');
        const ticketDiscount = document.getElementById('ticket-discount');
        const fnbCount = document.getElementById('selected-fnb-count');
        const fnbSubtotal = document.getElementById('fnb-subtotal');
        const selectedFnbList = document.getElementById('selected-fnb-list');
        const grandTotal = document.getElementById('grand-total');
        const confirmation = document.getElementById('pos-confirmation');
        const confirmationDialog = confirmation?.querySelector('.pos-confirm-dialog');
        const confirmButton = document.getElementById('confirm-pos-checkout');
        const cancelConfirmation = document.getElementById('cancel-pos-confirmation');
        if (!form || !seatCount || !fnbCount || !fnbSubtotal) return;
        const confirmationInput = form.querySelector('input[name="checkoutConfirmed"]');

        const money = new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 });
        const formatMoney = amount => money.format(Math.round(amount)) + 'đ';
        const appendEmptyMessage = (container, message) => {
            const empty = document.createElement('span');
            empty.className = 'summary-empty';
            empty.textContent = message;
            container.appendChild(empty);
        };

        const updateSummary = () => {
            const selectedSeats = [...form.querySelectorAll('input[name="seatIds"]:checked')];
            const discountPercent = Number(form.querySelector('input[name="ticketClass"]:checked')?.dataset.discountPercent || 0);
            let baseTicketTotal = 0;
            let discountedTicketTotal = 0;
            selectedSeatList.replaceChildren();
            if (selectedSeats.length === 0) {
                appendEmptyMessage(selectedSeatList, 'Chưa chọn ghế.');
            }
            selectedSeats.forEach(seat => {
                const basePrice = Number(seat.dataset.price || 0);
                const price = Math.round(basePrice * (100 - discountPercent)) / 100;
                baseTicketTotal += basePrice;
                discountedTicketTotal += price;
                const row = document.createElement('div');
                row.className = 'summary-detail-row';
                const seatName = document.createElement('span');
                seatName.textContent = 'Ghế ' + seat.dataset.seatCode;
                const seatPrice = document.createElement('strong');
                seatPrice.textContent = formatMoney(price);
                row.append(seatName, seatPrice);
                selectedSeatList.appendChild(row);
            });

            const quantities = [...document.querySelectorAll('[data-fnb-quantity]')];
            let items = 0;
            let fnbTotal = 0;
            selectedFnbList.replaceChildren();
            quantities.forEach(input => {
                const quantity = Math.max(0, Math.min(Number(input.max) || 0, Math.floor(Number(input.value) || 0)));
                items += quantity;
                const unitPrice = Number(input.dataset.price || 0);
                const lineTotal = quantity * unitPrice;
                fnbTotal += lineTotal;
                if (quantity > 0) {
                    const row = document.createElement('div');
                    row.className = 'summary-detail-row';
                    const itemName = document.createElement('span');
                    itemName.textContent = input.dataset.name + ' × ' + quantity;
                    const itemTotal = document.createElement('strong');
                    itemTotal.textContent = formatMoney(lineTotal);
                    row.append(itemName, itemTotal);
                    selectedFnbList.appendChild(row);
                }
            });
            if (items === 0) appendEmptyMessage(selectedFnbList, 'Chưa chọn bắp nước.');

            seatCount.textContent = selectedSeats.length;
            fnbCount.textContent = items + ' phần';
            ticketSubtotal.textContent = formatMoney(discountedTicketTotal);
            ticketDiscount.textContent = discountPercent + '%' + (discountPercent > 0
                    ? ' · giảm ' + formatMoney(baseTicketTotal - discountedTicketTotal)
                    : '');
            fnbSubtotal.textContent = formatMoney(fnbTotal);
            grandTotal.textContent = formatMoney(discountedTicketTotal + fnbTotal);
        };

        form.querySelectorAll('input[name="seatIds"]').forEach(input => {
            input.addEventListener('change', updateSummary);
        });
        form.querySelectorAll('[data-fnb-quantity]').forEach(input => {
            const updateQuantity = () => {
                const max = Number(input.max) || 0;
                input.value = Math.max(0, Math.min(max, Math.floor(Number(input.value) || 0)));
                updateSummary();
            };
            input.addEventListener('change', updateQuantity);
            input.addEventListener('input', updateQuantity);
        });
        form.querySelector('.pos-product-grid')?.addEventListener('click', event => {
            const card = event.target.closest('[data-fnb-card]');
            if (!card || event.target.closest('input, label, select, a, button')) return;
            const input = card.querySelector('[data-fnb-quantity]');
            if (!input) return;
            const max = Number(input.max) || 0;
            const current = Math.max(0, Math.floor(Number(input.value) || 0));
            if (current < max) {
                input.value = current + 1;
                updateSummary();
            }
        });
        form.addEventListener('submit', event => {
            if (confirmationInput?.value === 'true') {
                form.querySelector('.checkout-button').disabled = true;
                return;
            }
            event.preventDefault();
            const selectedSeats = form.querySelectorAll('input[name="seatIds"]:checked');
            if (selectedSeats.length === 0) {
                const firstSeat = form.querySelector('input[name="seatIds"]');
                if (firstSeat) {
                    firstSeat.focus();
                    firstSeat.closest('.seat-card').scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
                return;
            }
            if (!confirmation) return;
            const copyRows = (sourceId, targetId) => {
                const source = document.getElementById(sourceId);
                const target = document.getElementById(targetId);
                target.replaceChildren(...[...source.children].map(row => row.cloneNode(true)));
            };
            document.getElementById('confirm-showtime').textContent =
                    document.querySelector('.selected-showtime-info')?.innerText.trim() || '';
            document.getElementById('confirm-seat-count').textContent = selectedSeats.length + ' ghế';
            copyRows('selected-seat-list', 'confirm-seat-list');
            document.getElementById('confirm-ticket-total').textContent = ticketSubtotal.textContent;
            document.getElementById('confirm-ticket-discount').textContent = ticketDiscount.textContent;
            document.getElementById('confirm-fnb-count').textContent = fnbCount.textContent;
            copyRows('selected-fnb-list', 'confirm-fnb-list');
            document.getElementById('confirm-fnb-total').textContent = fnbSubtotal.textContent;
            document.getElementById('confirm-payment').textContent =
                    form.querySelector('[name="paymentMethod"] option:checked')?.textContent.trim() || '';
            document.getElementById('confirm-grand-total').textContent = grandTotal.textContent;
            confirmation.hidden = false;
            confirmationDialog?.focus();
        });
        confirmButton?.addEventListener('click', () => {
            if (confirmationInput.value === 'true') return;
            confirmationInput.value = 'true';
            confirmButton.disabled = true;
            confirmButton.textContent = 'Đang tạo đơn…';
            if (form.requestSubmit) form.requestSubmit();
            else form.submit();
        });
        cancelConfirmation?.addEventListener('click', () => {
            confirmation.hidden = true;
            form.querySelector('.checkout-button')?.focus();
        });
        confirmation?.addEventListener('click', event => {
            if (event.target === confirmation) {
                confirmation.hidden = true;
                form.querySelector('.checkout-button')?.focus();
            }
        });
        document.querySelectorAll('.choice-card input').forEach(input => {
            input.addEventListener('change', () => {
                document.querySelectorAll('.choice-card').forEach(card => card.classList.remove('is-selected'));
                input.closest('.choice-card').classList.add('is-selected');
                updateSummary();
            });
        });
        updateSummary();
    })();
</script>
</body>
</html>
