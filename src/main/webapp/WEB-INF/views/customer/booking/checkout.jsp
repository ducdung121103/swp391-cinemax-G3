<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Thanh Toán Đặt Vé - Cinema Chain</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/booking.css">
</head>
<body>
    <jsp:include page="../../common/header.jsp" />

    <div class="checkout-container">
        <div class="countdown-timer">
            ⏱️ Thời gian giữ ghế còn lại: <span id="timer">05:00</span>
        </div>

        <h2>XÁC NHẬN ĐƠN ĐẶT VÉ</h2>
        <table class="order-summary-table">
            <thead>
                <tr>
                    <th>Sản phẩm</th>
                    <th>Chi tiết</th>
                    <th>Thành tiền</th>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <td><strong>Vé xem phim (Suất chiếu #${showtimeId})</strong></td>
                    <td>Ghế: VIP F08, VIP F09</td>
                    <td>190.000 đ</td>
                </tr>
                <tr>
                    <td><strong>Combo Bắp Nước Solo</strong></td>
                    <td>1 Bắp Caramel (L) + 1 Pepsi (M)</td>
                    <td>85.000 đ</td>
                </tr>
            </tbody>
            <tfoot>
                <tr>
                    <th colspan="2" style="text-align: right;">Tổng thanh toán:</th>
                    <th style="color: #e50914; font-size: 20px;">275.000 đ</th>
                </tr>
            </tfoot>
        </table>

        <div style="margin-top: 25px;">
            <h3>Phương thức thanh toán</h3>
            <label style="display: block; margin: 10px 0;"><input type="radio" name="payMethod" value="VNPAY" checked> Cổng thanh toán trực tuyến VNPAY (ATM/QR/Visa)</label>
        </div>

        <button type="button" class="btn-register" style="width: 100%; padding: 14px; font-size: 18px; margin-top: 25px; border: none; cursor: pointer;">
            Thanh Toán Ngay
        </button>
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
            timerElem.innerText = `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
            if (timeLeft <= 0) {
                clearInterval(interval);
                alert('Thời gian giữ ghế đã hết hạn! Vui lòng chọn lại ghế.');
                window.location.href = '${pageContext.request.contextPath}/movies';
            }
        }, 1000);
    </script>
</body>
</html>
