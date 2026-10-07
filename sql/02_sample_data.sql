-- ================================================================
-- SQL Server Script: Dữ liệu mẫu (Sample Data) phong phú
-- Sử dụng DATEADD(day, X, GETDATE()) để đảm bảo luôn có phim ĐANG CHIẾU & SẮP CHIẾU
-- ================================================================

USE MovieBrowsingDB;
GO

-- 1. Insert Danh mục thể loại (Genres)
SET IDENTITY_INSERT genres ON;
INSERT INTO genres (genre_id, genre_name, description) VALUES
(1, N'Hành Động', N'Phim có nhiều cảnh chiến đấu, đuổi bắt kịch tính'),
(2, N'Phiêu Lưu', N'Các chuyến hành trình khám phá mạo hiểm'),
(3, N'Hoạt Hình', N'Phim hoạt hình 2D/3D cho mọi lứa tuổi'),
(4, N'Hài Hước', N'Phim mang tính giải trí, hài hước, vui vẻ'),
(5, N'Kinh Dị', N'Phim rùng rợn, giật gân, tạo cảm giác hồi hộp sợ hãi'),
(6, N'Tình Cảm', N'Phim lãng mạn, câu chuyện tình yêu ngọt ngào hoặc trắc trở'),
(7, N'Khoa Học Viễn Tưởng', N'Phim về tương lai, không gian, công nghệ vũ trụ'),
(8, N'Tâm Lý - Xã Hội', N'Phim chiều sâu tâm lý nhân vật và các vấn đề xã hội'),
(9, N'Giật Gân', N'Phim điều tra phá án, gián điệp, bất ngờ phút chót');
SET IDENTITY_INSERT genres OFF;
GO

-- 2. Insert Phim (Movies)
-- Phim đang chiếu: release_date <= GETDATE() và (end_date >= GETDATE() hoặc end_date IS NULL)
-- Phim sắp chiếu: release_date > GETDATE()

SET IDENTITY_INSERT movies ON;
INSERT INTO movies 
(movie_id, title, description, duration, release_date, end_date, rating, age_rating, director, cast, poster_url, trailer_url, is_active)
VALUES
-- === NHÓM 1: PHIM ĐANG CHIẾU (NOW SHOWING) ===
(1, 
 N'Chiến Binh Sấm Sét: Cuộc Chiến Vô Cực', 
 N'Một mối đe dọa vũ trụ trỗi dậy từ vùng tối của thiên hà buộc biệt đội chiến binh cổ đại phải tái hợp. Với đồ họa mãn nhãn cùng những trận đánh nghẹt thở, bộ phim đưa người xem vào trận chiến sinh tử bảo vệ nền văn minh nhân loại.', 
 148, 
 DATEADD(day, -15, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 25, CAST(GETDATE() AS DATE)), 
 4.8, 'T16', 
 N'Christopher Nolan Jr.', 
 N'Tom Hiddleston, Chris Evans, Scarlett Davis, Ryan Lee', 
 'https://images.unsplash.com/photo-1536440136628-849c177e76a1?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=TcMBFSGVi1c', 
 1),

(2, 
 N'Hành Tinh Băng Giá: Vùng Đất Chết', 
 N'Khi Trái Đất bước vào kỷ băng hà thứ hai, một đoàn thám hiểm liều mình tiến vào lõi địa cực để kích hoạt nguồn năng lượng địa nhiệt bí ẩn. Họ phát hiện ra những sinh vật kỳ lạ đang thức giấc dưới lớp băng ngàn năm.', 
 126, 
 DATEADD(day, -7, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 35, CAST(GETDATE() AS DATE)), 
 4.6, 'T13', 
 N'Denis Villeneuve', 
 N'Timothée Chalamet, Rebecca Ferguson, Oscar Isaac', 
 'https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=8g18jFHCLXk', 
 1),

