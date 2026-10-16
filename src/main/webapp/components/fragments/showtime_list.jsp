<%@ page contentType="text/html;charset=UTF-8" language="java" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

            <c:if test="${empty movies}">
                <div class="text-center p-5 dark-card rounded">
                    <i class="fa fa-calendar-times-o fa-3x text-muted mb-3 opacity-50"></i>
                    <h4 class="text-secondary">Không có suất chiếu nào trong ngày này.</h4>
                    <p class="text-muted">Vui lòng chọn ngày khác hoặc rạp khác.</p>
                </div>
            </c:if>

            <c:forEach var="movie" items="${movies}">
                <div class="movie-group">
                    <img src="${not empty movie.posterUrl ? movie.posterUrl : 'images/default-poster.jpg'}"
                        class="movie-poster" alt="${movie.title}">
                    <div class="movie-info">
                        <div class="movie-title">
                            <a href="${pageContext.request.contextPath}/movie?movieId=${movie.movieId}"
                                class="text-decoration-none">${movie.title}</a>
                        </div>
                        <p class="text-secondary">
                            <span class="badge bg-warning text-dark me-2">${not empty movie.ageRating ? movie.ageRating
                                :
                                'G'}</span>
                            <i class="fa fa-clock-o me-1"></i> ${movie.duration} phút
                        </p>
                        <div class="showtime-list">
                            <c:choose>
                                <c:when test="${not empty movie.showtimes}">
                                    <c:forEach var="st" items="${movie.showtimes}">
                                        <c:set var="timeStr" value="${st.startTime.toString().substring(0, 5)}" />
                                        <a href="${pageContext.request.contextPath}/booking-tickets?showtimeId=${st.showtimeId}"
                                            class="showtime-btn" data-start-datetime="${st.showDate}T${st.startTime}">
                                            ${timeStr}
                                        </a>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise>
                                    <span class="text-muted">Hết suất chiếu</span>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </c:forEach>

            <script>

                document.addEventListener('click', function (e) {
                    const btn = e.target.closest('.showtime-btn');
                    if (!btn) return;

                    const dtStr = btn.getAttribute('data-start-datetime');
                    if (!dtStr) return;

                    const startTime = new Date(dtStr);
                    if (isNaN(startTime.getTime())) return;

                    const now = new Date();
                    if (startTime <= now) {
                        e.preventDefault();
                        alert('Đã qua thời gian chiếu phim');
                    }
                });
            </script>