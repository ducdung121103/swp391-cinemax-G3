<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Đăng Ký Thành Viên - CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5 d-flex justify-content-center align-items-center" style="min-height: 75vh;">
    <div class="card bg-dark border-secondary p-4 p-md-5 text-white shadow-lg" style="max-width: 480px; width: 100%;">
        <div class="text-center mb-4">
            <i class="fa-solid fa-clapperboard text-warning fs-1 mb-2"></i>
            <h3 class="fw-bold">ĐĂNG KÝ THÀNH VIÊN</h3>
            <p class="text-secondary small">Trở thành thành viên CineMax để nhận ưu đãi</p>
        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger py-2 small fw-bold">${errorMessage}</div>
        </c:if>

        <form action="${pageContext.request.contextPath}/register" method="post">
            <div class="mb-3">
                <label class="form-label text-secondary small fw-semibold">Họ và Tên:</label>
                <input type="text" name="fullName" required class="form-control form-input-cinema" placeholder="Nguyễn Văn A">
            </div>
            <div class="mb-3">
                <label class="form-label text-secondary small fw-semibold">Số điện thoại:</label>
                <input type="tel" name="phone" class="form-control form-input-cinema" placeholder="0901234567">
            </div>
            <div class="mb-3">
                <label class="form-label text-secondary small fw-semibold">Email đăng nhập:</label>
                <input type="email" name="email" required class="form-control form-input-cinema" placeholder="example@gmail.com">
            </div>
            <div class="mb-4">
                <label class="form-label text-secondary small fw-semibold">Mật khẩu:</label>
                <input type="password" name="password" required class="form-control form-input-cinema" placeholder="Tối thiểu 6 ký tự">
            </div>
            <button type="submit" class="btn btn-cinema-primary w-100 py-2 fw-bold">
                <i class="fa-solid fa-user-plus me-1"></i> Tạo Tài Khoản
            </button>
        </form>
        <p class="mt-4 mb-0 text-center text-secondary small">
            Đã có tài khoản? <a href="${pageContext.request.contextPath}/login" class="text-warning text-decoration-none fw-bold">Đăng nhập</a>
        </p>
    </div>
</div>

<jsp:include page="../../common/footer.jsp" />
