package com.cinema.testgen;

import java.util.ArrayList;
import java.util.List;

public class SystemTestData {

    public static List<GenerateTestingDeliverables.SheetDef> getSystemTestWorkflows() {
        List<GenerateTestingDeliverables.SheetDef> list = new ArrayList<>();

        // -------------------------------------------------------------
        // WF1: Cinema Branch Management
        // -------------------------------------------------------------
        GenerateTestingDeliverables.SheetDef wf1 = new GenerateTestingDeliverables.SheetDef(
                "WF1 Cinema Branch",
                "Quản Lý Cụm Rạp Toàn Chuỗi (Cinema Branch Management)",
                "Kiểm thử luồng nghiệp vụ quản lý vòng đời cụm rạp từ thêm mới, sửa đổi thông tin, kiểm tra trùng lặp mã rạp, đổi trạng thái hoạt động đến xóa mềm theo SRS M-04.1."
        );

        GenerateTestingDeliverables.ScenarioGroup wf1_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Tạo mới cụm rạp và kiểm tra hiển thị đồng bộ");
        wf1_s1.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF1-001",
                "Admin tạo cụm rạp mới với thông tin hợp lệ",
                "1. Đăng nhập Admin (admin@cinemax.vn / 123456).\n2. Vào menu 'Quản lý Cụm Rạp' (/admin/infrastructure/cinemas).\n3. Bấm 'Thêm Cụm Rạp', nhập mã 'C-HP01', Tên 'CineMax Hải Phòng', Thành phố 'Hải Phòng', Địa chỉ 'Lê Chân, HP', Hotline '19006001'.\n4. Bấm 'Lưu lại'.",
                "Hệ thống thông báo thêm thành công; bản ghi C-HP01 hiển thị trong danh sách rạp; DB bảng cinemas có record mới với is_deleted=0, is_active=1.",
                "Tài khoản Admin hợp lệ, database đã kết nối.",
                "Passed", "2026-11-03", "DungBD", "Đã xác minh qua live app & SQL query"
        ));
        wf1_s1.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF1-002",
                "Kiểm tra cụm rạp mới xuất hiện tại bộ lọc các màn hình con",
                "1. Vào màn hình 'Quản lý Phòng Chiếu' (/admin/infrastructure/rooms).\n2. Mở dropdown chọn Cụm Rạp.\n3. Vào màn hình 'Báo Cáo Tỷ Lệ Lấp Đầy' (/admin/infrastructure/occupancy).\n4. Mở dropdown chọn Cụm Rạp.",
                "Cụm rạp mới 'CineMax Hải Phòng (C-HP01)' xuất hiện trong cả 2 dropdown lọc rạp của hệ thống.",
                "Cụm rạp C-HP01 đã được tạo thành công ở ST-WF1-001.",
                "Passed", "2026-11-03", "DungBD", "Xác minh dropdown bind đúng qua DAO"
        ));
        wf1.scenarios.add(wf1_s1);

        GenerateTestingDeliverables.ScenarioGroup wf1_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Cập nhật thông tin chi nhánh & validation");
        wf1_s2.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF1-003",
                "Cập nhật thông tin cụm rạp và đổi hotline",
                "1. Tại danh sách rạp, bấm 'Sửa' tại dòng 'CineMax Hải Phòng'.\n2. Đổi hotline thành '19006099', bổ sung mô tả rạp.\n3. Bấm 'Cập nhật'.",
                "Thông báo cập nhật thành công; thông tin mới được lưu và hiển thị chuẩn xác; DB bảng cinemas cập nhật updated_at.",
                "Cụm rạp C-HP01 tồn tại.",
                "Passed", "2026-11-03", "DungBD", "Xác minh qua DAO updateCinema"
        ));
        wf1_s2.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF1-004",
                "Thêm rạp trùng mã code đã tồn tại",
                "1. Bấm 'Thêm Cụm Rạp'.\n2. Nhập mã trùng 'C-HN01' (đã có của rạp Bà Triệu).\n3. Bấm 'Lưu lại'.",
                "Hệ thống báo lỗi 'Mã cụm rạp đã tồn tại, vui lòng chọn mã khác!'; không thêm dòng mới vào DB.",
                "Mã C-HN01 đã tồn tại trong database.",
                "Passed", "2026-11-03", "DungBD", "Validation constraint bắt lỗi chuẩn"
        ));
        wf1.scenarios.add(wf1_s2);

        GenerateTestingDeliverables.ScenarioGroup wf1_s3 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 3: Chuyển đổi trạng thái hoạt động / Tạm dừng");
        wf1_s3.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF1-005",
                "Chuyển cụm rạp sang trạng thái Tạm ngưng hoạt động",
                "1. Tại danh sách rạp, tìm rạp 'CineMax Hải Phòng'.\n2. Bấm nút chuyển đổi trạng thái 'Tạm ngưng'.\n3. Xác nhận popup cảnh báo.",
                "Trạng thái rạp chuyển sang 'Tạm ngưng'; badge màu xám; DB cập nhật is_active = 0.",
                "Rạp C-HP01 đang hoạt động.",
                "Passed", "2026-11-03", "DungBD", "Verified toggle status DB & UI"
        ));
        wf1_s3.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF1-006",
                "Kiểm tra hiệu ứng rạp tạm ngưng trên báo cáo so sánh",
                "1. Vào 'So Sánh Hiệu Suất Rạp' (/admin/reports/comparison).\n2. Không tích 'Hiện rạp tạm ngưng' -> Kiểm tra danh sách.\n3. Tích 'Hiện rạp tạm ngưng' -> Bấm Áp dụng.",
                "Khi chưa tích checkbox: rạp C-HP01 không hiển thị; Khi tích checkbox: rạp C-HP01 hiển thị với nhãn 'Tạm ngưng'.",
                "Rạp C-HP01 có is_active = 0.",
                "Passed", "2026-11-03", "DungBD", "BranchComparisonDAO filter includeInactive"
        ));
        wf1.scenarios.add(wf1_s3);

        GenerateTestingDeliverables.ScenarioGroup wf1_s4 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 4: Xóa mềm rạp và kiểm tra ràng buộc toàn vẹn dữ liệu");
        wf1_s4.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF1-007",
                "Chặn xóa rạp khi đang có phòng chiếu trực thuộc",
                "1. Tìm rạp 'CineMax Bà Triệu (C-HN01)' (đang có 4 phòng chiếu).\n2. Bấm nút 'Xóa'.\n3. Xác nhận xóa.",
                "Hệ thống cảnh báo lỗi: 'Không thể xóa cụm rạp đang có phòng chiếu hoạt động! Hãy xóa hoặc di dời phòng chiếu trước.'; rạp không bị xóa.",
                "Rạp C-HN01 có phòng chiếu liên kết.",
                "Passed", "2026-11-03", "DungBD", "Referential integrity guard verified"
        ));
        wf1_s4.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF1-008",
                "Xóa mềm cụm rạp không có ràng buộc",
                "1. Tạo rạp thử nghiệm 'C-TEST01' không có phòng chiếu.\n2. Bấm 'Xóa' rạp C-TEST01.\n3. Xác nhận.",
                "Xóa thành công; rạp biến mất khỏi danh sách; DB cập nhật is_deleted = 1 (không xóa vật lý hard-delete).",
                "Rạp C-TEST01 không có phòng chiếu.",
                "Passed", "2026-11-03", "DungBD", "Soft delete pattern verified"
        ));
        wf1.scenarios.add(wf1_s4);

        list.add(wf1);

        // -------------------------------------------------------------
        // WF2: Showtime & Room Management
        // -------------------------------------------------------------
        GenerateTestingDeliverables.SheetDef wf2 = new GenerateTestingDeliverables.SheetDef(
                "WF2 Room & Seat",
                "Quản Lý Phòng Chiếu & Sơ Đồ Ghế (Showtime & Room Management)",
                "Kiểm thử toàn bộ vòng đời thiết lập hạ tầng phòng chiếu: tạo phòng, thiết kế ma trận ghế (A-Z x 1-N), phân bổ loại ghế (Standard/VIP/Couple), đồng bộ sức chứa và cơ chế khóa bảo trì ghế (SRS M-04.2, M-04.3)."
        );

        GenerateTestingDeliverables.ScenarioGroup wf2_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Tạo phòng chiếu và tự động sinh layout ghế");
        wf2_s1.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF2-001",
                "Admin tạo phòng chiếu mới chuẩn IMAX",
                "1. Vào menu 'Quản lý Phòng Chiếu' (/admin/infrastructure/rooms).\n2. Bấm 'Thêm Phòng', chọn rạp 'CineMax Bà Triệu', nhập tên 'Phòng 05 (IMAX Laser)', chuẩn 'IMAX', số hàng 8, số cột 12.\n3. Bấm 'Khởi tạo phòng'.",
                "Phòng chiếu được tạo thành công; hệ thống tự động sinh 96 ghế theo ma trận A01-H12; tổng sức chứa total_capacity = 96.",
                "Rạp C-HN01 đang hoạt động.",
                "Passed", "2026-11-03", "DungBD", "ScreeningRoomService auto seat generator"
        ));
        wf2_s1.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF2-002",
                "Kiểm tra hiển thị trực quan trên sơ đồ ghế (Seat Grid)",
                "1. Vào màn hình 'Thiết Kế Sơ Đồ Ghế' (/admin/infrastructure/seats).\n2. Chọn rạp C-HN01, chọn 'Phòng 05 (IMAX Laser)'.\n3. Quan sát ma trận hiển thị.",
                "Ma trận hiển thị đủ 8 hàng (A đến H) và 12 cột (01 đến 12); màn hình Screen ở phía trên; tất cả ghế có trạng thái 'AVAILABLE'.",
                "Phòng 05 đã được tạo ở ST-WF2-001.",
                "Passed", "2026-11-03", "DungBD", "SeatGridServlet rendering verified"
        ));
        wf2.scenarios.add(wf2_s1);

        GenerateTestingDeliverables.ScenarioGroup wf2_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Cấu hình loại ghế VIP và Ghế Đôi (Couple)");
        wf2_s2.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF2-003",
                "Chuyển đổi phân hạng ghế Standard sang VIP và Couple",
                "1. Tại giao diện Seat Grid của Phòng 05, chọn công cụ 'Gán loại ghế VIP'.\n2. Click chọn các ghế hàng D (D04, D05, D06, D07, D08).\n3. Chọn công cụ 'Gán ghế Couple' và click hàng H (H01-H02, H03-H04).\n4. Bấm 'Lưu cấu hình layout'.",
                "Hệ thống lưu thành công; ghế VIP đổi màu vàng ánh kim; ghế đôi đổi màu tím; DB bảng seats cập nhật seat_type_id tương ứng.",
                "Layout phòng 05 đang mở.",
                "Passed", "2026-11-03", "DungBD", "Seat layout configuration verified"
        ));
        wf2.scenarios.add(wf2_s2);

        GenerateTestingDeliverables.ScenarioGroup wf2_s3 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 3: Cơ chế bảo trì ghế hỏng (Maintenance Toggle)");
        wf2_s3.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF2-004",
                "Đóng bảo trì ghế hỏng đệm / gãy tay vịn",
                "1. Tại Seat Grid, chuyển sang tab 'Quản trị Bảo Trì'.\n2. Click chọn ghế D05 (VIP).\n3. Chọn lý do 'Rách đệm da, chờ thay thế' và bấm 'Đóng bảo trì ghế'.",
                "Ghế D05 chuyển sang màu cờ lê xám với trạng thái 'MAINTENANCE'; is_active = 0; bảng maintenance_schedules được ghi nhận 1 dòng lịch sử bảo trì.",
                "Ghế D05 đang AVAILABLE.",
                "Passed", "2026-11-03", "DungBD", "Verified with 04_seed_analytics_and_maintenance"
        ));
        wf2_s3.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF2-005",
                "Mở lại trạng thái khả dụng cho ghế sau khi sửa chữa",
                "1. Click lại vào ghế D05 đang bảo trì.\n2. Bấm 'Hoàn tất bảo trì & Mở bán'.",
                "Ghế D05 chuyển lại trạng thái 'AVAILABLE'; is_active = 1; sẵn sàng phục vụ bán vé.",
                "Ghế D05 đang MAINTENANCE.",
                "Passed", "2026-11-03", "DungBD", "Toggle restore maintenance verified"
        ));
        wf2.scenarios.add(wf2_s3);

        GenerateTestingDeliverables.ScenarioGroup wf2_s4 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 4: Ghế bảo trì bị loại trừ khỏi luồng bán vé và tỷ lệ lấp đầy");
        wf2_s4.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF2-006",
                "Kiểm tra ghế bảo trì trên màn hình chọn ghế bán vé POS",
                "1. Đăng nhập tài khoản Nhân viên (staff.hn01@cinemax.vn / 123456).\n2. Vào màn hình Quầy POS (/admin/operation/pos).\n3. Chọn suất chiếu của Phòng 05.",
                "Ghế D05 hiển thị trạng thái Khóa Bảo Trì (icon cờ lê); nhân viên không thể click chọn ghế này để đặt vé.",
                "Ghế D05 đang ở trạng thái MAINTENANCE.",
                "Passed", "2026-11-03", "DungBD", "Verified business logic in POS order service"
        ));
        wf2_s4.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF2-007",
                "Kiểm tra tỷ lệ lấp đầy tự động khấu trừ ghế bảo trì",
                "1. Đăng nhập Manager chi nhánh.\n2. Vào 'Báo Cáo Tỷ Lệ Lấp Đầy' (/admin/reports/occupancy).\n3. Lọc theo Phòng 05.",
                "Công thức tính công suất ghế cung ứng (Seat Supply) tự động loại trừ các ghế đang bảo trì; báo cáo phản ánh tỷ lệ lấp đầy thực tế chuẩn xác.",
                "Phòng 05 có ghế bảo trì.",
                "Passed", "2026-11-03", "DungBD", "InfrastructureReportDAO capacity calculation"
        ));
        wf2.scenarios.add(wf2_s4);

        list.add(wf2);

        // -------------------------------------------------------------
        // WF3: System Administration
        // -------------------------------------------------------------
        GenerateTestingDeliverables.SheetDef wf3 = new GenerateTestingDeliverables.SheetDef(
                "WF3 System Admin",
                "Quản Trị Tham Số Vận Hành Hệ Thống (System Administration)",
                "Kiểm thử việc cấu hình và tác động thực thi của các tham số quy tắc nghiệp vụ toàn hệ thống: thời gian giữ ghế đặt vé, độ trễ giãn cách giữa các ca chiếu và thời gian đệm bảo trì theo SRS M-11."
        );

        GenerateTestingDeliverables.ScenarioGroup wf3_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Quản trị thời gian giữ ghế đặt vé (Hold Duration)");
        wf3_s1.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF3-001",
                "Admin điều chỉnh thời gian giữ ghế đặt vé trực tuyến",
                "1. Đăng nhập Admin.\n2. Vào 'Cấu Hình Hệ Thống' (/admin/infrastructure/settings).\n3. Tại tham số 'BOOKING_HOLD_TIME_MINUTES', sửa từ 5 thành 7 phút.\n4. Bấm 'Lưu Cấu Hình'.",
                "Hệ thống thông báo 'Cập nhật cấu hình thành công!'; giá trị 7 được lưu vào bảng system_settings.",
                "Quyền Admin, kết nối DB thông suốt.",
                "Passed", "2026-11-03", "DungBD", "SettingAdminServlet & SystemSettingDAO verified"
        ));
        wf3_s1.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF3-002",
                "Kiểm tra luồng hủy ghế tự động khi quá thời gian giữ ghế",
                "1. Khách hàng thực hiện giữ chỗ vé trên web nhưng không thanh toán.\n2. Hệ thống kiểm tra thời gian quá 7 phút.",
                "Booking chuyển trạng thái 'EXPIRED'; các ghế được giải phóng tự động về trạng thái 'AVAILABLE' cho khách khác đặt.",
                "BOOKING_HOLD_TIME_MINUTES = 7.",
                "Passed", "2026-11-03", "DungBD", "BookingEngineService timeout verified"
        ));
        wf3.scenarios.add(wf3_s1);

        GenerateTestingDeliverables.ScenarioGroup wf3_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Cấu hình khoảng cách tối thiểu giữa 2 suất chiếu");
        wf3_s2.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF3-003",
                "Điều chỉnh giãn cách suất chiếu SHOWTIME_MIN_GAP_MINUTES",
                "1. Tại trang Cấu Hình Hệ Thống, sửa tham số 'SHOWTIME_MIN_GAP_MINUTES' thành 20 phút.\n2. Bấm 'Lưu Cấu Hình'.",
                "Lưu thành công giá trị 20 phút; DB bảng system_settings được cập nhật.",
                "Quyền Admin.",
                "Passed", "2026-11-03", "DungBD", "SystemSettingDAO getIntSetting verified"
        ));
        wf3_s2.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF3-004",
                "Kiểm tra validation khi xếp lịch chiếu vi phạm giãn cách",
                "1. Quản lý chi nhánh vào xếp lịch chiếu mới cho Phòng 01.\n2. Chọn giờ bắt đầu ca chiếu mới cách ca chiếu trước chỉ 10 phút (nhỏ hơn 20 phút quy định).\n3. Bấm 'Lưu suất chiếu'.",
                "Hệ thống chặn và thông báo: 'Khoảng cách giữa hai suất chiếu cùng phòng phải tối thiểu 20 phút để dọn dẹp và đón khách!'; không tạo suất chiếu.",
                "SHOWTIME_MIN_GAP_MINUTES = 20.",
                "Passed", "2026-11-03", "DungBD", "Showtime validation rule enforced"
        ));
        wf3.scenarios.add(wf3_s2);

        GenerateTestingDeliverables.ScenarioGroup wf3_s3 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 3: Kiểm soát phân quyền RBAC cấu hình hệ thống");
        wf3_s3.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF3-005",
                "Manager và Staff bị chặn khi truy cập trang Cấu hình",
                "1. Đăng nhập tài khoản Manager hoặc Staff.\n2. Cố tình gõ trực tiếp URL '/admin/infrastructure/settings' trên thanh trình duyệt.",
                "Hệ thống chặn truy cập và trả về mã lỗi HTTP 403 Forbidden; thông báo không có thẩm quyền truy cập.",
                "Tài khoản đăng nhập có Role MANAGER hoặc STAFF.",
                "Passed", "2026-11-03", "DungBD", "AuthorizationFilter RBAC verified"
        ));
        wf3.scenarios.add(wf3_s3);

        list.add(wf3);

        // -------------------------------------------------------------
        // WF4: Dashboard & Reporting
        // -------------------------------------------------------------
        GenerateTestingDeliverables.SheetDef wf4 = new GenerateTestingDeliverables.SheetDef(
                "WF4 Dashboard & Reporting",
                "Dashboard Đầu Não & Báo Cáo Doanh Thu, So Sánh Chuỗi (Dashboard & Reporting)",
                "Kiểm thử tính toàn vẹn dữ liệu tài chính xuyên suốt chuỗi: đối soát số liệu chéo (Dashboard == Báo Cáo Doanh Thu == Tổng So Sánh Liên Rạp), kiểm tra bộ lọc đa chiều và xuất báo cáo Excel/PDF (SRS M-10.1, M-10.2, M-10.3, M-11)."
        );

        GenerateTestingDeliverables.ScenarioGroup wf4_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Đối soát dữ liệu chéo 3 tầng (Cross-Check Data Integrity)");
        wf4_s1.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-001",
                "Đối soát Tổng Doanh Thu giữa Dashboard và Báo Cáo Doanh Thu",
                "1. Vào 'Tổng quan Dashboard' (/admin/dashboard), ghi nhận chỉ số 'Tổng Doanh Thu'.\n2. Vào 'Báo Cáo Doanh Thu' (/admin/reports/revenue), chọn khoảng thời gian cùng kỳ toàn bộ lịch sử (All time).\n3. So sánh 2 con số doanh thu.",
                "Doanh thu tổng trên Dashboard và Báo cáo Doanh thu khớp nhau 100% (cùng áp dụng chuẩn payments.status='SUCCESS' và bookings.status='CONFIRMED').",
                "Dữ liệu mẫu 04_seed và 05_seed đã nạp.",
                "Passed", "2026-11-03", "DungBD", "Canonical revenue query verified"
        ));
        wf4_s1.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-002",
                "Đối soát Tổng Doanh Thu với Tổng các cụm rạp trên Báo Cáo So Sánh",
                "1. Vào 'So Sánh Hiệu Suất Rạp' (/admin/reports/comparison).\n2. Chọn cùng khoảng thời gian với Báo cáo Doanh Thu.\n3. Lấy tổng cột 'Doanh Thu Kỳ Này' của tất cả các cụm rạp cộng lại.",
                "Tổng doanh thu của tất cả cụm rạp trên bảng so sánh bằng chính xác Tổng Doanh Thu trên Báo Cáo Doanh Thu và Dashboard.",
                "Cùng phạm vi thời gian lọc.",
                "Passed", "2026-11-03", "DungBD", "Mathematical reconciliation verified"
        ));
        wf4.scenarios.add(wf4_s1);

        GenerateTestingDeliverables.ScenarioGroup wf4_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Kiểm tra loại trừ các đơn hàng không hợp lệ");
        wf4_s2.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-003",
                "Loại trừ đơn hàng CANCELLED, FAILED, EXPIRED khỏi doanh thu",
                "1. Kiểm tra trong DB các đơn hàng có trạng thái CANCELLED, FAILED hoặc thanh toán thất bại.\n2. Đối chiếu với bảng chi tiết đơn hàng trên Báo Cáo Doanh Thu (/admin/reports/revenue).",
                "Tất cả các đơn hàng CANCELLED, FAILED, EXPIRED tuyệt đối không xuất hiện trong bảng chi tiết và không được tính vào KPI Tổng Doanh Thu.",
                "Có các đơn hàng thất bại trong seed 05.",
                "Passed", "2026-11-03", "DungBD", "RevenueReportDAO status guard verified"
        ));
        wf4.scenarios.add(wf4_s2);

        GenerateTestingDeliverables.ScenarioGroup wf4_s3 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 3: Bộ lọc đa chiều và phân tích báo cáo doanh thu");
        wf4_s3.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-004",
                "Lọc doanh thu theo cụm rạp cụ thể và kênh bán hàng (ONLINE vs POS)",
                "1. Tại Báo Cáo Doanh Thu, chọn Rạp 'CineMax Bà Triệu (C-HN01)', kênh 'POS'.\n2. Bấm 'Áp dụng bộ lọc'.",
                "KPI và biểu đồ cập nhật lọc chính xác doanh thu bán vé tại quầy của riêng rạp Bà Triệu; bảng chi tiết đơn hàng chỉ hiển thị kênh POS của rạp C-HN01.",
                "Dữ liệu có phát sinh giao dịch POS tại C-HN01.",
                "Passed", "2026-11-03", "DungBD", "Multi-criteria filter verified"
        ));
        wf4_s3.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-005",
                "Validation khoảng ngày lọc: Ngày bắt đầu lớn hơn ngày kết thúc",
                "1. Nhập 'Từ ngày' là 2026-11-30 và 'Đến ngày' là 2026-11-01.\n2. Bấm 'Áp dụng'.",
                "Hệ thống tự động đảo ngược thông minh hoặc báo lỗi thân thiện; không bao giờ xảy ra lỗi 500 hoặc crash hệ thống.",
                "Giao diện Báo Cáo Doanh Thu.",
                "Passed", "2026-11-03", "DungBD", "RevenueReportService validation unit test passed"
        ));
        wf4.scenarios.add(wf4_s3);

        GenerateTestingDeliverables.ScenarioGroup wf4_s4 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 4: So sánh hiệu suất liên chi nhánh & Phân tầng");
        wf4_s4.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-006",
                "Xếp hạng doanh thu và tính toán % tăng trưởng kỳ trước (MoM)",
                "1. Vào 'So Sánh Hiệu Suất Rạp' (/admin/reports/comparison).\n2. Chọn chế độ 'Kỳ liền trước (MoM)'.\n3. Quan sát thứ hạng và % tăng trưởng.",
                "Các cụm rạp được xếp hạng từ 1 đến N theo doanh thu giảm dần; cột tăng trưởng hiển thị đúng tỷ lệ % tăng/giảm so với kỳ trước; cụm rạp dẫn đầu được gắn huy hiệu Hạng 1.",
                "Dữ liệu kỳ hiện tại và kỳ trước đã có sẵn.",
                "Passed", "2026-11-03", "DungBD", "BranchComparisonService ranking & growth verified"
        ));
        wf4_s4.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-007",
                "Phân tầng hiệu suất cụm rạp (Top Tier, Trên TB, Dưới TB, Cần Cải Thiện)",
                "1. Quan sát cột 'Phân Tầng' trên bảng so sánh.\n2. So sánh doanh thu rạp với 'Doanh thu TB mỗi rạp' trên thẻ KPI.",
                "Rạp có doanh thu >= 120% TB được gắn badge 'Top Tier'; rạp >= 100% gắn 'Trên TB'; rạp < 70% gắn badge cảnh báo 'Cần Cải Thiện'.",
                "Chỉ số Doanh thu TB toàn chuỗi đã được tính.",
                "Passed", "2026-11-03", "DungBD", "BranchComparisonService tier classification passed"
        ));
        wf4.scenarios.add(wf4_s4);

        GenerateTestingDeliverables.ScenarioGroup wf4_s5 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 5: Xuất báo cáo tài chính ra Excel (.xlsx) và PDF");
        wf4_s5.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-008",
                "Xuất Báo Cáo Doanh Thu ra file Excel chuẩn định dạng",
                "1. Tại trang Báo Cáo Doanh Thu, bấm 'Xuất Excel'.\n2. Mở file .xlsx vừa tải về.",
                "File Excel mở bình thường, gồm 4 sheets (Tổng Quan KPI, Doanh Thu Theo Phim, Theo Rạp, Chi Tiết Đơn Hàng); số liệu định dạng tiền tệ '#,##0 ₫' khớp 100% với màn hình web.",
                "Apache POI 5.2.5 đã tích hợp.",
                "Passed", "2026-11-03", "DungBD", "ReportExportService Excel export verified"
        ));
        wf4_s5.add(new GenerateTestingDeliverables.TestCaseData(
                "ST-WF4-009",
                "Xuất Báo Cáo So Sánh Cụm Rạp ra file PDF tiếng Việt",
                "1. Tại trang So Sánh Rạp, bấm 'Xuất PDF'.\n2. Mở file .pdf vừa tải về.",
                "File PDF khổ ngang (A4 Landscape) hiển thị đầy đủ bảng so sánh đa chiều; font chữ Arial hiển thị hoàn hảo dấu tiếng Việt, không bị lỗi font ô vuông hay ?.",
                "OpenPDF 1.3.40 với font Arial Unicode.",
                "Passed", "2026-11-03", "DungBD", "ReportExportService PDF export verified"
        ));
        wf4.scenarios.add(wf4_s5);

        list.add(wf4);

        return list;
    }
}
