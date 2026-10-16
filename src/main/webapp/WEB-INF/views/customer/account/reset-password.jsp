<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đặt Lại Mật Khẩu — CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height: 80vh;">
    <div class="card p-4 p-md-5 text-white shadow-lg"
         style="max-width: 480px; width: 100%; background: #141820; border: 1px solid #2E3550; border-radius: 20px;">

        <%-- ── Icon Header ── --%>
        <div class="text-center mb-3">
            <div style="width: 72px; height: 72px; border-radius: 50%; background: rgba(229,9,20,0.12);
                        display: inline-flex; align-items: center; justify-content: center; margin-bottom: 8px;">
                <i class="fa-solid fa-shield-halved" style="font-size: 2rem; color: #E50914;"></i>
            </div>
            <h2 class="fw-bold mb-1" style="font-size: 1.5rem; letter-spacing: 0.5px;">ĐẶT LẠI MẬT KHẨU</h2>
            <p style="color: #6B7A99; font-size: 0.88rem; line-height: 1.5;">
                Vui lòng nhập mật khẩu mới bảo mật cho tài khoản của bạn.
            </p>
        </div>

        <%-- ── Alerts ── --%>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2 small fw-bold d-flex align-items-center gap-2">
                <i class="fa-solid fa-triangle-exclamation"></i>
                ${errorMessage}
            </div>
        </c:if>

        <c:choose>
            <%-- Trường hợp Token hết hạn hoặc không hợp lệ --%>
            <c:when test="${tokenInvalid == true}">
                <div class="text-center py-4">
                    <div class="mb-3">
                        <i class="fa-solid fa-clock-rotate-left text-warning" style="font-size: 3rem;"></i>
                    </div>
                    <h5 class="fw-bold text-white mb-2">Liên kết đã hết hiệu lực</h5>
                    <p style="color: #6B7A99; font-size: 0.88rem; line-height: 1.6;">
                        Liên kết đặt lại mật khẩu chỉ có hiệu lực trong 15 phút hoặc đã được sử dụng trước đó.
                    </p>
                    <a href="${pageContext.request.contextPath}/forgot-password"
                       class="btn btn-cinema-primary px-4 py-2 mt-2 fw-bold"
                       style="border-radius: 10px;">
                        <i class="fa-solid fa-paper-plane me-1"></i> Yêu Cầu Liên Kết Mới
                    </a>
                </div>
            </c:when>

            <%-- Trường hợp Token hợp lệ: Hiển thị form đặt mật khẩu mới --%>
            <c:otherwise>
                <form id="resetForm" action="${pageContext.request.contextPath}/reset-password" method="post" novalidate>
                    <input type="hidden" name="token" value="${token}">

                    <%-- Mật khẩu mới --%>
                    <div class="mb-3">
                        <label for="newPassword" class="form-label text-secondary small fw-semibold">
                            <i class="fa-solid fa-lock me-1"></i>Mật khẩu mới <span class="text-danger">*</span>
                        </label>
                        <div class="input-group">
                            <input type="password" id="newPassword" name="password" required
                                   class="form-control form-input-cinema border-end-0"
                                   placeholder="Tối thiểu 8 ký tự (hoa, thường, số, đặc biệt)"
                                   autocomplete="new-password"
                                   minlength="8"
                                   oninput="checkStrength(this.value); checkMatch()">
                            <button type="button"
                                    class="btn btn-outline-secondary border-0 border-start-0"
                                    style="background: #1E2333; border: 1px solid #2E3550 !important; border-left: none !important;"
                                    onclick="togglePwdField('newPassword', 'eyeIcon1')">
                                <i id="eyeIcon1" class="fa-regular fa-eye" style="color:#6B7A99;"></i>
                            </button>
                        </div>
                        <div class="form-text" style="color: #6B7A99; font-size: 0.78rem;">
                            Bao gồm ít nhất 8 ký tự, 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt.
                        </div>
                        <%-- Thanh độ mạnh --%>
                        <div id="strengthBar" class="mt-1" style="display:none;">
                            <div style="height:4px; border-radius:4px; background:#2E3550; overflow:hidden;">
                                <div id="strengthFill" style="height:100%; width:0%; border-radius:4px; transition:width 0.3s, background 0.3s;"></div>
                            </div>
                            <span id="strengthText" class="small" style="font-size:0.75rem;"></span>
                        </div>
                    </div>

                    <%-- Xác nhận mật khẩu mới --%>
                    <div class="mb-4">
                        <label for="confirmNewPassword" class="form-label text-secondary small fw-semibold">
                            <i class="fa-solid fa-lock me-1"></i>Xác nhận mật khẩu mới <span class="text-danger">*</span>
                        </label>
                        <div class="input-group">
                            <input type="password" id="confirmNewPassword" name="confirmPassword" required
                                   class="form-control form-input-cinema border-end-0"
                                   placeholder="Nhập lại mật khẩu mới"
                                   autocomplete="new-password"
                                   oninput="checkMatch()">
                            <button type="button"
                                    class="btn btn-outline-secondary border-0 border-start-0"
                                    style="background: #1E2333; border: 1px solid #2E3550 !important; border-left: none !important;"
                                    onclick="togglePwdField('confirmNewPassword', 'eyeIcon2')">
                                <i id="eyeIcon2" class="fa-regular fa-eye" style="color:#6B7A99;"></i>
                            </button>
                        </div>
                        <div id="matchMessage" class="small mt-1" style="font-size:0.78rem;"></div>
                    </div>

                    <button type="submit" id="submitBtn" class="btn btn-cinema-primary w-100 py-2 fw-bold"
                            style="border-radius: 10px; font-size: 0.95rem; letter-spacing: 0.5px;">
                        <i class="fa-solid fa-check me-1"></i> Cập Nhật Mật Khẩu
                    </button>
                </form>
            </c:otherwise>
        </c:choose>

        <%-- ── Footer Link ── --%>
        <div class="mt-4 pt-3 text-center" style="border-top: 1px solid #2E3550;">
            <p class="mb-0 text-secondary small">
                Quay lại
                <a href="${pageContext.request.contextPath}/login"
                   class="text-decoration-none fw-bold" style="color:#E50914;">
                    Đăng nhập <i class="fa-solid fa-arrow-right ms-1"></i>
                </a>
            </p>
        </div>
    </div>
