-- ============================================================================
-- MULTI-BRANCH CINEMA MANAGEMENT SYSTEM (SWP391 - GROUP 3)
-- SCRIPT 01: COMPREHENSIVE DATABASE SCHEMA DEFINITION (31 TABLES - 3NF T-SQL)
-- Conforms 100% with: Feature-Tree-Project.docx (Modules M-01 -> M-12)
-- Engine: Microsoft SQL Server 2019+ | Collation: Vietnamese_CI_AS
-- ============================================================================

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'cinema_chain_db')
BEGIN
    CREATE DATABASE cinema_chain_db COLLATE Vietnamese_CI_AS;
END
GO

USE cinema_chain_db;
GO

-- Xóa sạch khóa ngoại cũ nếu script chạy lại nhiều lần để DROP TABLE không bị xung đột
DECLARE @sqlDropFk NVARCHAR(MAX) = N'';
SELECT @sqlDropFk += N'ALTER TABLE ' + QUOTENAME(OBJECT_SCHEMA_NAME(parent_object_id)) + '.' + QUOTENAME(OBJECT_NAME(parent_object_id)) + 
                     N' DROP CONSTRAINT ' + QUOTENAME(name) + N';' + CHAR(13)
FROM sys.foreign_keys;
IF @sqlDropFk <> N'' EXEC sp_executesql @sqlDropFk;
GO

-- ----------------------------------------------------------------------------
-- ZONE 1: INFRASTRUCTURE & CINEMA MANAGEMENT (TV 1 - 7 TABLES)
-- ----------------------------------------------------------------------------

