<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng Nhập — CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height: 80vh;">
    <div class="auth-card card p-4 p-md-5 text-white shadow-lg" style="max-width: 440px; width: 100%; background: #141820; border: 1px solid #2E3550; border-radius: 20px;">

        <%-- ── Brand Header ── --%>
        <div class="text-center mb-4">
            <div class="brand-icon-wrap mb-3">
                <i class="fa-solid fa-clapperboard" style="font-size: 2.5rem; color: #E50914;"></i>
            </div>
            <h2 class="fw-bold mb-1" style="font-size: 1.6rem; letter-spacing: 1px;">ĐĂNG NHẬP</h2>
            <p class="mb-0" style="color: #6B7A99; font-size: 0.88rem;">Chào mừng bạn quay lại <strong style="color:#F5C518;">CineMax</strong></p>
        </div>

        <%-- ── Alert Messages ── --%>
        <c:if test="${param.msg == 'verified'}">
            <div class="alert alert-success py-2 small fw-bold d-flex align-items-center gap-2">
                <i class="fa-solid fa-circle-check"></i>
                Xác thực email thành công! Hãy đăng nhập.
            </div>
        </c:if>
        <c:if test="${param.msg == 'logged_out'}">
            <div class="alert alert-secondary py-2 small d-flex align-items-center gap-2">
                <i class="fa-solid fa-right-from-bracket"></i>
                Bạn đã đăng xuất thành công.
            </div>
        </c:if>
        <c:if test="${param.msg == 'register_success'}">
            <div class="alert alert-success py-2 small d-flex align-items-center gap-2">
                <i class="fa-solid fa-envelope-circle-check"></i>
                Đăng ký thành công! Vui lòng đăng nhập.
            </div>
        </c:if>
        <c:if test="${param.msg == 'locked'}">
            <div class="alert alert-danger py-2 small fw-bold d-flex align-items-center gap-2">
                <i class="fa-solid fa-lock"></i>
                Tài khoản đã bị khóa
            </div>
        </c:if>
        <c:if test="${param.msg == 'reset_success'}">
            <div class="alert alert-success py-2 small fw-bold d-flex align-items-center gap-2">
                <i class="fa-solid fa-circle-check"></i>
                Đặt lại mật khẩu thành công! Vui lòng đăng nhập bằng mật khẩu mới.
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2 small fw-bold d-flex align-items-center gap-2">
                <i class="fa-solid fa-triangle-exclamation"></i>
                ${errorMessage}
            </div>
        </c:if>

        <%-- ── Login Form ── --%>
        <form id="loginForm" action="${pageContext.request.contextPath}/login" method="post" novalidate>
            <%-- Giữ redirect param nếu có --%>
            <c:if test="${not empty param.redirect}">
                <input type="hidden" name="redirect" value="${param.redirect}">
            </c:if>

            <div class="mb-3">
                <label for="loginEmail" class="form-label text-secondary small fw-semibold">
                    <i class="fa-solid fa-envelope me-1"></i>Email đăng nhập
                </label>
                <input type="email" id="loginEmail" name="email" required
                       class="form-control form-input-cinema"
                       placeholder="example@gmail.com"
                       autocomplete="email">
            </div>

            <div class="mb-2 position-relative">
                <label for="loginPassword" class="form-label text-secondary small fw-semibold">
                    <i class="fa-solid fa-lock me-1"></i>Mật khẩu
                </label>
                <div class="input-group">
                    <input type="password" id="loginPassword" name="password" required
                           class="form-control form-input-cinema border-end-0"
                           placeholder="Nhập mật khẩu"
                           autocomplete="current-password">
                    <button type="button" class="btn btn-outline-secondary border-0 border-start-0"
                            id="togglePassword"
                            style="background: #1E2333; border: 1px solid #2E3550 !important; border-left: none !important;"
                            onclick="togglePwd()">
                        <i id="eyeIcon" class="fa-regular fa-eye" style="color:#6B7A99;"></i>
                    </button>
                </div>
            </div>

            <div class="d-flex justify-content-end mb-4">
                <a href="${pageContext.request.contextPath}/forgot-password"
                   class="text-decoration-none small" style="color:#F5C518;">
                    <i class="fa-solid fa-key me-1"></i>Quên mật khẩu?
                </a>
            </div>

            <button type="submit" id="loginBtn" class="btn btn-cinema-primary w-100 py-2 fw-bold" style="border-radius: 10px; font-size: 0.95rem; letter-spacing: 0.5px;">
                <i class="fa-solid fa-arrow-right-to-bracket me-1"></i> Đăng Nhập
            </button>
        </form>

        <%-- ── Register Link ── --%>
        <div class="mt-4 pt-3 text-center" style="border-top: 1px solid #2E3550;">
            <p class="mb-0 text-secondary small">
                Chưa có tài khoản?
                <a href="${pageContext.request.contextPath}/register"
                   class="text-decoration-none fw-bold" style="color:#E50914;">
                    Đăng ký ngay <i class="fa-solid fa-arrow-right ms-1"></i>
                </a>
            </p>
        </div>
    </div>
</div>

<script>
function togglePwd() {
    const pwd = document.getElementById('loginPassword');
    const icon = document.getElementById('eyeIcon');
    if (pwd.type === 'password') {
        pwd.type = 'text';
        icon.className = 'fa-regular fa-eye-slash';
    } else {
        pwd.type = 'password';
        icon.className = 'fa-regular fa-eye';
    }
}

// Loading state khi submit
document.getElementById('loginForm').addEventListener('submit', function() {
    const btn = document.getElementById('loginBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang đăng nhập...';
});
</script>

<jsp:include page="../../common/footer.jsp" />
