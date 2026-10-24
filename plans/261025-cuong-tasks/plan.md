---
title: "Cường: tài khoản, lịch chiếu, thông báo"
description: "Hoàn thành 8 task của Cường trên tracking mới. Bỏ email hoàn tiền. Tái sử dụng quên mật khẩu, API suất chiếu và hàm gửi email đã có."
status: in-progress
priority: P1
branch: "cuong"
tags: [identity, showtime, pricing, notification]
blockedBy: []
blocks: []
created: "2026-10-24T18:41:38.724Z"
createdBy: "ck:plan"
source: skill
---

# Cường: tài khoản, lịch chiếu, thông báo

## Overview

Đối chiếu lại sheet Functions ngày 25/10/2026. Cường không có dòng Iteration 1. Iteration 1 trên sheet là của Dũng, Linh, Thịnh và Tuyển.

Việc tiếp theo là phần Iteration 2 còn thiếu: lịch chiếu công khai, form tạo suất của Manager, và màn bảng giá. Email vé thuộc Iteration 3, làm sau. Không làm email hoàn tiền.

| Task tracking | Iteration | Phase | Code hiện tại |
| :--- | :--- | :--- | :--- |
| M-01.2 Quên / đặt lại mật khẩu | 2 | 1 | Đã có trong `AuthServlet`. Chỉ cần test lại token 15 phút |
| M-01.3 Hồ sơ và đổi mật khẩu | 2 | 1 | Đã làm `/customer/account/profile` |
| M-01.4 Quản lý tài khoản | 2 | 1 | Đã làm `/admin/identity/users`, có STT và chặn đăng nhập khi LOCKED |
| M-01.4 Gán vai trò và chi nhánh | 3 | 1 | Đã làm cùng màn trên. CUSTOMER và ADMIN không có form gán chi nhánh |
| M-01.1 Lịch chiếu công khai | 2 | 2 | Chưa làm. `movie-detail.jsp` vẫn trỏ `showtimeId=1` |
| M-03.1 Tạo suất chiếu, buffer 15 phút | 2 | 2 | Chưa có form. `GET /manager/showtimes` vẫn trả JSON. Buffer đã có trong service |
| M-03.2 Cấu hình giá vé | 2 | 2 | Chưa có `PricingAdminServlet`. DAO giá chỉ đọc |
| M-09.1 Email xác nhận vé | 3 | 3 | Chưa gọi sau khi đặt vé commit |
| M-09.1 Nhắc giờ chiếu | 3 | 3 | Chưa có job |
| M-09.2 Email hoàn tiền | 3 | — | Không làm |

## Không làm

- Email hủy vé, hoàn tiền, hoặc bất kỳ mẫu mail nào có chữ hoàn tiền.
- Đăng nhập, đăng ký OTP, đăng xuất. Tracking mới giao cho Thịnh. Code đã chạy.
- Sơ đồ ghế khách, VNPay thật, QR HMAC, lịch sử vé, hủy vé. Phần của Linh.
- CRUD phim admin, đánh giá phim. Phần của Thịnh.
- Báo cáo doanh thu. Phần của Dũng.
- Đăng nhập Google, tích điểm, voucher.
- Thêm cột giá HSSV/Senior vào `ticket_pricings`. POS đã nhân hệ số từ `STUDENT_TICKET_DISCOUNT_PERCENT` và `SENIOR_TICKET_DISCOUNT_PERCENT`.

## Dependencies

Không có plan khác trong repo. Phase 3 chỉ móc một chỗ vào `BookingEngineServiceImpl.createBooking` sau khi transaction trả về. Không sửa logic giữ ghế, trừ kho, hay thanh toán của Linh.

## Thứ tự làm tiếp

1. Phase 2, ưu tiên vì là Iteration 2 còn mở: trang `/showtimes`, form tạo suất Manager, CRUD bảng giá và hai mức giảm HSSV/Senior.
2. Test lại quên mật khẩu nếu muốn đóng nốt M-01.2 trên sheet. Không viết lại.
3. Phase 3 chỉ bắt đầu sau khi lịch chiếu và bảng giá chạy. Email hoàn tiền vẫn bỏ.

## Thứ tự và cách kiểm

1. Phase 1 đã code: khách sửa hồ sơ, admin khóa user, khách bị khóa thấy "Tài khoản đã bị khóa", gán Staff/Manager về một rạp, khách không có form gán chi nhánh.
2. Phase 2: khách thấy suất thật, Manager tạo suất bị chặn khi trùng buffer, đổi giá thì POS đổi theo.
3. Phase 3: booking có tài khoản, kể cả vé quầy, thì có email. Khách vãng lai không có. Tắt SMTP thì vé vẫn `CONFIRMED`. Cùng booking không nhận email nhắc lần hai. Không có đường gửi email hoàn tiền.

