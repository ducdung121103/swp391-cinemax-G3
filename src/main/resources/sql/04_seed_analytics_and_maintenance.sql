-- ============================================================================
-- CINEMAX - 04_seed_analytics_and_maintenance.sql
-- Module: Infrastructure & Operations Analytics Seed Data (TV 1 - Dũng)
-- Phục vụ: Function 5 (Seat Maintenance Toggle), Function 6 (Admin Dashboard),
--          Function 7 (Branch Occupancy Report - SRS: M-10.3)
-- An toàn 100%: Dùng IF NOT EXISTS / idempotent, không xung đột với các module khác
-- ============================================================================

USE cinema_chain_db;
GO

-- ----------------------------------------------------------------------------
-- 1. SEED MAINTENANCE SCHEDULES (Lịch bảo trì phòng chiếu - Bảng maintenance_schedules)
-- ----------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM maintenance_schedules WHERE reason LIKE N'%Laser Barco 4K%')
BEGIN
    INSERT INTO maintenance_schedules (screening_room_id, start_time, end_time, reason, created_by, is_deleted, created_at, updated_at) VALUES
    (1, DATEADD(DAY, -1, GETDATE()), DATEADD(DAY, 1, GETDATE()), N'Thay bóng đèn máy chiếu Laser Barco 4K & kiểm tra kính lọc', 1, 0, GETDATE(), GETDATE()),
    (2, DATEADD(DAY, 2, GETDATE()), DATEADD(DAY, 3, GETDATE()), N'Bảo trì định kỳ hệ thống âm thanh vòm Dolby Atmos', 1, 0, GETDATE(), GETDATE()),
    (3, DATEADD(HOUR, -6, GETDATE()), DATEADD(HOUR, 18, GETDATE()), N'Sửa chữa motor ngả lưng hàng ghế VIP D04, D05', 2, 0, GETDATE(), GETDATE()),
    (5, DATEADD(DAY, -2, GETDATE()), DATEADD(DAY, -1, GETDATE()), N'Vệ sinh và căng lại màn chiếu cong tráng bạc', 2, 0, GETDATE(), GETDATE());

    -- Cập nhật 4 ghế sang trạng thái bảo trì thực tế
    UPDATE seats SET status = N'MAINTENANCE', is_active = 0, updated_at = GETDATE()
    WHERE screening_room_id = 1 AND seat_code IN (N'D04', N'D05');

    UPDATE seats SET status = N'MAINTENANCE', is_active = 0, updated_at = GETDATE()
    WHERE screening_room_id = 3 AND seat_code IN (N'D04', N'D05');

    PRINT N'[04_seed] Đã nạp 4 lịch bảo trì mẫu và cập nhật ghế bảo trì thành công!';
END
GO

