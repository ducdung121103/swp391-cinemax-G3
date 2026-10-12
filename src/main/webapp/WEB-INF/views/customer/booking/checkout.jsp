<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Thanh Toán Đặt Vé - CineMax" />
<jsp:include page="../../common/header.jsp" />

<div class="container py-5" style="max-width: 800px;">
    <!-- Countdown Timer Alert -->
    <div class="alert alert-warning d-flex align-items-center justify-content-between p-3 rounded-3 mb-4 shadow-sm">
        <div class="d-flex align-items-center gap-2">
            <i class="fa-regular fa-clock fs-5"></i>
            <span class="fw-semibold">Thời gian giữ ghế còn lại:</span>
        </div>
        <span id="timer" class="badge bg-danger fs-5 px-3 py-2">05:00</span>
    </div>

    <div class="card bg-dark border-secondary text-white p-4 p-md-5 shadow-lg rounded-4">
        <h3 class="fw-bold text-center mb-4 text-warning">
            <i class="fa-solid fa-receipt me-2"></i> XÁC NHẬN ĐƠN ĐẶT VÉ
        </h3>

        <div class="table-responsive mb-4">
            <table class="table table-dark table-borderless align-middle mb-0">
                <thead class="border-bottom border-secondary">
                    <tr class="text-secondary small text-uppercase">
                        <th>Sản phẩm</th>
                        <th>Chi tiết</th>
                        <th class="text-end">Thành tiền</th>
                    </tr>
                </thead>
                <tbody>
                    <tr class="border-bottom border-secondary border-opacity-50">
                        <td><strong>Vé xem phim</strong></td>
                        <td>Ghế: VIP F08, VIP F09 (Suất #${showtimeId})</td>
                        <td class="text-end text-warning fw-semibold">190.000 đ</td>
                    </tr>
                    <tr class="border-bottom border-secondary border-opacity-50">
                        <td><strong>Combo Bắp Nước Solo</strong></td>
                        <td>1 Bắp Caramel (L) + 1 Pepsi (M)</td>
                        <td class="text-end text-warning fw-semibold">85.000 đ</td>
                    </tr>
                </tbody>
                <tfoot>
                    <tr>
                        <th colspan="2" class="text-end fs-5 text-white pt-3">Tổng thanh toán:</th>
                        <th class="text-end fs-4 text-danger fw-bold pt-3">275.000 đ</th>
                    </tr>
                </tfoot>
            </table>
        </div>

        <div class="bg-black bg-opacity-50 p-3 rounded-3 border border-secondary mb-4">
            <h5 class="fw-semibold text-white mb-2 fs-6">Phương thức thanh toán</h5>
            <div class="form-check">
                <input class="form-check-input" type="radio" name="payMethod" value="VNPAY" id="payVnpay" checked>
                <label class="form-check-label small" for="payVnpay">
                    <i class="fa-solid fa-qrcode text-warning me-1"></i> Cổng thanh toán trực tuyến <strong>VNPAY</strong> (ATM Nội Địa / QR Code / Thẻ Quốc Tế Visa/Master)
                </label>
            </div>
        </div>

        <button type="button" class="btn btn-cinema-primary w-100 py-3 fs-5 fw-bold shadow">
            <i class="fa-solid fa-lock me-2"></i> Thanh Toán Ngay
        </button>
    </div>
</div>

<jsp:include page="../../common/footer.jsp" />

<script>
    // Đồng hồ đếm ngược 5 phút
    let timeLeft = 300;
    const timerElem = document.getElementById('timer');
    const interval = setInterval(() => {
        timeLeft--;
        const mins = Math.floor(timeLeft / 60);
        const secs = timeLeft % 60;
        if (timerElem) {
            timerElem.innerText = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
        }
        if (timeLeft <= 0) {
            clearInterval(interval);
            alert('Thời gian giữ ghế đã hết hạn! Vui lòng chọn lại ghế.');
            window.location.href = '${pageContext.request.contextPath}/movies';
        }
    }, 1000);
</script>
