<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cinema - Xác thực OTP</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="${pageContext.request.contextPath}/web/img/logo/favicon/DC-Symbol.png" rel="icon"/>

    <style>
        /* Tái sử dụng CSS Login */
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Roboto', sans-serif;
            background: linear-gradient(135deg, #9ACBD0 0%, #006A71 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .container {
            width: 100%; max-width: 450px; background: white;
            border-radius: 20px; box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
            padding: 50px 40px; animation: slideUp 0.5s ease-out;
        }
        @keyframes slideUp { from { opacity: 0; transform: translateY(20px); } to { opacity: 1; transform: translateY(0); } }

        .signin-header { margin-bottom: 30px; text-align: center; }
        .signin-title { font-size: 28px; font-weight: 700; color: #1a202c; margin-bottom: 8px; }
        .signin-subtitle { color: #718096; font-size: 15px; }

        .form-group { margin-bottom: 20px; }
        .form-label { display: block; margin-bottom: 8px; color: #4a5568; font-weight: 600; font-size: 14px; }

        .form-input {
            width: 100%; padding: 12px 16px; border: 2px solid #e2e8f0;
            border-radius: 10px; font-size: 24px; outline: none; background: #f8fafc;
            transition: all 0.3s ease; text-align: center; letter-spacing: 8px; font-weight: bold;
        }
        .form-input:focus { border-color: #4c51bf; background: white; }

        .signin-btn {
            width: 100%; padding: 14px;
            background: linear-gradient(135deg, #FFACAC 0%, #FFBFA9 100%);
            color: white; border: none; border-radius: 10px;
            font-size: 16px; font-weight: 700; cursor: pointer; transition: all 0.3s ease;
            box-shadow: 0 4px 12px rgba(255, 172, 172, 0.4);
        }
        .signin-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(255, 172, 172, 0.6); }

        .back-link { display: block; text-align: center; margin-top: 20px; text-decoration: none; color: #718096; }
        .back-link:hover { color: #006A71; text-decoration: underline; }

        .error-message {
            background: #fff5f5; color: #c53030; padding: 12px; border-radius: 8px;
            font-size: 14px; margin-bottom: 20px; text-align: center; border: 1px solid #feb2b2;
        }
        .info-box {
            background: #ebf8ff; color: #2b6cb0; padding: 12px; border-radius: 8px;
            font-size: 14px; margin-bottom: 20px; text-align: center; border: 1px solid #bee3f8;
        }
    </style>
</head>
<body>

<div class="container">
    <div class="signin-header">
        <h1 class="signin-title">Xác thực OTP</h1>
        <h4 class="signin-subtitle">Nhập mã 6 số đã được gửi tới email của bạn.</h4>
    </div>

    <form action="otp-confirm" method="post">

        <div class="info-box">
            <i class="fa-regular fa-envelope"></i> Mã OTP có hiệu lực trong 5 phút.
        </div>

        <div class="form-group">
            <input type="text" id="otp" name="otp" class="form-input" required
                   placeholder="******" maxlength="6" pattern="\d{6}" autofocus>
        </div>

        <c:if test="${not empty error}">
            <div class="error-message">
                <i class="fa-solid fa-circle-exclamation"></i> ${error}
            </div>
        </c:if>

        <button type="submit" class="signin-btn">Xác nhận</button>
    </form>

    <div style="text-align: center; margin-top: 20px;">
        <span style="color: #718096; font-size: 14px;">Chưa nhận được mã?</span>
        <a href="${pageContext.request.contextPath}/forgot-password" class="back-link" style="display: inline; margin-top: 0;">Gửi lại</a>
    </div>
</div>

</body>
</html>