<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cinema - Đặt lại mật khẩu</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="${pageContext.request.contextPath}/web/img/logo/favicon/DC-Symbol.png" rel="icon"/>

    <style>
        /* CSS Reuse from Login */
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: 'Roboto', sans-serif;
            background: linear-gradient(135deg, #9ACBD0 0%, #006A71 100%);
            min-height: 100vh;
            display: flex; align-items: center; justify-content: center;
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
            border-radius: 10px; font-size: 16px; outline: none; background: #f8fafc;
            transition: all 0.3s ease;
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

        .error-message {
            background: #fff5f5; color: #c53030; padding: 12px; border-radius: 8px;
            font-size: 14px; margin-bottom: 20px; text-align: center; border: 1px solid #feb2b2;
        }

        /* Password Toggle Eye */
        .password-wrapper { position: relative; }
        .toggle-password {
            position: absolute; right: 15px; top: 50%; transform: translateY(-50%);
            cursor: pointer; color: #a0aec0;
        }
        .toggle-password:hover { color: #4c51bf; }
    </style>
</head>
<body>

<div class="container">
    <div class="signin-header">
        <h1 class="signin-title">Đặt lại mật khẩu</h1>
        <h4 class="signin-subtitle">Nhập mật khẩu mới cho tài khoản của bạn.</h4>
    </div>

    <form action="reset-password" method="post" id="resetForm">

        <div class="form-group">
            <label for="newPassword" class="form-label">Mật khẩu mới</label>
            <div class="password-wrapper">
                <input type="password" id="newPassword" name="newPassword" class="form-input" required
                       placeholder="Tối thiểu 8 ký tự" minlength="8">
                <i class="fa-regular fa-eye toggle-password" onclick="togglePwd('newPassword')"></i>
            </div>
        </div>

        <div class="form-group">
            <label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
            <div class="password-wrapper">
                <input type="password" id="confirmPassword" name="confirmPassword" class="form-input" required
                       placeholder="Nhập lại mật khẩu mới">
                <i class="fa-regular fa-eye toggle-password" onclick="togglePwd('confirmPassword')"></i>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="error-message">
                <i class="fa-solid fa-circle-exclamation"></i> ${error}
            </div>
        </c:if>

        <button type="submit" class="signin-btn">Lưu mật khẩu</button>
    </form>
</div>

<script>
    function togglePwd(id) {
        const input = document.getElementById(id);
        const type = input.getAttribute('type') === 'password' ? 'text' : 'password';
        input.setAttribute('type', type);
    }

    // Client-side validation match password
    document.getElementById('resetForm').addEventListener('submit', function(e) {
        const p1 = document.getElementById('newPassword').value;
        const p2 = document.getElementById('confirmPassword').value;
        if(p1 !== p2) {
            e.preventDefault();
            alert("Mật khẩu xác nhận không khớp!");
        }
    });
</script>

</body>
</html>