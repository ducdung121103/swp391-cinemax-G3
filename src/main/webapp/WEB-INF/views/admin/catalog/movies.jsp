<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Danh Mục Phim - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .filter-bar {
            display: flex;
            gap: 12px;
            align-items: center;
            flex-wrap: wrap;
            margin-bottom: 20px;
            background: #fff;
            padding: 16px 20px;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }
        .filter-input {
            padding: 8px 12px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.9rem;
            min-width: 240px;
        }
        .filter-select {
            padding: 8px 12px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.9rem;
            background-color: #fff;
        }
        .btn-primary {
            background: #e50914;
            color: #fff;
            padding: 9px 18px;
            border: none;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
            transition: background 0.2s;
        }
        .btn-primary:hover { background: #b20710; }
        .btn-secondary {
            background: #4a5568;
            color: #fff;
            padding: 8px 14px;
            border: none;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
        }
        .btn-secondary:hover { background: #2d3748; }
        .btn-sm {
            padding: 5px 10px;
            font-size: 0.8rem;
            border-radius: 4px;
            cursor: pointer;
            border: none;
            text-decoration: none;
            display: inline-block;
        }
        .badge {
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 0.78rem;
            font-weight: 600;
            display: inline-block;
            text-align: center;
        }
        .badge-p { background: #e2fbe8; color: #166534; border: 1px solid #bbf7d0; }
        .badge-k { background: #e0f2fe; color: #0369a1; border: 1px solid #bae6fd; }
        .badge-t13 { background: #fef9c3; color: #854d0e; border: 1px solid #fef08a; }
        .badge-t16 { background: #ffedd5; color: #9a3412; border: 1px solid #fed7aa; }
        .badge-t18 { background: #fee2e2; color: #991b1b; border: 1px solid #fecaca; }
        .badge-c { background: #f3f4f6; color: #374151; border: 1px solid #d1d5db; }

        .status-badge {
            padding: 4px 8px;
            border-radius: 4px;
            font-size: 0.8rem;
            font-weight: 600;
            display: inline-block;
        }
        .status-now-showing { background: #c6f6d5; color: #22543d; }
        .status-coming-soon { background: #fefcbf; color: #744210; }
        .status-ended { background: #edf2f7; color: #4a5568; }

        .movie-thumb {
            width: 48px;
            height: 68px;
            object-fit: cover;
            border-radius: 4px;
            box-shadow: 0 1px 3px rgba(0,0,0,0.15);
            background: #e2e8f0;
            display: block;
        }
        .genre-pill {
            display: inline-block;
            background: #f1f5f9;
            color: #475569;
            padding: 2px 7px;
            border-radius: 4px;
            font-size: 0.78rem;
            margin: 2px 2px 2px 0;
            border: 1px solid #e2e8f0;
        }

        /* Modal styling */
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0, 0, 0, 0.6);
            z-index: 999;
            align-items: center;
            justify-content: center;
            padding: 20px;
        }
        .modal-overlay.active { display: flex; }
        .modal-content {
            background: #fff;
            width: 100%;
            max-width: 780px;
            border-radius: 10px;
            padding: 24px;
            box-shadow: 0 10px 30px rgba(0,0,0,0.25);
            max-height: 90vh;
            overflow-y: auto;
        }
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 18px;
            border-bottom: 1px solid #e2e8f0;
            padding-bottom: 12px;
        }
        .modal-header h3 { margin: 0; color: #1a202c; font-size: 1.3rem; }
        .close-modal { font-size: 1.5rem; cursor: pointer; color: #a0aec0; border: none; background: none; }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 0.85rem; color: #4a5568; }
        .form-control {
            width: 100%;
            padding: 9px 12px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.9rem;
            box-sizing: border-box;
        }
        .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
        .form-row-3 { display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 14px; }

        .genres-checkbox-container {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
            gap: 8px;
            max-height: 130px;
            overflow-y: auto;
            padding: 10px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            background: #f8fafc;
        }
        .genre-checkbox-item {
            display: flex;
            align-items: center;
            gap: 6px;
            font-size: 0.85rem;
            cursor: pointer;
        }

        .alert-toast {
            padding: 12px 18px;
            border-radius: 6px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .alert-success { background: #c6f6d5; color: #22543d; border-left: 5px solid #38a169; }
        .alert-error { background: #fed7d7; color: #742a2a; border-left: 5px solid #e53e3e; }
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../../common/navbar.jsp" />
            <div class="admin-content">

                <!-- Alert Message -->
                <c:if test="${not empty msg}">
                    <div class="alert-toast alert-success">
                        <span>
                            <i class="fa-solid fa-circle-check" style="margin-right: 6px;"></i>
                            <c:choose>
                                <c:when test="${msg == 'created'}">Đã thêm mới phim vào danh mục thành công!</c:when>
                                <c:when test="${msg == 'updated'}">Đã cập nhật thông tin phim thành công!</c:when>
                                <c:when test="${msg == 'status_updated'}">Đã cập nhật trạng thái phim thành công!</c:when>
                                <c:when test="${msg == 'deleted'}">Đã ngưng chiếu và xóa phim khỏi danh mục!</c:when>
                                <c:otherwise>Thao tác thành công!</c:otherwise>
                            </c:choose>
                        </span>
                        <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem;">&times;</button>
                    </div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert-toast alert-error">
                        <span>
                            <i class="fa-solid fa-triangle-exclamation" style="margin-right: 6px;"></i>
                            ${error}
                        </span>
                        <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem;">&times;</button>
                    </div>
                </c:if>

                <!-- Thanh công cụ Tìm kiếm, Lọc và Thêm mới -->
                <form method="GET" action="${pageContext.request.contextPath}/admin/catalog/movies" class="filter-bar">
                    <input type="text" name="keyword" class="filter-input" placeholder="Tìm theo tên phim, đạo diễn, diễn viên..." value="${keyword}">
                    
                    <select name="genreId" class="filter-select">
                        <option value="ALL">-- Tất cả thể loại --</option>
                        <c:forEach items="${genres}" var="g">
                            <option value="${g.id}" ${selectedGenreId == g.id ? 'selected' : ''}>${g.name}</option>
                        </c:forEach>
                    </select>

                    <select name="status" class="filter-select">
                        <option value="ALL">-- Tất cả trạng thái --</option>
                        <option value="NOW_SHOWING" ${selectedStatus == 'NOW_SHOWING' ? 'selected' : ''}>🎬 Đang chiếu</option>
                        <option value="COMING_SOON" ${selectedStatus == 'COMING_SOON' ? 'selected' : ''}>⏳ Sắp chiếu</option>
                        <option value="ENDED" ${selectedStatus == 'ENDED' ? 'selected' : ''}>🛑 Ngưng chiếu</option>
                    </select>

                    <select name="ageRating" class="filter-select">
                        <option value="ALL">-- Tất cả độ tuổi --</option>
                        <option value="P" ${selectedAgeRating == 'P' ? 'selected' : ''}>P - Mọi lứa tuổi</option>
                        <option value="K" ${selectedAgeRating == 'K' ? 'selected' : ''}>K - Dưới 13 tuổi có bảo hộ</option>
                        <option value="T13" ${selectedAgeRating == 'T13' ? 'selected' : ''}>T13 - Khán giả từ 13+</option>
                        <option value="T16" ${selectedAgeRating == 'T16' ? 'selected' : ''}>T16 - Khán giả từ 16+</option>
                        <option value="T18" ${selectedAgeRating == 'T18' ? 'selected' : ''}>T18 - Khán giả từ 18+</option>
                        <option value="C" ${selectedAgeRating == 'C' ? 'selected' : ''}>C - Phim bị cấm phổ biến</option>
                    </select>

                    <button type="submit" class="btn-secondary">
                        <i class="fa-solid fa-filter"></i> Lọc
                    </button>
                    <a href="${pageContext.request.contextPath}/admin/catalog/movies" class="btn-secondary" style="background:#718096;">
                        <i class="fa-solid fa-rotate-left"></i> Đặt lại
                    </a>

                    <div style="margin-left: auto;">
                        <button type="button" class="btn-primary" onclick="openCreateModal()">
                            <i class="fa-solid fa-plus"></i> Thêm Phim Mới
                        </button>
                    </div>
                </form>

                <!-- Danh sách Phim -->
                <div class="admin-card">
                    <div class="admin-card-header">
                        <h3 class="admin-card-title">
                            <i class="fa-solid fa-film"></i>
                            Quản lý Danh Mục Phim Hệ Thống (M-02.1)
                        </h3>
                        <span style="font-size: 0.85rem; color: #64748b; font-weight: 600;">Tổng số: <strong>${movies.size()}</strong> phim</span>
                    </div>

                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="width: 60px; text-align: center;">Poster</th>
                                    <th>Tên Phim & Tên Gốc</th>
                                    <th style="width: 170px;">Thể Loại</th>
                                    <th style="width: 100px;">Thời Lượng</th>
                                    <th style="width: 80px; text-align: center;">Độ Tuổi</th>
                                    <th style="width: 140px;">Lịch Chiếu</th>
                                    <th style="width: 145px; text-align: center;">Trạng Thái</th>
                                    <th style="width: 170px; text-align: center;">Thao Tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${movies}" var="m">
                                    <tr>
                                        <td style="text-align: center;">
                                            <c:choose>
                                                <c:when test="${not empty m.posterUrl}">
                                                    <img src="${m.posterUrl}" alt="${m.title}" class="movie-thumb"
                                                         onerror="this.style.display='none'; this.nextElementSibling.style.display='flex';">
                                                    <div style="display:none; width:48px; height:68px; background:#e2e8f0; border-radius:4px; align-items:center; justify-content:center; color:#94a3b8;">
                                                        <i class="fa-solid fa-image"></i>
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <div style="width:48px; height:68px; background:#e2e8f0; border-radius:4px; display:flex; align-items:center; justify-content:center; color:#94a3b8;">
                                                        <i class="fa-solid fa-film"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <div style="font-weight: 700; color: #0f172a; font-size: 0.95rem;">
                                                ${m.title}
                                            </div>
                                            <c:if test="${not empty m.originalTitle}">
                                                <div style="font-size: 0.82rem; color: #64748b; font-style: italic;">
                                                    ${m.originalTitle}
                                                </div>
                                            </c:if>
                                            <c:if test="${not empty m.director}">
                                                <div style="font-size: 0.8rem; color: #475569; margin-top: 2px;">
                                                    <span style="color: #94a3b8;">ĐD:</span> ${m.director}
                                                </div>
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty m.genres}">
                                                    <c:forEach items="${m.genres}" var="g">
                                                        <span class="genre-pill">${g.name}</span>
                                                    </c:forEach>
                                                </c:when>
                                                <c:otherwise>
                                                    <span style="color: #94a3b8; font-size: 0.82rem;">Chưa cập nhật</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="font-weight: 600; color: #334155; font-size: 0.88rem;">
                                            <i class="fa-regular fa-clock" style="color: #94a3b8; margin-right: 4px;"></i>${m.durationMinutes}p
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="badge ${m.ageBadgeClass}">${m.ageRating}</span>
                                        </td>
                                        <td style="font-size: 0.82rem; color: #475569;">
                                            <div><strong style="color: #334155;">Khởi chiếu:</strong> ${m.formattedReleaseDate}</div>
                                            <c:if test="${not empty m.endDate}">
                                                <div style="color: #94a3b8; margin-top: 2px;">
                                                    <span>Kết thúc:</span> ${m.endDate}
                                                </div>
                                            </c:if>
                                        </td>
                                        <td style="text-align: center;">
                                            <form method="POST" action="${pageContext.request.contextPath}/admin/catalog/movies" style="display:inline;">
                                                <input type="hidden" name="action" value="changeStatus">
                                                <input type="hidden" name="id" value="${m.id}">
                                                <select name="status" class="filter-select btn-sm" onchange="this.form.submit()" style="font-weight:600; cursor:pointer; padding: 4px 6px;">
                                                    <option value="NOW_SHOWING" ${m.status == 'NOW_SHOWING' ? 'selected' : ''}>🎬 Đang chiếu</option>
                                                    <option value="COMING_SOON" ${m.status == 'COMING_SOON' ? 'selected' : ''}>⏳ Sắp chiếu</option>
                                                    <option value="ENDED" ${m.status == 'ENDED' ? 'selected' : ''}>🛑 Ngưng chiếu</option>
                                                </select>
                                            </form>
                                        </td>
                                        <td style="text-align: center;">
                                            <!-- Nút Sửa gắn toàn bộ thuộc tính an toàn qua data-* -->
                                            <button type="button" class="btn-sm btn-secondary"
                                                    data-id="${m.id}"
                                                    data-title="<c:out value='${m.title}'/>"
                                                    data-original-title="<c:out value='${m.originalTitle}'/>"
                                                    data-duration="${m.durationMinutes}"
                                                    data-release-date="${m.releaseDate}"
                                                    data-end-date="${m.endDate}"
                                                    data-age-rating="${m.ageRating}"
                                                    data-language="<c:out value='${m.language}'/>"
                                                    data-director="<c:out value='${m.director}'/>"
                                                    data-actors="<c:out value='${m.actors}'/>"
                                                    data-cast-members="<c:out value='${m.castMembers}'/>"
                                                    data-synopsis="<c:out value='${m.synopsis}'/>"
                                                    data-poster-url="<c:out value='${m.posterUrl}'/>"
                                                    data-trailer-url="<c:out value='${m.trailerUrl}'/>"
                                                    data-status="${m.status}"
                                                    data-genres="${m.genreIdsString}"
                                                    onclick="openEditModalFromBtn(this)">
                                                <i class="fa-solid fa-pen-to-square"></i> Sửa
                                            </button>

                                            <!-- Nút Xóa Mềm Phim -->
                                            <form method="POST" action="${pageContext.request.contextPath}/admin/catalog/movies" style="display:inline;" onsubmit="return confirm('Bạn có chắc muốn xóa mềm phim này? Phim sẽ không được xóa nếu đang có suất chiếu hoạt động!')">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="id" value="${m.id}">
                                                <button type="submit" class="btn-sm btn-primary" style="background:#ef4444;" title="Xóa phim">
                                                    <i class="fa-solid fa-trash"></i> Xóa
                                                </button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty movies}">
                                    <tr>
                                        <td colspan="8" style="text-align: center; color: #a0aec0; padding: 30px;">
                                            Không tìm thấy bộ phim nào phù hợp với bộ lọc!
                                        </td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <!-- MODAL THÊM PHIM MỚI -->
    <div id="createModal" class="modal-overlay">
        <div class="modal-content">
            <div class="modal-header">
                <h3><i class="fa-solid fa-film" style="color: #e50914; margin-right: 8px;"></i>Thêm Phim Mới Vào Danh Mục</h3>
                <button type="button" class="close-modal" onclick="closeCreateModal()">&times;</button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/admin/catalog/movies">
                <input type="hidden" name="action" value="create">

                <div class="form-row">
                    <div class="form-group">
                        <label>Tên Phim (Tiếng Việt) (*):</label>
                        <input type="text" name="title" class="form-control" required placeholder="VD: Mai, Đào, Phở và Piano">
                    </div>
                    <div class="form-group">
                        <label>Tên Gốc / Tiếng Anh:</label>
                        <input type="text" name="originalTitle" class="form-control" placeholder="VD: Mai (2024), Dune: Part Two">
                    </div>
                </div>

                <div class="form-row-3">
                    <div class="form-group">
                        <label>Thời Lượng (Phút) (*):</label>
                        <input type="number" name="durationMinutes" class="form-control" required min="1" max="500" value="120">
                    </div>
                    <div class="form-group">
                        <label>Phân Loại Độ Tuổi (*):</label>
                        <select name="ageRating" class="form-control" required>
                            <option value="P" selected>P - Mọi lứa tuổi</option>
                            <option value="K">K - Dưới 13 tuổi có bảo hộ</option>
                            <option value="T13">T13 - Khán giả 13+</option>
                            <option value="T16">T16 - Khán giả 16+</option>
                            <option value="T18">T18 - Khán giả 18+</option>
                            <option value="C">C - Cấm chiếu</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Trạng Thái (*):</label>
                        <select name="status" class="form-control" required>
                            <option value="COMING_SOON" selected>⏳ Sắp chiếu</option>
                            <option value="NOW_SHOWING">🎬 Đang chiếu</option>
                            <option value="ENDED">🛑 Ngưng chiếu</option>
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Ngày Khởi Chiếu (*):</label>
                        <input type="date" name="releaseDate" id="createReleaseDate" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>Ngày Kết Thúc (Dự kiến):</label>
                        <input type="date" name="endDate" id="createEndDate" class="form-control">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Đạo Diễn:</label>
                        <input type="text" name="director" class="form-control" placeholder="VD: Trấn Thành, Christopher Nolan">
                    </div>
                    <div class="form-group">
                        <label>Ngôn Ngữ / Quốc Gia:</label>
                        <input type="text" name="language" class="form-control" placeholder="VD: Tiếng Việt - Phụ đề Tiếng Anh">
                    </div>
                </div>

                <div class="form-group">
                    <label>Diễn Viên Chính:</label>
                    <input type="text" name="actors" class="form-control" placeholder="VD: Phương Anh Đào, Tuấn Trần, Uyển Ân...">
                </div>

                <div class="form-group">
                    <label>Thể Loại Phim (chọn một hoặc nhiều):</label>
                    <div class="genres-checkbox-container">
                        <c:forEach items="${genres}" var="g">
                            <label class="genre-checkbox-item">
                                <input type="checkbox" name="genreIds" value="${g.id}">
                                <span>${g.name}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Đường Dẫn Poster (URL):</label>
                        <input type="url" name="posterUrl" id="createPosterUrl" class="form-control" placeholder="https://..." oninput="updateCreatePosterPreview()">
                    </div>
                    <div class="form-group">
                        <label>Đường Dẫn Trailer Youtube (URL):</label>
                        <input type="url" name="trailerUrl" class="form-control" placeholder="https://www.youtube.com/watch?v=...">
                    </div>
                </div>

                <!-- Live Preview Ảnh Poster -->
                <div id="createPosterPreviewBox" style="display:none; margin-bottom: 14px; align-items:center; gap: 12px; background: #f8fafc; padding: 10px; border-radius: 6px;">
                    <img id="createPosterImg" src="" alt="Poster Preview" style="width: 50px; height: 75px; object-fit: cover; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.2);">
                    <span style="font-size: 0.85rem; color: #64748b;">Xem trước ảnh Poster</span>
                </div>

                <div class="form-group">
                    <label>Tóm Tắt Nội Dung Phim (Synopsis):</label>
                    <textarea name="synopsis" rows="3" class="form-control" placeholder="Nhập tóm tắt nội dung hấp dẫn của bộ phim..."></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                    <button type="button" class="btn-secondary" onclick="closeCreateModal()">Hủy bỏ</button>
                    <button type="submit" class="btn-primary">💾 Lưu Phim Mới</button>
                </div>
            </form>
        </div>
    </div>

    <!-- MODAL SỬA PHIM -->
    <div id="editModal" class="modal-overlay">
        <div class="modal-content">
            <div class="modal-header">
                <h3><i class="fa-solid fa-pen-to-square" style="color: #e50914; margin-right: 8px;"></i>Cập Nhật Thông Tin Phim</h3>
                <button type="button" class="close-modal" onclick="closeEditModal()">&times;</button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/admin/catalog/movies">
                <input type="hidden" name="action" value="update">
                <input type="hidden" id="editId" name="id" value="">

                <div class="form-row">
                    <div class="form-group">
                        <label>Tên Phim (Tiếng Việt) (*):</label>
                        <input type="text" id="editTitle" name="title" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>Tên Gốc / Tiếng Anh:</label>
                        <input type="text" id="editOriginalTitle" name="originalTitle" class="form-control">
                    </div>
                </div>

                <div class="form-row-3">
                    <div class="form-group">
                        <label>Thời Lượng (Phút) (*):</label>
                        <input type="number" id="editDurationMinutes" name="durationMinutes" class="form-control" required min="1" max="500">
                    </div>
                    <div class="form-group">
                        <label>Phân Loại Độ Tuổi (*):</label>
                        <select id="editAgeRating" name="ageRating" class="form-control" required>
                            <option value="P">P - Mọi lứa tuổi</option>
                            <option value="K">K - Dưới 13 tuổi có bảo hộ</option>
                            <option value="T13">T13 - Khán giả 13+</option>
                            <option value="T16">T16 - Khán giả 16+</option>
                            <option value="T18">T18 - Khán giả 18+</option>
                            <option value="C">C - Cấm chiếu</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label>Trạng Thái (*):</label>
                        <select id="editStatus" name="status" class="form-control" required>
                            <option value="COMING_SOON">⏳ Sắp chiếu</option>
                            <option value="NOW_SHOWING">🎬 Đang chiếu</option>
                            <option value="ENDED">🛑 Ngưng chiếu</option>
                        </select>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Ngày Khởi Chiếu (*):</label>
                        <input type="date" id="editReleaseDate" name="releaseDate" class="form-control" required>
                    </div>
                    <div class="form-group">
                        <label>Ngày Kết Thúc (Dự kiến):</label>
                        <input type="date" id="editEndDate" name="endDate" class="form-control">
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Đạo Diễn:</label>
                        <input type="text" id="editDirector" name="director" class="form-control">
                    </div>
                    <div class="form-group">
                        <label>Ngôn Ngữ / Quốc Gia:</label>
                        <input type="text" id="editLanguage" name="language" class="form-control">
                    </div>
                </div>

                <div class="form-group">
                    <label>Diễn Viên Chính:</label>
                    <input type="text" id="editActors" name="actors" class="form-control">
                </div>

                <div class="form-group">
                    <label>Thể Loại Phim (chọn một hoặc nhiều):</label>
                    <div class="genres-checkbox-container" id="editGenresContainer">
                        <c:forEach items="${genres}" var="g">
                            <label class="genre-checkbox-item">
                                <input type="checkbox" name="genreIds" value="${g.id}">
                                <span>${g.name}</span>
                            </label>
                        </c:forEach>
                    </div>
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label>Đường Dẫn Poster (URL):</label>
                        <input type="url" id="editPosterUrl" name="posterUrl" class="form-control" oninput="updateEditPosterPreview()">
                    </div>
                    <div class="form-group">
                        <label>Đường Dẫn Trailer Youtube (URL):</label>
                        <input type="url" id="editTrailerUrl" name="trailerUrl" class="form-control">
                    </div>
                </div>

                <!-- Live Preview Ảnh Poster Modal Edit -->
                <div id="editPosterPreviewBox" style="display:none; margin-bottom: 14px; align-items:center; gap: 12px; background: #f8fafc; padding: 10px; border-radius: 6px;">
                    <img id="editPosterImg" src="" alt="Poster Preview" style="width: 50px; height: 75px; object-fit: cover; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.2);">
                    <span style="font-size: 0.85rem; color: #64748b;">Xem trước ảnh Poster</span>
                </div>

                <div class="form-group">
                    <label>Tóm Tắt Nội Dung Phim (Synopsis):</label>
                    <textarea id="editSynopsis" name="synopsis" rows="3" class="form-control"></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                    <button type="button" class="btn-secondary" onclick="closeEditModal()">Hủy bỏ</button>
                    <button type="submit" class="btn-primary">💾 Cập Nhật Phim</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openCreateModal() {
            document.getElementById('createModal').classList.add('active');
        }
        function closeCreateModal() {
            document.getElementById('createModal').classList.remove('active');
        }

        function openEditModalFromBtn(btn) {
            document.getElementById('editId').value = btn.dataset.id || '';
            document.getElementById('editTitle').value = btn.dataset.title || '';
            document.getElementById('editOriginalTitle').value = btn.dataset.originalTitle || '';
            document.getElementById('editDurationMinutes').value = btn.dataset.duration || '120';
            document.getElementById('editReleaseDate').value = btn.dataset.releaseDate || '';
            document.getElementById('editEndDate').value = btn.dataset.endDate || '';
            document.getElementById('editAgeRating').value = btn.dataset.ageRating || 'P';
            document.getElementById('editStatus').value = btn.dataset.status || 'COMING_SOON';
            document.getElementById('editDirector').value = btn.dataset.director || '';
            document.getElementById('editLanguage').value = btn.dataset.language || '';
            document.getElementById('editActors').value = btn.dataset.actors || '';
            document.getElementById('editPosterUrl').value = btn.dataset.posterUrl || '';
            document.getElementById('editTrailerUrl').value = btn.dataset.trailerUrl || '';
            document.getElementById('editSynopsis').value = btn.dataset.synopsis || '';

            // Cập nhật preview ảnh poster
            updateEditPosterPreview();

            // Check các thể loại của phim
            const rawGenres = btn.dataset.genres || '';
            const genreIds = rawGenres.split(',').map(s => s.trim()).filter(Boolean);
            const checkboxes = document.querySelectorAll('#editGenresContainer input[type="checkbox"]');
            checkboxes.forEach(cb => {
                cb.checked = genreIds.includes(cb.value);
            });

            document.getElementById('editModal').classList.add('active');
        }

        function closeEditModal() {
            document.getElementById('editModal').classList.remove('active');
        }

        function updateCreatePosterPreview() {
            const url = document.getElementById('createPosterUrl').value.trim();
            const box = document.getElementById('createPosterPreviewBox');
            const img = document.getElementById('createPosterImg');
            if (url) {
                img.src = url;
                box.style.display = 'flex';
                img.onerror = function() { box.style.display = 'none'; };
            } else {
                box.style.display = 'none';
            }
        }

        function updateEditPosterPreview() {
            const url = document.getElementById('editPosterUrl').value.trim();
            const box = document.getElementById('editPosterPreviewBox');
            const img = document.getElementById('editPosterImg');
            if (url) {
                img.src = url;
                box.style.display = 'flex';
                img.onerror = function() { box.style.display = 'none'; };
            } else {
                box.style.display = 'none';
            }
        }

        window.onclick = function(event) {
            if (event.target.classList.contains('modal-overlay')) {
                event.target.classList.remove('active');
            }
        };
    </script>
</body>
</html>
