<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cinema - Đăng ký</title>

    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link href="${pageContext.request.contextPath}/web/img/logo/favicon/DC-Symbol.png" rel="icon"/>
    <script src="${pageContext.request.contextPath}/js/validator.js"></script>

    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }

        body {
            font-family: 'Roboto', sans-serif;
            background: linear-gradient(135deg, #9ACBD0 0%, #006A71 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 20px;
            position: relative;
        }

        /* Nút quay về trang chủ - Đồng bộ với trang Login */
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

        /* Container chứa form */
        .container {
            width: 100%;
            max-width: 550px; /* Rộng hơn login một chút vì nhiều trường hơn */
            background: white;
            border-radius: 20px;
            box-shadow: 0 20px 40px rgba(0, 0, 0, 0.2);
            padding: 40px;
            position: relative;
            animation: slideUp 0.5s ease-out;
            margin-top: 20px;
            margin-bottom: 20px;
        }

        @keyframes slideUp {
            from { opacity: 0; transform: translateY(20px); }
            to { opacity: 1; transform: translateY(0); }
        }

        .signup-header { margin-bottom: 25px; text-align: center; }
        .signup-title { font-size: 28px; font-weight: 700; color: #1a202c; margin-bottom: 5px; }

        .form-group { margin-bottom: 15px; }
        .form-label { display: block; margin-bottom: 6px; color: #4a5568; font-weight: 600; font-size: 14px; }

        .form-input {
            width: 100%;
            padding: 10px 14px;
            border: 2px solid #e2e8f0;
            border-radius: 8px;
            font-size: 15px;
            transition: all 0.3s ease;
            outline: none;
            background: #f8fafc;
        }

        .form-input:focus {
            border-color: #4c51bf;
            box-shadow: 0 0 0 3px rgba(76, 81, 191, 0.1);
            background: white;
        }

        .form-row {
            display: flex;
            gap: 15px;
        }
        .form-row .form-group { flex: 1; }

        .checkbox-group {
            display: flex;
            align-items: flex-start;
            gap: 10px;
            margin-bottom: 20px;
            margin-top: 10px;
        }
        .checkbox {
            width: 18px;
            height: 18px;
            accent-color: #4c51bf;
            margin-top: 2px;
            flex-shrink: 0;
        }
        .checkbox-label { color: #4a5568; font-size: 13px; line-height: 1.4; }
        .checkbox-label a { color: #FFACAC; text-decoration: none; font-weight: 500; }
        .checkbox-label a:hover { text-decoration: underline; color: #006A71; }

        .signup-btn {
            width: 100%;
            padding: 14px;
            background: linear-gradient(135deg, #FFBFA9 0%, #FFACAC 100%);
            color: white;
            border: none;
            border-radius: 10px;
            font-size: 16px;
            font-weight: 700;
            cursor: pointer;
            transition: all 0.3s ease;
            margin-bottom: 20px;
            box-shadow: 0 4px 12px rgba(255, 172, 172, 0.4);
        }
        .signup-btn:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 20px rgba(255, 172, 172, 0.6);
        }
        .signup-btn:disabled { opacity: 0.7; cursor: not-allowed; }

        .signin-link { text-align: center; color: #718096; text-decoration: none; font-size: 14px; display: block; }
        .signin-link strong { color: #FFACAC; font-weight: 700; }
        .signin-link:hover strong { color: #006A71; }

        .alert-error {
            background-color: #fff5f5;
            color: #e53e3e;
            border: 1px solid #feb2b2;
            padding: 10px 12px;
            border-radius: 8px;
            margin-bottom: 15px;
            font-size: 14px;
            text-align: center;
            animation: shake 0.4s ease-in-out;
        }

        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            25% { transform: translateX(-5px); }
            75% { transform: translateX(5px); }
        }

        /* Responsive */
        @media (max-width: 600px) {
            .container { padding: 30px 20px; }
            .form-row { flex-direction: column; gap: 0; }
            .back-home-btn { top: 15px; left: 15px; background: white; color: #006A71; }
        }
    </style>
</head>
<body>

<a href="${pageContext.request.contextPath}/home" class="back-home-btn" title="Quay về trang chủ">
    <i class="fa-solid fa-arrow-left"></i>
</a>

<div class="container">
    <div class="signup-header">
        <h1 class="signup-title">Đăng ký tài khoản</h1>
    </div>

    <form action="register" method="post" id="signupForm">

        <div class="form-group">
            <label for="fullName" class="form-label">Họ và tên</label>
            <input
                    type="text"
                    id="fullName"
                    name="fullName"
                    class="form-input"
                    required
                    pattern="^[a-zA-ZÀ-ỹ\s]{2,100}$"
                    title="Họ tên phải từ 2 ký tự trở lên"
                    value="${fullName != null ? fullName : param.fullName}"
                    placeholder="Nhập họ và tên của bạn"
            >
        </div>

        <div class="form-group">
            <label for="username" class="form-label">Tên đăng nhập</label>
            <input
                    type="text"
                    id="username"
                    name="username"
                    class="form-input"
                    required
                    pattern="^(?=.{3,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$"
                    title="Tên đăng nhập phải từ 3-20 ký tự, chỉ bao gồm chữ cái, số và dấu chấm"
                    value="${username != null ? username : param.username}"
                    placeholder="Chọn tên đăng nhập"
            >
        </div>

        <div class="form-row">
            <div class="form-group">
                <label for="email" class="form-label">Email</label>
                <input
                        type="email"
                        id="email"
                        name="email"
                        class="form-input"
                        required
                        pattern="^[a-zA-Z0-9._%+-]+@gmail\.com$/,}"
                        title="Email không hợp lệ"
                        value="${email != null ? email : param.email}"
                        placeholder="vidu@email.com"
                >
            </div>

            <div class="form-group">
                <label for="phone" class="form-label">Số điện thoại</label>
                <input
                        type="text"
                        class="form-input"
                        id="phone"
                        name="phone"
                        placeholder="0xxxxxxxxx"
                        required
                        pattern="^0\d{9}$"
                        title="Số điện thoại phải bắt đầu bằng 0 và có 10-11 số"
                        value="${phone != null ? phone : param.phone}"
                >
            </div>
        </div>

        <div class="form-group">
            <label for="birthday" class="form-label">Ngày sinh</label>
            <input
                    type="date"
                    id="birthday"
                    name="birthday"
                    class="form-input"
                    value="${birthday != null ? birthday : param.birthday}"
                    max="2026-01-01"
            >
        </div>

        <div class="form-row">
            <div class="form-group">
                <label for="password" class="form-label">Mật khẩu</label>
                <input
                        type="password"
                        id="password"
                        name="password"
                        class="form-input"
                        required
                        placeholder="Tối thiểu 8 ký tự"
                        pattern="^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&]).{8,}$"
                        title="Mật khẩu >8 ký tự, gồm chữ, số và ký tự đặc biệt"
                />
            </div>
            <div class="form-group">
                <label for="confirmPassword" class="form-label">Xác nhận mật khẩu</label>
                <input
                        type="password"
                        id="confirmPassword"
                        name="confirmPassword"
                        class="form-input"
                        required
                        placeholder="Nhập lại mật khẩu"
                />
            </div>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-error" id="serverErrorMessage" style="display: block;">
                    ${error}
            </div>
        </c:if>
        <div class="alert alert-error" id="clientErrorMessage" style="display: none;">
            Vui lòng kiểm tra lại thông tin!
        </div>

        <button type="submit" class="signup-btn">Tạo tài khoản</button>
    </form>

    <a href="${pageContext.request.contextPath}/login" class="signin-link">
        Bạn đã có tài khoản? <strong>Đăng nhập ngay</strong>
    </a>
</div>

<script>
    document.getElementById('signupForm').addEventListener('submit', function (e) {
        // Lấy giá trị
        const fullName = document.getElementById('fullName').value.trim();
        const phone = document.getElementById('phone').value.trim();
        const email = document.getElementById('email').value.trim();
        const username = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value;
        const confirmPassword = document.getElementById('confirmPassword').value;
        const birthday = document.getElementById('birthday').value;

        const clientErrorMessage = document.getElementById('clientErrorMessage');
        if(clientErrorMessage) clientErrorMessage.style.display = 'none';

        const showError = (msg) => {
            e.preventDefault();
            clientErrorMessage.textContent = msg;
            clientErrorMessage.style.display = 'block';
        };

        // Check Required
        if (!fullName || !phone || !email || !username || !password || !confirmPassword) {
            return showError(Validator.messages.required);
        }

        // Validate FullName
        if (!Validator.isValid('fullName', fullName)) {
            return showError(Validator.messages.fullName);
        }

        // Validate Phone
        if (!Validator.isValid('phone', phone)) {
            return showError(Validator.messages.phone);
        }

        // Validate Email
        if (!Validator.isValid('email', email)) {
            return showError(Validator.messages.email);
        }

        // Validate Username
        if (!Validator.isValid('username', username)) {
            return showError(Validator.messages.username);
        }

        // Validate Birthday
        if (birthday) {
            const selectedDate = new Date(birthday);
            const maxDate = new Date('2026-01-01');
            if (selectedDate > maxDate) {
                return showError('Ngày sinh không được vượt quá ngày 01/01/2026');
            }
        }

        // Validate Password
        if (!Validator.isValid('password', password)) {
            return showError(Validator.messages.password);
        }

        // Validate Confirm Password
        if (password !== confirmPassword) {
            return showError(Validator.messages.confirmPassword);
        }
        return true;
    });

    // Auto-hide error messages
    const errorMessages = document.querySelectorAll('.alert-error');
    errorMessages.forEach(function(errorMsg) {
        if (errorMsg.style.display === 'block') {
            setTimeout(() => {
                errorMsg.style.display = 'none';
            }, 5000);
        }
    });

    // Clear client error messages when user starts typing
    const formInputs = document.querySelectorAll('.form-input');
    const clientErrorMessage = document.getElementById('clientErrorMessage');
    formInputs.forEach(input => {
        input.addEventListener('input', function() {
            if(clientErrorMessage) clientErrorMessage.style.display = 'none';
        });
    });

    // Add loading state
    const submitBtn = document.querySelector('.signup-btn');
    const form = document.getElementById('signupForm');
    form.addEventListener('submit', function () {
        submitBtn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang tạo tài khoản...';
        submitBtn.disabled = true;
        setTimeout(() => {
            submitBtn.innerHTML = 'Tạo tài khoản';
            submitBtn.disabled = false;
        }, 3000);
    });
</script>
</body>
</html>