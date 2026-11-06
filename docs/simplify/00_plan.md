# KẾ HOẠCH ĐƠN GIẢN HÓA VÀ BẢO VỆ LUỒNG DỮ LIỆU (SWP391 - Dũng)

Tài liệu này được lập theo chuẩn kỹ thuật thận trọng nhằm phục vụ việc cắt giảm độ phức tạp, bảo đảm luồng dữ liệu 4 tầng (JSP → Servlet → Service → DAO → DB) trong sáng và ngăn chặn 100% rủi ro làm gãy các module khác của nhóm (Showtime, Booking, POS).

---

## 1. BẢNG ĐỐI CHIẾU CÁC HẠNG MỤC CẮT GIẢM

| Hạng mục cắt giảm | File tác động | Hàm / Thuộc tính / Khối | Ai đang gọi / Đối tượng ảnh hưởng | Đánh giá an toàn |
| :--- | :--- | :--- | :--- | :--- |
| **Doanh thu theo kênh & Thanh toán** | `ReportAdminServlet.java`<br>`RevenueReportService.java`<br>`RevenueReportServiceImpl.java`<br>`RevenueReportDAO.java`<br>`revenue-report.jsp` | `getRevenueByChannel`<br>`getRevenueByPaymentMethod` | Chỉ `ReportAdminServlet` gọi nội bộ để hiển thị lên `revenue-report.jsp`. Không ai khác gọi. | **Tuyệt đối an toàn** |
| **Biểu đồ thời gian (Chart.js)** | `ReportAdminServlet.java`<br>`RevenueReportService.java`<br>`RevenueReportServiceImpl.java`<br>`RevenueReportDAO.java`<br>`revenue-report.jsp` | `getTimeSeriesRevenue`<br>`RevenueTimeSeriesDTO`<br>Thẻ CDN Chart.js & canvas | Chỉ `ReportAdminServlet` gọi nội bộ để vẽ Chart. Không module nào khác phụ thuộc. | **Tuyệt đối an toàn** |
| **Phần trăm tăng trưởng vs kỳ trước** | `RevenueReportServiceImpl.java`<br>`RevenueKpiDTO.java`<br>`revenue-report.jsp`<br>`RevenueReportServiceTest.java` | `calculateGrowth`<br>`revenueGrowthPercent`<br>`bookingsGrowthPercent`<br>`ticketsGrowthPercent` | Chỉ nội bộ tính toán delta KPI trong `RevenueReportServiceImpl`. | **Tuyệt đối an toàn** (cập nhật lại test tương ứng) |
| **Bộ lọc dư thừa (Phim, Kênh, Gom ngày)** | `RevenueFilterDTO.java`<br>`RevenueReportServiceImpl.java`<br>`RevenueReportDAO.java`<br>`revenue-report.jsp` | `movieId`, `channel`, `groupBy`<br>`appendCommonFilters` | Chỉ nội bộ lọc trong form JSP và câu query SQL của DAO. | **Tuyệt đối an toàn** |
| **Xuất file Server-Side (Excel/PDF)** | `ReportAdminServlet.java`<br>`revenue-report.jsp` | `handleRevenueExport`<br>URL `/admin/reports/revenue/export` | Chỉ phục vụ 2 nút xuất file trên giao diện cũ. Nút mới chuyển sang `window.print()`. | **Giữ nguyên interface `ReportExportService`** không sửa pom.xml để tránh lỗi build. |
| **Lập lịch bảo trì ngày-giờ trên UI** | `SeatGridServlet.java`<br>`seat-designer.jsp` | Action `createMaintenanceSchedule`<br>Action `resolveMaintenanceSchedule`<br>Form lập lịch & bảng lịch JSP | Chỉ `SeatGridServlet` và `seat-designer.jsp` gọi. | **Tuyệt đối an toàn** khi cắt khỏi UI. Giữ bảng DB và hàm Service cho Showtime! |

---

## 2. BẰNG CHỨNG `git grep` XÁC MINH KHÔNG ẢNH HƯỞNG MODULE KHÁC

