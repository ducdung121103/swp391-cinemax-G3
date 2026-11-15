<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Lịch chiếu - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .schedule-board { padding: 0; }
        .schedule-head {
            display: flex; justify-content: space-between; align-items: center; gap: 16px; flex-wrap: wrap;
            padding: 22px 24px; border-bottom: 1px solid #e2e8f0;
        }
        .schedule-head h2 { margin: 0; font-size: 1.25rem; }
        .create-btn {
            display: inline-flex; align-items: center; gap: 10px;
            padding: 10px 16px 10px 10px; border-radius: 999px;
            background: #fff; color: #e50914; text-decoration: none; font-weight: 700;
            border: 1px solid #f3c3c6; box-shadow: 0 6px 16px rgba(229, 9, 20, 0.12);
        }
        .create-btn:hover { background: #fff5f5; color: #b20710; }
        .create-plus {
            width: 34px; height: 34px; border-radius: 50%;
            display: inline-flex; align-items: center; justify-content: center;
            background: #e50914; color: #fff; font-size: 22px; line-height: 1;
            box-shadow: inset 0 0 0 1px rgba(255,255,255,.25);
        }
        .schedule-body { padding: 20px 24px 28px; }
        .alert { padding: 12px 16px; border-radius: 8px; margin-bottom: 16px; }
        .alert-success { background: #c6f6d5; color: #22543d; }
        .alert-danger { background: #fed7d7; color: #742a2a; }
        .filter-bar {
            display: flex; flex-wrap: wrap; gap: 12px; align-items: end;
            margin-bottom: 18px; padding: 14px; border: 1px solid #e2e8f0;
            border-radius: 12px; background: #f8fafc;
        }
        .filter-bar .filter-field { flex: 1 1 140px; min-width: 120px; }
        .filter-bar .filter-actions {
            display: flex; gap: 8px; flex: 0 0 auto; align-items: center;
        }
        .filter-select, .slot-input, .filter-date {
            padding: 8px 12px; border: 1px solid #cbd5e0; border-radius: 8px; background: #fff;
            width: 100%; box-sizing: border-box;
        }
        .filter-apply {
            height: 38px; padding: 0 14px; border-radius: 8px; font-weight: 700; cursor: pointer;
            border: 0; background: #14141e; color: #fff;
        }
        .filter-apply:hover { background: #e50914; }
        .filter-empty {
            padding: 18px; border: 1px dashed #cbd5e0; border-radius: 12px; color: #64748b;
            margin-bottom: 8px;
        }
        label { display: block; font-size: 12px; color: #4a5568; margin-bottom: 4px; }
        .movie-frame {
            border: 1px solid #e2e8f0; border-radius: 12px; background: #fff;
            margin-bottom: 16px; overflow: hidden;
        }
        .movie-head {
            display: flex; align-items: center; gap: 14px;
            padding: 14px 16px; background: #f8fafc; border-bottom: 1px solid #e2e8f0;
        }
        .add-slot {
            margin-left: auto; padding: 8px 14px; border-radius: 999px;
            border: 1px solid #f3c3c6; background: #fff; color: #e50914;
            font-weight: 700; text-decoration: none; white-space: nowrap;
        }
        .add-slot:hover { background: #fff5f5; }
        .stt {
            width: 36px; height: 36px; border-radius: 10px;
            display: inline-flex; align-items: center; justify-content: center;
            background: #14141e; color: #fff; font-weight: 700; flex-shrink: 0;
        }
        .movie-head h3 { margin: 0 0 4px; font-size: 1rem; }
        .movie-meta { margin: 0; color: #64748b; font-size: 13px; }
        .slot-list { padding: 12px 16px 16px; display: flex; flex-direction: column; gap: 10px; }
        .slot-form {
            display: flex; flex-wrap: wrap; gap: 10px; align-items: end;
            padding: 12px; border: 1px solid #e2e8f0; border-radius: 10px; background: #fff;
        }
        .slot-form.is-cancelled {
            background: #fff8f8; border-color: #fecaca; opacity: 0.95;
        }
        .slot-form.is-finished {
            background: #f8fafc; border-color: #e2e8f0;
        }
        .slot-form > div { flex: 1 1 140px; min-width: 0; }
        .slot-status-wrap { flex: 0 0 120px !important; }
        .slot-input { width: 100%; }
        .slot-input:disabled { background: #f1f5f9; color: #64748b; cursor: not-allowed; }
        .status-badge {
            display: inline-flex; align-items: center; justify-content: center;
            height: 38px; padding: 0 12px; border-radius: 999px;
            font-size: 12px; font-weight: 700; white-space: nowrap; width: 100%;
            box-sizing: border-box;
        }
        .status-scheduled { background: #ecfdf5; color: #047857; border: 1px solid #a7f3d0; }
        .status-opening { background: #eff6ff; color: #1d4ed8; border: 1px solid #bfdbfe; }
        .status-finished { background: #f1f5f9; color: #475569; border: 1px solid #cbd5e1; }
        .status-cancelled { background: #fef2f2; color: #b91c1c; border: 1px solid #fecaca; }
        .status-other { background: #f8fafc; color: #64748b; border: 1px solid #e2e8f0; }
        .save-btn {
            height: 38px; padding: 0 14px; border: 0; border-radius: 8px;
            background: #14141e; color: #fff; font-weight: 700; cursor: pointer;
        }
        .save-btn:hover { background: #e50914; }
        .save-btn:disabled, .delete-btn:disabled {
            opacity: 0.45; cursor: not-allowed;
        }
        .save-btn:disabled:hover { background: #14141e; }
        .delete-btn {
            height: 38px; padding: 0 14px; border: 1px solid #e50914; border-radius: 8px;
            background: #fff; color: #e50914; font-weight: 700; cursor: pointer;
        }
        .delete-btn:hover { background: #fff5f5; }
        .delete-btn:disabled:hover { background: #fff; }
        .empty-slot {
            padding: 14px; border: 1px dashed #cbd5e0; border-radius: 10px; color: #64748b;
        }
        @media (max-width: 900px) {
            .slot-form { grid-template-columns: 1fr 1fr; }
            .schedule-head { align-items: flex-start; flex-direction: column; }
        }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <div class="admin-content">
            <section class="admin-card schedule-board">
                <div class="schedule-head">
                    <h2>Quản lý lịch chiếu</h2>
                </div>
                <div class="schedule-body">
                    <c:if test="${msg == 'created'}">
                        <div class="alert alert-success">
                            <c:choose>
                                <c:when test="${skippedCount > 0}">Đã tạo ${createdCount} suất. ${skippedCount} suất không lưu vì phòng bận.</c:when>
                                <c:when test="${createdCount > 1}">Đã tạo ${createdCount} suất chiếu.</c:when>
                                <c:otherwise>Đã tạo suất chiếu.</c:otherwise>
                            </c:choose>
                        </div>
                    </c:if>
                    <c:if test="${msg == 'updated'}"><div class="alert alert-success">Đã cập nhật suất chiếu.</div></c:if>
                    <c:if test="${msg == 'deleted'}"><div class="alert alert-success">Đã xóa suất chưa có vé.</div></c:if>
                    <c:if test="${msg == 'kept'}"><div class="alert alert-success">Suất đã có vé nên không xóa. Đã chuyển sang Đã hủy.</div></c:if>
                    <c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>

                    <form method="get" action="${pageContext.request.contextPath}${listPath}" class="filter-bar">
                        <c:if test="${actor.role.roleName == 'ADMIN'}">
                            <div class="filter-field">
                                <label>Rạp</label>
                                <select class="filter-select" name="cinemaId">
                                    <c:forEach var="cinema" items="${cinemas}">
                                        <option value="${cinema.id}" ${selectedCinemaId == cinema.id ? 'selected' : ''}>${cinema.name}</option>
                                    </c:forEach>
                                </select>
                            </div>
                        </c:if>
                        <div class="filter-field">
                            <label>Ngày <span style="font-weight:400;color:#94a3b8">(để trống = tất cả)</span></label>
                            <input class="filter-date" type="date" name="date" value="${selectedDate}" />
                        </div>
                        <div class="filter-field">
                            <label>Phim</label>
                            <select class="filter-select" name="movieId">
                                <option value="">Tất cả phim</option>
                                <c:forEach var="movie" items="${movies}">
                                    <option value="${movie.id}" ${selectedMovieId == movie.id ? 'selected' : ''}>${movie.title}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-field">
                            <label>Phòng</label>
                            <select class="filter-select" name="roomId">
                                <option value="">Tất cả phòng</option>
                                <c:forEach var="room" items="${rooms}">
                                    <option value="${room.id}" ${selectedRoomId == room.id ? 'selected' : ''}>${room.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                        <div class="filter-field">
                            <label>Trạng thái</label>
                            <select class="filter-select" name="status">
                                <option value="" ${empty selectedStatus ? 'selected' : ''}>Tất cả</option>
                                <option value="ACTIVE" ${selectedStatus == 'ACTIVE' ? 'selected' : ''}>Đang hoạt động</option>
                                <option value="SCHEDULED" ${selectedStatus == 'SCHEDULED' ? 'selected' : ''}>Đã lên lịch</option>
                                <option value="OPENING" ${selectedStatus == 'OPENING' ? 'selected' : ''}>Đang chiếu</option>
                                <option value="FINISHED" ${selectedStatus == 'FINISHED' ? 'selected' : ''}>Đã chiếu</option>
                                <option value="CANCELLED" ${selectedStatus == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                            </select>
                        </div>
                        <div class="filter-actions">
                            <button class="filter-apply" type="submit">Áp dụng</button>
                        </div>
                    </form>

                    <c:if test="${empty moviesToShow}">
                        <div class="filter-empty">Không có suất chiếu khớp bộ lọc. Đổi ngày/phim/phòng hoặc bấm Thêm suất từ danh sách phim sau khi chọn 1 phim.</div>
                    </c:if>

                    <c:forEach var="movie" items="${moviesToShow}" varStatus="st">
                        <article class="movie-frame">
                            <header class="movie-head">
                                <span class="stt">${st.index + 1}</span>
                                <div>
                                    <h3>${movie.title}</h3>
                                    <p class="movie-meta">${movie.durationMinutes} phút ·
                                        <c:choose>
                                            <c:when test="${movie.status == 'NOW_SHOWING'}">Đang chiếu</c:when>
                                            <c:when test="${movie.status == 'COMING_SOON'}">Sắp chiếu</c:when>
                                            <c:otherwise>${movie.status}</c:otherwise>
                                        </c:choose>
                                    </p>
                                </div>
                                <a class="add-slot" href="${pageContext.request.contextPath}${listPath}/create?movieId=${movie.id}&amp;cinemaId=${selectedCinemaId}<c:if test='${not empty selectedDate}'>&amp;date=${selectedDate}</c:if>">Thêm suất</a>
                            </header>
                            <div class="slot-list">
                                <c:set var="hasSlot" value="false" />
                                <c:forEach var="showtime" items="${showtimes}">
                                    <c:if test="${showtime.movieId == movie.id}">
                                        <c:set var="hasSlot" value="true" />
                                        <c:set var="stStatus" value="${showtime.displayStatus}" />
                                        <c:set var="locked" value="${showtime.editLocked}" />
                                        <form class="slot-form ${stStatus == 'CANCELLED' ? 'is-cancelled' : ''} ${stStatus == 'FINISHED' ? 'is-finished' : ''} ${locked && stStatus == 'SCHEDULED' ? 'is-finished' : ''}"
                                              method="post" action="${pageContext.request.contextPath}${listPath}">
                                            <input type="hidden" name="showtimeId" value="${showtime.id}" />
                                            <input type="hidden" name="cinemaId" value="${selectedCinemaId}" />
                                            <input type="hidden" name="date" value="${selectedDate}" />
                                            <input type="hidden" name="movieId" value="${selectedMovieId}" />
                                            <input type="hidden" name="roomId" value="${selectedRoomId}" />
                                            <input type="hidden" name="status" value="${selectedStatus}" />
                                            <div class="slot-status-wrap">
                                                <label>Trạng thái</label>
                                                <c:choose>
                                                    <c:when test="${stStatus == 'SCHEDULED'}">
                                                        <span class="status-badge status-scheduled">Đã lên lịch</span>
                                                    </c:when>
                                                    <c:when test="${stStatus == 'OPENING'}">
                                                        <span class="status-badge status-opening">Đang chiếu</span>
                                                    </c:when>
                                                    <c:when test="${stStatus == 'FINISHED'}">
                                                        <span class="status-badge status-finished">Đã chiếu</span>
                                                    </c:when>
                                                    <c:when test="${stStatus == 'CANCELLED'}">
                                                        <span class="status-badge status-cancelled">Đã hủy</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="status-badge status-other">${stStatus}</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div>
                                                <label>Phòng</label>
                                                <select class="slot-input" name="screeningRoomId" required ${locked ? 'disabled' : ''}>
                                                    <c:forEach var="room" items="${rooms}">
                                                        <option value="${room.id}" ${showtime.screeningRoomId == room.id ? 'selected' : ''}>${room.name}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div>
                                                <label>Giờ bắt đầu</label>
                                                <input class="slot-input" type="datetime-local" name="startTime" required
                                                       value="${fn:substring(showtime.startTime, 0, 16)}" ${locked ? 'disabled' : ''} />
                                            </div>
                                            <div>
                                                <label>Định dạng</label>
                                                <select class="slot-input" name="experienceFormat" ${locked ? 'disabled' : ''}>
                                                    <option value="2D" ${showtime.experienceFormat == '2D' ? 'selected' : ''}>2D</option>
                                                    <option value="3D" ${showtime.experienceFormat == '3D' ? 'selected' : ''}>3D</option>
                                                    <option value="IMAX" ${showtime.experienceFormat == 'IMAX' ? 'selected' : ''}>IMAX</option>
                                                </select>
                                            </div>
                                            <c:choose>
                                                <c:when test="${!locked}">
                                                    <button class="save-btn" type="submit" name="action" value="update">Lưu</button>
                                                    <button class="delete-btn" type="submit" name="action" value="delete"
                                                            onclick="return confirm('Xóa suất này? Suất đã có vé sẽ được hủy, không xóa khỏi lịch sử vé.');">Xóa</button>
                                                </c:when>
                                                <c:when test="${stStatus == 'SCHEDULED'}">
                                                    <span class="empty-slot" style="flex:1 1 180px;margin:0;padding:8px 12px;font-size:12px;">
                                                        Khóa sửa (&lt; 30 phút trước giờ chiếu)
                                                    </span>
                                                </c:when>
                                            </c:choose>
                                        </form>
                                    </c:if>
                                </c:forEach>
                                <c:if test="${hasSlot == 'false'}"><div class="empty-slot">Chưa có suất</div></c:if>
                            </div>
                        </article>
                    </c:forEach>
                </div>
            </section>
        </div>
    </div>
</div>
</body>
</html>
