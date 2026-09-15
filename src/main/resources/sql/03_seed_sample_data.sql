-- ============================================================================
-- MULTI-BRANCH CINEMA MANAGEMENT SYSTEM (SWP391)
-- SCRIPT 03: SAMPLE DEMONSTRATION SEED DATA (PHASE 2)
-- Engine: MySQL 8.0+ | Charset: utf8mb4 | Collation: utf8mb4_unicode_ci
-- ============================================================================

USE `cinema_chain_db`;

SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------------------------------------------------------
-- 1. SEED BRANCHES (Cụm rạp chi nhánh Hà Nội & Sài Gòn)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `branches`;
INSERT INTO `branches` (`id`, `branch_code`, `name`, `address`, `city`, `phone`, `email`, `total_halls`, `is_active`, `is_deleted`, `created_at`) VALUES
(1, 'B-HN01', 'CineMax Vincom Bà Triệu', 'Tầng 6, TTTM Vincom Center, 191 Bà Triệu, Q. Hai Bà Trưng', 'Hà Nội', '02439741234', 'hn.batrieu@cinemax.vn', 2, 1, 0, NOW()),
(2, 'B-SG01', 'CineMax Landmark 81', 'Tầng B1, Tòa Landmark 81, 720A Điện Biên Phủ, Q. Bình Thạnh', 'Hồ Chí Minh', '02838991234', 'sg.landmark81@cinemax.vn', 2, 1, 0, NOW());

-- Liên kết tài khoản nhân sự với chi nhánh làm việc
UPDATE `users` SET `branch_id` = 1 WHERE `id` IN (2, 3); -- Manager HN & Staff HN
UPDATE `users` SET `branch_id` = 2 WHERE `id` = 4;        -- Staff SG

-- ----------------------------------------------------------------------------
-- 2. SEED SCREENING HALLS (Phòng chiếu mỗi chi nhánh: 1 Standard 2D, 1 IMAX 3D)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `screening_halls`;
INSERT INTO `screening_halls` (`id`, `branch_id`, `name`, `hall_type`, `total_rows`, `total_columns`, `total_capacity`, `status`, `is_deleted`, `created_at`) VALUES
(1, 1, 'Cinema Hall 01 (2D Digital)', 'STANDARD_2D', 10, 10, 100, 'ACTIVE', 0, NOW()),
(2, 1, 'IMAX Laser Hall 02',          'IMAX_3D',    10, 10, 100, 'ACTIVE', 0, NOW()),
(3, 2, 'Cinema Hall 01 (2D Digital)', 'STANDARD_2D', 10, 10, 100, 'ACTIVE', 0, NOW()),
(4, 2, 'IMAX Laser Hall 02',          'IMAX_3D',    10, 10, 100, 'ACTIVE', 0, NOW());

-- ----------------------------------------------------------------------------
-- 3. SEED SEATS (Tạo ma trận 100 ghế ngồi 10x10 cho 4 phòng chiếu = 400 ghế)
-- Quy luật: Hàng A, B, C: STANDARD (1) | Hàng D -> H: VIP (2) | Hàng I, J: COUPLE (3)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `seats`;

DROP PROCEDURE IF EXISTS `sp_seed_hall_seats`;
DELIMITER $$
CREATE PROCEDURE `sp_seed_hall_seats`(IN p_hall_id INT)
BEGIN
    DECLARE r INT DEFAULT 1;
    DECLARE c INT DEFAULT 1;
    DECLARE v_row_letter VARCHAR(2);
    DECLARE v_type_id INT;
    DECLARE v_seat_code VARCHAR(10);

    WHILE r <= 10 DO
        SET v_row_letter = CHAR(64 + r); -- 1->A, 2->B, ..., 10->J
        
        -- Phân định loại ghế theo hàng
        IF r <= 3 THEN
            SET v_type_id = 1; -- STANDARD
        ELSEIF r <= 8 THEN
            SET v_type_id = 2; -- VIP
        ELSE
            SET v_type_id = 3; -- COUPLE (Sweetbox)
        END IF;

        SET c = 1;
        WHILE c <= 10 DO
            SET v_seat_code = CONCAT(v_row_letter, LPAD(c, 2, '0'));
            
            INSERT INTO `seats` 
                (`screening_hall_id`, `seat_type_id`, `seat_row`, `seat_number`, `seat_code`, `grid_row_index`, `grid_col_index`, `is_active`, `is_deleted`, `created_at`)
            VALUES 
                (p_hall_id, v_type_id, v_row_letter, c, v_seat_code, r, c, 1, 0, NOW());
            
            SET c = c + 1;
        END WHILE;
        
        SET r = r + 1;
    END WHILE;
