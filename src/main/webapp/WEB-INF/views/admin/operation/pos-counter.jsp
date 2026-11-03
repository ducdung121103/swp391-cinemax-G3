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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/operation.css?v=3">
    <style>
        .legend-seat.couple { border-color: #d63384; background: #fff0f6; }
        .seat-option.seat-couple span {
            border-color: #f783ac;
            background: #fff0f6;
            color: #d63384;
            font-weight: 700;
        }
        .seat-option.seat-couple:hover span {
            border-color: #d63384;
            background: #fcc2d7;
            color: #a61e4d;
            transform: translateY(-2px);
        }
        .seat-option.seat-couple input:checked + span {
            background: #d63384;
            border-color: #a61e4d;
            color: #ffffff;
            box-shadow: 0 0 8px rgba(214, 51, 132, 0.4);
        }
    </style>
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
                        <h2 id="receipt-title">Đơn hàng đã được tạo</h2>
                        <p>Mã đơn <strong><c:out value="${checkoutResult.bookingCode}" /></strong><span class="receipt-divider">·</span>Trạng thái <strong><c:out value="${checkoutResult.status}" /></strong></p>
                    </div>
                    <div class="receipt-total"><small>Tổng thanh toán</small><strong><fmt:formatNumber value="${checkoutResult.finalAmount}" pattern="#,##0" groupingUsed="true" /><span>đ</span></strong></div>
                    <button class="button button-secondary receipt-print" type="button" onclick="window.print()">In hóa đơn</button>
                </section>
            </c:if>

            <section class="admin-card pos-picker-card" aria-labelledby="pos-picker-title">
                <div class="section-heading compact-heading">
                    <div class="section-number">01</div>
                    <div><h2 id="pos-picker-title">Chọn suất chiếu</h2><p>Danh sách suất chiếu trong ngày tại chi nhánh.</p></div>
                </div>
                <form class="pos-picker-form" method="get" action="${pageContext.request.contextPath}/admin/operation/pos">
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
                    <div class="empty-inline">Hôm nay chưa có suất chiếu để bán.</div>
                </c:if>
            </section>

            <c:if test="${not empty selectedShowtime}">
                <form class="pos-order-form" method="post" action="${pageContext.request.contextPath}/admin/operation/pos">
                    <input type="hidden" name="cinemaId" value="${cinemaId}">
                    <input type="hidden" name="showtimeId" value="${selectedShowtime}">

                    <div class="pos-main-column">
                        <section class="admin-card operation-card seat-card" aria-labelledby="seat-title">
                            <div class="section-heading">
                                <div class="section-number">02</div>
                                <div><h2 id="seat-title">Chọn ghế</h2><p>Chạm vào ghế còn trống để thêm vào đơn.</p></div>
                            </div>
                            <div class="seat-legend" aria-label="Chú thích trạng thái ghế">
                                <span><i class="legend-seat available"></i>Tiêu chuẩn / VIP</span>
                                <span><i class="legend-seat couple"></i>Ghế đôi Sweetbox</span>
                                <span><i class="legend-seat selected"></i>Đang chọn</span>
                            </div>
                            <div class="screen-label"><span>MÀN HÌNH</span></div>
                            <c:choose>
                                <c:when test="${not empty availableSeats}">
                                    <div class="seat-grid">
                                        <c:forEach items="${availableSeats}" var="seat">
                                            <c:set var="isCouple" value="${seat.seatTypeId eq 3 or (not empty seat.seatType and seat.seatType.typeCode eq 'COUPLE')}" />
                                            <label class="seat-option ${isCouple ? 'seat-couple' : ''}" title="${isCouple ? 'Ghế đôi Sweetbox (Yêu cầu chọn theo cặp liền kề)' : 'Ghế tiêu chuẩn / VIP'}">
                                                <input type="checkbox" name="seatIds" value="${seat.id}"
                                                       data-is-couple="${isCouple}"
                                                       data-row="${seat.seatRow}"
                                                       data-number="${seat.seatNumber}"
                                                       data-code="${seat.seatCode}">
                                                <span><c:out value="${seat.seatCode}" /></span>
                                            </label>
                                        </c:forEach>
                                    </div>
                                    <div id="couple-seat-warning" class="operation-alert alert-error" style="display:none; margin-top:14px;" role="alert">
                                        <span class="alert-icon">!</span>
                                        <span id="couple-seat-warning-text"></span>
                                    </div>
                                </c:when>
                                <c:otherwise><div class="empty-state"><span>◌</span><strong>Chưa có ghế trống</strong><small>Suất chiếu đã hết ghế hoặc phòng chưa được cấu hình.</small></div></c:otherwise>
                            </c:choose>
                        </section>

                        <section class="admin-card operation-card ticket-class-card" aria-labelledby="ticket-class-title">
                            <div class="section-heading">
                                <div class="section-number">03</div>
                                <div><h2 id="ticket-class-title">Loại giá vé</h2><p>Nhân viên xác minh giấy tờ ưu đãi tại quầy.</p></div>
                            </div>
                            <div class="ticket-class-options">
                                <label class="choice-card is-selected"><input type="radio" name="ticketClass" value="STANDARD" checked><span class="choice-radio"></span><span class="choice-copy"><strong>Tiêu chuẩn</strong><small>Giá vé thông thường</small></span><span class="choice-tag">STANDARD</span></label>
                                <label class="choice-card"><input type="radio" name="ticketClass" value="STUDENT"><span class="choice-radio"></span><span class="choice-copy"><strong>Học sinh / Sinh viên</strong><small>Giảm <c:out value="${studentDiscount}" />% · cần thẻ hợp lệ</small></span><span class="choice-tag">STUDENT</span></label>
                                <label class="choice-card"><input type="radio" name="ticketClass" value="SENIOR"><span class="choice-radio"></span><span class="choice-copy"><strong>Người cao tuổi</strong><small>Giảm <c:out value="${seniorDiscount}" />% · cần giấy tờ hợp lệ</small></span><span class="choice-tag">SENIOR</span></label>
                            </div>
                        </section>

                        <section class="admin-card operation-card concessions-card" aria-labelledby="concessions-title">
                            <div class="section-heading">
                                <div class="section-number">04</div>
                                <div><h2 id="concessions-title">Bắp nước & combo</h2><p>Chọn số lượng muốn bán kèm vé.</p></div>
                                <span class="section-count"><c:out value="${empty fnbItems ? 0 : fnbItems.size()}" /> sản phẩm</span>
                            </div>
                            <c:choose>
                                <c:when test="${not empty fnbItems}">
                                    <div class="pos-product-grid">
                                        <c:forEach items="${fnbItems}" var="item">
                                            <article class="pos-product-card ${item.stockQuantity le 0 ? 'is-out' : ''}">
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
                                                        <div class="product-quantity"><label for="fnb-${item.id}">Số lượng</label><input id="fnb-${item.id}" type="number" name="fnb_${item.id}" min="0" max="${item.stockQuantity}" value="0" data-fnb-quantity data-price="${item.price}"></div>
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
                            <div class="summary-line">
                                <span>Ghế đã chọn</span>
                                <strong><span id="selected-seat-count">0</span> ghế</strong>
                            </div>
                            <div id="couple-seat-summary-line" style="display:none; font-size:11px; margin-top:-6px; margin-bottom:8px; padding: 4px 8px; border-radius: 4px; background: #fff0f6; border: 1px solid #fcc2d7;">
                                <span id="couple-seat-summary-text" style="color: #d63384; font-weight: 600;"></span>
                            </div>
                            <div class="summary-line"><span>Bắp nước</span><strong id="selected-fnb-count">0 món</strong></div>
                            <div class="summary-line summary-fnb-total"><span>Tạm tính bắp nước</span><strong id="fnb-subtotal">0đ</strong></div>
                            <p class="summary-help">Giá vé và tổng thanh toán được tính khi tạo đơn.</p>
                            <label class="field-group payment-field">Phương thức thanh toán
                                <select name="paymentMethod" required>
                                    <option value="CASH">Tiền mặt</option>
                                    <option value="VNPAY_POS">Quét QR VNPay POS</option>
                                </select>
                            </label>
                            <button class="button button-primary checkout-button" type="submit" ${empty availableSeats ? 'disabled' : ''}>Tạo đơn & thanh toán <span aria-hidden="true">→</span></button>
                            <p class="checkout-note">Đơn hàng sẽ được ghi nhận sau khi xác nhận thanh toán tại quầy.</p>
                        </section>
                    </aside>
                </form>
            </c:if>
        </main>
    </div>
</div>
<script>
    (() => {
        const seatCount = document.getElementById('selected-seat-count');
        const fnbCount = document.getElementById('selected-fnb-count');
        const fnbSubtotal = document.getElementById('fnb-subtotal');
        const coupleSummaryLine = document.getElementById('couple-seat-summary-line');
        const coupleSummaryText = document.getElementById('couple-seat-summary-text');
        const coupleWarningBox = document.getElementById('couple-seat-warning');
        const coupleWarningText = document.getElementById('couple-seat-warning-text');

        if (!seatCount || !fnbCount || !fnbSubtotal) return;

        const money = new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 });

        // Hàm kiểm tra nghiệp vụ chọn ghế đôi Sweetbox
        const validateCoupleSeatsSelection = (showWarning = false) => {
            const coupleChecked = [...document.querySelectorAll('input[name="seatIds"]:checked')]
                .filter(inp => inp.dataset.isCouple === 'true');

            if (coupleChecked.length === 0) {
                if (coupleSummaryLine) coupleSummaryLine.style.display = 'none';
                if (coupleWarningBox) coupleWarningBox.style.display = 'none';
                return { valid: true, count: 0 };
            }

            // Quy tắc 1: Tổng số lượng ghế đôi Sweetbox phải là số chẵn
            if (coupleChecked.length % 2 !== 0) {
                const msg = 'Ghế đôi Sweetbox phải được chọn theo số lượng chẵn (bạn đang chọn ' + coupleChecked.length + ' ghế đôi; ví dụ: 2, 4, 6... ghế, không được chọn số lẻ như 1 hay 3 ghế)!';
                if (coupleSummaryLine && coupleSummaryText) {
                    coupleSummaryLine.style.display = 'block';
                    coupleSummaryLine.style.background = '#fef2f2';
                    coupleSummaryLine.style.borderColor = '#fca5a5';
                    coupleSummaryText.style.color = '#dc2626';
                    coupleSummaryText.textContent = '⚠️ Ghế đôi: ' + coupleChecked.length + ' (cần số chẵn)';
                }
                if (showWarning && coupleWarningBox && coupleWarningText) {
                    coupleWarningText.textContent = msg;
                    coupleWarningBox.style.display = 'flex';
                }
                return { valid: false, message: msg, count: coupleChecked.length };
            }

            // Quy tắc 2: Phải đi theo cặp cùng hàng (không chọn chéo hàng)
            const byRow = {};
            coupleChecked.forEach(inp => {
                const row = (inp.dataset.row || '').toUpperCase();
                if (!byRow[row]) byRow[row] = [];
                byRow[row].push(inp);
            });

            for (const row in byRow) {
                const rowSeats = byRow[row];
                if (rowSeats.length % 2 !== 0) {
                    const msg = 'Ghế đôi Sweetbox phải được chọn theo cặp trong cùng một hàng (hàng ' + row + ' đang chọn ' + rowSeats.length + ' ghế lẻ, không thể ghép cặp)!';
                    if (coupleSummaryLine && coupleSummaryText) {
                        coupleSummaryLine.style.display = 'block';
                        coupleSummaryLine.style.background = '#fef2f2';
                        coupleSummaryLine.style.borderColor = '#fca5a5';
                        coupleSummaryText.style.color = '#dc2626';
                        coupleSummaryText.textContent = '⚠️ Hàng ' + row + ' có ghế lẻ';
                    }
                    if (showWarning && coupleWarningBox && coupleWarningText) {
                        coupleWarningText.textContent = msg;
                        coupleWarningBox.style.display = 'flex';
                    }
                    return { valid: false, message: msg, count: coupleChecked.length };
                }

                // Quy tắc 3: Trong cùng một hàng, các ghế phải liền kề nhau theo từng cặp (không được cách quãng)
                rowSeats.sort((a, b) => Number(a.dataset.number) - Number(b.dataset.number));
                for (let i = 0; i < rowSeats.length; i += 2) {
                    const s1 = rowSeats[i];
                    const s2 = rowSeats[i + 1];
                    const num1 = Number(s1.dataset.number);
                    const num2 = Number(s2.dataset.number);
                    const diff = Math.abs(num2 - num1);
                    if (diff !== 1) {
                        const gap = diff - 1;
                        const msg = 'Ghế đôi Sweetbox được chọn phải liền kề nhau, không được cách quãng! Cặp ghế ' + s1.dataset.code + ' và ' + s2.dataset.code + ' đang cách nhau ' + gap + ' ghế, không hợp lệ.';
                        if (coupleSummaryLine && coupleSummaryText) {
                            coupleSummaryLine.style.display = 'block';
                            coupleSummaryLine.style.background = '#fef2f2';
                            coupleSummaryLine.style.borderColor = '#fca5a5';
                            coupleSummaryText.style.color = '#dc2626';
                            coupleSummaryText.textContent = '⚠️ Ghế đôi không nằm cạnh nhau';
                        }
                        if (showWarning && coupleWarningBox && coupleWarningText) {
                            coupleWarningText.textContent = msg;
                            coupleWarningBox.style.display = 'flex';
                        }
                        return { valid: false, message: msg, count: coupleChecked.length };
                    }
                }
            }

            // Hợp lệ hoàn toàn
            if (coupleSummaryLine && coupleSummaryText) {
                coupleSummaryLine.style.display = 'block';
                coupleSummaryLine.style.background = '#f0fdf4';
                coupleSummaryLine.style.borderColor = '#86efac';
                coupleSummaryText.style.color = '#16a34a';
                coupleSummaryText.textContent = '✓ ' + coupleChecked.length + ' ghế đôi Sweetbox (' + (coupleChecked.length / 2) + ' cặp liền kề)';
            }
            if (coupleWarningBox) coupleWarningBox.style.display = 'none';
            return { valid: true, count: coupleChecked.length };
        };

        const updateSummary = () => {
            const selectedSeats = document.querySelectorAll('input[name="seatIds"]:checked').length;
            const quantities = [...document.querySelectorAll('[data-fnb-quantity]')];
            let items = 0;
            let subtotal = 0;
            quantities.forEach(input => {
                const quantity = Math.max(0, Math.min(Number(input.max), Number(input.value) || 0));
                items += quantity;
                subtotal += quantity * Number(input.dataset.price || 0);
            });
            seatCount.textContent = selectedSeats;
            fnbCount.textContent = `${items} món`;
            fnbSubtotal.textContent = `${money.format(subtotal)}đ`;

            // Kiểm tra trạng thái ghế đôi Sweetbox realtime
            validateCoupleSeatsSelection(false);
        };

        document.querySelectorAll('input[name="seatIds"], [data-fnb-quantity]').forEach(input => {
            input.addEventListener('change', updateSummary);
            input.addEventListener('input', updateSummary);
        });

        document.querySelector('.pos-order-form')?.addEventListener('submit', event => {
            const checkedSeats = document.querySelectorAll('input[name="seatIds"]:checked');
            if (checkedSeats.length === 0) {
                event.preventDefault();
                const firstSeat = document.querySelector('input[name="seatIds"]');
                if (firstSeat) {
                    firstSeat.focus();
                    firstSeat.closest('.seat-card').scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
                return;
            }

            // Kiểm tra chặt chẽ ghế đôi Sweetbox trước khi submit
            const coupleValidation = validateCoupleSeatsSelection(true);
            if (!coupleValidation.valid) {
                event.preventDefault();
                alert(coupleValidation.message);
                if (coupleWarningBox) {
                    coupleWarningBox.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });

        document.querySelectorAll('.choice-card input').forEach(input => {
            input.addEventListener('change', () => {
                document.querySelectorAll('.choice-card').forEach(card => card.classList.remove('is-selected'));
                input.closest('.choice-card').classList.add('is-selected');
            });
        });
    })();
</script>
</body>
</html>
