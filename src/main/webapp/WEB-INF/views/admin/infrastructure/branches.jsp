<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quản lý Chi nhánh - Cinema Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../../common/navbar.jsp" />
            <div class="admin-content">
                <div class="admin-card">
                    <h2>Danh sách Cụm Rạp Chi Chi Nhánh (TV 1)</h2>
                    <table class="order-summary-table" style="margin-top: 15px;">
                        <thead>
                            <tr>
                                <th>Mã Rạp</th>
                                <th>Tên Cụm Rạp</th>
                                <th>Thành Phố</th>
                                <th>Địa Chỉ</th>
                                <th>Hotline</th>
                                <th>Số Phòng Chiếu</th>
                                <th>Trạng Thái</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach items="${branches}" var="b">
                                <tr>
                                    <td><strong>${b.branchCode}</strong></td>
                                    <td>${b.name}</td>
                                    <td>${b.city}</td>
                                    <td>${b.address}</td>
                                    <td>${b.phone}</td>
                                    <td>${b.totalHalls} phòng</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${b.isActive}"><span style="color: green;">● Đang hoạt động</span></c:when>
                                            <c:otherwise><span style="color: red;">● Tạm đóng cửa</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
