<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="CineMax - Danh Sách Phim | Tìm Kiếm & Khám Phá Điện Ảnh" />
<jsp:include page="../../common/header.jsp" />

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
            <p class="text-secondary small mb-0">Khám phá các tác phẩm điện ảnh xuất sắc nhất tại hệ thống cụm rạp CineMax toàn quốc</p>
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
                        <option value="${g.id}" ${selectedGenreId == g.id ? 'selected' : ''}>
                            ${g.name}
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
                    <option value="title_asc" ${selectedSort == 'title_asc' ? 'selected' : ''}>Tên phim (A - Z)</option>
                    <option value="duration_desc" ${selectedSort == 'duration_desc' ? 'selected' : ''}>Thời lượng dài nhất</option>
                </select>
            </div>

            <!-- Action Buttons -->
            <div class="col-lg-1 col-md-12 d-flex gap-2">
                <button type="submit" class="btn btn-cinema-primary w-100 justify-content-center p-2" title="Áp dụng bộ lọc">
                    <i class="fa-solid fa-filter"></i>
                </button>
                <a href="${pageContext.request.contextPath}/movies?type=${type}" class="btn btn-cinema-outline p-2" title="Xóa bộ lọc">
                    <i class="fa-solid fa-rotate-left"></i>
                </a>
            </div>
        </form>
    </div>

    <!-- ============================================================ -->
    <!-- MOVIE CARDS GRID                                             -->
    <!-- ============================================================ -->
    <c:choose>
        <c:when test="${not empty movies}">
            <div class="row row-cols-2 row-cols-md-3 row-cols-lg-4 g-4 mb-5">
                <c:forEach var="movie" items="${movies}">
                    <div class="col">
                        <div class="movie-card-cinema">
                            <!-- Poster Box -->
                            <div class="movie-poster-box">
                                <img src="${movie.posterUrl}" alt="${movie.title}" class="movie-poster-img" loading="lazy" onerror="this.src='https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=500&auto=format&fit=crop&q=80'">
                                
                                <!-- Age Rating Badge -->
                                <span class="badge-age-top-left ${movie.ageBadgeClass}">
                                    ${movie.ageRating}
                                </span>

                                <!-- Status Badge -->
                                <c:choose>
                                    <c:when test="${movie.nowShowing}">
                                        <span class="badge-status-top-right">
                                            <i class="fa-solid fa-circle text-danger me-1" style="font-size: 6px;"></i> Đang Chiếu
                                        </span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge-status-top-right">
                                            <i class="fa-solid fa-calendar-days text-info me-1"></i> Sắp Chiếu
                                        </span>
                                    </c:otherwise>
                                </c:choose>

                                <!-- Hover Overlay -->
                                <div class="movie-hover-overlay">
                                    <c:if test="${not empty movie.trailerUrl}">
                                        <button type="button" class="btn-play-trailer" data-trailer-url="${movie.trailerUrl}" data-movie-title="${movie.title}" title="Xem Trailer">
                                            <i class="fa-solid fa-play ms-1"></i>
                                        </button>
                                    </c:if>
                                    <c:choose>
                                        <c:when test="${movie.nowShowing}">
                                            <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}" class="btn btn-cinema-primary btn-sm w-100 mt-2">
                                                <i class="fa-solid fa-ticket"></i> Mua Vé Ngay
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}" class="btn btn-cinema-outline btn-sm w-100 mt-2">
                                                <i class="fa-solid fa-circle-info"></i> Chi Tiết Phim
                                            </a>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            
                            <!-- Card Body -->
                            <div class="movie-card-body">
                                <h3 class="movie-card-title" title="${movie.title}">
                                    <a href="${pageContext.request.contextPath}/movie/detail?id=${movie.id}">${movie.title}</a>
                                </h3>
                                <div class="movie-card-genres">
                                    <i class="fa-solid fa-film me-1 text-warning"></i> ${movie.genreString}
                                </div>
                                <div class="movie-card-footer">
                                    <span><i class="fa-regular fa-clock me-1"></i> ${movie.duration} phút</span>
                                    <c:choose>
                                        <c:when test="${movie.nowShowing}">
                                            <span class="movie-card-rating">
                                                <i class="fa-solid fa-star"></i> ${movie.rating}
                                            </span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="text-info small fw-bold">
                                                <i class="fa-regular fa-calendar-check me-1"></i> ${movie.formattedReleaseDate}
                                            </span>
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
                <nav aria-label="Page navigation" class="d-flex justify-content-center my-4">
                    <ul class="pagination pagination-cinema">
                        <!-- Trang Trước -->
                        <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/movies?type=${type}&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}&page=${currentPage - 1}" aria-label="Previous">
                                <i class="fa-solid fa-chevron-left"></i>
                            </a>
                        </li>

                        <!-- Các trang số -->
                        <c:forEach begin="1" end="${totalPages}" var="p">
                            <c:if test="${p == 1 || p == totalPages || (p >= currentPage - 2 && p <= currentPage + 2)}">
                                <li class="page-item ${p == currentPage ? 'active' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/movies?type=${type}&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}&page=${p}">${p}</a>
                                </li>
                            </c:if>
                        </c:forEach>

                        <!-- Trang Sau -->
                        <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                            <a class="page-link" href="${pageContext.request.contextPath}/movies?type=${type}&q=${keyword}&genreId=${selectedGenreId}&ageRating=${selectedAgeRating}&sort=${selectedSort}&page=${currentPage + 1}" aria-label="Next">
                                <i class="fa-solid fa-chevron-right"></i>
                            </a>
                        </li>
                    </ul>
                </nav>
            </c:if>
        </c:when>
        
        <c:otherwise>
            <!-- EMPTY STATE -->
            <div class="empty-state">
                <i class="fa-solid fa-film-slash empty-state-icon"></i>
                <h4 class="fw-bold mb-2">Không tìm thấy bộ phim nào phù hợp!</h4>
                <p class="text-secondary mb-4">Vui lòng thử thay đổi từ khóa tìm kiếm hoặc điều chỉnh lại bộ lọc thể loại & lứa tuổi.</p>
                <a href="${pageContext.request.contextPath}/movies?type=all" class="btn btn-cinema-primary">
                    <i class="fa-solid fa-arrow-rotate-left me-1"></i> Xem Tất Cả Phim
                </a>
            </div>
        </c:otherwise>
    </c:choose>

</div>

<jsp:include page="../../common/footer.jsp" />
