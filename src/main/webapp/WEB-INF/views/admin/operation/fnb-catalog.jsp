<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html><html lang="vi"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>Quản lý danh mục F&B</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/fnb-catalog.css">
</head><body>
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp"/>
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp"/>
        <main class="admin-content">
            <header class="fnb-heading">
                <div><span class="eyebrow">VẬN HÀNH / F&B</span>
                    <h1>Quản lý danh mục F&B</h1>
                    <p>Thêm, chỉnh sửa và tạm ngừng bán món ăn hoặc combo.</p></div>
            </header>
            <c:if test="${param.success eq '1'}">
                <div class="notice success">Đã lưu thay đổi.</div>
            </c:if><c:if test="${not empty param.error}">
            <div class="notice error"><c:out value="${param.error}"/></div>
        </c:if>
            <section class="admin-card">
                <div class="section-title">
                    <div><h2>Sản phẩm và combo</h2>
                        <p>Thông tin bán hàng hiển thị tại quầy POS.</p></div>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/operation/fnb-catalog"
                      class="fnb-form"><input type="hidden" name="action" value="saveItem"><label>Mã sản phẩm<input
                        name="itemCode" maxlength="30" required placeholder="VD: POPCORN-CARAMEL-L"></label><label>Tên
                    sản phẩm<input name="name" maxlength="150" required></label><label>Danh mục<select name="categoryId"
                                                                                                       required><c:forEach
                        items="${categories}" var="cat"><c:if test="${cat.isActive}">
                    <option value="${cat.id}"><c:out value="${cat.name}"/></option>
                </c:if></c:forEach></select></label><label>Giá (₫)<input type="number" name="price" min="0" step="0.01"
                                                                         required></label><label class="wide">URL hình
                    ảnh<input name="imageUrl" maxlength="255" placeholder="https://..."></label><label
                        class="check-label"><input type="checkbox" name="isCombo" value="true"> Đây là combo</label>
                    <button class="primary-button" type="submit">Thêm sản phẩm</button>
                </form>
                <div class="table-scroll">
                    <table class="fnb-table">
                        <thead>
                        <tr>
                            <th>Sản phẩm</th>
                            <th>Danh mục</th>
                            <th>Giá</th>
                            <th>Loại</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody><c:forEach items="${items}" var="item">
                            <tr>
                                <td><strong><c:out value="${item.name}"/></strong><small><c:out
                                        value="${item.itemCode}"/></small></td>
                                <td><c:out value="${item.category.name}"/></td>
                                <td><c:out value="${item.price}"/> ₫</td>
                                <td><c:choose><c:when
                                        test="${item.isCombo}">Combo</c:when><c:otherwise>Món lẻ</c:otherwise></c:choose></td>
                                <td><span class="status ${item.isActive ? 'on' : 'off'}"><c:choose><c:when
                                        test="${item.isActive}">Đang bán</c:when><c:otherwise>Đã ngừng</c:otherwise></c:choose></span>
                                </td>
                                <td class="actions">
                                    <details>
                                        <summary>Sửa</summary>
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/admin/operation/fnb-catalog"
                                              class="edit-form"><input type="hidden" name="action"
                                                                       value="saveItem"><input type="hidden" name="id"
                                                                                               value="${item.id}"><label>Mã<input
                                                name="itemCode" value="<c:out value='${item.itemCode}'/>"
                                                required></label><label>Tên<input name="name"
                                                                                  value="<c:out value='${item.name}'/>"
                                                                                  required></label><label>Danh
                                            mục<select name="categoryId"><c:forEach items="${categories}" var="cat">
                                                <option value="${cat.id}" ${cat.id eq item.categoryId ? 'selected' : ''}>
                                                    <c:out value="${cat.name}"/></option>
                                            </c:forEach></select></label><label>Giá<input type="number" name="price"
                                                                                          min="0" step="0.01"
                                                                                          value="${item.price}"
                                                                                          required></label><label>URL
                                            hình ảnh<input name="imageUrl"
                                                           value="<c:out value='${item.imageUrl}'/>"/></label><label
                                                class="check-label"><input type="checkbox" name="isCombo"
                                                                           value="true" ${item.isCombo ? 'checked' : ''}>
                                            Combo</label>
                                            <button class="primary-button">Lưu</button>
                                        </form>
                                    </details>
                                    <form method="post"
                                          action="${pageContext.request.contextPath}/admin/operation/fnb-catalog"><input
                                            type="hidden" name="action" value="toggleItem"><input type="hidden"
                                                                                                  name="id"
                                                                                                  value="${item.id}"><input
                                            type="hidden" name="active" value="${!item.isActive}">
                                        <button class="text-button">${item.isActive ? 'Ngừng bán' : 'Bật bán'}</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach><c:if test="${empty items}">
                            <tr>
                                <td colspan="6" class="empty">Chưa có sản phẩm nào.</td>
                            </tr>
                        </c:if></tbody>
                    </table>
                </div>
            </section>
            <section class="admin-card">
                <div class="section-title">
                    <div><h2>Danh mục</h2>
                        <p>Danh mục có sản phẩm tham chiếu có thể tạm ngừng sử dụng.</p></div>
                </div>
                <form method="post" action="${pageContext.request.contextPath}/admin/operation/fnb-catalog"
                      class="category-add"><input type="hidden" name="action" value="saveCategory"><input name="name"
                                                                                                          maxlength="100"
                                                                                                          required
                                                                                                          placeholder="Tên danh mục mới"><input
                        name="description" maxlength="255" placeholder="Mô tả">
                    <button class="primary-button">Thêm danh mục</button>
                </form>
                <div class="table-scroll">
                    <table class="fnb-table">
                        <thead>
                        <tr>
                            <th>Tên danh mục</th>
                            <th>Mô tả</th>
                            <th>Trạng thái</th>
                            <th>Thao tác</th>
                        </tr>
                        </thead>
                        <tbody><c:forEach items="${categories}" var="cat">
                            <tr>
                                <td><strong><c:out value="${cat.name}"/></strong></td>
                                <td><c:out value="${cat.description}"/></td>
                                <td><span
                                        class="status ${cat.isActive ? 'on' : 'off'}">${cat.isActive ? 'Đang dùng' : 'Đã ngừng'}</span>
                                </td>
                                <td class="actions">
                                    <details>
                                        <summary>Sửa</summary>
                                        <form method="post"
                                              action="${pageContext.request.contextPath}/admin/operation/fnb-catalog"
                                              class="edit-form"><input type="hidden" name="action" value="saveCategory"><input
                                                type="hidden" name="id" value="${cat.id}"><label>Tên<input name="name"
                                                                                                           value="<c:out value='${cat.name}'/>"
                                                                                                           required></label><label>Mô
                                            tả<input name="description"
                                                     value="<c:out value='${cat.description}'/>"/></label>
                                            <button class="primary-button">Lưu</button>
                                        </form>
                                    </details>
                                    <form method="post"
                                          action="${pageContext.request.contextPath}/admin/operation/fnb-catalog"><input
                                            type="hidden" name="action" value="toggleCategory"><input type="hidden"
                                                                                                      name="id"
                                                                                                      value="${cat.id}"><input
                                            type="hidden" name="active" value="${!cat.isActive}">
                                        <button class="text-button">${cat.isActive ? 'Ngừng dùng' : 'Kích hoạt'}</button>
                                    </form>
                                </td>
                            </tr>
                        </c:forEach><c:if test="${empty categories}">
                            <tr>
                                <td colspan="4" class="empty">Chưa có danh mục. Hãy tạo danh mục trước khi thêm sản
                                    phẩm.
                                </td>
                            </tr>
                        </c:if></tbody>
                    </table>
                </div>
            </section>
        </main>
    </div>
</div>
</body></html>
