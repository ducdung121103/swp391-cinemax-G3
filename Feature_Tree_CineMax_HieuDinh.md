# CineMax — Multi-Branch Cinema Management System
## FEATURE TREE — v2.2 (Final Definition - Bản hoàn chỉnh)

> ### 📌 Vai trò của tài liệu này
> Đây là **nguồn chân lý duy nhất (Single Source of Truth)** về phạm vi nghiệp vụ và actor sở hữu cho toàn bộ dự án CineMax, thay thế hoàn toàn:
> - Bản Feature Tree gốc (`0__SE2056-JV_-_Group_3__-_Feature_Tree.docx`) — chỉ giữ giá trị tham khảo lịch sử, không còn dùng để quyết định phạm vi.
> - Cây 14 nhánh MBCMS (bản phác thảo phân việc) — chỉ dùng cho việc chia task trong nhóm (xem `Phan_Viec_MBCMS_Mapping_Code_That.md`), **không dùng để quyết định ai làm UC nào**.
>
> Mọi UC Diagram, Sequence Diagram, Activity Diagram, Permission Matrix, và việc scaffold code/DB đều phải đối chiếu và trỏ về đúng 1 dòng cụ thể trong văn bản này. Khi có mâu thuẫn giữa tài liệu khác và văn bản này — **văn bản này luôn thắng**.

---

## ✅ CÁC QUYẾT ĐỊNH NGHIỆP VỤ ĐÃ CHỐT DỨT KHOÁT (v2.2)

Dự án đề cao nghiệp vụ sâu, luồng dữ liệu chuẩn chỉ và tính toàn vẹn (ACID, Concurrency) hơn độ phủ tính năng dàn trải. Nhóm đã thống nhất chốt hạ toàn bộ các điểm phân vân sau:

| # | Hạng mục nghiệp vụ | Quyết định dứt khoát v2.2 | Rationale & Cơ chế chuẩn hóa |
|---|---|---|---|
| 1 | **Vận hành ca làm việc (Staff Shifts & Cash Drawer)** | ❌ **Cắt bỏ hoàn toàn** | Không triển khai phân ca (`Assign Staff Shifts`), mở/đóng két tiền (`Cash Drawer`) hay báo cáo ca riêng lẻ. Doanh số & giao dịch ghi nhận trực tiếp theo `staff_id` + `timestamp` của từng hóa đơn trong ngày. Cinema Staff còn đúng **9 Use Case sạch**. |
| 2 | **Chỉnh sửa đơn vé sau thanh toán (`Modify booking`)** | ❌ **Loại bỏ hoàn toàn** | Chặn đứng rủi ro Race Condition, chênh lệch giá vé và hoàn tiền phức tạp. Thay thế bằng: **Chỉ được Hủy vé có điều kiện (`Cancel booking (conditional)`)** trước giờ chiếu tối thiểu X tiếng, tự động hoàn tiền và giải phóng ghế. |
| 3 | **Chiết khấu tại quầy vé POS (`Discounts`)** | ⚖️ **Chuẩn hóa thành Giá ưu đãi theo đối tượng (`Concession Pricing`)** | Cắt bỏ Voucher/Promotion/Loyalty. Tại quầy, Staff chỉ áp dụng bảng giá định sẵn cấu hình tại M-03.2 (Vé HSSV, Người cao tuổi) khi khách xuất trình giấy tờ hợp lệ, không nhập mã giảm giá tự do. |
| 4 | **Đồng bộ Tồn kho Bắp nước (F&B Real-time Stock)** | 🔒 **Khép kín Real-time & Auto Out of Stock** | Trừ kho tự động theo thời gian thực ngay khi thanh toán thành công (Online / POS). Khi tồn kho = 0, hệ thống tự động khóa/ẩn món trên cả Web và Counter để chống bán âm kho. |
| 5 | **Voucher / Promotion** | ❌ **Cắt hẳn khỏi phạm vi** | Gỡ bỏ UI và các Use Case liên quan đến mã voucher/khuyến mãi. |
| 6 | **Loyalty & Membership Tiers** | ❌ **Cắt hẳn khỏi phạm vi** | Không triển khai UI tích/tiêu điểm thưởng, không thăng hạng thành viên. |

