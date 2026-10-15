<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Cấu Hình Tham Số Hệ Thống - Cinema Admin</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">
    <style>
        .settings-grid {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 24px;
            margin-bottom: 30px;
        }
        @media (max-width: 992px) {
            .settings-grid { grid-template-columns: 1fr; }
        }

        .setting-card {
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            padding: 22px;
            border-top: 4px solid #e50914;
        }
        .setting-card.card-booking { border-top-color: #e50914; }
        .setting-card.card-showtime { border-top-color: #3182ce; }
        .setting-card.card-security { border-top-color: #805ad5; }
        .setting-card.card-payment { border-top-color: #38a169; }
        .setting-card.card-loyalty { border-top-color: #d69e2e; }

        .setting-card-header {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 16px;
            border-bottom: 1px solid #edf2f7;
            padding-bottom: 12px;
        }
        .setting-card-header h3 {
            margin: 0;
            font-size: 1.15rem;
            color: #1a202c;
        }
        .setting-card-header .icon-badge {
            font-size: 1.3rem;
            width: 36px;
            height: 36px;
            display: flex;
            align-items: center;
            justify-content: center;
            background: #f7fafc;
            border-radius: 8px;
        }

        .setting-item {
            margin-bottom: 18px;
        }
        .setting-item:last-child { margin-bottom: 0; }
        .setting-label {
            display: flex;
            justify-content: space-between;
            align-items: center;
            font-weight: 600;
            font-size: 0.85rem;
            color: #2d3748;
            margin-bottom: 6px;
        }
        .setting-code {
            font-family: monospace;
            font-size: 0.75rem;
            color: #718096;
            background: #edf2f7;
            padding: 2px 6px;
            border-radius: 4px;
        }
        .setting-desc {
            font-size: 0.78rem;
            color: #718096;
            margin-top: 4px;
        }
        .form-input {
            width: 100%;
            padding: 9px 12px;
            border: 1px solid #cbd5e0;
            border-radius: 6px;
            font-size: 0.9rem;
            box-sizing: border-box;
            background: #fcfdfd;
            transition: border-color 0.2s;
        }
        .form-input:focus {
            border-color: #e50914;
            outline: none;
            background: #fff;
        }

        .btn-primary {
            background: #e50914;
            color: #fff;
            padding: 10px 22px;
            border: none;
            border-radius: 6px;
            font-weight: 600;
            cursor: pointer;
            font-size: 0.95rem;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: background 0.2s;
        }
        .btn-primary:hover { background: #b20710; }
        .btn-secondary {
            background: #4a5568;
            color: #fff;
            padding: 9px 16px;
            border: none;
            border-radius: 6px;
            font-weight: 500;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 6px;
        }
        .btn-secondary:hover { background: #2d3748; }

        /* Modal styling */
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0; right: 0; bottom: 0;
            background: rgba(0, 0, 0, 0.55);
            z-index: 999;
            align-items: center;
            justify-content: center;
        }
        .modal-overlay.active { display: flex; }
        .modal-content {
            background: #fff;
            width: 100%;
            max-width: 520px;
            border-radius: 10px;
            padding: 24px;
            box-shadow: 0 10px 25px rgba(0,0,0,0.2);
        }
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 18px;
            border-bottom: 1px solid #e2e8f0;
            padding-bottom: 12px;
        }
        .modal-header h3 { margin: 0; color: #1a202c; font-size: 1.25rem; }
        .close-modal { font-size: 1.5rem; cursor: pointer; color: #a0aec0; border: none; background: none; }
        .form-group { margin-bottom: 14px; }
        .form-group label { display: block; margin-bottom: 6px; font-weight: 600; font-size: 0.85rem; color: #4a5568; }

        .alert-toast {
            padding: 12px 18px;
            border-radius: 6px;
            margin-bottom: 20px;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }
        .alert-success { background: #c6f6d5; color: #22543d; border-left: 5px solid #38a169; }
        .alert-error { background: #fed7d7; color: #742a2a; border-left: 5px solid #e53e3e; }
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../../common/sidebar.jsp" />
        <div class="admin-main">
            <jsp:include page="../../common/navbar.jsp" />
            <div class="admin-content">

                <!-- Alert Message -->
                <c:if test="${not empty msg}">
                    <div class="alert-toast alert-success">
                        <span>
                            <i class="fa-solid fa-circle-check" style="margin-right: 6px;"></i>
                            <c:choose>
                                <c:when test="${msg == 'all_saved'}">Đã lưu toàn bộ tham số cấu hình hệ thống thành công!</c:when>
                                <c:when test="${msg == 'single_saved'}">Đã thêm mới/cập nhật tham số thành công!</c:when>
                                <c:otherwise>Thao tác thành công!</c:otherwise>
                            </c:choose>
                        </span>
                        <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem;">&times;</button>
                    </div>
                </c:if>
                <c:if test="${not empty error}">
                    <div class="alert-toast alert-error">
                        <span>
                            <i class="fa-solid fa-triangle-exclamation" style="margin-right: 6px;"></i>
                            ${error}
                        </span>
                        <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem;">&times;</button>
                    </div>
                </c:if>

                <!-- Thanh tiêu đề & Nút hành động -->
                <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; flex-wrap: wrap; gap: 12px;">
                    <div>
                        <h2 style="margin: 0; font-size: 1.4rem;">Cấu Hình Tham Số Toàn Cục Hệ Thống (M-11)</h2>
                        <p style="margin: 4px 0 0 0; color: #718096; font-size: 0.85rem;">
                            Quản lý các thông số cốt lõi: Giữ ghế, Thời gian vệ sinh, Bảo mật OTP, Cổng thanh toán.
                        </p>
                    </div>
                    <div style="display: flex; gap: 10px;">
                        <button type="button" class="btn-secondary" onclick="openNewSettingModal()">
                            <i class="fa-solid fa-plus"></i> Thêm Tham Số Mới
                        </button>
                        <button type="button" class="btn-primary" onclick="document.getElementById('settingsForm').submit()">
                            <i class="fa-solid fa-floppy-disk"></i> Lưu Toàn Bộ Cấu Hình
                        </button>
                    </div>
                </div>

                <!-- FORM CẬP NHẬT TẤT CẢ CÁC SETTING -->
                <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/settings" id="settingsForm">
                    <input type="hidden" name="action" value="saveAll">

                    <div class="settings-grid">

                        <!-- 1. Cấu hình Đặt vé & Giữ ghế (BOOKING) -->
                        <div class="setting-card card-booking">
                            <div class="setting-card-header">
                                <div class="icon-badge">
                                    <i class="fa-solid fa-ticket"></i>
                                </div>
                                <div>
                                    <h3>Cấu Hình Đặt Vé & Giữ Ghế (Booking Engine)</h3>
                                    <small style="color: #718096;">Ảnh hưởng trực tiếp đến M-05.1 & M-05.3 của TV 4</small>
                                </div>
                            </div>

                            <div class="setting-item">
                                <div class="setting-label">
                                    <span>Thời gian giữ ghế tạm thời (giây) (*):</span>
                                    <span class="setting-code">SEAT_HOLD_DURATION_SECONDS</span>
                                </div>
                                <input type="number" name="setting_SEAT_HOLD_DURATION_SECONDS" class="form-input" 
                                       value="${settingsMap['SEAT_HOLD_DURATION_SECONDS'] != null ? settingsMap['SEAT_HOLD_DURATION_SECONDS'] : '300'}" 
                                       min="60" max="3600" required>
                                <div class="setting-desc">
                                    <i class="fa-solid fa-circle-info" style="color: #0284c7; margin-right: 4px;"></i> 300 giây tương đương với <strong>5 phút</strong>. Ghế sẽ tự động nhả nếu khách không thanh toán kịp.
                                </div>
                            </div>

                            <div class="setting-item">
                                <div class="setting-label">
                                    <span>Thời hạn cho phép hoàn hủy vé (giờ trước giờ chiếu):</span>
                                    <span class="setting-code">CANCELLATION_DEADLINE_HOURS</span>
                                </div>
                                <input type="number" name="setting_CANCELLATION_DEADLINE_HOURS" class="form-input" 
                                       value="${settingsMap['CANCELLATION_DEADLINE_HOURS'] != null ? settingsMap['CANCELLATION_DEADLINE_HOURS'] : '2'}" 
                                       min="0" max="48" required>
                                <div class="setting-desc">
                                    Khách hàng chỉ được phép hủy vé trực tuyến trước giờ phim chiếu tối thiểu số giờ này.
                                </div>
                            </div>
                        </div>

                        <!-- 2. Cấu hình Lịch chiếu & Vệ sinh phòng (SHOWTIME) -->
                        <div class="setting-card card-showtime">
                            <div class="setting-card-header">
                                <div class="icon-badge">
                                    <i class="fa-solid fa-clock"></i>
                                </div>
                                <div>
                                    <h3>Cấu Hình Xếp Lịch Chiếu (Showtime Buffer)</h3>
                                    <small style="color: #718096;">Quy chuẩn an toàn giữa 2 suất chiếu (M-03.1)</small>
                                </div>
                            </div>

                            <div class="setting-item">
                                <div class="setting-label">
                                    <span>Thời gian giãn cách dọn dẹp vệ sinh (phút) (*):</span>
                                    <span class="setting-code">CLEANING_BUFFER_MINUTES</span>
                                </div>
                                <input type="number" name="setting_CLEANING_BUFFER_MINUTES" class="form-input" 
                                       value="${settingsMap['CLEANING_BUFFER_MINUTES'] != null ? settingsMap['CLEANING_BUFFER_MINUTES'] : '15'}" 
                                       min="5" max="60" required>
                                <div class="setting-desc">
                                    Khoảng thời gian nghỉ tối thiểu để nhân viên dọn dẹp phòng chiếu trước khi đón lượt khách tiếp theo.
                                </div>
                            </div>
                        </div>

                        <!-- 3. Cấu hình Bảo mật & Xác thực (SECURITY) -->
                        <div class="setting-card card-security">
                            <div class="setting-card-header">
                                <div class="icon-badge">
                                    <i class="fa-solid fa-shield-halved"></i>
                                </div>
                                <div>
                                    <h3>Bảo Mật, OTP & Chữ Ký Số QR (Security)</h3>
                                    <small style="color: #718096;">Chống giả mạo vé và rò rỉ tài khoản (M-01.2 & M-07.1)</small>
                                </div>
                            </div>

                            <div class="setting-item">
                                <div class="setting-label">
                                    <span>Thời hạn mã xác thực OTP gửi qua email (giây):</span>
                                    <span class="setting-code">OTP_EXPIRY_SECONDS</span>
                                </div>
                                <input type="number" name="setting_OTP_EXPIRY_SECONDS" class="form-input" 
                                       value="${settingsMap['OTP_EXPIRY_SECONDS'] != null ? settingsMap['OTP_EXPIRY_SECONDS'] : '300'}" 
                                       min="30" max="1800" required>
                                <div class="setting-desc">Thời gian sống của mã OTP 6 số dùng để kích hoạt tài khoản hoặc reset mật khẩu.</div>
                            </div>

                            <div class="setting-item">
                                <div class="setting-label">
                                    <span>Thời hạn link đặt lại mật khẩu (phút):</span>
                                    <span class="setting-code">PASSWORD_RESET_TOKEN_EXPIRY_MINUTES</span>
                                </div>
                                <input type="number" name="setting_PASSWORD_RESET_TOKEN_EXPIRY_MINUTES" class="form-input" 
                                       value="${settingsMap['PASSWORD_RESET_TOKEN_EXPIRY_MINUTES'] != null ? settingsMap['PASSWORD_RESET_TOKEN_EXPIRY_MINUTES'] : '15'}" 
                                       min="5" max="120" required>
                            </div>

                            <div class="setting-item">
                                <div class="setting-label">
                                    <span>Khóa bí mật ký số vé QR (HMAC-SHA256 Secret):</span>
                                    <span class="setting-code">QR_HMAC_SECRET</span>
                                </div>
                                <input type="text" name="setting_QR_HMAC_SECRET" class="form-input" 
                                       value="${settingsMap['QR_HMAC_SECRET'] != null ? settingsMap['QR_HMAC_SECRET'] : 'CINEMA_SECURE_QR_HMAC_SECRET_2026'}" 
                                       required>
                                <div class="setting-desc">Dùng để sinh và kiểm tra chữ ký số chống làm giả vé QR khi quét tại cửa rạp.</div>
                            </div>
                        </div>

                        <!-- 4. Cấu hình Cổng thanh toán (PAYMENT) -->
                        <div class="setting-card card-payment">
                            <div class="setting-card-header">
                                <div class="icon-badge">
                                    <i class="fa-solid fa-credit-card"></i>
                                </div>
                                <div>
                                    <h3>Cổng Thanh Toán Trực Tuyến (VNPAY)</h3>
                                    <small style="color: #718096;">Tích hợp Sandbox & Production Cổng VNPAY (M-06.1)</small>
                                </div>
                            </div>

                            <div class="setting-item">
                                <div class="setting-label">
                                    <span>Mã định danh Merchant VNPAY (TMN Code):</span>
                                    <span class="setting-code">VNPAY_TMN_CODE</span>
                                </div>
                                <input type="text" name="setting_VNPAY_TMN_CODE" class="form-input" 
                                       value="${settingsMap['VNPAY_TMN_CODE'] != null ? settingsMap['VNPAY_TMN_CODE'] : 'DEMO_TMN'}" 
                                       required>
                            </div>

                            <div class="setting-item">
                                <div class="setting-label">
                                    <span>Mã băm bí mật Checksum (Hash Secret HMAC-SHA512):</span>
                                    <span class="setting-code">VNPAY_HASH_SECRET</span>
                                </div>
                                <input type="password" name="setting_VNPAY_HASH_SECRET" class="form-input" 
                                       value="${settingsMap['VNPAY_HASH_SECRET'] != null ? settingsMap['VNPAY_HASH_SECRET'] : 'DEMO_HASH_SECRET_KEY_CINEMA_2026'}" 
                                       required>
                                <div class="setting-desc">Bảo đảm tính toàn vẹn gói tin IPN Webhook phản hồi từ VNPAY.</div>
                            </div>
                        </div>

                        <!-- 5. Cấu hình Tích điểm Hội viên (LOYALTY) -->
                        <div class="setting-card card-loyalty" style="grid-column: 1 / -1;">
                            <div class="setting-card-header">
                                <div class="icon-badge">
                                    <i class="fa-solid fa-star"></i>
                                </div>
                                <div>
                                    <h3>Chương Trình Khách Hàng Thân Thiết (Loyalty Points)</h3>
                                    <small style="color: #718096;">Tỷ lệ quy đổi điểm tích lũy và tiêu điểm (Zone 2)</small>
                                </div>
                            </div>

                            <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 20px;">
                                <div class="setting-item">
                                    <div class="setting-label">
                                        <span>Chi tiêu bao nhiêu để nhận 1 điểm (VND):</span>
                                        <span class="setting-code">POINT_EARN_RATE_VND</span>
                                    </div>
                                    <input type="number" name="setting_POINT_EARN_RATE_VND" class="form-input" 
                                           value="${settingsMap['POINT_EARN_RATE_VND'] != null ? settingsMap['POINT_EARN_RATE_VND'] : '10000'}" 
                                           min="1000" step="1000" required>
                                    <div class="setting-desc">Mặc định tiêu 10,000 VND sẽ tích được 1 điểm thưởng.</div>
                                </div>

                                <div class="setting-item">
                                    <div class="setting-label">
                                        <span>Giá trị quy đổi của 1 điểm khi thanh toán (VND):</span>
                                        <span class="setting-code">POINT_REDEEM_VALUE_VND</span>
                                    </div>
                                    <input type="number" name="setting_POINT_REDEEM_VALUE_VND" class="form-input" 
                                           value="${settingsMap['POINT_REDEEM_VALUE_VND'] != null ? settingsMap['POINT_REDEEM_VALUE_VND'] : '1000'}" 
                                           min="100" step="100" required>
                                    <div class="setting-desc">Mặc định 1 điểm thưởng được giảm 1,000 VND khi trừ tiền vé.</div>
                                </div>
                            </div>
                        </div>

                    </div>
                </form>

                <!-- Bảng danh sách chi tiết các Setting trong CSDL -->
                <div class="admin-card">
                    <div class="admin-card-header">
                        <h3 class="admin-card-title">
                            <i class="fa-solid fa-database"></i>
                            Chi Tiết Bản Ghi Trong CSDL (system_settings)
                        </h3>
                    </div>
                    <div class="table-responsive">
                        <table class="admin-table">
                            <thead>
                                <tr>
                                    <th style="width: 280px;">Khóa Tham Số (Key)</th>
                                    <th style="width: 180px;">Giá Trị Hiện Tại</th>
                                    <th style="width: 140px;">Nhóm (Group)</th>
                                    <th>Mô Tả Chức Năng</th>
                                    <th style="width: 170px;">Cập Nhật Lần Cuối</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${settings}" var="st">
                                    <tr>
                                        <td><span class="badge-code">${st.settingKey}</span></td>
                                        <td><strong style="color: #e50914; font-family: ui-monospace, monospace;">${st.settingValue}</strong></td>
                                        <td><span class="badge-city">${st.groupName}</span></td>
                                        <td style="font-size: 0.88rem; color: #475569;">${st.description}</td>
                                        <td style="font-size: 0.82rem; color: #64748b; font-family: ui-monospace, monospace;">
                                            ${st.updatedAt}
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>

            </div>
        </div>
    </div>

    <!-- MODAL THÊM THAM SỐ MỚI -->
    <div id="newSettingModal" class="modal-overlay">
        <div class="modal-content">
            <div class="modal-header">
                <h3>Thêm Tham Số Cấu Hình Mới</h3>
                <button type="button" class="close-modal" onclick="closeNewSettingModal()">&times;</button>
            </div>
            <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/settings">
                <input type="hidden" name="action" value="saveSingle">

                <div class="form-group">
                    <label>Khóa Tham Số (Setting Key) (*):</label>
                    <input type="text" name="settingKey" class="form-input" required placeholder="VD: MAX_TICKETS_PER_BOOKING" pattern="[A-Z0-9_]{3,50}" style="text-transform: uppercase;">
                    <small style="color: #718096;">Viết hoa, nối bằng dấu gạch dưới (VD: MAX_SEATS_PER_ORDER)</small>
                </div>

                <div class="form-group">
                    <label>Giá Trị Tham Số (*):</label>
                    <input type="text" name="settingValue" class="form-input" required placeholder="VD: 8, 300, https://...">
                </div>

                <div class="form-group">
                    <label>Nhóm Cấu Hình (Group) (*):</label>
                    <select name="groupName" class="form-input" required>
                        <option value="BOOKING">BOOKING (Đặt vé & Giữ ghế)</option>
                        <option value="SHOWTIME">SHOWTIME (Lịch chiếu & Phòng)</option>
                        <option value="SECURITY">SECURITY (Bảo mật & OTP)</option>
                        <option value="PAYMENT">PAYMENT (Cổng thanh toán)</option>
                        <option value="LOYALTY">LOYALTY (Tích điểm & Hội viên)</option>
                        <option value="SYSTEM">SYSTEM (Hệ thống chung)</option>
                    </select>
                </div>

                <div class="form-group">
                    <label>Mô Tả Ý Nghĩa:</label>
                    <textarea name="description" class="form-input" rows="2" placeholder="Giải thích mục đích của tham số cấu hình này..."></textarea>
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 10px; margin-top: 20px;">
                    <button type="button" class="btn-secondary" onclick="closeNewSettingModal()">Hủy bỏ</button>
                    <button type="submit" class="btn-primary">💾 Thêm Tham Số</button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function openNewSettingModal() {
            document.getElementById('newSettingModal').classList.add('active');
        }
        function closeNewSettingModal() {
            document.getElementById('newSettingModal').classList.remove('active');
        }
        window.onclick = function(event) {
            if (event.target.classList.contains('modal-overlay')) {
                event.target.classList.remove('active');
            }
        }
    </script>
</body>
</html>
