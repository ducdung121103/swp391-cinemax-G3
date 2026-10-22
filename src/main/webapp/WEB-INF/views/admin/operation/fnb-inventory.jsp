<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!doctype html>
<html lang="vi">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Tồn kho bắp nước | CineMax</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/operation.css?v=3">
</head>
<body>
<c:url var="fnbFallbackUrl" value="/assets/images/fnb/default.svg" />
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <main class="admin-content operation-page inventory-page">
            <header class="operation-heading">
                <div>
                    <span class="operation-eyebrow">VẬN HÀNH · KHO HÀNG</span>
                    <h1>Tồn kho bắp nước</h1>
                    <p>Theo dõi số lượng, cập nhật hàng hóa và nhận biết sản phẩm cần bổ sung.</p>
                </div>
                <div class="inventory-branch-badge"><span>◉</span> Kho theo chi nhánh</div>
            </header>

            <c:if test="${param.success eq '1'}">
                <div class="operation-alert alert-success" role="status"><span class="alert-icon">✓</span>Đã cập nhật tồn kho thành công.</div>
            </c:if>
            <c:if test="${not empty param.error}">
                <div class="operation-alert alert-error" role="alert"><span class="alert-icon">!</span><c:out value="${param.error}" /></div>
            </c:if>
            <c:if test="${not empty inventoryMessage}">
                <div class="operation-alert alert-info" role="status"><span class="alert-icon">i</span><c:out value="${inventoryMessage}" /></div>
            </c:if>

            <section class="admin-card inventory-toolbar-card">
                <div class="section-heading compact-heading">
                    <div class="section-number">01</div>
                    <div><h2>Chọn chi nhánh</h2><p>Tồn kho được quản lý độc lập tại từng rạp.</p></div>
                </div>
                <c:choose>
                    <c:when test="${not empty cinemas}">
                        <form class="inventory-branch-form" method="get" action="${pageContext.request.contextPath}${fnbInventoryUrl}">
                            <label class="field-group">Chi nhánh
                                <select name="cinemaId" onchange="this.form.submit()">
                                    <c:forEach items="${cinemas}" var="cinema">
                                        <option value="${cinema.id}" ${cinema.id eq cinemaId ? 'selected' : ''}><c:out value="${cinema.name}" /></option>
                                    </c:forEach>
                                </select>
                            </label>
                            <noscript><button class="button button-primary" type="submit">Xem tồn kho</button></noscript>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <div class="branch-current"><span class="branch-current-dot"></span><strong>Chi nhánh của tôi</strong><small>Mỗi thay đổi chỉ áp dụng cho rạp đang quản lý.</small></div>
                    </c:otherwise>
                </c:choose>
            </section>

            <section class="inventory-overview" aria-label="Tổng quan tồn kho">
                <article class="stat-card inventory-stat stat-total"><span class="stat-icon">▦</span><div><small>Tổng sản phẩm</small><strong id="inventory-total">0</strong><span>mặt hàng trong danh mục</span></div></article>
                <article class="stat-card inventory-stat stat-low"><span class="stat-icon">⌁</span><div><small>Sắp hết hàng</small><strong id="inventory-low">0</strong><span>dưới ngưỡng cảnh báo</span></div></article>
                <article class="stat-card inventory-stat stat-empty"><span class="stat-icon">!</span><div><small>Hết hàng</small><strong id="inventory-empty">0</strong><span>cần nhập thêm tồn kho</span></div></article>
            </section>

            <section class="admin-card operation-card inventory-list-card" aria-labelledby="inventory-list-title">
                <div class="inventory-list-heading">
                    <div class="section-heading compact-heading">
                        <div class="section-number">02</div>
                        <div><h2 id="inventory-list-title">Danh sách sản phẩm</h2><p>Điều chỉnh tồn kho và ngưỡng cảnh báo cho từng mặt hàng.</p></div>
                    </div>
                    <div class="inventory-filters">
                        <label class="search-field"><span aria-hidden="true">⌕</span><input id="inventory-search" type="search" placeholder="Tìm sản phẩm hoặc mã hàng" aria-label="Tìm sản phẩm"></label>
                        <label class="filter-select"><span class="sr-only">Lọc trạng thái tồn kho</span><select id="inventory-status"><option value="all">Tất cả trạng thái</option><option value="low">Sắp hết</option><option value="out">Hết hàng</option><option value="ok">Còn hàng</option></select></label>
                    </div>
                </div>

                <div class="inventory-table-wrap">
                    <table class="inventory-table">
                        <thead><tr><th>Sản phẩm</th><th>Đơn giá</th><th>Tồn kho</th><th>Ngưỡng cảnh báo</th><th>Trạng thái</th><th>Cập nhật</th></tr></thead>
                        <tbody id="inventory-rows">
                        <c:choose>
                            <c:when test="${not empty inventory}">
                                <c:forEach items="${inventory}" var="row">
                                    <tr class="inventory-row" data-stock="${row.stock}" data-threshold="${row.threshold}">
                                        <td>
                                            <div class="inventory-product">
                                                <c:choose>
                                                    <c:when test="${empty row.imageUrl}"><c:set var="itemImageUrl" value="${fnbFallbackUrl}" /></c:when>
                                                    <c:when test="${fn:startsWith(fn:toLowerCase(fn:trim(row.imageUrl)), 'http://') or fn:startsWith(fn:toLowerCase(fn:trim(row.imageUrl)), 'https://')}"><c:set var="itemImageUrl" value="${fn:trim(row.imageUrl)}" /></c:when>
                                                    <c:when test="${fn:startsWith(fn:toLowerCase(fn:trim(row.imageUrl)), '/assets/images/fnb/') and fn:endsWith(fn:toLowerCase(fn:trim(row.imageUrl)), '.png')}"><c:url var="itemImageUrl" value="${fn:replace(fn:trim(row.imageUrl), '.png', '.svg')}" /></c:when>
                                                    <c:when test="${fn:startsWith(fn:trim(row.imageUrl), '/')}"><c:url var="itemImageUrl" value="${fn:trim(row.imageUrl)}" /></c:when>
                                                    <c:otherwise><c:set var="relativeItemImageUrl">/${fn:trim(row.imageUrl)}</c:set><c:url var="itemImageUrl" value="${relativeItemImageUrl}" /></c:otherwise>
                                                </c:choose>
                                                <img class="inventory-product-image" src="<c:out value='${itemImageUrl}' />" data-fallback="<c:out value='${fnbFallbackUrl}' />" onerror="this.onerror=null;this.src=this.dataset.fallback" alt="" loading="lazy">
                                                <span><strong><c:out value="${row.name}" /></strong><small><c:out value="${row.code}" /></small></span>
                                            </div>
                                        </td>
                                        <td class="inventory-price"><fmt:formatNumber value="${row.price}" pattern="#,##0" groupingUsed="true" /><span>đ</span></td>
                                        <td><strong class="stock-quantity"><c:out value="${row.stock}" /></strong><small class="stock-unit">đơn vị</small></td>
                                        <td><span class="threshold-value"><c:out value="${row.threshold}" /> <small>đơn vị</small></span></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${row.stock le 0}"><span class="inventory-status-pill status-out"><i></i>Hết hàng</span></c:when>
                                                <c:when test="${row.stock le row.threshold}"><span class="inventory-status-pill status-low"><i></i>Sắp hết</span></c:when>
                                                <c:otherwise><span class="inventory-status-pill status-ok"><i></i>Còn hàng</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <form class="inventory-edit-form" method="post" action="${pageContext.request.contextPath}${fnbInventoryUrl}">
                                                <input type="hidden" name="cinemaId" value="${cinemaId}">
                                                <input type="hidden" name="itemId" value="${row.id}">
                                                <label><span class="sr-only">Số lượng tồn</span><input type="number" min="0" name="quantity" value="${row.stock}" required></label>
                                                <label><span class="sr-only">Ngưỡng cảnh báo</span><input type="number" min="0" name="threshold" value="${row.threshold}" required></label>
                                                <button class="button button-save" type="submit">Lưu</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:when>
                            <c:otherwise><tr><td class="inventory-empty-row" colspan="6"><span>◌</span><strong>Chưa có sản phẩm tồn kho</strong><small>Hãy chọn chi nhánh hoặc thêm sản phẩm vào danh mục F&B.</small></td></tr></c:otherwise>
                        </c:choose>
                        </tbody>
                    </table>
                </div>
                <div class="inventory-no-results" id="inventory-no-results" hidden>Không tìm thấy sản phẩm phù hợp.</div>
                <footer class="inventory-list-footer"><span><i class="footer-dot"></i>Thay đổi sẽ được lưu vào kho của chi nhánh đang chọn.</span><span id="inventory-visible-count"></span></footer>
            </section>
        </main>
    </div>
