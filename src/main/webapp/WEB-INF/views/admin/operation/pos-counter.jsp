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
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/operation.css?v=7">
    <style>
        /* Chú thích loại ghế */
        .seat-legend { display: flex; justify-content: center; flex-wrap: wrap; gap: 14px 20px; margin: -3px 0 16px; color: #64748b; font-size: 11px; }
        .seat-legend span { display: inline-flex; align-items: center; gap: 6px; }
        .legend-seat { display: inline-block; width: 16px; height: 14px; border: 1px solid #cdd1db; border-radius: 4px; }
        .legend-seat.standard { border-color: #cbd5e1; background: #f8fafc; border-bottom: 3px solid #94a3b8; }
        .legend-seat.vip { border-color: #f59e0b; background: #fef3c7; border-bottom: 3px solid #d97706; }
        .legend-seat.couple { border-color: #f472b6; background: #fce7f3; border-bottom: 3px solid #db2777; }
        .legend-seat.selected { border-color: #b91c1c; background: #e50914; }
        .legend-seat.sold { border-color: #aeb3bf; background: #d9dce3; }
        .legend-seat.held { border-color: #64748b; background: repeating-linear-gradient(45deg, #f1f5f9, #f1f5f9 3px, #cbd5e1 3px, #cbd5e1 6px); }

        /* Phân biệt loại ghế trên sơ đồ POS */
        .seat-option.seat-std span {
            color: #334155;
            background: #f8fafc;
            border: 1px solid #cbd5e1;
            border-bottom: 3px solid #94a3b8;
        }
        .seat-option.seat-vip span {
            color: #92400e;
            background: #fef3c7;
            border: 1px solid #f59e0b;
            border-bottom: 3px solid #d97706;
            font-weight: 800;
        }
        .seat-option.seat-couple span {
            color: #9d174d;
            background: #fce7f3;
            border: 1px solid #f472b6;
            border-bottom: 3px solid #db2777;
            font-weight: 800;
        }

        /* Hiệu ứng rê chuột */
        .seat-option.seat-std:hover:not(.is-sold):not(.is-held):not(.is-unavailable) span {
            color: #0f172a;
            border-color: #64748b;
            transform: translateY(-2px);
            box-shadow: 0 4px 10px rgba(0,0,0,0.12);
        }
        .seat-option.seat-vip:hover:not(.is-sold):not(.is-held):not(.is-unavailable) span {
            color: #78350f;
            border-color: #b45309;
            transform: translateY(-2px);
            box-shadow: 0 4px 10px rgba(217, 119, 6, 0.3);
        }
        .seat-option.seat-couple:hover:not(.is-sold):not(.is-held):not(.is-unavailable) span {
            color: #831843;
            border-color: #be185d;
            transform: translateY(-2px);
            box-shadow: 0 4px 10px rgba(219, 39, 119, 0.3);
        }

        /* Ghế đang chọn */
        .seat-option input:checked + span {
            color: #ffffff !important;
            background: #e50914 !important;
            border-color: #b91c1c !important;
            border-bottom: 3px solid #991b1b !important;
            box-shadow: 0 0 12px rgba(229, 9, 20, 0.7) !important;
            transform: scale(1.08) !important;
            z-index: 2;
        }

        /* Ghế đã bán / giữ */
        .seat-option.is-sold span {
            color: #858a96 !important;
            background: #d9dce3 !important;
            border-color: #c1c5ce !important;
            border-bottom: 3px solid #aeb3bf !important;
            cursor: not-allowed;
        }
        .seat-option.is-held span {
            color: #475569 !important;
            background: repeating-linear-gradient(45deg, #f1f5f9, #f1f5f9 4px, #e2e8f0 4px, #e2e8f0 8px) !important;
            border: 1px dashed #94a3b8 !important;
            border-bottom: 3px solid #64748b !important;
            cursor: not-allowed;
            opacity: 0.85;
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

            <!-- HEADER CỦA QUẦY POS CÓ HIỂN THỊ TÊN RẠP NỔI BẬT -->
            <header class="operation-heading">
                <div>
                    <span class="operation-eyebrow">VẬN HÀNH · POS COUNTER</span>
                    <h1>Quầy bán vé</h1>
                    <p>Quy trình bán vé 4 bước: Chọn lịch chiếu &rarr; Chọn ghế &rarr; Chọn bắp nước &rarr; Thanh toán.</p>
                </div>
                <div class="header-chips">
                    <div class="pos-cinema-chip">
                        <span class="cinema-dot"></span>
                        <div>
                            <small>Rạp chiếu hiện tại</small>
                            <strong>
                                <c:choose>
                                    <c:when test="${not empty currentCinema}"><c:out value="${currentCinema.name}" /></c:when>
                                    <c:when test="${not empty selectedShowtimeDetails.screeningRoom.cinema}"><c:out value="${selectedShowtimeDetails.screeningRoom.cinema.name}" /></c:when>
                                    <c:otherwise>CineMax Chain</c:otherwise>
                                </c:choose>
                            </strong>
                        </div>
                    </div>
                    <div class="operator-chip">
                        <span class="operator-dot"></span>
                        <div>
                            <small>Nhân viên trực</small>
                            <strong><c:out value="${staff.fullName}" /></strong>
                        </div>
                    </div>
                </div>
            </header>

            <!-- THANH TIẾN TRÌNH STEPPER 4 BƯỚC (ẨN ĐI SAU KHI ĐÃ THANH TOÁN THÀNH CÔNG) -->
            <c:if test="${empty checkoutResult}">
                <nav class="pos-stepper" aria-label="Tiến trình bán vé tại quầy">
                    <div class="stepper-item ${empty selectedShowtime ? 'is-active' : 'is-completed'}" data-step="1" onclick="goToStep(1)">
                        <div class="step-circle">1</div>
                        <div class="step-info">
                            <span class="step-title">1. Chọn lịch chiếu</span>
                            <small class="step-sub">Chọn phim & suất</small>
                        </div>
                    </div>
                    <div class="stepper-line"></div>
                    <div class="stepper-item ${not empty selectedShowtime ? 'is-active' : ''}" data-step="2" onclick="goToStep(2)">
                        <div class="step-circle">2</div>
                        <div class="step-info">
                            <span class="step-title">2. Chọn ghế</span>
                            <small class="step-sub">Chọn ghế ngồi</small>
                        </div>
                    </div>
                    <div class="stepper-line"></div>
                    <div class="stepper-item" data-step="3" onclick="goToStep(3)">
                        <div class="step-circle">3</div>
                        <div class="step-info">
                            <span class="step-title">3. Chọn bắp nước</span>
                            <small class="step-sub">F&B & combo</small>
                        </div>
                    </div>
                    <div class="stepper-line"></div>
                    <div class="stepper-item" data-step="4" onclick="goToStep(4)">
                        <div class="step-circle">4</div>
                        <div class="step-info">
                            <span class="step-title">4. Thanh toán</span>
                            <small class="step-sub">Loại vé & xuất hóa đơn</small>
                        </div>
                    </div>
                </nav>
            </c:if>

            <!-- THÔNG BÁO LỖI VÀ TRẠNG THÁI -->
            <div id="clientPosError" class="operation-alert alert-error" role="alert" style="display:none; margin-bottom: 16px;">
                <span class="alert-icon">!</span>
                <span id="clientPosErrorText"></span>
            </div>
            <c:if test="${not empty posError}">
                <div class="operation-alert alert-error" role="alert"><span class="alert-icon">!</span><c:out value="${posError}" /></div>
            </c:if>
            <c:if test="${not empty posSetupMessage}">
                <div class="operation-alert alert-info" role="status"><span class="alert-icon">i</span><c:out value="${posSetupMessage}" /></div>
            </c:if>

            <!-- KẾT QUẢ GIAO DỊCH SAU KHI THANH TOÁN THÀNH CÔNG -->
            <c:if test="${not empty checkoutResult}">
                <section class="receipt-card" id="receipt" aria-labelledby="receipt-title">
                    <div class="receipt-mark">✓</div>
                    <div class="receipt-copy">
                        <span class="operation-eyebrow">GIAO DỊCH HOÀN TẤT</span>
                        <h2 id="receipt-title">Đơn hàng đã được tạo và thanh toán</h2>
                        <p>Mã đơn <strong><c:out value="${checkoutResult.bookingCode}" /></strong><span class="receipt-divider">·</span>Trạng thái <strong><c:out value="${checkoutResult.status}" /></strong></p>
                        <p class="receipt-showtime">Rạp: <strong><c:out value="${not empty currentCinema ? currentCinema.name : checkoutShowtime.screeningRoom.cinema.name}" /></strong> · <c:out value="${checkoutShowtime.movie.title}" /> · <c:out value="${not empty checkoutShowtime.formattedStartTime ? checkoutShowtime.formattedStartTime : checkoutShowtime.startTime}" /> · <c:out value="${checkoutShowtime.screeningRoom.name}" /></p>
                        <p class="receipt-showtime">Thanh toán: <strong><c:choose><c:when test="${checkoutResult.paymentMethod eq 'CASH'}">Tiền mặt</c:when><c:when test="${checkoutResult.paymentMethod eq 'VNPAY_POS' or checkoutResult.paymentMethod eq 'VNPAY'}">Cổng VNPAY</c:when><c:otherwise><c:out value="${checkoutResult.paymentMethod}" /></c:otherwise></c:choose></strong></p>
                    </div>
                    <div class="receipt-total"><small>Tổng thanh toán</small><strong><fmt:formatNumber value="${checkoutResult.finalAmount}" pattern="#,##0" groupingUsed="true" /><span>đ</span></strong></div>
                    <div style="display: flex; gap: 10px; align-items: center;">
                        <button class="button button-secondary receipt-print" type="button" onclick="window.print()">In hóa đơn</button>
                        <a href="${pageContext.request.contextPath}/operation/pos" class="button button-primary">Bán đơn mới →</a>
                    </div>
                </section>
                <section class="receipt-detail-card" aria-label="Chi tiết xác nhận thanh toán" style="margin-bottom: 20px;">
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

            <!-- =============================================================== -->
            <!-- BƯỚC 1: MÀN HÌNH CHỌN LỊCH CHIẾU                              -->
            <!-- =============================================================== -->
            <section class="pos-step-panel ${empty selectedShowtime ? 'is-active' : ''}" id="step-1-panel">
                <div class="admin-card pos-picker-card" aria-labelledby="pos-picker-title">
                    <div class="section-heading compact-heading" style="margin-bottom: 16px;">
                        <div class="section-number">01</div>
                        <div>
                            <h2 id="pos-picker-title">Bước 1: Chọn lịch chiếu</h2>
                            <p>
                                Rạp: <strong><c:out value="${not empty currentCinema ? currentCinema.name : 'Chi nhánh đang trực'}" /></strong>
                                <c:if test="${not empty currentCinema.address}"> &bull; <c:out value="${currentCinema.address}" /></c:if>
                            </p>
                        </div>
                    </div>

                    <form class="pos-picker-form" id="pos-picker-form" method="get" action="${pageContext.request.contextPath}/operation/pos" style="display:flex; align-items:flex-end; gap:16px; flex-wrap:wrap; margin-bottom: 20px;">
                        <input type="hidden" name="showtimeId" id="pos-showtime-input" value="${selectedShowtime}">
                        <c:if test="${not empty cinemaId}"><input type="hidden" name="cinemaId" value="${cinemaId}"></c:if>
                        <label class="field-group date-field" style="margin: 0; min-width: 220px;">
                            <span style="display:block; font-weight:700; margin-bottom:6px; color:#334155;">Chọn ngày chiếu:</span>
                            <input type="date" name="date" value="${selectedDate}" min="${today}"
                                   onchange="document.getElementById('pos-showtime-input').value='';this.form.submit()"
                                   style="padding: 9px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-weight: 600; width: 100%;">
                        </label>
                        <c:if test="${not empty cinemas and fn:length(cinemas) gt 1}">
                            <label class="field-group branch-field" style="margin: 0; min-width: 240px;">
                                <span style="display:block; font-weight:700; margin-bottom:6px; color:#334155;">Chọn chi nhánh:</span>
                                <select name="cinemaId" onchange="document.getElementById('pos-showtime-input').value='';this.form.submit()"
                                        style="padding: 9px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-weight: 600; width: 100%;">
                                    <c:forEach items="${cinemas}" var="cinema">
                                        <option value="${cinema.id}" ${cinema.id eq cinemaId ? 'selected' : ''}><c:out value="${cinema.name}" /></option>
                                    </c:forEach>
                                </select>
                            </label>
                        </c:if>
                    </form>

                    <c:if test="${empty showtimes and empty posSetupMessage}">
                        <div class="empty-inline" style="margin-top: 20px;">Không có suất chiếu còn bán trong ngày đã chọn tại rạp này.</div>
                    </c:if>

                    <!-- DANH SÁCH THẺ PHIM & SUẤT CHIẾU TRỰC QUAN -->
                    <c:if test="${not empty showtimes}">
                        <div style="margin-top: 24px;">
                            <h3 style="font-size: 14px; font-weight: 700; color: #475569; margin-bottom: 12px; text-transform: uppercase; letter-spacing: 0.05em;">Danh sách suất chiếu trong ngày:</h3>
                            <div class="pos-movies-showtimes">
                                <c:forEach items="${showtimes}" var="st">
                                    <article class="pos-movie-card">
                                        <div class="pos-movie-poster-wrap">
                                            <c:choose>
                                                <c:when test="${not empty st.movie.posterUrl}">
                                                    <img class="pos-movie-poster" src="<c:out value='${st.movie.posterUrl}' />" alt="<c:out value='${st.movie.title}' />" onerror="this.src='${pageContext.request.contextPath}/assets/images/placeholder-poster.png'" loading="lazy">
                                                </c:when>
                                                <c:otherwise>
                                                    <div style="width:100%;height:100%;display:flex;align-items:center;justify-content:center;color:#64748b;font-size:12px;">No poster</div>
                                                </c:otherwise>
                                            </c:choose>
                                            <span class="pos-age-badge"><c:out value="${not empty st.movie.ageRating ? st.movie.ageRating : 'P'}" /></span>
                                        </div>
                                        <div class="pos-movie-content">
                                            <div class="pos-movie-title"><c:out value="${st.movie.title}" /></div>
                                            <div class="pos-movie-meta">
                                                <c:if test="${not empty st.movie.durationMinutes and st.movie.durationMinutes gt 0}"><c:out value="${st.movie.durationMinutes}" /> phút &bull; </c:if>
                                                Phòng: <strong><c:out value="${st.screeningRoom.name}" /></strong>
                                            </div>
                                            <div class="pos-showtime-buttons">
                                                <button type="button" class="pos-time-btn ${st.id eq selectedShowtime ? 'is-active' : ''}" onclick="selectShowtimeAndLoad('${st.id}')" title="Chọn suất chiếu này">
                                                    <span><c:out value="${st.startTime}" /></span>
                                                    <small>(<c:out value="${st.experienceFormat}" />)</small>
                                                    <span class="btn-pick-label">Chọn ghế &rarr;</span>
                                                </button>
                                            </div>
                                        </div>
                                    </article>
                                </c:forEach>
                            </div>
                        </div>
                    </c:if>

                    <c:if test="${not empty selectedShowtime}">
                        <div class="pos-step-nav" style="margin-top: 24px;">
                            <span class="nav-summary-pill">Đang chọn: <strong><c:out value="${selectedShowtimeDetails.movie.title}" /></strong> (<c:out value="${selectedShowtimeDetails.startTime}" /> &bull; <c:out value="${selectedShowtimeDetails.screeningRoom.name}" />)</span>
                            <button type="button" class="button button-primary btn-step-next" onclick="goToStep(2)">Chọn ghế &rarr;</button>
                        </div>
                    </c:if>
                </div>
            </section>

            <!-- =============================================================== -->
            <!-- FORM ĐẶT VÉ CHÍNH: CHỨA BƯỚC 2 (GHẾ), BƯỚC 3 (FNB), BƯỚC 4 (PAY) -->
            <!-- =============================================================== -->
            <c:if test="${not empty selectedShowtime}">
                <form class="pos-order-form-wizard" id="pos-order-form" method="post" action="${pageContext.request.contextPath}/operation/pos">
                    <input type="hidden" name="checkoutConfirmed" value="false">
                    <input type="hidden" name="cinemaId" value="${cinemaId}">
                    <input type="hidden" name="date" value="${selectedDate}">
                    <input type="hidden" name="showtimeId" value="${selectedShowtime}">
                    <input type="hidden" name="quotedTotal" value="">

                    <!-- =========================================================== -->
                    <!-- BƯỚC 2: MÀN HÌNH CHỌN GHẾ                                  -->
                    <!-- =========================================================== -->
                    <section class="pos-step-panel ${not empty selectedShowtime ? 'is-active' : ''}" id="step-2-panel">
                        <div class="admin-card operation-card seat-card" aria-labelledby="seat-title">
                            <div class="section-heading">
                                <div class="section-number">02</div>
                                <div>
                                    <h2 id="seat-title">Bước 2: Chọn ghế</h2>
                                    <p>Chạm vào ghế còn trống để chọn ghế cho khách.</p>
                                </div>
                            </div>

                            <div class="selected-showtime-info" style="background:#f8fafc; padding: 12px 16px; border-radius: 8px; border: 1px solid #e2e8f0; margin-bottom: 20px;">
                                <span>Rạp: <strong><c:out value="${not empty currentCinema ? currentCinema.name : selectedShowtimeDetails.screeningRoom.cinema.name}" /></strong></span>
                                <span>&bull;</span>
                                <span>Phim: <strong><c:out value="${selectedShowtimeDetails.movie.title}" /></strong></span>
                                <span>&bull;</span>
                                <span>Giờ chiếu: <strong><c:out value="${selectedShowtimeDetails.startTime}" /></strong></span>
                                <span>&bull;</span>
                                <span>Phòng: <strong><c:out value="${selectedShowtimeDetails.screeningRoom.name}" /></strong> (<c:out value="${selectedShowtimeDetails.experienceFormat}" />)</span>
                            </div>

                            <div class="seat-legend" aria-label="Chú thích trạng thái và loại ghế">
                                <span><i class="legend-seat standard"></i>Ghế thường</span>
                                <span><i class="legend-seat vip"></i>Ghế VIP</span>
                                <span><i class="legend-seat couple"></i>Ghế đôi Sweetbox</span>
                                <span><i class="legend-seat selected"></i>Đang chọn</span>
                                <span><i class="legend-seat sold"></i>Đã bán</span>
                                <span><i class="legend-seat held"></i>Đang giữ</span>
                            </div>

                            <div class="screen-label"><span>MÀN HÌNH</span></div>

                            <c:choose>
                                <c:when test="${not empty seatMap}">
                                    <div class="seat-grid-scroll">
                                        <div class="seat-grid" style="grid-template-columns: repeat(${seatColumnCount}, 36px);">
                                            <c:forEach items="${seatMap}" var="seat">
                                                <label class="seat-option ${seat.status eq 'AVAILABLE' ? 'is-available' : seat.status eq 'SOLD' ? 'is-sold' : seat.status eq 'HELD' ? 'is-held' : 'is-unavailable'} ${seat.seatTypeId eq 2 ? 'seat-vip' : seat.seatTypeId eq 3 ? 'seat-couple' : 'seat-std'}"
                                                       style="grid-row: ${seat.gridRowIndex}; grid-column: ${seat.gridColIndex};"
                                                       title="Ghế ${seat.seatCode} (${seat.seatTypeId eq 2 ? 'VIP' : seat.seatTypeId eq 3 ? 'Sweetbox' : 'Thường'}) - <fmt:formatNumber value='${seatPrices[seat.id]}' pattern='#,##0' /> đ">
                                                    <c:choose>
                                                        <c:when test="${seat.status eq 'AVAILABLE'}">
                                                            <input type="checkbox" name="seatIds" value="${seat.id}"
                                                                   aria-label="Ghế ${seat.seatCode}, còn trống"
                                                                   data-seat-code="${seat.seatCode}" data-seat-row="${seat.seatRow}" data-seat-number="${seat.seatNumber}" data-seat-col="${seat.gridColIndex}" data-seat-type-id="${seat.seatTypeId}" data-price="${seatPrices[seat.id]}">
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
                                <c:otherwise>
                                    <div class="empty-state"><span>○</span><strong>Phòng chưa có cấu hình ghế</strong><small>Hãy cấu hình ghế cho phòng chiếu trước khi bán.</small></div>
                                </c:otherwise>
                            </c:choose>

                            <!-- FOOTER ĐIỀU HƯỚNG BƯỚC 2 -->
                            <div class="pos-step-nav">
                                <button type="button" class="button button-secondary btn-step-prev" onclick="goToStep(1)">&larr; Chọn lại suất chiếu</button>
                                <span class="nav-summary-pill">Đang chọn: <strong id="step2-seat-count">0</strong> ghế (<span id="step2-seat-codes">Chưa chọn ghế</span>)</span>
                                <button type="button" class="button button-primary btn-step-next" onclick="proceedToStep3()">Tiếp tục: Chọn bắp nước &rarr;</button>
                            </div>
                        </div>
                    </section>

                    <!-- =========================================================== -->
                    <!-- BƯỚC 3: MÀN HÌNH CHỌN BẮP NƯỚC (F&B)                        -->
                    <!-- =========================================================== -->
                    <section class="pos-step-panel" id="step-3-panel">
                        <div class="admin-card operation-card concessions-card" aria-labelledby="concessions-title">
                            <div class="section-heading">
                                <div class="section-number">03</div>
                                <div>
                                    <h2 id="concessions-title">Bước 3: Chọn bắp nước & combo</h2>
                                    <p>Chạm vào thẻ món để thêm một phần, hoặc chỉnh số lượng mong muốn.</p>
                                </div>
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
                                                        <div class="product-quantity">
                                                            <label for="fnb-${item.id}">Số lượng</label>
                                                            <input id="fnb-${item.id}" type="number" name="fnb_${item.id}" min="0" max="${item.stockQuantity le 99 ? item.stockQuantity : 99}" value="0" data-fnb-quantity data-price="${item.price}" data-name="<c:out value='${item.name}' />">
                                                        </div>
                                                        <span class="stock-label">Còn <c:out value="${item.stockQuantity}" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </article>
                                        </c:forEach>
                                    </div>
                                </c:when>
                                <c:otherwise><div class="empty-state"><span>◌</span><strong>Chưa có sản phẩm còn hàng</strong><small>Kiểm tra tồn kho của chi nhánh rồi thử lại.</small></div></c:otherwise>
                            </c:choose>

                            <!-- FOOTER ĐIỀU HƯỚNG BƯỚC 3 -->
                            <div class="pos-step-nav">
                                <button type="button" class="button button-secondary btn-step-prev" onclick="goToStep(2)">&larr; Quay lại chọn ghế</button>
                                <span class="nav-summary-pill">Bắp nước: <strong id="step3-fnb-count">0</strong> món · Tạm tính: <strong id="step3-fnb-subtotal">0đ</strong></span>
                                <button type="button" class="button button-primary btn-step-next" onclick="goToStep(4)">Tiếp tục: Thanh toán &rarr;</button>
                            </div>
                        </div>
                    </section>

                    <!-- =========================================================== -->
                    <!-- BƯỚC 4: MÀN HÌNH THANH TOÁN (CÓ CHỌN LOẠI GIÁ VÉ)           -->
                    <!-- =========================================================== -->
                    <section class="pos-step-panel" id="step-4-panel">
                        <div style="margin-bottom: 16px;">
                            <button type="button" class="button button-secondary btn-step-prev" onclick="goToStep(3)">&larr; Quay lại chọn bắp nước</button>
                        </div>

                        <div class="pos-checkout-layout">
                            <!-- CỘT TRÁI: CHI TIẾT ĐƠN HÀNG & CHỌN LOẠI VÉ & PHƯƠNG THỨC THANH TOÁN -->
                            <div class="pos-checkout-left">

                                <!-- CARD THÔNG TIN SUẤT CHIẾU & RẠP -->
                                <div class="checkout-section-card">
                                    <h3>
                                        <span class="section-number" style="width:24px;height:24px;font-size:12px;">★</span>
                                        Thông tin vé & Suất chiếu
                                    </h3>
                                    <div class="summary-line" style="margin-bottom: 6px;">
                                        <span>Rạp chiếu:</span>
                                        <strong><c:out value="${not empty currentCinema ? currentCinema.name : selectedShowtimeDetails.screeningRoom.cinema.name}" /></strong>
                                    </div>
                                    <div class="summary-line" style="margin-bottom: 6px;">
                                        <span>Phim chiếu:</span>
                                        <strong><c:out value="${selectedShowtimeDetails.movie.title}" /></strong>
                                    </div>
                                    <div class="summary-line" style="margin-bottom: 6px;">
                                        <span>Suất & Phòng:</span>
                                        <span><c:out value="${selectedShowtimeDetails.startTime}" /> &bull; <c:out value="${selectedShowtimeDetails.screeningRoom.name}" /> (<c:out value="${selectedShowtimeDetails.experienceFormat}" />)</span>
                                    </div>
                                    <div class="summary-line" style="margin-bottom: 6px;">
                                        <span>Ghế đã chọn:</span>
                                        <strong><span id="checkout-seat-count">0</span> ghế</strong>
                                    </div>
                                    <div class="summary-detail-list" id="selected-seat-list" aria-live="polite" style="max-height: 160px; overflow-y: auto;">
                                        <span class="summary-empty">Chưa chọn ghế.</span>
                                    </div>
                                </div>

                                <!-- CARD CHỌN LOẠI GIÁ VÉ (ĐÃ TÁCH VÀO PHẦN THANH TOÁN THEO YÊU CẦU) -->
                                <div class="checkout-section-card">
                                    <h3>
                                        <span class="section-number" style="width:24px;height:24px;font-size:12px;">03</span>
                                        Chọn loại giá vé áp dụng
                                    </h3>
                                    <p style="font-size: 12px; color: #64748b; margin-bottom: 12px;">Nhân viên kiểm tra thẻ/giấy tờ ưu đãi hợp lệ của khách tại quầy.</p>
                                    <div class="ticket-class-options">
                                        <label class="choice-card is-selected">
                                            <input type="radio" name="ticketClass" value="STANDARD" data-discount-percent="0" checked>
                                            <span class="choice-radio"></span>
                                            <span class="choice-copy"><strong>Tiêu chuẩn</strong><small>Giá vé thông thường (100%)</small></span>
                                            <span class="choice-tag">STANDARD</span>
                                        </label>
                                        <label class="choice-card">
                                            <input type="radio" name="ticketClass" value="STUDENT" data-discount-percent="${studentDiscount}">
                                            <span class="choice-radio"></span>
                                            <span class="choice-copy"><strong>Học sinh / Sinh viên</strong><small>Giảm <c:out value="${studentDiscount}" />% &bull; Thẻ HSSV hợp lệ</small></span>
                                            <span class="choice-tag">STUDENT</span>
                                        </label>
                                        <label class="choice-card">
                                            <input type="radio" name="ticketClass" value="SENIOR" data-discount-percent="${seniorDiscount}">
                                            <span class="choice-radio"></span>
                                            <span class="choice-copy"><strong>Người cao tuổi</strong><small>Giảm <c:out value="${seniorDiscount}" />% &bull; CCCD/CMND hợp lệ</small></span>
                                            <span class="choice-tag">SENIOR</span>
                                        </label>
                                    </div>
                                </div>

                                <!-- CARD BẮP NƯỚC ĐÃ CHỌN -->
                                <div class="checkout-section-card">
                                    <h3>
                                        <span class="section-number" style="width:24px;height:24px;font-size:12px;">04</span>
                                        Bắp nước & Combo đã chọn
                                    </h3>
                                    <div class="summary-line" style="margin-bottom: 6px;">
                                        <span>Số lượng:</span>
                                        <strong id="selected-fnb-count">0 món</strong>
                                    </div>
                                    <div class="summary-detail-list" id="selected-fnb-list" style="max-height: 140px; overflow-y: auto;">
                                        <span class="summary-empty">Chưa chọn bắp nước.</span>
                                    </div>
                                </div>

                                <!-- CARD PHƯƠNG THỨC THANH TOÁN & QUÉT QR -->
                                <div class="checkout-section-card">
                                    <h3>
                                        <span class="section-number" style="width:24px;height:24px;font-size:12px;">05</span>
                                        Phương thức thanh toán
                                    </h3>
                                    <label class="field-group payment-field" style="margin-bottom: 12px;">
                                        <select name="paymentMethod" id="pos-payment-method" required onchange="onPaymentMethodChange()">
                                            <option value="CASH">Tiền mặt (CASH)</option>
                                            <option value="VNPAY">Cổng thanh toán điện tử VNPAY (Thẻ / QR / App)</option>
                                        </select>
                                    </label>

                                    <!-- KHUNG HIỂN THỊ CỔNG VNPAY & MÃ QR TỰ ĐỘNG KHI CHỌN VNPAY -->
                                    <div id="pos-vnpay-qr-container" style="display:none; text-align:center; padding:16px; background:#181923; border-radius:10px; border:1px solid #334155; margin-top: 12px;">
                                        <div style="font-weight:700; color:#e5a93b; margin-bottom:10px; font-size:13px; text-transform:uppercase; letter-spacing:0.5px;">
                                            <i class="fa-solid fa-qrcode me-1"></i> Cổng Thanh Toán VNPAY Tại Quầy POS
                                        </div>
                                        <div id="pos-vnpay-loading" style="display:none; padding:16px; color:#94a3b8; font-size:13px;">
                                            <span class="spinner-border spinner-border-sm me-2"></span> Đang tạo mã QR VNPAY...
                                        </div>
                                        <div id="pos-vnpay-content">
                                            <img id="pos-vnpay-qr-image" src="" alt="Mã QR VNPay POS" style="width:190px; height:190px; border-radius:8px; background:#fff; padding:6px; display:inline-block; box-shadow:0 4px 14px rgba(0,0,0,0.4);">
                                            <p style="margin-top:10px; font-size:13px; color:#cbd5e1; line-height:1.5; margin-bottom:0;">
                                                Khách hàng quét mã QR bằng App Ngân hàng hoặc ví VNPay.<br>
                                                Số tiền thanh toán: <strong id="pos-qr-amount-text" style="color:#22c55e; font-size: 15px;"></strong>
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- CỘT PHẢI: TỔNG KẾT TÀI CHÍNH & NÚT XÁC NHẬN THANH TOÁN -->
                            <div class="pos-checkout-right" style="position: sticky; top: 18px;">
                                <div class="admin-card operation-card order-summary-card">
                                    <div class="section-heading summary-heading" style="margin-bottom: 14px;">
                                        <div class="section-number">✓</div>
                                        <div>
                                            <h2 id="summary-title" style="font-size: 16px;">Tổng kết thanh toán</h2>
                                            <p><c:out value="${not empty currentCinema ? currentCinema.name : 'Quầy CineMax'}" /></p>
                                        </div>
                                    </div>

                                    <div class="summary-line"><span>Số lượng vé:</span><strong><span id="selected-seat-count">0</span> vé</strong></div>
                                    <div class="summary-line"><span>Tổng giá vé gốc:</span><strong id="ticket-base-subtotal">0đ</strong></div>
                                    <div class="summary-line" style="color: #10b981;"><span>Ưu đãi loại vé:</span><strong id="ticket-discount">0%</strong></div>
                                    <div class="summary-line"><span>Tổng vé sau ưu đãi:</span><strong id="ticket-subtotal">0đ</strong></div>
                                    <div class="summary-line"><span>Tạm tính bắp nước:</span><strong id="fnb-subtotal">0đ</strong></div>
                                    <hr style="border:0;border-top:1px dashed #cbd5e1;margin:12px 0;">
                                    <div class="summary-line summary-grand-total" style="font-size: 15px;">
                                        <span>TỔNG THANH TOÁN:</span>
                                        <strong id="grand-total" style="font-size: 20px; color: #e50914;">0đ</strong>
                                    </div>

                                    <p class="summary-help" style="margin-top: 10px; margin-bottom: 16px;">Đơn hàng sẽ được ghi nhận và in vé ngay khi bấm xác nhận thanh toán.</p>

                                    <button class="button button-primary checkout-button" id="btn-final-checkout" type="submit" style="width: 100%; min-height: 48px; font-size: 14px; font-weight: 700;">
                                        Xác nhận thanh toán & Xuất vé &rarr;
                                    </button>
                                </div>
                            </div>
                        </div>
                    </section>
                </form>
            </c:if>

        </main>
    </div>
</div>

<script>
    (() => {
        const form = document.getElementById('pos-order-form');
        const hasSelectedShowtime = ${not empty selectedShowtime};
        let currentStep = hasSelectedShowtime ? 2 : 1;

        // Elements
        const seatCount = document.getElementById('selected-seat-count');
        const checkoutSeatCount = document.getElementById('checkout-seat-count');
        const step2SeatCount = document.getElementById('step2-seat-count');
        const step2SeatCodes = document.getElementById('step2-seat-codes');
        const selectedSeatList = document.getElementById('selected-seat-list');
        const ticketBaseSubtotal = document.getElementById('ticket-base-subtotal');
        const ticketSubtotal = document.getElementById('ticket-subtotal');
        const ticketDiscount = document.getElementById('ticket-discount');
        const fnbCount = document.getElementById('selected-fnb-count');
        const step3FnbCount = document.getElementById('step3-fnb-count');
        const fnbSubtotal = document.getElementById('fnb-subtotal');
        const step3FnbSubtotal = document.getElementById('step3-fnb-subtotal');
        const selectedFnbList = document.getElementById('selected-fnb-list');
        const grandTotal = document.getElementById('grand-total');
        const finalCheckoutBtn = document.getElementById('btn-final-checkout');

        const clientPosError = document.getElementById('clientPosError');
        const clientPosErrorText = document.getElementById('clientPosErrorText');

        const money = new Intl.NumberFormat('en-US', { maximumFractionDigits: 0 });
        const formatMoney = amount => money.format(Math.round(amount)) + 'đ';

        const showPosError = (msg) => {
            if (clientPosError && clientPosErrorText) {
                clientPosErrorText.textContent = msg;
                clientPosError.style.display = 'flex';
                clientPosError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            } else {
                alert(msg);
            }
        };

        const hidePosError = () => {
            if (clientPosError) clientPosError.style.display = 'none';
        };

        // Stepper Navigation
        window.goToStep = (step) => {
            hidePosError();
            if (step > 1 && !hasSelectedShowtime) {
                showPosError('Vui lòng chọn suất chiếu trước.');
                return;
            }
            if (step > 2) {
                const selectedSeats = form ? form.querySelectorAll('input[name="seatIds"]:checked') : [];
                if (!selectedSeats || selectedSeats.length === 0) {
                    showPosError('Vui lòng chọn ít nhất một ghế trước khi tiếp tục.');
                    return;
                }
                if (!validateCoupleSeats(selectedSeats)) {
                    return;
                }
            }

            // Toggle panels
            document.querySelectorAll('.pos-step-panel').forEach(p => p.classList.remove('is-active'));
            const targetPanel = document.getElementById('step-' + step + '-panel');
            if (targetPanel) {
                targetPanel.classList.add('is-active');
            }

            // Update stepper indicator
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
            if (step === 4) {
                onPaymentMethodChange();
            }
        };

        window.proceedToStep3 = () => {
            goToStep(3);
        };

        window.selectShowtimeAndLoad = (showtimeId) => {
            const input = document.getElementById('pos-showtime-input');
            const form = document.getElementById('pos-picker-form');
            if (input && form) {
                input.value = showtimeId;
                form.submit();
            }
        };

        // Validate quy tắc ghế đôi Sweetbox tại quầy POS
        const validateCoupleSeats = (selectedSeats) => {
            const coupleSeats = [...selectedSeats].filter(cb => cb.dataset.seatTypeId === '3');
            if (coupleSeats.length > 0) {
                if (coupleSeats.length % 2 !== 0) {
                    showPosError('Ghế đôi Sweetbox bắt buộc phải chọn theo số lượng chẵn (2, 4, 6... ghế)! Bạn đang chọn ' + coupleSeats.length + ' ghế đôi, không hợp lệ.');
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
                        showPosError('Ghế đôi Sweetbox phải đi theo cặp trong cùng một hàng! Hàng ' + row + ' đang có ' + rowCouples.length + ' ghế đôi lẻ, không thể ghép cặp.');
                        return false;
                    }
                    rowCouples.sort((a, b) => a.num - b.num);
                    for (let i = 0; i < rowCouples.length; i += 2) {
                        const diff = Math.abs(rowCouples[i + 1].num - rowCouples[i].num);
                        if (diff !== 1) {
                            showPosError('Hai ghế trong mỗi cặp ghế đôi bắt buộc phải nằm LIỀN KỀ NHAU trong cùng một hàng! Cặp ghế ' + rowCouples[i].code + ' và ' + rowCouples[i + 1].code + ' đang bị cách quãng ' + (diff - 1) + ' ghế, không hợp lệ.');
                            return false;
                        }
                    }
                }
            }
            return true;
        };

        const updateSummary = () => {
            if (!form) return;
            const selectedSeats = [...form.querySelectorAll('input[name="seatIds"]:checked')];
            const discountPercent = Number(form.querySelector('input[name="ticketClass"]:checked')?.dataset.discountPercent || 0);
            let baseTicketTotal = 0;
            let discountedTicketTotal = 0;

            const seatCodesList = selectedSeats.map(s => s.dataset.seatCode).join(', ');

            if (step2SeatCount) step2SeatCount.textContent = selectedSeats.length;
            if (step2SeatCodes) step2SeatCodes.textContent = seatCodesList || 'Chưa chọn ghế';
            if (seatCount) seatCount.textContent = selectedSeats.length;
            if (checkoutSeatCount) checkoutSeatCount.textContent = selectedSeats.length;

            if (selectedSeatList) selectedSeatList.replaceChildren();

            if (selectedSeats.length === 0) {
                if (selectedSeatList) {
                    const empty = document.createElement('span');
                    empty.className = 'summary-empty';
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
                        row.className = 'summary-detail-row';
                        row.style.display = 'flex';
                        row.style.justifyContent = 'space-between';
                        row.style.padding = '4px 0';
                        const seatName = document.createElement('span');
                        seatName.textContent = 'Ghế ' + seat.dataset.seatCode;
                        const seatPrice = document.createElement('strong');
                        seatPrice.textContent = formatMoney(price);
                        row.append(seatName, seatPrice);
                        selectedSeatList.appendChild(row);
                    }
                });
            }

            const quantities = [...document.querySelectorAll('[data-fnb-quantity]')];
            let items = 0;
            let fnbTotal = 0;
            if (selectedFnbList) selectedFnbList.replaceChildren();

            quantities.forEach(input => {
                const quantity = Math.max(0, Math.min(Number(input.max) || 0, Math.floor(Number(input.value) || 0)));
                items += quantity;
                const unitPrice = Number(input.dataset.price || 0);
                const lineTotal = quantity * unitPrice;
                fnbTotal += lineTotal;

                if (quantity > 0 && selectedFnbList) {
                    const row = document.createElement('div');
                    row.className = 'summary-detail-row';
                    row.style.display = 'flex';
                    row.style.justifyContent = 'space-between';
                    row.style.padding = '4px 0';
                    const itemName = document.createElement('span');
                    itemName.textContent = input.dataset.name + ' × ' + quantity;
                    const itemTotal = document.createElement('strong');
                    itemTotal.textContent = formatMoney(lineTotal);
                    row.append(itemName, itemTotal);
                    selectedFnbList.appendChild(row);
                }
            });

            if (items === 0 && selectedFnbList) {
                const empty = document.createElement('span');
                empty.className = 'summary-empty';
                empty.textContent = 'Chưa chọn bắp nước.';
                selectedFnbList.appendChild(empty);
            }

            if (fnbCount) fnbCount.textContent = items + ' phần';
            if (step3FnbCount) step3FnbCount.textContent = items;
            if (ticketBaseSubtotal) ticketBaseSubtotal.textContent = formatMoney(baseTicketTotal);
            if (ticketSubtotal) ticketSubtotal.textContent = formatMoney(discountedTicketTotal);
            if (ticketDiscount) {
                ticketDiscount.textContent = discountPercent + '%' + (discountPercent > 0
                    ? ' (giảm ' + formatMoney(baseTicketTotal - discountedTicketTotal) + ')'
                    : '');
            }
            if (fnbSubtotal) fnbSubtotal.textContent = formatMoney(fnbTotal);
            if (step3FnbSubtotal) step3FnbSubtotal.textContent = formatMoney(fnbTotal);
            const total = discountedTicketTotal + fnbTotal;
            if (grandTotal) grandTotal.textContent = formatMoney(total);

            const qrAmountText = document.getElementById('pos-qr-amount-text');
            if (qrAmountText) qrAmountText.textContent = formatMoney(total);

            const method = document.getElementById('pos-payment-method')?.value;
            if (method === 'VNPAY' || method === 'VNPAY_POS') {
                triggerPosVnpayRefresh();
            }
        };

        let vnpayTimer = null;
        function triggerPosVnpayRefresh() {
            clearTimeout(vnpayTimer);
            vnpayTimer = setTimeout(fetchPosVnpayUrl, 250);
        }

        async function fetchPosVnpayUrl() {
            const method = document.getElementById('pos-payment-method')?.value;
            if (method !== 'VNPAY' && method !== 'VNPAY_POS') return;
            const selectedSeats = form ? form.querySelectorAll('input[name="seatIds"]:checked') : [];
            if (selectedSeats.length === 0) return;

            const loadingElem = document.getElementById('pos-vnpay-loading');
            const contentElem = document.getElementById('pos-vnpay-content');
            const qrImage = document.getElementById('pos-vnpay-qr-image');
            const portalLink = document.getElementById('pos-vnpay-portal-link');

            if (loadingElem) loadingElem.style.display = 'block';
            if (contentElem) contentElem.style.opacity = '0.5';

            try {
                const formData = new FormData(form);
                formData.set('mode', 'vnpay_url');
                const searchParams = new URLSearchParams(formData);

                const resp = await fetch(form.action, {
                    method: 'POST',
                    headers: { 'Accept': 'application/json' },
                    body: searchParams
                });
                const res = await resp.json();
                if (res.success && res.qrUrl) {
                    if (qrImage) qrImage.src = res.qrUrl;
                    if (portalLink) portalLink.href = res.paymentUrl;
                }
            } catch (err) {
                console.warn('Lỗi tải VNPay URL cho POS:', err);
            } finally {
                if (loadingElem) loadingElem.style.display = 'none';
                if (contentElem) contentElem.style.opacity = '1';
            }
        }

        window.onPaymentMethodChange = () => {
            const method = document.getElementById('pos-payment-method')?.value;
            const qrContainer = document.getElementById('pos-vnpay-qr-container');
            const checkoutBtn = document.getElementById('btn-final-checkout');
            if (method === 'VNPAY_POS' || method === 'VNPAY') {
                if (qrContainer) qrContainer.style.display = 'block';
                if (checkoutBtn) checkoutBtn.textContent = 'Đã nhận tiền VNPAY & Xuất vé →';
                triggerPosVnpayRefresh();
            } else {
                if (qrContainer) qrContainer.style.display = 'none';
                if (checkoutBtn) checkoutBtn.textContent = 'Xác nhận thanh toán & Xuất vé →';
            }
        };

        if (form) {
            form.querySelectorAll('input[name="seatIds"]').forEach(input => {
                input.addEventListener('change', () => {
                    hidePosError();
                    updateSummary();
                });
            });

            form.querySelectorAll('[data-fnb-quantity]').forEach(input => {
                const updateQty = () => {
                    const max = Number(input.max) || 0;
                    input.value = Math.max(0, Math.min(max, Math.floor(Number(input.value) || 0)));
                    updateSummary();
                };
                input.addEventListener('change', updateQty);
                input.addEventListener('input', updateQty);
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

            document.querySelectorAll('.choice-card input').forEach(input => {
                input.addEventListener('change', () => {
                    document.querySelectorAll('.choice-card').forEach(card => card.classList.remove('is-selected'));
                    input.closest('.choice-card').classList.add('is-selected');
                    updateSummary();
                });
            });

            form.addEventListener('submit', async event => {
                const confirmationInput = form.querySelector('input[name="checkoutConfirmed"]');
                if (confirmationInput?.value === 'true') {
                    if (finalCheckoutBtn) {
                        finalCheckoutBtn.disabled = true;
                        finalCheckoutBtn.textContent = 'Đang tạo đơn & xuất vé…';
                    }
                    return;
                }
                event.preventDefault();

                const selectedSeats = form.querySelectorAll('input[name="seatIds"]:checked');
                if (selectedSeats.length === 0) {
                    showPosError('Vui lòng chọn ít nhất một ghế trước khi thanh toán.');
                    goToStep(2);
                    return;
                }
                if (!validateCoupleSeats(selectedSeats)) {
                    goToStep(2);
                    return;
                }

                // Lấy quote báo giá từ server
                const quotedTotalInput = form.querySelector('input[name="quotedTotal"]');
                const formData = new FormData(form);
                formData.set('mode', 'quote');
                try {
                    const response = await fetch(form.action, {
                        method: 'POST',
                        body: formData,
                        headers: { 'Accept': 'application/json' }
                    });
                    if (response.ok) {
                        const quote = await response.json();
                        if (quotedTotalInput) quotedTotalInput.value = Number(quote.grandTotal).toFixed(2);
                    }
                } catch (e) {
                    console.warn('Quote failed, proceed anyway:', e);
                }

                confirmationInput.value = 'true';
                if (finalCheckoutBtn) {
                    finalCheckoutBtn.disabled = true;
                    finalCheckoutBtn.textContent = 'Đang tạo đơn & xuất vé…';
                }
                form.submit();
            });

            updateSummary();
        }
    })();
</script>
</body>
</html>