(3, 
 N'Biệt Đội Thú Cưng: Đại Náo Rừng Xanh', 
 N'Bộ phim hoạt hình 3D vui nhộn kể về Max - chú cún can đảm dẫn đầu liên minh thú cưng lên đường giải cứu khu rừng nhiệt đới khỏi tay một tập đoàn xây dựng tham lam. Những tình huống dở khóc dở cười ngập tràn cảm xúc.', 
 95, 
 DATEADD(day, -20, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 18, CAST(GETDATE() AS DATE)), 
 4.7, 'P', 
 N'Chris Renaud', 
 N'Lồng tiếng: Trấn Thành, Ninh Dương Lan Ngọc, BB Trần', 
 'https://images.unsplash.com/photo-1534447677768-be436bb09401?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=mYfJxlgR2jw', 
 1),

(4, 
 N'Mùa Hè Năm Ấy Chúng Ta Từng Yêu', 
 N'Một câu chuyện tình thanh xuân ngọt ngào nhưng đượm buồn giữa hai người bạn thân thời trung học tại thành phố biển Đà Nẵng. Sau 10 năm gặp lại, những kỷ niệm xưa thức dậy cùng ngã rẽ cuộc đời.', 
 112, 
 DATEADD(day, -10, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 20, CAST(GETDATE() AS DATE)), 
 4.4, 'T13', 
 N'Nguyễn Quang Dũng', 
 N'Kaity Nguyễn, Avin Lu, Hoàng Hà, Trần Nghĩa', 
 'https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=d9MyW72ELq0', 
 1),

(5, 
 N'Ngôi Nhà Cổ Trong Sương Mù', 
 N'Đôi vợ chồng trẻ thừa kế một dinh thự cổ kính ở vùng cao nguyên Lâm Đồng. Nhưng màn sương dày đặc buông xuống mỗi đêm cũng là lúc những âm thanh cào cấu và bóng đen bí ẩn xuất hiện khắp các căn phòng.', 
 104, 
 DATEADD(day, -5, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 30, CAST(GETDATE() AS DATE)), 
 4.2, 'T18', 
 N'James Wan', 
 N'Quách Ngọc Ngoan, Maya, Huỳnh Anh, Khả Như', 
 'https://images.unsplash.com/photo-1509198397868-475647b2a1e5?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=k10ETZ41q5o', 
 1),

(6, 
 N'Đặc Vụ Ngầm: Bản Hợp Đồng Bóng Ma', 
 N'Một điệp viên bị chính tổ chức của mình phản bội phải chạy đua với thời gian để bảo vệ chiếc chìa khóa mã hóa chứa danh sách toàn bộ mật vụ ngầm toàn cầu trước khi rơi vào tay mạng lưới khủng bố.', 
 135, 
 DATEADD(day, -12, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 22, CAST(GETDATE() AS DATE)), 
 4.5, 'T16', 
 N'Chad Stahelski', 
 N'Keanu Reeves, Donnie Yen, Hiroyuki Sanada, Bill Skarsgård', 
 'https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=qEVUtrk8_B4', 
 1),

(7, 
 N'Gia Đình Hoàn Hảo: Bí Mật Sau Cánh Cửa', 
 N'Một gia đình trung lưu mẫu mực bất ngờ rơi vào khủng hoảng khi người con trai lớn phát hiện ra những bí mật tài chính đen tối của cha mình và sự thật về vụ án mạng 15 năm trước.', 
 118, 
 DATEADD(day, -3, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 28, CAST(GETDATE() AS DATE)), 
 4.3, 'T16', 
 N'Bong Joon-ho', 
 N'Song Kang-ho, Choi Woo-shik, Park So-dam', 
 'https://images.unsplash.com/photo-1478720568477-152d9b164e26?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=5xH0Hf1VU4Y', 
 1),

(8, 
 N'Cuộc Đua Siêu Cấp: Đường Chân Trời', 
 N'Những quái xế hàng đầu hội tụ tại giải đua xe ngầm xuyên lục địa nguy hiểm nhất hành tinh. Không chỉ cạnh tranh về tốc độ, các tay đua còn phải đối mặt với cạm bẫy sinh tử từ đối thủ.', 
 128, 
 DATEADD(day, -8, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 25, CAST(GETDATE() AS DATE)), 
 4.1, 'T13', 
 N'Justin Lin', 
 N'Vin Diesel, Michelle Rodriguez, Jason Statham', 
 'https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=aSiDu3Ywi8E', 
 1),

