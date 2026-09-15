<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng Ký Thành Viên - Cinema Chain</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/identity.css">
</head>
<body>
    <jsp:include page="../../common/header.jsp" />

    <div class="auth-box">
        <h2>ĐĂNG KÝ THÀNH VIÊN</h2>
        <c:if test="${not empty errorMessage}">
            <div style="color: red; margin-bottom: 15px; font-weight: bold;">${errorMessage}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <div style="margin-bottom: 15px;">
                <label>Họ và Tên:</label>
                <input type="text" name="fullName" required style="width: 100%; padding: 10px; margin-top: 5px;">
            </div>
            <div style="margin-bottom: 15px;">
                <label>Số điện thoại:</label>
                <input type="tel" name="phone" style="width: 100%; padding: 10px; margin-top: 5px;">
            </div>
            <div style="margin-bottom: 15px;">
                <label>Email đăng nhập:</label>
                <input type="email" name="email" required style="width: 100%; padding: 10px; margin-top: 5px;">
            </div>
            <div style="margin-bottom: 20px;">
                <label>Mật khẩu:</label>
                <input type="password" name="password" required style="width: 100%; padding: 10px; margin-top: 5px;">
            </div>
            <button type="submit" class="btn-register" style="width: 100%; padding: 12px; font-size: 16px; cursor: pointer; border: none;">Tạo Tài Khoản</button>
        </form>
        <p style="margin-top: 20px; text-align: center;">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login">Đăng nhập</a></p>
    </div>

    <jsp:include page="../../common/footer.jsp" />
</body>
</html>
