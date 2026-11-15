<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Bảng giá vé - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .pricing-board { padding: 0; overflow: hidden; }
        .pricing-head {
            display: flex; justify-content: space-between; align-items: flex-start; gap: 16px; flex-wrap: wrap;
            padding: 22px 24px; border-bottom: 1px solid #e2e8f0;
        }
        .pricing-head h2 { margin: 0; font-size: 1.25rem; color: #1a202c; }
        .pricing-head p { margin: 6px 0 0; color: #718096; font-size: 13px; }
        .pricing-body { padding: 20px 24px 28px; }
        .alert {
            padding: 12px 16px; border-radius: 8px; margin-bottom: 16px;
            display: flex; align-items: center; gap: 10px; font-size: 14px;
        }
        .alert-success { background: #c6f6d5; color: #22543d; border: 1px solid #9ae6b4; }
        .alert-danger { background: #fed7d7; color: #742a2a; border: 1px solid #feb2b2; }
        .panel {
            border: 1px solid #e2e8f0; border-radius: 12px; background: #fff;
            padding: 16px; margin-bottom: 16px;
        }
        .panel h3 { margin: 0 0 12px; font-size: 1rem; color: #1a202c; }
        .panel-hint { margin: -4px 0 14px; color: #64748b; font-size: 13px; }
        .form-row {
            display: flex; flex-wrap: wrap; gap: 12px; align-items: end;
        }
        .form-row > div { min-width: 140px; }
        .form-row .grow { flex: 1 1 160px; }
        label { display: block; font-size: 12px; color: #4a5568; margin-bottom: 4px; }
        .field {
            width: 100%; padding: 8px 12px; border: 1px solid #cbd5e0; border-radius: 8px;
            background: #fff; box-sizing: border-box;
        }
        .save-btn {
            height: 38px; padding: 0 14px; border: 0; border-radius: 8px;
            background: #14141e; color: #fff; font-weight: 700; cursor: pointer; white-space: nowrap;
        }
        .save-btn:hover { background: #e50914; }
        .add-btn {
            height: 38px; padding: 0 16px; border: 0; border-radius: 8px;
            background: #e50914; color: #fff; font-weight: 700; cursor: pointer; white-space: nowrap;
        }
        .add-btn:hover { background: #b20710; }
        .delete-btn {
            height: 38px; padding: 0 14px; border: 1px solid #e50914; border-radius: 8px;
            background: #fff; color: #e50914; font-weight: 700; cursor: pointer; white-space: nowrap;
        }
        .delete-btn:hover { background: #fff5f5; }
        .filter-bar { display: flex; gap: 12px; flex-wrap: wrap; align-items: end; margin-bottom: 14px; }
        .filter-bar .field { min-width: 220px; width: auto; }
        .table-wrap { overflow-x: auto; border: 1px solid #e2e8f0; border-radius: 12px; background: #fff; }
        table { width: 100%; border-collapse: collapse; }
        th, td {
            padding: 12px 10px; border-bottom: 1px solid #e2e8f0;
            text-align: left; vertical-align: middle; font-size: 13.5px;
        }
        th { background: #f8fafc; font-weight: 700; color: #4a5568; }
        tbody tr:last-child td { border-bottom: 0; }
        td form { display: flex; gap: 8px; align-items: center; flex-wrap: wrap; margin: 0; }
        .price-input { width: 120px; padding: 8px 10px; border: 1px solid #cbd5e0; border-radius: 8px; }
        .price-readonly { font-weight: 700; color: #1a202c; }
        .empty-row { padding: 18px; text-align: center; color: #64748b; }
        .chip-chain {
            display: inline-block; padding: 3px 8px; border-radius: 999px;
            background: #edf2f7; color: #4a5568; font-size: 12px; font-weight: 700;
        }
        @media (max-width: 900px) {
            .pricing-head { flex-direction: column; }
            .form-row > div { flex: 1 1 140px; }
        }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <div class="admin-content">
            <section class="admin-card pricing-board">
                <div class="pricing-head">
                    <div>
                        <h2>Bảng giá vé</h2>
                        <p>Cấu hình phần trăm giảm đối tượng và dòng giá theo rạp, ngày, khung giờ, định dạng ghế.</p>
                    </div>
                </div>
                <div class="pricing-body">
                    <c:if test="${msg == 'save' || msg == 'delete' || msg == 'discount'}">
                        <div class="alert alert-success"><i class="fa-solid fa-check-circle"></i> Đã lưu thay đổi.</div>
                    </c:if>
                    <c:if test="${error == 'FORBIDDEN'}">
                        <div class="alert alert-danger"><i class="fa-solid fa-triangle-exclamation"></i> Không đủ quyền: chỉ Admin đổi % giảm; Manager chỉ sửa giá rạp mình, không sửa giá toàn chuỗi.</div>
                    </c:if>
                    <c:if test="${error == 'INVALID'}">
                        <div class="alert alert-danger"><i class="fa-solid fa-triangle-exclamation"></i> Dữ liệu giá không hợp lệ.</div>
                    </c:if>
                    <c:if test="${not empty error && error != 'FORBIDDEN' && error != 'INVALID'}">
                        <div class="alert alert-danger"><i class="fa-solid fa-triangle-exclamation"></i> Không lưu được.</div>
                    </c:if>

                    <div class="panel">
                        <h3>Giảm giá đối tượng</h3>
                        <p class="panel-hint">
                            Áp dụng khi khách chọn loại vé HSSV hoặc người cao tuổi lúc đặt chỗ.
                            <c:if test="${actor.role.roleName != 'ADMIN'}"> Chỉ Admin được thay đổi phần trăm này.</c:if>
                        </p>
                        <c:choose>
                            <c:when test="${actor.role.roleName == 'ADMIN'}">
                                <form method="post" action="${pageContext.request.contextPath}/admin/catalog/pricing" class="form-row">
                                    <input type="hidden" name="action" value="discount" />
                                    <div>
                                        <label>HSSV (%)</label>
                                        <input class="field" type="number" name="studentPercent" min="0" max="100" step="1" value="${studentDiscount}" required />
                                    </div>
                                    <div>
                                        <label>Người cao tuổi (%)</label>
                                        <input class="field" type="number" name="seniorPercent" min="0" max="100" step="1" value="${seniorDiscount}" required />
                                    </div>
                                    <button class="save-btn" type="submit">Lưu phần trăm</button>
                                </form>
                            </c:when>
                            <c:otherwise>
                                <div class="form-row">
                                    <div>
                                        <label>HSSV (%)</label>
                                        <input class="field" type="number" value="${studentDiscount}" readonly />
                                    </div>
                                    <div>
                                        <label>Người cao tuổi (%)</label>
                                        <input class="field" type="number" value="${seniorDiscount}" readonly />
                                    </div>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <div class="panel">
                        <h3>Thêm dòng giá</h3>
                        <p class="panel-hint">Mỗi dòng là một mức giá gốc theo tổ hợp rạp · ngày · khung giờ · định dạng · loại ghế.</p>
                        <form method="post" action="${pageContext.request.contextPath}/admin/catalog/pricing" class="form-row">
                            <input type="hidden" name="action" value="save" />
                            <c:choose>
                                <c:when test="${actor.role.roleName == 'ADMIN'}">
                                    <div class="grow">
                                        <label>Rạp</label>
                                        <select class="field" name="cinemaId">
                                            <option value="">Toàn chuỗi</option>
                                            <c:forEach var="cinema" items="${cinemas}">
                                                <option value="${cinema.id}">${cinema.name}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <input type="hidden" name="cinemaId" value="${actor.cinemaId}" />
                                </c:otherwise>
                            </c:choose>
                            <div>
                                <label>Ngày</label>
                                <select class="field" name="dayType">
                                    <option value="WEEKDAY">Ngày thường</option>
                                    <option value="WEEKEND">Cuối tuần</option>
                                    <option value="HOLIDAY">Ngày lễ</option>
                                </select>
                            </div>
                            <div>
                                <label>Khung giờ</label>
                                <select class="field" name="timeSlot">
                                    <option value="EARLY">Sớm</option>
                                    <option value="STANDARD">Tiêu chuẩn</option>
                                    <option value="PRIME">Cao điểm</option>
                                </select>
                            </div>
                            <div>
                                <label>Định dạng</label>
                                <select class="field" name="experienceFormat">
                                    <option value="2D">2D</option>
                                    <option value="3D">3D</option>
                                    <option value="IMAX">IMAX</option>
                                </select>
                            </div>
                            <div class="grow">
                                <label>Loại ghế</label>
                                <select class="field" name="seatTypeId">
                                    <c:forEach var="seatType" items="${seatTypes}">
                                        <option value="${seatType.id}">${seatType.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                            <div>
                                <label>Giá gốc (₫)</label>
                                <input class="field money-input" type="text" name="basePrice" inputmode="numeric"
                                       required placeholder="ví dụ 50,000" autocomplete="off" />
                            </div>
                            <div>
                                <label>Từ ngày</label>
                                <input class="field" type="date" name="effectiveFrom" required />
                            </div>
                            <div>
                                <label>Đến ngày</label>
                                <input class="field" type="date" name="effectiveTo" />
                            </div>
                            <button class="add-btn" type="submit"><i class="fa-solid fa-plus"></i> Thêm</button>
                        </form>
                    </div>

                    <c:if test="${actor.role.roleName == 'ADMIN'}">
                        <form method="get" action="${pageContext.request.contextPath}/admin/catalog/pricing" class="filter-bar">
                            <div>
                                <label>Lọc theo rạp</label>
                                <select class="field" name="cinemaId" onchange="this.form.submit()">
                                    <option value="" ${empty selectedCinemaFilter ? 'selected' : ''}>Tất cả</option>
                                    <option value="chain" ${selectedCinemaFilter == 'chain' ? 'selected' : ''}>Toàn chuỗi</option>
                                    <c:forEach var="cinema" items="${cinemas}">
                                        <option value="${cinema.id}" ${selectedCinemaFilter == cinema.id.toString() ? 'selected' : ''}>${cinema.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </form>
                    </c:if>

                    <div class="table-wrap">
                        <table>
                            <thead>
                            <tr>
                                <th>Rạp</th>
                                <th>Ngày</th>
                                <th>Khung giờ</th>
                                <th>Định dạng</th>
                                <th>Loại ghế</th>
                                <th>Giá</th>
                                <th>Hiệu lực</th>
                                <th style="width: 88px;"></th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${empty prices}">
                                    <tr>
                                        <td colspan="8" class="empty-row">Chưa có dòng giá phù hợp bộ lọc.</td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="price" items="${prices}">
                                        <c:set var="ownRow" value="${actor.role.roleName == 'ADMIN' || (not empty price.cinemaId && price.cinemaId == actor.cinemaId)}" />
                                        <tr>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${empty price.cinemaName}"><span class="chip-chain">Toàn chuỗi</span></c:when>
                                                    <c:otherwise>${price.cinemaName}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${price.dayType == 'WEEKDAY'}">Ngày thường</c:when>
                                                    <c:when test="${price.dayType == 'WEEKEND'}">Cuối tuần</c:when>
                                                    <c:when test="${price.dayType == 'HOLIDAY'}">Ngày lễ</c:when>
                                                    <c:otherwise>${price.dayType}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${price.timeSlot == 'EARLY'}">Sớm</c:when>
                                                    <c:when test="${price.timeSlot == 'STANDARD'}">Tiêu chuẩn</c:when>
                                                    <c:when test="${price.timeSlot == 'PRIME'}">Cao điểm</c:when>
                                                    <c:otherwise>${price.timeSlot}</c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>${price.experienceFormat}</td>
                                            <td>${price.seatTypeName}</td>
                                            <td>
                                                <c:if test="${ownRow}">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/catalog/pricing">
                                                        <input type="hidden" name="action" value="save" />
                                                        <input type="hidden" name="id" value="${price.id}" />
                                                        <input type="hidden" name="cinemaId" value="${price.cinemaId}" />
                                                        <input type="hidden" name="dayType" value="${price.dayType}" />
                                                        <input type="hidden" name="timeSlot" value="${price.timeSlot}" />
                                                        <input type="hidden" name="experienceFormat" value="${price.experienceFormat}" />
                                                        <input type="hidden" name="seatTypeId" value="${price.seatTypeId}" />
                                                        <input type="hidden" name="effectiveFrom" value="${price.effectiveFrom}" />
                                                        <input type="hidden" name="effectiveTo" value="${price.effectiveTo}" />
                                                        <input class="price-input money-input" type="text" name="basePrice" inputmode="numeric"
                                                               required autocomplete="off"
                                                               value="<fmt:formatNumber value='${price.basePrice}' pattern='#,##0' />" />
                                                        <button class="save-btn" type="submit">Lưu</button>
                                                    </form>
                                                </c:if>
                                                <c:if test="${!ownRow}">
                                                    <span class="price-readonly"><fmt:formatNumber value="${price.basePrice}" pattern="#,##0" /> ₫</span>
                                                </c:if>
                                            </td>
                                            <td>${price.effectiveFrom} → ${empty price.effectiveTo ? 'không hạn' : price.effectiveTo}</td>
                                            <td>
                                                <c:if test="${ownRow}">
                                                    <form method="post" action="${pageContext.request.contextPath}/admin/catalog/pricing"
                                                          onsubmit="return confirm('Xóa dòng giá này?');">
                                                        <input type="hidden" name="action" value="delete" />
                                                        <input type="hidden" name="id" value="${price.id}" />
                                                        <button class="delete-btn" type="submit">Xóa</button>
                                                    </form>
                                                </c:if>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        </div>
    </div>
</div>
<script>
(function () {
    function digitsOnly(value) {
        return String(value || '').replace(/\D/g, '');
    }
    function formatMoney(value) {
        var digits = digitsOnly(value);
        if (!digits) return '';
        return digits.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
    }
    function bindMoneyInput(input) {
        input.addEventListener('input', function () {
            var start = input.selectionStart;
            var before = input.value.length;
            input.value = formatMoney(input.value);
            var after = input.value.length;
            var next = Math.max(0, start + (after - before));
            try { input.setSelectionRange(next, next); } catch (e) { /* ignore */ }
        });
        input.addEventListener('blur', function () {
            input.value = formatMoney(input.value);
        });
        if (input.value) {
            input.value = formatMoney(input.value);
        }
    }
    document.querySelectorAll('.money-input').forEach(bindMoneyInput);
    document.querySelectorAll('form').forEach(function (form) {
        form.addEventListener('submit', function () {
            form.querySelectorAll('.money-input').forEach(function (input) {
                input.value = digitsOnly(input.value);
            });
        });
    });
})();
</script>
</body>
</html>
