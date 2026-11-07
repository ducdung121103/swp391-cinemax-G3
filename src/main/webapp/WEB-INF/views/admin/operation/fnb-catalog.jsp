<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Quản lý danh mục F&B | CineMax</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/fnb-catalog.css?v=2">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/fnb-catalog-images.css?v=2">
</head>
<body>
<c:url var="fnbFallbackUrl" value="/assets/images/fnb/default.svg" />
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp"/>
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp"/>
        <main class="admin-content">

            <!-- HEADING -->
            <header class="fnb-heading">
                <div>
                    <span class="eyebrow">VẬN HÀNH / F&B</span>
                    <h1>Quản lý danh mục F&B</h1>
                    <p>Thêm mới, tìm kiếm, chỉnh sửa, đổi trạng thái và xóa mềm món ăn, combo bắp nước.</p>
                </div>
            </header>

            <!-- THÔNG BÁO -->
            <c:if test="${param.success eq '1'}">
                <div class="notice success"><i class="fa-solid fa-circle-check me-1"></i> Thao tác thành công! Dữ liệu danh mục đã được cập nhật.</div>
            </c:if>
            <c:if test="${not empty param.error}">
                <div class="notice error"><i class="fa-solid fa-triangle-exclamation me-1"></i> <c:out value="${param.error}"/></div>
            </c:if>

            <!-- SECTION SẢN PHẨM & COMBO -->
            <section class="admin-card">
                <div class="section-title">
                    <div>
                        <h2>Sản phẩm và combo</h2>
                        <p>Thông tin món ăn & thức uống hiển thị trên toàn chuỗi rạp và quầy POS.</p>
                    </div>
                </div>

                <!-- FORM THÊM MỚI SẢN PHẨM -->
                <form method="post" action="${pageContext.request.contextPath}${fnbCatalogUrl}" class="fnb-form">
                    <input type="hidden" name="action" value="saveItem">
                    <label>
                        Mã sản phẩm (*)
                        <input name="itemCode" maxlength="30" required placeholder="VD: FNB-CB-05">
                    </label>
                    <label>
                        Tên sản phẩm (*)
                        <input name="name" maxlength="150" required placeholder="VD: Combo Bắp Phô Mai">
                    </label>
                    <label>
                        Danh mục (*)
                        <select name="categoryId" required>
                            <c:forEach items="${categories}" var="cat">
                                <c:if test="${cat.isActive}">
                                    <option value="${cat.id}"><c:out value="${cat.name}"/></option>
                                </c:if>
                            </c:forEach>
                        </select>
                    </label>
                    <label>
                        Giá bán (₫) (*)
                        <input type="number" name="price" min="0" step="1000" required placeholder="VD: 89000">
                    </label>
                    <label class="wide">
                        URL hình ảnh
                        <input name="imageUrl" maxlength="255" placeholder="/assets/images/fnb/... hoặc link ảnh online">
                    </label>
                    <label class="check-label">
                        <input type="checkbox" name="isCombo" value="true">
                        <span>Đánh dấu là Combo</span>
                    </label>
                    <button class="primary-button" type="submit">
                        <i class="fa-solid fa-plus"></i> Thêm sản phẩm
                    </button>
                </form>

                <!-- THANH TÌM KIẾM VÀ BỘ LỌC F&B -->
                <div class="fnb-filter-bar">
                    <div class="fnb-search-box">
                        <i class="fa-solid fa-magnifying-glass search-icon"></i>
                        <input type="text" id="fnbSearchInput" class="fnb-search-input"
                               placeholder="Tìm theo tên món, mã (FNB-...), danh mục..."
                               value="${fn:escapeXml(keyword)}"
                               oninput="filterFnbItems()">
                        <button type="button" class="fnb-search-clear" id="fnbSearchClear" onclick="clearFnbSearch()" title="Xóa tìm kiếm">
                            <i class="fa-solid fa-xmark"></i>
                        </button>
                    </div>
                    <div class="fnb-filter-group">
                        <select id="fnbCategoryFilter" class="fnb-filter-select" onchange="filterFnbItems()">
                            <option value="">Tất cả danh mục</option>
                            <c:forEach items="${categories}" var="cat">
                                <option value="${fn:escapeXml(cat.name)}"><c:out value="${cat.name}"/></option>
                            </c:forEach>
                        </select>
                        <select id="fnbStatusFilter" class="fnb-filter-select" onchange="filterFnbItems()">
                            <option value="">Tất cả trạng thái</option>
                            <option value="on">Đang bán</option>
                            <option value="off">Đã ngừng</option>
                        </select>
                    </div>
                    <div class="fnb-count-badge" id="fnbCountBadge">
                        Hiển thị: <strong><span id="fnbVisibleCount">${fn:length(items)}</span>/${fn:length(items)}</strong> sản phẩm
                    </div>
                </div>

                <!-- BẢNG DANH SÁCH SẢN PHẨM -->
                <div class="table-scroll">
                    <table class="fnb-table" id="fnbTable">
                        <thead>
                        <tr>
                            <th style="min-width: 240px;">Sản phẩm</th>
                            <th>Danh mục</th>
                            <th>Giá</th>
                            <th>Loại</th>
                            <th>Trạng thái</th>
                            <th class="actions-cell">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody id="fnbTableBody">
                        <c:forEach items="${items}" var="item">
                            <tr class="fnb-row"
                                data-name="${fn:escapeXml(item.name)}"
                                data-code="${fn:escapeXml(item.itemCode)}"
                                data-category="${fn:escapeXml(item.category.name)}"
                                data-status="${item.isActive ? 'on' : 'off'}">
                                <td>
                                    <div class="fnb-product-cell">
                                        <c:choose>
                                            <c:when test="${empty item.imageUrl}"><c:set var="itemImageUrl" value="${fnbFallbackUrl}" /></c:when>
                                            <c:when test="${fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), 'http://') or fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), 'https://')}"><c:set var="itemImageUrl" value="${fn:trim(item.imageUrl)}" /></c:when>
                                            <c:when test="${fn:startsWith(fn:toLowerCase(fn:trim(item.imageUrl)), '/assets/images/fnb/') and fn:endsWith(fn:toLowerCase(fn:trim(item.imageUrl)), '.png')}"><c:url var="itemImageUrl" value="${fn:replace(fn:trim(item.imageUrl), '.png', '.svg')}" /></c:when>
                                            <c:when test="${fn:startsWith(fn:trim(item.imageUrl), '/')}"><c:url var="itemImageUrl" value="${fn:trim(item.imageUrl)}" /></c:when>
                                            <c:otherwise><c:set var="relativeItemImageUrl">/${fn:trim(item.imageUrl)}</c:set><c:url var="itemImageUrl" value="${relativeItemImageUrl}" /></c:otherwise>
                                        </c:choose>
                                        <img class="fnb-product-thumb" src="<c:out value='${itemImageUrl}'/>" data-fallback="<c:out value='${fnbFallbackUrl}'/>" onerror="this.onerror=null;this.src=this.dataset.fallback" alt="" loading="lazy">
                                        <span>
                                            <strong><c:out value="${item.name}"/></strong>
                                            <small><c:out value="${item.itemCode}"/></small>
                                        </span>
                                    </div>
                                </td>
                                <td><c:out value="${item.category.name}"/></td>
                                <td style="font-weight: 700; color: #1e293b;">
                                    <fmt:formatNumber value="${item.price}" pattern="#,##0" groupingUsed="true" />₫
                                </td>
                                <td>
                                    <span style="font-size: 12px; color: #475569; font-weight: 600;">
                                        <c:choose><c:when test="${item.isCombo}">Combo</c:when><c:otherwise>Món lẻ</c:otherwise></c:choose>
                                    </span>
                                </td>
                                <td>
                                    <span class="status ${item.isActive ? 'on' : 'off'}">
                                        <c:choose><c:when test="${item.isActive}">Đang bán</c:when><c:otherwise>Đã ngừng</c:otherwise></c:choose>
                                    </span>
                                </td>
                                <td class="actions-cell">
                                    <div class="action-btn-group">
                                        <!-- NÚT SỬA -->
                                        <button type="button" class="btn-action btn-edit"
                                                onclick="openEditItemModal('${item.id}', '${fn:escapeXml(item.itemCode)}', '${fn:escapeXml(item.name)}', '${item.categoryId}', '${item.price}', '${fn:escapeXml(item.imageUrl)}', ${item.isCombo})"
                                                title="Chỉnh sửa thông tin món">
                                            <i class="fa-solid fa-pen-to-square"></i>
                                            <span>Sửa</span>
                                        </button>

                                        <!-- NÚT BẬT / NGỪNG BÁN -->
                                        <form method="post" action="${pageContext.request.contextPath}${fnbCatalogUrl}" style="margin: 0;">
                                            <input type="hidden" name="action" value="toggleItem">
                                            <input type="hidden" name="id" value="${item.id}">
                                            <input type="hidden" name="active" value="${!item.isActive}">
                                            <c:choose>
                                                <c:when test="${item.isActive}">
                                                    <button type="submit" class="btn-action btn-toggle-off" title="Tạm dừng kinh doanh món này tại quầy POS">
                                                        <i class="fa-solid fa-ban"></i>
                                                        <span>Ngừng bán</span>
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="submit" class="btn-action btn-toggle-on" title="Kích hoạt bán lại món này tại quầy POS">
                                                        <i class="fa-solid fa-circle-check"></i>
                                                        <span>Mở bán</span>
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </form>

                                        <!-- NÚT XÓA MỀM -->
                                        <form method="post" action="${pageContext.request.contextPath}${fnbCatalogUrl}" id="deleteItemForm_${item.id}" style="margin: 0;">
                                            <input type="hidden" name="action" value="deleteItem">
                                            <input type="hidden" name="id" value="${item.id}">
                                            <button type="button" class="btn-action btn-delete"
                                                    onclick="confirmDeleteItem('${item.id}', '${fn:escapeXml(item.name)}', '${fn:escapeXml(item.itemCode)}')"
                                                    title="Xóa mềm món này khỏi hệ thống">
                                                <i class="fa-solid fa-trash-can"></i>
                                                <span>Xóa</span>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <tr id="fnbNoResultsRow" style="display: none;">
                            <td colspan="6" class="empty">Không tìm thấy sản phẩm nào phù hợp với bộ lọc tìm kiếm.</td>
                        </tr>
                        <c:if test="${empty items}">
                            <tr>
                                <td colspan="6" class="empty">Chưa có sản phẩm nào trong danh mục F&B.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </section>

            <!-- SECTION DANH MỤC -->
            <section class="admin-card">
                <div class="section-title">
                    <div>
                        <h2>Danh mục F&B</h2>
                        <p>Quản lý các nhóm phân loại món ăn, đồ uống và combo.</p>
                    </div>
                </div>

                <!-- FORM THÊM MỚI DANH MỤC -->
                <form method="post" action="${pageContext.request.contextPath}${fnbCatalogUrl}" class="category-add">
                    <input type="hidden" name="action" value="saveCategory">
                    <input name="name" maxlength="100" required placeholder="Tên danh mục mới (*)">
                    <input name="description" maxlength="255" placeholder="Mô tả danh mục...">
                    <button class="primary-button" type="submit">
                        <i class="fa-solid fa-plus"></i> Thêm danh mục
                    </button>
                </form>

                <div class="table-scroll">
                    <table class="fnb-table">
                        <thead>
                        <tr>
                            <th style="min-width: 180px;">Tên danh mục</th>
                            <th>Mô tả</th>
                            <th>Trạng thái</th>
                            <th class="actions-cell">Thao tác</th>
                        </tr>
                        </thead>
                        <tbody>
                        <c:forEach items="${categories}" var="cat">
                            <tr>
                                <td><strong><c:out value="${cat.name}"/></strong></td>
                                <td><c:out value="${cat.description}"/></td>
                                <td>
                                    <span class="status ${cat.isActive ? 'on' : 'off'}">
                                        <c:choose><c:when test="${cat.isActive}">Đang dùng</c:when><c:otherwise>Đã ngừng</c:otherwise></c:choose>
                                    </span>
                                </td>
                                <td class="actions-cell">
                                    <div class="action-btn-group">
                                        <!-- NÚT SỬA DANH MỤC -->
                                        <button type="button" class="btn-action btn-edit"
                                                onclick="openEditCategoryModal('${cat.id}', '${fn:escapeXml(cat.name)}', '${fn:escapeXml(cat.description)}')"
                                                title="Sửa tên hoặc mô tả danh mục">
                                            <i class="fa-solid fa-pen-to-square"></i>
                                            <span>Sửa</span>
                                        </button>

                                        <!-- NÚT BẬT / NGỪNG DÙNG DANH MỤC -->
                                        <form method="post" action="${pageContext.request.contextPath}${fnbCatalogUrl}" style="margin: 0;">
                                            <input type="hidden" name="action" value="toggleCategory">
                                            <input type="hidden" name="id" value="${cat.id}">
                                            <input type="hidden" name="active" value="${!cat.isActive}">
                                            <c:choose>
                                                <c:when test="${cat.isActive}">
                                                    <button type="submit" class="btn-action btn-toggle-off" title="Tạm ngừng sử dụng danh mục này">
                                                        <i class="fa-solid fa-ban"></i>
                                                        <span>Ngừng dùng</span>
                                                    </button>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="submit" class="btn-action btn-toggle-on" title="Kích hoạt lại danh mục này">
                                                        <i class="fa-solid fa-circle-check"></i>
                                                        <span>Kích hoạt</span>
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </form>

                                        <!-- NÚT XÓA MỀM DANH MỤC -->
                                        <form method="post" action="${pageContext.request.contextPath}${fnbCatalogUrl}" id="deleteCategoryForm_${cat.id}" style="margin: 0;">
                                            <input type="hidden" name="action" value="deleteCategory">
                                            <input type="hidden" name="id" value="${cat.id}">
                                            <button type="button" class="btn-action btn-delete"
                                                    onclick="confirmDeleteCategory('${cat.id}', '${fn:escapeXml(cat.name)}')"
                                                    title="Xóa mềm danh mục">
                                                <i class="fa-solid fa-trash-can"></i>
                                                <span>Xóa</span>
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty categories}">
                            <tr>
                                <td colspan="4" class="empty">Chưa có danh mục nào. Hãy tạo danh mục trước khi thêm sản phẩm.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>
