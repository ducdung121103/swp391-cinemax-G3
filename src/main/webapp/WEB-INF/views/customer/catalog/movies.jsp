<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Phim Đang Chiếu - Cinema Chain</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/catalog.css">
</head>
<body>
    <jsp:include page="../../common/header.jsp" />

    <div style="max-width: 1200px; margin: 30px auto; padding: 0 15px;">
        <h2 style="border-left: 5px solid #e50914; padding-left: 15px; margin-bottom: 25px;">PHIM ĐANG CHIẾU</h2>
        <div class="movie-grid">
            <c:forEach items="${nowShowing}" var="m">
                <div class="movie-card">
                    <img src="${m.posterUrl}" alt="${m.title}" class="movie-poster">
                    <div class="movie-info">
                        <div class="movie-title" title="${m.title}">${m.title}</div>
                        <p style="font-size: 13px; color: #666; margin-bottom: 10px;">Thời lượng: ${m.durationMinutes} phút | ${m.ageRating}</p>
                        <a href="${pageContext.request.contextPath}/movie/detail?id=${m.id}" class="btn-register" style="display: block; text-align: center; text-decoration: none; padding: 8px;">Mua Vé</a>
                    </div>
                </div>
            </c:forEach>
        </div>

        <h2 style="border-left: 5px solid #ffc107; padding-left: 15px; margin: 40px 0 25px;">PHIM SẮP CHIẾU</h2>
        <div class="movie-grid">
            <c:forEach items="${comingSoon}" var="m">
                <div class="movie-card">
                    <img src="${m.posterUrl}" alt="${m.title}" class="movie-poster">
                    <div class="movie-info">
                        <div class="movie-title" title="${m.title}">${m.title}</div>
                        <p style="font-size: 13px; color: #666; margin-bottom: 10px;">Khởi chiếu: ${m.releaseDate}</p>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

    <jsp:include page="../../common/footer.jsp" />
</body>
</html>
