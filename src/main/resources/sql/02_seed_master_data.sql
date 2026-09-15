-- ============================================================================
-- MULTI-BRANCH CINEMA MANAGEMENT SYSTEM (SWP391)
-- SCRIPT 02: MASTER REFERENCE SEED DATA (PHASE 1)
-- Engine: MySQL 8.0+ | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ============================================================================

USE `cinema_chain_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. SEED ROLES (Vai trò phân quyền hệ thống)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `roles`;
INSERT INTO `roles` (`id`, `role_name`, `description`, `is_active`, `is_deleted`, `created_at`) VALUES
(1, 'ADMIN', 'Quản trị viên tối cao hệ thống - Toàn quyền cấu hình', 1, 0, NOW()),
(2, 'MANAGER', 'Quản lý cụm rạp chi nhánh - Quản lý phòng chiếu, lịch chiếu, nhân sự rạp', 1, 0, NOW()),
(3, 'STAFF', 'Nhân viên bán vé tại quầy (POS) và nhân viên soát vé (Scanner)', 1, 0, NOW()),
(4, 'CUSTOMER', 'Khách hàng thành viên xem phim trực tuyến', 1, 0, NOW());

-- ----------------------------------------------------------------------------
-- 2. SEED MEMBERSHIP TIERS (Hạng thành viên & chính sách tích điểm)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `membership_tiers`;
INSERT INTO `membership_tiers` (`id`, `tier_name`, `min_spent`, `discount_percent`, `point_rate`, `is_active`, `is_deleted`, `created_at`) VALUES
(1, 'STANDARD', 0.00, 0, 1.00, 1, 0, NOW()),
(2, 'VIP_SILVER', 2000000.00, 5, 1.20, 1, 0, NOW()),
(3, 'VIP_GOLD', 5000000.00, 10, 1.50, 1, 0, NOW()),
(4, 'DIAMOND', 10000000.00, 15, 2.00, 1, 0, NOW());

-- ----------------------------------------------------------------------------
-- 3. SEED SEAT TYPES (Phân loại ghế & phụ thu)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `seat_types`;
INSERT INTO `seat_types` (`id`, `type_code`, `name`, `color_hex`, `surcharge`, `description`, `is_active`, `is_deleted`, `created_at`) VALUES
(1, 'STANDARD', 'Ghế Tiêu Chuẩn', '#6c757d', 0.00, 'Ghế đơn thông thường, góc nhìn tiêu chuẩn', 1, 0, NOW()),
(2, 'VIP', 'Ghế VIP', '#e63946', 15000.00, 'Ghế trung tâm phòng chiếu, tầm nhìn và âm thanh tối ưu nhất', 1, 0, NOW()),
(3, 'COUPLE', 'Ghế Đôi Sweetbox', '#d63384', 40000.00, 'Ghế đôi vách ngăn riêng tư ở hàng cuối cùng', 1, 0, NOW());

-- ----------------------------------------------------------------------------
-- 4. SEED GENRES (Danh mục thể loại phim điện ảnh)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `genres`;
INSERT INTO `genres` (`id`, `name`, `description`, `is_active`, `is_deleted`, `created_at`) VALUES
(1, 'Hành Động', 'Phim có nhiều cảnh chiến đấu, rượt đuổi, kỹ xảo kịch tính', 1, 0, NOW()),
(2, 'Phiêu Lưu', 'Những chuyến du hành kỳ thú, khám phá vùng đất mới', 1, 0, NOW()),
(3, 'Hoạt Hình', 'Phim hoạt hình 2D/3D cho mọi lứa tuổi gia đình', 1, 0, NOW()),
(4, 'Hài Hước', 'Phim mang lại tiếng cười sảng khoái, giải trí nhẹ nhàng', 1, 0, NOW()),
(5, 'Kinh Dị', 'Phim giật gân, rùng rợn, khám phá bí ẩn huyền bí', 1, 0, NOW()),
(6, 'Khoa Học Viễn Tưởng', 'Khám phá tương lai, vũ trụ, công nghệ vượt thời đại', 1, 0, NOW()),
(7, 'Lãng Mạn', 'Tình yêu đôi lứa, cảm xúc sâu lắng, câu chuyện ngọt ngào', 1, 0, NOW()),
(8, 'Giật Gân', 'Những tình tiết hồi hộp, xoắn não, bất ngờ đến phút chót', 1, 0, NOW()),
(9, 'Tâm Lý', 'Khai thác chiều sâu tâm lý con người và bi kịch xã hội', 1, 0, NOW()),
(10, 'Gia Đình', 'Gắn kết tình cảm gia đình, giáo dục và bài học nhân văn', 1, 0, NOW());

-- ----------------------------------------------------------------------------
-- 5. SEED F&B CATEGORIES (Danh mục bắp nước quầy bar)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `fnb_categories`;
INSERT INTO `fnb_categories` (`id`, `name`, `description`, `is_active`, `is_deleted`, `created_at`) VALUES
(1, 'Bắp Rang Bơ', 'Bắp giòn rụm thơm ngon rang mới mỗi ngày với nhiều vị hấp dẫn', 1, 0, NOW()),
(2, 'Nước Giải Khát', 'Các loại nước ngọt có ga mát lạnh sảng khoái và nước trái cây', 1, 0, NOW()),
(3, 'Combo Siêu Tiết Kiệm', 'Gói combo bắp và nước phối hợp hoàn hảo với giá ưu đãi', 1, 0, NOW());

-- ----------------------------------------------------------------------------
-- 6. SEED ROOT ADMIN & PRE-CONFIGURED USERS (Password default: '123456')
-- BCrypt Hash: $2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `users`;
INSERT INTO `users` (`id`, `role_id`, `branch_id`, `email`, `password_hash`, `full_name`, `phone`, `loyalty_points`, `tier_id`, `avatar_url`, `status`, `is_deleted`, `created_at`) VALUES
(1, 1, NULL, 'admin@cinema.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Hệ Thống Quản Trị Viên', '0900000001', 0, NULL, '/assets/images/avatars/admin.png', 'ACTIVE', 0, NOW()),
(2, 2, NULL, 'manager.hn@cinema.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Trần Quản Lý Hà Nội', '0900000002', 0, NULL, '/assets/images/avatars/manager.png', 'ACTIVE', 0, NOW()),
(3, 3, NULL, 'staff.hn@cinema.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Lê Thu Ngân Viên Hà Nội', '0900000003', 0, NULL, '/assets/images/avatars/staff.png', 'ACTIVE', 0, NOW()),
(4, 3, NULL, 'staff.sg@cinema.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Phạm Thu Ngân Sài Gòn', '0900000004', 0, NULL, '/assets/images/avatars/staff.png', 'ACTIVE', 0, NOW()),
(5, 4, NULL, 'customer@gmail.com', '$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Nguyễn Văn Khách Hàng', '0987654321', 250, 1, '/assets/images/avatars/customer.png', 'ACTIVE', 0, NOW());

SET FOREIGN_KEY_CHECKS = 1;