END$$
DELIMITER ;

CALL `sp_seed_hall_seats`(1);
CALL `sp_seed_hall_seats`(2);
CALL `sp_seed_hall_seats`(3);
CALL `sp_seed_hall_seats`(4);
DROP PROCEDURE IF EXISTS `sp_seed_hall_seats`;

-- ----------------------------------------------------------------------------
-- 4. SEED MOVIES (5 bộ phim bom tấn đa dạng thể loại & độ tuổi)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `movies`;
INSERT INTO `movies` (`id`, `title`, `poster_url`, `trailer_url`, `duration_minutes`, `release_date`, `age_rating`, `language`, `director`, `actors`, `synopsis`, `status`, `is_deleted`, `created_at`) VALUES
(1, 'Dune: Hành Tinh Cát - Phần Hai', 
    'https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg', 
    'https://www.youtube.com/watch?v=Way9Dexny3w', 
    166, '2024-03-01', 'C16', 'Tiếng Anh - Phụ đề Tiếng Việt', 
    'Denis Villeneuve', 'Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem', 
    'Paul Atreides hợp nhất với Chani và người Fremen để trả thù những kẻ đã hủy diệt gia tộc mình, đối mặt với sự giằng xé giữa tình yêu và định mệnh vũ trụ.', 
    'NOW_SHOWING', 0, NOW()),

(2, 'Godzilla x Kong: Đế Chế Mới', 
    'https://image.tmdb.org/t/p/w500/tMefBSflR6PGQLv7WvFPpKLZkyk.jpg', 
    'https://www.youtube.com/watch?v=lV1OOlGwExg', 
    115, '2024-03-29', 'C13', 'Tiếng Anh - Phụ đề & Lồng tiếng', 
    'Adam Wingard', 'Rebecca Hall, Brian Tyree Henry, Dan Stevens, Kaylee Hottle', 
    'Hai quái thú huyền thoại Godzilla và Kong buộc phải gạt bỏ hiềm khích để cùng kề vai tác chiến trước hiểm họa Skar King đe dọa nuốt chửng cả Trái Đất.', 
    'NOW_SHOWING', 0, NOW()),

(3, 'Kung Fu Panda 4', 
    'https://image.tmdb.org/t/p/w500/kDp1vUBnMpe8ak4rjgl3cLELqjU.jpg', 
    'https://www.youtube.com/watch?v=_inKs4eeHiI', 
    94, '2024-03-08', 'P', 'Lồng tiếng Tiếng Việt', 
    'Mike Mitchell', 'Jack Black, Awkwafina, Viola Davis, Dustin Hoffman', 
    'Gấu Po chuẩn bị bước lên vị trí Thủ lĩnh Tinh thần của Thung lũng Bình Yên và lên đường tìm kiếm truyền nhân Thần Long Đại Hiệp tiếp theo.', 
    'NOW_SHOWING', 0, NOW()),

(4, 'Deadpool & Wolverine', 
    'https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg', 
    'https://www.youtube.com/watch?v=73_1biulkYk', 
    128, '2024-07-26', 'C18', 'Tiếng Anh - Phụ đề Tiếng Việt', 
    'Shawn Levy', 'Ryan Reynolds, Hugh Jackman, Emma Corrin, Morena Baccarin', 
    'Tổ chức Phương sai Thời gian (TVA) lôi kéo Deadpool vào một nhiệm vụ bảo vệ dòng thời gian, buộc anh phải hợp tác cùng một Wolverine bất đắc dĩ.', 
    'COMING_SOON', 0, NOW()),

(5, 'Mai (Đạo diễn Trấn Thành)', 
    'https://upload.wikimedia.org/wikipedia/vi/6/6f/Mai_poster.jpg', 
    'https://www.youtube.com/watch?v=5XnB1Zc4lZs', 
    131, '2024-02-10', 'C18', 'Tiếng Việt - Phụ đề Tiếng Anh', 
    'Trấn Thành', 'Phương Anh Đào, Tuấn Trần, Trấn Thành, NSND Việt Anh, Hồng Đào', 
    'Mai - một phụ nữ làm nghề massage trị liệu gần 40 tuổi với nhiều tổn thương quá khứ, bất ngờ đón nhận tình yêu chân thành từ chàng nhạc công đào hoa Dương.', 
    'NOW_SHOWING', 0, NOW());

