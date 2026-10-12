<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="${movie.title} - Chi Tiết Phim | CineMax" />
<jsp:include page="../../common/header.jsp" />

<!-- Breadcrumb -->
<div class="bg-dark py-2 border-bottom border-secondary">
    <div class="container">
        <nav aria-label="breadcrumb">
            <ol class="breadcrumb mb-0 small">
                <li class="breadcrumb-item"><a href="${pageContext.request.contextPath}/home" class="text-secondary text-decoration-none">Trang chủ</a></li>
                <li class="breadcrumb-item">
                    <c:choose>
                        <c:when test="${movie.nowShowing}">
                            <a href="${pageContext.request.contextPath}/movies?type=now_showing" class="text-secondary text-decoration-none">Phim đang chiếu</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/movies?type=coming_soon" class="text-secondary text-decoration-none">Phim sắp chiếu</a>
                        </c:otherwise>
                    </c:choose>
                </li>
                <li class="breadcrumb-item active text-warning" aria-current="page">${movie.title}</li>
            </ol>
        </nav>
    </div>
</div>

<!-- Movie Detail Hero -->
<div class="detail-hero">
    <div class="container">
        <div class="row g-5 align-items-center">
            <!-- Left: Poster -->
            <div class="col-lg-4 col-md-5">
                <div class="position-relative">
                    <img src="${movie.posterUrl}" alt="${movie.title}" class="detail-poster-img" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80'">
                    
                    <!-- Badges -->
                    <c:choose>
                        <c:when test="${movie.nowShowing}">
                            <span class="badge-status-top-left badge-tag badge-now-showing fs-6">
                                <i class="fa-solid fa-circle-play me-1"></i> Đang Chiếu
                            </span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge-status-top-left badge-tag badge-coming-soon fs-6">
                                <i class="fa-solid fa-calendar-day me-1"></i> Sắp Chiếu
                            </span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <!-- Right: Details -->
            <div class="col-lg-8 col-md-7">
                <div class="d-flex align-items-center gap-3 mb-3">
                    <span class="badge-tag ${movie.ageBadgeClass} fs-6">${movie.ageRating}</span>
                    <div class="text-warning fw-bold fs-5">
                        <i class="fa-solid fa-star me-1"></i> ${movie.rating} <span class="text-secondary fs-6">/ 5.0</span>
                    </div>
                </div>

                <h1 class="display-6 fw-bold text-white mb-2">${movie.title}</h1>
                <c:if test="${not empty movie.originalTitle}">
                    <p class="text-secondary fs-6 mb-3 fst-italic">${movie.originalTitle}</p>
                </c:if>

                <!-- Metadata List -->
                <div class="row g-3 my-3">
                    <div class="col-sm-6">
                        <div class="detail-meta-group">
                            <span class="detail-meta-label"><i class="fa-solid fa-film text-warning me-1"></i> Thể Loại</span>
                            <span class="detail-meta-val">${movie.genreString}</span>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="detail-meta-group">
                            <span class="detail-meta-label"><i class="fa-regular fa-clock text-warning me-1"></i> Thời Lượng</span>
                            <span class="detail-meta-val">${movie.duration} phút</span>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="detail-meta-group">
                            <span class="detail-meta-label"><i class="fa-regular fa-calendar-check text-warning me-1"></i> Khởi Chiếu</span>
                            <span class="detail-meta-val">${movie.formattedReleaseDate}</span>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="detail-meta-group">
                            <span class="detail-meta-label"><i class="fa-solid fa-language text-warning me-1"></i> Ngôn Ngữ</span>
                            <span class="detail-meta-val">${not empty movie.language ? movie.language : 'Phụ đề Tiếng Việt'}</span>
                        </div>
                    </div>
                    <c:if test="${not empty movie.director}">
                        <div class="col-sm-6">
                            <div class="detail-meta-group">
                                <span class="detail-meta-label"><i class="fa-solid fa-user-tie text-warning me-1"></i> Đạo Diễn</span>
                                <span class="detail-meta-val">${movie.director}</span>
                            </div>
                        </div>
                    </c:if>
                    <c:if test="${not empty movie.cast}">
                        <div class="col-sm-12">
                            <div class="detail-meta-group">
                                <span class="detail-meta-label"><i class="fa-solid fa-users text-warning me-1"></i> Diễn Viên</span>
                                <span class="detail-meta-val">${movie.cast}</span>
                            </div>
                        </div>
                    </c:if>
                </div>

                <!-- Synopsis -->
                <div class="my-4">
                    <h5 class="text-white fw-bold mb-2">Nội Dung Phim</h5>
                    <p class="text-secondary leading-relaxed">${movie.description}</p>
                </div>

                <!-- Action CTA Buttons -->
                <div class="d-flex flex-wrap gap-3 mt-4">
                    <c:choose>
                        <c:when test="${movie.nowShowing}">
                            <a href="${pageContext.request.contextPath}/booking/checkout?showtimeId=1" class="btn btn-cinema-primary btn-lg">
                                <i class="fa-solid fa-ticket me-2"></i> Mua Vé Ngay
                            </a>
                        </c:when>
                        <c:otherwise>
                            <button class="btn btn-secondary btn-lg disabled" disabled>
                                <i class="fa-regular fa-calendar me-2"></i> Sắp Khởi Chiếu
                            </button>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty movie.trailerUrl}">
                        <button type="button" class="btn btn-cinema-outline btn-lg" data-trailer-url="${movie.trailerUrl}" data-movie-title="${movie.title}">
                            <i class="fa-solid fa-play me-2"></i> Xem Trailer
                        </button>
                    </c:if>
                    
                    <a href="${pageContext.request.contextPath}/movies" class="btn btn-outline-secondary btn-lg">
                        <i class="fa-solid fa-arrow-left me-1"></i> Quay Lại
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Related Movies Section -->
<c:if test="${not empty relatedMovies}">
    <div class="container py-5 border-top border-secondary">
        <h3 class="fw-bold text-white mb-4">
            <i class="fa-solid fa-clapperboard text-warning me-2"></i> Phim Cùng Thể Loại
        </h3>
        <div class="row row-cols-2 row-cols-md-4 g-4">
            <c:forEach var="relMovie" items="${relatedMovies}">
                <div class="col">
                    <div class="movie-card">
                        <div class="movie-poster-wrap">
                            <img src="${relMovie.posterUrl}" alt="${relMovie.title}" class="movie-poster" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&auto=format&fit=crop&q=80'">
                            <span class="badge-tag ${relMovie.ageBadgeClass} badge-status-top-left">
                                ${relMovie.ageRating}
                            </span>
                            <div class="movie-overlay">
                                <a href="${pageContext.request.contextPath}/movie/detail?id=${relMovie.id}" class="btn btn-cinema-primary btn-sm mb-2 w-75">
                                    <i class="fa-solid fa-circle-info"></i> Xem Chi Tiết
                                </a>
                            </div>
                        </div>
                        <div class="movie-body">
                            <h6 class="movie-card-title" title="${relMovie.title}">
                                <a href="${pageContext.request.contextPath}/movie/detail?id=${relMovie.id}">${relMovie.title}</a>
                            </h6>
                            <div class="movie-meta-item">
                                <i class="fa-solid fa-film text-warning"></i> ${relMovie.genreString}
                            </div>
                            <div class="d-flex justify-content-between align-items-center mt-2 small text-secondary">
                                <span>${relMovie.duration} phút</span>
                                <span class="text-warning fw-bold"><i class="fa-solid fa-star"></i> ${relMovie.rating}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>

<jsp:include page="../../common/footer.jsp" />