---

**Chú giải Ký hiệu Actor:**
| Ký hiệu | Actor | Mô tả vai trò |
|:---:|---|---|
| 🟣 | **Guest** | Khách vãng lai, chưa đăng nhập hệ thống |
| 🟢 | **Customer** | Khách hàng đã đăng ký/đăng nhập tài khoản |
| 🟡 | **Cinema Staff** | Nhân viên quầy vé & soát vé tại rạp |
| 🔵 | **Cinema Manager** | Quản lý vận hành tại 1 chi nhánh rạp cụ thể |
| 🔴 | **Administrator** | Quản trị viên tối cao toàn chuỗi rạp |
| ⚙️ | **System** | Hệ thống tự động thực thi ngầm (Background Job, Event Listener, Webhook) |

---

## M-01. Account & Authentication Management

### M-01.1 Guest & Public Access — 🟣 Guest
- Browse movies (Now Showing & Coming Soon)
- View movie details & trailers
- Search and filter movies
- View cinema branches
- View showtimes & seat availability
- Register account (enter info, set password, email/phone OTP verification)

### M-01.2 Authentication & Security
- Login (password hashed with BCrypt) — 🟣🟢🟡🔵🔴 *(mọi actor có tài khoản)*
- Optional Two-Factor Authentication (2FA) via Email OTP — 🟢🟡🔵🔴 *(chỉ áp dụng sau khi đã có tài khoản)*
- Logout (session/token invalidation) — 🟢🟡🔵🔴
- Forgot/reset password (email token/OTP, ~15-minute expiry) — 🟢🟡🔵🔴
- Email verification — 🟣 *(diễn ra trong lúc đăng ký, trước khi trở thành Customer)*
- Role-based access control (RBAC matrix: **Admin / Cinema Manager / Cinema Staff / Customer**) — ⚙️ System

### M-01.3 Customer Profile Management — 🟢 Customer
- View/edit profile
- Change password

### M-01.4 Staff & User Administration — 🔴 Administrator
- View user accounts
- Manage user accounts & roles
- Assign / transfer staff & manager to branch
- Activate/deactivate accounts (guard against an Admin locking themselves out)

---

## M-02. Movie & Review Management

### M-02.1 Movie Catalog — 🔴 Administrator
- Add movie (title, genre, duration, age rating, synopsis, poster, trailer)
- Edit movie information
- Delete/archive movie (block deletion while active bookings exist)
- Manage genres, languages, duration
- Manage movie status (Coming Soon, Now Showing, Ended)

### M-02.2 Reviews & Ratings
- Rate movies (1–5 star scale) — 🟢 Customer
- Write / edit / delete reviews — 🟢 Customer
- View reviews & average rating — 🟣🟢 *(công khai)*
- Moderate reviews (profanity filtering, hide inappropriate comments) — 🔴 Administrator

---

## M-03. Showtime & Pricing Management

### M-03.1 Showtime Scheduling — 🔵 Cinema Manager
- Create showtime (select movie, hall, date/time) — *việc chọn phim cho suất chiếu này đồng thời là bước "gán phim cho rạp"; không cần quy trình duyệt riêng, vì một phim chỉ thật sự "có mặt" tại 1 rạp khi rạp đó tạo suất chiếu cho phim*
- Edit showtime (restricted once tickets have been sold)
- Cancel showtime (automatic refund & customer notification)
- Prevent schedule conflicts (overlap check, ~15-minute cleaning buffer between screenings)