</div>
<script>
    (() => {
        const rows = [...document.querySelectorAll('.inventory-row')];
        const search = document.getElementById('inventory-search');
        const status = document.getElementById('inventory-status');
        const visibleCount = document.getElementById('inventory-visible-count');
        const noResults = document.getElementById('inventory-no-results');
        let low = 0;
        let out = 0;
        rows.forEach(row => {
            const stock = Number(row.dataset.stock);
            const threshold = Number(row.dataset.threshold);
            if (stock <= 0) out++;
            else if (stock <= threshold) low++;
        });
        document.getElementById('inventory-total').textContent = rows.length;
        document.getElementById('inventory-low').textContent = low;
        document.getElementById('inventory-empty').textContent = out;

        const applyFilters = () => {
            const term = search.value.trim().toLocaleLowerCase('vi');
            const selectedStatus = status.value;
            let visible = 0;
            rows.forEach(row => {
                const text = row.querySelector('.inventory-product').textContent.toLocaleLowerCase('vi');
                const stock = Number(row.dataset.stock);
                const threshold = Number(row.dataset.threshold);
                const rowStatus = stock <= 0 ? 'out' : (stock <= threshold ? 'low' : 'ok');
                const show = text.includes(term) && (selectedStatus === 'all' || selectedStatus === rowStatus);
                row.hidden = !show;
                if (show) visible++;
            });
            noResults.hidden = visible > 0 || rows.length === 0;
            visibleCount.textContent = `${visible} / ${rows.length} sản phẩm`;
        };
        search.addEventListener('input', applyFilters);
        status.addEventListener('change', applyFilters);
        applyFilters();
    })();
</script>
</body>
</html>
