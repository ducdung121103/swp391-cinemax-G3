<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="Lịch chiếu — CineMax" />
<jsp:include page="../../common/header.jsp" />

<style>
    .st-movie-card {
        background: #141820;
        border: 1px solid #2E3550;
        border-radius: 14px;
        overflow: hidden;
    }
    .st-movie-toggle {
        width: 100%;
        text-align: left;
        background: transparent;
        border: 0;
        color: #EAEAEA;
        padding: 1rem 1.1rem;
        display: flex;
        gap: 1rem;
        align-items: center;
    }
    .st-movie-toggle:hover { background: rgba(229, 9, 20, 0.06); }
    .st-movie-toggle:focus { box-shadow: none; }
    .st-poster {
        width: 64px;
        height: 96px;
        object-fit: cover;
        border-radius: 8px;
        background: #1E2333;
        flex-shrink: 0;
    }
    .st-poster-fallback {
        width: 64px;
        height: 96px;
        border-radius: 8px;
        background: #1E2333;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #6B7A99;
        flex-shrink: 0;
    }
    .st-showtime-row {
        display: flex;
        flex-wrap: wrap;
        gap: 0.75rem;
        align-items: center;
        justify-content: space-between;
        padding: 0.85rem 0;
        border-top: 1px solid rgba(46, 53, 80, 0.7);
    }
    .st-showtime-meta { color: #9AA0B2; font-size: 0.9rem; }
    .st-showtime-time { color: #F5C518; font-weight: 700; font-size: 1.05rem; }
    .st-chevron { transition: transform 0.2s ease; color: #9AA0B2; }
    .st-movie-toggle:not(.collapsed) .st-chevron { transform: rotate(180deg); color: #E50914; }
</style>

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

    <c:choose>
        <c:when test="${not empty movieShowtimes}">
            <div class="accordion d-flex flex-column gap-3" id="movieShowtimesAccordion">
                <c:forEach var="entry" items="${movieShowtimes}" varStatus="loop">
                    <c:set var="movie" value="${entry.key}" />
                    <c:set var="movieShows" value="${entry.value}" />
                    <c:set var="panelId" value="moviePanel${movie.id}" />
                    <c:set var="shouldExpand" value="${(not empty selectedMovieId and selectedMovieId == movie.id) or (empty selectedMovieId and loop.first)}" />

                    <div class="st-movie-card">
                        <button class="st-movie-toggle ${shouldExpand ? '' : 'collapsed'}"
                                type="button"
                                data-bs-toggle="collapse"
                                data-bs-target="#${panelId}"
                                aria-expanded="${shouldExpand}"
                                aria-controls="${panelId}">
                            <c:choose>
                                <c:when test="${not empty movie.posterUrl}">
                                    <img class="st-poster" src="${movie.posterUrl}" alt="${fn:escapeXml(movie.title)}"
                                         onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                    <div class="st-poster-fallback" style="display:none;"><i class="fa-solid fa-film"></i></div>
                                </c:when>
                                <c:otherwise>
                                    <div class="st-poster-fallback"><i class="fa-solid fa-film"></i></div>
                                </c:otherwise>
                            </c:choose>
                            <div class="flex-grow-1">
                                <div class="fw-bold fs-5 text-white">${movie.title}</div>
                                <div class="text-secondary small mt-1">
                                    <c:if test="${not empty movie.ageRating}">
                                        <span class="badge bg-danger bg-opacity-25 text-danger border border-danger border-opacity-25 me-1">${movie.ageRating}</span>
                                    </c:if>
                                    <c:if test="${not empty movie.durationMinutes}">
                                        <span class="me-2"><i class="fa-regular fa-clock me-1"></i>${movie.durationMinutes} phút</span>
                                    </c:if>
                                    <span><i class="fa-solid fa-ticket me-1 text-warning"></i>${fn:length(movieShows)} suất</span>
                                </div>
                            </div>
                            <i class="fa-solid fa-chevron-down st-chevron"></i>
                        </button>

                        <div id="${panelId}" class="collapse ${shouldExpand ? 'show' : ''}"
                             data-bs-parent="#movieShowtimesAccordion">
                            <div class="px-3 pb-3">
                                <c:forEach var="showtime" items="${movieShows}">
                                    <div class="st-showtime-row">
                                        <div>
                                            <div class="st-showtime-time">
                                                <i class="fa-regular fa-clock me-1"></i>${showtime.formattedTimeOnly}
                                                <span class="text-secondary fw-normal ms-1">· ${showtime.formattedDateOnly}</span>
                                            </div>
                                            <div class="st-showtime-meta mt-1">
                                                <i class="fa-solid fa-location-dot me-1 text-danger"></i>
                                                ${showtime.screeningRoom.cinema.name} · ${showtime.screeningRoom.name}
                                                <span class="ms-1">· ${showtime.experienceFormat}</span>
                                            </div>
                                        </div>
                                        <div class="d-flex gap-2 flex-shrink-0">
                                            <a href="${pageContext.request.contextPath}/booking/seats?showtimeId=${showtime.id}"
                                               class="btn btn-cinema-primary btn-sm">
                                                <i class="fa-solid fa-chair me-1"></i> Chọn ghế
                                            </a>
                                            <a href="${pageContext.request.contextPath}/movie/detail?id=${showtime.movieId}&amp;showtimeId=${showtime.id}"
                                               class="btn btn-outline-secondary btn-sm text-white">
                                                Chi tiết
                                            </a>
                                        </div>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </c:when>
        <c:otherwise>
            <p class="text-secondary mb-0">Không có suất chiếu đang mở trong ngày này.</p>
        </c:otherwise>
    </c:choose>
</div>

<jsp:include page="../../common/footer.jsp" />
