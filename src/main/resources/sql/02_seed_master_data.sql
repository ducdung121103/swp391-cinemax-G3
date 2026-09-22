-- ============================================================================
-- MULTI-BRANCH CINEMA MANAGEMENT SYSTEM (SWP391)
-- SCRIPT 02: MASTER REFERENCE SEED DATA (PHASE 1 - T-SQL)
-- Engine: Microsoft SQL Server 2019+ | Collation: Vietnamese_CI_AS
-- ============================================================================

USE cinema_chain_db;
GO

-- ----------------------------------------------------------------------------
-- 1. SEED ROLES (Vai trò phân quyền hệ thống)
-- ----------------------------------------------------------------------------
DELETE FROM roles;
GO
SET IDENTITY_INSERT roles ON;
INSERT INTO roles (id, role_name, description, is_active, is_deleted, created_at) VALUES
(1, N'ADMIN', N'Quản trị viên tối cao hệ thống - Toàn quyền cấu hình', 1, 0, GETDATE()),
(2, N'MANAGER', N'Quản lý cụm rạp chi nhánh - Quản lý phòng chiếu, lịch chiếu, nhân sự rạp', 1, 0, GETDATE()),
(3, N'STAFF', N'Nhân viên bán vé tại quầy (POS) và nhân viên soát vé (Scanner)', 1, 0, GETDATE()),
(4, N'CUSTOMER', N'Khách hàng thành viên xem phim trực tuyến', 1, 0, GETDATE());
SET IDENTITY_INSERT roles OFF;
GO

-- ----------------------------------------------------------------------------
-- 2. SEED MEMBERSHIP TIERS (Hạng thành viên & chính sách tích điểm)
-- ----------------------------------------------------------------------------
DELETE FROM membership_tiers;
GO
SET IDENTITY_INSERT membership_tiers ON;
INSERT INTO membership_tiers (id, tier_name, min_spent, discount_percent, point_rate, is_active, is_deleted, created_at) VALUES
(1, N'STANDARD', 0.00, 0, 1.00, 1, 0, GETDATE()),
(2, N'VIP_SILVER', 2000000.00, 5, 1.20, 1, 0, GETDATE()),
(3, N'VIP_GOLD', 5000000.00, 10, 1.50, 1, 0, GETDATE()),
(4, N'DIAMOND', 10000000.00, 15, 2.00, 1, 0, GETDATE());
SET IDENTITY_INSERT membership_tiers OFF;
GO

-- ----------------------------------------------------------------------------
-- 3. SEED SEAT TYPES (Phân loại ghế & phụ thu)
-- ----------------------------------------------------------------------------
DELETE FROM seat_types;
GO
SET IDENTITY_INSERT seat_types ON;
INSERT INTO seat_types (id, type_code, name, color_hex, surcharge, description, is_active, is_deleted, created_at) VALUES
(1, N'STANDARD', N'Ghế Tiêu Chuẩn', N'#6c757d', 0.00, N'Ghế đơn thông thường, góc nhìn tiêu chuẩn', 1, 0, GETDATE()),
(2, N'VIP', N'Ghế VIP', N'#e63946', 15000.00, N'Ghế trung tâm phòng chiếu, tầm nhìn và âm thanh tối ưu nhất', 1, 0, GETDATE()),
(3, N'COUPLE', N'Ghế Đôi Sweetbox', N'#d63384', 40000.00, N'Ghế đôi vách ngăn riêng tư ở hàng cuối cùng', 1, 0, GETDATE());
SET IDENTITY_INSERT seat_types OFF;
GO

-- ----------------------------------------------------------------------------
-- 4. SEED GENRES (Danh mục thể loại phim điện ảnh)
-- ----------------------------------------------------------------------------
DELETE FROM genres;
GO
SET IDENTITY_INSERT genres ON;
INSERT INTO genres (id, name, description, is_active, is_deleted, created_at) VALUES
(1, N'Hành Động', N'Phim có nhiều cảnh chiến đấu, rượt đuổi, kỹ xảo kịch tính', 1, 0, GETDATE()),
(2, N'Phiêu Lưu', N'Những chuyến du hành kỳ thú, khám phá vùng đất mới', 1, 0, GETDATE()),
(3, N'Hoạt Hình', N'Phim hoạt hình 2D/3D cho mọi lứa tuổi gia đình', 1, 0, GETDATE()),
(4, N'Hài Hước', N'Phim mang lại tiếng cười sảng khoái, giải trí nhẹ nhàng', 1, 0, GETDATE()),
(5, N'Kinh Dị', N'Phim giật gân, rùng rợn, khám phá bí ẩn huyền bí', 1, 0, GETDATE()),
(6, N'Khoa Học Viễn Tưởng', N'Khám phá tương lai, vũ trụ, công nghệ vượt thời đại', 1, 0, GETDATE()),
(7, N'Lãng Mạn', N'Tình yêu đôi lứa, cảm xúc sâu lắng, câu chuyện ngọt ngào', 1, 0, GETDATE()),
(8, N'Giật Gân', N'Những tình tiết hồi hộp, xoắn não, bất ngờ đến phút chót', 1, 0, GETDATE()),
(9, N'Tâm Lý', N'Khai thác chiều sâu tâm lý con người và bi kịch xã hội', 1, 0, GETDATE()),
(10, N'Gia Đình', N'Gắn kết tình cảm gia đình, giáo dục và bài học nhân văn', 1, 0, GETDATE());
SET IDENTITY_INSERT genres OFF;
GO

