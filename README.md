# HỆ THỐNG QUẢN LÝ CỤM RẠP CHI NHÁNH TOÀN QUỐC (SWP391 - Nhóm 3)
> **MULTI-BRANCH CINEMA CHAIN ENTERPRISE SYSTEM**  
> **Lớp:** SE2056-JV | **Học kỳ:** Fall 2024 / Spring 2025  
> **Kiến trúc:** Package-by-Feature kết hợp Layered Architecture (DDD-Lite)  
> **Nền tảng công nghệ:** Java 17 LTS, Jakarta EE 10 (Servlet 5.0, JSP 3.0, JSTL 2.0), MySQL 8.0 (InnoDB), HikariCP 5.1.0, Apache Tomcat 10.1.x

---

## 1. PHÂN CÔNG PHẠM VI TRÁCH NHIỆM & RANH GIỚI TỪNG THÀNH VIÊN (ZERO CONFLICT)

| Vị trí | Phụ trách Zone / Bounded Context | Package Backend (`com.cinema.modules.*`) | Thư mục Giao diện (`WEB-INF/views/`) | Bảng CSDL Làm Chủ (28 Bảng 3NF) |
| :--- | :--- | :--- | :--- | :--- |
| **TV 1 (Leader)** | **Infrastructure & Cinema Management** | `modules.infrastructure` | `admin/infrastructure/` | `branches`, `screening_halls`, `seat_types`, `seats`, `system_logs`, `maintenance_schedules` |
| **TV 2** | **Identity, Auth & Membership** | `modules.identity` | `customer/account/`, `admin/users/` | `roles`, `membership_tiers`, `users`, `point_histories`, `customer_vouchers` |
| **TV 3** | **Movies, Showtimes & Dynamic Pricing** | `modules.catalog` | `customer/catalog/`, `admin/catalog/` | `genres`, `movies`, `movie_genres`, `showtimes`, `ticket_pricings`, `reviews` |
| **TV 4** | **Core Booking Engine & Payment** | `modules.booking` | `customer/booking/`, `admin/booking/` | `vouchers`, `seat_holdings`, `bookings`, `tickets`, `order_items`, `payments` |
| **TV 5** | **POS Counter, F&B & Check-in** | `modules.operation` | `admin/operation/` | `fnb_categories`, `fnb_items`, `branch_inventories`, `ticket_checkin_logs`, `cash_drawers` |

---

## 2. NGUYÊN TẮC THIẾT KẾ & BẢO MẬT BẮT BUỘC

1. **Khóa cứng `web.xml` (Zero Merge Conflict):**  
   - Tuyệt đối **KHÔNG** khai báo thẻ `<servlet>` hoặc `<servlet-mapping>` trong `web.xml`.
   - 100% Servlet được định tuyến bằng annotation `@WebServlet(name = "...", urlPatterns = {"/path"})`.
2. **Quyền riêng tư DAO (Strict Private DAO):**  
   - Các class DAO chỉ có phạm vi **package-private** (`class BranchDAOImpl`) hoặc chỉ được gọi nội bộ bên trong Service của module đó.
   - Module khác muốn lấy dữ liệu BẮT BUỘC phải gọi qua **Public Service Interface** (ví dụ: `ScreeningHallService`, `MovieService`, `BookingEngineService`).
3. **Quản lý Transaction An toàn Lồng nhau (`TransactionManager`):**  
   - Tất cả nghiệp vụ ghi nhiều bảng (đặc biệt là tạo đơn `Booking` + `Tickets` + `OrderItems` + `Payments`) phải được bọc trong `TransactionManager.executeInTransaction(...)`.
   - Cơ chế `depthHolder` bảo đảm không bị commit sớm khi Service A gọi lồng Service B.
4. **Bảo mật mật khẩu:**  
   - Toàn bộ mật khẩu người dùng được băm một chiều bằng Salted BCrypt (12 rounds) thông qua `PasswordUtil`.

---

## 3. HƯỚNG DẪN CÀI ĐẶT CƠ SỞ DỮ LIỆU (DATABASE SETUP)

### Bước 1: Khởi tạo CSDL MySQL 8.0+
Chạy tuần tự 3 script SQL nằm trong thư mục `src/main/resources/sql/`:

```bash
# 1. Khởi tạo Schema 28 bảng 3NF InnoDB chuẩn chỉ
mysql -u root -p < src/main/resources/sql/01_schema.sql

# 2. Nạp dữ liệu danh mục lõi (Roles, Tiers, Seat Types, Genres, Admin account)
mysql -u root -p < src/main/resources/sql/02_seed_master_data.sql

# 3. Nạp dữ liệu mẫu chi nhánh, phòng chiếu, 400 ghế, phim, suất chiếu, bắp nước
mysql -u root -p < src/main/resources/sql/03_seed_sample_data.sql
```

