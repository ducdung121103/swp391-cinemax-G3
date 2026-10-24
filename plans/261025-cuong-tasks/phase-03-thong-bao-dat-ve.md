---
phase: 3
title: "Thong bao dat ve"
status: pending
priority: P2
dependencies: []
effort: "1 ngay"
---

# Phase 3: Thông báo đặt vé và nhắc suất

## Overview

Làm sau Iteration 2. Sau khi đặt vé thành công, gửi email xác nhận. Trước giờ chiếu, gửi một email nhắc cho mỗi booking. Không có email hoàn tiền và không có email hủy suất.

## Requirements

- Functional: mọi booking có `userId` nhận email mã vé, tên phim, suất, ghế. Gồm vé online và vé quầy của khách đã có tài khoản. Booking không có `userId` thì không gửi. Ảnh QR để null cho đến khi Linh làm HMAC. Job nhắc gửi khi suất còn `SCHEDULED` và nằm trong cửa sổ `SHOWTIME_REMINDER_MINUTES` (mặc định 120). Mỗi booking chỉ nhắc một lần.
- Non-functional: gửi email nằm ngoài transaction đặt vé. SMTP lỗi thì vé vẫn `CONFIRMED`. Module booking chỉ gọi service, không gọi `UserDAO` hay `MovieDAO`.

## Architecture

`createBooking` có hai caller: `BookingFlowServlet` (online) và `PosOrderServiceImpl` (quầy). Cả hai đi qua cùng một chỗ gửi mail. Email gọi sau khi `TransactionManager.executeInTransaction` trả về, trong `catch` riêng. Thất bại chỉ ghi log. Điều kiện gửi là `userId` khác null, không lọc theo channel.

<!-- Updated: Validation Session 1 - email any booking with userId; reminder query lives on BookingEngineService -->

Nội dung mail lấy qua `UserService.getUserById`, `ShowtimeService.getShowtimeById`, `MovieService.getMovieById`. `EmailService.sendBookingConfirmationEmail` đã có sẵn tham số `qrImageBase64`.

Nhắc suất dùng `ServletContextListener` và `ScheduledExecutorService` trong JDK, không thêm dependency. Listener được khai báo trong `web.xml`. Servlet vẫn dùng annotation. Query danh sách cần nhắc nằm ở method mới `BookingEngineService.findBookingsNeedingReminder(int withinMinutes)`, implement trong module booking. Method trả booking id, user id, tên phim, giờ chiếu, phòng và rạp. Job identity gọi method đó, lấy email qua `UserService`, gửi mail, rồi `NotificationDAO.insertIfAbsent`. Không viết SQL bookings trong module identity. Suất `CANCELLED` bị loại trong query của booking, không gửi mail hủy.

## Related Code Files

- Create: `src/main/java/com/cinema/modules/identity/dao/NotificationDAO.java`
- Create: `src/main/java/com/cinema/modules/identity/job/ShowtimeReminderListener.java`
- Modify: `EmailService.java`, `EmailServiceImpl.java` — thêm `sendShowtimeReminderEmail`
- Create: `src/main/java/com/cinema/modules/booking/dto/ShowtimeReminderDTO.java`
- Modify: `BookingEngineService.java`, `BookingEngineServiceImpl.java` — gửi mail sau transaction, và `findBookingsNeedingReminder`
- Modify: `src/main/webapp/WEB-INF/web.xml` — khai báo listener, không khai báo servlet
- Modify: seed hoặc system settings — key `SHOWTIME_REMINDER_MINUTES` = 120 nếu chưa có. Dùng `SystemSettingDAO`, không cần migration nếu `upsertSetting` chạy lúc job khởi động

## Implementation Steps

1. Thêm `sendShowtimeReminderEmail(toEmail, fullName, movieTitle, showtimeText, cinemaRoomText)`. Mẫu chỉ có giờ chiếu, phim, phòng, rạp. Không có câu hoàn tiền hay hủy vé.
2. Trong `createBooking`, sau khi transaction trả về result thành công và `dto.getUserId()` khác null: lấy email, tên phim, suất, danh sách ghế, gọi `sendBookingConfirmationEmail` với `qrImageBase64 = null`. Áp dụng cho cả online và POS. Bọc try/catch. Không đưa lời gọi này vào lambda transaction.
3. `NotificationDAO.insertIfAbsent(userId, type, referenceId, title, message)` để hai lần chạy job không tạo hai dòng cùng `type + reference_id`.
4. `ShowtimeReminderListener` chạy mỗi 5 phút. Cửa sổ đọc từ `SHOWTIME_REMINDER_MINUTES`. Gọi `findBookingsNeedingReminder`. Bỏ booking đã có notification `SHOWTIME` cùng `reference_id`.
5. `contextDestroyed` gọi `shutdown` scheduler.
6. Không thêm method, nhánh, hay template hoàn tiền. Không lắng nghe trạng thái `CANCELLED` để gửi mail.

## Success Criteria

- [ ] Đặt vé online với user đăng nhập thì email xác nhận được gửi. Mã vé trong mail khớp booking vừa tạo.
- [ ] Bán vé tại quầy cho khách đã có tài khoản cũng gửi email. Khách vãng lai không có `userId` thì không gửi.
- [ ] Cố ý sai SMTP: booking vẫn `CONFIRMED`, ghế không bị hoàn lại bước giữ.
- [ ] Job nhắc gửi một email. Chạy job lần hai không gửi thêm.
- [ ] Suất `CANCELLED` không sinh email.
- [ ] Không có method nào trong `EmailService` dành cho hoàn tiền.

## Risk Assessment

Gửi mail bên trong transaction sẽ giữ lock ghế nếu SMTP chậm, và rollback vé khi mail lỗi. Gọi sau commit. Job trên nhiều instance Tomcat có thể gửi trùng. Đồ án chạy một server. `insertIfAbsent` vẫn cần để lần chạy sau không gửi lại. Sửa `BookingEngineServiceImpl` dễ đụng nhánh của Linh. Chỉ thêm khối sau transaction, không sửa các bước 1 đến 9 bên trong lambda.
