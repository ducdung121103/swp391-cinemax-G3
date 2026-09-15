<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Soát Vé Qua Camera QR - Cinema Admin</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/operation.css">
    <!-- Thư viện quét mã QR bằng camera HTML5 -->
    <script src="https://unpkg.com/html5-qrcode" type="text/javascript"></script>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../../common/navbar.jsp" />
            <div class="admin-content">
                <h2>CỔNG SOÁT VÉ QR BẰNG CAMERA & MÃ VẠCH (TV 5)</h2>
                
                <div class="admin-card" style="max-width: 650px; margin: 20px auto; text-align: center;">
                    <div id="qr-reader" class="scanner-video-box"></div>

                    <div style="margin: 20px 0;">
                        <label style="font-weight: bold;">Hoặc nhập mã vé thủ công / Bắn súng Barcode:</label>
                        <div style="display: flex; gap: 10px; margin-top: 10px;">
                            <input type="text" id="manualBarcode" placeholder="Ví dụ: TK-1-12-8F3AC1" style="flex-grow: 1; padding: 10px; font-size: 16px;">
                            <button type="button" onclick="checkTicket(document.getElementById('manualBarcode').value)" class="btn-register" style="padding: 10px 20px; border: none; cursor: pointer;">Kiểm Tra</button>
                        </div>
                    </div>

                    <!-- Màn hình kết quả thông báo -->
                    <div id="checkResult" style="display: none; padding: 20px; border-radius: 8px; margin-top: 20px; font-size: 18px; font-weight: bold;"></div>
                </div>
            </div>
        </div>
    </div>

    <script>
        function checkTicket(barcode) {
            if (!barcode) return;
            fetch('${pageContext.request.contextPath}/admin/operation/scanner', {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: 'barcode=' + encodeURIComponent(barcode)
            })
            .then(res => res.json())
            .then(data => {
                const resBox = document.getElementById('checkResult');
                resBox.style.display = 'block';
                if (data.status === 'SUCCESS') {
                    resBox.style.background = '#d4edda';
                    resBox.style.color = '#155724';
                    resBox.innerHTML = '✅ ' + data.message + '<br><small>Mã vé: ' + barcode + '</small>';
                } else {
                    resBox.style.background = '#f8d7da';
                    resBox.style.color = '#721c24';
                    resBox.innerHTML = '❌ ' + data.message + '<br><small>Mã vé: ' + barcode + '</small>';
                }
            });
        }

        // Tự động bật camera quét QR
        function onScanSuccess(decodedText, decodedResult) {
            checkTicket(decodedText);
        }
        const html5QrcodeScanner = new Html5QrcodeScanner("qr-reader", { fps: 10, qrbox: 250 });
        html5QrcodeScanner.render(onScanSuccess);
    </script>
</body>
</html>