</div>

<script>
function togglePwdField(inputId, iconId) {
    const input = document.getElementById(inputId);
    const icon  = document.getElementById(iconId);
    if (!input) return;
    if (input.type === 'password') {
        input.type = 'text';
        icon.className = 'fa-regular fa-eye-slash';
    } else {
        input.type = 'password';
        icon.className = 'fa-regular fa-eye';
    }
}

function checkStrength(value) {
    const bar  = document.getElementById('strengthBar');
    const fill = document.getElementById('strengthFill');
    const text = document.getElementById('strengthText');
    if (!bar) return;
    if (!value) { bar.style.display = 'none'; return; }
    bar.style.display = 'block';

    let score = 0;
    if (value.length >= 8)  score++;
    if (value.length >= 12) score++;
    if (/[a-z]/.test(value)) score++;
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
    const lvl = levels[Math.min(Math.floor(score * 5 / 6), 4)] || levels[0];
    fill.style.width    = lvl.pct;
    fill.style.background = lvl.color;
    text.textContent    = lvl.label;
    text.style.color    = lvl.color;
}

function checkMatch() {
    const pwd     = document.getElementById('newPassword');
    const confirm = document.getElementById('confirmNewPassword');
    const msg     = document.getElementById('matchMessage');
    if (!pwd || !confirm || !msg) return;
    if (!confirm.value) { msg.textContent = ''; return; }
    if (pwd.value === confirm.value) {
        msg.textContent = '✓ Mật khẩu khớp';
        msg.style.color = '#28a745';
    } else {
        msg.textContent = '✗ Mật khẩu chưa khớp';
        msg.style.color = '#E50914';
    }
}

const resetForm = document.getElementById('resetForm');
if (resetForm) {
    resetForm.addEventListener('submit', function(e) {
        const pwd     = document.getElementById('newPassword').value;
        const confirm = document.getElementById('confirmNewPassword').value;
        const strongPwdRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).{8,}$/;

        if (pwd !== confirm) {
            e.preventDefault();
            document.getElementById('matchMessage').textContent = '✗ Mật khẩu xác nhận chưa khớp!';
            document.getElementById('matchMessage').style.color = '#E50914';
            return;
        }

        if (!strongPwdRegex.test(pwd)) {
            e.preventDefault();
            alert('Mật khẩu mới phải có ít nhất 8 ký tự, bao gồm ít nhất 1 chữ hoa, 1 chữ thường, 1 số và 1 ký tự đặc biệt!');
            return;
        }

        const btn = document.getElementById('submitBtn');
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang cập nhật...';
    });
}
</script>

<jsp:include page="../../common/footer.jsp" />
