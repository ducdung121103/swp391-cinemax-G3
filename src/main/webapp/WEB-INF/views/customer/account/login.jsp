<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Đăng Nhập - Cinema Chain</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/identity.css">
</head>
<body>
    <jsp:include page="../../common/header.jsp" />

    <div class="auth-box">
        <h2>ĐĂNG NHẬP</h2>
        <c:if test="${not empty errorMessage}">
            <div style="color: red; margin-bottom: 15px; font-weight: bold;">${errorMessage}</div>
        </c:if>
        <c:if test="${param.msg == 'register_success'}">
            <div style="color: green; margin-bottom: 15px; font-weight: bold;">Đăng ký tài khoản thành công! Vui lòng đăng nhập.</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <div style="margin-bottom: 15px;">
                <label>Email đăng nhập:</label>
                <input type="email" name="email" required style="width: 100%; padding: 10px; margin-top: 5px;" value="admin@cinema.com">
            </div>
            <div style="margin-bottom: 20px;">
                <label>Mật khẩu:</label>
                <input type="password" name="password" required style="width: 100%; padding: 10px; margin-top: 5px;" value="123456">
            </div>
            <button type="submit" class="btn-register" style="width: 100%; padding: 12px; font-size: 16px; cursor: pointer; border: none;">Đăng Nhập</button>
        </form>
        <p style="margin-top: 20px; text-align: center;">Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register">Đăng ký ngay</a></p>
    </div>

    <jsp:include page="../../common/footer.jsp" />
</body>
</html>
