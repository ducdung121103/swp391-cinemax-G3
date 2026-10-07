<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="pageTitle" value="MBCinema - Danh Sách Phim | Tìm Kiếm & Khám Phá" />
<jsp:include page="/views/common/header.jsp" />

<div class="container py-4">

    <!-- Page Title & Breadcrumb -->
    <div class="d-flex flex-wrap justify-content-between align-items-center mb-4">
        <div>
            <h1 class="h3 fw-bold text-white mb-1">
                <c:choose>
                    <c:when test="${type == 'now_showing'}"><i class="fa-solid fa-fire text-danger me-2"></i>Phim Đang Chiếu</c:when>
                    <c:when test="${type == 'coming_soon'}"><i class="fa-solid fa-calendar-days text-info me-2"></i>Phim Sắp Chiếu</c:when>
                    <c:otherwise><i class="fa-solid fa-film text-warning me-2"></i>Tất Cả Phim</c:otherwise>
                </c:choose>
            </h1>
            <p class="text-secondary small mb-0">Khám phá các tác phẩm điện ảnh xuất sắc nhất tại hệ thống cụm rạp MBCinema</p>
        </div>
        <div class="text-secondary small">
            Tìm thấy <strong class="text-warning">${totalCount}</strong> bộ phim phù hợp
        </div>
    </div>

    <!-- ============================================================ -->
    <!-- FILTER & SEARCH TOOLBAR                                      -->
    <!-- ============================================================ -->
    <div class="filter-card">
        <!-- 1. Tabs chuyển loại phim -->
        <div class="type-tab-group">
            <a href="${pageContext.request.contextPath}/movies?type=all&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}" 
               class="type-tab-btn ${empty type || type == 'all' ? 'active' : ''}">
                <i class="fa-solid fa-border-all me-1"></i> Tất Cả Phim
            </a>
            <a href="${pageContext.request.contextPath}/movies?type=now_showing&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}" 
               class="type-tab-btn ${type == 'now_showing' ? 'active' : ''}">
                <i class="fa-solid fa-fire me-1 text-danger"></i> Đang Chiếu
            </a>
            <a href="${pageContext.request.contextPath}/movies?type=coming_soon&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}" 
               class="type-tab-btn ${type == 'coming_soon' ? 'active' : ''}">
                <i class="fa-solid fa-calendar-day me-1 text-info"></i> Sắp Chiếu
            </a>
        </div>

        <!-- 2. Form Lọc Chi Tiết -->
        <form action="${pageContext.request.contextPath}/movies" method="GET" class="row g-3 align-items-end">
            <input type="hidden" name="type" value="${type}" />

            <!-- Keyword Search -->
            <div class="col-lg-4 col-md-6">
                <label class="form-label text-secondary small fw-semibold">Từ khóa tìm kiếm</label>
                <div class="input-group">
                    <span class="input-group-text bg-dark border-secondary text-secondary">
                        <i class="fa-solid fa-magnifying-glass"></i>
                    </span>
                    <input type="text" name="q" class="form-control form-input-cinema" placeholder="Tên phim, diễn viên, đạo diễn..." value="${keyword}">
                </div>
            </div>

            <!-- Genre Filter -->
            <div class="col-lg-3 col-md-6">
                <label class="form-label text-secondary small fw-semibold">Thể loại phim</label>
                <select name="genreId" class="form-select form-select-cinema auto-submit-filter">
                    <option value="">-- Tất cả thể loại --</option>
                    <c:forEach var="g" items="${genres}">
                        <option value="${g.genreId}" ${selectedGenreId == g.genreId ? 'selected' : ''}>
                            ${g.genreName}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <!-- Age Rating Filter -->
            <div class="col-lg-2 col-md-6">
                <label class="form-label text-secondary small fw-semibold">Độ tuổi</label>
                <select name="ageRating" class="form-select form-select-cinema auto-submit-filter">
                    <option value="">-- Tất cả lứa tuổi --</option>
                    <option value="P" ${selectedAgeRating == 'P' ? 'selected' : ''}>P - Mọi lứa tuổi</option>
                    <option value="K" ${selectedAgeRating == 'K' ? 'selected' : ''}>K - Dưới 13 có người lớn</option>
                    <option value="T13" ${selectedAgeRating == 'T13' ? 'selected' : ''}>T13 - Khán giả từ 13 tuổi</option>
                    <option value="T16" ${selectedAgeRating == 'T16' ? 'selected' : ''}>T16 - Khán giả từ 16 tuổi</option>
                    <option value="T18" ${selectedAgeRating == 'T18' ? 'selected' : ''}>T18 - Khán giả từ 18 tuổi</option>
                </select>
            </div>

            <!-- Sort By -->
            <div class="col-lg-2 col-md-6">
                <label class="form-label text-secondary small fw-semibold">Sắp xếp theo</label>
                <select name="sort" class="form-select form-select-cinema auto-submit-filter">
                    <option value="release_desc" ${selectedSort == 'release_desc' ? 'selected' : ''}>Mới nhất</option>
                    <option value="release_asc" ${selectedSort == 'release_asc' ? 'selected' : ''}>Ngày chiếu sớm nhất</option>
                    <option value="rating_desc" ${selectedSort == 'rating_desc' ? 'selected' : ''}>Đánh giá cao nhất</option>
                    <option value="title_asc" ${selectedSort == 'title_asc' ? 'selected' : ''}>Tên phim (A - Z)</option>
                    <option value="duration_desc" ${selectedSort == 'duration_desc' ? 'selected' : ''}>Thời lượng dài nhất</option>
                </select>
            </div>

            <!-- Action Buttons -->
            <div class="col-lg-1 col-md-12 d-flex gap-2">
                <button type="submit" class="btn btn-cinema-primary w-100 justify-content-center p-2" title="Áp dụng bộ lọc">
                    <i class="fa-solid fa-filter"></i>
                </button>
                <a href="${pageContext.request.contextPath}/movies?type=${type}" class="btn btn-secondary w-100 justify-content-center p-2 rounded-pill" title="Đặt lại bộ lọc">
                    <i class="fa-solid fa-rotate-left"></i>
                </a>
            </div>
        </form>
    </div>

    <!-- ============================================================ -->
    <!-- MOVIE GRID                                                   -->
    <!-- ============================================================ -->
    <c:choose>
        <c:when test="${not empty movies}">
            <div class="row g-4 mb-5">
                <c:forEach var="m" items="${movies}">
                    <div class="col-xl-3 col-lg-4 col-md-6 col-sm-6">
                        <div class="movie-card">
                            <!-- Poster -->
                            <div class="movie-poster-wrap">
                                <img src="${m.posterUrl}" alt="${m.title}" class="movie-poster-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80'">

                                <!-- Status Badge -->
                                <c:choose>
                                    <c:when test="${m.nowShowing}">
                                        <span class="badge-status-top-left badge-tag badge-now-showing">
                                            <i class="fa-solid fa-circle-play me-1"></i> Đang Chiếu
                                        </span>
                                    </c:when>
                                    <c:when test="${m.comingSoon}">
                                        <span class="badge-status-top-left badge-tag badge-coming-soon">
                                            <i class="fa-solid fa-calendar-day me-1"></i> Sắp Chiếu
                                        </span>
                                    </c:when>
                                </c:choose>

                                <!-- Age Badge -->
                                <span class="badge-age-top-right badge-tag ${m.ageBadgeClass}">
                                    ${m.ageRating}
                                </span>

                                <!-- Overlay -->
                                <div class="poster-hover-overlay">
                                    <a href="${pageContext.request.contextPath}/movie-detail?id=${m.movieId}" class="btn btn-cinema-primary btn-sm w-100">
                                        <i class="fa-solid fa-circle-info"></i> Xem Chi Tiết
                                    </a>
                                    <c:if test="${not empty m.trailerUrl}">
                                        <button type="button" class="btn btn-cinema-outline btn-sm w-100" data-trailer-url="${m.trailerUrl}" data-movie-title="${m.title}">
                                            <i class="fa-solid fa-play"></i> Xem Trailer
                                        </button>
                                    </c:if>
                                </div>
                            </div>

                            <!-- Body -->
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
                                    <i class="fa-regular fa-calendar me-1"></i> 
                                    <c:choose>
                                        <c:when test="${m.comingSoon}">
                                            <span class="text-info">Dự kiến: ${m.formattedReleaseDate}</span>
                                        </c:when>
                                        <c:otherwise>
                                            Khởi chiếu: ${m.formattedReleaseDate}
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>

            <!-- ============================================================ -->
            <!-- PAGINATION                                                   -->
            <!-- ============================================================ -->
            <c:if test="${totalPages > 1}">
                <nav aria-label="Page navigation" class="d-flex justify-content-center mb-5">
                    <ul class="pagination pagination-cinema">
                        <!-- Nút Trang Trước -->
                        <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/movies?type=${type}&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}&page=${currentPage - 1}">
                                <i class="fa-solid fa-chevron-left"></i>
                            </a>
                        </li>

                        <!-- Các trang -->
                        <c:forEach begin="1" end="${totalPages}" var="i">
                            <li class="page-item ${currentPage == i ? 'active' : ''}">
                                <a class="page-link" href="${pageContext.request.contextPath}/movies?type=${type}&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}&page=${i}">
                                    ${i}
                                </a>
                            </li>
                        </c:forEach>

                        <!-- Nút Trang Sau -->
                        <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/movies?type=${type}&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}&page=${currentPage + 1}">
                                <i class="fa-solid fa-chevron-right"></i>
                            </a>
                        </li>
                    </ul>
                </nav>
            </c:if>
        </c:when>
        <c:otherwise>
            <!-- Trạng thái trống không có kết quả -->
            <div class="text-center py-5 my-4 border border-secondary rounded-4 bg-dark">
                <i class="fa-solid fa-film text-secondary display-3 mb-3"></i>
                <h4 class="text-white fw-bold">Không tìm thấy bộ phim nào phù hợp</h4>
                <p class="text-secondary mb-4">Vui lòng thử tìm kiếm với từ khóa khác hoặc điều chỉnh lại bộ lọc thể loại / độ tuổi.</p>
                <a href="${pageContext.request.contextPath}/movies" class="btn btn-cinema-primary">
                    <i class="fa-solid fa-rotate-left me-1"></i> Xóa Bộ Lọc & Xem Tất Cả
                </a>
            </div>
        </c:otherwise>
    </c:choose>

</div>

<jsp:include page="/views/common/footer.jsp" />
