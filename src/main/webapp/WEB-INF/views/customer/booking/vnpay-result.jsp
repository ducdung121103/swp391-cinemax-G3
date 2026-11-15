<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<fmt:setLocale value="en_US" />
<c:set var="pageTitle" value="Kết Quả Thanh Toán VNPay - CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5" style="max-width: 650px;">
    <div class="card bg-dark border-secondary text-white p-4 p-md-5 shadow-lg rounded-4 text-center">
        <c:choose>
            <c:when test="${isSuccess}">
                <div class="mb-3">
                    <span class="d-inline-flex align-items-center justify-content-center bg-success bg-opacity-25 text-success rounded-circle" style="width: 80px; height: 80px; font-size: 40px;">
                        <i class="fa-solid fa-check"></i>
                    </span>
                </div>
                <h3 class="fw-bold text-success mb-2">Thanh Toán Thành Công!</h3>
                <p class="text-secondary small mb-2">Giao dịch qua Cổng VNPay đã được xử lý thành công. Vé điện tử và bắp nước của bạn đã được ghi nhận vào hệ thống.</p>
                <p class="text-warning small mb-4">
                    <i class="fa-solid fa-envelope-open-text me-1"></i>
                    Email xác nhận kèm <strong>E-Ticket QR</strong> đã được gửi tới hộp thư của bạn (kiểm tra cả Spam).
                </p>

                <div class="bg-black bg-opacity-50 p-4 rounded-3 border border-secondary mb-4 text-start">
                    <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-50">
                        <span class="text-secondary">Mã đơn hàng:</span>
                        <strong class="text-warning font-monospace"><c:out value="${orderCode}" /></strong>
                    </div>
                    <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-50">
                        <span class="text-secondary">Mã giao dịch VNPay:</span>
                        <strong class="font-monospace"><c:out value="${transactionNo}" /></strong>
                    </div>
                    <c:if test="${not empty bankCode}">
                        <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-50">
                            <span class="text-secondary">Ngân hàng thanh toán:</span>
                            <strong><c:out value="${bankCode}" /></strong>
                        </div>
                    </c:if>
                    <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-50">
                        <span class="text-secondary">Phương thức:</span>
                        <span><i class="fa-solid fa-qrcode text-warning me-1"></i> VNPay QR / ATM / Quốc tế</span>
                    </div>
                    <div class="d-flex justify-content-between pt-3 fs-5">
                        <span class="fw-semibold">Tổng thanh toán:</span>
                        <strong class="text-warning fw-bold"><fmt:formatNumber value="${amount}" pattern="#,##0" groupingUsed="true" /> đ</strong>
                    </div>
                </div>

                <div class="d-grid gap-2">
                    <a href="${pageContext.request.contextPath}/customer/bookings?code=${orderCode}" class="btn btn-cinema-primary py-2 fw-semibold">
                        <i class="fa-solid fa-ticket me-2"></i> Xem Vé Điện Tử & Lịch Sử
                    </a>
                    <a href="${pageContext.request.contextPath}/home" class="btn btn-outline-secondary py-2 text-white">
                        <i class="fa-solid fa-house me-2"></i> Về Trang Chủ
                    </a>
                </div>
            </c:when>

            <c:otherwise>
                <div class="mb-3">
                    <span class="d-inline-flex align-items-center justify-content-center bg-danger bg-opacity-25 text-danger rounded-circle" style="width: 80px; height: 80px; font-size: 40px;">
                        <i class="fa-solid fa-xmark"></i>
                    </span>
                </div>
                <h3 class="fw-bold text-danger mb-2">Thanh Toán Thất Bại</h3>
                <p class="text-secondary small mb-4"><c:out value="${message}" /></p>

                <div class="bg-black bg-opacity-50 p-4 rounded-3 border border-secondary mb-4 text-start">
                    <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-50">
                        <span class="text-secondary">Mã đơn hàng:</span>
                        <strong class="text-warning font-monospace"><c:out value="${orderCode}" /></strong>
                    </div>
                    <div class="d-flex justify-content-between py-2 border-bottom border-secondary border-opacity-50">
                        <span class="text-secondary">Mã phản hồi VNPay:</span>
                        <span class="badge bg-danger"><c:out value="${responseCode}" /></span>
                    </div>
                    <div class="d-flex justify-content-between pt-2">
                        <span class="text-secondary">Trạng thái:</span>
                        <span class="text-danger fw-semibold">Giao dịch bị hủy / Không thành công</span>
                    </div>
                </div>

                <div class="d-grid gap-2">
                    <a href="${pageContext.request.contextPath}/home" class="btn btn-cinema-primary py-2 fw-semibold">
                        <i class="fa-solid fa-arrow-rotate-left me-2"></i> Thử Lại Hoặc Về Trang Chủ
                    </a>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</div>

<jsp:include page="../../common/footer.jsp" />

