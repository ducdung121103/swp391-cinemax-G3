<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Quầy Bán Vé & F&B (POS) - Cinema Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/operation.css">
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../../common/navbar.jsp" />
            <div class="admin-content">
                <h2>MÀN HÌNH BÁN VÉ & BẮP NƯỚC TẠI QUẦY (POS - TV 5)</h2>
                <div class="pos-layout" style="margin-top: 20px;">
                    <!-- Cột trái: Chọn Bắp nước & Vé -->
                    <div class="admin-card">
                        <h3>Danh Mục Bắp & Nước (F&B)</h3>
                        <div class="fnb-items-grid" style="margin-top: 15px;">
                            <c:forEach items="${fnbItems}" var="item">
                                <div class="fnb-item-card">
                                    <div style="font-size: 32px;">🍿</div>
                                    <div style="font-weight: bold; margin: 5px 0;">${item.name}</div>
                                    <div style="color: #e50914; font-weight: bold;">${item.price} đ</div>
                                </div>
                            </c:forEach>
                        </div>
                    </div>

                    <!-- Cột phải: Hóa đơn thanh toán POS -->
                    <div class="admin-card">
                        <h3>Hóa Đơn Hiện Tại</h3>
                        <p style="color: #888; font-size: 13px;">Thu ngân: <strong>${sessionScope.currentUser.fullName}</strong></p>
                        <div style="margin: 20px 0; border-top: 1px solid #eee; border-bottom: 1px solid #eee; padding: 15px 0;">
                            <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
                                <span>Ghế VIP F08 (Phim Mai)</span>
                                <strong>95.000 đ</strong>
                            </div>
                            <div style="display: flex; justify-content: space-between;">
                                <span>1x Bắp Phô Mai (L)</span>
                                <strong>45.000 đ</strong>
                            </div>
                        </div>
                        <div style="display: flex; justify-content: space-between; font-size: 18px; margin-bottom: 20px;">
                            <span>Tổng Tiền:</span>
                            <span style="color: #e50914; font-weight: bold;">140.000 đ</span>
                        </div>
                        <button type="button" class="btn-register" style="width: 100%; padding: 12px; font-size: 16px; border: none; cursor: pointer;">
                            Thu Tiền Mặt & In Vé
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
