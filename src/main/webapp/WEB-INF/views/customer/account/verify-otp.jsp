<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Xác Thực Email — CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height: 80vh;">
    <div class="auth-card card p-4 p-md-5 text-white shadow-lg text-center"
         style="max-width: 460px; width: 100%; background: #141820; border: 1px solid #2E3550; border-radius: 20px;">

        <%-- ── Icon Header ── --%>
        <div class="mb-3">
            <div style="width: 72px; height: 72px; border-radius: 50%; background: rgba(229,9,20,0.12);
                        display: inline-flex; align-items: center; justify-content: center; margin-bottom: 8px;">
                <i class="fa-solid fa-envelope-open-text" style="font-size: 2rem; color: #E50914;"></i>
            </div>
        </div>
        <h2 class="fw-bold mb-1" style="font-size: 1.5rem; letter-spacing: 0.5px;">XÁC THỰC EMAIL</h2>
        <p style="color: #6B7A99; font-size: 0.88rem; line-height: 1.6; margin-bottom: 4px;">
            Chúng tôi đã gửi mã OTP <strong style="color: #F5C518;">6 chữ số</strong> đến
        </p>
        <p class="fw-bold mb-3" style="color: #EAEAEA; font-size: 0.95rem;">
            <i class="fa-solid fa-envelope me-1" style="color:#E50914;"></i>
            <span id="emailDisplay">${not empty verifyEmail ? verifyEmail : param.email}</span>
        </p>

        <%-- ── Alert Messages ── --%>
        <c:if test="${param.msg == 'otp_sent'}">
            <div class="alert alert-success py-2 small d-flex align-items-center gap-2 text-start">
                <i class="fa-solid fa-paper-plane"></i>
                Mã OTP đã được gửi! Vui lòng kiểm tra hộp thư (kể cả Spam).
            </div>
        </c:if>
        <c:if test="${param.msg == 'need_verify'}">
            <div class="alert alert-warning py-2 small d-flex align-items-center gap-2 text-start">
                <i class="fa-solid fa-triangle-exclamation"></i>
                Tài khoản chưa được xác thực. Vui lòng nhập mã OTP đã gửi.
            </div>
        </c:if>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success py-2 small d-flex align-items-center gap-2 text-start">
                <i class="fa-solid fa-check-circle"></i>
                ${successMessage}
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2 small fw-bold d-flex align-items-center gap-2 text-start">
                <i class="fa-solid fa-triangle-exclamation"></i>
                ${errorMessage}
            </div>
        </c:if>

        <%-- ── OTP Input Form ── --%>
        <form id="otpForm" action="${pageContext.request.contextPath}/verify-otp" method="post">
            <input type="hidden" name="email" value="${not empty verifyEmail ? verifyEmail : param.email}">
            <input type="hidden" name="action" value="verify">

            <%-- 6 ô nhập từng chữ số OTP --%>
            <div class="d-flex justify-content-center gap-2 mb-4 mt-3" id="otpBoxes">
                <c:forEach begin="1" end="6" var="i">
                    <input type="text"
                           id="digit${i}"
                           name="digit${i}"
                           maxlength="1"
                           inputmode="numeric"
                           pattern="[0-9]"
                           class="otp-digit-input text-center fw-bold"
                           style="width: 52px; height: 62px; font-size: 1.6rem; border-radius: 12px;
                                  background: #1E2333; border: 2px solid #2E3550; color: #F5C518;
                                  outline: none; transition: border-color 0.2s;"
                           autocomplete="off">
                </c:forEach>
            </div>

            <%-- Input ẩn gộp OTP (backup) --%>
            <input type="hidden" id="otpCode" name="otpCode">

            <button type="submit" id="verifyBtn"
                    class="btn btn-cinema-primary w-100 py-2 fw-bold mb-3"
                    style="border-radius: 10px; font-size: 0.95rem;">
                <i class="fa-solid fa-shield-check me-1"></i> Xác Thực Ngay
            </button>
        </form>

        <%-- ── Gửi lại OTP ── --%>
        <div class="mb-3">
            <p class="text-secondary small mb-2">Không nhận được mã?</p>

            <%-- Countdown trước khi cho gửi lại --%>
            <div id="countdownWrap">
                <span class="text-secondary small">Gửi lại sau </span>
                <span id="countdown" style="color: #F5C518; font-weight: 700;">60</span>
                <span class="text-secondary small"> giây</span>
            </div>

            <form id="resendForm" action="${pageContext.request.contextPath}/verify-otp" method="post" style="display:none;">
                <input type="hidden" name="email" value="${not empty verifyEmail ? verifyEmail : param.email}">
                <input type="hidden" name="action" value="resend">
                <button type="submit" class="btn btn-outline-warning btn-sm px-4" style="border-radius: 8px;">
                    <i class="fa-solid fa-paper-plane me-1"></i> Gửi Lại OTP
                </button>
            </form>
        </div>

        <%-- ── Back to Register ── --%>
        <div class="pt-3" style="border-top: 1px solid #2E3550;">
            <a href="${pageContext.request.contextPath}/register"
               class="text-secondary small text-decoration-none">
                <i class="fa-solid fa-arrow-left me-1"></i> Quay lại đăng ký
            </a>
        </div>
    </div>