### M-03.2 Ticket Pricing
- Set standard ticket price — 🔴 Administrator *(giá mặc định áp dụng toàn chuỗi)*
- Configure prices by seat type — 🔴 Administrator
- Configure prices by hall/format (2D, 3D, IMAX) — 🔴 Administrator
- Configure time-based pricing (weekday/weekend/holiday/sneak show) — 🔴 Administrator
- Configure concession pricing templates (Standard, Student/U22, Senior) — 🔴 Administrator
- Manage cinema-specific pricing — 🔵 Cinema Manager *(ghi đè giá mặc định cho riêng chi nhánh mình)*

### M-03.3 Showtime Monitoring — 🔵 Cinema Manager
- View showtime schedule
- Track seat occupancy
- Monitor booking status
- Showtime reminder / change / cancellation notification (triggers Notification module) — ⚙️ System

---

## M-04. Cinema, Room & Seat Management

### M-04.1 Cinema Management — 🔴 Administrator
- Add/edit/delete cinema (name, address, hotline, coordinates)
- Manage cinema status & operating hours

### M-04.2 Room Management — 🔴 Administrator
- Add/edit/delete room
- Configure room capacity
- Configure room type (Standard, VIP, IMAX/Other)

### M-04.3 Seat Management
- Configure seat layout (Grid Designer: A–Z × 1–N matrix) — 🔴 Administrator
- Manage seat types (Standard, VIP, Sweetbox/Couple) — 🔴 Administrator
- Set seat maintenance status (hide faulty seats from every sales channel) — 🔴 Administrator *(toàn quyền)* / 🔵 Cinema Manager *(giới hạn trong phạm vi rạp mình)*
- View seat availability — 🟣🟢🟡 *(công khai lúc đặt vé / tại quầy)*

---

## M-05. Booking & Seat Reservation Management

### M-05.1 Online Booking Flow — 🟢 Customer
- Select cinema / movie / showtime / seats
- Add tickets to booking
- Select F&B combo during booking (with real-time stock verification)
- Confirm booking (ends here — ticket/QR issuance belongs to M-07, not this module)

### M-05.2 Counter Booking Flow — 🟡 Cinema Staff
- Search showtimes at the counter
- Select seats, select F&B items
- Select concession ticket type (Standard / Student / Senior based on valid ID verification)
- Create booking (channel: POS, recorded under operator `staff_id`)
- Temporarily reserve seats (~5 minutes)

### M-05.3 Seat Reservation Lock — ⚙️ System
- Temporarily lock selected seats (Pessimistic Lock / Redis-DB lock, ~5 minutes, real-time countdown)
- Release expired seat reservations (background job auto-releases seats to AVAILABLE)
- Prevent double booking
- Confirm seat availability before payment gateway transition

### M-05.4 Booking Management — 🟢 Customer
- View booking details & history
- Track booking status (PENDING, PAID, CANCELLED, COMPLETED)
- Cancel booking (conditional) — *chỉ cho phép hủy trực tuyến khi thời điểm hiện tại cách giờ chiếu tối thiểu X tiếng (cấu hình hệ thống, VD: ≥ 2 tiếng); kích hoạt luồng hoàn tiền và giải phóng ghế*

---

## M-06. Payment Management

### M-06.1 Online Payment — 🟢 Customer *(khởi tạo)* / ⚙️ System *(xử lý)*
- Payment gateway integration (VNPay)
- Asynchronous payment webhook processing & IPN verification — ⚙️ System
- Payment confirmation / failure handling & transaction rollback — ⚙️ System

### M-06.2 Counter Payment — 🟡 Cinema Staff
- Process counter payment (Cash, VNPay QR)
- Confirm payment receipt (recorded with `staff_id` and timestamp)
- Print physical receipt / ticket

### M-06.3 Payment Tracking — 🟢 Customer *(lịch sử của mình)* / 🔴 Administrator *(toàn chuỗi)*
- View payment history, track payment status
- Generate electronic invoices/receipts

---

## M-07. Ticket & Entry Management