</div>

<!-- MODAL CHỈNH SỬA SẢN PHẨM -->
<div id="editItemModal" class="fnb-modal-backdrop" style="display: none;">
    <div class="fnb-modal-card">
        <div class="fnb-modal-header">
            <h3><i class="fa-solid fa-pen-to-square text-primary"></i> Chỉnh Sửa Sản Phẩm F&B</h3>
            <button type="button" class="fnb-modal-close" onclick="closeEditItemModal()">&times;</button>
        </div>
        <form method="post" action="${pageContext.request.contextPath}${fnbCatalogUrl}">
            <input type="hidden" name="action" value="saveItem">
            <input type="hidden" name="id" id="editItemId">
            <div class="fnb-modal-body">
                <label>
                    Mã sản phẩm (*)
                    <input type="text" name="itemCode" id="editItemCode" maxlength="30" required>
                </label>
                <label>
                    Tên sản phẩm (*)
                    <input type="text" name="name" id="editItemName" maxlength="150" required>
                </label>
                <label>
                    Danh mục (*)
                    <select name="categoryId" id="editItemCategory" required>
                        <c:forEach items="${categories}" var="cat">
                            <option value="${cat.id}"><c:out value="${cat.name}"/></option>
                        </c:forEach>
                    </select>
                </label>
                <label>
                    Giá bán (₫) (*)
                    <input type="number" name="price" id="editItemPrice" min="0" step="1000" required>
                </label>
                <label>
                    URL hình ảnh
                    <input type="text" name="imageUrl" id="editItemImageUrl" maxlength="255" placeholder="/assets/images/fnb/... hoặc link ảnh">
                </label>
                <label class="check-label" style="margin-top: 6px;">
                    <input type="checkbox" name="isCombo" id="editItemIsCombo" value="true">
                    <span>Đánh dấu là Combo</span>
                </label>
            </div>
            <div class="fnb-modal-footer">
                <button type="button" class="btn-secondary" onclick="closeEditItemModal()">Hủy bỏ</button>
                <button type="submit" class="primary-button"><i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi</button>
            </div>
        </form>
    </div>
