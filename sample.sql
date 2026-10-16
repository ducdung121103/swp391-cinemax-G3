USE MBCMS;
GO
SET NOCOUNT ON;

-- ================================================================
-- DATA CŨ ĐÃ CÓ SẴN TRONG SCHEMA (không insert lại):
--   roles        : 5 rows  (role_id 1=GUEST,2=CUSTOMER,3=CINEMA_STAFF,
--                            4=BRANCH_MANAGER,5=ADMIN)
--   membership_tiers: 5 rows (tier_id 1=MEMBER,2=BRONZE,3=SILVER,
--                              4=GOLD,5=DIAMOND)
--   loyalty_configs : 1 row  (config_id=1: 10.000đ=1pt, min 100pt)
--   seat_type_surcharges: 0 row (chưa có branch nào)
-- ================================================================

-- ================================================================
-- SECTION 1: USERS
-- Dùng subquery lấy role_id / tier_id theo tên để tránh hardcode
-- ================================================================
INSERT INTO users
    (role_id, username, email, password, fullName, phone,
     status, points, total_accumulated_points, tier_id)
SELECT r.role_id,
       v.username, v.email, v.password, v.fullName, v.phone,
       v.status,   v.points, v.total_pts, t.tier_id
FROM (VALUES
    -- role_name          tier_name  username          email                       password           fullName               phone        status   points  total_pts
    ('ADMIN',           'MEMBER',  'admin01',        'admin01@mbcms.vn',         '$2a$10$h001', N'Nguyễn Văn Admin',   '0901000001','ACTIVE',  0,     0),
    ('BRANCH_MANAGER',  'MEMBER',  'manager_q1',     'manager.q1@mbcms.vn',      '$2a$10$h002', N'Trần Thị Thu Hà',    '0901000002','ACTIVE',  0,     0),
    ('BRANCH_MANAGER',  'MEMBER',  'manager_q7',     'manager.q7@mbcms.vn',      '$2a$10$h003', N'Lê Văn Minh',        '0901000003','ACTIVE',  0,     0),
    ('CINEMA_STAFF',    'MEMBER',  'staff_q1_01',    'staff.q1.01@mbcms.vn',     '$2a$10$h004', N'Phạm Thị Lan',       '0901000004','ACTIVE',  0,     0),
    ('CINEMA_STAFF',    'MEMBER',  'staff_q1_02',    'staff.q1.02@mbcms.vn',     '$2a$10$h005', N'Nguyễn Văn Hùng',    '0901000005','ACTIVE',  0,     0),
    ('CINEMA_STAFF',    'MEMBER',  'staff_q7_01',    'staff.q7.01@mbcms.vn',     '$2a$10$h006', N'Hoàng Thị Mai',      '0901000006','ACTIVE',  0,     0),
    ('CUSTOMER',        'MEMBER',  'khachhang01',    'khachhang01@gmail.com',     '$2a$10$h007', N'Nguyễn Thị Hoa',     '0912000001','ACTIVE',  50,    50),
    ('CUSTOMER',        'BRONZE',  'khachhang02',    'khachhang02@gmail.com',     '$2a$10$h008', N'Trần Văn Nam',       '0912000002','ACTIVE',  120,   150),
    ('CUSTOMER',        'SILVER',  'khachhang03',    'khachhang03@gmail.com',     '$2a$10$h009', N'Lê Thị Bình',       '0912000003','ACTIVE',  250,   280),
    ('CUSTOMER',        'GOLD',    'khachhang04',    'khachhang04@gmail.com',     '$2a$10$h010', N'Phạm Văn Đức',      '0912000004','ACTIVE',  350,   420)
) AS v(role_name, tier_name, username, email, password, fullName, phone,
       status, points, total_pts)
INNER JOIN roles            r ON r.role_name = v.role_name
INNER JOIN membership_tiers t ON t.tier_name = v.tier_name;
GO

-- ================================================================
-- SECTION 2: CINEMA BRANCHES
-- branch_id 1 = Q1, branch_id 2 = Q7  (IDENTITY tự tăng)
-- ================================================================
INSERT INTO cinema_branches
    (branch_name, address, phone, email, manager_id, is_active)
SELECT
    v.branch_name, v.address, v.phone, v.email,
    u.user_id,     1
FROM (VALUES
    (N'MB Cinema Quận 1',
     N'123 Nguyễn Huệ, P.Bến Nghé, Q.1, TP.HCM',
     '02838111001','q1@mbcinema.vn','manager_q1'),
    (N'MB Cinema Quận 7',
     N'456 Nguyễn Thị Thập, P.Tân Phú, Q.7, TP.HCM',
     '02838111002','q7@mbcinema.vn','manager_q7')
) AS v(branch_name, address, phone, email, manager_username)
INNER JOIN users u ON u.username = v.manager_username;
GO

-- Gán branch_id cho nhân viên
UPDATE users
SET branch_id = (SELECT branch_id FROM cinema_branches WHERE branch_name = N'MB Cinema Quận 1')
WHERE username IN ('manager_q1','staff_q1_01','staff_q1_02');

UPDATE users
SET branch_id = (SELECT branch_id FROM cinema_branches WHERE branch_name = N'MB Cinema Quận 7')
WHERE username IN ('manager_q7','staff_q7_01');
GO

-- ================================================================
-- SECTION 3: SEAT TYPE SURCHARGES
-- Schema đã INSERT mặc định 0% nhưng chưa có branch nên = 0 rows.
-- Insert đúng giá trị thực tế cho 2 branch vừa tạo.
-- ================================================================
INSERT INTO seat_type_surcharges (branch_id, seat_type, surcharge_rate)
SELECT b.branch_id, v.seat_type, v.rate
FROM (VALUES
    (N'MB Cinema Quận 1','NORMAL',  0.00),
    (N'MB Cinema Quận 1','VIP',    30.00),
    (N'MB Cinema Quận 1','COUPLE', 50.00),
    (N'MB Cinema Quận 7','NORMAL',  0.00),
    (N'MB Cinema Quận 7','VIP',    25.00),
    (N'MB Cinema Quận 7','COUPLE', 40.00)
) AS v(branch_name, seat_type, rate)
INNER JOIN cinema_branches b ON b.branch_name = v.branch_name
WHERE NOT EXISTS (
    SELECT 1 FROM seat_type_surcharges s
    WHERE s.branch_id = b.branch_id AND s.seat_type = v.seat_type
);
GO