</div>

<script>
// ── Auto-focus và di chuyển giữa các ô OTP ──────────────────────────────────
const inputs = document.querySelectorAll('.otp-digit-input');

inputs.forEach((input, idx) => {
    // Chỉ cho nhập số
    input.addEventListener('input', function () {
        this.value = this.value.replace(/[^0-9]/g, '');
        if (this.value && idx < inputs.length - 1) {
            inputs[idx + 1].focus();
        }
        syncOtpCode();
        updateBoxStyle(this);
    });

    // Backspace → về ô trước
    input.addEventListener('keydown', function (e) {
        if (e.key === 'Backspace' && !this.value && idx > 0) {
            inputs[idx - 1].focus();
            inputs[idx - 1].value = '';
            syncOtpCode();
        }
    });

    // Focus effect
    input.addEventListener('focus', function () {
        this.style.borderColor = '#E50914';
        this.style.boxShadow = '0 0 0 3px rgba(229,9,20,0.2)';
    });
    input.addEventListener('blur', function () {
        this.style.borderColor = this.value ? '#F5C518' : '#2E3550';
        this.style.boxShadow = 'none';
    });

    // Paste toàn bộ OTP (vd: 123456)
    input.addEventListener('paste', function (e) {
        e.preventDefault();
        const pasted = (e.clipboardData || window.clipboardData).getData('text').replace(/\D/g, '');
        if (pasted.length >= 6) {
            inputs.forEach((inp, i) => {
                inp.value = pasted[i] || '';
                updateBoxStyle(inp);
            });
            inputs[5].focus();
            syncOtpCode();
        }
    });
});

function updateBoxStyle(input) {
    input.style.borderColor = input.value ? '#F5C518' : '#2E3550';
}

function syncOtpCode() {
    document.getElementById('otpCode').value =
        Array.from(inputs).map(i => i.value).join('');
}

// Focus vào ô đầu tiên khi load
inputs[0].focus();

// ── Submit: validate trước ──────────────────────────────────────────────────
document.getElementById('otpForm').addEventListener('submit', function (e) {
    syncOtpCode();
    const otp = document.getElementById('otpCode').value;
    if (otp.length < 6 || !/^\d{6}$/.test(otp)) {
        e.preventDefault();
        alert('Vui lòng nhập đủ 6 chữ số OTP!');
        inputs[0].focus();
        return;
    }
    const btn = document.getElementById('verifyBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Đang xác thực...';
});

// ── Countdown 60 giây trước khi hiện nút gửi lại ───────────────────────────
let seconds = 60;
const countdownEl  = document.getElementById('countdown');
const countdownWrap = document.getElementById('countdownWrap');
const resendForm    = document.getElementById('resendForm');

const timer = setInterval(() => {
    seconds--;
    countdownEl.textContent = seconds;
    if (seconds <= 0) {
        clearInterval(timer);
        countdownWrap.style.display = 'none';
        resendForm.style.display = 'block';
    }
}, 1000);
</script>

<jsp:include page="../../common/footer.jsp" />
