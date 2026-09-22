-- ============================================================================
-- MULTI-BRANCH CINEMA MANAGEMENT SYSTEM (SWP391)
-- SCRIPT 03: SAMPLE DEMONSTRATION SEED DATA (PHASE 2 - T-SQL)
-- Engine: Microsoft SQL Server 2019+ | Collation: Vietnamese_CI_AS
-- ============================================================================

USE cinema_chain_db;
GO

-- ----------------------------------------------------------------------------
-- 1. SEED CINEMAS (Cụm rạp chiếu phim Hà Nội & Sài Gòn)
-- ----------------------------------------------------------------------------
DELETE FROM cinemas;
GO
SET IDENTITY_INSERT cinemas ON;
INSERT INTO cinemas (id, cinema_code, name, address, city, phone, email, total_rooms, is_active, is_deleted, created_at) VALUES
(1, N'C-HN01', N'CineMax Vincom Bà Triệu', N'Tầng 6, TTTM Vincom Center, 191 Bà Triệu, Q. Hai Bà Trưng', N'Hà Nội', N'02439741234', N'hn.batrieu@cinemax.vn', 2, 1, 0, GETDATE()),
(2, N'C-SG01', N'CineMax Landmark 81', N'Tầng B1, Tòa Landmark 81, 720A Điện Biên Phủ, Q. Bình Thạnh', N'Hồ Chí Minh', N'02838991234', N'sg.landmark81@cinemax.vn', 2, 1, 0, GETDATE());
SET IDENTITY_INSERT cinemas OFF;
GO

-- Liên kết tài khoản nhân sự với rạp làm việc
UPDATE users SET cinema_id = 1 WHERE id IN (2, 3); -- Manager HN & Staff HN
UPDATE users SET cinema_id = 2 WHERE id = 4;        -- Staff SG
GO

-- ----------------------------------------------------------------------------
-- 2. SEED SCREENING ROOMS (Phòng chiếu mỗi chi nhánh: 1 Standard 2D, 1 IMAX 3D)
-- ----------------------------------------------------------------------------
DELETE FROM screening_rooms;
GO
SET IDENTITY_INSERT screening_rooms ON;
INSERT INTO screening_rooms (id, cinema_id, name, room_type, total_rows, total_columns, total_capacity, status, is_deleted, created_at) VALUES
(1, 1, N'Cinema Room 01 (2D Digital)', N'STANDARD_2D', 10, 10, 100, N'ACTIVE', 0, GETDATE()),
(2, 1, N'IMAX Laser Room 02',          N'IMAX_3D',    10, 10, 100, N'ACTIVE', 0, GETDATE()),
(3, 2, N'Cinema Room 01 (2D Digital)', N'STANDARD_2D', 10, 10, 100, N'ACTIVE', 0, GETDATE()),
(4, 2, N'IMAX Laser Room 02',          N'IMAX_3D',    10, 10, 100, N'ACTIVE', 0, GETDATE());
SET IDENTITY_INSERT screening_rooms OFF;
GO

-- ----------------------------------------------------------------------------
-- 3. SEED SEATS (Tạo ma trận 100 ghế ngồi 10x10 cho 4 phòng chiếu = 400 ghế)
-- Quy luật: Hàng A, B, C: STANDARD (1) | Hàng D -> H: VIP (2) | Hàng I, J: COUPLE (3)
-- ----------------------------------------------------------------------------
DELETE FROM seats;
GO

CREATE OR ALTER PROCEDURE sp_seed_room_seats @p_room_id INT
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @r INT = 1;
    DECLARE @c INT = 1;
    DECLARE @v_row_letter NVARCHAR(2);
    DECLARE @v_type_id INT;
    DECLARE @v_seat_code NVARCHAR(10);

    WHILE @r <= 10
    BEGIN
        SET @v_row_letter = CHAR(64 + @r); -- 1->A, 2->B, ..., 10->J
        
        -- Phân định loại ghế theo hàng
        IF @r <= 3
            SET @v_type_id = 1; -- STANDARD
        ELSE IF @r <= 8
            SET @v_type_id = 2; -- VIP
        ELSE
            SET @v_type_id = 3; -- COUPLE (Sweetbox)

        SET @c = 1;
        WHILE @c <= 10
        BEGIN
            SET @v_seat_code = @v_row_letter + RIGHT('0' + CAST(@c AS VARCHAR(2)), 2);
            
            INSERT INTO seats 
                (screening_room_id, seat_type_id, seat_row, seat_number, seat_code, grid_row_index, grid_col_index, status, is_active, is_deleted, created_at)
            VALUES 
                (@p_room_id, @v_type_id, @v_row_letter, @c, @v_seat_code, @r, @c, N'ACTIVE', 1, 0, GETDATE());
            
            SET @c = @c + 1;
        END

        SET @r = @r + 1;
    END
