# CineMax — Multi-Branch Cinema Management System
## FEATURE TREE — v2.1 (Văn bản chính thức)

> ### 📌 Vai trò của tài liệu này
> Đây là **nguồn chân lý duy nhất** về phạm vi và actor sở hữu cho toàn bộ dự án CineMax, thay thế hoàn toàn:
> - Bản Feature Tree gốc (`0__SE2056-JV_-_Group_3__-_Feature_Tree.docx`) — chỉ giữ giá trị tham khảo lịch sử, không còn dùng để quyết định phạm vi.
> - Cây 14 nhánh MBCMS (bản phác thảo phân việc) — chỉ dùng cho việc chia task trong nhóm (xem `Phan_Viec_MBCMS_Mapping_Code_That.md`), **không dùng để quyết định ai làm UC nào**.
>
> Mọi UC Diagram, Permission Matrix, và việc scaffold code (Bước 2–4 trong lộ trình 4 bước) đều phải đối chiếu và trỏ về đúng 1 dòng cụ thể trong văn bản này. Khi có mâu thuẫn giữa tài liệu khác và văn bản này — **văn bản này luôn thắng**.

---

## ✅ QUYẾT ĐỊNH ĐÃ CHỐT (v2.1) — 3 nhánh chính thức NGOÀI PHẠM VI dự án

Môn học đề cao nghiệp vụ/luồng/logic hơn độ phủ tính năng — nhóm quyết định **cắt hẳn 3 nhánh sau** để tập trung làm sâu các luồng còn lại, không dàn trải:

| # | Nhánh bị cắt | Quyết định | Ghi chú |
|---|---|---|---|
| 1 | **Assign Staff Shifts** (phân ca làm việc) | ❌ Cắt hẳn khỏi UC Diagram Cinema Manager | Chưa từng có module M-xx hậu thuẫn — xác nhận đúng là ngoài phạm vi |
| 2 | **Voucher / Promotion** | ❌ Cắt hẳn — gỡ cả code (`VoucherServiceImpl`/`VoucherDAO`) lẫn UC liên quan | Dù code đã chạy + test pass 100%, vẫn cắt theo đúng định hướng "ít mà sâu" |
| 3 | **Loyalty & Membership** *(phát hiện thêm: chưa từng có mã M-xx chính thức trong toàn bộ Feature Tree, kể cả bản gốc)* | ❌ Cắt hẳn — gỡ cả code (`LoyaltyServiceImpl`, bảng `point_histories`/`membership_tiers` không cần UI) | Không cần bổ sung module mới vì đã quyết định cắt, không giữ |

**Hệ quả cần xử lý đồng bộ ở các tài liệu khác (không chỉ Feature Tree này):**
- UC Diagram **Customer** (chưa từng được sửa ở Bước 2) phải xoá 2 UC: `Apply Promo Voucher` *(extend của Book Tickets Online)* và `Manage Loyalty Points`.
- `ProjectTracking_CineMax.xlsx`: xoá toàn bộ nhóm nghiệp vụ "Voucher & Loyalty" (4 dòng Functions, 2 dòng Use Case) — xem prompt riêng `Buoc2.5_Sua_Tracking_CineMax.md`.
- Bước 3 (scaffold code) **không scaffold bất kỳ thứ gì** cho 3 nhánh này.

---

**Chú giải Actor:**
| Ký hiệu | Actor |
|---|---|
| 🟣 Guest | Khách vãng lai, chưa đăng nhập |
| 🟢 Customer | Khách hàng đã đăng ký/đăng nhập |
| 🟡 Cinema Staff | Nhân viên quầy vé & soát vé tại rạp |
| 🔵 Cinema Manager | Quản lý vận hành 1 chi nhánh |
| 🔴 Administrator | Quản trị toàn chuỗi |
| ⚙️ System | Hệ thống tự động thực hiện, không do người dùng thao tác trực tiếp |

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
- Select F&B combo during booking
- Confirm booking (ends here — ticket/QR issuance belongs to M-07, not this module)