-- ================================================================
-- SECTION 4: SCREENING ROOMS
-- ================================================================
INSERT INTO screening_rooms (branch_id, room_name, status)
SELECT b.branch_id, v.room_name, 'ACTIVE'
FROM (VALUES
    (N'MB Cinema Quận 1', N'Phòng 01 - Standard'),
    (N'MB Cinema Quận 1', N'Phòng 02 - Standard'),
    (N'MB Cinema Quận 1', N'Phòng 03 - VIP'),
    (N'MB Cinema Quận 7', N'Phòng 01 - Standard'),
    (N'MB Cinema Quận 7', N'Phòng 02 - Standard')
) AS v(branch_name, room_name)
INNER JOIN cinema_branches b ON b.branch_name = v.branch_name;
GO

-- ================================================================
-- SECTION 5: SEATS
-- Dùng biến để lấy room_id sau khi insert, tránh hardcode
-- ================================================================
DECLARE
    @r1 INT = (SELECT room_id FROM screening_rooms
               WHERE room_name = N'Phòng 01 - Standard'
                 AND branch_id = (SELECT branch_id FROM cinema_branches WHERE branch_name = N'MB Cinema Quận 1')),
    @r2 INT = (SELECT room_id FROM screening_rooms
               WHERE room_name = N'Phòng 02 - Standard'
                 AND branch_id = (SELECT branch_id FROM cinema_branches WHERE branch_name = N'MB Cinema Quận 1')),
    @r3 INT = (SELECT room_id FROM screening_rooms
               WHERE room_name = N'Phòng 03 - VIP'
                 AND branch_id = (SELECT branch_id FROM cinema_branches WHERE branch_name = N'MB Cinema Quận 1')),
    @r4 INT = (SELECT room_id FROM screening_rooms
               WHERE room_name = N'Phòng 01 - Standard'
                 AND branch_id = (SELECT branch_id FROM cinema_branches WHERE branch_name = N'MB Cinema Quận 7')),
    @r5 INT = (SELECT room_id FROM screening_rooms
               WHERE room_name = N'Phòng 02 - Standard'
                 AND branch_id = (SELECT branch_id FROM cinema_branches WHERE branch_name = N'MB Cinema Quận 7'));

-- ── Room 1 (Q1-P01): A-C NORMAL | D VIP | E COUPLE ──────────────
INSERT INTO seats (room_id,seat_code,seat_type,row_number,seat_number,status)
SELECT @r1,v.code,v.stype,v.rnum,v.snum,'AVAILABLE'
FROM (VALUES
    ('A1','NORMAL','A',1),('A2','NORMAL','A',2),('A3','NORMAL','A',3),('A4','NORMAL','A',4),
    ('A5','NORMAL','A',5),('A6','NORMAL','A',6),('A7','NORMAL','A',7),('A8','NORMAL','A',8),
    ('B1','NORMAL','B',1),('B2','NORMAL','B',2),('B3','NORMAL','B',3),('B4','NORMAL','B',4),
    ('B5','NORMAL','B',5),('B6','NORMAL','B',6),('B7','NORMAL','B',7),('B8','NORMAL','B',8),
    ('C1','NORMAL','C',1),('C2','NORMAL','C',2),('C3','NORMAL','C',3),('C4','NORMAL','C',4),
    ('C5','NORMAL','C',5),('C6','NORMAL','C',6),('C7','NORMAL','C',7),('C8','NORMAL','C',8),
    ('D1','VIP',  'D',1),('D2','VIP',  'D',2),('D3','VIP',  'D',3),
    ('D4','VIP',  'D',4),('D5','VIP',  'D',5),('D6','VIP',  'D',6),
    ('E1','COUPLE','E',1),('E2','COUPLE','E',2),('E3','COUPLE','E',3),('E4','COUPLE','E',4)
) AS v(code,stype,rnum,snum);

-- ── Room 2 (Q1-P02): A-C NORMAL ─────────────────────────────────
INSERT INTO seats (room_id,seat_code,seat_type,row_number,seat_number,status)
SELECT @r2,v.code,v.stype,v.rnum,v.snum,'AVAILABLE'
FROM (VALUES
    ('A1','NORMAL','A',1),('A2','NORMAL','A',2),('A3','NORMAL','A',3),('A4','NORMAL','A',4),
    ('A5','NORMAL','A',5),('A6','NORMAL','A',6),('A7','NORMAL','A',7),('A8','NORMAL','A',8),
    ('B1','NORMAL','B',1),('B2','NORMAL','B',2),('B3','NORMAL','B',3),('B4','NORMAL','B',4),
    ('B5','NORMAL','B',5),('B6','NORMAL','B',6),('B7','NORMAL','B',7),('B8','NORMAL','B',8),
    ('C1','NORMAL','C',1),('C2','NORMAL','C',2),('C3','NORMAL','C',3),('C4','NORMAL','C',4),
    ('C5','NORMAL','C',5),('C6','NORMAL','C',6),('C7','NORMAL','C',7),('C8','NORMAL','C',8)
) AS v(code,stype,rnum,snum);

-- ── Room 3 (Q1-VIP): A-B VIP | C COUPLE ─────────────────────────
INSERT INTO seats (room_id,seat_code,seat_type,row_number,seat_number,status)
SELECT @r3,v.code,v.stype,v.rnum,v.snum,'AVAILABLE'
FROM (VALUES
    ('A1','VIP','A',1),('A2','VIP','A',2),('A3','VIP','A',3),('A4','VIP','A',4),
    ('A5','VIP','A',5),('A6','VIP','A',6),('A7','VIP','A',7),('A8','VIP','A',8),
    ('B1','VIP','B',1),('B2','VIP','B',2),('B3','VIP','B',3),('B4','VIP','B',4),
    ('B5','VIP','B',5),('B6','VIP','B',6),('B7','VIP','B',7),('B8','VIP','B',8),
    ('C1','COUPLE','C',1),('C2','COUPLE','C',2),('C3','COUPLE','C',3),('C4','COUPLE','C',4)
) AS v(code,stype,rnum,snum);

