<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>500 - Lỗi máy chủ</title>
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
        <div class="error-code">500</div>
        <div class="error-msg">Hệ thống đã xảy ra sự cố ngoài ý muốn!</div>
        <p>Đội ngũ kỹ thuật đã được thông báo. Xin vui lòng thử lại sau.</p>
        <a href="${pageContext.request.contextPath}/" class="btn-home">Về Trang Chủ</a>
    </div>
</body>
</html>