### M-07.1 Ticket Issuance
- Generate ticket (QR code signed with HMAC-SHA256) — ⚙️ System *(tự động ngay sau khi M-06 xác nhận thanh toán thành công)*
- View ticket details — 🟢 Customer
- Reissue ticket / cancel ticket (authorized rollback) — 🟡 Cinema Staff
- Look up & reprint ticket by booking code or customer phone number — 🟡 Cinema Staff
- Export/download e-ticket (PDF / QR image) — 🟢 Customer

### M-07.2 Ticket Validation (Gate Check-in) — 🟡 Cinema Staff
- Scan QR ticket (phone camera / barcode scanner)
- Validate ticket (correct showtime, correct screening hall, ticket status = PAID)
- Prevent duplicate entry (flip ticket status to USED, block re-entry attempts)
- Record attendance log (timestamp, gate validator `staff_id`)
- Verify customer age against ID for age-restricted movies (T13/T16/T18) & student card for concession tickets

---

## M-08. Food & Beverage Management

### M-08.1 F&B Catalog & Stock
- Add/edit/delete food & drink items — 🔴 Administrator
- Manage categories, base pricing — 🔴 Administrator
- Manage branch inventory stock (`stock_quantity`) — 🔵 Cinema Manager
- Deduct stock in real-time upon successful payment (Online / POS) — ⚙️ System
- Auto-flag Out of Stock (disable adding to cart when `stock_quantity <= 0`) — ⚙️ System

### M-08.2 F&B Ordering — 🟢 Customer *(online)* / 🟡 Cinema Staff *(tại quầy)*
- Browse menu, select items/combos (real-time stock availability check)
- Add F&B to booking (writes into `order_items`, `item_type = 'FNB'`)
- Track order delivery / pick-up status at the counter

---

## M-09. Notification Management — ⚙️ System *(gửi tự động, người nhận là Customer)*

### M-09.1 Booking & Payment Notifications
- Booking confirmation (with e-ticket QR code attachment)
- Payment confirmation & transaction reference
- Cancellation & refund status notification

### M-09.2 Showtime Notifications
- Showtime reminder (sent X hours prior to screening)
- Showtime schedule change / cancellation alert

---

## M-10. Reporting & Analytics

### M-10.1 Sales Reports — 🔴 Administrator
- Ticket sales, box office revenue, F&B revenue, payment method breakdowns
- Export reports to Excel (.xlsx) & PDF

### M-10.2 Branch Reports — 🔴 Administrator *(toàn chuỗi)* / 🔵 Cinema Manager *(chi nhánh mình)*
- Branch revenue, ticket sales, hall occupancy, operational performance
- Chain-wide revenue & performance comparison across branches — 🔴 Administrator *(chỉ Admin có thẩm quyền so sánh liên chi nhánh)*

### M-10.3 Operation Reports — 🔴 Administrator / 🔵 Cinema Manager *(chi nhánh mình)*
- Hall occupancy rate, popular movies/showtimes, peak booking periods
- Ticket attendance rate, demographic statistics & peak hour traffic

---

## M-11. System Administration — 🔴 Administrator
- View all branches, monitor branch operational status, system-wide overview
- Configure global system parameters (seat hold timeout = 5 mins, cleaning buffer = 15 mins, cancellation deadline = 2 hours...)

---

## 📊 BẢNG TỔNG HỢP — ĐỐI CHIẾU NHANH FEATURE TREE ↔ USE CASE DIAGRAM

Bảng này đóng vai trò **Single Source of Truth** để nghiệm thu các Use Case Diagram của từng Actor. Mọi Use Case trên sơ đồ thiết kế UML bắt buộc phải tương ứng đúng 1 dòng trong bảng này.