-- ----------------------------------------------------------------------------
-- 5. SEED MOVIE GENRES (Bảng liên kết Phim & Thể loại)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `movie_genres`;
INSERT INTO `movie_genres` (`movie_id`, `genre_id`) VALUES
(1, 1), (1, 2), (1, 6), -- Dune 2: Action, Adventure, Sci-Fi
(2, 1), (2, 6),         -- Godzilla x Kong: Action, Sci-Fi
(3, 3), (3, 4), (3, 10),-- Kung Fu Panda 4: Animation, Comedy, Family
(4, 1), (4, 4), (4, 6), -- Deadpool: Action, Comedy, Sci-Fi
(5, 7), (5, 9);         -- Mai: Romance, Drama

-- ----------------------------------------------------------------------------
-- 6. SEED TICKET PRICINGS (Cấu hình bảng giá chuẩn theo khung giờ & định dạng)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `ticket_pricings`;
INSERT INTO `ticket_pricings` (`id`, `day_type`, `time_slot`, `experience_format`, `base_price`, `effective_from`, `effective_to`, `is_deleted`, `created_at`) VALUES
(1, 'WEEKDAY', 'EARLY',    '2D',   75000.00, '2024-01-01', NULL, 0, NOW()),
(2, 'WEEKDAY', 'STANDARD', '2D',   85000.00, '2024-01-01', NULL, 0, NOW()),
(3, 'WEEKDAY', 'PRIME',    '2D',   95000.00, '2024-01-01', NULL, 0, NOW()),
(4, 'WEEKDAY', 'STANDARD', '3D',  110000.00, '2024-01-01', NULL, 0, NOW()),
(5, 'WEEKEND', 'EARLY',    '2D',   90000.00, '2024-01-01', NULL, 0, NOW()),
(6, 'WEEKEND', 'STANDARD', '2D',  105000.00, '2024-01-01', NULL, 0, NOW()),
(7, 'WEEKEND', 'PRIME',    '2D',  120000.00, '2024-01-01', NULL, 0, NOW()),
(8, 'WEEKEND', 'STANDARD', '3D',  135000.00, '2024-01-01', NULL, 0, NOW()),
(9, 'WEEKEND', 'PRIME',    '3D',  150000.00, '2024-01-01', NULL, 0, NOW());

-- ----------------------------------------------------------------------------
-- 7. SEED SHOWTIMES (Suất chiếu hôm nay, ngày mai và ngày kia)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `showtimes`;
INSERT INTO `showtimes` (`id`, `movie_id`, `screening_hall_id`, `start_time`, `end_time`, `experience_format`, `status`, `is_deleted`, `created_at`) VALUES
-- Rạp 1 - Hà Nội: Hall 1 (2D)
(1, 1, 1, CONCAT(CURDATE(), ' 09:30:00'), CONCAT(CURDATE(), ' 12:20:00'), '2D', 'SCHEDULED', 0, NOW()),
(2, 3, 1, CONCAT(CURDATE(), ' 13:00:00'), CONCAT(CURDATE(), ' 14:40:00'), '2D', 'SCHEDULED', 0, NOW()),
(3, 5, 1, CONCAT(CURDATE(), ' 15:30:00'), CONCAT(CURDATE(), ' 17:45:00'), '2D', 'SCHEDULED', 0, NOW()),
(4, 1, 1, CONCAT(CURDATE(), ' 19:30:00'), CONCAT(CURDATE(), ' 22:20:00'), '2D', 'OPENING',   0, NOW()),

