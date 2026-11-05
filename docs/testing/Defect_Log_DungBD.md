# BẢNG GHI NHẬN LỖI PHÁT HIỆN & KHẮC PHỤC (DEFECT LOG) – DungBD
**Dự án:** CineMax – Multi-Branch Cinema Management System (SWP391-Gr3)  
**Người thực hiện:** Bùi Đức Dũng (DungBD) – Team Leader / Infrastructure Architect & BI Specialist  
**Ngày cập nhật:** 03/11/2026  

---

## 1. Danh Sách Lỗi Phát Hiện & Đã Xử Lý (Resolved Defects)

| Defect ID | Màn hình / Thành phần | Mô tả lỗi (Symptom & Root Cause) | Mức độ | Trạng thái | Giải pháp khắc phục & File xử lý |
|---|---|---|---|---|---|
| **DEF-001** | `Template3.2_System-Test.xlsx` | Lỗi công thức `=#REF!` tại ô D3 và D4 trên sheet `Test Cases` do link tham chiếu Cover bị đứt gãy. | High | **Closed** | Sửa công thức D3 thành `=Cover!F15` (Project Name) và D4 thành `=Cover!F16` (Project Code). Khắc phục tự động qua POI generator. |
| **DEF-002** | `Template3.2_System-Test.xlsx` | Lỗi công thức hàng 7 và 8 (Round 2 và 3) đếm nhầm cột `F` (của Round 1) thay vì đếm cột `I` (Round 2) và cột `L` (Round 3). | High | **Closed** | Cập nhật formula: Round 2 đếm `$I$11:$I$500`, Round 3 đếm `$L$11:$L$500`. |
| **DEF-003** | `Template3.2_System-Test.xlsx` | Công thức `Number of TCs` dùng `COUNTA(A12:A1000)` đếm cả dòng tiêu đề Scenario, dẫn đến lệch tổng số ca test. | Medium | **Closed** | Sửa thành `COUNTIF($F$11:$F$500, "<>")` chỉ đếm các dòng test case thực tế (vì dòng Scenario để trống cột F). |
| **DEF-004** | `Template3.2_System-Test.xlsx` | Công thức tỷ lệ phủ `Test coverage` trên sheet `Test Statistics` chia cho `(H14-G14)` bị lỗi `#DIV/0!` khi mẫu số bằng 0. | Medium | **Closed** | Bọc toàn bộ công thức với `IFERROR((D14+E14)/(H14-G14), 0)`. |
| **DEF-005** | `05_seed_revenue_reports.sql` | Xung đột khóa ngoại `fk_tickets_seat` khi seed dữ liệu vé mẫu do hardcode seat_id từ 1 đến 4 trong khi bảng seats thực tế sinh ID từ 2201+. | High | **Closed** | Sửa script seed sang truy vấn động ID ghế thực tế: `SET @s1_r1 = (SELECT TOP 1 id FROM seats WHERE screening_room_id = 1)`. |
| **DEF-006** | `RevenueReportDAO.java` | Lỗi nhân đôi doanh thu thanh toán (fan-out aggregation) khi 1 đơn hàng booking có nhiều vé và liên kết với order_items F&B. | Critical | **Closed** | Áp dụng CTEs (`WITH BookingMetrics AS (...)`) để gom nhóm và tính doanh thu thanh toán trước khi join vé. |
| **DEF-007** | `AuthorizationFilter.java` | Tài khoản Manager vẫn có thể truy cập nhầm vào URL Báo Cáo Doanh Thu và So Sánh Rạp do filter chỉ kiểm tra danh mục phim và F&B. | High | **Closed** | Bổ sung kiểm tra chặn 403 Forbidden cho Manager đối với `/admin/reports/revenue*` và `/admin/reports/comparison*`. |
| **DEF-008** | `OpenPDF Font Encoding` | File PDF xuất ra bị lỗi hiển thị font dấu tiếng Việt ô vuông hoặc dấu hỏi khi dùng font mặc định Helvetica/Times-Roman. | Medium | **Closed** | Nhúng font Unicode `src/main/resources/fonts/Arial.ttf` với bảng mã `BaseFont.IDENTITY_H` và `BaseFont.EMBEDDED`. |
| **DEF-009** | `BranchComparisonDAO.java` | Lỗi biên dịch `cannot find symbol variable java` tại khai báo Logger do gõ nhầm `BranchComparisonDAO.java` thay vì `.class`. | Low | **Closed** | Sửa thành `LoggerFactory.getLogger(BranchComparisonDAO.class)`. |

---

## 2. Kết Luận Kiểm Định Chất Lượng
- 100% các lỗi nghiêm trọng về dữ liệu tài chính, phân quyền RBAC và lỗi template đã được triệt tiêu hoàn toàn.
- Toàn bộ 92 automated tests chạy qua lệnh `mvn test` đều đạt kết quả **BUILD SUCCESS (0 Failures, 0 Errors)**.
