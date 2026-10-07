<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="MBCinema - Trang Chủ | Phim Đang Chiếu & Phim Sắp Chiếu" />
<jsp:include page="/views/common/header.jsp" />

<div class="container py-4">

    <!-- ============================================================ -->
    <!-- 1. HERO CAROUSEL: PHIM BOM TẤN NỔI BẬT                       -->
    <!-- ============================================================ -->
    <c:if test="${not empty featuredMovies}">
        <div id="heroCarousel" class="carousel slide hero-carousel" data-bs-ride="carousel" data-bs-interval="5000">
            <!-- Carousel Indicators -->
            <div class="carousel-indicators">
                <c:forEach var="fMovie" items="${featuredMovies}" varStatus="status">
                    <button type="button" data-bs-target="#heroCarousel" data-bs-slide-to="${status.index}" class="${status.first ? 'active' : ''}" aria-label="Slide ${status.index + 1}"></button>
                </c:forEach>
            </div>

            <!-- Carousel Slides -->
            <div class="carousel-inner">
                <c:forEach var="fMovie" items="${featuredMovies}" varStatus="status">
                    <div class="carousel-item ${status.first ? 'active' : ''}">
                        <div class="hero-slide" style="background-image: url('${fMovie.posterUrl}');">
                            <div class="hero-overlay">
                                <div class="hero-content">
                                    <span class="hero-badge">
                                        <i class="fa-solid fa-fire me-1"></i> Bom Tấn Nổi Bật
                                    </span>
                                    <h1 class="hero-title">${fMovie.title}</h1>
                                    <div class="d-flex align-items-center gap-3 mb-3">
                                        <span class="badge-tag ${fMovie.ageBadgeClass}">${fMovie.ageRating}</span>
                                        <span class="text-white-50"><i class="fa-regular fa-clock me-1"></i> ${fMovie.duration} phút</span>
                                        <span class="text-warning fw-bold"><i class="fa-solid fa-star me-1"></i> ${fMovie.rating} / 5.0</span>
                                        <span class="text-white-50"><i class="fa-solid fa-film me-1"></i> ${fMovie.genreString}</span>
                                    </div>
                                    <p class="hero-desc">${fMovie.description}</p>
                                    <div class="hero-actions">
                                        <a href="${pageContext.request.contextPath}/movie-detail?id=${fMovie.movieId}" class="btn btn-cinema-primary">
                                            <i class="fa-solid fa-circle-info"></i> Xem Chi Tiết
                                        </a>
                                        <c:if test="${not empty fMovie.trailerUrl}">
                                            <button type="button" class="btn btn-cinema-outline" data-trailer-url="${fMovie.trailerUrl}" data-movie-title="${fMovie.title}">
                                                <i class="fa-solid fa-play"></i> Xem Trailer
                                            </button>
                                        </c:if>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- Controls -->
            <button class="carousel-control-prev" type="button" data-bs-target="#heroCarousel" data-bs-slide="prev">
                <span class="carousel-control-prev-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Trước</span>
            </button>
            <button class="carousel-control-next" type="button" data-bs-target="#heroCarousel" data-bs-slide="next">
                <span class="carousel-control-next-icon" aria-hidden="true"></span>
                <span class="visually-hidden">Sau</span>
            </button>
        </div>
    </c:if>

    <!-- ============================================================ -->
    <!-- 2. QUICK GENRE PILLS                                         -->
    <!-- ============================================================ -->
    <c:if test="${not empty genres}">
        <div class="d-flex align-items-center gap-2 overflow-x-auto pb-3 mb-4">
            <span class="text-secondary fw-semibold text-nowrap"><i class="fa-solid fa-tags me-1"></i> Thể loại:</span>
            <a href="${pageContext.request.contextPath}/movies" class="type-tab-btn py-1 px-3 fs-6 active">Tất cả</a>
            <c:forEach var="g" items="${genres}">
                <a href="${pageContext.request.contextPath}/movies?genreId=${g.genreId}" class="type-tab-btn py-1 px-3 fs-6 text-nowrap">
                    ${g.genreName}
                </a>
            </c:forEach>
        </div>
    </c:if>

    <!-- ============================================================ -->
    <!-- 3. PHIM ĐANG CHIẾU (NOW SHOWING)                             -->
    <!-- ============================================================ -->
    <section class="mb-5">
        <div class="section-header-wrap">
            <h2 class="section-title">Phim Đang Chiếu</h2>
            <a href="${pageContext.request.contextPath}/movies?type=now_showing" class="section-view-all">
                Xem toàn bộ <i class="fa-solid fa-angle-right ms-1"></i>
            </a>
        </div>

        <c:choose>
            <c:when test="${not empty nowShowingMovies}">
                <div class="row g-4">
                    <c:forEach var="m" items="${nowShowingMovies}">
                        <div class="col-xl-3 col-lg-3 col-md-4 col-sm-6">
                            <div class="movie-card">
                                <!-- Poster -->
                                <div class="movie-poster-wrap">
                                    <img src="${m.posterUrl}" alt="${m.title}" class="movie-poster-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80'">
                                    
                                    <!-- Badges -->
                                    <span class="badge-status-top-left badge-tag badge-now-showing">
                                        <i class="fa-solid fa-circle-play me-1"></i> Đang Chiếu
                                    </span>
                                    <span class="badge-age-top-right badge-tag ${m.ageBadgeClass}">
                                        ${m.ageRating}
                                    </span>

                                    <!-- Hover Overlay -->
                                    <div class="poster-hover-overlay">
                                        <a href="${pageContext.request.contextPath}/movie-detail?id=${m.movieId}" class="btn btn-cinema-primary btn-sm w-100">
                                            <i class="fa-solid fa-ticket"></i> Mua Vé / Chi Tiết
                                        </a>
                                        <c:if test="${not empty m.trailerUrl}">
                                            <button type="button" class="btn btn-cinema-outline btn-sm w-100" data-trailer-url="${m.trailerUrl}" data-movie-title="${m.title}">
                                                <i class="fa-solid fa-play"></i> Xem Trailer
                                            </button>
                                        </c:if>
                                    </div>
                                </div>

                                <!-- Card Body -->
                                <div class="movie-card-body">
                                    <a href="${pageContext.request.contextPath}/movie-detail?id=${m.movieId}" class="movie-title" title="${m.title}">
                                        ${m.title}
                                    </a>
                                    <div class="movie-genre-text" title="${m.genreString}">
                                        ${m.genreString}
                                    </div>
                                    <div class="movie-meta">
                                        <span><i class="fa-regular fa-clock me-1"></i> ${m.duration} phút</span>
                                        <span class="ms-auto movie-rating-badge">
                                            <i class="fa-solid fa-star"></i> ${m.rating}
                                        </span>
                                    </div>
                                    <div class="movie-release-text">
                                        <i class="fa-regular fa-calendar-check me-1"></i> Khởi chiếu: ${m.formattedReleaseDate}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-dark text-center py-4 border-secondary">
                    <i class="fa-solid fa-film fs-2 text-secondary mb-2"></i>
                    <p class="mb-0">Hiện tại chưa có phim đang chiếu trong hệ thống.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- ============================================================ -->
    <!-- 4. PHIM SẮP CHIẾU (COMING SOON)                              -->
    <!-- ============================================================ -->
    <section class="mb-5">
        <div class="section-header-wrap">
            <h2 class="section-title">Phim Sắp Chiếu</h2>
            <a href="${pageContext.request.contextPath}/movies?type=coming_soon" class="section-view-all">
                Xem toàn bộ <i class="fa-solid fa-angle-right ms-1"></i>
            </a>
        </div>

        <c:choose>
            <c:when test="${not empty comingSoonMovies}">
                <div class="row g-4">
                    <c:forEach var="m" items="${comingSoonMovies}">
                        <div class="col-xl-3 col-lg-3 col-md-4 col-sm-6">
                            <div class="movie-card">
                                <!-- Poster -->
                                <div class="movie-poster-wrap">
                                    <img src="${m.posterUrl}" alt="${m.title}" class="movie-poster-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80'">
                                    
                                    <!-- Badges -->
                                    <span class="badge-status-top-left badge-tag badge-coming-soon">
                                        <i class="fa-solid fa-calendar-day me-1"></i> Sắp Chiếu
                                    </span>
                                    <span class="badge-age-top-right badge-tag ${m.ageBadgeClass}">
                                        ${m.ageRating}
                                    </span>

                                    <!-- Hover Overlay -->
                                    <div class="poster-hover-overlay">
                                        <a href="${pageContext.request.contextPath}/movie-detail?id=${m.movieId}" class="btn btn-cinema-primary btn-sm w-100">
                                            <i class="fa-solid fa-circle-info"></i> Thông Tin Phim
                                        </a>
                                        <c:if test="${not empty m.trailerUrl}">
                                            <button type="button" class="btn btn-cinema-outline btn-sm w-100" data-trailer-url="${m.trailerUrl}" data-movie-title="${m.title}">
                                                <i class="fa-solid fa-play"></i> Xem Trailer
                                            </button>
                                        </c:if>
                                    </div>
                                </div>

                                <!-- Card Body -->
                                <div class="movie-card-body">
                                    <a href="${pageContext.request.contextPath}/movie-detail?id=${m.movieId}" class="movie-title" title="${m.title}">
                                        ${m.title}
                                    </a>
                                    <div class="movie-genre-text" title="${m.genreString}">
                                        ${m.genreString}
                                    </div>
                                    <div class="movie-meta">
                                        <span><i class="fa-regular fa-clock me-1"></i> ${m.duration} phút</span>
                                        <span class="ms-auto text-info fw-bold">
                                            <i class="fa-solid fa-hourglass-start me-1"></i> Sắp ra mắt
                                        </span>
                                    </div>
                                    <div class="movie-release-text text-warning fw-semibold">
                                        <i class="fa-regular fa-calendar-days me-1"></i> Dự kiến: ${m.formattedReleaseDate}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-dark text-center py-4 border-secondary">
                    <i class="fa-solid fa-clapperboard fs-2 text-secondary mb-2"></i>
                    <p class="mb-0">Hiện tại chưa có phim sắp chiếu mới.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

</div>

<jsp:include page="/views/common/footer.jsp" />
