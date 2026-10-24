<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Bảng giá vé - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        form.inline { display: flex; gap: 12px; flex-wrap: wrap; margin: 16px 0; align-items: end; }
        .filter-input, .filter-select { padding: 8px 12px; border: 1px solid #cbd5e0; border-radius: 6px; }
        .alert { padding: 12px 16px; border-radius: 6px; margin-bottom: 16px; }
        .alert-success { background: #c6f6d5; color: #22543d; }
        .alert-danger { background: #fed7d7; color: #742a2a; }
        table { width: 100%; border-collapse: collapse; background: #fff; }
        th, td { padding: 10px; border-bottom: 1px solid #e2e8f0; text-align: left; vertical-align: top; }
        label { display: block; font-size: 12px; color: #4a5568; }
        td form { display: flex; gap: 8px; align-items: center; }
    </style>
</head>
<body>
<div class="admin-wrapper">
    <jsp:include page="../../common/sidebar.jsp" />
    <div class="admin-main">
        <jsp:include page="../../common/navbar.jsp" />
        <div class="admin-content">
            <h2>Bảng giá vé</h2>
            <c:if test="${msg == 'save' || msg == 'delete' || msg == 'discount'}">
                <div class="alert alert-success">Đã lưu thay đổi.</div>
            </c:if>
            <c:if test="${error == 'FORBIDDEN'}"><div class="alert alert-danger">Không được sửa giá của rạp khác hoặc giá toàn chuỗi.</div></c:if>
            <c:if test="${error == 'INVALID'}"><div class="alert alert-danger">Dữ liệu giá không hợp lệ.</div></c:if>
            <c:if test="${not empty error && error != 'FORBIDDEN' && error != 'INVALID'}"><div class="alert alert-danger">Không lưu được.</div></c:if>

            <h3>Giảm giá đối tượng</h3>
            <form method="post" action="${pageContext.request.contextPath}/admin/catalog/pricing" class="inline">
                <input type="hidden" name="action" value="discount" />
                <div>
                    <label>HSSV (%)</label>
                    <input class="filter-input" type="number" name="studentPercent" min="0" max="100" value="${studentDiscount}" required />
                </div>
                <div>
                    <label>Người cao tuổi (%)</label>
                    <input class="filter-input" type="number" name="seniorPercent" min="0" max="100" value="${seniorDiscount}" required />
                </div>
                <button class="btn btn-primary" type="submit">Lưu phần trăm</button>
            </form>

            <h3>Thêm dòng giá</h3>
            <form method="post" action="${pageContext.request.contextPath}/admin/catalog/pricing" class="inline">
                <input type="hidden" name="action" value="save" />
                <c:choose>
                    <c:when test="${actor.role.roleName == 'ADMIN'}">
                        <div>
                            <label>Rạp</label>
                            <select class="filter-select" name="cinemaId">
                                <option value="">Toàn chuỗi</option>
                                <c:forEach var="cinema" items="${cinemas}">
                                    <option value="${cinema.id}">${cinema.name}</option>
                                </c:forEach>
                            </select>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <input type="hidden" name="cinemaId" value="${actor.cinemaId}" />
                    </c:otherwise>
                </c:choose>
                <div>
                    <label>Ngày</label>
                    <select class="filter-select" name="dayType">
                        <option value="WEEKDAY">WEEKDAY</option>
                        <option value="WEEKEND">WEEKEND</option>
                        <option value="HOLIDAY">HOLIDAY</option>
                    </select>
                </div>
                <div>
                    <label>Khung giờ</label>
                    <select class="filter-select" name="timeSlot">
                        <option value="EARLY">EARLY</option>
                        <option value="STANDARD">STANDARD</option>
                        <option value="PRIME">PRIME</option>
                    </select>
                </div>
                <div>
                    <label>Định dạng</label>
                    <select class="filter-select" name="experienceFormat">
                        <option value="2D">2D</option>
                        <option value="3D">3D</option>
                        <option value="IMAX">IMAX</option>
                    </select>
                </div>
                <div>
                    <label>Loại ghế</label>
                    <select class="filter-select" name="seatTypeId">
                        <c:forEach var="seatType" items="${seatTypes}">
                            <option value="${seatType.id}">${seatType.name}</option>
                        </c:forEach>
                    </select>
                </div>
                <div>
                    <label>Giá gốc</label>
                    <input class="filter-input" type="number" name="basePrice" min="1" step="1000" required />
                </div>
                <div>
                    <label>Từ ngày</label>
                    <input class="filter-input" type="date" name="effectiveFrom" required />
                </div>
                <div>
                    <label>Đến ngày</label>
                    <input class="filter-input" type="date" name="effectiveTo" />
                </div>
                <button class="btn btn-primary" type="submit">Thêm</button>
            </form>

            <table>
                <thead>
                <tr>
                    <th>Rạp</th><th>Ngày</th><th>Khung giờ</th><th>Định dạng</th><th>Loại ghế</th><th>Giá</th><th>Hiệu lực</th><th></th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="price" items="${prices}">
                    <c:set var="ownRow" value="${actor.role.roleName == 'ADMIN' || (not empty price.cinemaId && price.cinemaId == actor.cinemaId)}" />
                    <tr>
                        <td>${empty price.cinemaName ? 'Toàn chuỗi' : price.cinemaName}</td>
                        <td>${price.dayType}</td>
                        <td>${price.timeSlot}</td>
                        <td>${price.experienceFormat}</td>
                        <td>${price.seatTypeName}</td>
                        <td>
                            <c:if test="${ownRow}">
                                <form method="post" action="${pageContext.request.contextPath}/admin/catalog/pricing">
                                    <input type="hidden" name="action" value="save" />
                                    <input type="hidden" name="id" value="${price.id}" />
                                    <input type="hidden" name="cinemaId" value="${price.cinemaId}" />
                                    <input type="hidden" name="dayType" value="${price.dayType}" />
                                    <input type="hidden" name="timeSlot" value="${price.timeSlot}" />
                                    <input type="hidden" name="experienceFormat" value="${price.experienceFormat}" />
                                    <input type="hidden" name="seatTypeId" value="${price.seatTypeId}" />
                                    <input type="hidden" name="effectiveFrom" value="${price.effectiveFrom}" />
                                    <input type="hidden" name="effectiveTo" value="${price.effectiveTo}" />
                                    <input class="filter-input" type="number" name="basePrice" min="1" step="1000" value="${price.basePrice}" required />
                                    <button class="btn btn-sm btn-primary" type="submit">Lưu</button>
                                </form>
                            </c:if>
                            <c:if test="${!ownRow}">${price.basePrice}</c:if>
                        </td>
                        <td>${price.effectiveFrom} → ${empty price.effectiveTo ? 'không hạn' : price.effectiveTo}</td>
                        <td>
                            <c:if test="${ownRow}">
                                <form method="post" action="${pageContext.request.contextPath}/admin/catalog/pricing">
                                    <input type="hidden" name="action" value="delete" />
                                    <input type="hidden" name="id" value="${price.id}" />
                                    <button class="btn btn-sm btn-outline-secondary" type="submit">Xóa</button>
                                </form>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </div>
    </div>
</div>
</body>
</html>
