<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Quên Mật Khẩu — CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height: 80vh;">
    <div class="card p-4 p-md-5 text-white shadow-lg text-center"
         style="max-width: 460px; width: 100%; background: #141820; border: 1px solid #2E3550; border-radius: 20px;">

        <%-- ── Icon Header ── --%>
        <div class="mb-3">
            <div style="width: 72px; height: 72px; border-radius: 50%; background: rgba(229,9,20,0.12);
                        display: inline-flex; align-items: center; justify-content: center; margin-bottom: 8px;">
                <i class="fa-solid fa-key" style="font-size: 2rem; color: #E50914;"></i>
            </div>
        </div>
        <h2 class="fw-bold mb-1" style="font-size: 1.5rem; letter-spacing: 0.5px;">QUÊN MẬT KHẨU</h2>
        <p style="color: #6B7A99; font-size: 0.88rem; line-height: 1.6; margin-bottom: 24px;">
            Nhập email liên kết với tài khoản CineMax. Chúng tôi sẽ gửi một liên kết an toàn giúp bạn đặt lại mật khẩu mới.
        </p>

        <%-- ── Alerts ── --%>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success py-2 small d-flex align-items-center gap-2 text-start">
                <i class="fa-solid fa-circle-check"></i>
                ${successMessage}
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2 small fw-bold d-flex align-items-center gap-2 text-start">
                <i class="fa-solid fa-triangle-exclamation"></i>
                ${errorMessage}
            </div>
        </c:if>

        <%-- ── Form ── --%>
        <form id="forgotForm" action="${pageContext.request.contextPath}/forgot-password" method="post" novalidate>
            <div class="mb-4 text-start">
                <label for="forgotEmail" class="form-label text-secondary small fw-semibold">
                    <i class="fa-solid fa-envelope me-1"></i>Email tài khoản <span class="text-danger">*</span>
                </label>
                <input type="email" id="forgotEmail" name="email" required
                       class="form-control form-input-cinema"
                       placeholder="example@gmail.com"
                       autocomplete="email"
                       value="${not empty prevEmail ? prevEmail : ''}">
                <div class="form-text" style="color: #6B7A99; font-size: 0.78rem;">
                    ⏰ Liên kết đặt lại mật khẩu sẽ có hiệu lực trong 15 phút.
                </div>
            </div>

            <button type="submit" id="submitBtn" class="btn btn-cinema-primary w-100 py-2 fw-bold"
                    style="border-radius: 10px; font-size: 0.95rem; letter-spacing: 0.5px;">
                <i class="fa-solid fa-paper-plane me-1"></i> Gửi Link Xác Nhận
            </button>
        </form>

        <%-- ── Back to Login Link ── --%>
        <div class="mt-4 pt-3 text-center" style="border-top: 1px solid #2E3550;">
            <p class="mb-0 text-secondary small">
                Nhớ lại mật khẩu rồi?
                <a href="${pageContext.request.contextPath}/login"
                   class="text-decoration-none fw-bold" style="color:#E50914;">
                    Đăng nhập ngay <i class="fa-solid fa-arrow-right ms-1"></i>
                </a>
            </p>
        </div>
    </div>
</div>

<script>
document.getElementById('forgotForm').addEventListener('submit', function(e) {
    const email = document.getElementById('forgotEmail').value.trim();
    if (!email) {
        e.preventDefault();
        alert('Vui lòng nhập địa chỉ email của bạn!');
        return;
    }
    const btn = document.getElementById('submitBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang gửi email...';
});
</script>

<jsp:include page="../../common/footer.jsp" />
