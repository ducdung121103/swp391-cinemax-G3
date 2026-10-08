<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="CineMax - Hệ Thống Rạp Chiếu Phim & Đặt Vé Đẳng Cấp Toàn Quốc" />
<jsp:include page="../../common/header.jsp" />

<main class="container py-4">

    <!-- ============================================================ -->
    <!-- 1. HERO CAROUSEL: PHIM BOM TẤN NỔI BẬT                       -->
    <!-- ============================================================ -->
    <c:if test="${not empty featuredMovies}">
        <div id="heroCarousel" class="carousel slide hero-carousel" data-bs-ride="carousel" data-bs-interval="6000">
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
                        <div class="hero-slide">
                            <!-- Background Ambient Blur -->
                            <img src="${fMovie.posterUrl}" alt="${fMovie.title}" class="hero-backdrop-blur">
                            <div class="hero-gradient-overlay"></div>

                            <!-- Hero Content: 2 Columns Layout -->
                            <div class="hero-content-wrap">
                                <div class="row align-items-center g-4">
                                    <div class="col-lg-8 col-md-7">
                                        <div class="hero-badge-spotlight">
                                            <i class="fa-solid fa-fire text-danger"></i> Bom Tấn Nổi Bật
                                        </div>
                                        <h1 class="hero-title">${fMovie.title}</h1>
                                        
                                        <div class="hero-meta">
                                            <span class="badge-age-top-left position-static ${fMovie.ageBadgeClass}">${fMovie.ageRating}</span>
                                            <span><i class="fa-regular fa-clock me-1 text-warning"></i> ${fMovie.duration} phút</span>
                                            <span class="text-warning fw-bold"><i class="fa-solid fa-star me-1"></i> ${fMovie.rating} / 5.0</span>
                                            <span><i class="fa-solid fa-film me-1 text-info"></i> ${fMovie.genreString}</span>
                                        </div>

                                        <p class="hero-synopsis">${fMovie.description}</p>

                                        <div class="d-flex flex-wrap gap-3">
                                            <a href="${pageContext.request.contextPath}/movie/detail?id=${fMovie.id}" class="btn btn-cinema-primary">
                                                <i class="fa-solid fa-ticket"></i> Đặt Vé Ngay
                                            </a>
                                            <c:if test="${not empty fMovie.trailerUrl}">
                                                <button type="button" class="btn btn-cinema-outline" data-trailer-url="${fMovie.trailerUrl}" data-movie-title="${fMovie.title}">
                                                    <i class="fa-solid fa-play text-danger"></i> Xem Trailer
                                                </button>
                                            </c:if>
                                        </div>
                                    </div>

                                    <!-- Right Column: 3D Poster Card -->
                                    <div class="col-lg-4 col-md-5 d-none d-md-flex justify-content-center">
                                        <img src="${fMovie.posterUrl}" alt="${fMovie.title}" class="hero-poster-card" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&auto=format&fit=crop&q=80'">
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
    <!-- 2. QUICK GENRES FILTER PILLS                                 -->
    <!-- ============================================================ -->
    <c:if test="${not empty genres}">
        <div class="mb-5">
            <div class="d-flex align-items-center mb-2">
                <span class="text-secondary small fw-bold text-uppercase me-3">
                    <i class="fa-solid fa-layer-group text-warning me-1"></i> Khám Phá Thể Loại:
                </span>
            </div>
            <div class="genre-pills-bar">
                <a href="${pageContext.request.contextPath}/movies?type=all" class="genre-pill active">
                    <i class="fa-solid fa-border-all me-1"></i> Tất Cả
                </a>
                <c:forEach var="g" items="${genres}">
                    <a href="${pageContext.request.contextPath}/movies?genreId=${g.id}" class="genre-pill">
                        ${g.name}
                    </a>
                </c:forEach>
            </div>
        </div>
    </c:if>

    <!-- ============================================================ -->
    <!-- 3. PHIM ĐANG CHIẾU (NOW SHOWING)                             -->
    <!-- ============================================================ -->
    <section class="section-cinema">
        <div class="section-header-box">
            <div>
                <h2 class="section-title-cinema">Phim Đang Chiếu</h2>
                <p class="section-subtitle-cinema">Các siêu phẩm điện ảnh bom tấn đang khởi chiếu tại tất cả các cụm rạp CineMax</p>
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
                            <div class="movie-card-cinema">
                                <!-- Poster Container -->
                                <div class="movie-poster-box">
                                    <img src="${movie.posterUrl}" alt="${movie.title}" class="movie-poster-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&auto=format&fit=crop&q=80'">
                                    
                                    <!-- Badges -->
                                    <span class="badge-age-top-left ${movie.ageBadgeClass}">
                                        ${movie.ageRating}
                                    </span>
                                    
                                    <span class="badge-status-top-right">
                                        <i class="fa-solid fa-circle text-danger me-1" style="font-size: 6px;"></i> Đang Chiếu
                                    </span>

                                    <!-- Hover Overlay -->
                                    <div class="movie-hover-overlay">
                                        <c:if test="${not empty movie.trailerUrl}">
                                            <button type="button" class="btn-play-trailer" data-trailer-url="${movie.trailerUrl}" data-movie-title="${movie.title}" title="Xem Trailer">
                                                <i class="fa-solid fa-play ms-1"></i>
                                            </button>
                                        </c:if>
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}" class="btn btn-cinema-primary btn-sm w-100 mt-2">
                                            <i class="fa-solid fa-ticket"></i> Mua Vé Ngay
                                        </a>
                                    </div>
                                </div>

                                <!-- Movie Info -->
                                <div class="movie-card-body">
                                    <h3 class="movie-card-title" title="${movie.title}">
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}">${movie.title}</a>
                                    </h3>
                                    <div class="movie-card-genres">
                                        <i class="fa-solid fa-film me-1 text-warning"></i> ${movie.genreString}
                                    </div>
                                    <div class="movie-card-footer">
                                        <span><i class="fa-regular fa-clock me-1"></i> ${movie.duration} phút</span>
                                        <span class="movie-card-rating">
                                            <i class="fa-solid fa-star"></i> ${movie.rating}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center py-5 text-muted">
                    <i class="fa-solid fa-film fs-1 mb-3"></i>
                    <p>Hiện chưa có phim nào đang chiếu trong hệ thống.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- ============================================================ -->
    <!-- 4. PHIM SẮP CHIẾU (COMING SOON)                             -->
    <!-- ============================================================ -->
    <section class="section-cinema">
        <div class="section-header-box">
            <div>
                <h2 class="section-title-cinema">Phim Sắp Chiếu</h2>
                <p class="section-subtitle-cinema">Những dự án điện ảnh được mong chờ nhất chuẩn bị đổ bộ phòng vé</p>
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
                            <div class="movie-card-cinema">
                                <!-- Poster Container -->
                                <div class="movie-poster-box">
                                    <img src="${movie.posterUrl}" alt="${movie.title}" class="movie-poster-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&auto=format&fit=crop&q=80'">
                                    
                                    <!-- Badges -->
                                    <span class="badge-age-top-left ${movie.ageBadgeClass}">
                                        ${movie.ageRating}
                                    </span>
                                    
                                    <span class="badge-status-top-right">
                                        <i class="fa-solid fa-calendar-days text-info me-1"></i> Sắp Chiếu
                                    </span>

                                    <!-- Hover Overlay -->
                                    <div class="movie-hover-overlay">
                                        <c:if test="${not empty movie.trailerUrl}">
                                            <button type="button" class="btn-play-trailer" data-trailer-url="${movie.trailerUrl}" data-movie-title="${movie.title}" title="Xem Trailer">
                                                <i class="fa-solid fa-play ms-1"></i>
                                            </button>
                                        </c:if>
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}" class="btn btn-cinema-outline btn-sm w-100 mt-2">
                                            <i class="fa-solid fa-circle-info"></i> Thông Tin Phim
                                        </a>
                                    </div>
                                </div>

                                <!-- Movie Info -->
                                <div class="movie-card-body">
                                    <h3 class="movie-card-title" title="${movie.title}">
                                        <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}">${movie.title}</a>
                                    </h3>
                                    <div class="movie-card-genres">
                                        <i class="fa-solid fa-film me-1 text-warning"></i> ${movie.genreString}
                                    </div>
                                    <div class="movie-card-footer">
                                        <span><i class="fa-regular fa-clock me-1"></i> ${movie.duration} phút</span>
                                        <span class="text-info small fw-bold">
                                            <i class="fa-regular fa-calendar-check me-1"></i> ${movie.formattedReleaseDate}
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise>
                <div class="text-center py-5 text-muted">
                    <i class="fa-solid fa-calendar-xmark fs-1 mb-3"></i>
                    <p>Hiện chưa có thông tin phim sắp chiếu.</p>
                </div>
            </c:otherwise>
        </c:choose>
    </section>

    <!-- ============================================================ -->
    <!-- 5. TRẢI NGHIỆM ĐIỆN ẢNH ĐỈNH CAO (IMAX & DOLBY ATMOS)        -->
    <!-- ============================================================ -->
    <section class="section-cinema mb-5">
        <div class="section-header-box">
            <div>
                <h2 class="section-title-cinema">Trải Nghiệm Điện Ảnh Đẳng Cấp</h2>
                <p class="section-subtitle-cinema">Hệ thống phòng chiếu chuẩn quốc tế hiện đại bậc nhất tại CineMax</p>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-md-4">
                <div class="experience-card">
                    <i class="fa-solid fa-vr-cardboard experience-icon"></i>
                    <h3 class="experience-title">IMAX Laser 3D</h3>
                    <p class="experience-desc">Màn hình cong khổng lồ, công nghệ chiếu Laser sắc nét gấp 4 lần và âm thanh vòm sống động chân thực đến từng chi tiết.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="experience-card">
                    <i class="fa-solid fa-volume-high experience-icon text-danger"></i>
                    <h3 class="experience-title">Dolby Atmos Audio</h3>
                    <p class="experience-desc">Hệ thống âm thanh đa chiều 360 độ độc quyền giúp khán giả đắm chìm vào từng thước phim như đang ở tâm điểm sự kiện.</p>
                </div>
            </div>
            <div class="col-md-4">
                <div class="experience-card">
                    <i class="fa-solid fa-couch experience-icon text-warning"></i>
                    <h3 class="experience-title">VIP Sweetbox & Gold</h3>
                    <p class="experience-desc">Ghế sofa đôi bọc da cao cấp êm ái, vách ngăn riêng tư và dịch vụ bắp nước phục vụ tận chỗ chuẩn 5 sao.</p>
                </div>
            </div>
        </div>
    </section>

</main>

<jsp:include page="../../common/footer.jsp" />
