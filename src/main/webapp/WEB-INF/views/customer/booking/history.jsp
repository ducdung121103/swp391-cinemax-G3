<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<c:set var="pageTitle" value="Lịch Sử Mua Vé — CineMax" />
<c:url var="fallbackPoster" value="/assets/images/default-movie.jpg" />
<jsp:include page="../../common/header.jsp" />

<style>
    .booking-history-wrapper {
        min-height: 85vh;
        padding-top: 2rem;
        padding-bottom: 5rem;
        color: #f1f5f9;
    }
    .history-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        flex-wrap: wrap;
        gap: 16px;
        margin-bottom: 28px;
        padding-bottom: 20px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    }
    .history-header h1 {
        font-size: 26px;
        font-weight: 800;
        color: #fff;
        margin: 0 0 6px;
        letter-spacing: -0.02em;
    }
    .history-header p {
        color: #94a3b8;
        font-size: 14px;
        margin: 0;
    }
    .history-filter-nav {
        display: flex;
        gap: 8px;
        background: #141b2b;
        padding: 5px;
        border-radius: 12px;
        border: 1px solid #232f48;
    }
    .filter-pill {
        background: transparent;
        border: none;
        color: #94a3b8;
        font-size: 13px;
        font-weight: 700;
        padding: 8px 18px;
        border-radius: 9px;
        cursor: pointer;
        transition: all 0.2s ease;
    }
    .filter-pill:hover {
        color: #fff;
    }
    .filter-pill.is-active {
        background: #e50914;
        color: #fff;
        box-shadow: 0 4px 12px rgba(229, 9, 20, 0.35);
    }

    /* Booking Card */
    .booking-card {
        background: #0f1523;
        border: 1px solid rgba(255, 255, 255, 0.08);
        border-radius: 16px;
        overflow: hidden;
        margin-bottom: 22px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.35);
        transition: transform 0.2s ease, border-color 0.2s ease;
    }
    .booking-card:hover {
        border-color: rgba(229, 9, 20, 0.4);
        transform: translateY(-2px);
    }
    .booking-card-body {
        display: grid;
        grid-template-columns: 140px 1fr auto;
        gap: 22px;
        padding: 22px;
    }
    @media (max-width: 860px) {
        .booking-card-body {
            grid-template-columns: 100px 1fr;
        }
        .booking-card-actions {
            grid-column: 1 / -1;
            display: flex;
            justify-content: flex-end;
            padding-top: 14px;
            border-top: 1px solid rgba(255, 255, 255, 0.06);
        }
    }
    @media (max-width: 576px) {
        .booking-card-body {
            grid-template-columns: 1fr;
        }
    }

    .booking-poster-wrap {
        width: 100%;
        height: 190px;
        border-radius: 10px;
        overflow: hidden;
        background: #000;
        box-shadow: 0 4px 14px rgba(0,0,0,0.4);
        position: relative;
    }
    .booking-poster {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    .age-badge {
        position: absolute;
        top: 6px;
        left: 6px;
        background: rgba(229, 9, 20, 0.9);
        color: #fff;
        font-size: 10px;
        font-weight: 800;
        padding: 2px 6px;
        border-radius: 4px;
    }

    .booking-main-info {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
    }
    .booking-top-meta {
        display: flex;
        align-items: center;
        gap: 10px;
        flex-wrap: wrap;
        margin-bottom: 8px;
    }
    .code-chip {
        font-family: monospace;
        font-size: 14px;
        font-weight: 800;
        background: #192236;
        color: #f5c518;
        padding: 4px 12px;
        border-radius: 8px;
        border: 1px solid #334366;
        display: inline-flex;
        align-items: center;
        gap: 8px;
    }
    .copy-btn {
        background: none;
        border: none;
        color: #94a3b8;
        cursor: pointer;
        padding: 0;
        font-size: 13px;
        transition: color 0.15s;
    }
    .copy-btn:hover {
        color: #fff;
    }
    .booking-movie-title {
        font-size: 20px;
        font-weight: 800;
        color: #fff;
        margin: 0 0 10px;
        line-height: 1.3;
    }
    .booking-detail-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
        gap: 10px;
        font-size: 13px;
        color: #cbd5e1;
        margin-bottom: 14px;
    }
    .detail-item {
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .detail-item i {
        color: #e50914;
        width: 16px;
        text-align: center;
    }

    .booking-seats-pill {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        background: #141c2c;
        border: 1px solid #222f48;
        padding: 6px 14px;
        border-radius: 8px;
        font-size: 13px;
        margin-top: 4px;
    }
    .booking-seats-pill strong {
        color: #f5c518;
    }

    .fnb-summary-chips {
        display: flex;
        flex-wrap: wrap;
        gap: 6px;
        margin-top: 8px;
    }
    .fnb-chip {
        background: #1e1b2e;
        border: 1px solid #3b2d5c;
        color: #d8b4fe;
        font-size: 11px;
        font-weight: 700;
        padding: 2px 8px;
        border-radius: 5px;
    }

    /* Right Action & Financials */
    .booking-card-actions {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        align-items: flex-end;
        min-width: 180px;
    }
    .booking-amount-box {
        text-align: right;
    }
    .booking-amount-box small {
        font-size: 11px;
        color: #94a3b8;
        display: block;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }
    .booking-amount-box strong {
        font-size: 22px;
        font-weight: 900;
        color: #e50914;
    }

    /* Ticket Detail Modal */
    .ticket-modal-card {
        background: #0f1523;
        border: 1px solid #334155;
        border-radius: 20px;
        color: #f1f5f9;
        overflow: hidden;
    }
    .booking-code-box {
        background: #131b2e;
        border: 2px dashed #f59e0b;
        border-radius: 12px;
        padding: 16px 20px;
        text-align: center;
        margin-bottom: 20px;
    }
    .booking-code-display {
        font-family: monospace;
        font-size: 28px;
        font-weight: 900;
        color: #f5c518;
        letter-spacing: 2px;
        margin: 6px 0;
    }
    .ticket-seat-card {
        background: #141c2c;
        border: 1px solid #23314d;
        border-radius: 10px;
        padding: 12px 16px;
        margin-bottom: 8px;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
</style>

<div class="container booking-history-wrapper">

    <!-- HEADER & BỘ LỌC -->
    <header class="history-header">
        <div>
            <div class="d-inline-flex align-items-center gap-2 mb-2">
                <span class="badge bg-danger text-uppercase fw-bold" style="letter-spacing:0.1em;font-size:10px;">Lịch sử giao dịch</span>
                <span class="text-secondary small">&bull;</span>
                <span class="text-secondary small">Tài khoản: <strong class="text-white"><c:out value="${currentUser.email}" /></strong></span>
            </div>
            <h1>Lịch Sử Mua Vé Của Tôi</h1>
            <p>Quản lý toàn bộ thông tin vé, suất chiếu và mã đơn hàng dùng để in vé tại rạp.</p>
        </div>

        <nav class="history-filter-nav" aria-label="Bộ lọc vé">
            <button class="filter-pill is-active" data-filter="all" onclick="filterBookings('all', this)">
                Tất cả (<c:out value="${totalBookings}" />)
            </button>
            <button class="filter-pill" data-filter="upcoming" onclick="filterBookings('upcoming', this)">
                <i class="fa-solid fa-clock me-1 text-warning"></i> Sắp chiếu
            </button>
            <button class="filter-pill" data-filter="past" onclick="filterBookings('past', this)">
                <i class="fa-solid fa-check-double me-1 text-secondary"></i> Đã xem
            </button>
        </nav>
    </header>

    <!-- DANH SÁCH VÉ / ĐƠN HÀNG -->
    <c:choose>
        <c:when test="${not empty historyList}">
            <div id="booking-list-container">
                <c:forEach items="${historyList}" var="item">
                    <article class="booking-card" data-upcoming="${item.upcoming}" data-code="${item.bookingCode}">
                        <div class="booking-card-body">
                            <!-- POSTER PHIM -->
                            <div class="booking-poster-wrap">
                                <c:choose>
                                    <c:when test="${not empty item.posterUrl}">
                                        <img src="<c:out value='${item.posterUrl}' />" alt="<c:out value='${item.movieTitle}' />" class="booking-poster" onerror="this.onerror=null;this.src='${fallbackPoster}';">
                                    </c:when>
                                    <c:otherwise>
                                        <img src="${fallbackPoster}" alt="Poster" class="booking-poster">
                                    </c:otherwise>
                                </c:choose>
                                <c:if test="${not empty item.ageRating}">
                                    <span class="age-badge"><c:out value="${item.ageRating}" /></span>
                                </c:if>
                            </div>

                            <!-- THÔNG TIN PHIM & SUẤT CHIẾU -->
                            <div class="booking-main-info">
                                <div>
                                    <div class="booking-top-meta">
                                        <span class="code-chip">
                                            <i class="fa-solid fa-ticket text-warning"></i>
                                            <span class="booking-code-text"><c:out value="${item.bookingCode}" /></span>
                                            <button type="button" class="copy-btn" title="Sao chép mã đơn" onclick="copyCode('${item.bookingCode}')">
                                                <i class="fa-regular fa-copy"></i>
                                            </button>
                                        </span>

                                        <span class="text-secondary small">
                                            <i class="fa-regular fa-calendar me-1"></i> <c:out value="${item.formattedCreatedAt}" />
                                        </span>

                                        <!-- TRẠNG THÁI THANH TOÁN -->
                                        <c:choose>
                                            <c:when test="${item.status eq 'CONFIRMED'}">
                                                <span class="badge bg-success bg-opacity-25 text-success border border-success border-opacity-50">
                                                    <i class="fa-solid fa-circle-check me-1"></i> Đã thanh toán
                                                </span>
                                            </c:when>
                                            <c:when test="${item.status eq 'PENDING'}">
                                                <span class="badge bg-warning bg-opacity-25 text-warning border border-warning border-opacity-50">
                                                    <i class="fa-solid fa-clock me-1"></i> Chờ thanh toán
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-danger bg-opacity-25 text-danger border border-danger border-opacity-50">
                                                    <i class="fa-solid fa-ban me-1"></i> Đã hủy
                                                </span>
                                            </c:otherwise>
                                        </c:choose>

                                        <c:if test="${item.channel eq 'POS'}">
                                            <span class="badge bg-info bg-opacity-25 text-info">Tại quầy</span>
                                        </c:if>
                                    </div>

                                    <h2 class="booking-movie-title">
                                        <c:out value="${item.movieTitle}" />
                                    </h2>

                                    <div class="booking-detail-grid">
                                        <div class="detail-item">
                                            <i class="fa-solid fa-location-dot"></i>
                                            <span><strong><c:out value="${item.cinemaName}" /></strong> &bull; <c:out value="${item.roomName}" /> (<c:out value="${item.experienceFormat}" />)</span>
                                        </div>
                                        <div class="detail-item">
                                            <i class="fa-regular fa-clock"></i>
                                            <span>Suất: <strong class="text-warning">${item.formattedShowtime}</strong></span>
                                        </div>
                                    </div>

                                    <!-- GHẾ ĐÃ ĐẶT -->
                                    <div class="booking-seats-pill">
                                        <i class="fa-solid fa-chair text-danger"></i>
                                        <span>Ghế (<c:out value="${item.ticketCount}" /> vé): <strong><c:out value="${item.seatCodesString}" /></strong></span>
                                    </div>

                                    <!-- BẮP NƯỚC ĐI KÈM NẾU CÓ -->
                                    <c:if test="${not empty item.fnbItems}">
                                        <div class="fnb-summary-chips">
                                            <c:forEach items="${item.fnbItems}" var="fnb">
                                                <span class="fnb-chip">
                                                    <i class="fa-solid fa-bowl-food me-1"></i>
                                                    <c:out value="${fnb.itemName}" /> (x<c:out value="${fnb.quantity}" />)
                                                </span>
                                            </c:forEach>
                                        </div>
                                    </c:if>
                                </div>
                            </div>

                            <!-- CỘT PHẢI: GIÁ TIỀN & NÚT XEM VÉ -->
                            <div class="booking-card-actions">
                                <div class="booking-amount-box">
                                    <small>Tổng thanh toán (<c:out value="${not empty item.paymentMethod ? item.paymentMethod : 'VNPAY'}" />)</small>
                                    <strong><fmt:formatNumber value="${item.finalAmount}" pattern="#,##0" groupingUsed="true" /> đ</strong>
                                </div>

                                <div class="d-flex flex-column gap-2 mt-3 w-100">
                                    <button type="button" class="btn btn-cinema-primary btn-sm w-100 fw-bold py-2 shadow"
                                            onclick="openTicketModal('${item.bookingCode}')">
                                        <i class="fa-solid fa-receipt me-1"></i> Xem Thông Tin Vé
                                    </button>

                                    <c:if test="${item.status eq 'PENDING' and item.paymentMethod eq 'VNPAY'}">
                                        <button type="button" class="btn btn-warning btn-sm w-100 fw-bold"
                                                onclick="retryVnpayPayment('${item.bookingCode}')">
                                            <i class="fa-solid fa-credit-card me-1"></i> Tiếp tục thanh toán
                                        </button>
                                        <button type="button" class="btn btn-outline-danger btn-sm w-100"
                                                onclick="cancelPendingPayment('${item.bookingCode}')">
                                            Hủy đơn và giải phóng ghế
                                        </button>
                                    </c:if>

                                    <c:if test="${not empty item.movieId}">
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${item.movieId}" class="btn btn-outline-secondary btn-sm w-100 text-white">
                                            <i class="fa-solid fa-film me-1"></i> Thông tin phim
                                        </a>
                                    </c:if>
                                </div>
                            </div>
                        </div>
                    </article>
                </c:forEach>
            </div>
        </c:when>

        <c:otherwise>
            <!-- EMPTY STATE -->
            <div class="text-center py-5 my-4 p-4 rounded-4" style="background:#0f1523;border:1px dashed #242f48;">
                <div class="mb-3 text-secondary" style="font-size: 64px;">
                    <i class="fa-solid fa-ticket-simple"></i>
                </div>
                <h3 class="fw-bold text-white mb-2">Bạn chưa có đơn đặt vé nào</h3>
                <p class="text-secondary small mb-4" style="max-width: 420px; margin: 0 auto;">
                    Hãy khám phá các siêu phẩm điện ảnh đang chiếu tại hệ thống CineMax và đặt chỗ ngồi ưng ý ngay hôm nay.
                </p>
                <a href="${pageContext.request.contextPath}/movies?type=now_showing" class="btn btn-cinema-primary px-4 py-2 fw-bold">
                    <i class="fa-solid fa-film me-2"></i> Khám Phá Phim Đang Chiếu &rarr;
                </a>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- ========================================================================= -->
<!-- MODAL CHI TIẾT THÔNG TIN VÉ - HIỂN THỊ MÃ ĐƠN HÀNG ĐỂ IN VÉ TẠI RẠP      -->
<!-- ========================================================================= -->
<div class="modal fade" id="ticketDetailModal" tabindex="-1" aria-labelledby="ticketModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered modal-lg">
        <div class="modal-content ticket-modal-card">
            <div class="modal-header border-bottom border-secondary border-opacity-25 px-4 py-3">
                <div class="d-flex align-items-center gap-2">
                    <i class="fa-solid fa-ticket fs-4 text-warning"></i>
                    <div>
                        <h5 class="modal-title fw-bold text-white mb-0" id="ticketModalLabel">Thông Tin Vé Đặt — CineMax</h5>
                        <small class="text-secondary">Chi tiết đơn đặt vé & Hướng dẫn nhận vé tại rạp</small>
                    </div>
                </div>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>

            <div class="modal-body p-4">

                <!-- HỘP NỔI BẬT: MÃ ĐƠN HÀNG DÙNG ĐỂ IN VÉ TẠI RẠP -->
                <div class="booking-code-box">
                    <span class="text-secondary small text-uppercase fw-bold" style="letter-spacing:0.12em;">Mã Đơn Hàng In Vé Tại Rạp</span>
                    <div class="booking-code-display" id="modal-code-display">BK-XXXXX</div>
                    <p class="text-light small mb-2">
                        <i class="fa-solid fa-print text-warning me-1"></i>
                        Nhân viên tại quầy sẽ sử dụng <strong>Mã đơn hàng</strong> này để in vé cứng cho bạn.
                    </p>
                    <button type="button" class="btn btn-outline-warning btn-sm fw-bold px-3" onclick="copyModalCode()">
                        <i class="fa-regular fa-copy me-1"></i> Sao chép mã đơn
                    </button>
                </div>

                <div class="row g-4">
                    <!-- CỘT TRÁI: THÔNG TIN PHIM & SUẤT CHIẾU -->
                    <div class="col-md-6 border-end border-secondary border-opacity-25 pe-md-4">
                        <div class="d-flex gap-3 align-items-start mb-3">
                            <img id="modal-movie-poster" src="${fallbackPoster}" alt="Poster" style="width: 75px; height: 110px; object-fit: cover; border-radius: 8px;">
                            <div>
                                <span class="badge bg-danger mb-1" id="modal-age-rating">T18</span>
                                <h5 class="fw-bold text-white mb-1" id="modal-movie-title">Tên Phim</h5>
                                <div class="text-warning small fw-semibold" id="modal-format-duration">2D &bull; 120 phút</div>
                            </div>
                        </div>

                        <div class="p-3 rounded-3 mb-3" style="background:#141b2b;border:1px solid #232f48;">
                            <div class="d-flex justify-content-between py-1 small">
                                <span class="text-secondary">Rạp chiếu:</span>
                                <strong class="text-white text-end" id="modal-cinema-name">--</strong>
                            </div>
                            <div class="d-flex justify-content-between py-1 small">
                                <span class="text-secondary">Phòng chiếu:</span>
                                <strong class="text-white" id="modal-room-name">--</strong>
                            </div>
                            <div class="d-flex justify-content-between py-1 small">
                                <span class="text-secondary">Suất chiếu:</span>
                                <strong class="text-warning" id="modal-showtime">--</strong>
                            </div>
                            <div class="d-flex justify-content-between py-1 small border-top border-secondary border-opacity-25 mt-2 pt-2">
                                <span class="text-secondary">Ghế đã đặt:</span>
                                <strong class="text-danger" id="modal-seat-codes">--</strong>
                            </div>
                        </div>

                        <!-- BẮP NƯỚC KÈM THEO -->
                        <div id="modal-fnb-section" style="display: none;">
                            <h6 class="small fw-bold text-secondary text-uppercase mb-2">Bắp nước kèm đơn</h6>
                            <div class="p-3 rounded-3 mb-3" style="background:#141b2b;border:1px solid #232f48;" id="modal-fnb-list">
                            </div>
                        </div>

                        <div class="d-flex justify-content-between align-items-center pt-2">
                            <span class="text-secondary small">Tổng thanh toán:</span>
                            <strong class="fs-4 text-warning fw-bold" id="modal-final-amount">0 đ</strong>
                        </div>
                    </div>

                    <!-- CỘT PHẢI: DANH SÁCH CHI TIẾT TỪNG VÉ GHẾ -->
                    <div class="col-md-6 ps-md-4">
                        <h6 class="fw-bold text-white mb-2 d-flex align-items-center gap-2">
                            <i class="fa-solid fa-chair text-danger"></i> Chi tiết vị trí ghế ngồi
                        </h6>
                        <p class="text-secondary small mb-3">Thông tin chi tiết chỗ ngồi và giá vé tương ứng của từng ghế:</p>

                        <div id="modal-seat-list" style="max-height: 320px; overflow-y: auto; padding-right: 4px;">
                            <!-- Từng ghế sẽ được render bằng JavaScript -->
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer border-top border-secondary border-opacity-25 px-4 py-3 d-flex justify-content-between">
                <span class="text-secondary small">
                    <i class="fa-solid fa-circle-info text-warning me-1"></i> Vui lòng đến rạp trước giờ chiếu 15 phút để in vé và nhận bắp nước.
                </span>
                <button type="button" class="btn btn-outline-secondary px-4 text-white" data-bs-dismiss="modal">Đóng</button>
            </div>
        </div>
    </div>
</div>

<jsp:include page="../../common/footer.jsp" />

<script>
    let currentModalBookingCode = '';
    const moneyFormatter = new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 });
    const formatMoney = num => moneyFormatter.format(Math.round(num || 0)) + ' đ';

    function copyCode(code) {
        if (!code) return;
        navigator.clipboard.writeText(code).then(() => {
            alert('Đã sao chép mã đơn hàng: ' + code);
        }).catch(() => {
            prompt('Mã đơn hàng:', code);
        });
    }

    function copyModalCode() {
        if (currentModalBookingCode) {
            copyCode(currentModalBookingCode);
        }
    }

    function filterBookings(type, btn) {
        document.querySelectorAll('.filter-pill').forEach(b => b.classList.remove('is-active'));
        if (btn) btn.classList.add('is-active');

        const cards = document.querySelectorAll('.booking-card');
        cards.forEach(card => {
            const isUpcoming = card.dataset.upcoming === 'true';
            if (type === 'all') {
                card.style.display = 'block';
            } else if (type === 'upcoming') {
                card.style.display = isUpcoming ? 'block' : 'none';
            } else if (type === 'past') {
                card.style.display = !isUpcoming ? 'block' : 'none';
            }
        });
    }

    async function openTicketModal(bookingCode) {
        if (!bookingCode) return;
        currentModalBookingCode = bookingCode;
        const modalElem = document.getElementById('ticketDetailModal');
        const modal = new bootstrap.Modal(modalElem);

        try {
            const resp = await fetch('${pageContext.request.contextPath}/customer/bookings?code=' + encodeURIComponent(bookingCode), {
                headers: {
                    'Accept': 'application/json',
                    'X-Requested-With': 'XMLHttpRequest'
                }
            });
            const resData = await resp.json();
            if (!resData || !resData.data) {
                alert('Không thể tải chi tiết vé.');
                return;
            }
            const item = resData.data;

            // Fill modal header & meta
            document.getElementById('modal-code-display').textContent = item.bookingCode;
            document.getElementById('modal-movie-title').textContent = item.movieTitle || 'Vé Xem Phim';
            if (item.posterUrl) {
                document.getElementById('modal-movie-poster').src = item.posterUrl;
            }
            document.getElementById('modal-age-rating').textContent = item.ageRating || 'P';
            document.getElementById('modal-format-duration').textContent = (item.experienceFormat || '2D') + ' • ' + (item.durationMinutes ? item.durationMinutes + ' phút' : '');
            document.getElementById('modal-cinema-name').textContent = item.cinemaName || 'CineMax Chain';
            document.getElementById('modal-room-name').textContent = item.roomName || 'Phòng chiếu';
            let stText = item.formattedShowtime;
            if (!stText && item.showtimeStartTime) {
                try {
                    const d = new Date(item.showtimeStartTime);
                    const hh = String(d.getHours()).padStart(2, '0');
                    const mm = String(d.getMinutes()).padStart(2, '0');
                    const dd = String(d.getDate()).padStart(2, '0');
                    const MM = String(d.getMonth() + 1).padStart(2, '0');
                    const yyyy = d.getFullYear();
                    stText = hh + ':' + mm + ' • ' + dd + '/' + MM + '/' + yyyy;
                } catch (e) {}
            }
            document.getElementById('modal-showtime').innerHTML = stText || '--';
            document.getElementById('modal-seat-codes').textContent = (item.tickets || []).map(t => t.seatCode).join(', ') || 'Chưa có ghế';
            document.getElementById('modal-final-amount').textContent = formatMoney(item.finalAmount);

            // Fill F&B
            const fnbSection = document.getElementById('modal-fnb-section');
            const fnbList = document.getElementById('modal-fnb-list');
            if (item.fnbItems && item.fnbItems.length > 0) {
                fnbSection.style.display = 'block';
                fnbList.replaceChildren();
                item.fnbItems.forEach(f => {
                    const row = document.createElement('div');
                    row.className = 'd-flex justify-content-between py-1 small';
                    row.innerHTML = '<span class="text-secondary">' + f.itemName + ' × ' + f.quantity + '</span><strong class="text-white">' + formatMoney(f.subtotal) + '</strong>';
                    fnbList.appendChild(row);
                });
            } else {
                fnbSection.style.display = 'none';
            }

            // Fill Seat list
            const seatContainer = document.getElementById('modal-seat-list');
            seatContainer.replaceChildren();

            if (item.tickets && item.tickets.length > 0) {
                item.tickets.forEach((t, idx) => {
                    const card = document.createElement('div');
                    card.className = 'ticket-seat-card';
                    const paymentStatus = item.status === 'PENDING'
                        ? '<small class="d-block text-warning">Chờ thanh toán</small>'
                        : '<small class="d-block text-success"><i class="fa-solid fa-check"></i> Đã thanh toán</small>';
                    card.innerHTML = '<div><span class="badge bg-danger me-2">Vé #' + (idx + 1) +
                        '</span><span class="fs-5 fw-bold text-warning">Ghế ' + t.seatCode +
                        '</span></div><div class="text-end"><span class="text-white fw-bold">' +
                        formatMoney(t.ticketPrice) + '</span>' + paymentStatus + '</div>';
                    seatContainer.appendChild(card);
                });
            } else {
                seatContainer.innerHTML = '<div class="text-center py-4 text-secondary">Chưa có thông tin ghế.</div>';
            }

            modal.show();
        } catch (err) {
            console.error('Error fetching booking detail:', err);
            alert('Lỗi kết nối máy chủ khi tải vé.');
        }
    }

    async function retryVnpayPayment(bookingCode) {
        try {
            const response = await fetch('${pageContext.request.contextPath}/booking/retry-payment', {
                method: 'POST',
                headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: new URLSearchParams({ bookingCode, ajax: 'true' })
            });
            const data = await response.json();
            if (data.paymentUrl) {
                window.location.href = data.paymentUrl;
                return;
            }
            alert(data.message || 'Không thể tạo lại liên kết thanh toán. Vui lòng tải lại trang.');
            window.location.reload();
        } catch (error) {
            alert('Không thể kết nối để tiếp tục thanh toán. Vui lòng thử lại.');
        }
    }

    async function cancelPendingPayment(bookingCode) {
        if (!confirm('Hủy đơn chưa thanh toán và giải phóng ghế đã giữ?')) return;
        try {
            const response = await fetch('${pageContext.request.contextPath}/booking/cancel-payment', {
                method: 'POST',
                headers: { 'Accept': 'application/json', 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: new URLSearchParams({ bookingCode, ajax: 'true' })
            });
            const data = await response.json();
            if (data.success) {
                window.location.reload();
                return;
            }
            alert(data.message || 'Không thể hủy đơn. Vui lòng tải lại trang.');
            window.location.reload();
        } catch (error) {
            alert('Không thể kết nối để hủy đơn. Vui lòng tải lại trang.');
        }
    }

    // Tự động mở modal nếu URL có tham số ?code=...
    document.addEventListener('DOMContentLoaded', () => {
        const urlParams = new URLSearchParams(window.location.search);
        const code = urlParams.get('code');
        if (code) {
            openTicketModal(code);
        }
    });
</script>
