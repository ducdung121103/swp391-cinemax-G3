---
phase: 2
title: "Lich chieu va bang gia"
status: pending
priority: P1
dependencies: []
effort: "2 ngay"
---

# Phase 2: Lịch chiếu và bảng giá

## Overview

Đây là việc Iteration 2 còn lại, làm ngay sau phần tài khoản. Khách xem lịch chiếu theo phim, rạp và ngày, rồi sang checkout đúng suất đó. Manager có trang tạo suất. Admin/Manager sửa bảng giá. Logic buffer 15 phút và công thức giá đang chạy, phase này thêm màn hình và CRUD còn thiếu.

## Requirements

- Functional: khách lọc suất và bấm một suất thật. Manager tạo suất trong rạp của mình. Suất trùng khoảng `thời lượng phim + CLEANING_BUFFER_MINUTES` bị từ chối với thông báo phòng bận. Admin sửa giá theo ngày, khung giờ, định dạng, loại ghế. Cùng màn hình sửa phần trăm giảm HSSV và Senior.
- Non-functional: không khai báo servlet trong `web.xml`. Manager không sửa giá hoặc suất của rạp khác. GET `/manager/showtimes` đang trả JSON vì sidebar trỏ thẳng vào đó. Giữ JSON khi có `format=json`.

## Architecture

`ShowtimeServiceImpl.createShowtime` đã cộng buffer từ `system_settings` và gọi `ScreeningRoomService.isRoomAvailable`. Servlet Manager chỉ dựng form và hiện `IllegalStateException`.

Giá vé bán ra vẫn là `PricingService.calculateTicketPrice`. HSSV/Senior không có cột riêng. POS nhân `STUDENT_TICKET_DISCOUNT_PERCENT` (mặc định 15) và `SENIOR_TICKET_DISCOUNT_PERCENT` (mặc định 10). Màn giá ghi hai key này bằng `SystemSettingDAO.upsertSetting`.

`ticket_pricings.cinema_id` null nghĩa là giá toàn chuỗi. Manager chỉ được ghi dòng có `cinema_id` bằng rạp của mình.

## Related Code Files

- Create: `src/main/java/com/cinema/modules/catalog/controller/ShowtimePublicServlet.java`
- Create: `src/main/webapp/WEB-INF/views/customer/catalog/showtimes.jsp`
- Create: `src/main/webapp/WEB-INF/views/admin/catalog/showtimes.jsp`
- Create: `src/main/java/com/cinema/modules/catalog/controller/PricingAdminServlet.java`
- Create: `src/main/webapp/WEB-INF/views/admin/catalog/pricing.jsp`
- Modify: `ShowtimeManagementServlet.java` — GET thường forward JSP, `format=json` giữ API
- Modify: `ShowtimeService.java` chỉ khi trang công khai cần một method gộp phim + rạp + ngày. Ưu tiên dùng `getShowtimesByMovieAndDate` và `getShowtimesByCinemaAndDate` đã có
- Modify: `TicketPricingDAO.java`, `PricingService.java`, `PricingServiceImpl.java` — list, insert, update, soft-delete
- Modify: `movie-detail.jsp` — nút Mua vé trỏ `/showtimes?movieId={id}` thay vì `/booking/checkout?showtimeId=1`
- Modify: `sidebar.jsp` — Admin thêm Bảng giá vé. Link lịch chiếu của Manager giữ nguyên

## Implementation Steps

1. `ShowtimePublicServlet` map `/showtimes`. Không đặt dưới `/admin`. Tham số `movieId`, `cinemaId`, `date` (mặc định hôm nay). Render danh sách suất còn `SCHEDULED` và chưa bị xóa. Mỗi suất là link `/booking/checkout?showtimeId={id}`. Không vẽ sơ đồ ghế.
2. Sửa `movie-detail.jsp` để phim đang chiếu đi tới `/showtimes?movieId=`.
3. Tách GET của `ShowtimeManagementServlet`: không có `format=json` thì forward `admin/catalog/showtimes.jsp`, có `format=json` thì giữ JSON hiện tại. POST tạo suất giữ nguyên kiểm tra Manager chỉ tạo cho rạp mình. JSP hiện lỗi `Phòng chiếu đang bận hoặc đang trong lịch bảo trì`.
4. Trang Manager: chọn phim, phòng thuộc rạp của user, giờ bắt đầu, định dạng 2D/3D/IMAX. Ghi chú trên form: giờ kết thúc = giờ bắt đầu + thời lượng phim + buffer trong system settings.
5. Thêm CRUD mềm trên `ticket_pricings`: `day_type` (WEEKDAY/WEEKEND), `time_slot` (EARLY/STANDARD/PRIME), `experience_format`, `seat_type_id`, `base_price`, `effective_from`, `effective_to`, `cinema_id`. Xóa là `is_deleted = 1`.
6. `PricingAdminServlet` map `/admin/catalog/pricing`. Admin được để `cinema_id` null. Manager bị ép `cinema_id` của mình và không sửa dòng toàn chuỗi.
7. Trên cùng trang, hai ô phần trăm HSSV và Senior. Lưu bằng `SystemSettingDAO.upsertSetting`. Không đổi schema.

## Success Criteria

- [ ] Trang chi tiết phim không còn `showtimeId=1`.
- [ ] Khách chưa đăng nhập mở được `/showtimes`, chọn suất, URL checkout mang đúng id.
- [ ] Manager mở `/manager/showtimes` thấy form, không thấy JSON thô.
- [ ] `GET /manager/showtimes?format=json` vẫn trả JSON.
- [ ] Tạo suất chồng lên suất cũ trong cùng phòng bị từ chối. Suất cách nhau đủ thời lượng + buffer thì tạo được.
- [ ] Manager tạo suất cho phòng của rạp khác bị từ chối.
- [ ] Sửa `base_price` một dòng WEEKEND/PRIME/2D làm `calculateTicketPrice` đổi theo. POS với vé HSSV đổi khi sửa phần trăm giảm.
- [ ] Manager không sửa được dòng giá `cinema_id` null.

## Risk Assessment

Đổi GET `/manager/showtimes` sang HTML sẽ làm hỏng client đang đọc JSON. Giữ `format=json`. Giá HSSV nếu bị thêm thành cột mới sẽ lệch POS đang nhân hệ số. Dùng đúng hai key system settings hiện có.
