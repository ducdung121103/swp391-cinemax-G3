<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cinema - Đăng nhập</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="${pageContext.request.contextPath}/web/img/logo/favicon/DC-Symbol.png" rel="icon"/>

    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Roboto', sans-serif;
            background: linear-gradient(135deg, #9ACBD0 0%, #006A71 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            position: relative; /* Để nút back căn theo body */
        }

        /* Nút quay về trang chủ */
        .back-home-btn {
            position: absolute;
            top: 30px;
            left: 30px;
            color: white;
            font-size: 24px;
            text-decoration: none;
            width: 45px;
            height: 45px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            background: rgba(255, 255, 255, 0.2);
            backdrop-filter: blur(5px);
            transition: all 0.3s ease;
            z-index: 10;
        }

        .back-home-btn:hover {
            background: white;
            color: #006A71;
            transform: translateX(-5px);
            box-shadow: 0 5px 15px rgba(0,0,0,0.2);
        }

        /* Container giờ chỉ chứa form login */
        .container {
            width: 100%;
            max-width: 450px; /* Thu nhỏ chiều rộng lại cho cân đối */
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
            padding: 50px 40px;
            position: relative;
            animation: slideUp 0.5s ease-out;
        }

        @keyframes slideUp {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .signin-header { margin-bottom: 30px; text-align: center; }
        .signin-title { font-size: 28px; font-weight: 700; color: #1a202c; margin-bottom: 8px; }
        .signin-subtitle { color: #718096; font-size: 15px; }

        .signin-form { width: 100%; }

        .form-group { margin-bottom: 20px; }
        .form-label { display: block; margin-bottom: 8px; color: #4a5568; font-weight: 600; font-size: 14px; }

        .form-input {
            width: 100%;
            padding: 12px 16px;
            border: 2px solid #e2e8f0;
            border-radius: 10px;
            font-size: 16px;
            transition: all 0.3s ease;
            outline: none;
            background: #f8fafc;
        }

        .form-input:focus {
            border-color: #4c51bf;
            box-shadow: 0 0 0 4px rgba(76, 81, 191, 0.1);
            background: white;
        }

        .form-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 25px;
        }

        .show-password label {
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            color: #4a5568;
            cursor: pointer;
        }

        .show-password input[type="checkbox"] {
            width: 16px;
            height: 16px;
            accent-color: #4c51bf;
        }

        .forgot-password-link {
            color: #FFACAC;
            text-decoration: none;
            font-size: 14px;
            font-weight: 500;
            transition: color 0.3s ease;
        }
        .forgot-password-link:hover { color: #006A71; text-decoration: underline; }

        .signin-btn {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #FFACAC 0%, #FFBFA9 100%);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.3s ease;
            margin-bottom: 25px;
            box-shadow: 0 4px 12px rgba(255, 172, 172, 0.4);
        }

        .signin-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(255, 172, 172, 0.6);
        }
        .signin-btn:active { transform: translateY(0); }
        .signin-btn:disabled { opacity: 0.7; cursor: not-allowed; }

        .signup-link-group { text-align: center; margin-bottom: 25px; padding-top: 20px; border-top: 1px solid #eee; }
        .signup-link { color: #718096; text-decoration: none; font-size: 14px; }
        .signup-link strong { color: #FFACAC; font-weight: 700; }
        .signup-link:hover strong { color: #006A71; }

        .error-message {
            background: #fff5f5;
            color: #c53030;
            padding: 12px;
            border-radius: 8px;
            font-size: 14px;
            margin-bottom: 20px;
            text-align: center;
            border: 1px solid #feb2b2;
            animation: shake 0.4s ease-in-out;
        }

        .alert-success {
            background-color: #f0fff4;
            color: #2f855a;
            padding: 12px;
            border-radius: 8px;
            border: 1px solid #9ae6b4;
            text-align: center;
            margin-bottom: 20px;
        }

        .footer-links { display: flex; justify-content: center; gap: 20px; }
        .footer-links a { color: #a0aec0; text-decoration: none; font-size: 13px; transition: color 0.3s ease; }
        .footer-links a:hover { color: #006A71; }

        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-5px); }
            75% { transform: translateX(5px); }
        }

        /* Responsive */
        @media (max-width: 480px) {
            .container {
                max-width: 90%;
                padding: 30px 20px;
            }
            .back-home-btn {
                top: 20px;
                left: 20px;
                background: white;
                color: #006A71;
            }
        }
    </style>
</head>
<body>

<a href="${pageContext.request.contextPath}/home" class="back-home-btn" title="Quay về trang chủ">
    <i class="fa-solid fa-arrow-left"></i>
</a>

<div class="container">
    <div class="signin-header">
        <h1 class="signin-title">Đăng nhập</h1>
        <h4 class="signin-subtitle">Chào mừng bạn trở lại với MyCinema!</h4>
    </div>

    <form action="login" method="post" id="signinForm" class="signin-form">

        <c:if test="${not empty message}">
            <div class="alert alert-success">
                    ${message}
            </div>
        </c:if>

        <div class="form-group">
            <label for="username" class="form-label">Tên đăng nhập</label>
            <input
                    type="text"
                    id="username"
                    name="username"
                    class="form-input"
                    required
                    placeholder="Nhập tên đăng nhập"
                    value="${username != null ? username : ''}"
            >
        </div>

        <div class="form-group">
            <label for="password" class="form-label">Mật khẩu</label>
            <input
                    type="password"
                    id="password"
                    name="password"
                    class="form-input"
                    required
                    placeholder="Nhập mật khẩu"
            >
        </div>

        <div class="form-row">
            <div class="show-password">
                <label>
                    <input
                            type="checkbox"
                            id="showPassword"
                            onclick="document.getElementById('password').type = this.checked ? 'text' : 'password';"
                    >
                    Hiển thị
                </label>
            </div>

            <div class="forgot-password-group">
                <a href="${pageContext.request.contextPath}/forgot-password" class="forgot-password-link">Quên mật khẩu?</a>
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="error-message" id="errorMessage" style="display: block;">
                    ${error}
            </div>
        </c:if>
        <div class="error-message" id="clientErrorMessage" style="display: none;">
            Tên người dùng hoặc mật khẩu không chính xác!
        </div>

        <button type="submit" class="signin-btn" value="Login">Đăng nhập</button>
    </form>

    <div class="signup-link-group">
        <a href="${pageContext.request.contextPath}/register" class="signup-link">
            Bạn chưa có tài khoản? <strong>Đăng ký ngay</strong>
        </a>
    </div>

    <div class="footer-links">
        <a href="#">Điều khoản</a>
        <a href="#">Chính sách</a>
        <a href="#">Hỗ trợ</a>
    </div>
</div>

<script>
    // Form validation and interactions
    document.getElementById('signinForm').addEventListener('submit', function (e) {
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const errorMessage = document.getElementById('errorMessage');
        const clientErrorMessage = document.getElementById('clientErrorMessage');

        // Hide previous errors
        if(errorMessage) errorMessage.style.display = 'none';
        clientErrorMessage.style.display = 'none';

        // Basic validation
        if (!username || !password) {
            e.preventDefault();
            clientErrorMessage.textContent = 'Vui lòng điền đầy đủ thông tin!';
            clientErrorMessage.style.display = 'block';
            return false;
        }
    });

    // Add loading state
    const submitBtn = document.querySelector('.signin-btn');
    const form = document.getElementById('signinForm');

    form.addEventListener('submit', function () {
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
        submitBtn.disabled = true;
        setTimeout(() => {
            submitBtn.innerHTML = 'Đăng nhập';
            submitBtn.disabled = false;
        }, 3000);
    });

    // Auto-hide error messages
    const errorMessages = document.querySelectorAll('.error-message');
    errorMessages.forEach(function(errorMsg) {
        if (errorMsg.style.display === 'block') {
            setTimeout(() => {
                errorMsg.style.display = 'none';
            }, 5000);
        }
    });

    // Clear errors on input
    const userInput = document.getElementById('username');
    const passwordInput = document.getElementById('password');
    const clientErrorMessage = document.getElementById('clientErrorMessage');

    userInput.addEventListener('input', function() {
        clientErrorMessage.style.display = 'none';
    });
    passwordInput.addEventListener('input', function() {
        clientErrorMessage.style.display = 'none';
    });
</script>
</body>
</html>