-- ── Room 4 (Q7-P01): A-C NORMAL | D VIP ─────────────────────────
INSERT INTO seats (room_id,seat_code,seat_type,row_number,seat_number,status)
SELECT @r4,v.code,v.stype,v.rnum,v.snum,'AVAILABLE'
FROM (VALUES
    ('A1','NORMAL','A',1),('A2','NORMAL','A',2),('A3','NORMAL','A',3),('A4','NORMAL','A',4),
    ('A5','NORMAL','A',5),('A6','NORMAL','A',6),('A7','NORMAL','A',7),('A8','NORMAL','A',8),
    ('B1','NORMAL','B',1),('B2','NORMAL','B',2),('B3','NORMAL','B',3),('B4','NORMAL','B',4),
    ('B5','NORMAL','B',5),('B6','NORMAL','B',6),('B7','NORMAL','B',7),('B8','NORMAL','B',8),
    ('C1','NORMAL','C',1),('C2','NORMAL','C',2),('C3','NORMAL','C',3),('C4','NORMAL','C',4),
    ('C5','NORMAL','C',5),('C6','NORMAL','C',6),('C7','NORMAL','C',7),('C8','NORMAL','C',8),
    ('D1','VIP',  'D',1),('D2','VIP',  'D',2),('D3','VIP',  'D',3),
    ('D4','VIP',  'D',4),('D5','VIP',  'D',5),('D6','VIP',  'D',6)
) AS v(code,stype,rnum,snum);

-- ── Room 5 (Q7-P02): A-B NORMAL ─────────────────────────────────
INSERT INTO seats (room_id,seat_code,seat_type,row_number,seat_number,status)
SELECT @r5,v.code,v.stype,v.rnum,v.snum,'AVAILABLE'
FROM (VALUES
    ('A1','NORMAL','A',1),('A2','NORMAL','A',2),('A3','NORMAL','A',3),('A4','NORMAL','A',4),
    ('A5','NORMAL','A',5),('A6','NORMAL','A',6),('A7','NORMAL','A',7),('A8','NORMAL','A',8),
    ('B1','NORMAL','B',1),('B2','NORMAL','B',2),('B3','NORMAL','B',3),('B4','NORMAL','B',4),
    ('B5','NORMAL','B',5),('B6','NORMAL','B',6),('B7','NORMAL','B',7),('B8','NORMAL','B',8)
) AS v(code,stype,rnum,snum);
GO

-- ================================================================
-- SECTION 6: GENRES
-- ================================================================
INSERT INTO genres (genre_name, description, is_active) VALUES
(N'Hành Động',           N'Phim hành động, chiến đấu, rượt đuổi',  1),
(N'Phiêu Lưu',           N'Phim phiêu lưu khám phá',               1),
(N'Chính Kịch',          N'Phim tâm lý, nhân văn sâu sắc',         1),
(N'Tình Cảm',            N'Phim lãng mạn, tình yêu',               1),
(N'Kinh Dị',             N'Phim kinh dị, ma quái',                 1),
(N'Giật Gân',            N'Phim hồi hộp, bí ẩn',                  1),
(N'Hoạt Hình',           N'Phim hoạt hình cho mọi lứa tuổi',       1),
(N'Gia Đình',            N'Phim dành cho gia đình',                 1),
(N'Hài Hước',            N'Phim hài, giải trí nhẹ nhàng',          1),
(N'Khoa Học Viễn Tưởng', N'Phim sci-fi, tương lai',                1);
GO

-- ================================================================
-- SECTION 7: MOVIES
-- ================================================================
INSERT INTO movies
    (title, description, duration, release_date, end_date,
     rating, age_rating, director, cast, poster_url, is_active)
VALUES
(N'Thunder Legion: Uprising',
 N'Liên minh siêu anh hùng đối mặt với mối đe dọa liên vũ trụ nguy hiểm nhất trong lịch sử.',
 150,'2026-03-15','2026-05-15',4.8,'PG-13',
 N'James Cameron Jr.',
 N'Chris Evans, Scarlett Johansson, Robert Downey IV',
 'https://cdn.mbcinema.vn/posters/thunder-legion.jpg',1),

(N'Mãi Là Của Nhau',
 N'Câu chuyện tình yêu xuyên thời gian của đôi trẻ vượt qua mọi thử thách để đến với nhau.',
 120,'2026-03-10','2026-04-30',4.2,'PG',
 N'Nguyễn Quang Dũng',
 N'Kaity Nguyễn, Isaac, Trấn Thành',
 'https://cdn.mbcinema.vn/posters/mai-la-cua-nhau.jpg',1),

(N'The Dark Ritual',
 N'Một nhóm thám tử điều tra vụ mất tích bí ẩn và vô tình chạm đến thế lực bóng tối cổ xưa.',
 105,'2026-02-28','2026-04-20',3.9,'R',
 N'James Wan',
 N'Patrick Wilson, Vera Farmiga, Javier Bardem',
 'https://cdn.mbcinema.vn/posters/dark-ritual.jpg',1),

(N'Paw Friends: The Lost Island',
 N'Những chú thú cưng dũng cảm cùng nhau phiêu lưu tìm đường trở về nhà từ hòn đảo bí ẩn.',
 95,'2026-03-20','2026-05-31',4.5,'G',
 N'Chris Buck',
 N'Lồng tiếng: Trấn Thành, Việt Hương, BB Trần',
 'https://cdn.mbcinema.vn/posters/paw-friends.jpg',1);
GO

-- ================================================================
-- SECTION 8: MOVIE_GENRES
-- ================================================================
INSERT INTO movie_genres (movie_id, genre_id)
SELECT m.movie_id, g.genre_id
FROM (VALUES
    (N'Thunder Legion: Uprising',    N'Hành Động'),
    (N'Thunder Legion: Uprising',    N'Phiêu Lưu'),
    (N'Thunder Legion: Uprising',    N'Khoa Học Viễn Tưởng'),
    (N'Mãi Là Của Nhau',             N'Chính Kịch'),
    (N'Mãi Là Của Nhau',             N'Tình Cảm'),
    (N'The Dark Ritual',             N'Kinh Dị'),
    (N'The Dark Ritual',             N'Giật Gân'),
    (N'Paw Friends: The Lost Island',N'Hoạt Hình'),
    (N'Paw Friends: The Lost Island',N'Gia Đình'),
    (N'Paw Friends: The Lost Island',N'Hài Hước')
) AS v(movie_title, genre_name)
INNER JOIN movies m ON m.title      = v.movie_title
INNER JOIN genres g ON g.genre_name = v.genre_name;
GO

