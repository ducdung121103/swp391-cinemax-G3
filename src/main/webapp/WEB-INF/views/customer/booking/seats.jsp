<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<c:set var="pageTitle" value="Đặt Vé: ${showtime.movie.title} — CineMax" />
<c:url var="fnbFallbackUrl" value="/assets/images/fnb/default.svg" />
<jsp:include page="../../common/header.jsp" />

<style>
    /* ==========================================================================
       ONLINE BOOKING WIZARD - CINEMAX DARK LUXURY THEME
       Tích hợp nguyên mẫu từ quy trình POS Counter bắt đầu từ phần Chọn ghế
       ========================================================================== */

    .booking-wizard-wrapper {
        color: #f1f5f9;
        padding-top: 1.5rem;
        padding-bottom: 4rem;
    }

    /* Top Heading & Chips */
    .booking-top-bar {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 16px;
        margin-bottom: 24px;
        padding-bottom: 18px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.08);
    }
    .booking-heading h1 {
        font-size: 24px;
        font-weight: 800;
        color: #fff;
        margin: 0 0 4px;
        letter-spacing: -0.02em;
    }
    .booking-heading p {
        color: #94a3b8;
        font-size: 14px;
        margin: 0;
    }
    .booking-eyebrow {
        display: inline-block;
        font-size: 11px;
        font-weight: 800;
        text-transform: uppercase;
        letter-spacing: 0.1em;
        color: #e50914;
        margin-bottom: 4px;
    }
    .booking-top-chips {
        display: flex;
        align-items: center;
        gap: 12px;
        flex-wrap: wrap;
    }

    /* Cinema Chip: Nền xám nhạt, chữ đen đậm tương phản cao theo yêu cầu */
    .booking-cinema-chip {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        background: #f1f5f9;
        color: #0f172a;
        padding: 8px 16px;
        border-radius: 10px;
        border: 1px solid #cbd5e1;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
    }
    .booking-cinema-chip .cinema-dot {
        width: 10px;
        height: 10px;
        background: #10b981;
        border-radius: 50%;
        box-shadow: 0 0 8px #10b981;
        flex-shrink: 0;
    }
    .booking-cinema-chip small {
        display: block;
        font-size: 10px;
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: #475569;
        font-weight: 700;
        line-height: 1.1;
    }
    .booking-cinema-chip strong {
        font-size: 13px;
        font-weight: 800;
        color: #0f172a;
        line-height: 1.2;
    }

    /* Timer Chip */
    .booking-timer-chip {
        display: inline-flex;
        align-items: center;
        gap: 8px;
        background: #1e1318;
        color: #f87171;
        border: 1px solid rgba(239, 68, 68, 0.4);
        padding: 8px 14px;
        border-radius: 10px;
    }
    .booking-timer-chip strong {
        font-size: 15px;
        font-weight: 800;
        font-family: monospace;
    }

    /* 3-Step Stepper */
    .booking-stepper {
        display: flex;
        align-items: center;
        justify-content: space-between;
        background: #0f1523;
        border: 1px solid rgba(255, 255, 255, 0.08);
        border-radius: 14px;
        padding: 14px 28px;
        margin-bottom: 28px;
        box-shadow: 0 4px 20px rgba(0,0,0,0.3);
    }
    .stepper-item {
        display: flex;
        align-items: center;
        gap: 12px;
        cursor: pointer;
        user-select: none;
        opacity: 0.55;
        transition: all 0.2s ease;
    }
    .stepper-item.is-active {
        opacity: 1;
    }
    .stepper-item.is-completed {
        opacity: 0.85;
    }
    .step-circle {
        width: 36px;
        height: 36px;
        border-radius: 50%;
        background: #1c2438;
        border: 2px solid #334155;
        color: #cbd5e1;
        font-weight: 800;
        font-size: 14px;
        display: grid;
        place-items: center;
        transition: all 0.2s;
    }
    .stepper-item.is-active .step-circle {
        background: #e50914;
        border-color: #e50914;
        color: #fff;
        box-shadow: 0 0 16px rgba(229, 9, 20, 0.6);
    }
    .stepper-item.is-completed .step-circle {
        background: #10b981;
        border-color: #10b981;
        color: #fff;
    }
    .step-info {
        display: flex;
        flex-direction: column;
    }
    .step-title {
        font-size: 14px;
        font-weight: 700;
        color: #fff;
    }
    .step-sub {
        font-size: 11px;
        color: #94a3b8;
    }
    .stepper-line {
        flex: 1;
        height: 2px;
        background: #1e293b;
        margin: 0 20px;
    }

    /* Panels */
    .booking-step-panel {
        display: none;
    }
    .booking-step-panel.is-active {
        display: block;
        animation: fadeIn 0.25s ease-in-out;
    }
    @keyframes fadeIn {
        from { opacity: 0; transform: translateY(6px); }
        to { opacity: 1; transform: translateY(0); }
    }

    /* Card Panels */
    .wizard-card {
        background: #0f1523;
        border: 1px solid rgba(255, 255, 255, 0.08);
        border-radius: 14px;
        padding: 24px;
        box-shadow: 0 8px 24px rgba(0,0,0,0.3);
        margin-bottom: 24px;
    }
    .wizard-section-heading {
        display: flex;
        align-items: center;
        gap: 14px;
        margin-bottom: 20px;
        padding-bottom: 14px;
        border-bottom: 1px solid rgba(255, 255, 255, 0.06);
    }
    .wizard-section-heading .section-number {
        width: 32px;
        height: 32px;
        border-radius: 8px;
        background: #e50914;
        color: #fff;
        font-weight: 800;
        font-size: 13px;
        display: grid;
        place-items: center;
    }
    .wizard-section-heading h2 {
        font-size: 17px;
        font-weight: 700;
        color: #fff;
        margin: 0;
    }
    .wizard-section-heading p {
        font-size: 12px;
        color: #94a3b8;
        margin: 2px 0 0;
    }

    /* Selected Showtime Info Banner */
    .showtime-info-banner {
        background: #141b2b;
        border: 1px solid #242f48;
        border-radius: 10px;
        padding: 12px 18px;
        margin-bottom: 24px;
        display: flex;
        align-items: center;
        flex-wrap: wrap;
        gap: 12px;
        font-size: 13px;
        color: #cbd5e1;
    }
    .showtime-info-banner strong {
        color: #fff;
    }

    /* Seat Legend */
    .seat-legend {
        display: flex;
        justify-content: center;
        gap: 20px;
        flex-wrap: wrap;
        margin-bottom: 24px;
        font-size: 12px;
        color: #94a3b8;
    }
    .legend-seat {
        display: inline-flex;
        align-items: center;
        gap: 6px;
    }
    .legend-box {
        display: inline-block;
        width: 18px;
        height: 18px;
        border-radius: 5px;
        border: 1px solid #334155;
    }
    .legend-box.available { background: #1c2333; }
    .legend-box.selected { background: #e50914; border-color: #e50914; }
    .legend-box.vip { background: #2a1f10; border-color: #f59e0b; }
    .legend-box.couple { background: #2e1222; border-color: #ec4899; }
    .legend-box.sold { background: #262c3a; opacity: 0.6; position: relative; }
    .legend-box.sold::after {
        content: '✕';
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 10px;
        color: #94a3b8;
        height: 100%;
    }
    .legend-box.held {
        background: repeating-linear-gradient(45deg, #334155, #334155 3px, #1e293b 3px, #1e293b 6px);
        border: 1px dashed #64748b;
    }

    /* Screen Label */
    .screen-label {
        text-align: center;
        margin: 10px 0 28px;
        position: relative;
    }
    .screen-label::before {
        content: '';
        display: block;
        width: 80%;
        max-width: 520px;
        height: 6px;
        margin: 0 auto 10px;
        background: linear-gradient(90deg, transparent, #e50914, transparent);
        border-radius: 50%;
        box-shadow: 0 2px 14px rgba(229, 9, 20, 0.7);
    }
    .screen-label span {
        display: inline-block;
        padding: 4px 30px;
        border-radius: 0 0 30px 30px;
        background: #141c2c;
        border: 1px solid #243048;
        border-top: none;
        color: #94a3b8;
        font-size: 11px;
        font-weight: 700;
        letter-spacing: 0.2em;
    }

    /* Seat Grid */
    .seat-grid-scroll {
        max-width: 100%;
        overflow-x: auto;
        padding: 10px 0 20px;
        display: flex;
        justify-content: center;
    }
    .seat-grid {
        display: grid;
        grid-auto-rows: 36px;
        gap: 8px;
        width: max-content;
        margin: 0 auto;
    }
    .seat-option {
        position: relative;
        display: block;
        cursor: pointer;
        user-select: none;
    }
    .seat-option input {
        position: absolute;
        width: 1px;
        height: 1px;
        opacity: 0;
        pointer-events: none;
    }
    .seat-option span {
        display: grid;
        width: 100%;
        height: 36px;
        place-items: center;
        color: #cbd5e1;
        background: #1c2333;
        border: 1px solid #334155;
        border-radius: 7px;
        font-size: 11px;
        font-weight: 700;
        transition: all 0.15s ease;
    }
    .seat-option:hover:not(.is-sold):not(.is-held):not(.is-unavailable) span {
        border-color: #f87171;
        color: #fff;
        transform: translateY(-2px);
        box-shadow: 0 4px 10px rgba(229, 9, 20, 0.3);
    }
    /* VIP Seat */
    .seat-option.seat-vip span {
        background: #251b10;
        border-color: #d97706;
        color: #fde68a;
    }
    /* Couple Seat (Sweetbox) */
    .seat-option.seat-couple span {
        background: #2b1320;
        border-color: #ec4899;
        color: #fbcfe8;
    }
    /* Selected Seat */
    .seat-option input:checked + span {
        background: #e50914 !important;
        border-color: #e50914 !important;
        color: #fff !important;
        box-shadow: 0 0 14px rgba(229, 9, 20, 0.8) !important;
        transform: scale(1.05);
    }
    /* Sold / Held / Inactive */
    .seat-option.is-sold {
        cursor: not-allowed;
    }
    .seat-option.is-sold span {
        background: #1e2433;
        border-color: #2b3244;
        color: #555e72;
        text-decoration: line-through;
    }
    .seat-option.is-held {
        cursor: not-allowed;
    }
    .seat-option.is-held span {
        background: repeating-linear-gradient(45deg, #1e293b, #1e293b 4px, #0f172a 4px, #0f172a 8px) !important;
        border: 1px dashed #64748b !important;
        color: #94a3b8 !important;
        opacity: 0.8;
    }
    .seat-option.is-unavailable {
        cursor: not-allowed;
        opacity: 0.35;
    }

    /* Bottom Step Nav */
    .wizard-step-nav {
        display: flex;
        justify-content: space-between;
        align-items: center;
        flex-wrap: wrap;
        gap: 16px;
        margin-top: 24px;
        padding-top: 18px;
        border-top: 1px solid rgba(255, 255, 255, 0.08);
    }
    .nav-summary-pill {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        background: #141b2b;
        border: 1px solid #232f48;
        padding: 8px 18px;
        border-radius: 30px;
        font-size: 13px;
        color: #cbd5e1;
    }
    .nav-summary-pill strong {
        color: #f5c518;
    }

    /* F&B Product Grid */
    .pos-product-grid {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
        gap: 16px;
    }
    .pos-product-card {
        display: flex;
        flex-direction: column;
        padding: 16px;
        background: #141b2b;
        border: 1px solid #232f48;
        border-radius: 12px;
        transition: all 0.2s ease;
        position: relative;
    }
    .pos-product-card[data-fnb-card] {
        cursor: pointer;
    }
    .pos-product-card[data-fnb-card]:hover {
        border-color: #e50914;
        transform: translateY(-3px);
        box-shadow: 0 8px 24px rgba(0,0,0,0.5);
    }
    .pos-product-card.is-out {
        opacity: 0.45;
        cursor: not-allowed;
    }
    .product-card-top {
        position: relative;
        width: 100%;
        height: 120px;
        border-radius: 8px;
        overflow: hidden;
        background: #090d16;
        margin-bottom: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    .product-image {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }
    .combo-tag {
        position: absolute;
        top: 6px;
        right: 6px;
        background: #f59e0b;
        color: #000;
        font-size: 9px;
        font-weight: 800;
        padding: 2px 7px;
        border-radius: 4px;
        letter-spacing: 0.05em;
    }
    .product-category {
        font-size: 11px;
        color: #94a3b8;
        margin-bottom: 4px;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }
    .pos-product-card h3 {
        font-size: 14px;
        font-weight: 700;
        color: #fff;
        margin: 0 0 8px;
        line-height: 1.35;
        min-height: 38px;
    }
    .product-price {
        font-size: 15px;
        font-weight: 800;
        color: #f5a623;
        margin-bottom: 12px;
    }
    .product-quantity-wrap {
        margin-top: auto;
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 8px;
    }
    .qty-btn {
        width: 32px;
        height: 32px;
        border-radius: 6px;
        background: #1c2438;
        border: 1px solid #334155;
        color: #fff;
        font-size: 16px;
        font-weight: bold;
        display: grid;
        place-items: center;
        cursor: pointer;
        transition: all 0.15s;
    }
    .qty-btn:hover {
        background: #e50914;
        border-color: #e50914;
    }
    .product-quantity-wrap input {
        width: 50px;
        text-align: center;
        background: #0d121d;
        border: 1px solid #334155;
        border-radius: 6px;
        color: #fff;
        font-weight: 700;
        font-size: 14px;
        padding: 4px 0;
    }
    .stock-badge {
        font-size: 11px;
        color: #64748b;
    }

    /* Step 3: Checkout 2-Column Layout */
    .booking-checkout-layout {
        display: grid;
        grid-template-columns: minmax(0, 1fr) 360px;
        gap: 24px;
        align-items: start;
    }
    .checkout-section-card {
        background: #0f1523;
        border: 1px solid rgba(255, 255, 255, 0.08);
        border-radius: 14px;
        padding: 20px 22px;
        margin-bottom: 18px;
        box-shadow: 0 6px 18px rgba(0,0,0,0.25);
    }
    .checkout-section-card h3 {
        font-size: 15px;
        font-weight: 700;
        color: #fff;
        margin: 0 0 14px;
        display: flex;
        align-items: center;
        gap: 8px;
    }
    .checkout-section-card .summary-line {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 13px;
        color: #cbd5e1;
        padding: 4px 0;
    }
    .checkout-section-card .summary-line strong {
        color: #fff;
    }

    /* Ticket Class Choice Cards */
    .ticket-class-options {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }
    .choice-card {
        display: flex;
        align-items: center;
        gap: 12px;
        background: #141b2b;
        border: 2px solid #242f48;
        border-radius: 10px;
        padding: 12px 16px;
        cursor: pointer;
        transition: all 0.15s ease;
    }
    .choice-card:hover {
        border-color: #3b82f6;
    }
    .choice-card.is-selected {
        border-color: #e50914;
        background: rgba(229, 9, 20, 0.08);
    }
    .choice-card input {
        display: none;
    }
    .choice-radio {
        width: 18px;
        height: 18px;
        border-radius: 50%;
        border: 2px solid #64748b;
        display: grid;
        place-items: center;
        flex-shrink: 0;
    }
    .choice-card.is-selected .choice-radio {
        border-color: #e50914;
    }
    .choice-card.is-selected .choice-radio::after {
        content: '';
        width: 8px;
        height: 8px;
        background: #e50914;
        border-radius: 50%;
    }
    .choice-copy {
        flex: 1;
        display: flex;
        flex-direction: column;
    }
    .choice-copy strong {
        font-size: 14px;
        color: #fff;
    }
    .choice-copy small {
        font-size: 11px;
        color: #94a3b8;
    }
    .choice-tag {
        font-size: 11px;
        font-weight: 800;
        background: #1e293b;
        color: #cbd5e1;
        padding: 3px 8px;
        border-radius: 6px;
    }
    .choice-card.is-selected .choice-tag {
        background: #e50914;
        color: #fff;
    }

    /* Sticky Right Summary */
    .order-summary-card {
        background: #0f1523;
        border: 1px solid #e50914;
        border-radius: 16px;
        padding: 24px;
        box-shadow: 0 10px 30px rgba(0,0,0,0.5);
        position: sticky;
        top: 90px;
    }
    .order-summary-card h2 {
        font-size: 18px;
        font-weight: 800;
        color: #fff;
        margin: 0 0 4px;
    }
    .order-summary-card .summary-line {
        display: flex;
        justify-content: space-between;
        align-items: center;
        font-size: 13px;
        color: #cbd5e1;
        padding: 6px 0;
    }
    .order-summary-card .summary-line strong {
        color: #fff;
    }
    .summary-grand-total {
        margin-top: 10px;
        padding-top: 10px;
        border-top: 1px dashed rgba(255, 255, 255, 0.15);
    }
    .summary-grand-total span {
        font-size: 15px;
        font-weight: 800;
        color: #fff;
    }
    .summary-grand-total strong {
        font-size: 22px;
        font-weight: 900;
        color: #e50914 !important;
    }

    @media (max-width: 991px) {
        .booking-checkout-layout {
            grid-template-columns: 1fr;
        }
        .order-summary-card {
            position: static;
        }
    }
</style>

<div class="container booking-wizard-wrapper">

    <!-- TOP BAR: THÔNG TIN PHIM, TÊN RẠP VÀ ĐỒNG HỒ -->
    <header class="booking-top-bar">
        <div class="booking-heading">
            <span class="booking-eyebrow">ĐẶT VÉ TRỰC TUYẾN · ONLINE BOOKING</span>
            <h1><c:out value="${showtime.movie.title}" /></h1>
            <p>
                <c:if test="${not empty showtime.movie.durationMinutes and showtime.movie.durationMinutes gt 0}"><c:out value="${showtime.movie.durationMinutes}" /> phút &bull; </c:if>
                Định dạng <strong><c:out value="${showtime.experienceFormat}" /></strong> &bull; Phòng: <strong><c:out value="${showtime.screeningRoom.name}" /></strong>
            </p>
        </div>

        <div class="booking-top-chips">
            <!-- TÊN RẠP CHIẾU NỔI BẬT (NỀN XÁM NHẠT, CHỮ ĐEN ĐẬM TƯƠNG PHẢN CAO) -->
            <div class="booking-cinema-chip">
                <span class="cinema-dot"></span>
                <div>
                    <small>Rạp chiếu</small>
                    <strong>
                        <c:choose>
                            <c:when test="${not empty currentCinema}"><c:out value="${currentCinema.name}" /></c:when>
                            <c:when test="${not empty showtime.screeningRoom.cinema}"><c:out value="${showtime.screeningRoom.cinema.name}" /></c:when>
                            <c:otherwise>CineMax Chain</c:otherwise>
                        </c:choose>
                    </strong>
                </div>
            </div>

            <!-- ĐỒNG HỒ ĐẾM NGƯỢC GIỮ VÉ -->
            <div class="booking-timer-chip">
                <i class="fa-regular fa-clock fs-5"></i>
                <div>
                    <small style="display:block;font-size:9px;text-transform:uppercase;">Giữ ghế</small>
                    <strong id="timer-display">05:00</strong>
                </div>
            </div>
        </div>
    </header>

    <!-- THANH TIẾN TRÌNH STEPPER 3 BƯỚC (BÊ NGUYÊN TỪ POS COUNTER BẮT ĐẦU TỪ CHỌN GHẾ) -->
    <nav class="booking-stepper" aria-label="Tiến trình đặt vé">
        <div class="stepper-item is-active" data-step="1" onclick="onStepClick(1)">
            <div class="step-circle">1</div>
            <div class="step-info">
                <span class="step-title">1. Chọn ghế</span>
                <small class="step-sub">Chọn vị trí ngồi</small>
            </div>
        </div>
        <div class="stepper-line"></div>
        <div class="stepper-item" data-step="2" onclick="onStepClick(2)">
            <div class="step-circle">2</div>
            <div class="step-info">
                <span class="step-title">2. Chọn bắp nước</span>
                <small class="step-sub">F&B & combo ưu đãi</small>
            </div>
        </div>
        <div class="stepper-line"></div>
        <div class="stepper-item" data-step="3" onclick="onStepClick(3)">
            <div class="step-circle">3</div>
            <div class="step-info">
                <span class="step-title">3. Thanh toán</span>
                <small class="step-sub">Loại vé & Cổng VNPAY</small>
            </div>
        </div>
    </nav>

    <!-- THÔNG BÁO LỖI NẾU CÓ -->
    <div id="clientBookingError" class="alert alert-danger align-items-center gap-2" style="display: none;" role="alert">
        <i class="fa-solid fa-triangle-exclamation"></i>
        <span id="clientBookingErrorText"></span>
    </div>
    <c:if test="${not empty seatError or not empty param.error}">
        <div class="alert alert-danger d-flex align-items-center gap-2">
            <i class="fa-solid fa-circle-exclamation"></i>
            <span><c:out value="${not empty seatError ? seatError : param.error}" /></span>
        </div>
    </c:if>

    <!-- FORM ĐẶT VÉ WIZARD CHÍNH -->
    <form id="booking-wizard-form" method="post" action="${pageContext.request.contextPath}/booking/checkout">
        <input type="hidden" name="showtimeId" value="${showtime.id}">
        <input type="hidden" name="paymentMethod" value="VNPAY">
        <input type="hidden" name="quotedTotal" id="quotedTotal" value="">

        <!-- =================================================================== -->
        <!-- BƯỚC 1: MÀN HÌNH CHỌN GHẾ (BƯỚC 2 CỦA POS)                         -->
        <!-- =================================================================== -->
        <section class="booking-step-panel is-active" id="step-1-panel">
            <div class="wizard-card">
                <div class="wizard-section-heading">
                    <div class="section-number">01</div>
                    <div>
                        <h2>Bước 1: Chọn ghế ngồi</h2>
                        <p>Chạm vào ghế còn trống để chọn chỗ ngồi ưng ý.</p>
                    </div>
                </div>

                <div class="showtime-info-banner">
                    <span>Rạp: <strong><c:out value="${not empty currentCinema ? currentCinema.name : showtime.screeningRoom.cinema.name}" /></strong></span>
                    <span>&bull;</span>
                    <span>Phim: <strong><c:out value="${showtime.movie.title}" /></strong></span>
                    <span>&bull;</span>
                    <span>Suất chiếu: <strong><c:out value="${showtimeText}" /></strong></span>
                    <span>&bull;</span>
                    <span>Phòng: <strong><c:out value="${showtime.screeningRoom.name}" /></strong> (<c:out value="${showtime.experienceFormat}" />)</span>
                </div>

                <!-- Chú thích loại ghế -->
                <div class="seat-legend">
                    <span class="legend-seat"><i class="legend-box available"></i>Còn trống</span>
                    <span class="legend-seat"><i class="legend-box selected"></i>Đang chọn</span>
                    <span class="legend-seat"><i class="legend-box vip"></i>Ghế VIP</span>
                    <span class="legend-seat"><i class="legend-box couple"></i>Ghế đôi Sweetbox</span>
                    <span class="legend-seat"><i class="legend-box sold"></i>Đã bán</span>
                    <span class="legend-seat"><i class="legend-box held"></i>Đang giữ</span>
                </div>

                <!-- Màn hình cong -->
                <div class="screen-label">
                    <span>MÀN HÌNH</span>
                </div>

                <!-- Sơ đồ ghế lưới -->
                <c:choose>
                    <c:when test="${not empty seatMap}">
                        <div class="seat-grid-scroll">
                            <div class="seat-grid" style="grid-template-columns: repeat(${seatColumnCount}, 38px);">
                                <c:forEach items="${seatMap}" var="seat">
                                    <label class="seat-option ${seat.status eq 'AVAILABLE' ? 'is-available' : seat.status eq 'SOLD' ? 'is-sold' : seat.status eq 'HELD' ? 'is-held' : 'is-unavailable'} ${seat.seatTypeId eq 2 ? 'seat-vip' : seat.seatTypeId eq 3 ? 'seat-couple' : 'seat-std'}"
                                           style="grid-row: ${seat.gridRowIndex}; grid-column: ${seat.gridColIndex};"
                                           title="Ghế ${seat.seatCode} (${seat.seatTypeId eq 2 ? 'VIP' : seat.seatTypeId eq 3 ? 'Sweetbox' : 'Thường'}) - <fmt:formatNumber value='${seatPrices[seat.id]}' pattern='#,##0' /> đ">
                                        <c:choose>
                                            <c:when test="${seat.status eq 'AVAILABLE'}">
                                                <input type="checkbox" name="seatIds" value="${seat.id}"
                                                       data-seat-code="${seat.seatCode}"
                                                       data-seat-row="${seat.seatRow}"
                                                       data-seat-number="${seat.seatNumber}"
                                                       data-seat-col="${seat.gridColIndex}"
                                                       data-seat-type-id="${seat.seatTypeId}"
                                                       data-price="${seatPrices[seat.id]}">
                                            </c:when>
                                            <c:otherwise>
                                                <input type="checkbox" disabled>
                                            </c:otherwise>
                                        </c:choose>
                                        <span><c:out value="${seat.seatCode}" /></span>
                                    </label>
                                </c:forEach>
                            </div>
                        </div>
                        <c:if test="${availableSeatCount eq 0}">
                            <div class="alert alert-warning text-center mt-3">Suất chiếu này hiện đã hết ghế trống.</div>
                        </c:if>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-5 text-secondary">Phòng chiếu này chưa có cấu hình sơ đồ ghế.</div>
                    </c:otherwise>
                </c:choose>

                <!-- DANH SÁCH GHẾ ĐÃ CHỌN, GIÁ TỪNG GHẾ & TỔNG TẠM TÍNH -->
                <div class="selected-seats-breakdown-card mt-4 p-3 rounded-3" id="step1-seats-breakdown" style="background: #141b2b; border: 1px solid #232f48;">
                    <div class="d-flex justify-content-between align-items-center mb-2 flex-wrap gap-2">
                        <div class="d-flex align-items-center gap-2">
                            <i class="fa-solid fa-couch text-danger fs-5"></i>
                            <span class="fw-bold text-white fs-6">Ghế đã chọn:</span>
                            <span class="badge bg-danger rounded-pill px-2 py-1" id="step1-breakdown-count">0 ghế</span>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                            <span class="text-secondary small">Tổng tạm tính:</span>
                            <strong class="text-warning fs-5 fw-bold" id="step1-breakdown-total">0 đ</strong>
                        </div>
                    </div>
                    <div class="d-flex flex-wrap gap-2 pt-2 border-top border-secondary border-opacity-25" id="step1-selected-seat-items">
                        <span class="text-secondary small fst-italic">Chưa chọn ghế nào. Vui lòng nhấp vào ghế trên sơ đồ để chọn chỗ ngồi.</span>
                    </div>
                </div>

                <!-- Footer điều hướng bước 1 -->
                <div class="wizard-step-nav">
                    <c:url var="changeShowtimeUrl" value="/showtimes">
                        <c:if test="${not empty showtime.movieId}">
                            <c:param name="movieId" value="${showtime.movieId}" />
                        </c:if>
                    </c:url>
                    <a href="${changeShowtimeUrl}" class="btn btn-outline-secondary">
                        <i class="fa-solid fa-arrow-left me-1"></i> Đổi suất chiếu
                    </a>
                    <span class="nav-summary-pill">
                        Đang chọn: <strong id="step1-seat-count">0</strong> ghế (<span id="step1-seat-codes">Chưa chọn ghế</span>) &bull; Tạm tính: <strong id="step1-nav-total" class="text-warning">0 đ</strong>
                    </span>
                    <button type="button" id="btn-proceed-step2" class="btn btn-cinema-primary" onclick="proceedToStep2()">
                        Tiếp tục: Chọn bắp nước <i class="fa-solid fa-arrow-right ms-1"></i>
                    </button>
                </div>
            </div>
        </section>

        <!-- =================================================================== -->
        <!-- BƯỚC 2: MÀN HÌNH CHỌN BẮP NƯỚC (BƯỚC 3 CỦA POS)                     -->
        <!-- =================================================================== -->
        <section class="booking-step-panel" id="step-2-panel">
            <div class="wizard-card">
                <div class="wizard-section-heading">
                    <div class="section-number">02</div>
                    <div>
                        <h2>Bước 2: Chọn bắp nước & combo ưu đãi</h2>
                        <p>Chạm vào thẻ món để thêm một phần, hoặc điều chỉnh số lượng mong muốn.</p>
                    </div>
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
                                    <div class="product-price"><fmt:formatNumber value="${item.price}" pattern="#,##0" groupingUsed="true" /> đ</div>

                                    <div class="product-quantity-wrap">
                                        <c:choose>
                                            <c:when test="${item.stockQuantity le 0}">
                                                <span class="stock-badge text-danger">Hết hàng</span>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="button" class="qty-btn" onclick="decreaseFnb('${item.id}', event)">-</button>
                                                <input id="fnb-${item.id}" type="number" name="fnb_${item.id}" min="0" max="${item.stockQuantity le 99 ? item.stockQuantity : 99}" value="0" data-fnb-quantity data-price="${item.price}" data-name="<c:out value='${item.name}' />" onchange="updateSummary()">
                                                <button type="button" class="qty-btn" onclick="increaseFnb('${item.id}', event)">+</button>
                                                <span class="stock-badge">Còn <c:out value="${item.stockQuantity}" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="text-center py-5 text-secondary">Chi nhánh hiện tại chưa có sản phẩm bắp nước còn hàng. Bạn có thể tiếp tục thanh toán vé.</div>
                    </c:otherwise>
                </c:choose>

                <!-- Footer điều hướng bước 2 -->
                <div class="wizard-step-nav">
                    <button type="button" class="btn btn-outline-secondary" onclick="goToStep(1)">
                        <i class="fa-solid fa-arrow-left me-1"></i> Quay lại chọn ghế
                    </button>
                    <span class="nav-summary-pill">
                        Bắp nước: <strong id="step2-fnb-count">0</strong> món &bull; Tạm tính: <strong id="step2-fnb-subtotal">0đ</strong>
                    </span>
                    <button type="button" class="btn btn-cinema-primary" onclick="goToStep(3)">
                        Tiếp tục: Thanh toán <i class="fa-solid fa-arrow-right ms-1"></i>
                    </button>
                </div>
            </div>
        </section>

        <!-- =================================================================== -->
        <!-- BƯỚC 3: MÀN HÌNH THANH TOÁN (BƯỚC 4 CỦA POS)                         -->
        <!-- =================================================================== -->
        <section class="booking-step-panel" id="step-3-panel">
            <div class="mb-3">
                <button type="button" class="btn btn-outline-secondary" onclick="goToStep(2)">
                    <i class="fa-solid fa-arrow-left me-1"></i> Quay lại chọn bắp nước
                </button>
            </div>

            <div class="booking-checkout-layout">
                <!-- CỘT TRÁI: CHI TIẾT VÉ, CHỌN LOẠI VÉ, FNB VÀ PHƯƠNG THỨC THANH TOÁN -->
                <div class="booking-checkout-left">

                    <!-- CARD THÔNG TIN KHÁCH HÀNG ĐẶT VÉ -->
                    <div class="checkout-section-card">
                        <h3>
                            <i class="fa-solid fa-user-check text-warning"></i>
                            Thông tin khách hàng đặt vé
                        </h3>
                        <div class="summary-line">
                            <span>Khách hàng:</span>
                            <strong><c:out value="${not empty currentUser.fullName ? currentUser.fullName : 'Khách hàng CineMax'}" /></strong>
                        </div>
                        <div class="summary-line">
                            <span>Email nhận vé điện tử:</span>
                            <strong><c:out value="${currentUser.email}" /></strong>
                        </div>
                        <div class="summary-line">
                            <span>Số điện thoại:</span>
                            <strong><c:out value="${not empty currentUser.phone ? currentUser.phone : 'Chưa cập nhật'}" /></strong>
                        </div>
                    </div>

                    <!-- CARD THÔNG TIN VÉ & SUẤT CHIẾU -->
                    <div class="checkout-section-card">
                        <h3>
                            <i class="fa-solid fa-ticket text-warning"></i>
                            Thông tin vé & Suất chiếu
                        </h3>
                        <div class="summary-line">
                            <span>Rạp chiếu:</span>
                            <strong><c:out value="${not empty currentCinema ? currentCinema.name : showtime.screeningRoom.cinema.name}" /></strong>
                        </div>
                        <div class="summary-line">
                            <span>Phim chiếu:</span>
                            <strong><c:out value="${showtime.movie.title}" /></strong>
                        </div>
                        <div class="summary-line">
                            <span>Suất & Phòng:</span>
                            <span><c:out value="${showtimeText}" /> &bull; <c:out value="${showtime.screeningRoom.name}" /> (<c:out value="${showtime.experienceFormat}" />)</span>
                        </div>
                        <div class="summary-line">
                            <span>Ghế đã chọn:</span>
                            <strong><span id="checkout-seat-count">0</span> ghế</strong>
                        </div>
                        <div class="mt-2 pt-2 border-top border-secondary border-opacity-25" id="selected-seat-list">
                            <span class="text-secondary small">Chưa chọn ghế.</span>
                        </div>
                    </div>

                    <!-- CARD CHỌN LOẠI GIÁ VÉ ÁP DỤNG (ĐÃ TÁCH VÀO PHẦN THANH TOÁN) -->
                    <div class="checkout-section-card">
                        <h3>
                            <i class="fa-solid fa-tags text-warning"></i>
                            Chọn loại giá vé áp dụng
                        </h3>
                        <p class="text-secondary small mb-3">Vui lòng xuất trình thẻ hoặc giấy tờ hợp lệ khi nhân viên soát vé tại rạp.</p>
                        <div class="ticket-class-options">
                            <label class="choice-card is-selected">
                                <input type="radio" name="ticketClass" value="STANDARD" data-discount-percent="0" checked>
                                <span class="choice-radio"></span>
                                <span class="choice-copy">
                                    <strong>Tiêu chuẩn</strong>
                                    <small>Giá vé thông thường (100%)</small>
                                </span>
                                <span class="choice-tag">STANDARD</span>
                            </label>
                            <label class="choice-card">
                                <input type="radio" name="ticketClass" value="STUDENT" data-discount-percent="${studentDiscount}">
                                <span class="choice-radio"></span>
                                <span class="choice-copy">
                                    <strong>Học sinh / Sinh viên</strong>
                                    <small>Giảm <c:out value="${studentDiscount}" />% &bull; Thẻ HSSV hợp lệ</small>
                                </span>
                                <span class="choice-tag">STUDENT</span>
                            </label>
                            <label class="choice-card">
                                <input type="radio" name="ticketClass" value="SENIOR" data-discount-percent="${seniorDiscount}">
                                <span class="choice-radio"></span>
                                <span class="choice-copy">
                                    <strong>Người cao tuổi</strong>
                                    <small>Giảm <c:out value="${seniorDiscount}" />% &bull; CCCD/CMND (>60 tuổi)</small>
                                </span>
                                <span class="choice-tag">SENIOR</span>
                            </label>
                        </div>
                    </div>

                    <!-- CARD BẮP NƯỚC ĐÃ CHỌN -->
                    <div class="checkout-section-card">
                        <h3>
                            <i class="fa-solid fa-bowl-food text-warning"></i>
                            Bắp nước & Combo đã chọn
                        </h3>
                        <div class="summary-line">
                            <span>Số lượng:</span>
                            <strong id="selected-fnb-count">0 món</strong>
                        </div>
                        <div class="mt-2 pt-2 border-top border-secondary border-opacity-25" id="selected-fnb-list">
                            <span class="text-secondary small">Chưa chọn bắp nước.</span>
                        </div>
                    </div>

                    <!-- CARD PHƯƠNG THỨC THANH TOÁN -->
                    <div class="checkout-section-card">
                        <h3>
                            <i class="fa-solid fa-credit-card text-warning"></i>
                            Phương thức thanh toán trực tuyến
                        </h3>
                        <div class="p-3 rounded-3" style="background:#141b2b;border:1px solid #242f48;">
                            <div class="d-flex align-items-center gap-3">
                                <i class="fa-solid fa-qrcode fs-2 text-danger"></i>
                                <div>
                                    <strong class="d-block text-white">Cổng thanh toán điện tử VNPAY</strong>
                                    <small class="text-secondary">Quét mã QR VietQR, thẻ ATM nội địa (30+ ngân hàng) hoặc thẻ Visa / Master / JCB</small>
                                </div>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- CỘT PHẢI: TỔNG KẾT TÀI CHÍNH & NÚT XÁC NHẬN THANH TOÁN -->
                <div class="booking-checkout-right">
                    <div class="order-summary-card">
                        <h2>Tổng kết thanh toán</h2>
                        <p class="text-secondary small mb-3"><c:out value="${not empty currentCinema ? currentCinema.name : 'Hệ thống CineMax'}" /></p>

                        <div class="summary-line">
                            <span>Số lượng vé:</span>
                            <strong><span id="selected-seat-count">0</span> vé</strong>
                        </div>
                        <div class="summary-line">
                            <span>Tổng giá vé gốc:</span>
                            <strong id="ticket-base-subtotal">0đ</strong>
                        </div>
                        <div class="summary-line" style="color: #10b981;">
                            <span>Ưu đãi loại vé:</span>
                            <strong id="ticket-discount">0%</strong>
                        </div>
                        <div class="summary-line">
                            <span>Tổng vé sau ưu đãi:</span>
                            <strong id="ticket-subtotal">0đ</strong>
                        </div>
                        <div class="summary-line">
                            <span>Tạm tính bắp nước:</span>
                            <strong id="fnb-subtotal">0đ</strong>
                        </div>

                        <div class="summary-line summary-grand-total">
                            <span>TỔNG THANH TOÁN:</span>
                            <strong id="grand-total">0đ</strong>
                        </div>

                        <button type="submit" id="btn-final-checkout" class="btn btn-cinema-primary w-100 py-3 fw-bold fs-5 shadow mt-4">
                            Xác nhận & Thanh toán VNPAY &rarr;
                        </button>
                        <button type="button" id="btn-mock-checkout" class="btn btn-outline-warning w-100 py-2 fw-bold mt-2 shadow-sm d-flex align-items-center justify-content-center gap-2">
                            <i class="fa-solid fa-bolt text-warning"></i> Test Thanh Toán Thành Công (Bỏ qua VNPay)
                        </button>
                        <p class="text-secondary small text-center mt-3 mb-0">
                            <i class="fa-solid fa-shield-halved me-1 text-success"></i>
                            Thanh toán an toàn & bảo mật 100% qua Cổng VNPAY.
                        </p>
                    </div>
                </div>
            </div>
        </section>
    </form>
</div>

<jsp:include page="../../common/footer.jsp" />

<script>
    (() => {
        const form = document.getElementById('booking-wizard-form');
        let currentStep = 1;

        // Elements
        const step1SeatCount = document.getElementById('step1-seat-count');
        const step1SeatCodes = document.getElementById('step1-seat-codes');
        const step1NavTotal = document.getElementById('step1-nav-total');
        const step1BreakdownCount = document.getElementById('step1-breakdown-count');
        const step1BreakdownTotal = document.getElementById('step1-breakdown-total');
        const step1SelectedSeatItems = document.getElementById('step1-selected-seat-items');
        const checkoutSeatCount = document.getElementById('checkout-seat-count');
        const selectedSeatCount = document.getElementById('selected-seat-count');
        const selectedSeatList = document.getElementById('selected-seat-list');

        const step2FnbCount = document.getElementById('step2-fnb-count');
        const step2FnbSubtotal = document.getElementById('step2-fnb-subtotal');
        const selectedFnbCount = document.getElementById('selected-fnb-count');
        const selectedFnbList = document.getElementById('selected-fnb-list');

        const ticketBaseSubtotal = document.getElementById('ticket-base-subtotal');
        const ticketSubtotal = document.getElementById('ticket-subtotal');
        const ticketDiscount = document.getElementById('ticket-discount');
        const fnbSubtotal = document.getElementById('fnb-subtotal');
        const grandTotal = document.getElementById('grand-total');
        const quotedTotalInput = document.getElementById('quotedTotal');
        const finalCheckoutBtn = document.getElementById('btn-final-checkout');

        const clientBookingError = document.getElementById('clientBookingError');
        const clientBookingErrorText = document.getElementById('clientBookingErrorText');

        const money = new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 });
        const formatMoney = amount => money.format(Math.round(amount)) + 'đ';

        const showError = (msg) => {
            if (clientBookingError && clientBookingErrorText) {
                clientBookingErrorText.textContent = msg;
                clientBookingError.style.display = 'flex';
                clientBookingError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            } else {
                alert(msg);
            }
        };

        const hideError = () => {
            if (clientBookingError) clientBookingError.style.display = 'none';
        };

        // Stepper Navigation
        window.goToStep = (step) => {
            hideError();
            if (step > 1) {
                const selectedSeats = form ? form.querySelectorAll('input[name="seatIds"]:checked') : [];
                if (!selectedSeats || selectedSeats.length === 0) {
                    showError('Vui lòng chọn ít nhất một ghế trước khi tiếp tục.');
                    return;
                }
                if (!validateCoupleSeats(selectedSeats)) {
                    return;
                }
            }

            // Chuyển panel
            document.querySelectorAll('.booking-step-panel').forEach(p => p.classList.remove('is-active'));
            const target = document.getElementById('step-' + step + '-panel');
            if (target) target.classList.add('is-active');

            // Cập nhật stepper indicator
            document.querySelectorAll('.stepper-item').forEach(item => {
                const s = parseInt(item.dataset.step, 10);
                item.classList.remove('is-active', 'is-completed');
                if (s < step) {
                    item.classList.add('is-completed');
                } else if (s === step) {
                    item.classList.add('is-active');
                }
            });

            currentStep = step;
            window.scrollTo({ top: 0, behavior: 'smooth' });
            updateSummary();
        };

        window.onStepClick = (step) => {
            if (step === 2 && currentStep === 1) {
                proceedToStep2();
            } else {
                goToStep(step);
            }
        };

        let holdTimerInterval = null;
        const startHoldTimer = (seconds) => {
            if (holdTimerInterval) clearInterval(holdTimerInterval);
            let timeLeft = seconds;
            const timerElem = document.getElementById('timer-display');
            const updateTimerDisplay = () => {
                const mins = String(Math.floor(timeLeft / 60)).padStart(2, '0');
                const secs = String(timeLeft % 60).padStart(2, '0');
                if (timerElem) {
                    timerElem.innerText = mins + ':' + secs;
                }
            };
            updateTimerDisplay();

            holdTimerInterval = setInterval(() => {
                timeLeft--;
                updateTimerDisplay();
                if (timeLeft <= 0) {
                    clearInterval(holdTimerInterval);
                    alert('Thời gian giữ ghế 5 phút đã hết hạn! Vui lòng chọn lại ghế.');
                    window.location.reload();
                }
            }, 1000);
        };

        // Chuyển từ bước 1 sang bước 2 có gọi giữ ghế qua /booking/hold
        window.proceedToStep2 = async () => {
            hideError();
            const selectedSeats = form ? form.querySelectorAll('input[name="seatIds"]:checked') : [];
            if (!selectedSeats || selectedSeats.length === 0) {
                showError('Vui lòng chọn ít nhất một ghế trước khi tiếp tục.');
                return;
            }
            if (!validateCoupleSeats(selectedSeats)) {
                return;
            }

            const step2Btn = document.getElementById('btn-proceed-step2');
            const originalBtnHtml = step2Btn ? step2Btn.innerHTML : '';
            if (step2Btn) {
                step2Btn.disabled = true;
                step2Btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Đang giữ ghế...';
            }

            // Khóa giữ ghế 5 phút trên server để khách có thời gian chọn F&B và thanh toán
            try {
                const holdParams = new URLSearchParams();
                holdParams.append('showtimeId', '${showtime.id}');
                selectedSeats.forEach(s => holdParams.append('seatIds', s.value));

                const resp = await fetch('${pageContext.request.contextPath}/booking/hold', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                        'X-Requested-With': 'XMLHttpRequest',
                        'Accept': 'application/json'
                    },
                    body: holdParams
                });
                const resData = await resp.json();

                if (resData.code === 'LOGIN_REQUIRED') {
                    alert('Vui lòng đăng nhập tài khoản để tiếp tục đặt vé.');
                    window.location.href = '${pageContext.request.contextPath}/login?msg=login_required&redirect=' + encodeURIComponent(window.location.href);
                    return;
                }

                const isSuccess = (resData.status === 'SUCCESS' || resData.code === '200' || resData.success === true);
                if (!isSuccess) {
                    showError(resData.message || 'Không thể giữ ghế do ghế đã có người đặt hoặc đang được giữ. Vui lòng chọn ghế khác.');
                    if (step2Btn) {
                        step2Btn.disabled = false;
                        step2Btn.innerHTML = originalBtnHtml;
                    }
                    return;
                }
            } catch (err) {
                console.warn('Seat holding check error:', err);
            } finally {
                if (step2Btn) {
                    step2Btn.disabled = false;
                    step2Btn.innerHTML = originalBtnHtml;
                }
            }

            // Bắt đầu đếm ngược 5 phút giữ ghế
            startHoldTimer(300);

            // Chuyển ngay sang bước 2 (Chọn bắp nước)
            goToStep(2);
        };

        // Validate quy tắc ghế đôi Sweetbox
        const validateCoupleSeats = (selectedSeats) => {
            const coupleSeats = [...selectedSeats].filter(cb => cb.dataset.seatTypeId === '3');
            if (coupleSeats.length > 0) {
                if (coupleSeats.length % 2 !== 0) {
                    showError('Ghế đôi Sweetbox bắt buộc phải chọn theo số lượng chẵn (2, 4, 6... ghế)! Bạn đang chọn ' + coupleSeats.length + ' ghế đôi, không hợp lệ.');
                    return false;
                }
                const byRow = {};
                coupleSeats.forEach(cb => {
                    const row = cb.dataset.seatRow || '';
                    const num = parseInt(cb.dataset.seatNumber || cb.dataset.seatCol || '0', 10);
                    const code = cb.dataset.seatCode || '';
                    if (!byRow[row]) byRow[row] = [];
                    byRow[row].push({ num, code });
                });
                for (const row in byRow) {
                    const rowCouples = byRow[row];
                    if (rowCouples.length % 2 !== 0) {
                        showError('Ghế đôi Sweetbox phải đi theo cặp trong cùng một hàng! Hàng ' + row + ' đang có ' + rowCouples.length + ' ghế đôi lẻ, không thể ghép cặp.');
                        return false;
                    }
                    rowCouples.sort((a, b) => a.num - b.num);
                    for (let i = 0; i < rowCouples.length; i += 2) {
                        const diff = Math.abs(rowCouples[i + 1].num - rowCouples[i].num);
                        if (diff !== 1) {
                            showError('Hai ghế trong mỗi cặp ghế đôi bắt buộc phải nằm LIỀN KỀ NHAU trong cùng một hàng! Cặp ghế ' + rowCouples[i].code + ' và ' + rowCouples[i + 1].code + ' đang bị cách quãng ' + (diff - 1) + ' ghế, không hợp lệ.');
                            return false;
                        }
                    }
                }
            }
            return true;
        };

        // F&B Quantity Controls
        window.increaseFnb = (id, event) => {
            if (event) event.stopPropagation();
            const input = document.getElementById('fnb-' + id);
            if (!input) return;
            const max = Number(input.max) || 99;
            const current = Math.max(0, Math.floor(Number(input.value) || 0));
            if (current < max) {
                input.value = current + 1;
                updateSummary();
            }
        };

        window.decreaseFnb = (id, event) => {
            if (event) event.stopPropagation();
            const input = document.getElementById('fnb-' + id);
            if (!input) return;
            const current = Math.max(0, Math.floor(Number(input.value) || 0));
            if (current > 0) {
                input.value = current - 1;
                updateSummary();
            }
        };

        // Click card to add 1
        document.querySelectorAll('[data-fnb-card]').forEach(card => {
            card.addEventListener('click', event => {
                if (event.target.closest('button, input, label, a')) return;
                const input = card.querySelector('[data-fnb-quantity]');
                if (!input) return;
                const max = Number(input.max) || 99;
                const current = Math.max(0, Math.floor(Number(input.value) || 0));
                if (current < max) {
                    input.value = current + 1;
                    updateSummary();
                }
            });
        });

        // Ticket Class radio select
        document.querySelectorAll('.choice-card').forEach(card => {
            card.addEventListener('click', () => {
                document.querySelectorAll('.choice-card').forEach(c => c.classList.remove('is-selected'));
                card.classList.add('is-selected');
                const radio = card.querySelector('input[type="radio"]');
                if (radio) {
                    radio.checked = true;
                    updateSummary();
                }
            });
        });

        // Real-time calculation
        window.updateSummary = () => {
            if (!form) return;
            const selectedSeats = [...form.querySelectorAll('input[name="seatIds"]:checked')];
            const discountPercent = Number(form.querySelector('input[name="ticketClass"]:checked')?.dataset.discountPercent || 0);
            let baseTicketTotal = 0;
            let discountedTicketTotal = 0;

            const seatCodesList = selectedSeats.map(s => s.dataset.seatCode).join(', ');

            if (step1SeatCount) step1SeatCount.textContent = selectedSeats.length;
            if (step1SeatCodes) step1SeatCodes.textContent = seatCodesList || 'Chưa chọn ghế';
            if (selectedSeatCount) selectedSeatCount.textContent = selectedSeats.length;
            if (checkoutSeatCount) checkoutSeatCount.textContent = selectedSeats.length;

            if (selectedSeatList) selectedSeatList.replaceChildren();

            if (selectedSeats.length === 0) {
                if (selectedSeatList) {
                    const empty = document.createElement('span');
                    empty.className = 'text-secondary small';
                    empty.textContent = 'Chưa chọn ghế.';
                    selectedSeatList.appendChild(empty);
                }
            } else {
                selectedSeats.forEach(seat => {
                    const basePrice = Number(seat.dataset.price || 0);
                    const price = Math.round(basePrice * (100 - discountPercent)) / 100;
                    baseTicketTotal += basePrice;
                    discountedTicketTotal += price;

                    if (selectedSeatList) {
                        const row = document.createElement('div');
                        row.className = 'd-flex justify-content-between py-1 small';
                        const seatName = document.createElement('span');
                        seatName.className = 'text-secondary';
                        seatName.textContent = 'Ghế ' + seat.dataset.seatCode;
                        const seatPrice = document.createElement('strong');
                        seatPrice.className = 'text-white';
                        seatPrice.textContent = formatMoney(price);
                        row.append(seatName, seatPrice);
                        selectedSeatList.appendChild(row);
                    }
                });
            }

            // Cập nhật bảng tổng hợp ghế đã chọn ở Bước 1
            if (step1BreakdownCount) step1BreakdownCount.textContent = selectedSeats.length + ' ghế';
            if (step1BreakdownTotal) step1BreakdownTotal.textContent = formatMoney(baseTicketTotal);
            if (step1NavTotal) step1NavTotal.textContent = formatMoney(baseTicketTotal);

            if (step1SelectedSeatItems) {
                step1SelectedSeatItems.replaceChildren();
                if (selectedSeats.length === 0) {
                    const emptySpan = document.createElement('span');
                    emptySpan.className = 'text-secondary small fst-italic';
                    emptySpan.textContent = 'Chưa chọn ghế nào. Vui lòng nhấp vào ghế trên sơ đồ để chọn chỗ ngồi.';
                    step1SelectedSeatItems.appendChild(emptySpan);
                } else {
                    selectedSeats.forEach(seat => {
                        const basePrice = Number(seat.dataset.price || 0);
                        const seatTypeId = Number(seat.dataset.seatTypeId);
                        let typeBadgeClass = 'bg-secondary text-white';
                        let typeName = 'Ghế thường';
                        if (seatTypeId === 2) {
                            typeBadgeClass = 'bg-warning text-dark';
                            typeName = 'Ghế VIP';
                        } else if (seatTypeId === 3) {
                            typeBadgeClass = 'bg-danger text-white';
                            typeName = 'Sweetbox';
                        }
                        const tag = document.createElement('div');
                        tag.className = 'd-inline-flex align-items-center gap-2 px-3 py-2 rounded-2 shadow-sm';
                        tag.style.background = '#1e2738';
                        tag.style.border = '1px solid #334155';
                        tag.innerHTML = '<span class="badge ' + typeBadgeClass + ' px-2 py-1">' + typeName + '</span>' +
                                        '<strong class="text-white fs-6">Ghế ' + (seat.dataset.seatCode || '') + '</strong>' +
                                        '<span class="text-warning fw-bold">' + formatMoney(basePrice) + '</span>';
                        step1SelectedSeatItems.appendChild(tag);
                    });
                }
            }

            // F&B
            const quantities = [...document.querySelectorAll('[data-fnb-quantity]')];
            let items = 0;
            let fnbTotal = 0;
            if (selectedFnbList) selectedFnbList.replaceChildren();

            quantities.forEach(input => {
                const quantity = Math.max(0, Math.min(Number(input.max) || 99, Math.floor(Number(input.value) || 0)));
                items += quantity;
                const unitPrice = Number(input.dataset.price || 0);
                const lineTotal = quantity * unitPrice;
                fnbTotal += lineTotal;

                if (quantity > 0 && selectedFnbList) {
                    const row = document.createElement('div');
                    row.className = 'd-flex justify-content-between py-1 small';
                    const itemName = document.createElement('span');
                    itemName.className = 'text-secondary';
                    itemName.textContent = input.dataset.name + ' × ' + quantity;
                    const itemTotal = document.createElement('strong');
                    itemTotal.className = 'text-warning';
                    itemTotal.textContent = formatMoney(lineTotal);
                    row.append(itemName, itemTotal);
                    selectedFnbList.appendChild(row);
                }
            });

            if (items === 0 && selectedFnbList) {
                const empty = document.createElement('span');
                empty.className = 'text-secondary small';
                empty.textContent = 'Chưa chọn bắp nước.';
                selectedFnbList.appendChild(empty);
            }

            if (step2FnbCount) step2FnbCount.textContent = items;
            if (step2FnbSubtotal) step2FnbSubtotal.textContent = formatMoney(fnbTotal);
            if (selectedFnbCount) selectedFnbCount.textContent = items + ' món';
            if (ticketBaseSubtotal) ticketBaseSubtotal.textContent = formatMoney(baseTicketTotal);
            if (ticketSubtotal) ticketSubtotal.textContent = formatMoney(discountedTicketTotal);
            if (ticketDiscount) {
                ticketDiscount.textContent = discountPercent + '%' + (discountPercent > 0
                    ? ' (tiết kiệm ' + formatMoney(baseTicketTotal - discountedTicketTotal) + ')'
                    : '');
            }
            if (fnbSubtotal) fnbSubtotal.textContent = formatMoney(fnbTotal);
            const total = discountedTicketTotal + fnbTotal;
            if (grandTotal) grandTotal.textContent = formatMoney(total);
            if (quotedTotalInput) quotedTotalInput.value = total;
        };

        // Listen for seat checkbox changes
        form?.querySelectorAll('input[name="seatIds"]').forEach(input => {
            input.addEventListener('change', () => {
                hideError();
                updateSummary();
            });
        });

        // Submit form
        form?.addEventListener('submit', async event => {
            event.preventDefault();
            hideError();

            const selectedSeats = form.querySelectorAll('input[name="seatIds"]:checked');
            if (selectedSeats.length === 0) {
                showError('Vui lòng chọn ít nhất một ghế trước khi thanh toán.');
                goToStep(1);
                return;
            }
            if (!validateCoupleSeats(selectedSeats)) {
                goToStep(1);
                return;
            }

            if (finalCheckoutBtn) {
                finalCheckoutBtn.disabled = true;
                finalCheckoutBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Đang kết nối Cổng VNPAY...';
            }

            try {
                const formData = new FormData(form);
                const searchParams = new URLSearchParams(formData);
                searchParams.set('ajax', 'true');

                const resp = await fetch(form.action, {
                    method: 'POST',
                    headers: { 'Accept': 'application/json' },
                    body: searchParams
                });

                const data = await resp.json();
                if (data.code === 'LOGIN_REQUIRED') {
                    alert('Vui lòng đăng nhập tài khoản để tiếp tục đặt vé.');
                    window.location.href = '${pageContext.request.contextPath}/login?msg=login_required&redirect=' + encodeURIComponent(window.location.href);
                    return;
                }
                if (data.paymentUrl) {
                    window.location.href = data.paymentUrl;
                } else if (data.data?.status === 'SUCCESS' || data.status === 'SUCCESS' || data.success) {
                    alert('Đặt vé thành công!');
                    const bCode = data.bookingCode || data.data?.bookingCode || '';
                    window.location.href = '${pageContext.request.contextPath}/customer/bookings?code=' + encodeURIComponent(bCode);
                } else {
                    showError(data.message || 'Lỗi xử lý thanh toán.');
                    if (finalCheckoutBtn) {
                        finalCheckoutBtn.disabled = false;
                        finalCheckoutBtn.innerHTML = 'Xác nhận & Thanh toán VNPAY &rarr;';
                    }
                }
            } catch (e) {
                showError('Không thể kết nối đến máy chủ. Vui lòng thử lại.');
                if (finalCheckoutBtn) {
                    finalCheckoutBtn.disabled = false;
                    finalCheckoutBtn.innerHTML = 'Xác nhận & Thanh toán VNPAY &rarr;';
                }
            }
        });

        // Test thanh toán nhanh thành công (bỏ qua cổng VNPay để test nghiệp vụ vé)
        const mockCheckoutBtn = document.getElementById('btn-mock-checkout');
        mockCheckoutBtn?.addEventListener('click', async () => {
            hideError();

            const selectedSeats = form.querySelectorAll('input[name="seatIds"]:checked');
            if (selectedSeats.length === 0) {
                showError('Vui lòng chọn ít nhất một ghế trước khi thanh toán.');
                goToStep(1);
                return;
            }
            if (!validateCoupleSeats(selectedSeats)) {
                goToStep(1);
                return;
            }

            mockCheckoutBtn.disabled = true;
            if (finalCheckoutBtn) finalCheckoutBtn.disabled = true;
            mockCheckoutBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span> Đang tạo đơn và thanh toán...';

            try {
                const formData = new FormData(form);
                formData.set('paymentMethod', 'TEST');
                formData.set('ajax', 'true');
                const searchParams = new URLSearchParams(formData);

                const resp = await fetch(form.action, {
                    method: 'POST',
                    headers: { 'Accept': 'application/json' },
                    body: searchParams
                });

                const data = await resp.json();
                if (data.code === 'LOGIN_REQUIRED') {
                    alert('Vui lòng đăng nhập tài khoản để tiếp tục đặt vé.');
                    window.location.href = '${pageContext.request.contextPath}/login?msg=login_required&redirect=' + encodeURIComponent(window.location.href);
                    return;
                }
                if (data.success || data.status === 'SUCCESS' || data.data?.status === 'SUCCESS') {
                    const bCode = data.bookingCode || data.data?.bookingCode || '';
                    window.location.href = '${pageContext.request.contextPath}/customer/bookings?code=' + encodeURIComponent(bCode);
                } else {
                    showError(data.message || 'Lỗi xử lý test thanh toán.');
                    mockCheckoutBtn.disabled = false;
                    if (finalCheckoutBtn) finalCheckoutBtn.disabled = false;
                    mockCheckoutBtn.innerHTML = '<i class="fa-solid fa-bolt text-warning"></i> Test Thanh Toán Thành Công (Bỏ qua VNPay)';
                }
            } catch (e) {
                showError('Không thể kết nối đến máy chủ. Vui lòng thử lại.');
                mockCheckoutBtn.disabled = false;
                if (finalCheckoutBtn) finalCheckoutBtn.disabled = false;
                mockCheckoutBtn.innerHTML = '<i class="fa-solid fa-bolt text-warning"></i> Test Thanh Toán Thành Công (Bỏ qua VNPay)';
            }
        });

        updateSummary();
    })();
</script>