-- === NHÓM 2: PHIM SẮP CHIẾU (COMING SOON) ===
(9, 
 N'Kỷ Nguyên Robot: Bình Minh Trỗi Dậy', 
 N'Vào năm 2150, khi trí tuệ nhân tạo đạt điểm bùng nổ, các người máy thế hệ mới yêu cầu quyền công dân độc lập. Một vụ bạo loạn nổ ra đe dọa sự sinh tồn của loài người trên địa cầu.', 
 140, 
 DATEADD(day, 7, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 45, CAST(GETDATE() AS DATE)), 
 4.9, 'T13', 
 N'Gareth Edwards', 
 N'John David Washington, Gemma Chan, Ken Watanabe', 
 'https://images.unsplash.com/photo-1485827404703-89b55fcc595e?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=ex3C1-5Dhb8', 
 1),

(10, 
 N'Thế Giới Phép Thuật: Học Viện Ánh Sáng', 
 N'Phần phim ma thuật huyền ảo theo chân cô bé mồ côi Lyra khám phá ra dòng máu phù thủy cổ xưa trong mình và bước vào học viện pháp thuật bí mật sâu dưới lòng London cổ kính.', 
 130, 
 DATEADD(day, 14, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 50, CAST(GETDATE() AS DATE)), 
 4.7, 'P', 
 N'David Yates', 
 N'Eddie Redmayne, Jude Law, Mads Mikkelsen', 
 'https://images.unsplash.com/photo-1514306191717-452ec28c7814?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=Y9dr2zw-TXQ', 
 1),

(11, 
 N'Ác Mộng Đêm Trăng Máu', 
 N'Một nhóm sinh viên cắm trại tại một ngôi làng bỏ hoang bị nguyền rủa đúng vào đêm thiên thực toàn phần. Từng người một phải đối diện với nỗi sợ hãi tột cùng sâu thẳm trong tâm trí.', 
 98, 
 DATEADD(day, 10, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 40, CAST(GETDATE() AS DATE)), 
 4.3, 'T18', 
 N'Ari Aster', 
 N'Florence Pugh, Jack Reynor, William Jackson Harper', 
 'https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=1Vnghdsjmd0', 
 1),

(12, 
 N'Vút Bay: Chuyến Phiêu Lưu Kỳ Diệu', 
 N'Siêu phẩm hoạt hình gia đình đưa các bé theo chân chú chim cánh cụt Pippin muốn học bay và quyết định chế tạo chiếc khinh khí cầu vượt đại dương đến vùng nhiệt đới ngập tràn nắng ấm.', 
 88, 
 DATEADD(day, 20, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 60, CAST(GETDATE() AS DATE)), 
 4.6, 'P', 
 N'Pete Docter', 
 N'Lồng tiếng: Thành Lộc, Cát Phượng, Hữu Châu', 
 'https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=ORFWdXl_zJ4', 
 1),

(13, 
 N'Lời Hẹn Ước Dưới Tán Hoa Anh Đào', 
 N'Một chuyện tình cảm động giữa một họa sĩ trẻ tài hoa và một cô gái mắc bệnh hiểm nghèo tại Kyoto cổ kính. Họ cùng nhau viết nên những trang nhật ký cuối cùng rực rỡ sắc hoa.', 
 115, 
 DATEADD(day, 18, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 50, CAST(GETDATE() AS DATE)), 
 4.5, 'P', 
 N'Makoto Shinkai', 
 N'Ryunosuke Kamiki, Mone Kamishiraishi', 
 'https://images.unsplash.com/photo-1522383225653-ed111181a951?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=xU47nhruN-Q', 
 1),

(14, 
 N'Hố Đen Tử Thần: Xuyên Không Gian', 
 N'Khi Trạm Không Gian Quốc tế gặp sự cố gần biên giới lỗ sâu vũ trụ, phi hành đoàn phải tính toán để thoát khỏi lực hút khổng lồ và bẻ cong dòng thời gian để tìm đường trở về.', 
 155, 
 DATEADD(day, 25, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 65, CAST(GETDATE() AS DATE)), 
 4.8, 'T13', 
 N'Christopher Nolan', 
 N'Matthew McConaughey, Anne Hathaway, Jessica Chastain', 
 'https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=zSWdZVtXT7E', 
 1),