-- ================================================================
-- SECTION 9: TICKET PRICES
-- Không có seat_type (đã xóa khỏi schema).
-- Giá cuối = price × (1 + surcharge_rate/100)
--   Q1 VIP   = price × 1.30
--   Q1 COUPLE= price × 1.50
--   Q7 VIP   = price × 1.25
--   Q7 COUPLE= price × 1.40
-- ================================================================
INSERT INTO ticket_prices
    (ticket_type, day_type, time_slot, price, effective_from, is_active, branch_id)
SELECT v.ticket_type, v.day_type, v.time_slot, v.price,
       '2026-01-01', 1, b.branch_id
FROM (VALUES
    -- ── Q1 ADULT ──────────────────────────────────────────────
    (N'MB Cinema Quận 1','ADULT','WEEKDAY','MORNING',   75000),
    (N'MB Cinema Quận 1','ADULT','WEEKDAY','AFTERNOON', 90000),
    (N'MB Cinema Quận 1','ADULT','WEEKDAY','EVENING',  110000),
    (N'MB Cinema Quận 1','ADULT','WEEKDAY','NIGHT',    105000),
    (N'MB Cinema Quận 1','ADULT','WEEKEND','MORNING',   90000),
    (N'MB Cinema Quận 1','ADULT','WEEKEND','AFTERNOON',105000),
    (N'MB Cinema Quận 1','ADULT','WEEKEND','EVENING',  130000),
    (N'MB Cinema Quận 1','ADULT','WEEKEND','NIGHT',    120000),
    -- ── Q1 CHILD ──────────────────────────────────────────────
    (N'MB Cinema Quận 1','CHILD','WEEKDAY','MORNING',   55000),
    (N'MB Cinema Quận 1','CHILD','WEEKDAY','AFTERNOON', 65000),
    (N'MB Cinema Quận 1','CHILD','WEEKDAY','EVENING',   80000),
    (N'MB Cinema Quận 1','CHILD','WEEKDAY','NIGHT',     75000),
    (N'MB Cinema Quận 1','CHILD','WEEKEND','MORNING',   65000),
    (N'MB Cinema Quận 1','CHILD','WEEKEND','AFTERNOON', 75000),
    (N'MB Cinema Quận 1','CHILD','WEEKEND','EVENING',   95000),
    (N'MB Cinema Quận 1','CHILD','WEEKEND','NIGHT',     85000),
    -- ── Q7 ADULT ──────────────────────────────────────────────
    (N'MB Cinema Quận 7','ADULT','WEEKDAY','MORNING',   65000),
    (N'MB Cinema Quận 7','ADULT','WEEKDAY','AFTERNOON', 80000),
    (N'MB Cinema Quận 7','ADULT','WEEKDAY','EVENING',   95000),
    (N'MB Cinema Quận 7','ADULT','WEEKDAY','NIGHT',     90000),
    (N'MB Cinema Quận 7','ADULT','WEEKEND','MORNING',   75000),
    (N'MB Cinema Quận 7','ADULT','WEEKEND','AFTERNOON', 90000),
    (N'MB Cinema Quận 7','ADULT','WEEKEND','EVENING',  110000),
    (N'MB Cinema Quận 7','ADULT','WEEKEND','NIGHT',    100000),
    -- ── Q7 CHILD ──────────────────────────────────────────────
    (N'MB Cinema Quận 7','CHILD','WEEKDAY','MORNING',   45000),
    (N'MB Cinema Quận 7','CHILD','WEEKDAY','AFTERNOON', 55000),
    (N'MB Cinema Quận 7','CHILD','WEEKDAY','EVENING',   70000),
    (N'MB Cinema Quận 7','CHILD','WEEKDAY','NIGHT',     65000),
    (N'MB Cinema Quận 7','CHILD','WEEKEND','MORNING',   55000),
    (N'MB Cinema Quận 7','CHILD','WEEKEND','AFTERNOON', 65000),
    (N'MB Cinema Quận 7','CHILD','WEEKEND','EVENING',   80000),
    (N'MB Cinema Quận 7','CHILD','WEEKEND','NIGHT',     70000)
) AS v(branch_name, ticket_type, day_type, time_slot, price)
INNER JOIN cinema_branches b ON b.branch_name = v.branch_name;
GO

-- ================================================================
-- SECTION 10: SHOWTIMES
-- 27/03/2026 = Thứ 6 (WEEKDAY) | 28/03/2026 = Thứ 7 (WEEKEND)
-- base_price lấy từ ticket_prices ADULT của ca / ngày tương ứng
-- ================================================================
INSERT INTO showtimes
    (movie_id, room_id, show_date, start_time, end_time, base_price, status)
SELECT
    m.movie_id,
    sr.room_id,
    v.show_date,
    v.start_time,
    v.end_time,
    tp.price      AS base_price,
    'SCHEDULED'
