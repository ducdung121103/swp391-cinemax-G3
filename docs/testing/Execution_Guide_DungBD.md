# HƯỚNG DẪN THỰC THI KIỂM THỬ (TEST EXECUTION GUIDE) – DungBD
**Dự án:** CineMax – Multi-Branch Cinema Management System (SWP391-Gr3)  
**Phụ trách:** Bùi Đức Dũng (DungBD) – Team Leader / Infrastructure Architect & BI Specialist  
**Ngày phát hành:** 03/11/2026 – Phiên bản: 1.0  

---

## 1. Môi Trường Thử Nghiệm Chuẩn (Test Environment)
1. **Hệ điều hành:** Windows 10/11 64-bit hoặc Linux Ubuntu 22.04 LTS.
2. **Nền tảng Java:** Oracle JDK 17 / OpenJDK 17 (Java SE Development Kit 17.0.12).
3. **Web Server:** Apache Tomcat 10.1.34 (Jakarta EE 10, Servlet 6.0, port `8080`).
4. **Cơ sở dữ liệu:** Microsoft SQL Server 2019/2022 (port `1433`), Database: `cinema_chain_db`.
5. **Kịch bản dữ liệu (Database Pipeline):**
   - Đảm bảo thực thi tuần tự 5 file SQL trong `src/main/resources/sql/`:
     - `01_schema.sql` (Khởi tạo bảng, khóa chính, khóa ngoại)
     - `02_seed_master_data.sql` (Cấu hình hệ thống, vai trò, loại ghế, tài khoản Admin)
     - `03_seed_sample_data.sql` (Dữ liệu cụm rạp C-HN01.., phòng chiếu, ghế, phim)
     - `04_seed_analytics_and_maintenance.sql` (Lịch bảo trì ghế, booking & payment mẫu)
     - `05_seed_revenue_reports.sql` (Dữ liệu doanh thu đa rạp, đa kênh phục vụ báo cáo)

---

## 2. Danh Sách Tài Khoản Thử Nghiệm Theo Phân Quyền (RBAC Test Accounts)
Tất cả tài khoản mẫu đều dùng mật khẩu mặc định: **`123456`**

| Vai trò (Role) | Email đăng nhập | Mật khẩu | Phạm vi quyền hạn |
|---|---|---|---|
| **System Admin** | `admin@cinemax.vn` | `123456` | Toàn quyền quản trị hệ thống, quản lý cụm rạp, phòng chiếu, ma trận ghế, cấu hình tham số, xem Dashboard và toàn bộ Báo cáo Doanh Thu / So Sánh Cụm Rạp. |
| **Cinema Manager** | `manager.hn01@cinemax.vn` | `123456` | Quản lý chi nhánh Hà Nội (Bà Triệu), xem báo cáo tỷ lệ lấp đầy rạp mình, toggle bảo trì ghế hỏng tại rạp. Bị chặn (403) khi truy cập Báo Cáo Doanh Thu, So Sánh Chuỗi và Cấu Hình Hệ Thống. |
| **Cinema Staff** | `staff.hn01@cinemax.vn` | `123456` | Nhân viên bán vé tại quầy (POS) và soát vé QR Scanner. Bị chặn (403) đối với toàn bộ khu vực quản trị hạ tầng và báo cáo. |
| **Customer** | `customer1@gmail.com` | `123456` | Khách hàng đặt vé xem phim online. Bị chặn tuyệt đối khỏi phân hệ `/admin/*`. |

---

## 3. Thứ Tự Thực Thi & Bằng Chứng Kiểm Thử

### Bước 1: Kiểm thử Tích hợp Đơn vị Màn hình (Integration Testing - IT)
Mở file `docs/testing/SE2056-JV_Group3_Integration-Test_DungBD.xlsx`, thực hiện theo từng sheet:
1. `IT-CIN`: Quản lý cụm rạp (/admin/infrastructure/cinemas) - 15 test cases.
2. `IT-ROOM`: Quản lý phòng chiếu (/admin/infrastructure/rooms) - 15 test cases.
3. `IT-SEAT`: Thiết kế ma trận ghế (/admin/infrastructure/seats) - 18 test cases.
4. `IT-MNT`: Quản lý bảo trì ghế (/admin/seats) - 14 test cases.
5. `IT-SET`: Cấu hình tham số hệ thống (/admin/infrastructure/settings) - 14 test cases.
6. `IT-DASH`: Dashboard tổng quan (/admin/dashboard) - 15 test cases.
7. `IT-OCC`: Báo cáo lấp đầy ghế (/admin/reports/occupancy) - 16 test cases.
8. `IT-REV`: Báo cáo doanh thu & doanh số (/admin/reports/revenue) - 20 test cases.
9. `IT-CMP`: So sánh hiệu suất cụm rạp (/admin/reports/comparison) - 18 test cases.

### Bước 2: Kiểm thử Luồng Nghiệp Vụ Đầu-Cuối (System Testing - ST)
Mở file `docs/testing/SE2056-JV_Group3_System-Test_DungBD.xlsx`, thực hiện theo 4 Workflow:
1. `WF1 Cinema Branch`: Vòng đời cụm rạp (Thêm rạp -> Cập nhật -> Đổi trạng thái -> Xóa mềm).
2. `WF2 Room & Seat`: Thiết lập hạ tầng (Tạo phòng -> Sinh layout ghế -> Gán VIP/Couple -> Bảo trì ghế -> Đồng bộ sức chứa).
3. `WF3 System Admin`: Tham số vận hành (Điều chỉnh giữ ghế -> Timeout hủy vé -> Giãn cách suất chiếu -> Chặn phân quyền Manager/Staff).
4. `WF4 Dashboard & Reporting`: Đối soát dữ liệu tài chính (Tổng Doanh Thu Dashboard == Báo Cáo Doanh Thu == Tổng So Sánh Các Rạp -> Lọc đa chiều -> Xuất Excel .xlsx và PDF tiếng Việt).

---

## 4. Cách Chụp Ảnh Bằng Chứng & Lưu Trữ
- Khi thực hiện kiểm thử thành công một trường hợp hoặc phát hiện lỗi, chụp ảnh màn hình bằng tổ hợp phím `Windows + Shift + S`.
- Lưu ảnh vào thư mục:
  `docs/testing/evidence/<TEST_CASE_ID>.png`  
  *(Ví dụ: `docs/testing/evidence/ST-WF4-001.png`, `docs/testing/evidence/IT-REV-010.png`)*.

---

## 5. Tự Động Hóa Cập Nhật & Xác Minh Workbook Bằng Lệnh
Dự án được trang bị công cụ tự động bằng Java Apache POI để tái sinh và xác minh toàn bộ công thức Excel mà không cần cài đặt Excel hay LibreOffice:

```bash
# 1. Chạy sinh lại 2 workbook kiểm thử với dữ liệu và công thức chuẩn nhất:
mvn test -Dtest=GenerateTestingDeliverables

# 2. Chạy kiểm tra tính hợp lệ của công thức (đảm bảo 0 lỗi #REF!, #DIV/0!, #NAME?):
mvn test -Dtest=ValidateGeneratedWorkbooksTest

# 3. Chạy toàn bộ 92 bài kiểm thử tự động của dự án:
mvn clean test
```