### M-05.2 Counter Booking Flow — 🟡 Cinema Staff
- Search showtimes at the counter
- Select seats, select F&B
- Create booking (channel: POS)
- Apply applicable discounts
- Temporarily reserve seats (~5 minutes)

### M-05.3 Seat Reservation Lock — ⚙️ System
- Temporarily lock selected seats (Pessimistic Lock, ~5 minutes, real-time countdown)
- Release expired seat reservations (background job auto-releases seats)
- Prevent double booking
- Confirm seat availability before payment

### M-05.4 Booking Management — 🟢 Customer
- View booking details / history
- Modify booking
- Track booking status

---

## M-06. Payment Management

### M-06.1 Online Payment — 🟢 Customer *(khởi tạo)* / ⚙️ System *(xử lý)*
- Payment gateway (VNPay)
- Asynchronous payment webhook processing & IPN verification — ⚙️ System
- Payment confirmation / failure handling — ⚙️ System

### M-06.2 Counter Payment — 🟡 Cinema Staff
- Cash payment, VNPay
- Confirm payment
- Print receipt

### M-06.3 Payment Tracking — 🟢 Customer *(lịch sử của mình)* / 🔴 Administrator *(toàn chuỗi)*
- View payment history, track status
- Generate invoices/receipts

---

## M-07. Ticket & Entry Management

### M-07.1 Ticket Issuance
- Generate ticket (QR code signed with HMAC-SHA256) — ⚙️ System *(tự động ngay sau khi M-06 xác nhận thanh toán)*
- View ticket details — 🟢 Customer
- Reissue ticket / cancel ticket — 🟡 Cinema Staff
- Look up & reprint ticket by booking code or phone number — 🟡 Cinema Staff
- Export/download e-ticket (PDF / QR image) — 🟢 Customer

### M-07.2 Ticket Validation (Gate Check-in) — 🟡 Cinema Staff
- Scan QR ticket (phone camera / barcode scanner)
- Validate ticket (correct showtime, correct hall)
- Prevent duplicate entry (flip ticket status to USED, block re-entry)
- Record attendance
- Verify customer age against ID for age-restricted movies (T13/T16/T18)

---

## M-08. Food & Beverage Management

### M-08.1 F&B Catalog & Stock
- Add/edit/delete food & drink items — 🔴 Administrator
- Manage categories, pricing — 🔴 Administrator
- Manage stock per branch — 🔵 Cinema Manager

### M-08.2 F&B Ordering — 🟢 Customer *(online)* / 🟡 Cinema Staff *(tại quầy)*
- Browse menu, select items/combos
- Add F&B to booking (writes into order_items, item_type = 'FNB')
- Track order status

---

## M-09. Notification Management — ⚙️ System *(gửi tự động, người nhận là Customer)*

### M-09.1 Booking & Payment Notifications
- Booking confirmation (with e-ticket QR code)
- Payment confirmation
- Cancellation / refund notification

### M-09.2 Showtime Notifications
- Showtime reminder / change / cancellation notification

---

## M-10. Reporting & Analytics

### M-10.1 Sales Reports — 🔴 Administrator
- Ticket sales, revenue, F&B sales, payment report
- Export reports to Excel (.xlsx) & PDF

### M-10.2 Branch Reports — 🔴 Administrator *(toàn chuỗi)* / 🔵 Cinema Manager *(chi nhánh mình)*
- Branch revenue, ticket sales, occupancy, performance
- Chain-wide revenue & performance comparison across branches — 🔴 Administrator *(chỉ Admin so sánh được liên chi nhánh)*

### M-10.3 Operation Reports — 🔴 Administrator / 🔵 Cinema Manager *(chi nhánh mình)*
- Hall occupancy, popular movies/showtimes, peak booking periods
- Ticket attendance, demographic statistics & peak hours

---

