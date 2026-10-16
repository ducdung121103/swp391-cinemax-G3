<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng Ký Thành Viên — CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height: 85vh;">
    <div class="auth-card card p-4 p-md-5 text-white shadow-lg" style="max-width: 500px; width: 100%; background: #141820; border: 1px solid #2E3550; border-radius: 20px;">

        <%-- ── Brand Header ── --%>
        <div class="text-center mb-4">
            <div class="mb-3">
                <i class="fa-solid fa-user-plus" style="font-size: 2.3rem; color: #E50914;"></i>
            </div>
            <h2 class="fw-bold mb-1" style="font-size: 1.6rem; letter-spacing: 1px;">ĐĂNG KÝ THÀNH VIÊN</h2>
            <p class="mb-0" style="color: #6B7A99; font-size: 0.88rem;">
                Trở thành thành viên <strong style="color:#F5C518;">CineMax</strong> để đặt vé dễ dàng
            </p>
        </div>

        <%-- ── Error Message ── --%>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2 small fw-bold d-flex align-items-center gap-2">
                <i class="fa-solid fa-triangle-exclamation"></i>
                ${errorMessage}
            </div>
        </c:if>

        <%-- ── Register Form ── --%>
        <form id="registerForm" action="${pageContext.request.contextPath}/register" method="post" novalidate>

            <%-- Họ và Tên --%>
            <div class="mb-3">
                <label for="regFullName" class="form-label text-secondary small fw-semibold">
                    <i class="fa-solid fa-user me-1"></i>Họ và Tên <span class="text-danger">*</span>
                </label>
                <input type="text" id="regFullName" name="fullName" required
                       class="form-control form-input-cinema"
                       placeholder="Nguyễn Văn A"
                       value="${not empty prevFullName ? prevFullName : ''}">
            </div>

            <%-- Số điện thoại --%>
            <div class="mb-3">
                <label for="regPhone" class="form-label text-secondary small fw-semibold">
                    <i class="fa-solid fa-phone me-1"></i>Số điện thoại
                </label>
                <input type="tel" id="regPhone" name="phone"
                       class="form-control form-input-cinema"
                       placeholder="0901234567"
                       pattern="[0-9]{10,11}"
                       value="${not empty prevPhone ? prevPhone : ''}">
                <div class="form-text" style="color: #6B7A99; font-size: 0.78rem;">Tùy chọn. 10–11 chữ số.</div>
            </div>

            <%-- Email --%>
            <div class="mb-3">
                <label for="regEmail" class="form-label text-secondary small fw-semibold">
                    <i class="fa-solid fa-envelope me-1"></i>Email đăng nhập <span class="text-danger">*</span>
                </label>
                <input type="email" id="regEmail" name="email" required
                       class="form-control form-input-cinema"
                       placeholder="example@gmail.com"
                       autocomplete="email"
                       value="${not empty prevEmail ? prevEmail : ''}">
            </div>

            <%-- Mật khẩu --%>
            <div class="mb-3">
                <label for="regPassword" class="form-label text-secondary small fw-semibold">
                    <i class="fa-solid fa-lock me-1"></i>Mật khẩu <span class="text-danger">*</span>
                </label>
                <div class="input-group">
                    <input type="password" id="regPassword" name="password" required
                           class="form-control form-input-cinema border-end-0"
                           placeholder="Tối thiểu 8 ký tự (hoa, thường, số, ký tự đặc biệt)"
                           autocomplete="new-password"
                           minlength="8"
                           oninput="checkStrength(this.value); checkMatch()">
                    <button type="button"
                            class="btn btn-outline-secondary border-0 border-start-0"
                            style="background: #1E2333; border: 1px solid #2E3550 !important; border-left: none !important;"
                            onclick="togglePwdField('regPassword', 'eyeIcon1')">
                        <i id="eyeIcon1" class="fa-regular fa-eye" style="color:#6B7A99;"></i>
                    </button>
                </div>
                <div class="form-text" style="color: #6B7A99; font-size: 0.78rem;">
                    Bao gồm ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt.
                </div>
                <%-- Thanh độ mạnh mật khẩu --%>
                <div id="strengthBar" class="mt-1" style="display:none;">
                    <div style="height:4px; border-radius:4px; background:#2E3550; overflow:hidden;">
                        <div id="strengthFill" style="height:100%; width:0%; border-radius:4px; transition:width 0.3s, background 0.3s;"></div>
                    </div>
                    <span id="strengthText" class="small" style="font-size:0.75rem;"></span>
                </div>
            </div>

            <%-- Xác nhận mật khẩu --%>
            <div class="mb-4">
                <label for="regConfirmPassword" class="form-label text-secondary small fw-semibold">
                    <i class="fa-solid fa-lock me-1"></i>Xác nhận mật khẩu <span class="text-danger">*</span>
                </label>
                <div class="input-group">
                    <input type="password" id="regConfirmPassword" name="confirmPassword" required
                           class="form-control form-input-cinema border-end-0"
                           placeholder="Nhập lại mật khẩu"
                           autocomplete="new-password"
                           oninput="checkMatch()">
                    <button type="button"
                            class="btn btn-outline-secondary border-0 border-start-0"
                            style="background: #1E2333; border: 1px solid #2E3550 !important; border-left: none !important;"
                            onclick="togglePwdField('regConfirmPassword', 'eyeIcon2')">
                        <i id="eyeIcon2" class="fa-regular fa-eye" style="color:#6B7A99;"></i>
                    </button>
                </div>
                <div id="matchMessage" class="small mt-1" style="font-size:0.78rem;"></div>
            </div>

            <%-- Submit Button --%>
            <button type="submit" id="registerBtn" class="btn btn-cinema-primary w-100 py-2 fw-bold" style="border-radius: 10px; font-size: 0.95rem; letter-spacing: 0.5px;">
                <i class="fa-solid fa-user-plus me-1"></i> Tạo Tài Khoản
            </button>
        </form>

        <%-- ── Login Link ── --%>
        <div class="mt-4 pt-3 text-center" style="border-top: 1px solid #2E3550;">
            <p class="mb-0 text-secondary small">
                Đã có tài khoản?
                <a href="${pageContext.request.contextPath}/login"
                   class="text-decoration-none fw-bold" style="color:#E50914;">
                    Đăng nhập <i class="fa-solid fa-arrow-right ms-1"></i>
                </a>
            </p>
        </div>
    </div>
