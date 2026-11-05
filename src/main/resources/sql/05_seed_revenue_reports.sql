-- ============================================================================
-- CINEMAX - 05_seed_revenue_reports.sql
-- Module: Infrastructure & Operations Analytics - Revenue & BI Reports (TV 1 - DungBD)
-- Phục vụ: Function 1 (Revenue & Sales Reports - M-10.1)
--          Function 2 (Enterprise BI & Comparison Reports - M-10.2)
-- An toàn 100%: Dùng IF NOT EXISTS / idempotent, tra cứu seat_id động
-- ============================================================================

USE cinema_chain_db;
GO

-- ----------------------------------------------------------------------------
-- 1. SEED HISTORICAL SHOWTIMES ACROSS 3 CINEMAS (Cho các mốc: Hiện tại, Tháng trước, Năm trước)
-- ----------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM showtimes WHERE id >= 101 AND id <= 120)
BEGIN
    SET IDENTITY_INSERT showtimes ON;

    -- Rạp 1: Hà Nội (Screening room 1: Standard, Room 2: IMAX)
    -- Suất chiếu 10 ngày trước (Tháng này)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (101, 1, 1, DATEADD(DAY, -10, DATEADD(HOUR, 19, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -10, DATEADD(HOUR, 22, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'2D', N'COMPLETED', 0, DATEADD(DAY, -15, GETDATE()));

    -- Suất chiếu 25 ngày trước (Kỳ trước / Tháng trước)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (102, 2, 1, DATEADD(DAY, -25, DATEADD(HOUR, 20, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -25, DATEADD(HOUR, 22, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'2D', N'COMPLETED', 0, DATEADD(DAY, -30, GETDATE()));

    -- Suất chiếu 40 ngày trước (2 tháng trước)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (103, 3, 2, DATEADD(DAY, -40, DATEADD(HOUR, 18, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -40, DATEADD(HOUR, 21, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'IMAX_3D', N'COMPLETED', 0, DATEADD(DAY, -45, GETDATE()));

    -- Suất chiếu cùng kỳ năm trước (~365 ngày trước)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (104, 1, 1, DATEADD(DAY, -365, DATEADD(HOUR, 19, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -365, DATEADD(HOUR, 22, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'2D', N'COMPLETED', 0, DATEADD(DAY, -370, GETDATE()));

    -- Rạp 2: TP. Hồ Chí Minh (Screening room 3: Standard, Room 4: IMAX)
    -- Suất chiếu 8 ngày trước (Tháng này)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (105, 4, 3, DATEADD(DAY, -8, DATEADD(HOUR, 19, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -8, DATEADD(HOUR, 21, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'2D', N'COMPLETED', 0, DATEADD(DAY, -12, GETDATE()));

    -- Suất chiếu 28 ngày trước (Tháng trước)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (106, 5, 4, DATEADD(DAY, -28, DATEADD(HOUR, 14, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -28, DATEADD(HOUR, 16, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'IMAX_3D', N'COMPLETED', 0, DATEADD(DAY, -35, GETDATE()));

    -- Suất chiếu cùng kỳ năm trước (~365 ngày trước)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (107, 2, 3, DATEADD(DAY, -365, DATEADD(HOUR, 20, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -365, DATEADD(HOUR, 23, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'2D', N'COMPLETED', 0, DATEADD(DAY, -370, GETDATE()));

    -- Rạp 3: Đà Nẵng (Screening room 5: Standard)
    -- Suất chiếu 5 ngày trước (Tháng này)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (108, 6, 5, DATEADD(DAY, -5, DATEADD(HOUR, 18, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -5, DATEADD(HOUR, 21, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'2D', N'COMPLETED', 0, DATEADD(DAY, -10, GETDATE()));

    -- Suất chiếu 32 ngày trước (Tháng trước)
    INSERT INTO showtimes (id, movie_id, screening_room_id, start_time, end_time, experience_format, status, is_deleted, created_at)
    VALUES (109, 1, 5, DATEADD(DAY, -32, DATEADD(HOUR, 15, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), DATEADD(DAY, -32, DATEADD(HOUR, 18, CAST(CAST(GETDATE() AS DATE) AS DATETIME2))), N'2D', N'COMPLETED', 0, DATEADD(DAY, -38, GETDATE()));

    SET IDENTITY_INSERT showtimes OFF;
    PRINT N'[05_seed] Đã nạp danh sách suất chiếu lịch sử thành công!';
END
GO

-- ----------------------------------------------------------------------------
-- 2. SEED SAMPLE BOOKINGS, PAYMENTS, TICKETS & ORDER_ITEMS
-- ----------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM bookings WHERE booking_code = N'BK-REV-2026-001')
BEGIN
    SET NOCOUNT ON;
    DECLARE @bkId BIGINT;

    -- Tra cứu seat_id thực tế từ cơ sở dữ liệu để đảm bảo không vi phạm khóa ngoại
    DECLARE @s1_r1 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 1 AND is_active = 1 ORDER BY id ASC);
    DECLARE @s2_r1 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 1 AND is_active = 1 AND id > @s1_r1 ORDER BY id ASC);
    DECLARE @s3_r1 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 1 AND is_active = 1 AND id > @s2_r1 ORDER BY id ASC);

    DECLARE @s1_r3 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 3 AND is_active = 1 ORDER BY id ASC);
    DECLARE @s2_r3 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 3 AND is_active = 1 AND id > @s1_r3 ORDER BY id ASC);

    DECLARE @s1_r4 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 4 AND is_active = 1 ORDER BY id ASC);
    DECLARE @s2_r4 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 4 AND is_active = 1 AND id > @s1_r4 ORDER BY id ASC);

    DECLARE @s1_r5 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 5 AND is_active = 1 ORDER BY id ASC);
    DECLARE @s2_r5 BIGINT = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 5 AND is_active = 1 AND id > @s1_r5 ORDER BY id ASC);

    -- =========================================================================
    -- ĐƠN 1: Rạp Hà Nội - Tháng này (10 ngày trước) - Kênh ONLINE (VNPAY)
    -- =========================================================================
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-REV-2026-001', 5, NULL, 101, NULL, N'ONLINE', 200000.00, 85000.00, 20000.00, 265000.00, N'CONFIRMED', 0, DATEADD(DAY, -10, GETDATE()), DATEADD(DAY, -10, GETDATE()));
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 265000.00, N'VNP-REV-001', DATEADD(DAY, -10, GETDATE()), N'SUCCESS', DATEADD(DAY, -10, GETDATE()));

    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES
    (@bkId, 101, @s1_r1, N'TICK-REV-001-A01', 100000.00, N'USED', 0, DATEADD(DAY, -10, GETDATE())),
    (@bkId, 101, @s2_r1, N'TICK-REV-001-A02', 100000.00, N'USED', 0, DATEADD(DAY, -10, GETDATE()));

    INSERT INTO order_items (booking_id, fnb_item_id, item_name, quantity, unit_price, subtotal, created_at) VALUES
    (@bkId, 1, N'Bắp Rang Bơ Vị Phô Mai Lớn', 1, 55000.00, 55000.00, DATEADD(DAY, -10, GETDATE())),
    (@bkId, 2, N'Nước ngọt Coca-Cola 32oz', 1, 30000.00, 30000.00, DATEADD(DAY, -10, GETDATE()));

    -- =========================================================================
    -- ĐƠN 2: Rạp Hà Nội - Tháng này (10 ngày trước) - Kênh POS (CASH)
    -- =========================================================================
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-REV-2026-002', NULL, 3, 101, NULL, N'POS', 115000.00, 0.00, 0.00, 115000.00, N'CONFIRMED', 0, DATEADD(DAY, -10, GETDATE()), DATEADD(DAY, -10, GETDATE()));
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'CASH', 115000.00, N'POS-REV-002', DATEADD(DAY, -10, GETDATE()), N'SUCCESS', DATEADD(DAY, -10, GETDATE()));

    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES
    (@bkId, 101, @s3_r1, N'TICK-REV-002-A03', 115000.00, N'USED', 0, DATEADD(DAY, -10, GETDATE()));

    -- =========================================================================
    -- ĐƠN 3: Rạp Hà Nội - Tháng trước (25 ngày trước) - Kênh ONLINE (VNPAY)
    -- =========================================================================
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-REV-2026-003', 5, NULL, 102, NULL, N'ONLINE', 230000.00, 65000.00, 0.00, 295000.00, N'CONFIRMED', 0, DATEADD(DAY, -25, GETDATE()), DATEADD(DAY, -25, GETDATE()));
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 295000.00, N'VNP-REV-003', DATEADD(DAY, -25, GETDATE()), N'SUCCESS', DATEADD(DAY, -25, GETDATE()));

    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES
    (@bkId, 102, @s1_r1, N'TICK-REV-003-A01', 115000.00, N'USED', 0, DATEADD(DAY, -25, GETDATE())),
    (@bkId, 102, @s2_r1, N'TICK-REV-003-A02', 115000.00, N'USED', 0, DATEADD(DAY, -25, GETDATE()));

    -- =========================================================================
    -- ĐƠN 4: Rạp Hà Nội - Năm trước (~365 ngày trước) - Kênh ONLINE (VNPAY)
    -- =========================================================================
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-REV-2026-004', 5, NULL, 104, NULL, N'ONLINE', 180000.00, 50000.00, 0.00, 230000.00, N'CONFIRMED', 0, DATEADD(DAY, -365, GETDATE()), DATEADD(DAY, -365, GETDATE()));
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 230000.00, N'VNP-REV-004', DATEADD(DAY, -365, GETDATE()), N'SUCCESS', DATEADD(DAY, -365, GETDATE()));

    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES
    (@bkId, 104, @s1_r1, N'TICK-REV-004-A01', 90000.00, N'USED', 0, DATEADD(DAY, -365, GETDATE())),
    (@bkId, 104, @s2_r1, N'TICK-REV-004-A02', 90000.00, N'USED', 0, DATEADD(DAY, -365, GETDATE()));

    -- =========================================================================
    -- ĐƠN 5: Rạp Sài Gòn (Landmark 81) - Tháng này (8 ngày trước) - Kênh ONLINE (POINTS)
    -- =========================================================================
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-REV-2026-005', 5, NULL, 105, NULL, N'ONLINE', 300000.00, 110000.00, 30000.00, 380000.00, N'CONFIRMED', 0, DATEADD(DAY, -8, GETDATE()), DATEADD(DAY, -8, GETDATE()));
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'POINTS', 380000.00, N'PTS-REV-005', DATEADD(DAY, -8, GETDATE()), N'SUCCESS', DATEADD(DAY, -8, GETDATE()));

    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES
    (@bkId, 105, @s1_r3, N'TICK-REV-005-A01', 150000.00, N'USED', 0, DATEADD(DAY, -8, GETDATE())),
    (@bkId, 105, @s2_r3, N'TICK-REV-005-A02', 150000.00, N'USED', 0, DATEADD(DAY, -8, GETDATE()));

    -- =========================================================================
    -- ĐƠN 6: Rạp Sài Gòn (Landmark 81) - Tháng trước (28 ngày trước) - Kênh POS (CASH)
    -- =========================================================================
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-REV-2026-006', NULL, 4, 106, NULL, N'POS', 320000.00, 80000.00, 0.00, 400000.00, N'CONFIRMED', 0, DATEADD(DAY, -28, GETDATE()), DATEADD(DAY, -28, GETDATE()));
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'CASH', 400000.00, N'POS-REV-006', DATEADD(DAY, -28, GETDATE()), N'SUCCESS', DATEADD(DAY, -28, GETDATE()));

    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES
    (@bkId, 106, @s1_r4, N'TICK-REV-006-A01', 160000.00, N'USED', 0, DATEADD(DAY, -28, GETDATE())),
    (@bkId, 106, @s2_r4, N'TICK-REV-006-A02', 160000.00, N'USED', 0, DATEADD(DAY, -28, GETDATE()));

    -- =========================================================================
    -- ĐƠN 7: Rạp Đà Nẵng (Vincom Ngô Quyền) - Tháng này (5 ngày trước) - Kênh ONLINE (VNPAY)
    -- =========================================================================
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-REV-2026-007', 5, NULL, 108, NULL, N'ONLINE', 160000.00, 45000.00, 0.00, 205000.00, N'CONFIRMED', 0, DATEADD(DAY, -5, GETDATE()), DATEADD(DAY, -5, GETDATE()));
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 205000.00, N'VNP-REV-007', DATEADD(DAY, -5, GETDATE()), N'SUCCESS', DATEADD(DAY, -5, GETDATE()));

    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES
    (@bkId, 108, @s1_r5, N'TICK-REV-007-A01', 80000.00, N'USED', 0, DATEADD(DAY, -5, GETDATE())),
    (@bkId, 108, @s2_r5, N'TICK-REV-007-A02', 80000.00, N'USED', 0, DATEADD(DAY, -5, GETDATE()));

    -- =========================================================================
    -- ĐƠN 8: Đơn HỦY (CANCELLED / FAILED) để kiểm thử bộ lọc loại trừ chính xác
    -- =========================================================================
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-REV-2026-008-CANCELLED', 5, NULL, 101, NULL, N'ONLINE', 100000.00, 0.00, 0.00, 100000.00, N'CANCELLED', 0, DATEADD(DAY, -2, GETDATE()), DATEADD(DAY, -2, GETDATE()));
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 100000.00, N'VNP-REV-008-FAIL', DATEADD(DAY, -2, GETDATE()), N'FAILED', DATEADD(DAY, -2, GETDATE()));

    PRINT N'[05_seed] Đã nạp thành công các đơn hàng, thanh toán và vé mẫu đa kỳ!';
END
GO
