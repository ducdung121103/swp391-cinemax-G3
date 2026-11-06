<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Chọn ghế — CineMax" />
<jsp:include page="../../common/header.jsp" />

<style>
    .seat-screen { text-align: center; color: #9aa3b8; letter-spacing: .18em; font-size: .8rem; margin: 8px 0 22px; }
    .seat-screen span { display: inline-block; min-width: 220px; padding: 8px 24px; border-radius: 0 0 40px 40px; border: 1px solid #3a425c; border-top: 0; background: linear-gradient(#2a3148, transparent); }
    .seat-row { display: flex; align-items: center; justify-content: center; gap: 8px; margin-bottom: 8px; }
    .row-label { width: 22px; color: #9aa3b8; font-weight: 700; }
    .seat-pick { position: relative; display: inline-grid; }
    .seat-pick input {
        appearance: none; width: 46px; height: 36px; margin: 0; cursor: pointer;
        border-radius: 8px 8px 4px 4px; border: 1px solid #3a425c; background: #1c2333;
    }
    .seat-pick span {
        position: absolute; inset: 0; display: grid; place-items: center; pointer-events: none;
        color: #fff; font-size: .75rem; font-weight: 700;
    }
    .seat-pick input:checked { background: #e50914; border-color: #e50914; }
    .seat-pick input:disabled { background: #2a3144; cursor: not-allowed; }
    .seat-pick input:disabled + span { color: #667; text-decoration: line-through; }
    .seat-legend { display: flex; gap: 16px; justify-content: center; color: #9aa3b8; font-size: .85rem; margin-bottom: 18px; }
    .legend-box { display: inline-block; width: 16px; height: 12px; border-radius: 3px; margin-right: 6px; vertical-align: -1px; }
</style>

<div class="container py-5" style="max-width: 980px;">
    <h2 class="text-white fw-bold mb-1">Chọn ghế</h2>
    <p class="text-secondary mb-4">
        ${showtime.movie.title} · ${showtime.screeningRoom.cinema.name} · ${showtime.screeningRoom.name}
        <br><span style="color:#F5C518;">${showtimeText} · ${showtime.experienceFormat}</span>
    </p>

    <c:if test="${not empty seatError}">
        <div class="alert alert-danger">${seatError}</div>
    </c:if>

    <form method="get" action="${pageContext.request.contextPath}/booking/checkout" id="seatForm">
        <input type="hidden" name="showtimeId" value="${showtime.id}" />
        <div class="card p-4 text-white" style="background:#141820;border:1px solid #2E3550;">
            <div class="seat-legend">
                <span><i class="legend-box" style="background:#1c2333;border:1px solid #3a425c;"></i>Còn trống</span>
                <span><i class="legend-box" style="background:#e50914;"></i>Đang chọn</span>
                <span><i class="legend-box" style="background:#2a3144;"></i>Đã bán</span>
            </div>
            <div class="seat-screen"><span>MÀN HÌNH</span></div>
            <c:forEach var="row" items="${seatRows}">
                <div class="seat-row">
                    <span class="row-label">${row[0].seatRow}</span>
                    <c:forEach var="seat" items="${row}">
                        <c:set var="open" value="${availableIds.contains(seat.id) and seat.status == 'ACTIVE'}" />
                        <label class="seat-pick">
                            <input type="checkbox" name="seatIds" value="${seat.id}" ${open ? '' : 'disabled'} />
                            <span>${seat.seatCode}</span>
                        </label>
                    </c:forEach>
                </div>
            </c:forEach>
            <c:if test="${empty seatRows}">
                <p class="text-secondary text-center mb-0">Phòng này chưa có sơ đồ ghế.</p>
            </c:if>
        </div>
        <div class="d-flex flex-wrap gap-3 mt-4">
            <button class="btn btn-cinema-primary btn-lg" type="submit">Tiếp tục</button>
            <a class="btn btn-outline-secondary btn-lg" href="${pageContext.request.contextPath}/movie/detail?id=${showtime.movieId}&amp;showtimeId=${showtime.id}">Quay lại phim</a>
        </div>
    </form>
</div>

<script>
    document.getElementById('seatForm').addEventListener('submit', function (event) {
        if (!this.querySelector('input[name="seatIds"]:checked')) {
            event.preventDefault();
            alert('Hãy chọn ít nhất một ghế.');
        }
    });
</script>

<jsp:include page="../../common/footer.jsp" />
