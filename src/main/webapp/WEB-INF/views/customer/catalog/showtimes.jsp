<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Lịch chiếu — CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5">
    <h2 class="text-white fw-bold mb-4">Lịch chiếu</h2>

    <form method="get" action="${pageContext.request.contextPath}/showtimes" class="row g-3 mb-4">
        <div class="col-md-4">
            <label class="form-label text-secondary">Phim</label>
            <select class="form-select" name="movieId">
                <option value="">Tất cả phim</option>
                <c:forEach var="movie" items="${movies}">
                    <option value="${movie.id}" ${selectedMovieId == movie.id ? 'selected' : ''}>${movie.title}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-4">
            <label class="form-label text-secondary">Rạp</label>
            <select class="form-select" name="cinemaId">
                <option value="">Tất cả rạp</option>
                <c:forEach var="cinema" items="${cinemas}">
                    <option value="${cinema.id}" ${selectedCinemaId == cinema.id ? 'selected' : ''}>${cinema.name}</option>
                </c:forEach>
            </select>
        </div>
        <div class="col-md-2">
            <label class="form-label text-secondary">Ngày</label>
            <input class="form-control" type="date" name="date" value="${selectedDate}" />
        </div>
        <div class="col-md-2 d-flex align-items-end">
            <button class="btn btn-cinema-primary w-100" type="submit">Xem suất</button>
        </div>
    </form>

    <div class="row g-3">
        <c:forEach var="showtime" items="${showtimes}">
            <div class="col-md-6 col-lg-4">
                <a class="text-decoration-none" href="${pageContext.request.contextPath}/booking/checkout?showtimeId=${showtime.id}">
                    <div class="card p-3 text-white h-100" style="background:#141820;border:1px solid #2E3550;">
                        <div class="fw-bold">${showtime.movie.title}</div>
                        <div class="text-secondary small mt-1">
                            ${showtime.screeningRoom.cinema.name} · ${showtime.screeningRoom.name}
                        </div>
                        <div class="mt-2" style="color:#F5C518;">${showtime.startTime} · ${showtime.experienceFormat}</div>
                    </div>
                </a>
            </div>
        </c:forEach>
        <c:if test="${empty showtimes}">
            <p class="text-secondary">Không có suất chiếu đang mở trong ngày này.</p>
        </c:if>
    </div>
</div>

<jsp:include page="../../common/footer.jsp" />
