<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thêm suất chiếu - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .planner { padding: 0; overflow: hidden; }
        .planner-head { display: flex; justify-content: space-between; gap: 16px; align-items: center; padding: 22px 24px; border-bottom: 1px solid #e2e8f0; }
        .planner-head h2 { margin: 0; }
        .planner-body { padding: 20px 24px 28px; }
        .back-link { color: #e50914; font-weight: 700; text-decoration: none; }
        .alert { padding: 12px 16px; border-radius: 8px; margin-bottom: 16px; }
        .alert-danger { background: #fed7d7; color: #742a2a; }
        .choice-row { display: flex; flex-wrap: wrap; gap: 12px; align-items: end; margin-bottom: 18px; }
        label { display: block; font-size: 12px; color: #4a5568; margin-bottom: 4px; }
        .field, .field-readonly { width: 100%; padding: 8px 12px; border: 1px solid #cbd5e0; border-radius: 8px; background: #fff; }
        .field-readonly { background: #f8fafc; color: #0f172a; }
        .form-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 14px 16px; }
        .form-grid .wide { grid-column: 1 / -1; }
        @media (max-width: 800px) { .form-grid { grid-template-columns: 1fr; } }
        .panel { border: 1px solid #e2e8f0; border-radius: 12px; padding: 14px; margin-bottom: 16px; }
        .panel h3 { margin: 0 0 10px; font-size: 1rem; }
        .block { display: flex; justify-content: space-between; gap: 12px; padding: 10px 12px; border: 1px solid #e2e8f0; border-radius: 8px; margin-bottom: 8px; }
        .block.is-conflict { border-color: #e50914; background: #fff5f5; }
        .suggests { display: flex; flex-wrap: wrap; gap: 8px; }
        .suggest { border: 1px solid #e2e8f0; background: #fff; border-radius: 999px; padding: 8px 14px; font-weight: 700; cursor: pointer; }
        .suggest.is-selected { background: #e50914; color: #fff; border-color: #e50914; }
        .day-check { display: inline-flex; align-items: center; gap: 6px; border: 1px solid #e2e8f0; border-radius: 999px; padding: 8px 12px; font-weight: 700; background: #fff; }
        .chip { display: inline-flex; align-items: center; gap: 8px; border: 1px solid #e2e8f0; border-radius: 999px; padding: 6px 10px; background: #fff; font-weight: 700; }
        .chip button { border: 0; background: transparent; cursor: pointer; font-weight: 700; color: #b20710; }
        .preview-table { width: 100%; border-collapse: collapse; }
        .preview-table th, .preview-table td { text-align: left; padding: 8px 10px; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
        .row-busy { color: #b20710; font-weight: 700; }
        .summary { font-weight: 700; }
        .end-line { margin: 12px 0; color: #0f172a; }
        .conflict { min-height: 20px; color: #b20710; font-weight: 700; }
        .save-btn { height: 40px; padding: 0 18px; border: 0; border-radius: 8px; background: #e50914; color: #fff; font-weight: 700; cursor: pointer; }
        .save-btn:disabled { background: #cbd5e0; cursor: not-allowed; }
        .hint { color: #64748b; margin-top: 0; }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <div class="admin-content">
            <section class="admin-card planner">
                <div class="planner-head">
                    <h2>Thêm suất chiếu</h2>
                    <a class="back-link" href="${pageContext.request.contextPath}${listPath}?cinemaId=${selectedCinemaId}">Quay lại danh sách</a>
                </div>
                <div class="planner-body">
                    <c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>

                            <c:set var="cinemaName" value="" />
                            <c:forEach var="cinema" items="${cinemas}">
                                <c:if test="${cinema.id == selectedCinemaId}"><c:set var="cinemaName" value="${cinema.name}" /></c:if>
                            </c:forEach>
                            <div class="panel">
                                <h3>Phim</h3>
                                <div class="form-grid">
                                    <div class="wide">
                                        <label>Tên phim</label>
                                        <input class="field-readonly" value="${selectedMovie.title}" readonly />
                                    </div>
                                    <div>
                                        <label>Thời lượng</label>
                                        <input class="field-readonly" value="${selectedMovie.durationMinutes} phút" readonly />
                                    </div>
                                    <div>
                                        <label>Độ tuổi</label>
                                        <input class="field-readonly" value="${empty selectedMovie.ageRating ? 'Đang cập nhật' : selectedMovie.ageRating}" readonly />
                                    </div>
                                    <div>
                                        <label>Ngôn ngữ</label>
                                        <input class="field-readonly" value="${empty selectedMovie.language ? 'Đang cập nhật' : selectedMovie.language}" readonly />
                                    </div>
                                    <div>
                                        <label>Ngày khởi chiếu</label>
                                        <input class="field-readonly" value="${empty selectedMovie.releaseDate ? 'Đang cập nhật' : selectedMovie.releaseDate}" readonly />
                                    </div>
                                </div>
                            </div>

                            <form method="get" action="${pageContext.request.contextPath}${listPath}/create" class="choice-row">
                                <input type="hidden" name="movieId" value="${selectedMovie.id}" />
                                <c:if test="${actor.role.roleName != 'ADMIN'}">
                                    <input type="hidden" name="cinemaId" value="${selectedCinemaId}" />
                                </c:if>
                                <div>
                                    <label>Rạp</label>
                                    <c:choose>
                                        <c:when test="${actor.role.roleName == 'ADMIN'}">
                                            <select class="field" name="cinemaId" onchange="this.form.submit()">
                                                <c:forEach var="cinema" items="${cinemas}">
                                                    <option value="${cinema.id}" ${selectedCinemaId == cinema.id ? 'selected' : ''}>${cinema.name}</option>
                                                </c:forEach>
                                            </select>
                                        </c:when>
                                        <c:otherwise>
                                            <input class="field-readonly" value="${cinemaName}" readonly />
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                                <div>
                                    <label>Phòng chiếu</label>
                                    <select class="field" name="screeningRoomId" onchange="this.form.submit()">
                                        <c:forEach var="room" items="${rooms}">
                                            <option value="${room.id}" ${selectedRoomId == room.id ? 'selected' : ''}>${room.name}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div>
                                    <label>Ngày bắt đầu</label>
                                    <input class="field" type="date" name="date" value="${selectedDate}" onchange="this.form.submit()" />
                                </div>
                                <div>
                                    <label>Lặp đến ngày</label>
                                    <input class="field" type="date" name="until" value="${untilDate}" onchange="this.form.submit()" />
                                </div>
                                <div class="wide" style="flex: 1 1 100%;">
                                    <label>Ngày trong tuần</label>
                                    <div class="suggests">
                                        <c:forEach var="day" items="${weekDays}">
                                            <label class="day-check">
                                                <input type="checkbox" name="dow" value="${day.value}" ${day.selected ? 'checked' : ''} onchange="this.form.submit()" />
                                                ${day.label}
                                            </label>
                                        </c:forEach>
                                    </div>
                                </div>
                                <p class="hint" style="flex: 1 1 100%;">${repeatSummary}</p>
                                <c:forEach var="chosen" items="${chosenTimes}">
                                    <input type="hidden" name="time" value="${chosen.clock}" />
                                </c:forEach>
                                <div class="wide" style="flex: 1 1 100%;">
                                    <label>Giờ gợi ý ngày ${suggestDateLabel}</label>
                                    <p class="hint">Bấm để thêm hoặc bỏ. Tối đa 8 giờ. Giờ kết thúc = giờ bắt đầu + thời lượng phim + ${bufferMinutes} phút dọn phòng.</p>
                                    <div class="suggests">
                                        <c:forEach var="option" items="${startOptions}">
                                            <c:set var="picked" value="false" />
                                            <c:forEach var="chosen" items="${chosenTimes}">
                                                <c:if test="${chosen.clock == option.label}"><c:set var="picked" value="true" /></c:if>
                                            </c:forEach>
                                            <button class="suggest ${picked == 'true' ? 'is-selected' : ''}" type="submit" name="toggleTime" value="${option.label}">${option.label}</button>
                                        </c:forEach>
                                        <c:if test="${empty startOptions}"><span class="hint">Không còn khung trống đủ cho phim này ở các ngày đã chọn.</span></c:if>
                                    </div>
                                </div>
                                <div class="wide" style="flex: 1 1 100%;">
                                    <label>Giờ đã chọn</label>
                                    <div class="suggests">
                                        <c:forEach var="chosen" items="${chosenTimes}">
                                            <span class="chip">${chosen.clock} → ${chosen.endClock}
                                                <button type="submit" name="toggleTime" value="${chosen.clock}" title="Bỏ giờ">×</button>
                                            </span>
                                        </c:forEach>
                                        <c:if test="${empty chosenTimes}"><span class="hint">Chưa chọn giờ.</span></c:if>
                                    </div>
                                </div>
                                <div>
                                    <label>Thêm giờ khác</label>
                                    <input class="field" type="time" name="addTime" step="60" />
                                </div>
                                <div>
                                    <label>&nbsp;</label>
                                    <button class="save-btn" type="submit" name="addClock" value="1">Thêm giờ</button>
                                </div>
                            </form>

                            <div class="panel">
                                <h3>Lịch phòng ngày bắt đầu</h3>
                                <c:choose>
                                    <c:when test="${empty roomBlocks}"><p class="hint">Phòng còn trống cả ngày.</p></c:when>
                                    <c:otherwise>
                                        <c:forEach var="block" items="${roomBlocks}">
                                            <div class="block" data-start="${block.startValue}" data-end="${block.endValue}" data-label="${fn:escapeXml(block.label)} · ${block.clock}">
                                                <span>${block.label}</span>
                                                <strong>${block.clock}</strong>
                                            </div>
                                        </c:forEach>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <form id="createForm" method="post" action="${pageContext.request.contextPath}${listPath}/create">
                                <input type="hidden" name="view" value="page" />
                                <input type="hidden" name="cinemaId" value="${selectedCinemaId}" />
                                <input type="hidden" name="movieId" value="${selectedMovie.id}" />
                                <input type="hidden" name="screeningRoomId" value="${selectedRoomId}" />
                                <input type="hidden" name="date" value="${selectedDate}" />
                                <input type="hidden" name="until" value="${untilDate}" />
                                <c:forEach var="day" items="${weekDays}">
                                    <c:if test="${day.selected}"><input type="hidden" name="dow" value="${day.value}" /></c:if>
                                </c:forEach>
                                <c:forEach var="chosen" items="${chosenTimes}">
                                    <input type="hidden" name="time" value="${chosen.clock}" />
                                </c:forEach>
                                <div class="panel">
                                    <h3>Suất sẽ tạo</h3>
                                    <div class="form-grid">
                                        <div>
                                            <label>Loại phòng</label>
                                            <input class="field-readonly" readonly value="${selectedRoom.roomType == 'IMAX_3D' ? 'IMAX' : (selectedRoom.roomType == 'VIP_LUXURY' ? 'VIP' : '2D tiêu chuẩn')}" />
                                        </div>
                                        <div>
                                            <label>Định dạng</label>
                                            <select class="field" name="experienceFormat">
                                                <c:forEach var="format" items="${formats}">
                                                    <option value="${format}">${format}</option>
                                                </c:forEach>
                                            </select>
                                        </div>
                                        <div>
                                            <label>Dọn phòng</label>
                                            <input class="field-readonly" value="${bufferMinutes} phút" readonly />
                                        </div>
                                        <div>
                                            <label>Trạng thái</label>
                                            <input class="field-readonly" value="Mở bán" readonly />
                                        </div>
                                    </div>
                                    <c:choose>
                                        <c:when test="${empty chosenTimes}">
                                            <p class="hint" style="margin-top: 14px;">Chọn ít nhất một giờ bắt đầu.</p>
                                        </c:when>
                                        <c:otherwise>
                                            <c:if test="${previewTruncated}"><p class="hint" style="margin-top: 14px;">Chỉ hiện ${fn:length(previewRows)} suất đầu. Thu hẹp ngày hoặc bớt giờ.</p></c:if>
                                            <table class="preview-table" style="margin-top: 14px;">
                                                <thead>
                                                    <tr><th></th><th>Ngày</th><th>Giờ</th><th>Kết thúc</th><th>Kết quả</th></tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="row" items="${previewRows}">
                                                        <tr>
                                                            <td>
                                                                <c:if test="${row.free}"><input type="checkbox" name="slot" value="${row.value}" checked /></c:if>
                                                            </td>
                                                            <td>${row.dateLabel}</td>
                                                            <td>${row.startLabel}</td>
                                                            <td>${row.endLabel}</td>
                                                            <td class="${row.free ? '' : 'row-busy'}">${row.result}</td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                            <p class="summary" id="summary" data-blocked="${blockedCount}">Sẽ tạo ${freeCount} suất. ${blockedCount} suất bị trùng, không lưu.</p>
                                        </c:otherwise>
                                    </c:choose>
                                    <button class="save-btn" id="saveBtn" type="submit" ${freeCount == 0 ? 'disabled' : ''}>Tạo các suất đã chọn</button>
                                </div>
                            </form>
                </div>
            </section>
        </div>
    </div>
</div>
<script>
    const boxes = Array.from(document.querySelectorAll('input[name="slot"]'));
    const saveBtn = document.getElementById('saveBtn');
    const summary = document.getElementById('summary');
    const blocked = summary ? summary.dataset.blocked : '0';
    function sync() {
        const selected = boxes.filter((box) => box.checked).length;
        if (saveBtn) saveBtn.disabled = selected === 0;
        if (summary) summary.textContent = 'Sẽ tạo ' + selected + ' suất. ' + blocked + ' suất bị trùng, không lưu.';
    }
    boxes.forEach((box) => box.addEventListener('change', sync));
    if (boxes.length) sync();
</script>
</body>
</html>