END;
GO

EXEC sp_seed_room_seats @p_room_id = 1;
EXEC sp_seed_room_seats @p_room_id = 2;
EXEC sp_seed_room_seats @p_room_id = 3;
EXEC sp_seed_room_seats @p_room_id = 4;
GO

DROP PROCEDURE IF EXISTS sp_seed_room_seats;
GO

-- ----------------------------------------------------------------------------
-- 4. SEED MOVIES (5 bộ phim bom tấn đa dạng thể loại & độ tuổi)
-- ----------------------------------------------------------------------------
DELETE FROM movies;
GO
SET IDENTITY_INSERT movies ON;
INSERT INTO movies (id, title, poster_url, trailer_url, duration_minutes, release_date, age_rating, language, director, actors, synopsis, status, is_deleted, created_at) VALUES
(1, N'Dune: Hành Tinh Cát - Phần Hai', 
    N'https://image.tmdb.org/t/p/w500/8b8R8l88Qje9dn9OE8PY05Nxl1X.jpg', 
    N'https://www.youtube.com/watch?v=Way9Dexny3w', 
    166, '2024-03-01', N'C16', N'Tiếng Anh - Phụ đề Tiếng Việt', 
    N'Denis Villeneuve', N'Timothée Chalamet, Zendaya, Rebecca Ferguson, Javier Bardem', 
    N'Paul Atreides hợp nhất với Chani và người Fremen để trả thù những kẻ đã hủy diệt gia tộc mình, đối mặt với sự giằng xé giữa tình yêu và định mệnh vũ trụ.', 
    N'NOW_SHOWING', 0, GETDATE()),

(2, N'Godzilla x Kong: Đế Chế Mới', 
    N'https://image.tmdb.org/t/p/w500/tMefBSflR6PGQLv7WvFPpKLZkyk.jpg', 
    N'https://www.youtube.com/watch?v=lV1OOlGwExg', 
    115, '2024-03-29', N'C13', N'Tiếng Anh - Phụ đề & Lồng tiếng', 
    N'Adam Wingard', N'Rebecca Hall, Brian Tyree Henry, Dan Stevens, Kaylee Hottle', 
    N'Hai quái thú huyền thoại Godzilla và Kong buộc phải gạt bỏ hiềm khích để cùng kề vai tác chiến trước hiểm họa Skar King đe dọa nuốt chửng cả Trái Đất.', 
    N'NOW_SHOWING', 0, GETDATE()),

(3, N'Kung Fu Panda 4', 
    N'https://image.tmdb.org/t/p/w500/kDp1vUBnMpe8ak4rjgl3cLELqjU.jpg', 
    N'https://www.youtube.com/watch?v=_inKs4eeHiI', 
    94, '2024-03-08', N'P', N'Lồng tiếng Tiếng Việt', 
    N'Mike Mitchell', N'Jack Black, Awkwafina, Viola Davis, Dustin Hoffman', 
    N'Gấu Po chuẩn bị bước lên vị trí Thủ lĩnh Tinh thần của Thung lũng Bình Yên và lên đường tìm kiếm truyền nhân Thần Long Đại Hiệp tiếp theo.', 
    N'NOW_SHOWING', 0, GETDATE()),

(4, N'Deadpool & Wolverine', 
    N'https://image.tmdb.org/t/p/w500/8cdWjvZQUExUUTzyp4t6EDMubfO.jpg', 
    N'https://www.youtube.com/watch?v=73_1biulkYk', 
    128, '2024-07-26', N'C18', N'Tiếng Anh - Phụ đề Tiếng Việt', 
    N'Shawn Levy', N'Ryan Reynolds, Hugh Jackman, Emma Corrin, Morena Baccarin', 
    N'Tổ chức Phương sai Thời gian (TVA) lôi kéo Deadpool vào một nhiệm vụ bảo vệ dòng thời gian, buộc anh phải hợp tác cùng một Wolverine bất đắc dĩ.', 
    N'COMING_SOON', 0, GETDATE()),

