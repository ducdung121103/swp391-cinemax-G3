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
        .filter-bar { margin-bottom: 16px; }
        .filter-select, .slot-input {
            padding: 8px 12px; border: 1px solid #cbd5e0; border-radius: 8px; background: #fff;
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
        .slot-form > div { flex: 1 1 160px; min-width: 0; }
        .slot-input { width: 100%; }
        .save-btn {
            height: 38px; padding: 0 14px; border: 0; border-radius: 8px;
            background: #14141e; color: #fff; font-weight: 700; cursor: pointer;
        }
        .save-btn:hover { background: #e50914; }
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
                    <a class="create-btn" href="${pageContext.request.contextPath}${listPath}/create">
                        <span class="create-plus">+</span>
                        Tạo lịch chiếu
                    </a>
                </div>
                <div class="schedule-body">
                    <c:if test="${msg == 'created'}"><div class="alert alert-success">Đã tạo suất chiếu.</div></c:if>
                    <c:if test="${msg == 'updated'}"><div class="alert alert-success">Đã cập nhật suất chiếu.</div></c:if>
                    <c:if test="${not empty error}"><div class="alert alert-danger">${error}</div></c:if>

                    <c:if test="${actor.role.roleName == 'ADMIN'}">
                        <form method="get" action="${pageContext.request.contextPath}${pageContext.request.servletPath}" class="filter-bar">
                            <label>Rạp</label>
                            <select class="filter-select" name="cinemaId" onchange="this.form.submit()">
                                <c:forEach var="cinema" items="${cinemas}">
                                    <option value="${cinema.id}" ${selectedCinemaId == cinema.id ? 'selected' : ''}>${cinema.name}</option>
                                </c:forEach>
                            </select>
                        </form>
                    </c:if>

                    <c:forEach var="movie" items="${movies}" varStatus="st">
                        <c:set var="hasSlot" value="false" />
                        <c:forEach var="showtime" items="${showtimes}">
                            <c:if test="${showtime.movieId == movie.id}"><c:set var="hasSlot" value="true" /></c:if>
                        </c:forEach>
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
                                <c:if test="${hasSlot == 'true'}">
                                    <a class="add-slot" href="${pageContext.request.contextPath}${listPath}/create?movieId=${movie.id}&amp;cinemaId=${selectedCinemaId}">Thêm suất</a>
                                </c:if>
                            </header>
                            <div class="slot-list">
                                <c:set var="hasSlot" value="false" />
                                <c:forEach var="showtime" items="${showtimes}">
                                    <c:if test="${showtime.movieId == movie.id}">
                                        <c:set var="hasSlot" value="true" />
                                        <form class="slot-form" method="post" action="${pageContext.request.contextPath}${listPath}">
                                            <input type="hidden" name="action" value="update" />
                                            <input type="hidden" name="showtimeId" value="${showtime.id}" />
                                            <input type="hidden" name="cinemaId" value="${selectedCinemaId}" />
                                            <div>
                                                <label>Phòng</label>
                                                <select class="slot-input" name="screeningRoomId" required>
                                                    <c:forEach var="room" items="${rooms}">
                                                        <option value="${room.id}" ${showtime.screeningRoomId == room.id ? 'selected' : ''}>${room.name}</option>
                                                    </c:forEach>
                                                </select>
                                            </div>
                                            <div>
                                                <label>Giờ bắt đầu</label>
                                                <input class="slot-input" type="datetime-local" name="startTime" required
                                                       value="${fn:substring(showtime.startTime, 0, 16)}" />
                                            </div>
                                            <div>
                                                <label>Định dạng</label>
                                                <select class="slot-input" name="experienceFormat">
                                                    <option value="2D" ${showtime.experienceFormat == '2D' ? 'selected' : ''}>2D</option>
                                                    <option value="3D" ${showtime.experienceFormat == '3D' ? 'selected' : ''}>3D</option>
                                                    <option value="IMAX" ${showtime.experienceFormat == 'IMAX' ? 'selected' : ''}>IMAX</option>
                                                </select>
                                            </div>
                                            <div>
                                                <label>Trạng thái</label>
                                                <select class="slot-input" name="status">
                                                    <option value="SCHEDULED" ${showtime.status == 'SCHEDULED' ? 'selected' : ''}>Đã lên lịch</option>
                                                    <option value="OPENING" ${showtime.status == 'OPENING' ? 'selected' : ''}>Đang chiếu</option>
                                                    <option value="FINISHED" ${showtime.status == 'FINISHED' ? 'selected' : ''}>Đã xong</option>
                                                    <option value="CANCELLED" ${showtime.status == 'CANCELLED' ? 'selected' : ''}>Đã hủy</option>
                                                </select>
                                            </div>
                                            <button class="save-btn" type="submit">Lưu</button>
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