</div>

<script>
// Toggle show/hide password
function togglePwdField(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon  = document.getElementById(iconId);
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fa-regular fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fa-regular fa-eye';
    }
}

// Kiểm tra độ mạnh mật khẩu
function checkStrength(value) {
    const bar  = document.getElementById('strengthBar');
    const fill = document.getElementById('strengthFill');
    const text = document.getElementById('strengthText');
    if (!value) { bar.style.display = 'none'; return; }
    bar.style.display = 'block';

    let score = 0;
    if (value.length >= 6)  score++;
    if (value.length >= 10) score++;
    if (/[A-Z]/.test(value)) score++;
    if (/[0-9]/.test(value)) score++;
    if (/[^A-Za-z0-9]/.test(value)) score++;

    const levels = [
        { pct: '20%', color: '#E50914', label: 'Rất yếu' },
        { pct: '40%', color: '#FF6B35', label: 'Yếu' },
        { pct: '60%', color: '#F5C518', label: 'Trung bình' },
        { pct: '80%', color: '#28a745', label: 'Mạnh' },
        { pct: '100%', color: '#00C851', label: 'Rất mạnh' }
    ];
    const lvl = levels[Math.min(score - 1, 4)] || levels[0];
    fill.style.width    = lvl.pct;
    fill.style.background = lvl.color;
    text.textContent    = lvl.label;
    text.style.color    = lvl.color;
}

// Kiểm tra khớp mật khẩu
function checkMatch() {
    const pwd     = document.getElementById('regPassword').value;
    const confirm = document.getElementById('regConfirmPassword').value;
    const msg     = document.getElementById('matchMessage');
    if (!confirm) { msg.textContent = ''; return; }
    if (pwd === confirm) {
        msg.textContent = '✓ Mật khẩu khớp';
        msg.style.color = '#28a745';
    } else {
        msg.textContent = '✗ Mật khẩu chưa khớp';
        msg.style.color = '#E50914';
    }
}

// Validate trước khi submit + loading state
document.getElementById('registerForm').addEventListener('submit', function(e) {
    const pwd     = document.getElementById('regPassword').value;
    const confirm = document.getElementById('regConfirmPassword').value;
    if (pwd !== confirm) {
        e.preventDefault();
        document.getElementById('matchMessage').textContent = '✗ Mật khẩu xác nhận chưa khớp!';
        document.getElementById('matchMessage').style.color = '#E50914';
        return;
    }
    const strongPwdRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;
    if (!strongPwdRegex.test(pwd)) {
        e.preventDefault();
        alert('Mật khẩu phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt!');
        return;
    }
    const btn = document.getElementById('registerBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang tạo tài khoản...';
});
</script>

<jsp:include page="../../common/footer.jsp" />
