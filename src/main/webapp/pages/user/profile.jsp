<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ cá nhân - ${u.username}</title>
    <!-- Bootstrap 5 CDN -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <!-- Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.0/css/all.min.css">
    <!-- Google Fonts: Poppins -->
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <style>
        body {
            font-family: 'Poppins', sans-serif;
            background: #f8f9fa;
            color: #212529;
            min-height: 100vh;
            padding-top: 80px;
        }
        .profile-container {
            max-width: 1000px;
            margin: 30px auto;
            padding: 0 15px;
        }
        .profile-card {
            background: #ffffff;
            border-radius: 16px;
            overflow: hidden;
            box-shadow: 0 10px 30px rgba(0,0,0,0.1);
            border: 1px solid #dee2e6;
        }
        .profile-header {
            background: linear-gradient(135deg, #212529 0%, #343a40 100%);
            color: #ffffff;
            padding: 40px 30px 60px;
            position: relative;
        }
        .avatar-wrapper {
            position: absolute;
            top: 30px;
            left: 30px;
        }
        .avatar-img {
            width: 110px;
            height: 110px;
            border-radius: 16px;
            object-fit: cover;
            border: 4px solid #ffffff;
            box-shadow: 0 6px 20px rgba(0,0,0,0.3);
            transition: all 0.3s ease;
        }
        .avatar-img:hover {
            transform: scale(1.08);
            box-shadow: 0 10px 30px rgba(0,0,0,0.4);
        }
        .camera-btn {
            position: absolute;
            bottom: -8px;
            right: -8px;
            background: #495057;
            color: #ffffff;
            width: 36px;
            height: 36px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            cursor: pointer;
            border: 3px solid #ffffff;
            font-size: 1rem;
            transition: all 0.3s;
        }
        .camera-btn:hover {
            background: #6c757d;
            transform: scale(1.15);
        }
        .profile-info {
            padding: 50px 30px 40px;
            background: #ffffff;
        }
        .info-title {
            font-size: 1.4rem;
            font-weight: 600;
            color: #212529;
            margin-bottom: 20px;
        }
        /* Tabs - căn giữa và tăng space giữa các nút */
        .nav-tabs {
            border-bottom: none;
            justify-content: center;
        }
        .nav-tabs .nav-item {
            margin: 0 12px; /* tăng khoảng cách giữa các tab */
        }
        .nav-tabs .nav-link {
            border: none;
            color: #6c757d;
            font-weight: 500;
            padding: 12px 32px; /* nút to hơn, dễ bấm */
            border-radius: 12px;
            background: #f1f3f5;
            transition: all 0.3s ease;
        }
        .nav-tabs .nav-link.active {
            background: #495057;
            color: #ffffff;
            font-weight: 600;
        }
        .nav-tabs .nav-link:hover {
            color: #212529;
            background: #dee2e6;
            transform: translateY(-2px);
        }
        .info-row {
            display: flex;
            align-items: center;
            padding: 16px 0;
            border-bottom: 1px solid #dee2e6;
        }
        .info-icon {
            width: 42px;
            height: 42px;
            background: #e9ecef;
            border-radius: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            margin-right: 18px;
            color: #495057;
            font-size: 1.3rem;
        }
        .info-label {
            flex: 0 0 140px;
            font-weight: 500;
            color: #495057;
        }
        .info-value {
            flex: 1;
            font-weight: 600;
            color: #212529;
        }
        .points-value {
            background: #e9ecef;
            color: #212529;
            padding: 5px 14px;
            border-radius: 20px;
            font-weight: 600;
            border: 1px solid #ced4da;
        }
        .form-label {
            font-weight: 500;
            color: #212529;
        }
        .form-control {
            background: #ffffff;
            border: 1px solid #ced4da;
            color: #212529;
        }
        .form-control:focus {
            border-color: #6c757d;
            box-shadow: 0 0 0 0.25rem rgba(108,117,125,0.25);
            color: #212529;
        }
        .form-control::placeholder {
            color: #6c757d;
        }
        .btn-primary {
            background: #495057;
            border-color: #495057;
            color: #ffffff;
        }
        .btn-primary:hover {
            background: #6c757d;
            border-color: #6c757d;
        }
        .alert {
            margin-top: 20px;
        }
        /* Responsive */
        @media (max-width: 768px) {
            .profile-info { padding: 80px 20px 30px; }
            .avatar-wrapper { top: 20px; left: 20px; }
            .avatar-img { width: 90px; height: 90px; }
            .info-label { flex: 0 0 120px; }
            .nav-tabs .nav-item { margin: 0 6px; }
            .nav-tabs .nav-link { padding: 10px 22px; font-size: 0.95rem; }
        }
    </style>
</head>
<body>
<!-- HEADER -->
<jsp:include page="/components/layout/Header.jsp" />

<div class="profile-container">
    <div class="profile-card">
        <!-- Header với avatar góc trái -->
        <div class="profile-header">
            <div class="avatar-wrapper">
                <img id="avatar"
                     class="avatar-img"
                     src="${empty u.avatarUrl ? 'assets/default-avatar.png' : u.avatarUrl}"
                     alt="Ảnh đại diện">
                <form id="avatarForm" action="${pageContext.request.contextPath}/profile/avatar"
                      method="post" enctype="multipart/form-data" style="display: none;">
                    <input type="file" name="avatarFile" id="avatarInput" accept="image/*"
                           onchange="document.getElementById('avatarForm').submit()">
                </form>
                <label for="avatarInput" class="camera-btn">
                    <i class="fas fa-camera"></i>
                </label>
            </div>
            <div class="text-center mt-4">
                <h3 class="mb-1">Xin chào, ${u.fullName}!</h3>
            </div>
        </div>

        <!-- Phần thông tin & chỉnh sửa -->
        <div class="profile-info">
            <!-- Tabs - căn giữa và tăng space -->
            <ul class="nav nav-tabs mb-5 justify-content-center" id="profileTab" role="tablist">
                <li class="nav-item" role="presentation">
                    <button class="nav-link active" id="view-tab" data-bs-toggle="tab" data-bs-target="#view" type="button" role="tab">
                        <i class="fas fa-eye me-2"></i>Xem hồ sơ
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="edit-info-tab" data-bs-toggle="tab" data-bs-target="#edit-info" type="button" role="tab">
                        <i class="fas fa-user-edit me-2"></i>Cập nhật thông tin
                    </button>
                </li>
                <li class="nav-item" role="presentation">
                    <button class="nav-link" id="edit-password-tab" data-bs-toggle="tab" data-bs-target="#edit-password" type="button" role="tab">
                        <i class="fas fa-lock me-2"></i>Cập nhật mật khẩu
                    </button>
                </li>
            </ul>

            <!-- Hiển thị thông báo -->
            <c:if test="${not empty message}">
                <div class="alert alert-success alert-dismissible fade show" role="alert">
                    ${message}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>
            <c:if test="${not empty error}">
                <div class="alert alert-danger alert-dismissible fade show" role="alert">
                    ${error}
                    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                </div>
            </c:if>

            <div class="tab-content">
                <!-- Tab Xem hồ sơ -->
                <div class="tab-pane fade show active" id="view" role="tabpanel">
                    <h4 class="info-title">Thông tin cá nhân</h4>
                    <div class="info-row">
                        <div class="info-icon"><i class="fas fa-user"></i></div>
                        <span class="info-label">Họ và tên</span>
                        <span class="info-value">${u.fullName}</span>
                    </div>
                    <div class="info-row">
                        <div class="info-icon"><i class="fas fa-envelope"></i></div>
                        <span class="info-label">Email</span>
                        <span class="info-value">${u.email}</span>
                        <div class="invalid-feedback">Vui lòng nhập email hợp lệ.</div>
                    </div>
                    <div class="info-row">
                        <div class="info-icon"><i class="fas fa-phone"></i></div>
                        <span class="info-label">Số điện thoại</span>
                        <span class="info-value">${u.phone}</span>
                        <div class="invalid-feedback">Số điện thoại phải có 10 chữ số.</div>
                    </div>
                    <div class="info-row">
                        <div class="info-icon"><i class="fas fa-birthday-cake"></i></div>
                        <span class="info-label">Ngày sinh</span>
                        <span class="info-value">
                            ${u.birthday != null ? u.birthday.toLocalDate() : 'Chưa cập nhật'}
                        </span>
                    </div>
                    <div class="info-row">
                        <div class="info-icon"><i class="fas fa-star"></i></div>
                        <span class="info-label">Điểm tích lũy</span>
                        <span class="info-value">
                            <span class="points-value">${u.points} điểm</span>
                        </span>
                    </div>
                </div>

                <!-- Tab Cập nhật thông tin cá nhân -->
                <div class="tab-pane fade" id="edit-info" role="tabpanel">
                    <h4 class="info-title">Cập nhật thông tin cá nhân</h4>
                    <form action="${pageContext.request.contextPath}/profile/update-info" method="post">
                        <div class="row g-4">
                            <div class="col-12">
                                <label class="form-label">Họ và tên</label>
                                <input type="text" class="form-control px-3 py-2" name="fullName" value="${u.fullName}" required>
                            </div>

                            <div class="col-12">
                                <label class="form-label">Email</label>
                                <input type="email" class="form-control px-3 py-2" name="email" value="${u.email}" required>
                                <div class="invalid-feedback">Vui lòng nhập email hợp lệ.</div>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label">Số điện thoại</label>
                                <input type="tel" class="form-control px-3 py-2" name="phone" value="${u.phone}">
                                <div class="invalid-feedback">Số điện thoại phải có 10 chữ số.</div>
                            </div>

                            <div class="col-md-6">
                                <label class="form-label">Ngày sinh</label>
                                <input type="date" class="form-control px-3 py-2" 
                                       name="birthday" 
                                       id="birthday"
                                       min="1950-01-01" 
                                       max="<%= java.time.LocalDate.now() %>"
                                       value="${u.birthday != null ? u.birthday.toLocalDate() : ''}"
                                       onblur="let today='<%= java.time.LocalDate.now() %>'; 
                                               if(this.value) { 
                                                   if(this.value < '1950-01-01') this.value = '1950-01-01'; 
                                                   if(this.value > today) this.value = today; 
                                               }">
                                <div class="invalid-feedback">Ngày sinh phải từ năm 1950 đến hiện tại.</div>
                            </div>
                        </div>

                        <div class="mt-5 text-center">
                            <button type="submit" class="btn btn-primary px-5 py-3 fw-bold shadow-sm">
                                <i class="fas fa-save me-2"></i>Lưu thông tin
                            </button>
                        </div>
                    </form>
                </div>

                <!-- Tab Cập nhật mật khẩu -->
                <div class="tab-pane fade" id="edit-password" role="tabpanel">
                    <h4 class="info-title">Cập nhật mật khẩu</h4>
                    <form action="${pageContext.request.contextPath}/profile/update-password" method="post">
                        <div class="row g-4">
                            <div class="col-12">
                                <label class="form-label">Mật khẩu hiện tại</label>
                                <input type="password" class="form-control" name="currentPassword" required>
                            </div>
                            <div class="col-12">
                                <label class="form-label">Mật khẩu mới</label>
                                <input type="password" class="form-control" name="newPassword" required>
                            </div>
                            <div class="col-12">
                                <label class="form-label">Xác nhận mật khẩu mới</label>
                                <input type="password" class="form-control" name="confirmPassword" required>
                            </div>
                        </div>
                        <div class="mt-5 text-center">
                            <button type="submit" class="btn btn-primary px-5 py-3">Cập nhật mật khẩu</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    document.addEventListener("DOMContentLoaded", function() {
        // --- 1. Validate Ngày sinh (Ép giá trị 1950 - Hiện tại) ---
        const birthdayInput = document.getElementById('birthday');
        if (birthdayInput) {
            birthdayInput.addEventListener('blur', function() {
                const minDate = "1950-01-01";
                const today = new Date().toISOString().split('T')[0];
                if (this.value) {
                    if (this.value < minDate) this.value = minDate;
                    if (this.value > today) this.value = today;
                }
            });
        }

        // --- 2. Validate Email (Định dạng) ---
        const emailInput = document.querySelector('input[name="email"]');
        emailInput.addEventListener('blur', function() {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(this.value)) {
                alert("Email không đúng định dạng!");
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        });

        // --- 3. Validate Số điện thoại (10 số) ---
        const phoneInput = document.querySelector('input[name="phone"]');
        phoneInput.addEventListener('blur', function() {
            const phoneRegex = /^[0-9]{10}$/;
            if (this.value && !phoneRegex.test(this.value)) {
                alert("Số điện thoại phải bao gồm 10 chữ số!");
                this.classList.add('is-invalid');
            } else {
                this.classList.remove('is-invalid');
            }
        });

        // --- 4. Validate Khớp mật khẩu ---
        const newPass = document.querySelector('input[name="newPassword"]');
        const confirmPass = document.querySelector('input[name="confirmPassword"]');
        
        function checkPasswordMatch() {
            if (confirmPass.value && newPass.value !== confirmPass.value) {
                confirmPass.setCustomValidity("Mật khẩu xác nhận không khớp");
                confirmPass.classList.add('is-invalid');
            } else {
                confirmPass.setCustomValidity("");
                confirmPass.classList.remove('is-invalid');
            }
        }

        newPass.addEventListener('change', checkPasswordMatch);
        confirmPass.addEventListener('keyup', checkPasswordMatch);
    });
</script>
</body>
</html>