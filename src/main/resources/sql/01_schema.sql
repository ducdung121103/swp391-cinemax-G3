-- ============================================================================
-- MULTI-BRANCH CINEMA MANAGEMENT SYSTEM (SWP391 - GROUP 3)
-- SCRIPT 01: COMPREHENSIVE DATABASE SCHEMA DEFINITION (31 TABLES - 3NF INNODB)
-- Conforms 100% with: Feature-Tree-Project.docx (Modules M-01 -> M-12)
-- Engine: MySQL 8.0+ | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `cinema_chain_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cinema_chain_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- ZONE 1: INFRASTRUCTURE & CINEMA MANAGEMENT (TV 1 - 7 TABLES)
-- ----------------------------------------------------------------------------

-- 1. Bảng cinemas (Cụm rạp chiếu phim - M-04.1)
DROP TABLE IF EXISTS `cinemas`;
CREATE TABLE `cinemas` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `cinema_code` VARCHAR(20) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `email` VARCHAR(100) NULL,
    `total_rooms` INT NOT NULL DEFAULT 0,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_cinemas_city` (`city`),
    INDEX `idx_cinemas_active` (`is_active`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng screening_rooms (Phòng chiếu phim - M-04.2)
DROP TABLE IF EXISTS `screening_rooms`;
CREATE TABLE `screening_rooms` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `cinema_id` INT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `room_type` VARCHAR(30) NOT NULL DEFAULT 'STANDARD_2D', -- STANDARD_2D, VIP_2D, IMAX_3D, 4DX
    `total_rows` INT NOT NULL DEFAULT 10,
    `total_columns` INT NOT NULL DEFAULT 12,
    `total_capacity` INT NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, MAINTENANCE, INACTIVE
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_rooms_cinema` FOREIGN KEY (`cinema_id`) REFERENCES `cinemas` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_rooms_cinema` (`cinema_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng seat_types (Phân loại ghế ngồi & Phụ thu - M-04.3)
DROP TABLE IF EXISTS `seat_types`;
CREATE TABLE `seat_types` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `type_code` VARCHAR(30) NOT NULL UNIQUE, -- STANDARD, VIP, COUPLE
    `name` VARCHAR(50) NOT NULL,
    `color_hex` VARCHAR(10) NOT NULL DEFAULT '#6c757d',
    `surcharge` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng seats (Ghế ngồi trong phòng chiếu - Grid Designer - M-04.3)
DROP TABLE IF EXISTS `seats`;
CREATE TABLE `seats` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `screening_room_id` INT NOT NULL,
    `seat_type_id` INT NOT NULL,
    `seat_row` VARCHAR(10) NOT NULL,        -- A, B, C... (Hàng ghế)
    `seat_number` INT NOT NULL,             -- 1, 2, 3... (Số ghế)
    `seat_code` VARCHAR(10) NOT NULL,       -- A01, A02, B01... (Mã ghế hiển thị)
    `grid_row_index` INT NOT NULL DEFAULT 1,-- Tọa độ dòng trên lưới Matrix UI (Grid Designer)
    `grid_col_index` INT NOT NULL DEFAULT 1,-- Tọa độ cột trên lưới Matrix UI (Grid Designer)
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, MAINTENANCE, BLOCKED
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_seats_room` FOREIGN KEY (`screening_room_id`) REFERENCES `screening_rooms` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_seats_type` FOREIGN KEY (`seat_type_id`) REFERENCES `seat_types` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY `uk_room_seat_code` (`screening_room_id`, `seat_code`),
    INDEX `idx_seats_room` (`screening_room_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng maintenance_schedules (Lịch bảo trì phòng chiếu - M-04.3)
DROP TABLE IF EXISTS `maintenance_schedules`;
CREATE TABLE `maintenance_schedules` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `screening_room_id` INT NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `created_by` INT NULL,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_maint_room` FOREIGN KEY (`screening_room_id`) REFERENCES `screening_rooms` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_maint_time` (`screening_room_id`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng system_settings (Cấu hình tham số toàn cục hệ thống - M-03.1, M-05.3, M-09.2)
DROP TABLE IF EXISTS `system_settings`;
CREATE TABLE `system_settings` (
    `setting_key` VARCHAR(50) PRIMARY KEY,
    `setting_value` VARCHAR(255) NOT NULL,
    `description` VARCHAR(255) NULL,
    `group_name` VARCHAR(50) NOT NULL DEFAULT 'SYSTEM', -- SYSTEM, BOOKING, LOYALTY, PAYMENT
    `updated_by` INT NULL,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- ZONE 2: IDENTITY, AUTH, LOYALTY & SUPPORT (TV 2 - 8 TABLES)
-- ----------------------------------------------------------------------------

-- 8. Bảng roles (Vai trò phân quyền hệ thống - M-01.2)
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL UNIQUE, -- ADMIN, MANAGER, STAFF, CUSTOMER
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng membership_tiers (Hạng thành viên & Chính sách tích điểm - M-09.2)
DROP TABLE IF EXISTS `membership_tiers`;
CREATE TABLE `membership_tiers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tier_name` VARCHAR(50) NOT NULL UNIQUE, -- STANDARD, SILVER, GOLD, DIAMOND
    `min_spent` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `discount_percent` INT NOT NULL DEFAULT 0,
    `point_rate` DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Bảng users (Tài khoản người dùng toàn hệ thống - M-01.1 -> M-01.4)
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `role_id` INT NOT NULL,
    `cinema_id` INT NULL,
    `email` VARCHAR(120) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(120) NOT NULL,
    `phone` VARCHAR(20) NULL UNIQUE,
    `loyalty_points` INT NOT NULL DEFAULT 0,
    `tier_id` INT NULL,
    `avatar_url` VARCHAR(255) NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, LOCKED, INACTIVE
    `is_2fa_enabled` TINYINT(1) NOT NULL DEFAULT 0, -- M-01.2: 2FA qua Email OTP
    `email_verified` TINYINT(1) NOT NULL DEFAULT 0, -- M-01.2: Xác thực email
    `otp_code` VARCHAR(10) NULL,                    -- Mã OTP xác thực
    `otp_expires_at` DATETIME NULL,                 -- Thời hạn OTP (~5-15p)
    `reset_password_token` VARCHAR(100) NULL,       -- Token link reset mật khẩu
    `reset_token_expires_at` DATETIME NULL,         -- Thời hạn token 15 phút
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_users_cinema` FOREIGN KEY (`cinema_id`) REFERENCES `cinemas` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_users_tier` FOREIGN KEY (`tier_id`) REFERENCES `membership_tiers` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_role` (`role_id`),
    INDEX `idx_users_reset_token` (`reset_password_token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Bảng point_histories (Lịch sử tích & tiêu điểm thưởng - M-09.2)
DROP TABLE IF EXISTS `point_histories`;
CREATE TABLE `point_histories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `booking_id` BIGINT NULL,
    `points_change` INT NOT NULL, -- Dương: Tích điểm (+), Âm: Tiêu điểm (-)
    `balance_after` INT NOT NULL,
    `transaction_type` VARCHAR(30) NOT NULL DEFAULT 'EARN', -- EARN, REDEEM, ADJUST, EXPIRE
    `reason` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_points_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_points_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. Bảng customer_vouchers (Ví voucher cá nhân của khách hàng - M-09.1)
DROP TABLE IF EXISTS `customer_vouchers`;
CREATE TABLE `customer_vouchers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `voucher_id` INT NOT NULL,
    `is_used` TINYINT(1) NOT NULL DEFAULT 0,
    `used_at` DATETIME NULL,
    `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_cust_vouch_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_cust_vouch_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Bảng favorite_movies (Danh sách phim yêu thích của khách hàng - M-01.3)
DROP TABLE IF EXISTS `favorite_movies`;
CREATE TABLE `favorite_movies` (
    `user_id` INT NOT NULL,
    `movie_id` INT NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`user_id`, `movie_id`),
    CONSTRAINT `fk_fav_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_fav_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Bảng support_tickets (Quản lý khiếu nại & Hỗ trợ khách hàng - M-10)
DROP TABLE IF EXISTS `support_tickets`;
CREATE TABLE `support_tickets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ticket_code` VARCHAR(32) NOT NULL UNIQUE,
    `user_id` INT NOT NULL,
    `staff_id` INT NULL,
    `category` VARCHAR(50) NOT NULL DEFAULT 'BOOKING', -- BOOKING, PAYMENT, FNB, TECHNICAL, OTHER
    `subject` VARCHAR(200) NOT NULL,
    `content` TEXT NOT NULL,
    `response` TEXT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    `priority` VARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_support_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_support_staff` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_support_code` (`ticket_code`),
    INDEX `idx_support_user` (`user_id`),
    INDEX `idx_support_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Bảng notifications (Hệ thống thông báo In-app & Lịch sử gửi tin - M-11)
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `title` VARCHAR(200) NOT NULL,
    `message` TEXT NOT NULL,
    `type` VARCHAR(30) NOT NULL DEFAULT 'SYSTEM', -- BOOKING, PAYMENT, SHOWTIME, PROMOTION, SYSTEM
    `reference_id` VARCHAR(100) NULL, -- booking_code, movie_id, voucher_code...
    `is_read` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_notif_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_notif_user_read` (`user_id`, `is_read`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- ZONE 3: MOVIE CATALOG, SCHEDULING & DYNAMIC PRICING (TV 3 - 6 TABLES)
-- ----------------------------------------------------------------------------

-- 16. Bảng genres (Danh mục thể loại phim - M-02.1)
DROP TABLE IF EXISTS `genres`;
CREATE TABLE `genres` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Bảng movies (Kho phim điện ảnh - M-02.1)
DROP TABLE IF EXISTS `movies`;
CREATE TABLE `movies` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `original_title` VARCHAR(200) NULL,
    `duration_minutes` INT NOT NULL,
    `release_date` DATE NOT NULL,
    `end_date` DATE NULL,
    `age_rating` VARCHAR(10) NOT NULL DEFAULT 'P', -- P (Mọi lứa tuổi), T13, T16, T18, C (Cấm) - M-07.2
    `language` VARCHAR(100) NULL,
    `director` VARCHAR(150) NULL,
    `actors` TEXT NULL,
    `cast_members` TEXT NULL,
    `synopsis` TEXT NULL,
    `poster_url` VARCHAR(255) NULL,
    `trailer_url` VARCHAR(255) NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'COMING_SOON', -- COMING_SOON, NOW_SHOWING, ENDED
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_movies_status` (`status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cập nhật khóa ngoại favorite_movies -> movies
ALTER TABLE `favorite_movies`
    ADD CONSTRAINT `fk_fav_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 18. Bảng movie_genres (Quan hệ Nhiều - Nhiều giữa Phim và Thể loại - M-02.1)
DROP TABLE IF EXISTS `movie_genres`;
CREATE TABLE `movie_genres` (
    `movie_id` INT NOT NULL,
    `genre_id` INT NOT NULL,
    PRIMARY KEY (`movie_id`, `genre_id`),
    CONSTRAINT `fk_mg_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_mg_genre` FOREIGN KEY (`genre_id`) REFERENCES `genres` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 19. Bảng showtimes (Lịch chiếu phim - M-03.1: Chống trùng lịch + Đệm 15p dọn phòng)
DROP TABLE IF EXISTS `showtimes`;
CREATE TABLE `showtimes` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `movie_id` INT NOT NULL,
    `screening_room_id` INT NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL, -- Tự động = start_time + duration_minutes + 15 phút cleaning buffer
    `experience_format` VARCHAR(20) NOT NULL DEFAULT '2D', -- 2D, 3D, IMAX
    `status` VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, OPENING, FINISHED, CANCELLED
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_showtimes_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_showtimes_room` FOREIGN KEY (`screening_room_id`) REFERENCES `screening_rooms` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_showtimes_movie_time` (`movie_id`, `start_time`),
    INDEX `idx_showtimes_room_time` (`screening_room_id`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Bảng ticket_pricings (Ma trận cấu hình giá vé động - M-03.2)
DROP TABLE IF EXISTS `ticket_pricings`;
CREATE TABLE `ticket_pricings` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `cinema_id` INT NULL, -- NULL: áp dụng toàn hệ thống; NOT NULL: giá đặc thù rạp
    `day_type` VARCHAR(20) NOT NULL DEFAULT 'WEEKDAY', -- WEEKDAY, WEEKEND, HOLIDAY
    `time_slot` VARCHAR(20) NOT NULL DEFAULT 'STANDARD', -- EARLY (Sáng), STANDARD, PRIME (Tối), SNEAK_SHOW
    `experience_format` VARCHAR(20) NOT NULL DEFAULT '2D', -- 2D, 3D, IMAX
    `seat_type_id` INT NOT NULL DEFAULT 1,
    `base_price` DECIMAL(10,2) NOT NULL DEFAULT 80000.00,
    `effective_from` DATE NOT NULL,
    `effective_to` DATE NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_pricing_seat_type` FOREIGN KEY (`seat_type_id`) REFERENCES `seat_types` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_pricing_cinema` FOREIGN KEY (`cinema_id`) REFERENCES `cinemas` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_pricing_lookup` (`cinema_id`, `day_type`, `time_slot`, `experience_format`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 21. Bảng reviews (Đánh giá & Chấm điểm phim - M-02.3: Lọc từ ngữ thô tục & duyệt comment)
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `movie_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `rating_score` INT NOT NULL DEFAULT 5, -- 1 đến 5 sao
    `comment` TEXT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'APPROVED', -- APPROVED, PENDING, HIDDEN, FLAGGED
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_reviews_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_reviews_movie` (`movie_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- ZONE 4: CORE BOOKING, PAYMENTS & SEAT LOCKING ENGINE (TV 4 - 6 TABLES)
-- ----------------------------------------------------------------------------

-- 22. Bảng vouchers (Mã giảm giá & Chiến dịch khuyến mãi - M-09.1)
DROP TABLE IF EXISTS `vouchers`;
CREATE TABLE `vouchers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `code` VARCHAR(30) NOT NULL UNIQUE,
    `discount_type` VARCHAR(20) NOT NULL DEFAULT 'PERCENT', -- PERCENT, FIXED_AMOUNT
    `discount_val` DECIMAL(10,2) NOT NULL,
    `min_order_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `max_discount` DECIMAL(10,2) NULL,
    `usage_limit` INT NOT NULL DEFAULT 100,
    `used_count` INT NOT NULL DEFAULT 0,
    `valid_from` DATETIME NOT NULL,
    `valid_to` DATETIME NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_vouchers_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cập nhật khóa ngoại customer_vouchers -> vouchers
ALTER TABLE `customer_vouchers`
    ADD CONSTRAINT `fk_cust_vouch_vouch` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE CASCADE ON UPDATE CASCADE;

-- 23. Bảng seat_holdings (Khóa ghế tạm 5 phút thời gian thực - M-05.3: Pessimistic Lock)
DROP TABLE IF EXISTS `seat_holdings`;
CREATE TABLE `seat_holdings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `showtime_id` INT NOT NULL,
    `seat_id` INT NOT NULL,
    `session_id` VARCHAR(100) NOT NULL,
    `held_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expires_at` DATETIME NOT NULL, -- Mặc định held_at + 300 giây (từ system_settings)
    CONSTRAINT `fk_holding_showtime` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_holding_seat` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY `uk_showtime_seat_holding` (`showtime_id`, `seat_id`),
    INDEX `idx_holding_expiry` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 24. Bảng bookings (Đơn đặt vé & bắp nước - M-05.1 & M-05.4)
DROP TABLE IF EXISTS `bookings`;
CREATE TABLE `bookings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_code` VARCHAR(32) NOT NULL UNIQUE,
    `user_id` INT NULL,
    `staff_id` INT NULL, -- Ghi nhận nhân viên bán nếu mua tại quầy POS (M-05.2)
    `showtime_id` INT NOT NULL,
    `voucher_id` INT NULL,
    `channel` VARCHAR(20) NOT NULL DEFAULT 'ONLINE', -- ONLINE, POS
    `total_tickets_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `total_fnb_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `discount_amount` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `final_amount` DECIMAL(12,2) NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED, EXPIRED
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_bookings_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_bookings_staff` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_bookings_showtime` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_bookings_voucher` FOREIGN KEY (`voucher_id`) REFERENCES `vouchers` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_bookings_code` (`booking_code`),
    INDEX `idx_bookings_user` (`user_id`),
    INDEX `idx_bookings_showtime` (`showtime_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cập nhật khóa ngoại point_histories -> bookings
ALTER TABLE `point_histories`
    ADD CONSTRAINT `fk_points_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- 25. Bảng tickets (Vé điện tử & Vé giấy nhiệt - M-07.1: Duy nhất sinh vé & Ký số QR HMAC-SHA256)
DROP TABLE IF EXISTS `tickets`;
CREATE TABLE `tickets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL,
    `showtime_id` INT NOT NULL,
    `seat_id` INT NOT NULL,
    `barcode` VARCHAR(64) NOT NULL UNIQUE,
    `qr_signature` VARCHAR(255) NULL, -- M-07.1: Chuỗi token chữ ký số HMAC-SHA256
    `ticket_price` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'VALID', -- VALID, USED, REFUNDED, CANCELLED
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_tickets_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_tickets_showtime` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_tickets_seat` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY `uk_showtime_seat` (`showtime_id`, `seat_id`), -- CHỐT CHẶN CHỐNG BÁN TRÙNG GHẾ TUYỆT ĐỐI
    INDEX `idx_tickets_barcode` (`barcode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 26. Bảng order_items (Chi tiết bắp nước & Combo kèm đơn - M-08.2)
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL,
    `fnb_item_id` INT NOT NULL,
    `item_name` VARCHAR(150) NOT NULL,
    `quantity` INT NOT NULL DEFAULT 1,
    `unit_price` DECIMAL(10,2) NOT NULL,
    `subtotal` DECIMAL(12,2) NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_orderitems_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_orderitems_booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 27. Bảng payments (Lịch sử thanh toán & Giao dịch - M-06.1 -> M-06.3: VNPay, Tiền mặt)
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL,
    `payment_method` VARCHAR(30) NOT NULL DEFAULT 'VNPAY', -- VNPAY, CASH, POINTS
    `amount` DECIMAL(12,2) NOT NULL,
    `transaction_no` VARCHAR(100) NULL, -- Mã giao dịch VNPay
    `payment_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, FAILED, REFUNDED
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payments_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_payments_booking` (`booking_id`),
    INDEX `idx_payments_txn` (`transaction_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- ZONE 5: OPERATIONS, POS COUNTER, F&B & GATE CHECK-IN (TV 5 - 5 TABLES)
-- ----------------------------------------------------------------------------

-- 28. Bảng fnb_categories (Danh mục phân loại Bắp & Nước - M-08.1)
DROP TABLE IF EXISTS `fnb_categories`;
CREATE TABLE `fnb_categories` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 29. Bảng fnb_items (Danh mục sản phẩm & Combo Bắp Nước - M-08.1)
DROP TABLE IF EXISTS `fnb_items`;
CREATE TABLE `fnb_items` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `category_id` INT NOT NULL,
    `item_code` VARCHAR(30) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `price` DECIMAL(10,2) NOT NULL,
    `image_url` VARCHAR(255) NULL,
    `is_combo` TINYINT(1) NOT NULL DEFAULT 0,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_fnb_category` FOREIGN KEY (`category_id`) REFERENCES `fnb_categories` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_fnb_code` (`item_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Cập nhật khóa ngoại order_items -> fnb_items
ALTER TABLE `order_items`
    ADD CONSTRAINT `fk_orderitems_fnb` FOREIGN KEY (`fnb_item_id`) REFERENCES `fnb_items` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE;

-- 30. Bảng cinema_inventories (Quản lý tồn kho F&B theo từng rạp - M-08.1)
DROP TABLE IF EXISTS `cinema_inventories`;
CREATE TABLE `cinema_inventories` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `cinema_id` INT NOT NULL,
    `fnb_item_id` INT NOT NULL,
    `stock_quantity` INT NOT NULL DEFAULT 0,
    `warning_threshold` INT NOT NULL DEFAULT 10,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inv_cinema` FOREIGN KEY (`cinema_id`) REFERENCES `cinemas` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_inv_fnb` FOREIGN KEY (`fnb_item_id`) REFERENCES `fnb_items` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY `uk_cinema_fnb` (`cinema_id`, `fnb_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 31. Bảng ticket_checkin_logs (Nhật ký soát vé QR Gate Check-in - M-07.2: Chống vào lại & Kiểm tra tuổi)
DROP TABLE IF EXISTS `ticket_checkin_logs`;
CREATE TABLE `ticket_checkin_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ticket_id` BIGINT NOT NULL,
    `staff_id` INT NULL,
    `checkin_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, REJECTED_ALREADY_USED, WRONG_SHOWTIME, REJECTED_UNDERAGE
    `note` VARCHAR(255) NULL,
    CONSTRAINT `fk_checkin_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_checkin_staff` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_checkin_ticket` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 32. Bảng cash_drawers (Quản lý ca làm việc & Két tiền mặt POS Quầy - M-05.2 & M-06.2)
DROP TABLE IF EXISTS `cash_drawers`;
CREATE TABLE `cash_drawers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `cinema_id` INT NOT NULL,
    `staff_id` INT NOT NULL,
    `opening_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `closing_time` DATETIME NULL,
    `starting_cash` DECIMAL(12,2) NOT NULL DEFAULT 1000000.00,
    `total_cash_sales` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `ending_cash` DECIMAL(12,2) NULL,
    `difference_amount` DECIMAL(12,2) NULL DEFAULT 0.00,
    `status` VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, CLOSED
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_drawer_cinema` FOREIGN KEY (`cinema_id`) REFERENCES `cinemas` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_drawer_staff` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_drawer_cinema_status` (`cinema_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- END OF SCRIPT 01: 32 TABLES CREATED SUCCESSFULLY WITH ZERO INTEGRITY DEFECT
-- ============================================================================