Chi tiết từng phase nằm ở file phase tương ứng.

## Validation Log

### Verification Results
- Claims checked: 24
- Verified: 22 | Failed: 1 | Unverified: 1
- Tier: Standard
- Failures:
  1. [Fact Checker] Phase 1 nói `UserDAO` có `isPhoneTaken`. Method nằm ở `UserServiceImpl` (dòng 154), DAO chỉ có `findByPhone`.
- Unverified:
  1. [Contract Verifier] `createBooking` có 2 caller: `BookingFlowServlet` và `PosOrderServiceImpl`. Phase 3 chưa nêu caller POS. Cần chốt email có gửi cho vé quầy hay không.

### Session 1 — 2026-10-25
**Trigger:** Validate trước khi code, sau verification pass.
**Questions asked:** 4

#### Questions & Answers

1. **[Assumptions]** Phase 1 — Khi khách sửa hồ sơ, kiểm tra số điện thoại trùng đang nằm ở UserService.isPhoneTaken và trả true cả khi đó là số của chính họ. Lưu hồ sơ nên xử lý thế nào?
   - Options: Bỏ qua nếu số không đổi hoặc thuộc đúng user đang sửa | Chặn mọi số đã tồn tại, kể cả số hiện tại của chính họ
   - **Answer:** Bỏ qua nếu số không đổi hoặc thuộc đúng user đang sửa
   - **Rationale:** `isPhoneTaken` hiện không loại user hiện tại. Dùng nguyên hàm sẽ chặn khách lưu lại chính số của họ.

2. **[Risks]** Phase 1 — Màn gán vai trò cho phép Admin gán những vai trò nào?
   - Options: Chỉ STAFF và MANAGER, không tạo thêm ADMIN từ màn này | Được gán cả ADMIN
   - **Answer:** Chỉ STAFF và MANAGER
   - **Rationale:** Màn này không được nâng ai lên ADMIN. Hạ về CUSTOMER cũng không nằm trên màn này.

3. **[Scope]** Phase 3 — createBooking được gọi cả từ đặt vé online và từ quầy POS. Email xác nhận vé gửi cho ai?
   - Options: Chỉ booking ONLINE có userId | Mọi booking có userId, kể cả khách đăng nhập mua tại quầy
   - **Answer:** Mọi booking có userId, kể cả khách đăng nhập mua tại quầy
   - **Rationale:** Cả `BookingFlowServlet` và `PosOrderServiceImpl` đều gọi `createBooking`. Điều kiện gửi là có `userId`, không phải channel ONLINE.

4. **[Architecture]** Phase 3 — Job nhắc giờ chiếu cần đọc booking và suất chiếu. Query nên đặt ở đâu để không gọi DAO chéo module?
   - Options: Thêm method trên BookingEngineService, job identity chỉ gửi mail và ghi notifications | Viết SQL join bookings ngay trong NotificationDAO
   - **Answer:** Thêm method trên BookingEngineService
   - **Rationale:** Module identity không được query bảng bookings. Job chỉ gửi mail và ghi `notifications`.

#### Confirmed Decisions
- Điện thoại hồ sơ: trùng với user khác thì từ chối. Trùng chính mình thì cho lưu.
- Gán vai trò: chỉ STAFF và MANAGER. Không gán ADMIN. Không hạ CUSTOMER trên màn này.
- Email xác nhận: mọi booking có `userId`, gồm vé quầy. Không có `userId` thì không gửi.
- Nhắc suất: query nằm ở `BookingEngineService`. Job identity không join bảng booking.

#### Action Items
- [x] Sửa phase 1: kiểm tra điện thoại loại user hiện tại
- [x] Sửa phase 1: danh sách vai trò chỉ STAFF và MANAGER
- [x] Sửa phase 3: bỏ điều kiện channel ONLINE
- [x] Sửa phase 3: thêm method nhắc suất trên BookingEngineService

#### Impact on Phases
- Phase 1: `updateProfile` và màn gán vai trò
- Phase 2: không đổi
- Phase 3: điều kiện gửi mail và chỗ đặt query nhắc suất

### Whole-Plan Consistency Sweep
- Files re-read: `plan.md`, `phase-01-tai-khoan-va-phan-quyen.md`, `phase-02-lich-chieu-va-bang-gia.md`, `phase-03-thong-bao-dat-ve.md`
- Stale terms: điều kiện `channel ONLINE` đã bỏ khỏi bước gửi mail. `isPhoneTaken` chỉ còn trong mô tả lỗi cũ và trong log, không còn là cách kiểm tra hồ sơ.
- Contradictions: câu "không hạ admin cuối cùng" lệch với quyết định không gán ADMIN. Đã sửa thành không đổi vai trò user đang là ADMIN, và không khóa admin active cuối cùng.
- Phase 2 không đụng các quyết định này.
- Unresolved contradictions: không.