-- ----------------------------------------------------------------------------
-- 5. SEED F&B CATEGORIES (Danh mục bắp nước quầy bar)
-- ----------------------------------------------------------------------------
DELETE FROM fnb_categories;
GO
SET IDENTITY_INSERT fnb_categories ON;
INSERT INTO fnb_categories (id, name, description, is_active, is_deleted, created_at) VALUES
(1, N'Bắp Rang Bơ', N'Bắp giòn rụm thơm ngon rang mới mỗi ngày với nhiều vị hấp dẫn', 1, 0, GETDATE()),
(2, N'Nước Giải Khát', N'Các loại nước ngọt có ga mát lạnh sảng khoái và nước trái cây', 1, 0, GETDATE()),
(3, N'Combo Siêu Tiết Kiệm', N'Gói combo bắp và nước phối hợp hoàn hảo với giá ưu đãi', 1, 0, GETDATE());
SET IDENTITY_INSERT fnb_categories OFF;
GO

-- ----------------------------------------------------------------------------
-- 6. SEED ROOT ADMIN & PRE-CONFIGURED USERS (Password default: '123456')
-- BCrypt Hash: $2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ----------------------------------------------------------------------------
DELETE FROM users;
GO
SET IDENTITY_INSERT users ON;
INSERT INTO users (id, role_id, cinema_id, email, password_hash, full_name, phone, loyalty_points, tier_id, avatar_url, status, is_2fa_enabled, email_verified, is_deleted, created_at) VALUES
(1, 1, NULL, N'admin@cinema.com', N'$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Hệ Thống Quản Trị Viên', N'0900000001', 0, NULL, N'/assets/images/avatars/admin.png', N'ACTIVE', 0, 1, 0, GETDATE()),
(2, 2, NULL, N'manager.hn@cinema.com', N'$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Trần Quản Lý Hà Nội', N'0900000002', 0, NULL, N'/assets/images/avatars/manager.png', N'ACTIVE', 0, 1, 0, GETDATE()),
(3, 3, NULL, N'staff.hn@cinema.com', N'$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Lê Thu Ngân Viên Hà Nội', N'0900000003', 0, NULL, N'/assets/images/avatars/staff.png', N'ACTIVE', 0, 1, 0, GETDATE()),
(4, 3, NULL, N'staff.sg@cinema.com', N'$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Phạm Thu Ngân Sài Gòn', N'0900000004', 0, NULL, N'/assets/images/avatars/staff.png', N'ACTIVE', 0, 1, 0, GETDATE()),
(5, 4, NULL, N'customer@gmail.com', N'$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', N'Nguyễn Văn Khách Hàng', N'0987654321', 250, 1, N'/assets/images/avatars/customer.png', N'ACTIVE', 0, 1, 0, GETDATE());
SET IDENTITY_INSERT users OFF;
GO

-- ----------------------------------------------------------------------------
-- 7. SEED SYSTEM SETTINGS (Cấu hình tham số toàn cục dùng chung cho M-03.1, M-05.3, M-09.2)
-- ----------------------------------------------------------------------------
DELETE FROM system_settings;
GO
INSERT INTO system_settings (setting_key, setting_value, description, group_name, updated_by, updated_at) VALUES
(N'SEAT_HOLD_DURATION_SECONDS', N'300', N'Thời gian giữ ghế tạm thời khi khách thanh toán (giây)', N'BOOKING', 1, GETDATE()),
(N'CLEANING_BUFFER_MINUTES', N'15', N'Thời gian giãn cách dọn dẹp phòng chiếu giữa 2 suất chiếu (phút)', N'SHOWTIME', 1, GETDATE()),
(N'POINT_EARN_RATE_VND', N'10000', N'Số tiền chi tiêu (VND) để quy đổi ra 1 điểm thưởng', N'LOYALTY', 1, GETDATE()),
(N'POINT_REDEEM_VALUE_VND', N'1000', N'Giá trị quy đổi của 1 điểm thưởng khi thanh toán (VND)', N'LOYALTY', 1, GETDATE()),
(N'PASSWORD_RESET_TOKEN_EXPIRY_MINUTES', N'15', N'Thời hạn hiệu lực của link đặt lại mật khẩu qua email (phút)', N'SECURITY', 1, GETDATE()),
(N'OTP_EXPIRY_SECONDS', N'300', N'Thời hạn mã xác thực OTP gửi qua email (giây)', N'SECURITY', 1, GETDATE()),
(N'VNPAY_TMN_CODE', N'DEMO_TMN', N'Mã định danh terminal VNPAY', N'PAYMENT', 1, GETDATE()),
(N'VNPAY_HASH_SECRET', N'DEMO_HASH_SECRET_KEY_CINEMA_2026', N'Mã băm bí mật checksum HMAC-SHA512 VNPAY', N'PAYMENT', 1, GETDATE()),
(N'QR_HMAC_SECRET', N'CINEMA_SECURE_QR_HMAC_SECRET_2026', N'Khóa bí mật tạo chữ ký số chống làm giả vé QR Code', N'SECURITY', 1, GETDATE());
GO