(15, 
 N'Vụ Cướp Thế Kỷ Tại Casino Monaco', 
 N'Nhóm siêu trộm lừng danh quy tụ để thực hiện phi vụ táo bạo nhất lịch sử: Đánh cắp kho kim cương trị giá 500 triệu Euro trong đêm gala kỷ niệm 100 năm sòng bạc lớn nhất châu Âu.', 
 122, 
 DATEADD(day, 12, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 42, CAST(GETDATE() AS DATE)), 
 4.4, 'T16', 
 N'Steven Soderbergh', 
 N'George Clooney, Brad Pitt, Matt Damon', 
 'https://images.unsplash.com/photo-1511193311914-0346f16efe90?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=u7JMhG5cfCw', 
 1),

(16, 
 N'Chuyến Xe Bus Quái Đản', 
 N'Một nhóm hành khách bất đắc dĩ bị kẹt trên chuyến xe buýt chạy vào ban đêm ở vùng ngoại ô heo hút khi tài xế bỗng nhiên mất tích và con đường dường như lặp lại vô tận.', 
 92, 
 DATEADD(day, 30, CAST(GETDATE() AS DATE)), 
 DATEADD(day, 60, CAST(GETDATE() AS DATE)), 
 4.1, 'T16', 
 N'Jordan Peele', 
 N'Daniel Kaluuya, Keke Palmer, Steven Yeun', 
 'https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=800&auto=format&fit=crop&q=80', 
 'https://www.youtube.com/watch?v=InqQ6P39dKk', 
 1);

SET IDENTITY_INSERT movies OFF;
GO

-- 3. Gán thể loại cho phim (movie_genres)
INSERT INTO movie_genres (movie_id, genre_id) VALUES
-- Phim 1: Chiến Binh Sấm Sét (Hành động, Phiêu lưu, Viễn tưởng)
(1, 1), (1, 2), (1, 7),
-- Phim 2: Hành Tinh Băng Giá (Viễn tưởng, Phiêu lưu, Giật gân)
(2, 7), (2, 2), (2, 9),
-- Phim 3: Biệt Đội Thú Cưng (Hoạt hình, Hài hước)
(3, 3), (3, 4),
-- Phim 4: Mùa Hè Năm Ấy (Tình cảm, Tâm lý)
(4, 6), (4, 8),
-- Phim 5: Ngôi Nhà Cổ Trong Sương Mù (Kinh dị, Giật gân)
(5, 5), (5, 9),
-- Phim 6: Đặc Vụ Ngầm (Hành động, Giật gân)
(6, 1), (6, 9),
-- Phim 7: Gia Đình Hoàn Hảo (Tâm lý, Giật gân)
(7, 8), (7, 9),
-- Phim 8: Cuộc Đua Siêu Cấp (Hành động)
(8, 1),
-- Phim 9: Kỷ Nguyên Robot (Viễn tưởng, Hành động)
(9, 7), (9, 1),
-- Phim 10: Thế Giới Phép Thuật (Phiêu lưu, Hoạt hình)
(10, 2), (10, 3),
-- Phim 11: Ác Mộng Đêm Trăng Máu (Kinh dị)
(11, 5),
-- Phim 12: Vút Bay (Hoạt hình, Hài hước)
(12, 3), (12, 4),
-- Phim 13: Lời Hẹn Ước (Tình cảm, Tâm lý)
(13, 6), (13, 8),
-- Phim 14: Hố Đen Tử Thần (Viễn tưởng, Phiêu lưu)
(14, 7), (14, 2),
-- Phim 15: Vụ Cướp Thế Kỷ (Hành động, Giật gân)
(15, 1), (15, 9),
-- Phim 16: Chuyến Xe Bus Quái Đản (Kinh dị, Hài hước)
(16, 5), (16, 4);
GO

PRINT N'>>> Đã khởi tạo thành công dữ liệu mẫu cho MovieBrowsingDB!';
GO
