<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cấu Hình Tham Số Vận Hành - Cinema Admin</title>
    
    <!-- Google Fonts & FontAwesome -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@400;500;600;700;800&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css">
    
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/base.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/admin-layout.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/modules/infrastructure.css">

    <style>
        .settings-container {
            max-width: 860px;
            margin: 0 auto;
        }

        .settings-card {
            background: #ffffff;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
            border: 1px solid #edf2f7;
            overflow: hidden;
            margin-bottom: 24px;
        }

        .settings-card-header {
            padding: 24px 28px;
            background: linear-gradient(135deg, #1a1a24 0%, #242435 100%);
            color: #ffffff;
            display: flex;
            align-items: center;
            gap: 16px;
        }

        .settings-card-header .header-icon {
            width: 48px;
            height: 48px;
            border-radius: 10px;
            background: rgba(229, 9, 20, 0.18);
            border: 1px solid rgba(229, 9, 20, 0.35);
            color: #ff4d4d;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4rem;
        }

        .settings-card-header h2 {
            margin: 0;
            font-size: 1.35rem;
            font-weight: 700;
            color: #ffffff;
        }

        .settings-card-header p {
            margin: 4px 0 0 0;
            font-size: 0.88rem;
            color: #a0aec0;
            line-height: 1.4;
        }

        .settings-card-body {
            padding: 32px 28px;
        }

        .form-field-group {
            margin-bottom: 28px;
            padding-bottom: 24px;
            border-bottom: 1px dashed #e2e8f0;
        }

        .form-field-group:last-of-type {
            border-bottom: none;
            margin-bottom: 16px;
            padding-bottom: 8px;
        }

        .field-label-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 8px;
        }

        .field-label {
            font-size: 0.96rem;
            font-weight: 700;
            color: #1a202c;
            display: flex;
            align-items: center;
            gap: 8px;
        }

        .field-key-badge {
            font-family: 'Consolas', monospace;
            font-size: 0.76rem;
            font-weight: 600;
            background: #f1f5f9;
            color: #475569;
            padding: 3px 8px;
            border-radius: 4px;
            border: 1px solid #e2e8f0;
        }

        .field-input-wrapper {
            position: relative;
            max-width: 360px;
        }

        .field-input-wrapper input {
            width: 100%;
            padding: 12px 60px 12px 16px;
            font-size: 1.05rem;
            font-weight: 700;
            color: #0f172a;
            border: 2px solid #cbd5e1;
            border-radius: 8px;
            background: #f8fafc;
            transition: all 0.2s ease;
            box-sizing: border-box;
        }

        .field-input-wrapper input:focus {
            border-color: #e50914;
            background: #ffffff;
            outline: none;
            box-shadow: 0 0 0 3px rgba(229, 9, 20, 0.12);
        }

        .field-unit-tag {
            position: absolute;
            right: 14px;
            top: 50%;
            transform: translateY(-50%);
            font-size: 0.85rem;
            font-weight: 600;
            color: #64748b;
            background: #e2e8f0;
            padding: 3px 8px;
            border-radius: 4px;
            pointer-events: none;
        }

        .field-hint {
            margin-top: 8px;
            font-size: 0.82rem;
            color: #64748b;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .field-hint i {
            color: #3b82f6;
        }

        .settings-card-footer {
            padding: 20px 28px;
            background: #f8fafc;
            border-top: 1px solid #edf2f7;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 12px;
        }

        .footer-note {
            font-size: 0.8rem;
            color: #64748b;
            display: flex;
            align-items: center;
            gap: 6px;
        }

        .btn-save-settings {
            background: #e50914;
            color: #ffffff;
            border: none;
            padding: 12px 28px;
            font-size: 0.95rem;
            font-weight: 700;
            border-radius: 8px;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 10px;
            transition: all 0.2s ease;
            box-shadow: 0 4px 12px rgba(229, 9, 20, 0.25);
        }

        .btn-save-settings:hover {
            background: #b20710;
            transform: translateY(-1px);
            box-shadow: 0 6px 16px rgba(229, 9, 20, 0.35);
        }

        .btn-save-settings:active {
            transform: translateY(0);
        }

        /* Toast Alert */
        .alert-box {
            padding: 14px 20px;
            border-radius: 8px;
            margin-bottom: 22px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 0.92rem;
            font-weight: 500;
        }
        .alert-success {
            background: #dcfce7;
            color: #166534;
            border-left: 5px solid #22c55e;
        }
        .alert-error {
            background: #fee2e2;
            color: #991b1b;
            border-left: 5px solid #ef4444;
        }
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <jsp:include page="../../common/sidebar.jsp" />
        
        <div class="admin-main">
            <jsp:include page="../../common/navbar.jsp" />
            
            <div class="admin-content">
                <div class="settings-container">

                    <!-- BREADCRUMB -->
                    <div style="margin-bottom: 18px; font-size: 0.86rem; color: #64748b;">
                        <a href="${pageContext.request.contextPath}/admin/dashboard" style="color: #64748b; text-decoration: none;">Dashboard</a>
                        <span style="margin: 0 6px;">/</span>
                        <span style="color: #0f172a; font-weight: 600;">Cấu Hình Tham Số Vận Hành</span>
                    </div>

                    <!-- THÔNG BÁO FLASH MESSAGE -->
                    <c:if test="${not empty msg}">
                        <div class="alert-box alert-success">
                            <span>
                                <i class="fa-solid fa-circle-check" style="margin-right: 8px;"></i>
                                <c:choose>
                                    <c:when test="${msg == 'success' || msg == 'all_saved'}">
                                        Đã cập nhật thành công 3 tham số quy tắc vận hành rạp chiếu phim toàn cục!
                                    </c:when>
                                    <c:otherwise>Thao tác thành công!</c:otherwise>
                                </c:choose>
                            </span>
                            <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem; color:#166534;">&times;</button>
                        </div>
                    </c:if>

                    <c:if test="${not empty error}">
                        <div class="alert-box alert-error">
                            <span>
                                <i class="fa-solid fa-triangle-exclamation" style="margin-right: 8px;"></i>
                                ${error}
                            </span>
                            <button onclick="this.parentElement.style.display='none'" style="background:none; border:none; cursor:pointer; font-size: 1.2rem; color:#991b1b;">&times;</button>
                        </div>
                    </c:if>

                    <!-- FORM CẤU HÌNH VẬN HÀNH DUY NHẤT -->
                    <form method="POST" action="${pageContext.request.contextPath}/admin/infrastructure/settings" id="operationalSettingsForm">
                        <input type="hidden" name="action" value="saveAll">

                        <div class="settings-card">
                            <div class="settings-card-header">
                                <div class="header-icon">
                                    <i class="fa-solid fa-sliders"></i>
                                </div>
                                <div>
                                    <h2>⚙️ Tham Số Quy Tắc Vận Hành Hệ Thống (M-11)</h2>
                                    <p>Các thông số toàn cục chi phối luồng giữ ghế, lịch chiếu và chính sách hủy vé của toàn bộ chuỗi rạp CineMax.</p>
                                </div>
                            </div>

                            <div class="settings-card-body">
                                
                                <!-- 1. THỜI GIAN GIỮ GHẾ TẠM THỜI -->
                                <div class="form-field-group">
                                    <div class="field-label-row">
                                        <label for="seatHoldInput" class="field-label">
                                            <i class="fa-solid fa-couch" style="color: #e50914;"></i>
                                            1. Thời Gian Giữ Ghế Tạm Thời
                                        </label>
                                        <span class="field-key-badge">SEAT_HOLD_DURATION_SECONDS</span>
                                    </div>
                                    <div class="field-input-wrapper">
                                        <input type="number" 
                                               id="seatHoldInput" 
                                               name="SEAT_HOLD_DURATION_SECONDS" 
                                               value="${not empty settingsMap['SEAT_HOLD_DURATION_SECONDS'] ? settingsMap['SEAT_HOLD_DURATION_SECONDS'] : '300'}" 
                                               min="60" 
                                               max="3600" 
                                               step="30" 
                                               required>
                                        <span class="field-unit-tag">Giây</span>
                                    </div>
                                    <div class="field-hint">
                                        <i class="fa-solid fa-circle-info"></i>
                                        <span>Khóa ghế tạm thời khi khách hàng đang tiến hành thanh toán. Mặc định <strong>300 giây = 5 phút</strong>.</span>
                                    </div>
                                </div>

                                <!-- 2. THỜI GIAN ĐỆM DỌN DẸP PHÒNG CHIẾU -->
                                <div class="form-field-group">
                                    <div class="field-label-row">
                                        <label for="cleaningBufferInput" class="field-label">
                                            <i class="fa-solid fa-broom" style="color: #3b82f6;"></i>
                                            2. Thời Gian Đệm Dọn Vệ Sinh Phòng Chiếu
                                        </label>
                                        <span class="field-key-badge">CLEANING_BUFFER_MINUTES</span>
                                    </div>
                                    <div class="field-input-wrapper">
                                        <input type="number" 
                                               id="cleaningBufferInput" 
                                               name="CLEANING_BUFFER_MINUTES" 
                                               value="${not empty settingsMap['CLEANING_BUFFER_MINUTES'] ? settingsMap['CLEANING_BUFFER_MINUTES'] : '15'}" 
                                               min="5" 
                                               max="120" 
                                               step="5" 
                                               required>
                                        <span class="field-unit-tag">Phút</span>
                                    </div>
                                    <div class="field-hint">
                                        <i class="fa-solid fa-circle-info"></i>
                                        <span>Khoảng thời gian tự động cộng thêm giữa 2 suất chiếu liên tiếp để dọn phòng chiếu. Mặc định <strong>15 phút</strong>.</span>
                                    </div>
                                </div>

                                <!-- 3. THỜI HẠN HỦY VÉ TRỰC TUYẾN -->
                                <div class="form-field-group">
                                    <div class="field-label-row">
                                        <label for="cancellationDeadlineInput" class="field-label">
                                            <i class="fa-solid fa-clock-rotate-left" style="color: #f59e0b;"></i>
                                            3. Thời Hạn Hủy Vé Trực Tuyến
                                        </label>
                                        <span class="field-key-badge">CANCELLATION_DEADLINE_HOURS</span>
                                    </div>
                                    <div class="field-input-wrapper">
                                        <input type="number" 
                                               id="cancellationDeadlineInput" 
                                               name="CANCELLATION_DEADLINE_HOURS" 
                                               value="${not empty settingsMap['CANCELLATION_DEADLINE_HOURS'] ? settingsMap['CANCELLATION_DEADLINE_HOURS'] : '2'}" 
                                               min="1" 
                                               max="72" 
                                               step="1" 
                                               required>
                                        <span class="field-unit-tag">Giờ</span>
                                    </div>
                                    <div class="field-hint">
                                        <i class="fa-solid fa-circle-info"></i>
                                        <span>Số giờ tối thiểu trước giờ chiếu mà khách hàng được phép hoàn/hủy vé trực tuyến. Mặc định <strong>2 giờ</strong>.</span>
                                    </div>
                                </div>

                            </div>

                            <div class="settings-card-footer">
                                <div class="footer-note">
                                    <i class="fa-solid fa-shield-halved"></i>
                                    <span>Áp dụng thời gian thực (Real-time) cho toàn bộ các cụm rạp trên hệ thống.</span>
                                </div>
                                <button type="submit" class="btn-save-settings">
                                    <i class="fa-solid fa-floppy-disk"></i>
                                    <span>Lưu Cấu Hình Vận Hành</span>
                                </button>
                            </div>
                        </div>
                    </form>

                </div>
            </div>
        </div>
    </div>
</body>
</html>
