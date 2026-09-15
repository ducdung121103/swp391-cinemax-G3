-- ============================================================================
-- MULTI-BRANCH CINEMA MANAGEMENT SYSTEM (SWP391)
-- SCRIPT 01: DATABASE SCHEMA DEFINITION (28 TABLES - 3NF INNODB)
-- Engine: MySQL 8.0+ | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ============================================================================

CREATE DATABASE IF NOT EXISTS `cinema_chain_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `cinema_chain_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- ZONE 1: INFRASTRUCTURE & CINEMA MANAGEMENT (TV 1 - 6 TABLES)
-- ----------------------------------------------------------------------------

-- 1. Bảng branches (Chi nhánh cụm rạp)
DROP TABLE IF EXISTS `branches`;
CREATE TABLE `branches` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `branch_code` VARCHAR(20) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `address` VARCHAR(255) NOT NULL,
    `city` VARCHAR(100) NOT NULL,
    `phone` VARCHAR(20) NOT NULL,
    `email` VARCHAR(100) NULL,
    `total_halls` INT NOT NULL DEFAULT 0,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_branches_city` (`city`),
    INDEX `idx_branches_active` (`is_active`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Bảng screening_halls (Phòng chiếu phim)
DROP TABLE IF EXISTS `screening_halls`;
CREATE TABLE `screening_halls` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `branch_id` INT NOT NULL,
    `name` VARCHAR(50) NOT NULL,
    `hall_type` VARCHAR(30) NOT NULL DEFAULT 'STANDARD_2D',
    `total_rows` INT NOT NULL DEFAULT 10,
    `total_columns` INT NOT NULL DEFAULT 12,
    `total_capacity` INT NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_halls_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_halls_branch` (`branch_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Bảng seat_types (Phân loại ghế ngồi)
DROP TABLE IF EXISTS `seat_types`;
CREATE TABLE `seat_types` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `type_code` VARCHAR(30) NOT NULL UNIQUE,
    `name` VARCHAR(50) NOT NULL,
    `color_hex` VARCHAR(10) NOT NULL DEFAULT '#6c757d',
    `surcharge` DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. Bảng seats (Ghế vật lý trong phòng)
DROP TABLE IF EXISTS `seats`;
CREATE TABLE `seats` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `screening_hall_id` INT NOT NULL,
    `seat_type_id` INT NOT NULL,
    `seat_row` VARCHAR(5) NOT NULL,
    `seat_number` INT NOT NULL,
    `seat_code` VARCHAR(10) NOT NULL,
    `grid_row_index` INT NOT NULL,
    `grid_col_index` INT NOT NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_seats_hall` FOREIGN KEY (`screening_hall_id`) REFERENCES `screening_halls` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_seats_type` FOREIGN KEY (`seat_type_id`) REFERENCES `seat_types` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY `uk_hall_seat_code` (`screening_hall_id`, `seat_code`),
    UNIQUE KEY `uk_hall_grid_pos` (`screening_hall_id`, `grid_row_index`, `grid_col_index`),
    INDEX `idx_seats_hall` (`screening_hall_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. Bảng system_logs (Nhật ký kiểm toán)
DROP TABLE IF EXISTS `system_logs`;
CREATE TABLE `system_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NULL,
    `action` VARCHAR(100) NOT NULL,
    `module` VARCHAR(50) NOT NULL,
    `ip_address` VARCHAR(50) NULL,
    `details` TEXT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_logs_module` (`module`),
    INDEX `idx_logs_action` (`action`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. Bảng maintenance_schedules (Lịch bảo trì phòng)
DROP TABLE IF EXISTS `maintenance_schedules`;
CREATE TABLE `maintenance_schedules` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `screening_hall_id` INT NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `reason` VARCHAR(255) NOT NULL,
    `created_by` INT NULL,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_maint_hall` FOREIGN KEY (`screening_hall_id`) REFERENCES `screening_halls` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_maint_time` (`screening_hall_id`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- ZONE 2: IDENTITY, AUTH & MEMBERSHIP (TV 2 - 5 TABLES)
-- ----------------------------------------------------------------------------

-- 7. Bảng roles (Vai trò)
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. Bảng membership_tiers (Hạng hội viên)
DROP TABLE IF EXISTS `membership_tiers`;
CREATE TABLE `membership_tiers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `tier_name` VARCHAR(50) NOT NULL UNIQUE,
    `min_spent` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `discount_percent` INT NOT NULL DEFAULT 0,
    `point_rate` DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. Bảng users (Tài khoản người dùng toàn hệ thống)
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `role_id` INT NOT NULL,
    `branch_id` INT NULL,
    `email` VARCHAR(120) NOT NULL UNIQUE,
    `password_hash` VARCHAR(255) NOT NULL,
    `full_name` VARCHAR(120) NOT NULL,
    `phone` VARCHAR(20) NULL UNIQUE,
    `loyalty_points` INT NOT NULL DEFAULT 0,
    `tier_id` INT NULL,
    `avatar_url` VARCHAR(255) NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_users_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_users_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `fk_users_tier` FOREIGN KEY (`tier_id`) REFERENCES `membership_tiers` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_users_email` (`email`),
    INDEX `idx_users_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. Bảng point_histories (Lịch sử điểm thưởng)
DROP TABLE IF EXISTS `point_histories`;
CREATE TABLE `point_histories` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `booking_id` BIGINT NULL,
    `points` INT NOT NULL,
    `type` VARCHAR(30) NOT NULL, -- EARNED, REDEEMED
    `description` VARCHAR(255) NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_points_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_points_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. Bảng customer_vouchers (Ví voucher cá nhân)
DROP TABLE IF EXISTS `customer_vouchers`;
CREATE TABLE `customer_vouchers` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` INT NOT NULL,
    `voucher_id` INT NOT NULL,
    `is_used` TINYINT(1) NOT NULL DEFAULT 0,
    `used_at` DATETIME NULL,
    `assigned_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_cust_vouch_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_cust_voucher` (`user_id`, `voucher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- ZONE 3: MOVIES, SHOWTIMES & PRICING (TV 3 - 6 TABLES)
-- ----------------------------------------------------------------------------

-- 12. Bảng genres (Thể loại phim)
DROP TABLE IF EXISTS `genres`;
CREATE TABLE `genres` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(255) NULL,
    `is_active` TINYINT(1) NOT NULL DEFAULT 1,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. Bảng movies (Danh mục phim)
DROP TABLE IF EXISTS `movies`;
CREATE TABLE `movies` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `title` VARCHAR(200) NOT NULL,
    `poster_url` VARCHAR(255) NOT NULL,
    `trailer_url` VARCHAR(255) NULL,
    `duration_minutes` INT NOT NULL,
    `release_date` DATE NOT NULL,
    `age_rating` VARCHAR(10) NOT NULL DEFAULT 'P',
    `language` VARCHAR(50) NOT NULL DEFAULT 'Phụ đề Tiếng Việt',
    `director` VARCHAR(150) NULL,
    `actors` TEXT NULL,
    `synopsis` TEXT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'COMING_SOON', -- COMING_SOON, NOW_SHOWING, STOPPED
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_movies_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. Bảng movie_genres (Liên kết Phim & Thể loại)
DROP TABLE IF EXISTS `movie_genres`;
CREATE TABLE `movie_genres` (
    `movie_id` INT NOT NULL,
    `genre_id` INT NOT NULL,
    PRIMARY KEY (`movie_id`, `genre_id`),
    CONSTRAINT `fk_mg_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_mg_genre` FOREIGN KEY (`genre_id`) REFERENCES `genres` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. Bảng showtimes (Lịch chiếu phim)
DROP TABLE IF EXISTS `showtimes`;
CREATE TABLE `showtimes` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `movie_id` INT NOT NULL,
    `screening_hall_id` INT NOT NULL,
    `start_time` DATETIME NOT NULL,
    `end_time` DATETIME NOT NULL,
    `experience_format` VARCHAR(20) NOT NULL DEFAULT '2D', -- 2D, 3D, IMAX
    `status` VARCHAR(30) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, OPENING, FINISHED, CANCELLED
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_showtimes_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_showtimes_hall` FOREIGN KEY (`screening_hall_id`) REFERENCES `screening_halls` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_showtimes_movie_time` (`movie_id`, `start_time`),
    INDEX `idx_showtimes_hall_time` (`screening_hall_id`, `start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. Bảng ticket_pricings (Bảng cấu hình giá vé)
DROP TABLE IF EXISTS `ticket_pricings`;
CREATE TABLE `ticket_pricings` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `day_type` VARCHAR(20) NOT NULL DEFAULT 'WEEKDAY', -- WEEKDAY, WEEKEND, HOLIDAY
    `time_slot` VARCHAR(20) NOT NULL DEFAULT 'STANDARD', -- EARLY, STANDARD, PRIME
    `experience_format` VARCHAR(20) NOT NULL DEFAULT '2D',
    `base_price` DECIMAL(10,2) NOT NULL DEFAULT 80000.00,
    `effective_from` DATE NOT NULL,
    `effective_to` DATE NULL,
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_pricing_lookup` (`day_type`, `time_slot`, `experience_format`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. Bảng reviews (Đánh giá phim)
DROP TABLE IF EXISTS `reviews`;
CREATE TABLE `reviews` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `movie_id` INT NOT NULL,
    `user_id` INT NOT NULL,
    `rating_score` INT NOT NULL DEFAULT 5,
    `comment` TEXT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'APPROVED',
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_reviews_movie` FOREIGN KEY (`movie_id`) REFERENCES `movies` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_reviews_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- ZONE 4: CORE BOOKING & BILLING ENGINE (TV 4 - 6 TABLES)
-- ----------------------------------------------------------------------------

-- 18. Bảng vouchers (Mã giảm giá)
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

-- 19. Bảng seat_holdings (Khóa ghế tạm 5 phút thời gian thực - FE-05.4 / JOB-01)
DROP TABLE IF EXISTS `seat_holdings`;
CREATE TABLE `seat_holdings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `showtime_id` INT NOT NULL,
    `seat_id` INT NOT NULL,
    `session_id` VARCHAR(100) NOT NULL,
    `held_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `expires_at` DATETIME NOT NULL,
    CONSTRAINT `fk_holding_showtime` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_holding_seat` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY `uk_showtime_seat_holding` (`showtime_id`, `seat_id`),
    INDEX `idx_holding_expiry` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 20. Bảng bookings (Đơn đặt chỗ Master / Hóa đơn chính)
DROP TABLE IF EXISTS `bookings`;
CREATE TABLE `bookings` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_code` VARCHAR(32) NOT NULL UNIQUE,
    `user_id` INT NULL,
    `staff_id` INT NULL,
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

-- 21. Bảng tickets (Vé xem phim vật lý / điện tử chi tiết - Chốt chặn uk_showtime_seat)
DROP TABLE IF EXISTS `tickets`;
CREATE TABLE `tickets` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL,
    `showtime_id` INT NOT NULL,
    `seat_id` INT NOT NULL,
    `barcode` VARCHAR(64) NOT NULL UNIQUE,
    `ticket_price` DECIMAL(10,2) NOT NULL,
    `status` VARCHAR(30) NOT NULL DEFAULT 'VALID', -- VALID, CHECKED_IN, REFUNDED
    `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_tickets_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_tickets_showtime` FOREIGN KEY (`showtime_id`) REFERENCES `showtimes` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT `fk_tickets_seat` FOREIGN KEY (`seat_id`) REFERENCES `seats` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE KEY `uk_showtime_seat` (`showtime_id`, `seat_id`),
    INDEX `idx_tickets_barcode` (`barcode`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- ----------------------------------------------------------------------------
-- ZONE 5: OPERATIONS, POS, F&B & CHECK-IN (TV 5 - 5 TABLES)
-- ----------------------------------------------------------------------------

-- 22. Bảng fnb_categories (Danh mục bắp nước)
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

-- 23. Bảng fnb_items (Danh mục sản phẩm Bắp & Nước)
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

-- 24. Bảng order_items (Chi tiết bắp nước & combo đi kèm đơn hàng - Chuẩn 3NF tinh giản)
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
    CONSTRAINT `fk_orderitems_fnb` FOREIGN KEY (`fnb_item_id`) REFERENCES `fnb_items` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_orderitems_booking` (`booking_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 25. Bảng payments (Lịch sử thanh toán)
DROP TABLE IF EXISTS `payments`;
CREATE TABLE `payments` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `booking_id` BIGINT NOT NULL,
    `payment_method` VARCHAR(30) NOT NULL DEFAULT 'VNPAY', -- VNPAY, CASH, MOMO, POINTS
    `amount` DECIMAL(12,2) NOT NULL,
    `transaction_no` VARCHAR(100) NULL,
    `payment_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, FAILED, REFUNDED
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT `fk_payments_booking` FOREIGN KEY (`booking_id`) REFERENCES `bookings` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    INDEX `idx_payments_booking` (`booking_id`),
    INDEX `idx_payments_txn` (`transaction_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 26. Bảng branch_inventories (Tồn kho bắp nước chi nhánh)
DROP TABLE IF EXISTS `branch_inventories`;
CREATE TABLE `branch_inventories` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `branch_id` INT NOT NULL,
    `fnb_item_id` INT NOT NULL,
    `stock_quantity` INT NOT NULL DEFAULT 0,
    `warning_threshold` INT NOT NULL DEFAULT 10,
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT `fk_inv_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_inv_fnb` FOREIGN KEY (`fnb_item_id`) REFERENCES `fnb_items` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY `uk_branch_fnb` (`branch_id`, `fnb_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 27. Bảng ticket_checkin_logs (Nhật ký soát vé QR)
DROP TABLE IF EXISTS `ticket_checkin_logs`;
CREATE TABLE `ticket_checkin_logs` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `ticket_id` BIGINT NOT NULL,
    `staff_id` INT NULL,
    `checkin_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` VARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, REJECTED_ALREADY_USED, WRONG_SHOWTIME
    CONSTRAINT `fk_checkin_ticket` FOREIGN KEY (`ticket_id`) REFERENCES `tickets` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_checkin_staff` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX `idx_checkin_ticket` (`ticket_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 28. Bảng cash_drawers (Quản lý ca làm việc và két tiền POS)
DROP TABLE IF EXISTS `cash_drawers`;
CREATE TABLE `cash_drawers` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `branch_id` INT NOT NULL,
    `staff_id` INT NOT NULL,
    `opening_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `closing_time` DATETIME NULL,
    `starting_cash` DECIMAL(12,2) NOT NULL DEFAULT 1000000.00,
    `total_cash_sales` DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    `ending_cash` DECIMAL(12,2) NULL,
    `difference_amount` DECIMAL(12,2) NULL DEFAULT 0.00,
    `status` VARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, CLOSED
    CONSTRAINT `fk_drawer_branch` FOREIGN KEY (`branch_id`) REFERENCES `branches` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `fk_drawer_staff` FOREIGN KEY (`staff_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
    INDEX `idx_drawer_branch_status` (`branch_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;
