<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
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

                <c:if test="${not empty selectedShowtime}">
                    <div class="p-3 rounded-3 mb-2" style="background:#141820;border:1px solid #2E3550;">
                        <div class="text-secondary small">Suất đã chọn</div>
                        <div class="text-white fw-bold mt-1">
                            ${selectedShowtime.screeningRoom.cinema.name} · ${selectedShowtime.screeningRoom.name}
                        </div>
                        <div style="color:#F5C518;">${showtimeText} · ${selectedShowtime.experienceFormat}</div>
                    </div>
                </c:if>

                <!-- Action CTA Buttons -->
                <div class="d-flex flex-wrap gap-3 mt-4">
                    <c:choose>
                        <c:when test="${not empty selectedShowtime}">
                            <a href="${pageContext.request.contextPath}/booking/seats?showtimeId=${selectedShowtime.id}" class="btn btn-cinema-primary btn-lg">
                                <i class="fa-solid fa-chair me-2"></i> Chọn ghế
                            </a>
                        </c:when>
                        <c:when test="${movie.nowShowing}">
                            <a href="${pageContext.request.contextPath}/showtimes?movieId=${movie.id}" class="btn btn-cinema-primary btn-lg">
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
                    
                    <c:choose>
                        <c:when test="${not empty selectedShowtime}">
                            <a href="${pageContext.request.contextPath}/showtimes?movieId=${movie.id}" class="btn btn-outline-secondary btn-lg">
                                <i class="fa-solid fa-arrow-left me-1"></i> Quay Lại
                            </a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/movies" class="btn btn-outline-secondary btn-lg">
                                <i class="fa-solid fa-arrow-left me-1"></i> Quay Lại
                            </a>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="container py-5 border-top border-secondary">
    <h3 class="fw-bold text-white mb-3">
        <i class="fa-solid fa-star text-warning me-2"></i>Danh Gia Khach Hang
    </h3>

    <div class="mb-4 p-3 rounded" style="background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08);">
        <div class="d-flex flex-wrap gap-4 align-items-center">
            <div>
                <span class="text-secondary small">Diem trung binh</span>
                <div class="text-warning fs-4 fw-bold">
                    <fmt:formatNumber value="${reviewSummary.averageRating}" type="number" maxFractionDigits="1" minFractionDigits="1" /> / 5
                </div>
            </div>
            <div>
                <span class="text-secondary small">Tong danh gia da duyet</span>
                <div class="text-white fs-5 fw-semibold">${reviewSummary.totalReviews}</div>
            </div>
        </div>
    </div>

    <c:if test="${not empty param.reviewMsg}">
        <div class="alert alert-success"><c:out value="${param.reviewMsg}" /></div>
    </c:if>
    <c:if test="${not empty param.reviewErr}">
        <div class="alert alert-danger"><c:out value="${param.reviewErr}" /></div>
    </c:if>

    <c:if test="${not empty sessionScope.currentUser and sessionScope.currentUser.role.roleName == 'CUSTOMER'}">
        <div class="mb-4 p-3 rounded" style="background: rgba(255,255,255,0.04); border: 1px solid rgba(255,255,255,0.08);">
            <h5 class="text-white mb-3">Gui danh gia cua ban</h5>
            <c:choose>
                <c:when test="${not empty userReview}">
                    <p class="mb-2 text-secondary">Ban da gui danh gia cho phim nay.</p>
                    <p class="mb-0"><span class="badge bg-info text-dark">Trang thai: <c:out value="${userReview.status}" /></span></p>
                </c:when>
                <c:otherwise>
                    <form method="post" action="${pageContext.request.contextPath}/customer/reviews/create">
                        <input type="hidden" name="movieId" value="${movie.id}" />
                        <div class="mb-3">
                            <label for="rating" class="form-label text-secondary">So sao</label>
                            <select id="rating" name="rating" class="form-select" required>
                                <option value="">-- Chon danh gia --</option>
                                <option value="5">5 sao</option>
                                <option value="4">4 sao</option>
                                <option value="3">3 sao</option>
                                <option value="2">2 sao</option>
                                <option value="1">1 sao</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="comment" class="form-label text-secondary">Noi dung danh gia</label>
                            <textarea id="comment" name="comment" class="form-control" rows="4" required placeholder="Hay chia se cam nhan cua ban sau khi xem phim..."></textarea>
                        </div>
                        <button type="submit" class="btn btn-cinema-primary">
                            <i class="fa-solid fa-paper-plane me-1"></i>Gui danh gia
                        </button>
                    </form>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <div class="d-flex flex-column gap-3">
        <c:forEach items="${approvedReviews}" var="review">
            <div class="p-3 rounded" style="background: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.07);">
                <div class="d-flex justify-content-between align-items-start mb-2">
                    <div>
                        <div class="fw-semibold text-white"><c:out value="${review.customerName}" /></div>
                        <div class="text-warning">
                            <c:forEach begin="1" end="5" var="star">
                                <i class="fa${star <= review.ratingScore ? 's' : 'r'} fa-star"></i>
                            </c:forEach>
                        </div>
                    </div>
                    <small class="text-secondary"><c:out value="${review.createdAt}" /></small>
                </div>
                <p class="mb-0 text-secondary"><c:out value="${review.comment}" /></p>
            </div>
        </c:forEach>

        <c:if test="${empty approvedReviews}">
            <div class="text-secondary">Phim nay chua co danh gia da duyet.</div>
        </c:if>
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