-- ----------------------------------------------------------------------------
-- 2. SEED SAMPLE BOOKINGS, TICKETS & PAYMENTS (Phục vụ Dashboard & Báo cáo Lấp đầy)
-- ----------------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM bookings WHERE booking_code = N'BK-20261018-001')
BEGIN
    SET NOCOUNT ON;

    -- Biến tạm
    DECLARE @bkId BIGINT;
    DECLARE @stId INT;
    DECLARE @roomId INT;
    DECLARE @seatId INT;

    -- ------------------------------------------------------------------------
    -- ĐƠN 1: Showtime 1 (HN Room 1 - Dune 2 Sáng) - 4 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 1;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-001', 5, NULL, @stId, NULL, N'ONLINE', 340000.00, 79000.00, 0.00, 419000.00, N'CONFIRMED', 0, DATEADD(HOUR, -5, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 419000.00, N'VNPAY20261018001', DATEADD(HOUR, -5, GETDATE()), N'SUCCESS', GETDATE());

    -- 4 Tickets
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'A01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-001', 85000.00, N'USED', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'A02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-002', 85000.00, N'USED', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'A03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-003', 85000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'A04';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-004', 85000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 2: Showtime 4 (HN Room 1 - Oppenheimer Tối Giờ Vàng) - 5 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 4;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-002', 5, NULL, @stId, NULL, N'ONLINE', 475000.00, 119000.00, 0.00, 594000.00, N'CONFIRMED', 0, DATEADD(HOUR, -4, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 594000.00, N'VNPAY20261018002', DATEADD(HOUR, -4, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-005', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-006', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-007', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-008', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-009', 95000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 3: Showtime 4 (HN Room 1 - Oppenheimer Tối Giờ Vàng) - 3 Vé POS CASH
    -- ------------------------------------------------------------------------
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-003', NULL, 3, @stId, NULL, N'POS', 285000.00, 0.00, 0.00, 285000.00, N'CONFIRMED', 0, DATEADD(HOUR, -3, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'CASH', 285000.00, N'POS-CASH-001', DATEADD(HOUR, -3, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'F01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-010', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'F02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-011', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'F03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-012', 95000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 4: Showtime 7 (HN Room 2 IMAX 3D - Deadpool) - 4 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 7;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-004', 5, NULL, @stId, NULL, N'ONLINE', 540000.00, 0.00, 0.00, 540000.00, N'CONFIRMED', 0, DATEADD(HOUR, -3, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 540000.00, N'VNPAY20261018004', DATEADD(HOUR, -3, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D05';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-013', 135000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D06';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-014', 135000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D07';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-015', 135000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D08';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-016', 135000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 5: Showtime 8 (HN Room 2 IMAX 3D - Spider-Man Giờ Vàng) - 5 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 8;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-005', 5, NULL, @stId, NULL, N'ONLINE', 750000.00, 189000.00, 0.00, 939000.00, N'CONFIRMED', 0, DATEADD(HOUR, -2, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 939000.00, N'VNPAY20261018005', DATEADD(HOUR, -2, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E04';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-017', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E05';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-018', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E06';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-019', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E07';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-020', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E08';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-021', 150000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 6: Showtime 9 (SG Room 3 - Kung Fu Panda) - 4 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 9;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-006', 5, NULL, @stId, NULL, N'ONLINE', 340000.00, 0.00, 0.00, 340000.00, N'CONFIRMED', 0, DATEADD(HOUR, -6, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 340000.00, N'VNPAY20261018006', DATEADD(HOUR, -6, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'B01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-022', 85000.00, N'USED', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'B02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-023', 85000.00, N'USED', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'B03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-024', 85000.00, N'USED', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'B04';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-025', 85000.00, N'USED', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 7: Showtime 10 (SG Room 3 - Mai) - 4 Vé POS CASH
    -- ------------------------------------------------------------------------
    SET @stId = 10;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-007', NULL, 4, @stId, NULL, N'POS', 340000.00, 70000.00, 0.00, 410000.00, N'CONFIRMED', 0, DATEADD(HOUR, -4, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'CASH', 410000.00, N'POS-CASH-002', DATEADD(HOUR, -4, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'C01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-026', 85000.00, N'USED', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'C02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-027', 85000.00, N'USED', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'C03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-028', 85000.00, N'USED', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'C04';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-029', 85000.00, N'USED', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 8: Showtime 12 (SG Room 3 - Lật Mặt 7 Giờ Vàng) - 6 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 12;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-008', 5, NULL, @stId, NULL, N'ONLINE', 570000.00, 119000.00, 0.00, 689000.00, N'CONFIRMED', 0, DATEADD(HOUR, -2, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 689000.00, N'VNPAY20261018008', DATEADD(HOUR, -2, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-030', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-031', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-032', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-033', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-034', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-035', 95000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 9: Showtime 12 (SG Room 3 - Lật Mặt 7 Giờ Vàng) - 3 Vé POS CASH
    -- ------------------------------------------------------------------------
    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-009', NULL, 4, @stId, NULL, N'POS', 285000.00, 35000.00, 0.00, 320000.00, N'CONFIRMED', 0, DATEADD(HOUR, -1, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'CASH', 320000.00, N'POS-CASH-003', DATEADD(HOUR, -1, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'F04';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-036', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'F05';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-037', 95000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'F06';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-038', 95000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 10: Showtime 14 (SG Room 4 IMAX 3D - Oppenheimer) - 4 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 14;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-010', 5, NULL, @stId, NULL, N'ONLINE', 540000.00, 0.00, 0.00, 540000.00, N'CONFIRMED', 0, DATEADD(HOUR, -3, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 540000.00, N'VNPAY20261018010', DATEADD(HOUR, -3, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-039', 135000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-040', 135000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-041', 135000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D04';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-042', 135000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 11: Showtime 15 (SG Room 4 IMAX 3D - Dune 2 Giờ Vàng) - 5 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 15;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-011', 5, NULL, @stId, NULL, N'ONLINE', 750000.00, 119000.00, 0.00, 869000.00, N'CONFIRMED', 0, DATEADD(MINUTE, -90, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 869000.00, N'VNPAY20261018011', DATEADD(MINUTE, -90, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-043', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-044', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-045', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E04';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-046', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'E05';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-047', 150000.00, N'VALID', 0, GETDATE());

    -- ------------------------------------------------------------------------
    -- ĐƠN 12: Showtime 18 (Đà Nẵng Room 6 IMAX 3D - Dune 2 Giờ Vàng) - 5 Vé ONLINE VNPAY
    -- ------------------------------------------------------------------------
    SET @stId = 18;
    SELECT @roomId = screening_room_id FROM showtimes WHERE id = @stId;

    INSERT INTO bookings (booking_code, user_id, staff_id, showtime_id, voucher_id, channel, total_tickets_amount, total_fnb_amount, discount_amount, final_amount, status, is_deleted, created_at, updated_at)
    VALUES (N'BK-20261018-012', 5, NULL, @stId, NULL, N'ONLINE', 750000.00, 0.00, 0.00, 750000.00, N'CONFIRMED', 0, DATEADD(MINUTE, -45, GETDATE()), GETDATE());
    SET @bkId = SCOPE_IDENTITY();

    INSERT INTO payments (booking_id, payment_method, amount, transaction_no, payment_time, status, created_at)
    VALUES (@bkId, N'VNPAY', 750000.00, N'VNPAY20261018012', DATEADD(MINUTE, -45, GETDATE()), N'SUCCESS', GETDATE());

    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D01';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-048', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D02';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-049', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D03';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-050', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D04';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-051', 150000.00, N'VALID', 0, GETDATE());
    SELECT @seatId = id FROM seats WHERE screening_room_id = @roomId AND seat_code = 'D05';
    INSERT INTO tickets (booking_id, showtime_id, seat_id, barcode, ticket_price, status, is_deleted, created_at) VALUES (@bkId, @stId, @seatId, N'TK-20261018-052', 150000.00, N'VALID', 0, GETDATE());

    PRINT N'[04_seed] Đã nạp thành công 12 bookings, 52 vé tickets và 12 payments mẫu cho cả 3 cụm rạp!';
END
GO