Hoặc bạn có thể mở công cụ **MySQL Workbench** / **DBeaver** / **Navicat**, mở từng file và nhấn **Execute (Run All)** theo thứ tự từ `01` -> `02` -> `03`.

### Bước 2: Cấu hình kết nối DB (`db.properties`)
Mở file `src/main/resources/db.properties` hoặc tạo file `src/main/resources/db.local.properties` để ghi đè thông tin kết nối máy cá nhân:

```properties
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/cinema_chain_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=UTF-8
db.username=root
db.password=your_mysql_password
db.pool.maximumPoolSize=20
```

---

## 4. TÀI KHOẢN MẪU KHỞI TẠO SẴN ĐỂ ĐĂNG NHẬP KIỂM THỬ

> **Mật khẩu mặc định cho tất cả tài khoản bên dưới:** `123456`

| Email Đăng Nhập | Họ & Tên | Vai Trò (Role) | Chi Nhánh Trực Thuộc | Ghi chú quyền hạn |
| :--- | :--- | :--- | :--- | :--- |
| `admin@cinema.com` | Hệ Thống Quản Trị Viên | **ADMIN** | Toàn hệ thống | Quản trị tối cao, cấu hình rạp, tài khoản |
| `manager.hn@cinema.com` | Trần Quản Lý Hà Nội | **MANAGER** | Chi nhánh Vincom Bà Triệu | Quản lý phòng chiếu, lịch chiếu rạp HN |
| `staff.hn@cinema.com` | Lê Thu Ngân Viên Hà Nội | **STAFF** | Chi nhánh Vincom Bà Triệu | Thu ngân quầy vé POS & Quét mã QR soát vé |
| `staff.sg@cinema.com` | Phạm Thu Ngân Sài Gòn | **STAFF** | Chi nhánh Landmark 81 | Thu ngân quầy vé POS & Quét mã QR soát vé |
| `customer@gmail.com` | Nguyễn Văn Khách Hàng | **CUSTOMER** | Toàn quốc (Online) | Đã có sẵn 250 điểm loyalty & 2 mã Voucher |

---

## 5. CÁCH BUILD VÀ KHỞI CHẠY DỰ ÁN

### Cách 1: Build gói WAR bằng Maven CLI
```bash
# Di chuyển vào thư mục dự án
cd SWP391-Gr3

# Biên dịch và đóng gói file .war
mvn clean package -DskipTests
```
File WAR đầu ra sẽ được tạo tại: `target/SWP391-Gr3.war`.

### Cách 2: Triển khai trên Apache Tomcat 10.1.x
1. **IntelliJ IDEA Ultimate / Eclipse:**
   - Chọn **Add Configuration** -> **Tomcat Server** -> **Local**.
   - Trỏ đường dẫn Application Server tới thư mục cài đặt **Apache Tomcat 10.1.x**.
   - Trong tab **Deployment**, bấm `+` -> Chọn `SWP391-Gr3:war exploded` (hoặc file WAR).
   - Application context đặt là: `/` hoặc `/cinema`.
   - Nhấn **Run / Debug** (Shift + F10).
2. **Truy cập trình duyệt:**
   - Trang chủ người dùng: `http://localhost:8080/`
   - Đăng nhập: `http://localhost:8080/auth/login`
   - Quản trị chi nhánh (Leader/Admin): `http://localhost:8080/admin/branches`
   - Quầy POS bán vé tại rạp: `http://localhost:8080/pos/counter`
   - Quét mã QR soát vé: `http://localhost:8080/pos/scanner`

---

## 6. QUY TẮC PHÁT TRIỂN & QUẢN TRỊ GIT NHÓM

1. **Nhánh chính (Protected):**
   - `main`: Nhánh production ổn định để demo với giảng viên.
   - `develop`: Nhánh tích hợp chung của cả 5 thành viên.
2. **Nhánh tính năng thành viên (Feature Branches):**
   - TV 1: `feature/tv1-infrastructure-*`
   - TV 2: `feature/tv2-auth-user-*`
   - TV 3: `feature/tv3-movies-showtimes-*`
   - TV 4: `feature/tv4-booking-engine-*`
   - TV 5: `feature/tv5-pos-fnb-checkin-*`
3. **Quy trình gửi Pull Request (PR):**
   - Mỗi thành viên chỉ commit trong thư mục module backend và views frontend của mình.
   - Chỉ được merge vào `develop` khi chạy lệnh `mvn clean compile` thành công và không xung đột mã nguồn.
