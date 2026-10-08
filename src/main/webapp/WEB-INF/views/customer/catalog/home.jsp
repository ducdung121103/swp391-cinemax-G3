<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="CineMax - Trang Chủ | Hệ Thống Rạp Chiếu Phim Toàn Quốc" />
<jsp:include page="../../common/header.jsp" />

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
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${fMovie.id}" class="btn btn-cinema-primary">
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
    <!-- 2. QUICK GENRES BAR                                          -->
    <!-- ============================================================ -->
    <c:if test="${not empty genres}">
        <div class="my-4 d-flex align-items-center gap-2 overflow-auto py-2">
            <span class="text-secondary small fw-bold text-uppercase me-2 text-nowrap">
                <i class="fa-solid fa-tags me-1"></i> Thể Loại:
            </span>
            <a href="${pageContext.request.contextPath}/movies?type=all" class="badge bg-secondary text-decoration-none px-3 py-2 rounded-pill">
                Tất Cả
            </a>
            <c:forEach var="g" items="${genres}">
                <a href="${pageContext.request.contextPath}/movies?genreId=${g.id}" class="badge bg-dark border border-secondary text-light text-decoration-none px-3 py-2 rounded-pill hover-gold">
                    ${g.name}
                </a>
            </c:forEach>
        </div>
    </c:if>

    <!-- ============================================================ -->
    <!-- 3. PHIM ĐANG CHIẾU (NOW SHOWING)                             -->
    <!-- ============================================================ -->
    <section class="section-cinema">
        <div class="section-header">
            <div>
                <h2 class="section-title">
                    <i class="fa-solid fa-fire text-danger"></i> Phim Đang Chiếu
                </h2>
                <p class="section-sub">Các tác phẩm điện ảnh đỉnh cao đang khởi chiếu tại tất cả các cụm rạp CineMax</p>
            </div>
            <a href="${pageContext.request.contextPath}/movies?type=now_showing" class="btn btn-cinema-outline btn-sm">
                Xem Tất Cả (${nowShowingMovies.size()}) <i class="fa-solid fa-angle-right ms-1"></i>
            </a>
        </div>

        <c:choose>
            <c:when test="${not empty nowShowingMovies}">
                <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-4">
                    <c:forEach var="movie" items="${nowShowingMovies}">
                        <div class="col">
                            <div class="movie-card">
                                <div class="movie-poster-wrap">
                                    <img src="${movie.posterUrl}" alt="${movie.title}" class="movie-poster" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&auto=format&fit=crop&q=80'">
                                    
                                    <span class="badge-tag ${movie.ageBadgeClass} badge-status-top-left">
                                        ${movie.ageRating}
                                    </span>
                                    
                                    <span class="badge-tag badge-now-showing badge-status-top-right">
                                        <i class="fa-solid fa-circle me-1" style="font-size: 6px;"></i> Đang Chiếu
                                    </span>

                                    <div class="movie-overlay">
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}" class="btn btn-cinema-primary btn-sm mb-2 w-75">
                                            <i class="fa-solid fa-ticket"></i> Mua Vé Ngay
                                        </a>
                                        <c:if test="${not empty movie.trailerUrl}">
                                            <button type="button" class="btn btn-cinema-outline btn-sm w-75" data-trailer-url="${movie.trailerUrl}" data-movie-title="${movie.title}">
                                                <i class="fa-solid fa-play"></i> Trailer
                                            </button>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="movie-body">
                                    <h5 class="movie-card-title" title="${movie.title}">
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}">${movie.title}</a>
                                    </h5>
                                    <div class="movie-meta-item">
                                        <i class="fa-solid fa-film text-warning"></i> ${movie.genreString}
                                    </div>
                                    <div class="d-flex justify-content-between align-items-center mt-2">
                                        <div class="movie-meta-item mb-0">
                                            <i class="fa-regular fa-clock"></i> ${movie.duration} phút
                                        </div>
                                        <div class="text-warning small fw-bold">
                                            <i class="fa-solid fa-star"></i> ${movie.rating}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <i class="fa-solid fa-film empty-state-icon"></i>
                    <p class="mb-0">Hiện chưa có phim nào đang chiếu trong hệ thống.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- ============================================================ -->
    <!-- 4. PHIM SẮP CHIẾU (COMING SOON)                             -->
    <!-- ============================================================ -->
    <section class="section-cinema">
        <div class="section-header">
            <div>
                <h2 class="section-title">
                    <i class="fa-solid fa-calendar-days text-info"></i> Phim Sắp Chiếu
                </h2>
                <p class="section-sub">Những siêu phẩm đáng mong đợi sắp đổ bộ hệ thống phòng chiếu trong thời gian tới</p>
            </div>
            <a href="${pageContext.request.contextPath}/movies?type=coming_soon" class="btn btn-cinema-outline btn-sm">
                Xem Tất Cả (${comingSoonMovies.size()}) <i class="fa-solid fa-angle-right ms-1"></i>
            </a>
        </div>

        <c:choose>
            <c:when test="${not empty comingSoonMovies}">
                <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-4">
                    <c:forEach var="movie" items="${comingSoonMovies}">
                        <div class="col">
                            <div class="movie-card">
                                <div class="movie-poster-wrap">
                                    <img src="${movie.posterUrl}" alt="${movie.title}" class="movie-poster" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&auto=format&fit=crop&q=80'">
                                    
                                    <span class="badge-tag ${movie.ageBadgeClass} badge-status-top-left">
                                        ${movie.ageRating}
                                    </span>
                                    
                                    <span class="badge-tag badge-coming-soon badge-status-top-right">
                                        <i class="fa-solid fa-calendar-day me-1"></i> Sắp Chiếu
                                    </span>

                                    <div class="movie-overlay">
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}" class="btn btn-cinema-primary btn-sm mb-2 w-75">
                                            <i class="fa-solid fa-circle-info"></i> Thông Tin
                                        </a>
                                        <c:if test="${not empty movie.trailerUrl}">
                                            <button type="button" class="btn btn-cinema-outline btn-sm w-75" data-trailer-url="${movie.trailerUrl}" data-movie-title="${movie.title}">
                                                <i class="fa-solid fa-play"></i> Trailer
                                            </button>
                                        </c:if>
                                    </div>
                                </div>
                                <div class="movie-body">
                                    <h5 class="movie-card-title" title="${movie.title}">
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}">${movie.title}</a>
                                    </h5>
                                    <div class="movie-meta-item">
                                        <i class="fa-solid fa-film text-warning"></i> ${movie.genreString}
                                    </div>
                                    <div class="d-flex justify-content-between align-items-center mt-2">
                                        <div class="movie-meta-item mb-0 text-info fw-semibold">
                                            <i class="fa-regular fa-calendar"></i> ${movie.formattedReleaseDate}
                                        </div>
                                        <div class="text-white-50 small">
                                            ${movie.duration} phút
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="empty-state">
                    <i class="fa-solid fa-clapperboard empty-state-icon"></i>
                    <p class="mb-0">Hiện chưa có thông tin phim sắp chiếu.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- ============================================================ -->
    <!-- 5. BANNER GIỚI THIỆU TRẢI NGHIỆM ĐIỆN ẢNH CINEMAX             -->
    <!-- ============================================================ -->
    <section class="card bg-dark border-secondary p-4 p-md-5 my-5 text-white shadow-lg">
        <div class="row align-items-center g-4">
            <div class="col-lg-7">
                <span class="badge bg-warning text-dark fw-bold mb-2 px-3 py-2 rounded-pill">Trải Nghiệm Đẳng Cấp</span>
                <h3 class="fw-bold mb-3">Công Nghệ Phòng Chiếu IMAX Laser & Dolby Atmos</h3>
                <p class="text-secondary mb-4 leading-relaxed">
                    Hệ thống rạp CineMax mang đến trải nghiệm điện ảnh chân thực với màn hình cong cực đại, dàn âm thanh vòm sống động đa chiều và ghế ngả Sweetbox cao cấp dành cho mọi cặp đôi và gia đình.
                </p>
                <div class="d-flex flex-wrap gap-3">
                    <a href="${pageContext.request.contextPath}/movies" class="btn btn-cinema-primary">
                        <i class="fa-solid fa-ticket me-1"></i> Khám Phá Suất Chiếu
                    </a>
                </div>
            </div>
            <div class="col-lg-5 text-center">
                <div class="p-3 bg-black bg-opacity-50 rounded-4 border border-secondary border-opacity-50">
                    <div class="row g-3">
                        <div class="col-6">
                            <div class="p-3 border border-secondary rounded-3 text-center">
                                <i class="fa-solid fa-video text-warning fs-2 mb-2"></i>
                                <h6 class="fw-bold mb-1">IMAX Laser</h6>
                                <span class="small text-secondary">Độ nét 4K siêu thực</span>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 border border-secondary rounded-3 text-center">
                                <i class="fa-solid fa-volume-high text-danger fs-2 mb-2"></i>
                                <h6 class="fw-bold mb-1">Dolby Atmos</h6>
                                <span class="small text-secondary">Âm thanh vòm 360°</span>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 border border-secondary rounded-3 text-center">
                                <i class="fa-solid fa-couch text-info fs-2 mb-2"></i>
                                <h6 class="fw-bold mb-1">Ghế Sweetbox</h6>
                                <span class="small text-secondary">Êm ái & riêng tư</span>
                            </div>
                        </div>
                        <div class="col-6">
                            <div class="p-3 border border-secondary rounded-3 text-center">
                                <i class="fa-solid fa-burger text-success fs-2 mb-2"></i>
                                <h6 class="fw-bold mb-1">Bắp Nước F&B</h6>
                                <span class="small text-secondary">Bắp nóng giòn thơm ngon</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </section>

</div>

<jsp:include page="../../common/footer.jsp" />