### 2.1. Doanh thu theo kênh (`getRevenueByChannel`)
```text
src/main/java/com/cinema/modules/infrastructure/controller/ReportAdminServlet.java:89
src/main/java/com/cinema/modules/infrastructure/controller/ReportAdminServlet.java:154
src/main/java/com/cinema/modules/infrastructure/dao/RevenueReportDAO.java:242
src/main/java/com/cinema/modules/infrastructure/service/RevenueReportService.java:49
src/main/java/com/cinema/modules/infrastructure/service/impl/RevenueReportServiceImpl.java:160
=> 100% chỉ nằm trong package com.cinema.modules.infrastructure.
```

### 2.2. Doanh thu theo phương thức thanh toán (`getRevenueByPaymentMethod`)
```text
src/main/java/com/cinema/modules/infrastructure/controller/ReportAdminServlet.java:90
src/main/java/com/cinema/modules/infrastructure/dao/RevenueReportDAO.java:278
src/main/java/com/cinema/modules/infrastructure/service/RevenueReportService.java:54
src/main/java/com/cinema/modules/infrastructure/service/impl/RevenueReportServiceImpl.java:166
=> 100% chỉ nằm trong package com.cinema.modules.infrastructure.
```

### 2.3. Chuỗi thời gian Chart.js (`getTimeSeriesRevenue`)
```text
src/main/java/com/cinema/modules/infrastructure/controller/ReportAdminServlet.java:86
src/main/java/com/cinema/modules/infrastructure/controller/ReportAdminServlet.java:151
src/main/java/com/cinema/modules/infrastructure/dao/RevenueReportDAO.java:105
src/main/java/com/cinema/modules/infrastructure/service/RevenueReportService.java:34
src/main/java/com/cinema/modules/infrastructure/service/impl/RevenueReportServiceImpl.java:142
=> 100% chỉ nằm trong package com.cinema.modules.infrastructure.
```

### 2.4. Phát hiện đặc biệt quan trọng về Lịch bảo trì (`getMaintenanceSchedulesByRoom`)
```text
src/main/java/com/cinema/modules/catalog/controller/ShowtimeManagementServlet.java:497:
    for (MaintenanceSchedule schedule : screeningRoomService.getMaintenanceSchedulesByRoom(room.getId()))
=> Module Catalog (Quản lý lịch chiếu) của bạn khác đang gọi getMaintenanceSchedulesByRoom!
=> KẾT LUẬN: KHÔNG ĐƯỢC XÓA method này trong ScreeningRoomService/DAO! Chỉ loại bỏ request attribute trong SeatGridServlet và JSP!
```

---

## 3. DANH SÁCH CÁC THÀNH PHẦN BẤT KHẢ XÂM PHẠM (GIỮ NGUYÊN 100%)

1. **7 Hàm Hợp Đồng Gốc trong `ScreeningRoomService`**:
   - `getAllCinemas()`, `getCinemaById(id)`
   - `getRoomsByCinema(cinemaId)`, `getRoomById(roomId)`
   - `isRoomAvailable(roomId, start, end)` (kể cả overload có excludeShowtimeId)
   - `getRoomSeatMatrix(roomId)`
   - `getSeatById(seatId)`, `getSeatsByIds(seatIds)`
2. **Logic Ghế Đôi Sweetbox**:
   - `ValidationUtil.validateCoupleSeats` / `isCoupleSeat` (Booking & POS sử dụng).
   - 2 bước kiểm tra tách biệt trong `ScreeningRoomServiceImpl.updateBatchSeats` (nhóm ghế chọn và tổng thể toàn phòng).
3. **Cơ sở dữ liệu & Cấu hình**:
   - Bảng `maintenance_schedules` và các file SQL `01` đến `05`.
   - File `pom.xml`, các Filter `AuthorizationFilter`, `AuthenticationFilter`.
4. **Bản chất Doanh Thu**:
   - Điều kiện SQL CTE: `payments.status = 'SUCCESS' AND bookings.status = 'CONFIRMED' AND is_deleted = 0`.
   - Khoảng thời gian từ `from 00:00:00` đến `to 23:59:59.999`.
   - 3 chỉ số cốt lõi: Tổng doanh thu, Số đơn xác nhận, Số vé bán ra.
   - Bảng phân tích theo Cụm Rạp, Top Phim và Chi tiết đơn hàng phân trang `OFFSET / FETCH`.