(5, N'Mai (Đạo diễn Trấn Thành)', 
    N'https://upload.wikimedia.org/wikipedia/vi/6/6f/Mai_poster.jpg', 
    N'https://www.youtube.com/watch?v=5XnB1Zc4lZs', 
    131, '2024-02-10', N'C18', N'Tiếng Việt - Phụ đề Tiếng Anh', 
    N'Trấn Thành', N'Phương Anh Đào, Tuấn Trần, Trấn Thành, NSND Việt Anh, Hồng Đào', 
    N'Mai - một phụ nữ làm nghề massage trị liệu gần 40 tuổi với nhiều tổn thương quá khứ, bất ngờ đón nhận tình yêu chân thành từ chàng nhạc công đào hoa Dương.', 
    N'NOW_SHOWING', 0, GETDATE());
SET IDENTITY_INSERT movies OFF;
GO

-- ----------------------------------------------------------------------------
-- 5. SEED MOVIE GENRES (Bảng liên kết Phim & Thể loại)
-- ----------------------------------------------------------------------------
DELETE FROM movie_genres;
GO
INSERT INTO movie_genres (movie_id, genre_id) VALUES
(1, 1), (1, 2), (1, 6), -- Dune 2: Action, Adventure, Sci-Fi
(2, 1), (2, 6),         -- Godzilla x Kong: Action, Sci-Fi
(3, 3), (3, 4), (3, 10),-- Kung Fu Panda 4: Animation, Comedy, Family
(4, 1), (4, 4), (4, 6), -- Deadpool: Action, Comedy, Sci-Fi
(5, 7), (5, 9);         -- Mai: Romance, Drama
GO

-- ----------------------------------------------------------------------------
-- 6. SEED TICKET PRICINGS (Cấu hình bảng giá chuẩn theo khung giờ & định dạng)
-- ----------------------------------------------------------------------------
DELETE FROM ticket_pricings;
GO
SET IDENTITY_INSERT ticket_pricings ON;
INSERT INTO ticket_pricings (id, day_type, time_slot, experience_format, base_price, effective_from, effective_to, is_deleted, created_at) VALUES
(1, N'WEEKDAY', N'EARLY',    N'2D',   75000.00, '2024-01-01', NULL, 0, GETDATE()),
(2, N'WEEKDAY', N'STANDARD', N'2D',   85000.00, '2024-01-01', NULL, 0, GETDATE()),
(3, N'WEEKDAY', N'PRIME',    N'2D',   95000.00, '2024-01-01', NULL, 0, GETDATE()),
(4, N'WEEKDAY', N'STANDARD', N'3D',  110000.00, '2024-01-01', NULL, 0, GETDATE()),
(5, N'WEEKEND', N'EARLY',    N'2D',   90000.00, '2024-01-01', NULL, 0, GETDATE()),
(6, N'WEEKEND', N'STANDARD', N'2D',  105000.00, '2024-01-01', NULL, 0, GETDATE()),
(7, N'WEEKEND', N'PRIME',    N'2D',  120000.00, '2024-01-01', NULL, 0, GETDATE()),
(8, N'WEEKEND', N'STANDARD', N'3D',  135000.00, '2024-01-01', NULL, 0, GETDATE()),
(9, N'WEEKEND', N'PRIME',    N'3D',  150000.00, '2024-01-01', NULL, 0, GETDATE());
SET IDENTITY_INSERT ticket_pricings OFF;
GO