FROM (VALUES
-- movie_title                       branch               room_name              show_date     start   end     day_type  slot
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 1', N'Phòng 01 - Standard','2026-03-27','09:00','11:30','WEEKDAY','MORNING'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 1', N'Phòng 01 - Standard','2026-03-27','13:00','15:00','WEEKDAY','AFTERNOON'),
(N'The Dark Ritual',             N'MB Cinema Quận 1', N'Phòng 01 - Standard','2026-03-27','17:00','18:45','WEEKDAY','EVENING'),
(N'Paw Friends: The Lost Island',N'MB Cinema Quận 1', N'Phòng 01 - Standard','2026-03-27','21:00','22:35','WEEKDAY','NIGHT'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 1', N'Phòng 01 - Standard','2026-03-28','09:00','11:30','WEEKEND','MORNING'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 1', N'Phòng 01 - Standard','2026-03-28','13:00','15:00','WEEKEND','AFTERNOON'),
(N'Paw Friends: The Lost Island',N'MB Cinema Quận 1', N'Phòng 01 - Standard','2026-03-28','16:00','17:35','WEEKEND','AFTERNOON'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 1', N'Phòng 01 - Standard','2026-03-28','20:00','22:30','WEEKEND','NIGHT'),

(N'Paw Friends: The Lost Island',N'MB Cinema Quận 1', N'Phòng 02 - Standard','2026-03-27','10:00','11:35','WEEKDAY','MORNING'),
(N'The Dark Ritual',             N'MB Cinema Quận 1', N'Phòng 02 - Standard','2026-03-27','14:00','15:45','WEEKDAY','AFTERNOON'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 1', N'Phòng 02 - Standard','2026-03-27','18:00','20:00','WEEKDAY','EVENING'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 1', N'Phòng 02 - Standard','2026-03-27','21:00','23:30','WEEKDAY','NIGHT'),
(N'The Dark Ritual',             N'MB Cinema Quận 1', N'Phòng 02 - Standard','2026-03-28','10:00','11:45','WEEKEND','MORNING'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 1', N'Phòng 02 - Standard','2026-03-28','14:30','17:00','WEEKEND','AFTERNOON'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 1', N'Phòng 02 - Standard','2026-03-28','18:30','20:30','WEEKEND','EVENING'),
(N'Paw Friends: The Lost Island',N'MB Cinema Quận 1', N'Phòng 02 - Standard','2026-03-28','21:30','23:05','WEEKEND','NIGHT'),

(N'Mãi Là Của Nhau',             N'MB Cinema Quận 1', N'Phòng 03 - VIP',     '2026-03-27','11:00','13:00','WEEKDAY','MORNING'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 1', N'Phòng 03 - VIP',     '2026-03-27','15:00','17:30','WEEKDAY','AFTERNOON'),
(N'The Dark Ritual',             N'MB Cinema Quận 1', N'Phòng 03 - VIP',     '2026-03-27','19:30','21:15','WEEKDAY','EVENING'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 1', N'Phòng 03 - VIP',     '2026-03-28','11:00','13:30','WEEKEND','MORNING'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 1', N'Phòng 03 - VIP',     '2026-03-28','15:30','17:30','WEEKEND','AFTERNOON'),
(N'Paw Friends: The Lost Island',N'MB Cinema Quận 1', N'Phòng 03 - VIP',     '2026-03-28','20:00','21:35','WEEKEND','EVENING'),

(N'Thunder Legion: Uprising',    N'MB Cinema Quận 7', N'Phòng 01 - Standard','2026-03-27','09:00','11:30','WEEKDAY','MORNING'),
(N'The Dark Ritual',             N'MB Cinema Quận 7', N'Phòng 01 - Standard','2026-03-27','13:00','14:45','WEEKDAY','AFTERNOON'),
(N'Paw Friends: The Lost Island',N'MB Cinema Quận 7', N'Phòng 01 - Standard','2026-03-27','17:00','18:35','WEEKDAY','EVENING'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 7', N'Phòng 01 - Standard','2026-03-27','20:00','22:00','WEEKDAY','EVENING'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 7', N'Phòng 01 - Standard','2026-03-28','09:30','11:30','WEEKEND','MORNING'),
(N'Paw Friends: The Lost Island',N'MB Cinema Quận 7', N'Phòng 01 - Standard','2026-03-28','13:30','15:05','WEEKEND','AFTERNOON'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 7', N'Phòng 01 - Standard','2026-03-28','17:30','20:00','WEEKEND','EVENING'),
(N'The Dark Ritual',             N'MB Cinema Quận 7', N'Phòng 01 - Standard','2026-03-28','21:00','22:45','WEEKEND','NIGHT'),

(N'The Dark Ritual',             N'MB Cinema Quận 7', N'Phòng 02 - Standard','2026-03-27','10:00','11:45','WEEKDAY','MORNING'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 7', N'Phòng 02 - Standard','2026-03-27','14:00','16:00','WEEKDAY','AFTERNOON'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 7', N'Phòng 02 - Standard','2026-03-27','18:00','20:30','WEEKDAY','EVENING'),
(N'Paw Friends: The Lost Island',N'MB Cinema Quận 7', N'Phòng 02 - Standard','2026-03-27','21:30','23:05','WEEKDAY','NIGHT'),
(N'Paw Friends: The Lost Island',N'MB Cinema Quận 7', N'Phòng 02 - Standard','2026-03-28','10:00','11:35','WEEKEND','MORNING'),
(N'Thunder Legion: Uprising',    N'MB Cinema Quận 7', N'Phòng 02 - Standard','2026-03-28','14:00','16:30','WEEKEND','AFTERNOON'),
(N'The Dark Ritual',             N'MB Cinema Quận 7', N'Phòng 02 - Standard','2026-03-28','18:30','20:15','WEEKEND','EVENING'),
(N'Mãi Là Của Nhau',             N'MB Cinema Quận 7', N'Phòng 02 - Standard','2026-03-28','21:00','23:00','WEEKEND','NIGHT')
) AS v(movie_title, branch_name, room_name, show_date, start_time, end_time, day_type, time_slot)
INNER JOIN movies          m  ON m.title      = v.movie_title
INNER JOIN cinema_branches cb ON cb.branch_name = v.branch_name
INNER JOIN screening_rooms sr ON sr.room_name   = v.room_name
                              AND sr.branch_id   = cb.branch_id
-- Lấy giá ADULT của branch theo day_type + time_slot làm base_price
INNER JOIN ticket_prices   tp ON tp.branch_id   = cb.branch_id
                              AND tp.ticket_type = 'ADULT'
                              AND tp.day_type    = v.day_type
                              AND tp.time_slot   = v.time_slot
                              AND tp.is_active   = 1;
GO

-- ================================================================
-- SECTION 11: CONCESSIONS
-- ================================================================
INSERT INTO concessions
    (concession_type, concession_name, quantity, price_base, added_by)
SELECT v.ctype, v.cname, v.qty, v.price, u.user_id
FROM (VALUES
    ('FOOD',     N'Bắp Rang Lớn',               200, 55000.0),
    ('FOOD',     N'Bắp Rang Vừa',               300, 45000.0),
    ('FOOD',     N'Nachos Phô Mai',              150, 50000.0),
    ('BEVERAGE', N'Coca-Cola (L)',               400, 35000.0),
    ('BEVERAGE', N'Sprite (L)',                  350, 35000.0),
    ('FOOD',     N'Combo Đôi (Bắp L + 2 Coke)', 100, 99000.0)
) AS v(ctype, cname, qty, price)
CROSS JOIN (SELECT user_id FROM users WHERE username = 'admin01') u;
GO

-- ================================================================
-- SECTION 12: VOUCHERS
-- ================================================================
INSERT INTO vouchers
    (voucher_name, voucher_type, voucher_code,
     points_cost, discount_amount, max_usage_limit,
     current_usage, valid_days, is_active)
VALUES
(N'Voucher Ưu Đãi 50K',        'LOYALTY', NULL,            200, 50000,  0, 0, 30, 1),
(N'Voucher Ưu Đãi 100K',       'LOYALTY', NULL,            400,100000,  0, 0, 30, 1),
(N'Khai Trương Q7 – Giảm 30K', 'PUBLIC', 'KHAITRUONG30',    0, 30000, 100, 5, 60, 1);
GO

-- ================================================================
-- SECTION 13: USER VOUCHERS
-- ================================================================
INSERT INTO user_vouchers
    (user_id, voucher_id, voucher_code, status, redeemed_at, expires_at, used_at)
SELECT u.user_id, v.voucher_id, uv.code, uv.status,
       SYSDATETIME(),
       DATEADD(DAY, v.valid_days, SYSDATETIME()),
       CASE uv.status WHEN 'USED' THEN SYSDATETIME() ELSE NULL END
FROM (VALUES
    -- username          voucher_name             code                   status
    ('khachhang03', N'Voucher Ưu Đãi 50K',  'UV-C03-V1-00001', 'AVAILABLE'),
    ('khachhang04', N'Voucher Ưu Đãi 50K',  'UV-C04-V1-00001', 'USED'),
    ('khachhang04', N'Voucher Ưu Đãi 100K', 'UV-C04-V2-00001', 'AVAILABLE'),
    ('khachhang02', N'Khai Trương Q7 – Giảm 30K','KHAITRUONG30','AVAILABLE')
) AS uv(username, voucher_name, code, status)
INNER JOIN users    u ON u.username     = uv.username
INNER JOIN vouchers v ON v.voucher_name = uv.voucher_name;
GO

-- ================================================================
-- SECTION 14: POINT HISTORY (quá khứ trước ngày 27/03)
-- ================================================================
INSERT INTO point_history
    (user_id, points_changed, transaction_type, description, reference_id)
SELECT u.user_id, v.pts, v.ttype, v.descr, NULL
FROM (VALUES
-- username        pts   type        description
('khachhang01',   50, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-001'),

('khachhang02',   50, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-002'),
('khachhang02',   60, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-003'),
('khachhang02',   40, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-004'),
('khachhang02',  -30, 'REDEEM',  N'Đổi Voucher Khai Trương Q7'),

('khachhang03',   80, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-005'),
('khachhang03',   70, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-006'),
('khachhang03',   80, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-007'),
('khachhang03',   50, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-008'),
('khachhang03',  -30, 'REDEEM',  N'Đổi quà tặng điểm'),

('khachhang04',  100, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-009'),
('khachhang04',   80, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-010'),
('khachhang04',   90, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-011'),
('khachhang04',   80, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-012'),
('khachhang04',   70, 'EARN',    N'Tích điểm từ đặt vé online #B-HIST-013'),
('khachhang04',  -70, 'REDEEM',  N'Đổi điểm giảm giá tại quầy'),
('khachhang04', -200, 'REDEEM',  N'Đổi Voucher Ưu Đãi 50K – UV-C04-V1-00001')
) AS v(username, pts, ttype, descr)
INNER JOIN users u ON u.username = v.username;
GO

-- ================================================================
-- SECTION 15: COUNTER TICKETS (3 giao dịch mẫu)
--
-- Công thức:
--   base = ticket_prices.price (ADULT/CHILD × day_type × slot × branch)
--   final_seat_price = base × (1 + surcharge_rate/100)
--
-- GD1 – Showtime: Thunder Legion, R1-Q1, 27/03 09:00 (WEEKDAY MORNING)
--       Ghế A1+A2 NORMAL | ADULT | Q1 surcharge NORMAL=0%
--       Giá = 75.000 × 1.00 = 75.000đ/ghế | Staff: staff_q1_01
--
-- GD2 – Showtime: Mãi Là Của Nhau, R1-Q1, 27/03 13:00 (WEEKDAY AFTERNOON)
--       Ghế D1 VIP ADULT + D2 VIP CHILD | Q1 surcharge VIP=30%
--       ADULT = 90.000 × 1.30 = 117.000đ
--       CHILD = 65.000 × 1.30 =  84.500đ | Staff: staff_q1_02
--
-- GD3 – Showtime: Thunder Legion, R1-Q1, 28/03 20:00 (WEEKEND NIGHT)
--       Ghế E1+E2 COUPLE ADULT | Q1 surcharge COUPLE=50%
--       Giá = 120.000 × 1.50 = 180.000đ/ghế | Staff: staff_q1_01
-- ================================================================
INSERT INTO counter_tickets
    (showtime_id, seat_id, ticket_type, seat_type, price,
     sold_by, payment_method,
     customer_name, customer_phone, customer_email, notes)
-- ── GD1: 2 ghế NORMAL, vãng lai ─────────────────────────────────
SELECT
    st.showtime_id,
    s.seat_id,
    'ADULT', 'NORMAL', 75000,
    sold.user_id, 'CASH',
    N'Khách Vãng Lai','0900000000', NULL,
    N'GD1 – Vé lẻ tiền mặt'
FROM showtimes st
INNER JOIN movies          m  ON m.movie_id = st.movie_id  AND m.title = N'Thunder Legion: Uprising'
INNER JOIN screening_rooms sr ON sr.room_id = st.room_id
INNER JOIN cinema_branches cb ON cb.branch_id = sr.branch_id AND cb.branch_name = N'MB Cinema Quận 1'
INNER JOIN seats           s  ON s.room_id = sr.room_id AND s.seat_code IN ('A1','A2')
CROSS JOIN (SELECT user_id FROM users WHERE username = 'staff_q1_01') sold
WHERE st.show_date = '2026-03-27' AND st.start_time = '09:00'
  AND sr.room_name = N'Phòng 01 - Standard'

UNION ALL

-- ── GD2: VIP ADULT D1 + VIP CHILD D2, khachhang02, BANKING ──────
SELECT
    st.showtime_id,
    s.seat_id,
    CASE s.seat_code WHEN 'D1' THEN 'ADULT' ELSE 'CHILD' END,
    'VIP',
    CASE s.seat_code WHEN 'D1' THEN 117000  ELSE 84500  END,
    sold.user_id, 'BANKING',
    N'Trần Văn Nam','0912000002','khachhang02@gmail.com',
    N'GD2 – Thành viên BRONZE, chuyển khoản'
FROM showtimes st
INNER JOIN movies          m  ON m.movie_id = st.movie_id  AND m.title = N'Mãi Là Của Nhau'
INNER JOIN screening_rooms sr ON sr.room_id = st.room_id
INNER JOIN cinema_branches cb ON cb.branch_id = sr.branch_id AND cb.branch_name = N'MB Cinema Quận 1'
INNER JOIN seats           s  ON s.room_id = sr.room_id AND s.seat_code IN ('D1','D2')
CROSS JOIN (SELECT user_id FROM users WHERE username = 'staff_q1_02') sold
WHERE st.show_date = '2026-03-27' AND st.start_time = '13:00'
  AND sr.room_name = N'Phòng 01 - Standard'

UNION ALL

-- ── GD3: 2 ghế COUPLE ADULT, khachhang04 GOLD, Voucher -50K ─────
SELECT
    st.showtime_id,
    s.seat_id,
    'ADULT', 'COUPLE', 180000,
    sold.user_id, 'CASH',
    N'Phạm Văn Đức','0912000004','khachhang04@gmail.com',
    N'GD3 – Thành viên GOLD, dùng Voucher UV-C04-V1-00001 (giảm 50K)'
FROM showtimes st
INNER JOIN movies          m  ON m.movie_id = st.movie_id  AND m.title = N'Thunder Legion: Uprising'
INNER JOIN screening_rooms sr ON sr.room_id = st.room_id
INNER JOIN cinema_branches cb ON cb.branch_id = sr.branch_id AND cb.branch_name = N'MB Cinema Quận 1'
INNER JOIN seats           s  ON s.room_id = sr.room_id AND s.seat_code IN ('E1','E2')
CROSS JOIN (SELECT user_id FROM users WHERE username = 'staff_q1_01') sold
WHERE st.show_date = '2026-03-28' AND st.start_time = '20:00'
  AND sr.room_name = N'Phòng 01 - Standard';
GO

-- ================================================================
-- SECTION 16: INVOICES + INVOICE ITEMS
-- Dùng SCOPE_IDENTITY() từng bước để lấy invoice_id vừa tạo
-- ================================================================
DECLARE
    @inv1 INT, @inv2 INT, @inv3 INT,
    @branch1 INT = (SELECT branch_id FROM cinema_branches WHERE branch_name = N'MB Cinema Quận 1'),
    @staff1  INT = (SELECT user_id   FROM users WHERE username = 'staff_q1_01'),
    @staff2  INT = (SELECT user_id   FROM users WHERE username = 'staff_q1_02');

-- ── Invoice 1 ────────────────────────────────────────────────────
INSERT INTO invoices
    (invoice_code, sale_channel,
     customer_name, customer_phone,
     branch_id, total_amount, discount_amount, final_amount,
     payment_method, payment_status, status, created_by, notes)
VALUES
('INV-20260327-000001','COUNTER',
 N'Khách Vãng Lai','0900000000',
 @branch1, 150000, 0, 150000,
 'CASH','PAID','ACTIVE', @staff1,
 N'GD1: 2 vé NORMAL – Thunder Legion sáng 27/03');

SET @inv1 = SCOPE_IDENTITY();

INSERT INTO invoice_items
    (invoice_id, item_type, counter_ticket_id,
     item_description, movie_title, showtime_date, showtime_time,
     room_name, seat_code, ticket_type, seat_type,
     quantity, unit_price, amount)
SELECT
    @inv1, 'COUNTER_TICKET', ct.ticket_id,
    N'Thunder Legion: Uprising – NORMAL – ' + s.seat_code,
    N'Thunder Legion: Uprising', '2026-03-27', st.start_time,
    sr.room_name, s.seat_code, ct.ticket_type, ct.seat_type,
    1, ct.price, ct.price
FROM counter_tickets ct
INNER JOIN showtimes       st ON st.showtime_id = ct.showtime_id
INNER JOIN movies          m  ON m.movie_id     = st.movie_id
INNER JOIN seats           s  ON s.seat_id      = ct.seat_id
INNER JOIN screening_rooms sr ON sr.room_id     = s.room_id
WHERE m.title = N'Thunder Legion: Uprising'
  AND st.show_date = '2026-03-27' AND st.start_time = '09:00'
  AND s.seat_code IN ('A1','A2');

-- ── Invoice 2 ────────────────────────────────────────────────────
INSERT INTO invoices
    (invoice_code, sale_channel,
     customer_name, customer_phone, customer_email,
     branch_id, total_amount, discount_amount, final_amount,
     payment_method, payment_status, status, created_by, notes)
VALUES
('INV-20260327-000002','COUNTER',
 N'Trần Văn Nam','0912000002','khachhang02@gmail.com',
 @branch1, 201500, 0, 201500,
 'BANKING','PAID','ACTIVE', @staff2,
 N'GD2: VIP ADULT+CHILD – Mãi Là Của Nhau chiều 27/03');

SET @inv2 = SCOPE_IDENTITY();

INSERT INTO invoice_items
    (invoice_id, item_type, counter_ticket_id,
     item_description, movie_title, showtime_date, showtime_time,
     room_name, seat_code, ticket_type, seat_type,
     quantity, unit_price, amount)
SELECT
    @inv2, 'COUNTER_TICKET', ct.ticket_id,
    N'Mãi Là Của Nhau – VIP – ' + s.seat_code,
    N'Mãi Là Của Nhau', '2026-03-27', st.start_time,
    sr.room_name, s.seat_code, ct.ticket_type, ct.seat_type,
    1, ct.price, ct.price
FROM counter_tickets ct
INNER JOIN showtimes       st ON st.showtime_id = ct.showtime_id
INNER JOIN movies          m  ON m.movie_id     = st.movie_id
INNER JOIN seats           s  ON s.seat_id      = ct.seat_id
INNER JOIN screening_rooms sr ON sr.room_id     = s.room_id
WHERE m.title = N'Mãi Là Của Nhau'
  AND st.show_date = '2026-03-27' AND st.start_time = '13:00'
  AND s.seat_code IN ('D1','D2');

-- ── Invoice 3 ────────────────────────────────────────────────────
INSERT INTO invoices
    (invoice_code, sale_channel,
     customer_name, customer_phone, customer_email,
     branch_id, total_amount, discount_amount, final_amount,
     payment_method, payment_status, status, created_by, notes)
VALUES
('INV-20260328-000001','COUNTER',
 N'Phạm Văn Đức','0912000004','khachhang04@gmail.com',
 @branch1, 360000, 50000, 310000,
 'CASH','PAID','ACTIVE', @staff1,
 N'GD3: 2 COUPLE – Thunder Legion tối 28/03, Voucher UV-C04-V1-00001 (-50K)');

SET @inv3 = SCOPE_IDENTITY();

INSERT INTO invoice_items
    (invoice_id, item_type, counter_ticket_id,
     item_description, movie_title, showtime_date, showtime_time,
     room_name, seat_code, ticket_type, seat_type,
     quantity, unit_price, amount)
SELECT
    @inv3, 'COUNTER_TICKET', ct.ticket_id,
    N'Thunder Legion: Uprising – COUPLE – ' + s.seat_code,
    N'Thunder Legion: Uprising', '2026-03-28', st.start_time,
    sr.room_name, s.seat_code, ct.ticket_type, ct.seat_type,
    1, ct.price, ct.price
FROM counter_tickets ct
INNER JOIN showtimes       st ON st.showtime_id = ct.showtime_id
INNER JOIN movies          m  ON m.movie_id     = st.movie_id
INNER JOIN seats           s  ON s.seat_id      = ct.seat_id
INNER JOIN screening_rooms sr ON sr.room_id     = s.room_id
WHERE m.title = N'Thunder Legion: Uprising'
  AND st.show_date = '2026-03-28' AND st.start_time = '20:00'
  AND s.seat_code IN ('E1','E2');
GO

-- ================================================================
-- SECTION 17: CẬP NHẬT ĐIỂM sau giao dịch counter có thành viên
--
-- GD2 – khachhang02 (BRONZE, multiplier=1.1)
--   201.500 / 10.000 = 20 đơn vị × 1.1 ≈ 22 điểm
-- GD3 – khachhang04 (GOLD, multiplier=1.3)
--   310.000 / 10.000 = 31 đơn vị × 1.3 ≈ 40 điểm
-- ================================================================
UPDATE users
SET points                   = points + 22,
    total_accumulated_points = total_accumulated_points + 22
WHERE username = 'khachhang02';

INSERT INTO point_history
    (user_id, points_changed, transaction_type, description, reference_id)
SELECT user_id, 22, 'EARN',
       N'Tích điểm mua vé tại quầy – INV-20260327-000002',
       (SELECT invoice_id FROM invoices WHERE invoice_code = 'INV-20260327-000002')
FROM users WHERE username = 'khachhang02';

UPDATE users
SET points                   = points + 40,
    total_accumulated_points = total_accumulated_points + 40
WHERE username = 'khachhang04';

INSERT INTO point_history
    (user_id, points_changed, transaction_type, description, reference_id)
SELECT user_id, 40, 'EARN',
       N'Tích điểm mua vé tại quầy – INV-20260328-000001',
       (SELECT invoice_id FROM invoices WHERE invoice_code = 'INV-20260328-000001')
FROM users WHERE username = 'khachhang04';
GO

-- ================================================================
-- KIỂM TRA NHANH
-- ================================================================
SELECT 'roles'            AS [table], COUNT(*) AS total FROM roles             UNION ALL
SELECT 'membership_tiers',            COUNT(*)          FROM membership_tiers  UNION ALL
SELECT 'users',                       COUNT(*)          FROM users             UNION ALL
SELECT 'cinema_branches',             COUNT(*)          FROM cinema_branches   UNION ALL
SELECT 'seat_type_surcharges',        COUNT(*)          FROM seat_type_surcharges UNION ALL
SELECT 'screening_rooms',             COUNT(*)          FROM screening_rooms   UNION ALL
SELECT 'seats',                       COUNT(*)          FROM seats             UNION ALL
SELECT 'genres',                      COUNT(*)          FROM genres            UNION ALL
SELECT 'movies',                      COUNT(*)          FROM movies            UNION ALL
SELECT 'movie_genres',                COUNT(*)          FROM movie_genres      UNION ALL
SELECT 'ticket_prices',               COUNT(*)          FROM ticket_prices     UNION ALL
SELECT 'showtimes',                   COUNT(*)          FROM showtimes         UNION ALL
SELECT 'concessions',                 COUNT(*)          FROM concessions       UNION ALL
SELECT 'vouchers',                    COUNT(*)          FROM vouchers          UNION ALL
SELECT 'user_vouchers',               COUNT(*)          FROM user_vouchers     UNION ALL
SELECT 'counter_tickets',             COUNT(*)          FROM counter_tickets   UNION ALL
SELECT 'invoices',                    COUNT(*)          FROM invoices          UNION ALL
SELECT 'invoice_items',               COUNT(*)          FROM invoice_items     UNION ALL
SELECT 'point_history',               COUNT(*)          FROM point_history     UNION ALL
SELECT 'loyalty_configs',             COUNT(*)          FROM loyalty_configs;
GO