## M-11. System Administration — 🔴 Administrator
- View all branches, monitor branch status, system-wide overview
- Configure global system parameters (seat hold duration, cleaning buffer minutes...)

---

## BẢNG TỔNG HỢP — đối chiếu nhanh Feature Tree ↔ UC Diagram (theo kết quả Bước 2)

Dùng bảng này để kiểm tra chéo: mỗi UC trên sơ đồ phải có đúng 1 dòng ở đây trỏ về đúng mã M-xx.x phía trên. Nếu một UC trên sơ đồ không xuất hiện trong bảng này — nó đang **không có căn cứ chính thức**, xử lý theo đúng tinh thần mục "Mục đang chờ quyết định" ở đầu file.

### 🔴 Administrator (11 UC)
| UC trên sơ đồ | Mã Feature Tree |
|---|---|
| Manage User, Staff & Manager Accounts | M-01.4 |
| Manage Master Movie Catalog | M-02.1 |
| Manage Cinema Branches & Halls | M-04.1 + M-04.2 |
| Configure Seat Grid Designer | M-04.3 |
| Configure Default (Global) Pricing Template | M-03.2 |
| Manage Global F&B Catalog (Items, Categories & Pricing) | M-08.1 |
| Moderate User Reviews | M-02.2 |
| Generate Enterprise BI Reports | M-10.1, M-10.2 |
| System Dashboard & Global Parameters | M-11 |
| Toggle Seat Maintenance Status | M-04.3 |
| User Logout | M-01.2 |

### 🔵 Cinema Manager (7 UC — đã chốt, sạch)
| UC trên sơ đồ | Mã Feature Tree |
|---|---|
| Schedule Branch Showtimes | M-03.1 |
| Configure Branch Pricing & Rates | M-03.2 |
| Monitor Hall Occupancy | M-03.3 |
| Manage Branch F&B Stock | M-08.1 |
| Toggle Seat Maintenance Status | M-04.3 |
| View Branch Revenue & Sales Reports | M-10.2, M-10.3 |
| User Logout | M-01.2 |

*(Đã xoá "Assign Staff Shifts" khỏi sơ đồ — xem mục "Quyết định đã chốt" ở đầu file.)*

