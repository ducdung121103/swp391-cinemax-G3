<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tạo lịch chiếu - Cinema Admin</title>
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
        .movie-pick { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: 12px; }
        .movie-pick a { display: block; padding: 14px 16px; border: 1px solid #e2e8f0; border-radius: 12px; text-decoration: none; color: #0f172a; background: #fff; }
        .movie-pick a:hover { border-color: #e50914; }
        .movie-pick small { display: block; margin-top: 4px; color: #64748b; }
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
                    <h2>Tạo lịch chiếu</h2>
                    <a class="back-link" href="${pageContext.request.contextPath}${listPath}?cinemaId=${selectedCinemaId}">Quay lại danh sách</a>
                </div>
                <div class="planner-body">
                    <c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>

                    <c:if test="${actor.role.roleName == 'ADMIN' && empty selectedMovie}">
                        <form method="get" action="${pageContext.request.contextPath}${listPath}/create" class="choice-row">
                            <input type="hidden" name="movieId" value="${selectedMovie.id}" />
                            <div>
                                <label>Rạp</label>
                                <select class="field" name="cinemaId" onchange="this.form.submit()">
                                    <c:forEach var="cinema" items="${cinemas}">
                                        <option value="${cinema.id}" ${selectedCinemaId == cinema.id ? 'selected' : ''}>${cinema.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </form>
                    </c:if>

                    <c:choose>
                        <c:when test="${empty selectedMovie}">
                            <p class="hint">Chọn phim mới chưa có suất ở rạp này.</p>
                            <c:choose>
                                <c:when test="${empty newMovies}">
                                    <p class="hint">Mọi phim trong danh sách đã có lịch chiếu.</p>
                                </c:when>
                                <c:otherwise>
                                    <div class="movie-pick">
                                        <c:forEach var="movie" items="${newMovies}">
                                            <a href="${pageContext.request.contextPath}${listPath}/create?movieId=${movie.id}&amp;cinemaId=${selectedCinemaId}">
                                                ${movie.title}
                                                <small>${movie.durationMinutes} phút</small>
                                            </a>
                                        </c:forEach>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:when>
                        <c:otherwise>
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
                                <p class="hint" style="margin-top: 12px;"><a href="${pageContext.request.contextPath}${listPath}/create?cinemaId=${selectedCinemaId}">Đổi phim</a></p>
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
                                    <label>Ngày chiếu</label>
                                    <input class="field" type="date" name="date" value="${selectedDate}" onchange="this.form.submit()" />
                                </div>
                            </form>

                            <div class="panel">
                                <h3>Lịch phòng trong ngày</h3>
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
                                <div class="panel">
                                    <h3>Suất chiếu</h3>
                                    <p class="hint">Giờ gợi ý trong khung 08:00–23:30. Giờ kết thúc = giờ bắt đầu + thời lượng phim + ${bufferMinutes} phút dọn phòng.</p>
                                    <div class="suggests">
                                        <c:forEach var="option" items="${startOptions}">
                                            <button class="suggest" type="button" data-value="${option.value}">${option.label}</button>
                                        </c:forEach>
                                        <c:if test="${empty startOptions}"><span class="hint">Không còn khung trống đủ cho phim này.</span></c:if>
                                    </div>
                                    <div class="form-grid" style="margin-top: 14px;">
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
                                            <label>Giờ bắt đầu</label>
                                            <input class="field" id="startClock" type="time" step="60" required />
                                            <input type="hidden" id="startTime" name="startTime" />
                                        </div>
                                        <div>
                                            <label>Giờ kết thúc</label>
                                            <input class="field-readonly" id="endTime" value="" readonly placeholder="Tự tính khi chọn giờ bắt đầu" />
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
                                    <p class="conflict" id="conflict"></p>
                                    <button class="save-btn" id="saveBtn" type="submit" disabled>Tạo lịch chiếu</button>
                                </div>
                            </form>
                        </c:otherwise>
                    </c:choose>
                </div>
            </section>
        </div>
    </div>
</div>
<script>
        const neededMinutes = ${empty neededMinutes ? 0 : neededMinutes};
        const selectedDate = '${selectedDate}';
        const clockInput = document.getElementById('startClock');
        const startInput = document.getElementById('startTime');
        if (clockInput && startInput) {
            const endTime = document.getElementById('endTime');
            const conflict = document.getElementById('conflict');
            const saveBtn = document.getElementById('saveBtn');
            const form = document.getElementById('createForm');
            const blocks = Array.from(document.querySelectorAll('.block'));
            const buttons = Array.from(document.querySelectorAll('.suggest'));
            const pad = (value) => String(value).padStart(2, '0');
            const format = (date) => pad(date.getHours()) + ':' + pad(date.getMinutes());

            function refresh() {
                const value = clockInput.value ? selectedDate + 'T' + clockInput.value : '';
                startInput.value = value;
                blocks.forEach((block) => block.classList.remove('is-conflict'));
                buttons.forEach((button) => button.classList.toggle('is-selected', button.dataset.value === value));
                if (!value) {
                    endTime.value = '';
                    conflict.textContent = '';
                    saveBtn.disabled = true;
                    return;
                }
                const start = new Date(value);
                const end = new Date(start.getTime() + neededMinutes * 60000);
                endTime.value = format(end);
                const hit = blocks.find((block) => start.getTime() < new Date(block.dataset.end).getTime()
                    && end.getTime() > new Date(block.dataset.start).getTime());
                if (hit) {
                    hit.classList.add('is-conflict');
                    conflict.textContent = 'Trùng ' + hit.dataset.label;
                    saveBtn.disabled = true;
                } else {
                    conflict.textContent = '';
                    saveBtn.disabled = false;
                }
            }

            buttons.forEach((button) => button.addEventListener('click', () => {
                clockInput.value = button.dataset.value.substring(11, 16);
                refresh();
            }));
            clockInput.addEventListener('input', refresh);
            form.addEventListener('submit', (event) => {
                refresh();
                if (saveBtn.disabled) event.preventDefault();
            });
            refresh();
        }
</script>
</body>
</html>
