<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="${movie.title} - Chi Tiết Phim | MBCinema" />
<jsp:include page="/views/common/header.jsp" />

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

                <h1 class="detail-title">${movie.title}</h1>

                <div class="row g-3 mb-4">
                    <div class="col-sm-6">
                        <div class="detail-meta-item">
                            <strong><i class="fa-regular fa-clock me-2 text-warning"></i>Thời lượng:</strong>
                            <span>${movie.duration} phút</span>
                        </div>
                        <div class="detail-meta-item">
                            <strong><i class="fa-regular fa-calendar-check me-2 text-warning"></i>Khởi chiếu:</strong>
                            <span>${movie.formattedReleaseDate}</span>
                        </div>
                        <div class="detail-meta-item">
                            <strong><i class="fa-solid fa-film me-2 text-warning"></i>Thể loại:</strong>
                            <span class="text-warning">${movie.genreString}</span>
                        </div>
                    </div>
                    <div class="col-sm-6">
                        <div class="detail-meta-item">
                            <strong><i class="fa-solid fa-user-tie me-2 text-warning"></i>Đạo diễn:</strong>
                            <span>${not empty movie.director ? movie.director : 'Đang cập nhật'}</span>
                        </div>
                        <div class="detail-meta-item">
                            <strong><i class="fa-solid fa-users me-2 text-warning"></i>Diễn viên:</strong>
                            <span>${not empty movie.cast ? movie.cast : 'Đang cập nhật'}</span>
                        </div>
                        <div class="detail-meta-item">
                            <strong><i class="fa-solid fa-circle-exclamation me-2 text-warning"></i>Giới hạn tuổi:</strong>
                            <span>Phân loại ${movie.ageRating}</span>
                        </div>
                    </div>
                </div>

                <!-- Action buttons -->
                <div class="d-flex flex-wrap gap-3 mb-4">
                    <c:choose>
                        <c:when test="${movie.nowShowing}">
                            <a href="#" class="btn btn-cinema-primary btn-lg">
                                <i class="fa-solid fa-ticket"></i> Mua Vé Ngay
                            </a>
                        </c:when>
                        <c:otherwise>
                            <button type="button" class="btn btn-primary btn-lg rounded-pill" onclick="alert('Cảm ơn bạn đã quan tâm! Chúng tôi sẽ thông báo lịch chiếu sớm nhất.')">
                                <i class="fa-solid fa-bell me-1"></i> Nhắc Tôi Khi Có Vé
                            </button>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty movie.trailerUrl}">
                        <button type="button" class="btn btn-cinema-outline btn-lg" data-trailer-url="${movie.trailerUrl}" data-movie-title="${movie.title}">
                            <i class="fa-solid fa-play"></i> Xem Trailer
                        </button>
                    </c:if>

                    <a href="${pageContext.request.contextPath}/movies" class="btn btn-secondary btn-lg rounded-pill">
                        <i class="fa-solid fa-arrow-left me-1"></i> Quay lại
                    </a>
                </div>

                <!-- Synopsis -->
                <h5 class="text-white fw-bold mt-4"><i class="fa-solid fa-align-left text-warning me-2"></i>Nội dung phim</h5>
                <div class="detail-desc-box">
                    ${movie.description}
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Trailer Video Section (nếu có) -->
<c:if test="${not empty movie.embedTrailerUrl}">
    <div class="container mb-5">
        <div class="section-header-wrap">
            <h3 class="section-title">Official Trailer</h3>
        </div>
        <div class="ratio ratio-21x9 rounded-4 overflow-hidden border border-secondary shadow-lg">
            <iframe src="${movie.embedTrailerUrl}" title="Trailer ${movie.title}" allowfullscreen allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"></iframe>
        </div>
    </div>
</c:if>

<!-- Related Movies -->
<c:if test="${not empty relatedMovies}">
    <div class="container mb-5">
        <div class="section-header-wrap">
            <h3 class="section-title">Phim Cùng Thể Loại</h3>
            <a href="${pageContext.request.contextPath}/movies" class="section-view-all">
                Xem thêm <i class="fa-solid fa-angle-right ms-1"></i>
            </a>
        </div>
        <div class="row g-4">
            <c:forEach var="rm" items="${relatedMovies}">
                <div class="col-xl-3 col-lg-3 col-md-6 col-sm-6">
                    <div class="movie-card">
                        <div class="movie-poster-wrap">
                            <img src="${rm.posterUrl}" alt="${rm.title}" class="movie-poster-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80'">
                            <span class="badge-age-top-right badge-tag ${rm.ageBadgeClass}">${rm.ageRating}</span>
                            <div class="poster-hover-overlay">
                                <a href="${pageContext.request.contextPath}/movie-detail?id=${rm.movieId}" class="btn btn-cinema-primary btn-sm w-100">
                                    <i class="fa-solid fa-circle-info"></i> Xem Chi Tiết
                                </a>
                            </div>
                        </div>
                        <div class="movie-card-body">
                            <a href="${pageContext.request.contextPath}/movie-detail?id=${rm.movieId}" class="movie-title">
                                ${rm.title}
                            </a>
                            <div class="movie-meta">
                                <span><i class="fa-regular fa-clock me-1"></i> ${rm.duration} phút</span>
                                <span class="ms-auto movie-rating-badge"><i class="fa-solid fa-star"></i> ${rm.rating}</span>
                            </div>
                        </div>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>
</c:if>

<jsp:include page="/views/common/footer.jsp" />