### 🟡 Cinema Staff (11 UC)
| UC trên sơ đồ | Mã Feature Tree |
|---|---|
| Sell Tickets at Counter | M-05.2 |
| Order F&B at Counter | M-08.2 |
| Process Counter Payment | M-06.2 |
| Print Ticket | M-06.2 |
| Manage Shift Cash Drawer | *(ngoài phạm vi M-01→M-11, thuộc vận hành ca — xem thêm mục chờ quyết định #1)* |
| Validate QR Ticket | M-07.2 |
| Verify Age (T13/T16/T18) | M-07.2 |
| Reissue / Cancel Ticket | M-07.1 |
| Look Up & Reprint Ticket | M-07.1 |
| View Personal Shift Report | *(tương tự — liên quan vận hành ca)* |
| User Logout | M-01.2 |

### 🟢 Customer — 2 UC bị xoá theo quyết định v2.1
| UC trên sơ đồ cũ | Trạng thái |
|---|---|
| ~~Apply Promo Voucher~~ | ❌ Xoá — xem "Quyết định đã chốt" |
| ~~Manage Loyalty Points~~ | ❌ Xoá — xem "Quyết định đã chốt" |

---

## NHẬT KÝ PHIÊN BẢN

### v1.0 — Hiệu đính lần đầu
| # | Thay đổi | Lý do |
|---|---|---|
| 1 | Thêm nhãn **Actor** cho toàn bộ gạch đầu dòng | Bản gốc không ghi actor sở hữu ở bất kỳ đâu, khiến UC diagram phải tự suy luận ngược. |
| 2 | Bỏ bullet **"Apply promotion/voucher ?"** (M-05.1) | Bullet có dấu "?" của chính tác giả gốc — chưa chắc chắn, nay loại khỏi phạm vi. |
| 3 | Gộp "Assign staff/manager to branch" (M-01.4) + "Assign/transfer cinema managers" (M-04.1 cũ) thành 1 dòng ở M-01.4 | Trùng lặp cùng 1 hành động, rải ở 2 module khác nhau. |
| 4 | Gộp M-02.2 "Movie-Cinema Assignment" cũ vào bullet "Create showtime" của M-03.1 | Theo đúng nghiệp vụ thật — không cần bước duyệt riêng. |
| 5 | Đánh số lại liên tục M-09 → M-10 → M-11 | Bản gốc lỗi: tiêu đề "M-10" nhưng nội dung đánh M-12.x, M-11 lại nằm sau cùng. |
| 6 | Thống nhất RBAC chỉ còn 1 role **Cinema Staff** (bỏ Cashier/Usher tách riêng) | Mâu thuẫn nội bộ tài liệu — sửa lỗi, không phải thêm role mới. |
| 7 | Không thêm bullet/module nào ngoài 6 thay đổi trên | Giữ nguyên 100% nội dung còn lại so với bản gốc. |

### v2.0 — Nâng thành văn bản chính thức (bản hiện tại)
| # | Thay đổi | Lý do |
|---|---|---|
| 8 | Thêm mục **"Vai trò của tài liệu"** ở đầu file | Chính thức hoá vị trí "nguồn chân lý duy nhất", thay thế bản gốc và cây 14 nhánh MBCMS cho mục đích quyết định phạm vi. |
| 9 | Thêm mục **"MỤC ĐANG CHỜ QUYẾT ĐỊNH"** | Gom 2 điểm hở phát hiện được trong lúc làm Bước 2 (Assign Staff Shifts không có căn cứ; Voucher lệch giữa tài liệu và code) vào 1 chỗ dễ thấy, thay vì để rải rác/chìm ở cuối file như bản v1.0. |
| 10 | Thêm **"Bảng tổng hợp — đối chiếu nhanh Feature Tree ↔ UC Diagram"** | Chốt lại kết quả đã thống nhất ở Bước 2 (11 UC Admin / 7+1 UC Manager / 11 UC Staff), biến tài liệu thành nơi tra cứu 2 chiều — không chỉ "Feature Tree nói gì" mà còn "UC nào đang map vào đâu". |
| 11 | Không sửa nội dung bất kỳ module M-01 → M-11 nào so với v1.0 | Toàn bộ nội dung nghiệp vụ giữ nguyên, lần này chỉ nâng cấp vai trò + khả năng tra cứu của văn bản. |

### v2.1 — Chốt cắt 3 nhánh ngoài phạm vi
| # | Thay đổi | Lý do |
|---|---|---|
| 12 | Đóng cả 2 mục "đang chờ quyết định" — chốt **cắt hẳn** Assign Staff Shifts và Voucher, thay vì giữ lại như tín hiệu từ `ProjectTracking_CineMax.xlsx` gợi ý | Người dùng quyết định ưu tiên chiều sâu nghiệp vụ hơn độ phủ tính năng, đúng trọng tâm chấm điểm môn học |
| 13 | Phát hiện thêm và cắt luôn **Loyalty & Membership** — chưa từng có mã M-xx chính thức dù code đã triển khai khá đầy đủ | Cùng tinh thần "ít mà sâu" — không giữ lại 1 nhánh chưa từng được chính thức hoá trong tài liệu gốc |
| 14 | Cập nhật bảng tổng hợp UC Diagram: Manager còn đúng 7 UC sạch (hết dòng chờ); bổ sung bảng Customer ghi nhận 2 UC bị xoá (`Apply Promo Voucher`, `Manage Loyalty Points`) — đây là sơ đồ đầu tiên trong dự án có thay đổi do hệ quả của v2.1 | Đảm bảo UC Diagram Customer không bị bỏ sót dù nằm ngoài phạm vi Bước 2 ban đầu |