-- Rạp 1 - Hà Nội: Hall 2 (IMAX 3D)
(5, 1, 2, CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 12:50:00'), '3D', 'SCHEDULED', 0, NOW()),
(6, 2, 2, CONCAT(CURDATE(), ' 14:00:00'), CONCAT(CURDATE(), ' 16:00:00'), '3D', 'SCHEDULED', 0, NOW()),
(7, 2, 2, CONCAT(CURDATE(), ' 18:00:00'), CONCAT(CURDATE(), ' 20:00:00'), '3D', 'OPENING',   0, NOW()),
(8, 1, 2, CONCAT(CURDATE(), ' 20:45:00'), CONCAT(CURDATE(), ' 23:35:00'), '3D', 'SCHEDULED', 0, NOW()),

-- Rạp 2 - Sài Gòn: Hall 3 (2D)
(9,  3, 3, CONCAT(CURDATE(), ' 10:30:00'), CONCAT(CURDATE(), ' 12:10:00'), '2D', 'SCHEDULED', 0, NOW()),
(10, 5, 3, CONCAT(CURDATE(), ' 14:00:00'), CONCAT(CURDATE(), ' 16:15:00'), '2D', 'SCHEDULED', 0, NOW()),
(11, 1, 3, CONCAT(CURDATE(), ' 19:00:00'), CONCAT(CURDATE(), ' 21:50:00'), '2D', 'OPENING',   0, NOW()),

-- Rạp 2 - Sài Gòn: Hall 4 (IMAX 3D)
(12, 2, 4, CONCAT(CURDATE(), ' 11:00:00'), CONCAT(CURDATE(), ' 13:00:00'), '3D', 'SCHEDULED', 0, NOW()),
(13, 1, 4, CONCAT(CURDATE(), ' 15:00:00'), CONCAT(CURDATE(), ' 17:50:00'), '3D', 'SCHEDULED', 0, NOW()),
(14, 2, 4, CONCAT(CURDATE(), ' 19:30:00'), CONCAT(CURDATE(), ' 21:30:00'), '3D', 'OPENING',   0, NOW()),

-- Suất chiếu ngày mai (CURDATE() + 1 DAY)
(15, 1, 1, DATE_ADD(CONCAT(CURDATE(), ' 10:00:00'), INTERVAL 1 DAY), DATE_ADD(CONCAT(CURDATE(), ' 12:50:00'), INTERVAL 1 DAY), '2D', 'SCHEDULED', 0, NOW()),
(16, 2, 2, DATE_ADD(CONCAT(CURDATE(), ' 14:00:00'), INTERVAL 1 DAY), DATE_ADD(CONCAT(CURDATE(), ' 16:00:00'), INTERVAL 1 DAY), '3D', 'SCHEDULED', 0, NOW());

-- ----------------------------------------------------------------------------
-- 8. SEED F&B ITEMS (Sản phẩm Bắp & Nước giải khát)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `fnb_items`;
INSERT INTO `fnb_items` (`id`, `category_id`, `item_code`, `name`, `price`, `image_url`, `is_combo`, `is_active`, `is_deleted`, `created_at`) VALUES
(1, 1, 'FNB-PC-01', 'Bắp Rang Bơ Truyền Thống 60oz',    55000.00, '/assets/images/fnb/popcorn_butter.png',   0, 1, 0, NOW()),
(2, 1, 'FNB-PC-02', 'Bắp Rang Vị Phô Mai 60oz',         65000.00, '/assets/images/fnb/popcorn_cheese.png',   0, 1, 0, NOW()),
(3, 1, 'FNB-PC-03', 'Bắp Rang Vị Caramel 60oz',         65000.00, '/assets/images/fnb/popcorn_caramel.png',  0, 1, 0, NOW()),
(4, 2, 'FNB-DK-01', 'Pepsi Vị Chanh Không Calo 22oz',    35000.00, '/assets/images/fnb/pepsi.png',            0, 1, 0, NOW()),
(5, 2, 'FNB-DK-02', 'Coca-Cola Mát Lạnh 22oz',           35000.00, '/assets/images/fnb/cocacola.png',         0, 1, 0, NOW()),
(6, 2, 'FNB-DK-03', 'Nước Khoáng Dasani 500ml',         20000.00, '/assets/images/fnb/water.png',            0, 1, 0, NOW()),
(7, 2, 'FNB-DK-04', 'Trà Sữa Trân Châu Hoàng Gia 500ml', 45000.00, '/assets/images/fnb/milktea.png',          0, 1, 0, NOW()),
(8, 3, 'FNB-CB-01', 'Solo Combo (1 Bắp Ngọt + 1 Nước)',  79000.00, '/assets/images/fnb/combo_solo.png',       1, 1, 0, NOW()),
(9, 3, 'FNB-CB-02', 'Couple Combo (1 Bắp Lớn + 2 Nước)', 119000.00, '/assets/images/fnb/combo_couple.png',     1, 1, 0, NOW()),
(10,3, 'FNB-CB-03', 'Family Party (2 Bắp + 3 Nước + Snack)', 189000.00, '/assets/images/fnb/combo_family.png', 1, 1, 0, NOW());

-- ----------------------------------------------------------------------------
-- 9. SEED BRANCH INVENTORIES (Kho hàng bắp nước cho từng cụm rạp)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `branch_inventories`;
INSERT INTO `branch_inventories` (`branch_id`, `fnb_item_id`, `stock_quantity`, `warning_threshold`, `updated_at`) VALUES
(1, 1, 150, 20, NOW()), (1, 2, 120, 20, NOW()), (1, 3, 110, 20, NOW()), (1, 4, 300, 50, NOW()), (1, 5, 280, 50, NOW()),
(1, 6, 200, 30, NOW()), (1, 7,  80, 15, NOW()), (1, 8, 100, 20, NOW()), (1, 9,  90, 20, NOW()), (1, 10, 60, 10, NOW()),
(2, 1, 180, 20, NOW()), (2, 2, 140, 20, NOW()), (2, 3, 130, 20, NOW()), (2, 4, 350, 50, NOW()), (2, 5, 320, 50, NOW()),
(2, 6, 250, 30, NOW()), (2, 7,  95, 15, NOW()), (2, 8, 120, 20, NOW()), (2, 9, 110, 20, NOW()), (2, 10, 75, 10, NOW());

-- ----------------------------------------------------------------------------
-- 10. SEED VOUCHERS (Chương trình khuyến mãi giảm giá)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `vouchers`;
INSERT INTO `vouchers` (`id`, `code`, `discount_type`, `discount_val`, `min_order_amount`, `max_discount`, `usage_limit`, `used_count`, `valid_from`, `valid_to`, `is_active`, `is_deleted`, `created_at`) VALUES
(1, 'CINEMA10',    'PERCENT',      10.00, 100000.00,  50000.00, 500,  12, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 0, NOW()),
(2, 'WELCOME50',   'FIXED_AMOUNT', 50000.00, 150000.00,  50000.00, 200,  45, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 0, NOW()),
(3, 'VIPGOLD20',   'PERCENT',      20.00, 200000.00, 100000.00, 100,   8, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 0, NOW());

-- Phân phối Voucher cho tài khoản khách hàng mẫu (user_id = 5)
TRUNCATE TABLE `customer_vouchers`;
INSERT INTO `customer_vouchers` (`id`, `user_id`, `voucher_id`, `is_used`, `used_at`, `assigned_at`) VALUES
(1, 5, 1, 0, NULL, NOW()),
(2, 5, 2, 0, NULL, NOW());

-- ----------------------------------------------------------------------------
-- 11. SEED CASH DRAWERS (Mở ca làm việc mẫu cho nhân viên quầy POS)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `cash_drawers`;
INSERT INTO `cash_drawers` (`id`, `branch_id`, `staff_id`, `opening_time`, `closing_time`, `starting_cash`, `total_cash_sales`, `ending_cash`, `difference_amount`, `status`) VALUES
(1, 1, 3, CONCAT(CURDATE(), ' 08:00:00'), NULL, 1000000.00, 0.00, NULL, 0.00, 'OPEN'),
(2, 2, 4, CONCAT(CURDATE(), ' 08:30:00'), NULL, 1000000.00, 0.00, NULL, 0.00, 'OPEN');

-- ----------------------------------------------------------------------------
-- 12. SEED REVIEWS (Đánh giá phim từ khán giả)
-- ----------------------------------------------------------------------------
TRUNCATE TABLE `reviews`;
INSERT INTO `reviews` (`id`, `movie_id`, `user_id`, `rating_score`, `comment`, `status`, `is_deleted`, `created_at`) VALUES
(1, 1, 5, 5, 'Phần 2 thực sự là một kiệt tác điện ảnh! Hình ảnh và âm thanh IMAX quá sức choáng ngợp.', 'APPROVED', 0, NOW()),
(2, 3, 5, 4, 'Phim hoạt hình vui nhộn, gấu Po vẫn giữ được sự hài hước đặc trưng, rất thích hợp xem cùng gia đình.', 'APPROVED', 0, NOW()),
(3, 5, 5, 5, 'Diễn xuất của Phương Anh Đào và Tuấn Trần chạm đến cảm xúc. Rất đáng xem!', 'APPROVED', 0, NOW());

SET FOREIGN_KEY_CHECKS = 1;