-- 1. Bảng cinemas (Cụm rạp chiếu phim - M-04.1)
DROP TABLE IF EXISTS cinemas;
CREATE TABLE cinemas (
    id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_code NVARCHAR(20) NOT NULL UNIQUE,
    name NVARCHAR(150) NOT NULL,
    address NVARCHAR(255) NOT NULL,
    city NVARCHAR(100) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    email NVARCHAR(100) NULL,
    total_rooms INT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
CREATE INDEX idx_cinemas_city ON cinemas (city);
CREATE INDEX idx_cinemas_active ON cinemas (is_active, is_deleted);
GO

-- 2. Bảng screening_rooms (Phòng chiếu phim - M-04.2)
DROP TABLE IF EXISTS screening_rooms;
CREATE TABLE screening_rooms (
    id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_id INT NOT NULL,
    name NVARCHAR(50) NOT NULL,
    room_type NVARCHAR(30) NOT NULL DEFAULT 'STANDARD_2D', -- STANDARD_2D, VIP_2D, IMAX_3D, 4DX
    total_rows INT NOT NULL DEFAULT 10,
    total_columns INT NOT NULL DEFAULT 12,
    total_capacity INT NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, MAINTENANCE, INACTIVE
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_rooms_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE NO ACTION ON UPDATE CASCADE
);
CREATE INDEX idx_rooms_cinema ON screening_rooms (cinema_id);
GO

-- 3. Bảng seat_types (Phân loại ghế ngồi & Phụ thu - M-04.3)
DROP TABLE IF EXISTS seat_types;
CREATE TABLE seat_types (
    id INT IDENTITY(1,1) PRIMARY KEY,
    type_code NVARCHAR(30) NOT NULL UNIQUE, -- STANDARD, VIP, COUPLE
    name NVARCHAR(50) NOT NULL,
    color_hex NVARCHAR(10) NOT NULL DEFAULT '#6c757d',
    surcharge DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    description NVARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 4. Bảng seats (Ghế ngồi trong phòng chiếu - Grid Designer - M-04.3)
DROP TABLE IF EXISTS seats;
CREATE TABLE seats (
    id INT IDENTITY(1,1) PRIMARY KEY,
    screening_room_id INT NOT NULL,
    seat_type_id INT NOT NULL,
    seat_row NVARCHAR(10) NOT NULL,        -- A, B, C... (Hàng ghế)
    seat_number INT NOT NULL,             -- 1, 2, 3... (Số ghế)
    seat_code NVARCHAR(10) NOT NULL,       -- A01, A02, B01... (Mã ghế hiển thị)
    grid_row_index INT NOT NULL DEFAULT 1,-- Tọa độ dòng trên lưới Matrix UI (Grid Designer)
    grid_col_index INT NOT NULL DEFAULT 1,-- Tọa độ cột trên lưới Matrix UI (Grid Designer)
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, MAINTENANCE, BLOCKED
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_seats_room FOREIGN KEY (screening_room_id) REFERENCES screening_rooms (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_seats_type FOREIGN KEY (seat_type_id) REFERENCES seat_types (id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT uk_room_seat_code UNIQUE (screening_room_id, seat_code)
);
CREATE INDEX idx_seats_room ON seats (screening_room_id);
GO

-- 5. Bảng maintenance_schedules (Lịch bảo trì phòng chiếu - M-04.3)
DROP TABLE IF EXISTS maintenance_schedules;
CREATE TABLE maintenance_schedules (
    id INT IDENTITY(1,1) PRIMARY KEY,
    screening_room_id INT NOT NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2 NOT NULL,
    reason NVARCHAR(255) NOT NULL,
    created_by INT NULL,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_maint_room FOREIGN KEY (screening_room_id) REFERENCES screening_rooms (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_maint_time ON maintenance_schedules (screening_room_id, start_time, end_time);
GO

-- 6. Bảng system_settings (Cấu hình tham số toàn cục hệ thống - M-03.1, M-05.3, M-09.2)
DROP TABLE IF EXISTS system_settings;
CREATE TABLE system_settings (
    setting_key NVARCHAR(50) PRIMARY KEY,
    setting_value NVARCHAR(255) NOT NULL,
    description NVARCHAR(255) NULL,
    group_name NVARCHAR(50) NOT NULL DEFAULT 'SYSTEM', -- SYSTEM, BOOKING, LOYALTY, PAYMENT
    updated_by INT NULL,
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO


-- ----------------------------------------------------------------------------
-- ZONE 2: IDENTITY, AUTH, LOYALTY & SUPPORT (TV 2 - 8 TABLES)
-- ----------------------------------------------------------------------------

-- 7. Bảng roles (Vai trò phân quyền hệ thống - M-01.2)
DROP TABLE IF EXISTS roles;
CREATE TABLE roles (
    id INT IDENTITY(1,1) PRIMARY KEY,
    role_name NVARCHAR(50) NOT NULL UNIQUE, -- ADMIN, MANAGER, STAFF, CUSTOMER
    description NVARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 8. Bảng membership_tiers (Hạng thành viên & Chính sách tích điểm - M-09.2)
DROP TABLE IF EXISTS membership_tiers;
CREATE TABLE membership_tiers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    tier_name NVARCHAR(50) NOT NULL UNIQUE, -- STANDARD, SILVER, GOLD, DIAMOND
    min_spent DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_percent INT NOT NULL DEFAULT 0,
    point_rate DECIMAL(4,2) NOT NULL DEFAULT 1.00,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 9. Bảng users (Tài khoản người dùng toàn hệ thống - M-01.1 -> M-01.4)
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    role_id INT NOT NULL,
    cinema_id INT NULL,
    email NVARCHAR(120) NOT NULL UNIQUE,
    password_hash NVARCHAR(255) NOT NULL,
    full_name NVARCHAR(120) NOT NULL,
    phone NVARCHAR(20) NULL UNIQUE,
    loyalty_points INT NOT NULL DEFAULT 0,
    tier_id INT NULL,
    avatar_url NVARCHAR(255) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'ACTIVE', -- ACTIVE, LOCKED, INACTIVE
    is_2fa_enabled BIT NOT NULL DEFAULT 0, -- M-01.2: 2FA qua Email OTP
    email_verified BIT NOT NULL DEFAULT 0, -- M-01.2: Xác thực email
    otp_code NVARCHAR(10) NULL,                    -- Mã OTP xác thực
    otp_expires_at DATETIME2 NULL,                 -- Thời hạn OTP (~5-15p)
    reset_password_token NVARCHAR(100) NULL,       -- Token link reset mật khẩu
    reset_token_expires_at DATETIME2 NULL,         -- Thời hạn token 15 phút
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT fk_users_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_users_tier FOREIGN KEY (tier_id) REFERENCES membership_tiers (id) ON DELETE SET NULL ON UPDATE CASCADE
);
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role_id);
CREATE INDEX idx_users_reset_token ON users (reset_password_token);
GO

-- 10. Bảng point_histories (Lịch sử tích & tiêu điểm thưởng - M-09.2)
DROP TABLE IF EXISTS point_histories;
CREATE TABLE point_histories (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    booking_id BIGINT NULL,
    points_change INT NOT NULL, -- Dương: Tích điểm (+), Âm: Tiêu điểm (-)
    balance_after INT NOT NULL,
    transaction_type NVARCHAR(30) NOT NULL DEFAULT 'EARN', -- EARN, REDEEM, ADJUST, EXPIRE
    reason NVARCHAR(255) NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_points_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_points_user ON point_histories (user_id);
GO

-- 11. Bảng customer_vouchers (Ví voucher cá nhân của khách hàng - M-09.1)
DROP TABLE IF EXISTS customer_vouchers;
CREATE TABLE customer_vouchers (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    voucher_id INT NOT NULL,
    is_used BIT NOT NULL DEFAULT 0,
    used_at DATETIME2 NULL,
    assigned_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_cust_vouch_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_cust_vouch_user ON customer_vouchers (user_id);
GO

-- 12. Bảng favorite_movies (Danh sách phim yêu thích của khách hàng - M-01.3)
DROP TABLE IF EXISTS favorite_movies;
CREATE TABLE favorite_movies (
    user_id INT NOT NULL,
    movie_id INT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    PRIMARY KEY (user_id, movie_id),
    CONSTRAINT fk_fav_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_fav_user ON favorite_movies (user_id);
GO

-- 13. Bảng support_tickets (Quản lý khiếu nại & Hỗ trợ khách hàng - M-10)
DROP TABLE IF EXISTS support_tickets;
CREATE TABLE support_tickets (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ticket_code NVARCHAR(32) NOT NULL UNIQUE,
    user_id INT NOT NULL,
    staff_id INT NULL,
    category NVARCHAR(50) NOT NULL DEFAULT 'BOOKING', -- BOOKING, PAYMENT, FNB, TECHNICAL, OTHER
    subject NVARCHAR(200) NOT NULL,
    content NVARCHAR(MAX) NOT NULL,
    response NVARCHAR(MAX) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, IN_PROGRESS, RESOLVED, CLOSED
    priority NVARCHAR(20) NOT NULL DEFAULT 'MEDIUM', -- LOW, MEDIUM, HIGH, URGENT
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_support_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_support_staff FOREIGN KEY (staff_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX idx_support_code ON support_tickets (ticket_code);
CREATE INDEX idx_support_user ON support_tickets (user_id);
CREATE INDEX idx_support_status ON support_tickets (status);
GO

-- 14. Bảng notifications (Hệ thống thông báo In-app & Lịch sử gửi tin - M-11)
DROP TABLE IF EXISTS notifications;
CREATE TABLE notifications (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    title NVARCHAR(200) NOT NULL,
    message NVARCHAR(MAX) NOT NULL,
    type NVARCHAR(30) NOT NULL DEFAULT 'SYSTEM', -- BOOKING, PAYMENT, SHOWTIME, PROMOTION, SYSTEM
    reference_id NVARCHAR(100) NULL, -- booking_code, movie_id, voucher_code...
    is_read BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_notif_user_read ON notifications (user_id, is_read);
GO


-- ----------------------------------------------------------------------------
-- ZONE 3: MOVIE CATALOG, SCHEDULING & DYNAMIC PRICING (TV 3 - 6 TABLES)
-- ----------------------------------------------------------------------------

-- 15. Bảng genres (Danh mục thể loại phim - M-02.1)
DROP TABLE IF EXISTS genres;
CREATE TABLE genres (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 16. Bảng movies (Kho phim điện ảnh - M-02.1)
DROP TABLE IF EXISTS movies;
CREATE TABLE movies (
    id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(200) NOT NULL,
    original_title NVARCHAR(200) NULL,
    duration_minutes INT NOT NULL,
    release_date DATE NOT NULL,
    end_date DATE NULL,
    age_rating NVARCHAR(10) NOT NULL DEFAULT 'P', -- P (Mọi lứa tuổi), T13, T16, T18, C (Cấm) - M-07.2
    language NVARCHAR(100) NULL,
    director NVARCHAR(150) NULL,
    actors NVARCHAR(MAX) NULL,
    cast_members NVARCHAR(MAX) NULL,
    synopsis NVARCHAR(MAX) NULL,
    poster_url NVARCHAR(255) NULL,
    trailer_url NVARCHAR(255) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'COMING_SOON', -- COMING_SOON, NOW_SHOWING, ENDED
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
CREATE INDEX idx_movies_status ON movies (status, is_deleted);
GO

-- Cập nhật khóa ngoại favorite_movies -> movies
ALTER TABLE favorite_movies
    ADD CONSTRAINT fk_fav_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE ON UPDATE CASCADE;
GO

-- 17. Bảng movie_genres (Quan hệ Nhiều - Nhiều giữa Phim và Thể loại - M-02.1)
DROP TABLE IF EXISTS movie_genres;
CREATE TABLE movie_genres (
    movie_id INT NOT NULL,
    genre_id INT NOT NULL,
    PRIMARY KEY (movie_id, genre_id),
    CONSTRAINT fk_mg_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_mg_genre FOREIGN KEY (genre_id) REFERENCES genres (id) ON DELETE NO ACTION ON UPDATE CASCADE
);
GO

-- 18. Bảng showtimes (Lịch chiếu phim - M-03.1: Chống trùng lịch + Đệm 15p dọn phòng)
DROP TABLE IF EXISTS showtimes;
CREATE TABLE showtimes (
    id INT IDENTITY(1,1) PRIMARY KEY,
    movie_id INT NOT NULL,
    screening_room_id INT NOT NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2 NOT NULL, -- Tự động = start_time + duration_minutes + 15 phút cleaning buffer
    experience_format NVARCHAR(20) NOT NULL DEFAULT '2D', -- 2D, 3D, IMAX
    status NVARCHAR(30) NOT NULL DEFAULT 'SCHEDULED', -- SCHEDULED, OPENING, FINISHED, CANCELLED
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_showtimes_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT fk_showtimes_room FOREIGN KEY (screening_room_id) REFERENCES screening_rooms (id) ON DELETE NO ACTION ON UPDATE CASCADE
);
CREATE INDEX idx_showtimes_movie_time ON showtimes (movie_id, start_time);
CREATE INDEX idx_showtimes_room_time ON showtimes (screening_room_id, start_time, end_time);
GO

-- 19. Bảng ticket_pricings (Ma trận cấu hình giá vé động - M-03.2)
DROP TABLE IF EXISTS ticket_pricings;
CREATE TABLE ticket_pricings (
    id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_id INT NULL, -- NULL: áp dụng toàn hệ thống; NOT NULL: giá đặc thù rạp
    day_type NVARCHAR(20) NOT NULL DEFAULT 'WEEKDAY', -- WEEKDAY, WEEKEND, HOLIDAY
    time_slot NVARCHAR(20) NOT NULL DEFAULT 'STANDARD', -- EARLY (Sáng), STANDARD, PRIME (Tối), SNEAK_SHOW
    experience_format NVARCHAR(20) NOT NULL DEFAULT '2D', -- 2D, 3D, IMAX
    seat_type_id INT NOT NULL DEFAULT 1,
    base_price DECIMAL(10,2) NOT NULL DEFAULT 80000.00,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_pricing_seat_type FOREIGN KEY (seat_type_id) REFERENCES seat_types (id) ON DELETE NO ACTION ON UPDATE CASCADE,
    CONSTRAINT fk_pricing_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_pricing_lookup ON ticket_pricings (cinema_id, day_type, time_slot, experience_format);
GO

-- 20. Bảng reviews (Đánh giá & Chấm điểm phim - M-02.3: Lọc từ ngữ thô tục & duyệt comment)
DROP TABLE IF EXISTS reviews;
CREATE TABLE reviews (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    movie_id INT NOT NULL,
    user_id INT NOT NULL,
    rating_score INT NOT NULL DEFAULT 5, -- 1 đến 5 sao
    comment NVARCHAR(MAX) NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'APPROVED', -- APPROVED, PENDING, HIDDEN, FLAGGED
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_reviews_movie FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reviews_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_reviews_movie ON reviews (movie_id, status);
GO


-- ----------------------------------------------------------------------------
-- ZONE 4: CORE BOOKING, PAYMENTS & SEAT LOCKING ENGINE (TV 4 - 6 TABLES)
-- ----------------------------------------------------------------------------

-- 21. Bảng vouchers (Mã giảm giá & Chiến dịch khuyến mãi - M-09.1)
DROP TABLE IF EXISTS vouchers;
CREATE TABLE vouchers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    code NVARCHAR(30) NOT NULL UNIQUE,
    discount_type NVARCHAR(20) NOT NULL DEFAULT 'PERCENT', -- PERCENT, FIXED_AMOUNT
    discount_val DECIMAL(10,2) NOT NULL,
    min_order_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    max_discount DECIMAL(10,2) NULL,
    usage_limit INT NOT NULL DEFAULT 100,
    used_count INT NOT NULL DEFAULT 0,
    valid_from DATETIME2 NOT NULL,
    valid_to DATETIME2 NOT NULL,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
CREATE INDEX idx_vouchers_code ON vouchers (code);
GO

-- Cập nhật khóa ngoại customer_vouchers -> vouchers
ALTER TABLE customer_vouchers
    ADD CONSTRAINT fk_cust_vouch_vouch FOREIGN KEY (voucher_id) REFERENCES vouchers (id) ON DELETE CASCADE ON UPDATE CASCADE;
GO

-- 22. Bảng seat_holdings (Khóa ghế tạm 5 phút thời gian thực - M-05.3: Pessimistic Lock)
DROP TABLE IF EXISTS seat_holdings;
CREATE TABLE seat_holdings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    showtime_id INT NOT NULL,
    seat_id INT NOT NULL,
    session_id NVARCHAR(100) NOT NULL,
    held_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    expires_at DATETIME2 NOT NULL, -- Mặc định held_at + 300 giây (từ system_settings)
    CONSTRAINT fk_holding_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_holding_seat FOREIGN KEY (seat_id) REFERENCES seats (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT uk_showtime_seat_holding UNIQUE (showtime_id, seat_id)
);
CREATE INDEX idx_holding_expiry ON seat_holdings (expires_at);
GO

-- 23. Bảng bookings (Đơn đặt vé & bắp nước - M-05.1 & M-05.4)
DROP TABLE IF EXISTS bookings;
CREATE TABLE bookings (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_code NVARCHAR(32) NOT NULL UNIQUE,
    user_id INT NULL,
    staff_id INT NULL, -- Ghi nhận nhân viên bán nếu mua tại quầy POS (M-05.2)
    showtime_id INT NOT NULL,
    voucher_id INT NULL,
    channel NVARCHAR(20) NOT NULL DEFAULT 'ONLINE', -- ONLINE, POS
    total_tickets_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    total_fnb_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    discount_amount DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    final_amount DECIMAL(12,2) NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, CONFIRMED, CANCELLED, EXPIRED
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL ON UPDATE NO ACTION,
    CONSTRAINT fk_bookings_staff FOREIGN KEY (staff_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_bookings_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_bookings_voucher FOREIGN KEY (voucher_id) REFERENCES vouchers (id) ON DELETE SET NULL ON UPDATE NO ACTION
);
CREATE INDEX idx_bookings_code ON bookings (booking_code);
CREATE INDEX idx_bookings_user ON bookings (user_id);
CREATE INDEX idx_bookings_showtime ON bookings (showtime_id);
GO

-- Cập nhật khóa ngoại point_histories -> bookings
ALTER TABLE point_histories
    ADD CONSTRAINT fk_points_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE SET NULL ON UPDATE NO ACTION;
GO

-- 24. Bảng tickets (Vé điện tử & Vé giấy nhiệt - M-07.1: Duy nhất sinh vé & Ký số QR HMAC-SHA256)
DROP TABLE IF EXISTS tickets;
CREATE TABLE tickets (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    showtime_id INT NOT NULL,
    seat_id INT NOT NULL,
    barcode NVARCHAR(64) NOT NULL UNIQUE,
    qr_signature NVARCHAR(255) NULL, -- M-07.1: Chuỗi token chữ ký số HMAC-SHA256
    ticket_price DECIMAL(10,2) NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'VALID', -- VALID, USED, REFUNDED, CANCELLED
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_tickets_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_tickets_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT fk_tickets_seat FOREIGN KEY (seat_id) REFERENCES seats (id) ON DELETE NO ACTION ON UPDATE NO ACTION,
    CONSTRAINT uk_showtime_seat UNIQUE (showtime_id, seat_id) -- CHỐT CHẶN CHỐNG BÁN TRÙNG GHẾ TUYỆT ĐỐI
);
CREATE INDEX idx_tickets_barcode ON tickets (barcode);
GO

-- 25. Bảng order_items (Chi tiết bắp nước & Combo kèm đơn - M-08.2)
DROP TABLE IF EXISTS order_items;
CREATE TABLE order_items (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    fnb_item_id INT NOT NULL,
    item_name NVARCHAR(150) NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_orderitems_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_orderitems_booking ON order_items (booking_id);
GO

-- 26. Bảng payments (Lịch sử thanh toán & Giao dịch - M-06.1 -> M-06.3: VNPay, Tiền mặt)
DROP TABLE IF EXISTS payments;
CREATE TABLE payments (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    payment_method NVARCHAR(30) NOT NULL DEFAULT 'VNPAY', -- VNPAY, CASH, POINTS
    amount DECIMAL(12,2) NOT NULL,
    transaction_no NVARCHAR(100) NULL, -- Mã giao dịch VNPay
    payment_time DATETIME2 NOT NULL DEFAULT GETDATE(),
    status NVARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, FAILED, REFUNDED
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_payments_booking FOREIGN KEY (booking_id) REFERENCES bookings (id) ON DELETE CASCADE ON UPDATE CASCADE
);
CREATE INDEX idx_payments_booking ON payments (booking_id);
CREATE INDEX idx_payments_txn ON payments (transaction_no);
GO


-- ----------------------------------------------------------------------------
-- ZONE 5: OPERATIONS, POS COUNTER, F&B & GATE CHECK-IN (TV 5 - 5 TABLES)
-- ----------------------------------------------------------------------------

-- 27. Bảng fnb_categories (Danh mục phân loại Bắp & Nước - M-08.1)
DROP TABLE IF EXISTS fnb_categories;
CREATE TABLE fnb_categories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255) NULL,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE()
);
GO

-- 28. Bảng fnb_items (Danh mục sản phẩm & Combo Bắp Nước - M-08.1)
DROP TABLE IF EXISTS fnb_items;
CREATE TABLE fnb_items (
    id INT IDENTITY(1,1) PRIMARY KEY,
    category_id INT NOT NULL,
    item_code NVARCHAR(30) NOT NULL UNIQUE,
    name NVARCHAR(150) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    image_url NVARCHAR(255) NULL,
    is_combo BIT NOT NULL DEFAULT 0,
    is_active BIT NOT NULL DEFAULT 1,
    is_deleted BIT NOT NULL DEFAULT 0,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_fnb_category FOREIGN KEY (category_id) REFERENCES fnb_categories (id) ON DELETE NO ACTION ON UPDATE CASCADE
);
CREATE INDEX idx_fnb_code ON fnb_items (item_code);
GO

-- Cập nhật khóa ngoại order_items -> fnb_items
ALTER TABLE order_items
    ADD CONSTRAINT fk_orderitems_fnb FOREIGN KEY (fnb_item_id) REFERENCES fnb_items (id) ON DELETE NO ACTION ON UPDATE CASCADE;
GO

-- 29. Bảng cinema_inventories (Quản lý tồn kho F&B theo từng rạp - M-08.1)
DROP TABLE IF EXISTS cinema_inventories;
CREATE TABLE cinema_inventories (
    id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_id INT NOT NULL,
    fnb_item_id INT NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    warning_threshold INT NOT NULL DEFAULT 10,
    updated_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_inv_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_inv_fnb FOREIGN KEY (fnb_item_id) REFERENCES fnb_items (id) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT uk_cinema_fnb UNIQUE (cinema_id, fnb_item_id)
);
GO

-- 30. Bảng ticket_checkin_logs (Nhật ký soát vé QR Gate Check-in - M-07.2: Chống vào lại & Kiểm tra tuổi)
DROP TABLE IF EXISTS ticket_checkin_logs;
CREATE TABLE ticket_checkin_logs (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    ticket_id BIGINT NOT NULL,
    staff_id INT NULL,
    checkin_time DATETIME2 NOT NULL DEFAULT GETDATE(),
    status NVARCHAR(30) NOT NULL DEFAULT 'SUCCESS', -- SUCCESS, REJECTED_ALREADY_USED, WRONG_SHOWTIME, REJECTED_UNDERAGE
    note NVARCHAR(255) NULL,
    CONSTRAINT fk_checkin_ticket FOREIGN KEY (ticket_id) REFERENCES tickets (id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_checkin_staff FOREIGN KEY (staff_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX idx_checkin_ticket ON ticket_checkin_logs (ticket_id);
GO

-- 31. Bảng cash_drawers (Quản lý ca làm việc & Két tiền mặt POS Quầy - M-05.2 & M-06.2)
DROP TABLE IF EXISTS cash_drawers;
CREATE TABLE cash_drawers (
    id INT IDENTITY(1,1) PRIMARY KEY,
    cinema_id INT NOT NULL,
    staff_id INT NOT NULL,
    opening_time DATETIME2 NOT NULL DEFAULT GETDATE(),
    closing_time DATETIME2 NULL,
    starting_cash DECIMAL(12,2) NOT NULL DEFAULT 1000000.00,
    total_cash_sales DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    ending_cash DECIMAL(12,2) NULL,
    difference_amount DECIMAL(12,2) NULL DEFAULT 0.00,
    status NVARCHAR(30) NOT NULL DEFAULT 'OPEN', -- OPEN, CLOSED
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT fk_drawer_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas (id) ON DELETE CASCADE ON UPDATE NO ACTION,
    CONSTRAINT fk_drawer_staff FOREIGN KEY (staff_id) REFERENCES users (id) ON DELETE NO ACTION ON UPDATE NO ACTION
);
CREATE INDEX idx_drawer_cinema_status ON cash_drawers (cinema_id, status);
GO

-- ============================================================================
-- END OF SCRIPT 01: 31 TABLES CREATED SUCCESSFULLY WITH ZERO INTEGRITY DEFECT
-- ============================================================================
