<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Hồ sơ — CineMax" />
<c:set var="flashKey" value="${sessionScope.profileFlashKey}" />
<c:set var="flashValue" value="${sessionScope.profileFlashValue}" />
<c:remove var="profileFlashKey" scope="session" />
<c:remove var="profileFlashValue" scope="session" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5" style="max-width: 760px;">
    <h2 class="text-white fw-bold mb-4">Hồ sơ của tôi</h2>

    <c:if test="${flashKey == 'msg'}">
        <div class="alert alert-success">
            <c:choose>
                <c:when test="${flashValue == 'password_changed'}">Đã đổi mật khẩu.</c:when>
                <c:otherwise>Đã lưu hồ sơ.</c:otherwise>
            </c:choose>
        </div>
    </c:if>
    <c:if test="${flashKey == 'error'}">
        <div class="alert alert-danger">
            <c:choose>
                <c:when test="${flashValue == 'PHONE_TAKEN'}">Số điện thoại đã thuộc tài khoản khác.</c:when>
                <c:when test="${flashValue == 'PHONE'}">Số điện thoại không đúng định dạng Việt Nam.</c:when>
                <c:when test="${flashValue == 'NAME'}">Họ tên không được trống và tối đa 120 ký tự.</c:when>
                <c:when test="${flashValue == 'AVATAR'}">Đường dẫn ảnh tối đa 255 ký tự.</c:when>
                <c:when test="${flashValue == 'WEAK_PASSWORD'}">Mật khẩu mới cần tối thiểu 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.</c:when>
                <c:when test="${flashValue == 'PASSWORD_MISMATCH'}">Mật khẩu nhập lại không khớp.</c:when>
                <c:when test="${flashValue == 'BAD_PASSWORD'}">Mật khẩu cũ không đúng.</c:when>
                <c:otherwise>Không lưu được. Thử lại.</c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <div class="card p-4 mb-4 text-white" style="background:#141820;border:1px solid #2E3550;">
        <h5 class="mb-3">Thông tin cá nhân</h5>
        <form method="post" action="${pageContext.request.contextPath}/customer/account/profile">
            <input type="hidden" name="action" value="profile" />
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input class="form-control" value="${profileUser.email}" disabled />
            </div>
            <div class="mb-3">
                <label class="form-label" for="fullName">Họ tên</label>
                <input class="form-control" id="fullName" name="fullName" maxlength="120" required value="${profileUser.fullName}" />
            </div>
            <div class="mb-3">
                <label class="form-label" for="phone">Số điện thoại</label>
                <input class="form-control" id="phone" name="phone" maxlength="20" value="${profileUser.phone}" placeholder="0901234567" />
            </div>
            <div class="mb-3">
                <label class="form-label" for="avatarUrl">Ảnh đại diện (URL)</label>
                <input class="form-control" id="avatarUrl" name="avatarUrl" maxlength="255" value="${profileUser.avatarUrl}" />
            </div>
            <button class="btn btn-cinema-primary" type="submit">Lưu hồ sơ</button>
        </form>
    </div>

    <div class="card p-4 text-white" style="background:#141820;border:1px solid #2E3550;">
        <h5 class="mb-3">Đổi mật khẩu</h5>
        <form method="post" action="${pageContext.request.contextPath}/customer/account/profile">
            <input type="hidden" name="action" value="password" />
            <div class="mb-3">
                <label class="form-label" for="oldPassword">Mật khẩu cũ</label>
                <input class="form-control" id="oldPassword" name="oldPassword" type="password" required />
            </div>
            <div class="mb-3">
                <label class="form-label" for="newPassword">Mật khẩu mới</label>
                <input class="form-control" id="newPassword" name="newPassword" type="password" required />
            </div>
            <div class="mb-3">
                <label class="form-label" for="confirmPassword">Nhập lại mật khẩu mới</label>
                <input class="form-control" id="confirmPassword" name="confirmPassword" type="password" required />
            </div>
            <button class="btn btn-outline-light" type="submit">Đổi mật khẩu</button>
        </form>
    </div>
</div>

<jsp:include page="../../common/footer.jsp" />