### 🔴 Administrator (11 Use Cases — Chuẩn hóa)
| # | Use Case trên sơ đồ UML | Mã Feature Tree | Ghi chú phạm vi |
|:---:|---|---|---|
| 1 | Manage User, Staff & Manager Accounts | M-01.4 | Toàn quyền CRUD tài khoản & phân quyền |
| 2 | Manage Master Movie Catalog | M-02.1 | Kho phim dùng chung toàn hệ thống |
| 3 | Manage Cinema Branches & Halls | M-04.1, M-04.2 | Quản lý cụm rạp và phòng chiếu |
| 4 | Configure Seat Grid Designer | M-04.3 | Ma trận sơ đồ ghế (A-Z x 1-N) |
| 5 | Configure Default Pricing Template | M-03.2 | Bảng giá gốc (tiêu chuẩn, đối tượng, định dạng) |
| 6 | Manage Global F&B Catalog | M-08.1 | Danh mục món & giá bắp nước toàn hệ thống |
| 7 | Moderate User Reviews | M-02.2 | Kiểm duyệt bình luận/đánh giá người dùng |
| 8 | Generate Enterprise BI Reports | M-10.1, M-10.2 | Báo cáo doanh thu & so sánh liên rạp |
| 9 | System Dashboard & Global Parameters | M-11 | Cấu hình tham số hệ thống toàn cục |
| 10 | Toggle Seat Maintenance Status | M-04.3 | Đóng/mở bảo trì ghế toàn hệ thống |
| 11 | User Logout | M-01.2 | Đăng xuất tài khoản an toàn |

---

### 🔵 Cinema Manager (7 Use Cases — Đã chốt, sạch)
| # | Use Case trên sơ đồ UML | Mã Feature Tree | Ghi chú phạm vi |
|:---:|---|---|---|
| 1 | Schedule Branch Showtimes | M-03.1 | Lập lịch chiếu tại chi nhánh mình quản lý |
| 2 | Configure Branch Pricing & Rates | M-03.2 | Tùy biến giá riêng cho chi nhánh |
| 3 | Monitor Hall Occupancy | M-03.3 | Theo dõi tỷ lệ lấp đầy phòng chiếu |
| 4 | Manage Branch F&B Stock | M-08.1 | Kiểm kê và cập nhật kho bắp nước chi nhánh |
| 5 | Toggle Seat Maintenance Status | M-04.3 | Khóa ghế hỏng trong phạm vi rạp mình |
| 6 | View Branch Revenue & Sales Reports | M-10.2, M-10.3 | Báo cáo doanh số & vận hành chi nhánh |
| 7 | User Logout | M-01.2 | Đăng xuất tài khoản an toàn |

*(Đã cắt hoàn toàn "Assign Staff Shifts" — không quản lý ca làm việc phức tạp).*

---

### 🟡 Cinema Staff (Đúng 9 Use Cases — Đã chốt, sạch)
| # | Use Case trên sơ đồ UML | Mã Feature Tree | Ghi chú phạm vi & Chuẩn hóa v2.2 |
|:---:|---|---|---|
| 1 | Sell Tickets at Counter | M-05.2 | Tìm suất chiếu, giữ ghế, áp dụng giá ưu đãi đối tượng |
| 2 | Order F&B at Counter | M-08.2 | Chọn bắp nước, kiểm tra tồn kho tại quầy |
| 3 | Process Counter Payment | M-06.2 | Thu tiền mặt / quét QR VNPay (ghi nhận theo `staff_id`) |
| 4 | Print Ticket | M-06.2 | In vé cứng / hóa đơn cho khách |
| 5 | Validate QR Ticket | M-07.2 | Quét mã QR soát vé tại cửa phòng chiếu |
| 6 | Verify Age & Concession Eligibility | M-07.2 | Kiểm tra CCCD (T13/16/18) và thẻ HSSV |
| 7 | Reissue / Cancel Ticket | M-07.1 | Hủy vé sự cố hoặc cấp lại vé hợp lệ |
| 8 | Look Up & Reprint Ticket | M-07.1 | Tra cứu đơn hàng theo SĐT / mã đặt vé và in lại |
| 9 | User Logout | M-01.2 | Đăng xuất tài khoản an toàn |

