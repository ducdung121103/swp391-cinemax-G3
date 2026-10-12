<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng Nhập - CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height: 70vh;">
    <div class="card bg-dark border-secondary p-4 p-md-5 text-white shadow-lg" style="max-width: 440px; width: 100%;">
        <div class="text-center mb-4">
            <i class="fa-solid fa-clapperboard text-warning fs-1 mb-2"></i>
            <h3 class="fw-bold">ĐĂNG NHẬP</h3>
            <p class="text-secondary small">Chào mừng bạn quay lại với CineMax</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2 small fw-bold">${errorMessage}</div>
        </c:if>
        <c:if test="${param.msg == 'register_success'}">
            <div class="alert alert-success py-2 small fw-bold">Đăng ký tài khoản thành công! Vui lòng đăng nhập.</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/login" method="post">
            <div class="mb-3">
                <label class="form-label text-secondary small fw-semibold">Email đăng nhập:</label>
                <input type="email" name="email" required class="form-control form-input-cinema" value="admin@cinema.com">
            </div>
            <div class="mb-4">
                <label class="form-label text-secondary small fw-semibold">Mật khẩu:</label>
                <input type="password" name="password" required class="form-control form-input-cinema" value="123456">
            </div>
            <button type="submit" class="btn btn-cinema-primary w-100 py-2 fw-bold">
                <i class="fa-solid fa-arrow-right-to-bracket me-1"></i> Đăng Nhập
            </button>
        </form>
        <p class="mt-4 mb-0 text-center text-secondary small">
            Chưa có tài khoản? <a href="${pageContext.request.contextPath}/register" class="text-warning text-decoration-none fw-bold">Đăng ký ngay</a>
        </p>
    </div>
</div>

<jsp:include page="../../common/footer.jsp" />