-- ----------------------------------------------------------------------------
-- 7. SEED SHOWTIMES (Suất chiếu hôm nay, ngày mai và ngày kia)
-- ----------------------------------------------------------------------------
DELETE FROM showtimes;
GO
SET IDENTITY_INSERT showtimes ON;
INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at) VALUES
-- Rạp 1 - Hà Nội: Room 1 (2D)
(1, 1, 1, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 09:30:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 12:20:00'), N'2D', N'SCHEDULED', 0, GETDATE()),
(2, 3, 1, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 13:00:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 14:40:00'), N'2D', N'SCHEDULED', 0, GETDATE()),
(3, 5, 1, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 15:30:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 17:45:00'), N'2D', N'SCHEDULED', 0, GETDATE()),
(4, 1, 1, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 19:30:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 22:20:00'), N'2D', N'OPENING',   0, GETDATE()),

-- Rạp 1 - Hà Nội: Room 2 (IMAX 3D)
(5, 1, 2, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 10:00:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 12:50:00'), N'3D', N'SCHEDULED', 0, GETDATE()),
(6, 2, 2, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 14:00:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 16:00:00'), N'3D', N'SCHEDULED', 0, GETDATE()),
(7, 2, 2, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 18:00:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 20:00:00'), N'3D', N'OPENING',   0, GETDATE()),
(8, 1, 2, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 20:45:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 23:35:00'), N'3D', N'SCHEDULED', 0, GETDATE()),

-- Rạp 2 - Sài Gòn: Room 3 (2D)
(9,  3, 3, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 10:30:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 12:10:00'), N'2D', N'SCHEDULED', 0, GETDATE()),
(10, 5, 3, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 14:00:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 16:15:00'), N'2D', N'SCHEDULED', 0, GETDATE()),
(11, 1, 3, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 19:00:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 21:50:00'), N'2D', N'OPENING',   0, GETDATE()),

-- Rạp 2 - Sài Gòn: Room 4 (IMAX 3D)
(12, 2, 4, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 11:00:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 13:00:00'), N'3D', N'SCHEDULED', 0, GETDATE()),
(13, 1, 4, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 15:00:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 17:50:00'), N'3D', N'SCHEDULED', 0, GETDATE()),
(14, 2, 4, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 19:30:00'), CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 21:30:00'), N'3D', N'OPENING',   0, GETDATE()),

-- Suất chiếu ngày mai
(15, 1, 1, DATEADD(DAY, 1, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 10:00:00')), DATEADD(DAY, 1, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 12:50:00')), N'2D', N'SCHEDULED', 0, GETDATE()),
(16, 2, 2, DATEADD(DAY, 1, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 14:00:00')), DATEADD(DAY, 1, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 16:00:00')), N'3D', N'SCHEDULED', 0, GETDATE());
SET IDENTITY_INSERT showtimes OFF;
GO

-- ----------------------------------------------------------------------------
-- 8. SEED F&B ITEMS (Sản phẩm Bắp & Nước giải khát)
-- ----------------------------------------------------------------------------
DELETE FROM fnb_items;
GO
SET IDENTITY_INSERT fnb_items ON;
INSERT INTO fnb_items (id, category_id, item_code, name, price, image_url, is_combo, is_active, is_deleted, created_at) VALUES
(1, 1, N'FNB-PC-01', N'Bắp Rang Bơ Truyền Thống 60oz',    55000.00, N'/assets/images/fnb/popcorn_butter.png',   0, 1, 0, GETDATE()),
(2, 1, N'FNB-PC-02', N'Bắp Rang Vị Phô Mai 60oz',         65000.00, N'/assets/images/fnb/popcorn_cheese.png',   0, 1, 0, GETDATE()),
(3, 1, N'FNB-PC-03', N'Bắp Rang Vị Caramel 60oz',         65000.00, N'/assets/images/fnb/popcorn_caramel.png',  0, 1, 0, GETDATE()),
(4, 2, N'FNB-DK-01', N'Pepsi Vị Chanh Không Calo 22oz',    35000.00, N'/assets/images/fnb/pepsi.png',            0, 1, 0, GETDATE()),
(5, 2, N'FNB-DK-02', N'Coca-Cola Mát Lạnh 22oz',           35000.00, N'/assets/images/fnb/cocacola.png',         0, 1, 0, GETDATE()),
(6, 2, N'FNB-DK-03', N'Nước Khoáng Dasani 500ml',         20000.00, N'/assets/images/fnb/water.png',            0, 1, 0, GETDATE()),
(7, 2, N'FNB-DK-04', N'Trà Sữa Trân Châu Hoàng Gia 500ml', 45000.00, N'/assets/images/fnb/milktea.png',          0, 1, 0, GETDATE()),
(8, 3, N'FNB-CB-01', N'Solo Combo (1 Bắp Ngọt + 1 Nước)',  79000.00, N'/assets/images/fnb/combo_solo.png',       1, 1, 0, GETDATE()),
(9, 3, N'FNB-CB-02', N'Couple Combo (1 Bắp Lớn + 2 Nước)', 119000.00, N'/assets/images/fnb/combo_couple.png',     1, 1, 0, GETDATE()),
(10,3, N'FNB-CB-03', N'Family Party (2 Bắp + 3 Nước + Snack)', 189000.00, N'/assets/images/fnb/combo_family.png', 1, 1, 0, GETDATE());
SET IDENTITY_INSERT fnb_items OFF;
GO

-- ----------------------------------------------------------------------------
-- 9. SEED CINEMA INVENTORIES (Kho hàng bắp nước cho từng cụm rạp)
-- ----------------------------------------------------------------------------
DELETE FROM cinema_inventories;
GO
INSERT INTO cinema_inventories (cinema_id, fnb_item_id, stock_quantity, warning_threshold, updated_at) VALUES
(1, 1, 150, 20, GETDATE()), (1, 2, 120, 20, GETDATE()), (1, 3, 110, 20, GETDATE()), (1, 4, 300, 50, GETDATE()), (1, 5, 280, 50, GETDATE()),
(1, 6, 200, 30, GETDATE()), (1, 7,  80, 15, GETDATE()), (1, 8, 100, 20, GETDATE()), (1, 9,  90, 20, GETDATE()), (1, 10, 60, 10, GETDATE()),
(2, 1, 180, 20, GETDATE()), (2, 2, 140, 20, GETDATE()), (2, 3, 130, 20, GETDATE()), (2, 4, 350, 50, GETDATE()), (2, 5, 320, 50, GETDATE()),
(2, 6, 250, 30, GETDATE()), (2, 7,  95, 15, GETDATE()), (2, 8, 120, 20, GETDATE()), (2, 9, 110, 20, GETDATE()), (2, 10, 75, 10, GETDATE());
GO

-- ----------------------------------------------------------------------------
-- 10. SEED VOUCHERS (Chương trình khuyến mãi giảm giá)
-- ----------------------------------------------------------------------------
DELETE FROM vouchers;
GO
SET IDENTITY_INSERT vouchers ON;
INSERT INTO vouchers (id, code, discount_type, discount_val, min_order_amount, max_discount, usage_limit, used_count, valid_from, valid_to, is_active, is_deleted, created_at) VALUES
(1, N'CINEMA10',    N'PERCENT',      10.00, 100000.00,  50000.00, 500,  12, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 0, GETDATE()),
(2, N'WELCOME50',   N'FIXED_AMOUNT', 50000.00, 150000.00,  50000.00, 200,  45, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 0, GETDATE()),
(3, N'VIPGOLD20',   N'PERCENT',      20.00, 200000.00, 100000.00, 100,   8, '2024-01-01 00:00:00', '2025-12-31 23:59:59', 1, 0, GETDATE());
SET IDENTITY_INSERT vouchers OFF;
GO

-- Phân phối Voucher cho tài khoản khách hàng mẫu (user_id = 5)
DELETE FROM customer_vouchers;
GO
SET IDENTITY_INSERT customer_vouchers ON;
INSERT INTO customer_vouchers (id, user_id, voucher_id, is_used, used_at, assigned_at) VALUES
(1, 5, 1, 0, NULL, GETDATE()),
(2, 5, 2, 0, NULL, GETDATE());
SET IDENTITY_INSERT customer_vouchers OFF;
GO

-- ----------------------------------------------------------------------------
-- 11. SEED CASH DRAWERS (Mở ca làm việc mẫu cho nhân viên quầy POS)
-- ----------------------------------------------------------------------------
DELETE FROM cash_drawers;
GO
SET IDENTITY_INSERT cash_drawers ON;
INSERT INTO cash_drawers (id, cinema_id, staff_id, opening_time, closing_time, starting_cash, total_cash_sales, ending_cash, difference_amount, status) VALUES
(1, 1, 3, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 08:00:00'), NULL, 1000000.00, 0.00, NULL, 0.00, N'OPEN'),
(2, 2, 4, CONVERT(DATETIME2, CONVERT(VARCHAR(10), GETDATE(), 120) + ' 08:30:00'), NULL, 1000000.00, 0.00, NULL, 0.00, N'OPEN');
SET IDENTITY_INSERT cash_drawers OFF;
GO

-- ----------------------------------------------------------------------------
-- 12. SEED REVIEWS (Đánh giá phim từ khán giả)
-- ----------------------------------------------------------------------------
DELETE FROM reviews;
GO
SET IDENTITY_INSERT reviews ON;
INSERT INTO reviews (id, movie_id, user_id, rating_score, comment, status, is_deleted, created_at) VALUES
(1, 1, 5, 5, N'Phần 2 thực sự là một kiệt tác điện ảnh! Hình ảnh và âm thanh IMAX quá sức choáng ngợp.', N'APPROVED', 0, GETDATE()),
(2, 3, 5, 4, N'Phim hoạt hình vui nhộn, gấu Po vẫn giữ được sự hài hước đặc trưng, rất thích hợp xem cùng gia đình.', N'APPROVED', 0, GETDATE()),
(3, 5, 5, 5, N'Diễn xuất của Phương Anh Đào và Tuấn Trần chạm đến cảm xúc. Rất đáng xem!', N'APPROVED', 0, GETDATE());
SET IDENTITY_INSERT reviews OFF;
GO

-- ----------------------------------------------------------------------------
-- 13. SEED FAVORITE MOVIES (Danh sách phim yêu thích của khách hàng - M-01.3)
-- ----------------------------------------------------------------------------
DELETE FROM favorite_movies;
GO
INSERT INTO favorite_movies (user_id, movie_id, created_at) VALUES
(5, 1, GETDATE()), -- Khách hàng thích Dune 2
(5, 2, GETDATE()), -- Khách hàng thích Godzilla x Kong
(5, 4, GETDATE()); -- Khách hàng thích Deadpool & Wolverine
GO

-- ----------------------------------------------------------------------------
-- 14. SEED SUPPORT TICKETS (Quản lý khiếu nại & Hỗ trợ khách hàng - M-10)
-- ----------------------------------------------------------------------------
DELETE FROM support_tickets;
GO
SET IDENTITY_INSERT support_tickets ON;
INSERT INTO support_tickets (id, ticket_code, user_id, staff_id, category, subject, content, response, status, priority, created_at, updated_at) VALUES
(1, N'TK-2026-0001', 5, 3, N'BOOKING', N'Hỏi về chính sách đổi vé trước giờ chiếu', N'Tôi đã mua vé suất 19:30 tối nay nhưng bận đột xuất, có thể đổi sang suất ngày mai được không?', N'Chào bạn, theo chính sách rạp, vé đã thanh toán được hỗ trợ đổi suất chiếu trước giờ chiếu tối thiểu 120 phút tại quầy vé. Vui lòng mang CCCD và mã vé đến quầy nhân viên sẽ hỗ trợ bạn nhé!', N'RESOLVED', N'MEDIUM', DATEADD(HOUR, -2, GETDATE()), GETDATE()),
(2, N'TK-2026-0002', 5, NULL, N'FNB', N'Góp ý thêm vị bắp phô mai cay', N'Mong rạp có thêm tùy chọn bắp rang lắc phô mai cay cho các bạn trẻ thích ăn cay.', NULL, N'OPEN', N'LOW', GETDATE(), GETDATE());
SET IDENTITY_INSERT support_tickets OFF;
GO

-- ----------------------------------------------------------------------------
-- 15. SEED NOTIFICATIONS (Thông báo In-app hệ thống - M-11)
-- ----------------------------------------------------------------------------
DELETE FROM notifications;
GO
SET IDENTITY_INSERT notifications ON;
INSERT INTO notifications (id, user_id, title, message, type, reference_id, is_read, created_at) VALUES
(1, 5, N'Chào mừng thành viên mới', N'Chào mừng bạn gia nhập hệ thống rạp CineMax! Bạn nhận được 2 voucher chào mừng giảm đến 50.000đ trong ví voucher.', N'SYSTEM', NULL, 1, DATEADD(DAY, -3, GETDATE())),
(2, 5, N'Khuyến mãi đặc biệt cuối tuần', N'Nhập mã CINEMA10 để được giảm ngay 10% khi đặt vé xem phim bom tấn Dune 2 cuối tuần này!', N'PROMOTION', N'CINEMA10', 0, DATEADD(DAY, -1, GETDATE())),
(3, 5, N'Phản hồi yêu cầu hỗ trợ TK-2026-0001', N'Nhân viên chăm sóc khách hàng đã phản hồi thắc mắc của bạn về chính sách đổi vé. Vui lòng kiểm tra mục Khiếu nại & Hỗ trợ.', N'SYSTEM', N'TK-2026-0001', 0, GETDATE());
SET IDENTITY_INSERT notifications OFF;
GO