*(Đã loại bỏ hoàn toàn 2 Use Case lơ lửng: `Manage Shift Cash Drawer` và `View Personal Shift Report`. Mọi giao dịch được gán trực tiếp với `staff_id` và timestamp của nhân viên thực hiện).*

---

### 🟢 Customer (Các Use Case bị loại bỏ & Chuẩn hóa)
| Use Case liên quan | Trạng thái v2.2 | Giải thích nghiệp vụ |
|---|:---:|---|
| ~~Apply Promo Voucher~~ | ❌ **Đã xoá** | Cắt bỏ hoàn toàn module khuyến mãi/voucher. |
| ~~Manage Loyalty Points~~ | ❌ **Đã xoá** | Cắt bỏ hoàn toàn module tích/tiêu điểm thưởng. |
| ~~Modify Booking~~ | ❌ **Đã xoá** | Không cho phép sửa đổi ghế/suất chiếu sau khi đã thanh toán (chặn Race Condition & lệch giá). |
| Cancel Booking (Conditional) | ✅ **Giữ lại có điều kiện** | Cho phép hủy vé trước giờ chiếu tối thiểu X tiếng, tự động kích hoạt hoàn tiền và giải phóng ghế. |

---

## 📜 NHẬT KÝ PHIÊN BẢN (VERSION HISTORY)

### v1.0 — Hiệu đính lần đầu
- Thêm nhãn Actor cho toàn bộ tính năng.
- Bỏ bullet "Apply promotion/voucher ?" có dấu chấm hỏi.
- Gộp các thao tác phân bổ nhân sự vào M-01.4.
- Đánh số lại liên tục M-09 → M-10 → M-11.
- Thống nhất RBAC chỉ còn 1 role duy nhất tại rạp là **Cinema Staff**.

### v2.0 — Nâng cấp cấu trúc chính thức
- Xác lập vai trò "Single Source of Truth".
- Thêm bảng tổng hợp đối chiếu nhanh Feature Tree ↔ UC Diagram.
- Đưa các điểm phân vân vào khu vực chờ quyết định.

### v2.1 — Chốt cắt 3 nhánh ngoài phạm vi
- Cắt hẳn `Assign Staff Shifts` khỏi Actor Cinema Manager.
- Cắt hẳn `Voucher / Promotion` (loại bỏ cả code lẫn giao diện).
- Cắt hẳn `Loyalty & Membership` (loại bỏ tích điểm, hạng thẻ).
- Manager chốt sạch còn đúng 7 UC.

### v2.2 — Final Definition (Bản hoàn chỉnh - Single Source of Truth)
- **Chuẩn hóa Cinema Staff (11 UC → 9 UC sạch):** Xóa bỏ triệt để 2 Use Case `Manage Shift Cash Drawer` và `View Personal Shift Report`. Nhân viên không quản lý ca phức tạp, doanh số gắn liền với `staff_id` và timestamp giao dịch.
- **Chặn Race Condition & Lệch giá (M-05.4):** Xóa bỏ hoàn toàn tính năng `Modify booking`. Thay thế bằng quy tắc `Cancel booking (conditional)` (chỉ hủy trước giờ chiếu tối thiểu X tiếng, giải phóng ghế và hoàn tiền tự động).
- **Làm rõ ranh giới giá vé tại quầy (M-05.2):** Đổi hành động từ `Apply applicable discounts` thành `Select concession ticket type (Standard / Student / Senior)` căn cứ theo bảng giá định sẵn tại M-03.2 khi khách xuất trình giấy tờ hợp lệ, không nhập mã voucher.
- **Khép kín logic Tồn kho F&B (M-08.1 & M-08.2):** Bổ sung quy tắc trừ kho thời gian thực khi đơn hàng thanh toán thành công (Online / POS); tự động kích hoạt trạng thái `Out of Stock` (vô hiệu hóa nút thêm vào giỏ khi tồn kho = 0).
