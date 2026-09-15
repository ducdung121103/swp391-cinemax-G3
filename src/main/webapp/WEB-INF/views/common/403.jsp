<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>403 - Truy cập bị từ chối</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <style>
        .error-container { text-align: center; margin-top: 100px; font-family: sans-serif; }
        .error-code { font-size: 96px; font-weight: bold; color: #dc3545; }
        .error-msg { font-size: 24px; color: #333; margin-bottom: 20px; }
        .btn-home { display: inline-block; padding: 10px 20px; background: #007bff; color: #fff; text-decoration: none; border-radius: 5px; }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-code">403</div>
        <div class="error-msg">Bạn không có quyền truy cập vào tài nguyên này!</div>
        <p>Vui lòng liên hệ Quản trị viên hoặc quay trở lại trang chủ.</p>
        <a href="${pageContext.request.contextPath}/" class="btn-home">Về Trang Chủ</a>
    </div>
</body>
</html>
