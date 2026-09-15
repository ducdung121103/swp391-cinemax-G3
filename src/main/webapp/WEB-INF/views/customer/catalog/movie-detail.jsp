<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>${movie.title} - Chi Tiết Phim</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/catalog.css">
    <style>
        .detail-container {
            max-width: 1100px;
            margin: 40px auto;
            padding: 0 20px;
            display: grid;
            grid-template-columns: 320px 1fr;
            gap: 40px;
        }
        @media (max-width: 768px) {
            .detail-container {
                grid-template-columns: 1fr;
            }
        }
        .detail-poster {
            width: 100%;
            border-radius: 12px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
        }
        .detail-badge {
            display: inline-block;
            padding: 4px 10px;
            border-radius: 4px;
            font-weight: bold;
            font-size: 0.85rem;
            margin-bottom: 12px;
        }
        .badge-c18 { background: #e50914; color: white; }
        .badge-c16 { background: #e65100; color: white; }
        .badge-c13 { background: #f57f17; color: white; }
        .badge-p   { background: #2e7d32; color: white; }
        .meta-item {
            margin-bottom: 10px;
            color: #4a5568;
            font-size: 0.95rem;
        }
        .meta-item strong {
            color: #1a202c;
        }
        .synopsis-box {
            margin-top: 20px;
            line-height: 1.7;
            color: #2d3748;
            background: #f8fafc;
            padding: 20px;
            border-radius: 8px;
            border-left: 4px solid #e50914;
        }
        .btn-book-now {
            display: inline-block;
            margin-top: 25px;
            padding: 12px 30px;
            background: #e50914;
            color: white;
            font-weight: 600;
            text-decoration: none;
            border-radius: 6px;
            transition: background 0.2s;
        }
        .btn-book-now:hover {
            background: #b20710;
        }
    </style>
</head>
<body>
    <jsp:include page="../../common/header.jsp" />

    <div class="detail-container">
        <div>
            <img src="${movie.posterUrl}" alt="${movie.title}" class="detail-poster">
        </div>

        <div class="detail-info">
            <span class="detail-badge badge-${movie.ageRating.toLowerCase()}">${movie.ageRating}</span>
            <h1 style="margin: 0 0 15px 0; font-size: 2rem; color: #1a202c;">${movie.title}</h1>
            
            <div class="meta-item"><strong>Thời lượng:</strong> ${movie.durationMinutes} phút</div>
            <div class="meta-item"><strong>Khởi chiếu:</strong> ${movie.releaseDate}</div>
            <div class="meta-item"><strong>Ngôn ngữ:</strong> ${movie.language}</div>
            <c:if test="${not empty movie.director}">
                <div class="meta-item"><strong>Đạo diễn:</strong> ${movie.director}</div>
            </c:if>
            <c:if test="${not empty movie.actors}">
                <div class="meta-item"><strong>Diễn viên:</strong> ${movie.actors}</div>
            </c:if>

            <div class="synopsis-box">
                <h4 style="margin: 0 0 10px 0; color: #1a202c;">Nội Dung Phim</h4>
                <p style="margin: 0;">${movie.synopsis}</p>
            </div>

            <c:if test="${not empty movie.trailerUrl}">
                <div style="margin-top: 20px;">
                    <a href="${movie.trailerUrl}" target="_blank" style="color: #e50914; font-weight: 500; text-decoration: none;">
                        ▶ Xem Trailer chính thức trên YouTube
                    </a>
                </div>
            </c:if>

            <div style="margin-top: 25px;">
                <a href="${pageContext.request.contextPath}/booking/checkout?showtimeId=1" class="btn-book-now">
                    🎟️ Mua Vé Ngay (Suất Chiếu Hôm Nay)
                </a>
                <a href="${pageContext.request.contextPath}/movies" style="margin-left: 15px; color: #718096; text-decoration: none;">
                    ← Quay lại danh sách phim
                </a>
            </div>
        </div>
    </div>

    <jsp:include page="../../common/footer.jsp" />
</body>
</html>