</div>

<!-- MODAL CHỈNH SỬA DANH MỤC -->
<div id="editCategoryModal" class="fnb-modal-backdrop" style="display: none;">
    <div class="fnb-modal-card">
        <div class="fnb-modal-header">
            <h3><i class="fa-solid fa-pen-to-square text-primary"></i> Chỉnh Sửa Danh Mục</h3>
            <button type="button" class="fnb-modal-close" onclick="closeEditCategoryModal()">&times;</button>
        </div>
        <form method="post" action="${pageContext.request.contextPath}${fnbCatalogUrl}">
            <input type="hidden" name="action" value="saveCategory">
            <input type="hidden" name="id" id="editCatId">
            <div class="fnb-modal-body">
                <label>
                    Tên danh mục (*)
                    <input type="text" name="name" id="editCatName" maxlength="100" required>
                </label>
                <label>
                    Mô tả
                    <input type="text" name="description" id="editCatDescription" maxlength="255">
                </label>
            </div>
            <div class="fnb-modal-footer">
                <button type="button" class="btn-secondary" onclick="closeEditCategoryModal()">Hủy bỏ</button>
                <button type="submit" class="primary-button"><i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi</button>
            </div>
        </form>
    </div>
</div>

<!-- JAVASCRIPT LOGIC -->
<script>
    // Chuẩn hóa tiếng Việt không dấu để tìm kiếm thông minh
    function removeVietnameseTones(str) {
        if (!str) return '';
        str = str.toLowerCase();
        str = str.replace(/à|á|ạ|ả|ã|â|ầ|ấ|ậ|ẩ|ẫ|ă|ằ|ắ|ặ|ẳ|ẵ/g, "a");
        str = str.replace(/è|é|ẹ|ẻ|ẽ|ê|ề|ế|ệ|ể|ễ/g, "e");
        str = str.replace(/ì|í|ị|ỉ|ĩ/g, "i");
        str = str.replace(/ò|ó|ọ|ỏ|õ|ô|ồ|ố|ộ|ổ|ỗ|ơ|ờ|ớ|ợ|ở|ỡ/g, "o");
        str = str.replace(/ù|ú|ụ|ủ|ũ|ư|ừ|ứ|ự|ử|ữ/g, "u");
        str = str.replace(/ỳ|ý|ỵ|ỷ|ỹ/g, "y");
        str = str.replace(/đ/g, "d");
        str = str.replace(/\u0300|\u0301|\u0303|\u0309|\u0323/g, "");
        str = str.replace(/\u02C6|\u0306|\u031B/g, "");
        return str.trim();
    }

    // Bộ lọc sản phẩm F&B thời gian thực (Real-time Filter)
    function filterFnbItems() {
        const query = removeVietnameseTones(document.getElementById('fnbSearchInput').value);
        const catFilter = document.getElementById('fnbCategoryFilter').value.toLowerCase().trim();
        const statusFilter = document.getElementById('fnbStatusFilter').value;
        const clearBtn = document.getElementById('fnbSearchClear');

        if (clearBtn) {
            clearBtn.style.display = query ? 'block' : 'none';
        }

        const rows = document.querySelectorAll('.fnb-row');
        let visibleCount = 0;

        rows.forEach(row => {
            const name = removeVietnameseTones(row.getAttribute('data-name') || '');
            const code = removeVietnameseTones(row.getAttribute('data-code') || '');
            const category = removeVietnameseTones(row.getAttribute('data-category') || '');
            const rowCatRaw = (row.getAttribute('data-category') || '').toLowerCase().trim();
            const status = row.getAttribute('data-status');

            // Điều kiện lọc
            const matchesQuery = !query || name.includes(query) || code.includes(query) || category.includes(query);
            const matchesCat = !catFilter || rowCatRaw === catFilter;
            const matchesStatus = !statusFilter || status === statusFilter;

            if (matchesQuery && matchesCat && matchesStatus) {
                row.style.display = '';
                visibleCount++;
            } else {
                row.style.display = 'none';
            }
        });

        const noResultsRow = document.getElementById('fnbNoResultsRow');
        if (noResultsRow) {
            noResultsRow.style.display = (visibleCount === 0 && rows.length > 0) ? '' : 'none';
        }

        const countSpan = document.getElementById('fnbVisibleCount');
        if (countSpan) {
            countSpan.textContent = visibleCount;
        }
    }

    function clearFnbSearch() {
        const input = document.getElementById('fnbSearchInput');
        if (input) {
            input.value = '';
            filterFnbItems();
            input.focus();
        }
    }

    // Modal Sửa Sản Phẩm
    function openEditItemModal(id, code, name, categoryId, price, imageUrl, isCombo) {
        document.getElementById('editItemId').value = id;
        document.getElementById('editItemCode').value = code || '';
        document.getElementById('editItemName').value = name || '';
        document.getElementById('editItemCategory').value = categoryId;
        document.getElementById('editItemPrice').value = price || '';
        document.getElementById('editItemImageUrl').value = imageUrl || '';
        document.getElementById('editItemIsCombo').checked = Boolean(isCombo);

        document.getElementById('editItemModal').style.display = 'flex';
    }

    function closeEditItemModal() {
        document.getElementById('editItemModal').style.display = 'none';
    }

    // Modal Sửa Danh Mục
    function openEditCategoryModal(id, name, description) {
        document.getElementById('editCatId').value = id;
        document.getElementById('editCatName').value = name || '';
        document.getElementById('editCatDescription').value = description || '';

        document.getElementById('editCategoryModal').style.display = 'flex';
    }

    function closeEditCategoryModal() {
        document.getElementById('editCategoryModal').style.display = 'none';
    }

    // Xác nhận Xóa Mềm Sản Phẩm
    function confirmDeleteItem(id, name, code) {
        const msg = "Bạn có chắc chắn muốn XÓA món F&B này không?\n\n" +
                    "• Tên món: " + name + "\n" +
                    "• Mã món: " + code + "\n\n" +
                    "Món này sẽ được xóa mềm và ngừng hiển thị tại toàn bộ quầy POS & danh mục.";
        if (confirm(msg)) {
            document.getElementById('deleteItemForm_' + id).submit();
        }
    }

    // Xác nhận Xóa Mềm Danh Mục
    function confirmDeleteCategory(id, name) {
        const msg = "Bạn có chắc chắn muốn XÓA danh mục: \"" + name + "\"?\n\n" +
                    "Danh mục này sẽ được xóa mềm khỏi hệ thống.";
        if (confirm(msg)) {
            document.getElementById('deleteCategoryForm_' + id).submit();
        }
    }

    // Đóng modal khi click ra ngoài vùng backdrop
    window.addEventListener('click', function(e) {
        const itemModal = document.getElementById('editItemModal');
        const catModal = document.getElementById('editCategoryModal');
        if (e.target === itemModal) closeEditItemModal();
        if (e.target === catModal) closeEditCategoryModal();
    });

    // Chạy bộ lọc ban đầu nếu có giá trị tìm kiếm sẵn
    document.addEventListener('DOMContentLoaded', function() {
        if (document.getElementById('fnbSearchInput').value) {
            filterFnbItems();
        }
    });
</script>
</body>
</html>
