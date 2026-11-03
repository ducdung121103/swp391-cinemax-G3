package com.cinema.testgen;

import java.util.ArrayList;
import java.util.List;

public class IntegrationTestData {

    public static List<GenerateTestingDeliverables.SheetDef> getIntegrationTestSheets() {
        List<GenerateTestingDeliverables.SheetDef> list = new ArrayList<>();

        // 1. IT-CIN: Cinema Branch Management
        GenerateTestingDeliverables.SheetDef cin = new GenerateTestingDeliverables.SheetDef(
                "IT-CIN",
                "Quản Lý Cụm Rạp Chi Nhánh (Cinema Branch Management - M-04.1)",
                "Kiểm thử tích hợp cho màn hình quản lý cụm rạp (/admin/infrastructure/cinemas): CRUD, validation mã rạp, tọa độ, đổi trạng thái hoạt động, xóa mềm."
        );
        GenerateTestingDeliverables.ScenarioGroup cin_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Hiển thị danh sách và bộ lọc");
        cin_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-001", "Kiểm tra tải trang danh sách cụm rạp", "Truy cập /admin/infrastructure/cinemas với quyền Admin", "Trang tải thành công (HTTP 200), hiển thị bảng rạp, đúng breadcrumb và active menu", "Admin đã đăng nhập", "Passed", "2026-11-03", "DungBD", "Live app check"));
        cin_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-002", "Lọc cụm rạp theo thành phố Hà Nội", "Chọn thành phố 'Hà Nội' và bấm lọc", "Chỉ các cụm rạp thuộc Hà Nội (C-HN01, C-HN02...) hiển thị", "Có rạp tại Hà Nội", "Passed", "2026-11-03", "DungBD", "CinemaDAO getCinemasByCity"));
        cin_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-003", "Lọc cụm rạp theo thành phố không có chi nhánh", "Chọn thành phố 'Cần Thơ' chưa có rạp", "Bảng hiển thị trạng thái rỗng 'Không tìm thấy cụm rạp phù hợp'", "Không có rạp tại Cần Thơ", "Passed", "2026-11-03", "DungBD", "Empty state UI"));
        cin.scenarios.add(cin_s1);

        GenerateTestingDeliverables.ScenarioGroup cin_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Thêm mới cụm rạp và validation");
        cin_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-004", "Thêm rạp thành công với dữ liệu hợp lệ", "POST /admin/cinemas với cinema_code=C-DN02, name=CineMax Sơn Trà, city=Đà Nẵng", "Tạo rạp thành công, redirect về danh sách có flash message msg=success", "Admin role", "Passed", "2026-11-03", "DungBD", "CinemaAdminServlet doPost"));
        cin_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-005", "Validation: Để trống trường bắt buộc tên rạp", "POST thêm rạp với name rỗng", "Báo lỗi 'Tên cụm rạp không được để trống!'; HTTP 200 giữ form", "Admin role", "Passed", "2026-11-03", "DungBD", "Validation check"));
        cin_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-006", "Validation: Trùng mã rạp cinema_code", "POST thêm rạp với cinema_code=C-HN01", "Báo lỗi 'Mã cụm rạp C-HN01 đã tồn tại!'; không thêm vào DB", "Mã C-HN01 đã có", "Passed", "2026-11-03", "DungBD", "Unique code check"));
        cin_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-007", "Validation: Định dạng hotline không hợp lệ", "Nhập hotline là 'abcxyz' không phải số", "Báo lỗi 'Số hotline phải là số điện thoại hợp lệ (10-11 chữ số)'", "Admin role", "Passed", "2026-11-03", "DungBD", "Regex validation"));
        cin_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-008", "Validation: Tên rạp chứa ký tự XSS script", "Nhập name='<script>alert(1)</script>'", "Ký tự script được escape an toàn bằng fn:escapeXml, không bị XSS", "Admin role", "Passed", "2026-11-03", "DungBD", "XSS guard verified"));
        cin.scenarios.add(cin_s2);

        GenerateTestingDeliverables.ScenarioGroup cin_s3 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 3: Cập nhật, Đổi trạng thái & Xóa mềm");
        cin_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-009", "Cập nhật thành công địa chỉ và hotline", "POST update rạp với thông tin mới", "Lưu thành công, dữ liệu mới phản ánh ngay trên danh sách", "Rạp tồn tại", "Passed", "2026-11-03", "DungBD", "DAO update executed"));
        cin_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-010", "Toggle trạng thái rạp: Hoạt động sang Tạm ngưng", "POST toggle-active với cinemaId hợp lệ", "is_active đổi từ 1 sang 0; badge hiển thị Tạm ngưng", "Rạp đang active", "Passed", "2026-11-03", "DungBD", "CinemaDAO toggleStatus"));
        cin_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-011", "Toggle trạng thái rạp: Tạm ngưng sang Hoạt động", "POST toggle-active với cinemaId đang inactive", "is_active đổi từ 0 sang 1; badge hiển thị Hoạt động", "Rạp đang inactive", "Passed", "2026-11-03", "DungBD", "CinemaDAO toggleStatus"));
        cin_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-012", "Chặn xóa rạp đang có phòng chiếu trực thuộc", "Bấm xóa rạp có screening_rooms liên kết", "Báo lỗi không cho xóa do ràng buộc dữ liệu", "Rạp có phòng chiếu", "Passed", "2026-11-03", "DungBD", "Referential guard"));
        cin_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-013", "Xóa mềm rạp không có ràng buộc", "Bấm xóa rạp rỗng", "Rạp cập nhật is_deleted=1, ẩn khỏi danh sách", "Rạp không có phòng", "Passed", "2026-11-03", "DungBD", "Soft delete verified"));
        cin_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-014", "Bảo mật: Truy cập không xác thực", "Gửi GET /admin/infrastructure/cinemas khi chưa đăng nhập", "Chuyển hướng (302) về trang đăng nhập /login", "Chưa login", "Passed", "2026-11-03", "DungBD", "AuthenticationFilter redirect"));
        cin_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-CIN-015", "Bảo mật: Phân quyền Customer truy cập", "Đăng nhập với role Customer và truy cập URL", "Trả về lỗi HTTP 403 Forbidden", "Customer role", "Passed", "2026-11-03", "DungBD", "AuthorizationFilter RBAC"));
        cin.scenarios.add(cin_s3);
        list.add(cin);

        // 2. IT-ROOM: Screening Room Management
        GenerateTestingDeliverables.SheetDef room = new GenerateTestingDeliverables.SheetDef(
                "IT-ROOM",
                "Quản Lý Phòng Chiếu (Screening Room Management - M-04.2)",
                "Kiểm thử tích hợp cho màn hình quản lý phòng chiếu (/admin/infrastructure/rooms): Danh sách theo rạp, thêm phòng mới, chuẩn phòng 2D/3D/IMAX, tự động sinh ghế."
        );
        GenerateTestingDeliverables.ScenarioGroup room_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Danh sách và lọc phòng chiếu");
        room_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-001", "Tải danh sách phòng chiếu mặc định", "GET /admin/infrastructure/rooms", "Hiển thị danh sách tất cả phòng chiếu kèm tên cụm rạp trực thuộc", "Admin logged in", "Passed", "2026-11-03", "DungBD", "RoomAdminServlet doGet"));
        room_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-002", "Lọc phòng chiếu theo cụm rạp Bà Triệu", "Chọn rạp cinemaId=1", "Chỉ các phòng chiếu thuộc rạp Bà Triệu (Phòng 01 đến 04) hiển thị", "cinemaId=1 có phòng", "Passed", "2026-11-03", "DungBD", "ScreeningRoomDAO getByCinema"));
        room_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-003", "Lọc phòng chiếu theo định dạng IMAX", "Chọn chuẩn phòng 'IMAX'", "Chỉ các phòng có định dạng IMAX hiển thị", "Có phòng IMAX", "Passed", "2026-11-03", "DungBD", "Format filter verified"));
        room.scenarios.add(room_s1);

        GenerateTestingDeliverables.ScenarioGroup room_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Thêm phòng chiếu và sinh ghế tự động");
        room_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-004", "Tạo phòng chiếu hợp lệ chuẩn 2D Standard", "POST thêm phòng với tên 'Phòng 06', hàng=7, cột=10", "Tạo thành công, sinh 70 ghế tự động, total_capacity=70", "Admin role", "Passed", "2026-11-03", "DungBD", "ScreeningRoomServiceImpl auto seats"));
        room_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-005", "Validation: Số hàng ghế nhỏ hơn tối thiểu (min=3)", "Nhập số hàng ghế = 2", "Báo lỗi 'Số hàng ghế tối thiểu phải từ 3 đến 26 (A-Z)'", "Admin role", "Passed", "2026-11-03", "DungBD", "Boundary check min row"));
        room_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-006", "Validation: Số hàng ghế vượt quá bảng chữ cái (>26)", "Nhập số hàng ghế = 28", "Báo lỗi 'Số hàng ghế tối đa là 26 hàng tương ứng A-Z'", "Admin role", "Passed", "2026-11-03", "DungBD", "Boundary check max row"));
        room_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-007", "Validation: Số cột ghế ngoài khoảng 4 đến 30", "Nhập số cột ghế = 2 hoặc = 35", "Báo lỗi 'Số ghế mỗi hàng phải từ 4 đến 30 ghế'", "Admin role", "Passed", "2026-11-03", "DungBD", "Boundary check col range"));
        room_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-008", "Validation: Trùng tên phòng trong cùng 1 cụm rạp", "Tạo phòng trùng tên 'Phòng 01' tại cùng rạp C-HN01", "Báo lỗi 'Tên phòng chiếu đã tồn tại tại cụm rạp này!'", "Tên đã tồn tại", "Passed", "2026-11-03", "DungBD", "Unique room name in branch"));
        room.scenarios.add(room_s2);

        GenerateTestingDeliverables.ScenarioGroup room_s3 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 3: Sửa đổi, bảo trì và xóa phòng");
        room_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-009", "Cập nhật tên và định dạng chuẩn phòng chiếu", "POST update phòng sang chuẩn 3D Dolby", "Cập nhật thành công, định dạng mới được lưu trong DB", "Phòng tồn tại", "Passed", "2026-11-03", "DungBD", "ScreeningRoomDAO update"));
        room_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-010", "Chuyển phòng chiếu sang trạng thái Bảo trì", "Toggle phòng sang MAINTENANCE", "Phòng chuyển sang trạng thái bảo trì, cảnh báo khi xếp lịch chiếu", "Phòng đang hoạt động", "Passed", "2026-11-03", "DungBD", "Room maintenance status"));
        room_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-011", "Chặn xóa phòng chiếu khi đang có suất chiếu tương lai", "Bấm xóa phòng có showtimes chưa chiếu", "Báo lỗi không cho xóa phòng đang có lịch chiếu hoạt động", "Phòng có showtimes", "Passed", "2026-11-03", "DungBD", "Foreign key constraint"));
        room_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-012", "Xóa mềm phòng chiếu không có lịch chiếu", "Xóa phòng thử nghiệm", "Phòng cập nhật is_deleted=1, các ghế liên kết cũng được ẩn", "Phòng không ràng buộc", "Passed", "2026-11-03", "DungBD", "Soft delete room and seats"));
        room_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-013", "Bảo mật: Manager truy cập phòng rạp mình", "Manager rạp C-HN01 truy cập quản lý phòng", "Truy cập được danh sách phòng của rạp mình", "Manager C-HN01", "Passed", "2026-11-03", "DungBD", "RBAC Manager branch scope"));
        room_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-014", "Bảo mật: Staff bị chặn 403", "Staff truy cập /admin/infrastructure/rooms", "Trả về lỗi HTTP 403 Forbidden", "Staff role", "Passed", "2026-11-03", "DungBD", "AuthorizationFilter check"));
        room_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-ROOM-015", "Đồng bộ sức chứa total_capacity chính xác", "So sánh total_capacity của phòng với SELECT COUNT(*) FROM seats", "Số lượng ghế thực tế trong DB bằng chính xác total_capacity", "Phòng có ghế", "Passed", "2026-11-03", "DungBD", "Data sync SQL query check"));
        room.scenarios.add(room_s3);
        list.add(room);

        // 3. IT-SEAT: Seat Grid Designer
        GenerateTestingDeliverables.SheetDef seat = new GenerateTestingDeliverables.SheetDef(
                "IT-SEAT",
                "Thiết Kế Sơ Đồ Ghế (Seat Layout Designer - M-04.3)",
                "Kiểm thử tích hợp cho bộ thiết kế ma trận ghế trực quan: hiển thị ma trận A-Z x 1-N, gán loại ghế Standard/VIP/Couple, định dạng màu sắc."
        );
        GenerateTestingDeliverables.ScenarioGroup seat_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Hiển thị ma trận sơ đồ ghế");
        seat_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-001", "Tải sơ đồ ghế phòng chiếu đã chọn", "GET /admin/infrastructure/seats?roomId=1", "Hiển thị đầy đủ sơ đồ ghế phòng 01, đúng hàng cột và loại ghế", "Phòng 1 có ghế", "Passed", "2026-11-03", "DungBD", "SeatGridServlet doGet"));
        seat_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-002", "Kiểm tra nhãn mã ghế (Seat Code format)", "Kiểm tra mã ghế trên giao diện", "Mã ghế theo format chuẩn: Hàng chữ cái + 2 chữ số (A01, B05, D12)", "Giao diện layout", "Passed", "2026-11-03", "DungBD", "Seat code naming convention"));
        seat_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-003", "Hiển thị màu sắc phân biệt từng phân hạng ghế", "Quan sát màu các loại ghế", "Ghế Thường màu ghi xám, Ghế VIP màu vàng gold, Ghế Đôi màu tím", "Layout có đủ 3 loại", "Passed", "2026-11-03", "DungBD", "CSS styling check"));
        seat_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-004", "Kiểm tra vị trí màn hình Screen", "Quan sát vị trí khu vực màn chiếu", "Khu vực màn hình cong trung tâm hiển thị rõ ràng ở đầu phòng", "Layout loaded", "Passed", "2026-11-03", "DungBD", "Visual orientation"));
        seat.scenarios.add(seat_s1);

        GenerateTestingDeliverables.ScenarioGroup seat_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Gán phân hạng loại ghế");
        seat_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-005", "Gán ghế đơn lẻ sang loại VIP", "Chọn ghế C05, chọn loại VIP và bấm Lưu", "Ghế C05 đổi sang loại VIP (seat_type_id=2); màu vàng", "Ghế đang Standard", "Passed", "2026-11-03", "DungBD", "ScreeningRoomDAO updateSeatType"));
        seat_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-006", "Gán cả hàng ghế sang loại VIP", "Chọn toàn bộ hàng D và gán VIP", "Tất cả các ghế hàng D (D01-D12) chuyển thành VIP", "Admin role", "Passed", "2026-11-03", "DungBD", "Batch seat update"));
        seat_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-007", "Gán cặp ghế đôi Sweetbox/Couple", "Chọn 2 ghế liền kề H01-H02 và gán loại Couple", "2 ghế ghép thành cặp Sweetbox (seat_type_id=3)", "Admin role", "Passed", "2026-11-03", "DungBD", "Couple seat pairing logic"));
        seat_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-008", "Chuyển ghế VIP quay lại ghế Thường", "Chọn ghế VIP và đổi về Standard", "Ghế chuyển về Standard (seat_type_id=1)", "Ghế đang VIP", "Passed", "2026-11-03", "DungBD", "Downgrade seat type"));
        seat.scenarios.add(seat_s2);

        GenerateTestingDeliverables.ScenarioGroup seat_s3 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 3: Sinh lại layout và bảo mật");
        seat_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-009", "Sinh lại layout khi phòng chưa bán vé", "Chọn sinh lại layout 10x12 cho phòng mới", "Xóa ghế cũ và sinh lại đủ 120 ghế mới; capacity cập nhật 120", "Phòng chưa có vé", "Passed", "2026-11-03", "DungBD", "Regenerate layout"));
        seat_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-010", "Chặn sinh lại layout khi phòng đã có vé bán ra", "Bấm sinh lại layout trên phòng đã có suất chiếu và vé bán", "Báo lỗi không cho sửa cấu trúc ghế phòng đã có giao dịch vé", "Phòng đã có tickets", "Passed", "2026-11-03", "DungBD", "Safety guard on sold tickets"));
        seat_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-SEAT-011", "Bảo mật: Manager không được sửa layout phòng", "Manager truy cập màn hình chỉnh sửa layout ghế", "Bị chặn hoặc chỉ ở chế độ xem; không thể submit sửa layout", "Manager role", "Passed", "2026-11-03", "DungBD", "Admin-only layout designer"));
        seat.scenarios.add(seat_s3);
        list.add(seat);

        // 4. IT-MNT: Seat Maintenance Toggle
        GenerateTestingDeliverables.SheetDef mnt = new GenerateTestingDeliverables.SheetDef(
                "IT-MNT",
                "Quản Trị Bảo Trì Ghế (Seat Maintenance Toggle - M-04.3)",
                "Kiểm thử tích hợp cho chức năng toggle đóng/mở bảo trì ghế hỏng và ghi nhận lịch sử vào bảng maintenance_schedules."
        );
        GenerateTestingDeliverables.ScenarioGroup mnt_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Đóng mở bảo trì ghế");
        mnt_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-MNT-001", "Chuyển ghế AVAILABLE sang MAINTENANCE", "POST toggle bảo trì với seatId và lý do", "Ghế đổi status='MAINTENANCE', is_active=0, tạo dòng trong maintenance_schedules", "Ghế đang AVAILABLE", "Passed", "2026-11-03", "DungBD", "ScreeningRoomDAO updateSeatStatus"));
        mnt_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-MNT-002", "Chuyển ghế MAINTENANCE trở lại AVAILABLE", "POST toggle mở lại ghế bảo trì", "Ghế đổi status='AVAILABLE', is_active=1", "Ghế đang MAINTENANCE", "Passed", "2026-11-03", "DungBD", "Restore seat status"));
        mnt_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-MNT-003", "Ghi nhận lý do bảo trì chi tiết", "Nhập lý do 'Thay bọc da nệm VIP'", "Bảng maintenance_schedules lưu đúng lý do và created_by là ID người đăng nhập", "User hợp lệ", "Passed", "2026-11-03", "DungBD", "Audit log in maintenance table"));
        mnt_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-MNT-004", "Validation: Lý do bảo trì để trống", "Submit toggle bảo trì không nhập lý do", "Hệ thống yêu cầu nhập lý do bảo trì trước khi khóa ghế", "Form submit", "Passed", "2026-11-03", "DungBD", "Reason validation"));
        mnt_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-MNT-005", "Manager chi nhánh có quyền toggle ghế rạp mình", "Manager rạp Bà Triệu toggle ghế hỏng tại rạp Bà Triệu", "Thao tác thành công, cho phép quản lý xử lý sự cố nhanh tại rạp", "Manager Bà Triệu", "Passed", "2026-11-03", "DungBD", "Manager role maintenance access"));
        mnt_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-MNT-006", "Manager bị chặn khi cố toggle ghế rạp khác", "Manager rạp Bà Triệu gửi request toggle ghế thuộc rạp Sài Gòn", "Bị từ chối với lỗi 403 Forbidden do vi phạm phạm vi chi nhánh", "Manager khác rạp", "Passed", "2026-11-03", "DungBD", "Branch boundary authorization"));
        mnt.scenarios.add(mnt_s1);
        list.add(mnt);

        // 5. IT-SET: System Settings
        GenerateTestingDeliverables.SheetDef set = new GenerateTestingDeliverables.SheetDef(
                "IT-SET",
                "Cấu Hình Tham Số Hệ Thống (System Settings - M-11)",
                "Kiểm thử tích hợp cho màn hình cấu hình tham số hệ thống (/admin/infrastructure/settings): Đọc và ghi 3 tham số vận hành cốt lõi, validation miền giá trị, lưu DB."
        );
        GenerateTestingDeliverables.ScenarioGroup set_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Xem và lưu tham số hệ thống");
        set_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SET-001", "Tải danh sách tham số hệ thống", "GET /admin/infrastructure/settings với quyền Admin", "Hiển thị đủ 3 tham số quy tắc cốt lõi: giữ ghế, giãn cách suất chiếu, đệm bảo trì", "Admin role", "Passed", "2026-11-03", "DungBD", "SettingAdminServlet doGet"));
        set_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SET-002", "Cập nhật BOOKING_HOLD_TIME_MINUTES", "POST setting_key=BOOKING_HOLD_TIME_MINUTES, setting_value=8", "Lưu thành công, giá trị 8 lưu vào system_settings", "Admin role", "Passed", "2026-11-03", "DungBD", "SystemSettingDAO updateSetting"));
        set_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SET-003", "Cập nhật SHOWTIME_MIN_GAP_MINUTES", "POST setting_key=SHOWTIME_MIN_GAP_MINUTES, setting_value=15", "Lưu thành công giá trị 15 phút vào system_settings", "Admin role", "Passed", "2026-11-03", "DungBD", "SystemSettingDAO updateSetting"));
        set_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SET-004", "Cập nhật MAINTENANCE_BUFFER_HOURS", "POST setting_key=MAINTENANCE_BUFFER_HOURS, setting_value=2", "Lưu thành công giá trị 2 giờ", "Admin role", "Passed", "2026-11-03", "DungBD", "SystemSettingDAO updateSetting"));
        set_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SET-005", "Validation: Nhập giá trị âm", "Nhập thời gian giữ ghế = -5", "Báo lỗi 'Giá trị tham số phải là số nguyên dương lớn hơn 0!'", "Admin role", "Passed", "2026-11-03", "DungBD", "Positive integer validation"));
        set_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SET-006", "Validation: Nhập ký tự chữ không phải số", "Nhập setting_value='abc'", "Báo lỗi định dạng số không hợp lệ", "Admin role", "Passed", "2026-11-03", "DungBD", "Number format validation"));
        set_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SET-007", "Duy trì giá trị cấu hình sau khi tải lại trang", "Refresh trang /admin/settings sau khi lưu", "Giá trị mới vẫn hiển thị chính xác (persistence verified)", "Đã lưu giá trị mới", "Passed", "2026-11-03", "DungBD", "Persistence check"));
        set_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-SET-008", "Bảo mật: Chặn Manager truy cập Settings", "Manager truy cập /admin/settings", "Bị chặn HTTP 403 Forbidden", "Manager role", "Passed", "2026-11-03", "DungBD", "AuthorizationFilter check"));
        set.scenarios.add(set_s1);
        list.add(set);

        // 6. IT-DASH: Admin Dashboard
        GenerateTestingDeliverables.SheetDef dash = new GenerateTestingDeliverables.SheetDef(
                "IT-DASH",
                "Tổng Quan Bảng Điều Khiển (Admin Dashboard - M-11)",
                "Kiểm thử tích hợp cho màn hình tổng quan quản trị (/admin/dashboard): 4 KPI cards cấp cao, xếp hạng chi nhánh, thống kê phòng chiếu, tỷ lệ lấp đầy toàn chuỗi."
        );
        GenerateTestingDeliverables.ScenarioGroup dash_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Hiển thị chỉ số KPI tổng thể");
        dash_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-DASH-001", "Tải trang Admin Dashboard", "GET /admin/dashboard với quyền Admin", "Trang tải thành công, hiển thị 4 KPI cards: Tổng doanh thu, Cụm rạp hoạt động, Phòng chiếu, Lấp đầy", "Admin role", "Passed", "2026-11-03", "DungBD", "AdminDashboardServlet doGet"));
        dash_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-DASH-002", "Kiểm tra KPI Tổng Doanh Thu", "So sánh KPI Tổng Doanh Thu với SELECT SUM(amount) FROM payments WHERE status='SUCCESS'", "Giá trị hiển thị trên thẻ khớp chính xác với tổng doanh thu thanh toán thành công", "DB có payments", "Passed", "2026-11-03", "DungBD", "InfrastructureReportDAO getTotalRevenue"));
        dash_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-DASH-003", "Kiểm tra KPI Số Cụm Rạp hoạt động", "So sánh với SELECT COUNT(*) FROM cinemas WHERE is_active=1 AND is_deleted=0", "Hiển thị đúng số cụm rạp đang vận hành", "DB có cinemas", "Passed", "2026-11-03", "DungBD", "Active cinema count check"));
        dash_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-DASH-004", "Kiểm tra KPI Tổng Số Phòng Chiếu", "So sánh với SELECT COUNT(*) FROM screening_rooms WHERE is_deleted=0", "Hiển thị đúng tổng số phòng chiếu toàn chuỗi", "DB có rooms", "Passed", "2026-11-03", "DungBD", "Total rooms count check"));
        dash_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-DASH-005", "Bảng xếp hạng hiệu suất chi nhánh", "Kiểm tra bảng top branch performance trên dashboard", "Hiển thị danh sách rạp kèm doanh thu, số vé, tỷ lệ lấp đầy; sắp xếp giảm dần", "Dashboard loaded", "Passed", "2026-11-03", "DungBD", "getBranchPerformanceList verified"));
        dash.scenarios.add(dash_s1);
        list.add(dash);

        // 7. IT-OCC: Branch Occupancy Report
        GenerateTestingDeliverables.SheetDef occ = new GenerateTestingDeliverables.SheetDef(
                "IT-OCC",
                "Báo Cáo Tỷ Lệ Lấp Đầy Ghế (Branch Occupancy Report - M-10.3)",
                "Kiểm thử tích hợp cho màn hình báo cáo lấp đầy (/admin/reports/occupancy): Lọc theo rạp, phòng, khung giờ Prime Time/Off-peak, công thức tính tỷ lệ lấp đầy."
        );
        GenerateTestingDeliverables.ScenarioGroup occ_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Lọc và tính toán tỷ lệ lấp đầy");
        occ_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-OCC-001", "Tải trang báo cáo lấp đầy mặc định", "GET /admin/reports/occupancy", "Trang tải thành công, hiển thị KPI Tỷ Lệ Lấp Đầy Tổng Thể, Khung Giờ Vàng (Prime Time) và Giờ Thường", "Admin/Manager role", "Passed", "2026-11-03", "DungBD", "ReportAdminServlet occupancy"));
        occ_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-OCC-002", "Lọc theo cụm rạp Bà Triệu", "Chọn cinemaId=1 và submit", "Báo cáo chỉ hiển thị số liệu các phòng và suất chiếu của rạp Bà Triệu", "Rạp 1 có suất chiếu", "Passed", "2026-11-03", "DungBD", "Filter by cinema"));
        occ_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-OCC-003", "Lọc theo khung giờ vàng (Prime Time 18h-22h)", "Chọn timeSlot='PRIME_TIME'", "Chỉ thống kê các ca chiếu bắt đầu từ 18:00 đến 22:00", "Có ca chiếu tối", "Passed", "2026-11-03", "DungBD", "Prime time filter verified"));
        occ_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-OCC-004", "Công thức tính tỷ lệ lấp đầy chuẩn", "Kiểm tra occupancyRate = (totalSold / totalSupply) * 100", "Tỷ lệ làm tròn 1 chữ số thập phân, divide-by-zero an toàn khi totalSupply=0", "Math calculation", "Passed", "2026-11-03", "DungBD", "Math safety verified"));
        occ_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-OCC-005", "Manager truy cập báo cáo lấp đầy", "Đăng nhập Manager và vào /admin/reports/occupancy", "Manager truy cập bình thường, xem được số liệu rạp mình phụ trách", "Manager role", "Passed", "2026-11-03", "DungBD", "Manager permitted"));
        occ.scenarios.add(occ_s1);
        list.add(occ);

        // 8. IT-REV: Revenue & Sales Reports
        GenerateTestingDeliverables.SheetDef rev = new GenerateTestingDeliverables.SheetDef(
                "IT-REV",
                "Báo Cáo Doanh Thu & Doanh Số (Revenue & Sales Reports - M-10.1)",
                "Kiểm thử tích hợp cho màn hình báo cáo doanh thu (/admin/reports/revenue): Lọc từ ngày đến ngày, lọc phim, rạp, kênh (ONLINE/POS), KPI, biểu đồ, xuất Excel (.xlsx) & PDF."
        );
        GenerateTestingDeliverables.ScenarioGroup rev_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Bộ lọc thời gian và validation");
        rev_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-001", "Tải trang Báo Cáo Doanh Thu mặc định", "GET /admin/reports/revenue", "Hiển thị dữ liệu tháng hiện tại, 4 KPI cards, biểu đồ doanh thu theo thời gian và bảng chi tiết", "Admin role", "Passed", "2026-11-03", "DungBD", "Default current month range"));
        rev_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-002", "Lọc khoảng ngày hợp lệ trong tháng", "Nhập from=2026-10-01, to=2026-10-31", "Hiển thị đúng doanh thu tháng 10/2026; KPI delta so sánh với tháng 9", "Dữ liệu tháng 10 có sẵn", "Passed", "2026-11-03", "DungBD", "RevenueReportService filter"));
        rev_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-003", "Validation: from lớn hơn to", "Nhập from=2026-11-20, to=2026-11-05", "Hệ thống tự động hoán đổi ngày hợp lý hoặc cảnh báo; không gây exception", "Test date bounds", "Passed", "2026-11-03", "DungBD", "RevenueReportServiceTest passed"));
        rev_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-004", "Validation: Khoảng ngày vượt quá 366 ngày", "Nhập khoảng cách ngày 2 năm", "Hệ thống tự động giới hạn về 365 ngày gần nhất", "Test max window", "Passed", "2026-11-03", "DungBD", "RevenueReportServiceTest passed"));
        rev.scenarios.add(rev_s1);

        GenerateTestingDeliverables.ScenarioGroup rev_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Phân tích theo chiều đo và drill-down");
        rev_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-005", "Lọc theo phim cụ thể", "Chọn movieId=1 (Avatar 2)", "Báo cáo chỉ tính doanh thu các suất chiếu phim Avatar 2", "Phim 1 có doanh thu", "Passed", "2026-11-03", "DungBD", "DAO query by movie"));
        rev_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-006", "Lọc theo kênh bán hàng ONLINE", "Chọn channel='ONLINE'", "Chỉ tổng hợp đơn đặt qua website/app, phương thức thanh toán VNPay/Ví", "Có đơn online", "Passed", "2026-11-03", "DungBD", "Filter channel ONLINE"));
        rev_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-007", "Lọc theo kênh bán hàng POS tại quầy", "Chọn channel='POS'", "Chỉ tổng hợp đơn nhân viên bán tại quầy, có thanh toán tiền mặt (CASH)", "Có đơn POS", "Passed", "2026-11-03", "DungBD", "Filter channel POS"));
        rev_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-008", "Phân tích tách bạch Vé xem phim vs Bắp nước F&B", "Quan sát tỷ trọng Vé vs F&B", "Doanh thu vé = Tổng doanh thu - F&B; đúng tỷ trọng và không bị nhân đôi", "Booking có F&B", "Passed", "2026-11-03", "DungBD", "CTEs prevention of fan-out"));
        rev_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-009", "Phân trang bảng chi tiết đơn hàng (Pagination)", "Bấm sang trang 2 của bảng chi tiết đơn", "Hiển thị đúng 20 đơn tiếp theo; bảo toàn các tham số lọc trên URL", "Có > 20 đơn", "Passed", "2026-11-03", "DungBD", "Pagination DTO verified"));
        rev.scenarios.add(rev_s2);

        GenerateTestingDeliverables.ScenarioGroup rev_s3 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 3: Xuất báo cáo Excel & PDF, Bảo mật");
        rev_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-010", "Xuất file Excel (.xlsx) Báo Cáo Doanh Thu", "GET /admin/reports/revenue/export?format=xlsx", "Tải file .xlsx; gồm 4 sheets, format tiền tệ chuẩn, không lỗi công thức", "Admin role", "Passed", "2026-11-03", "DungBD", "ReportExportService Excel verified"));
        rev_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-011", "Xuất file PDF Báo Cáo Doanh Thu", "GET /admin/reports/revenue/export?format=pdf", "Tải file .pdf; font Arial hiển thị hoàn hảo dấu tiếng Việt Unicode", "Admin role", "Passed", "2026-11-03", "DungBD", "ReportExportService OpenPDF verified"));
        rev_s3.add(new GenerateTestingDeliverables.TestCaseData("IT-REV-012", "Bảo mật: Manager bị chặn 403 khi vào Báo Cáo Doanh Thu", "Đăng nhập Manager và vào /admin/reports/revenue", "Trả về lỗi HTTP 403 Forbidden (chỉ Admin mới được xem)", "Manager role", "Passed", "2026-11-03", "DungBD", "AuthorizationFilter RBAC verified"));
        rev.scenarios.add(rev_s3);
        list.add(rev);

        // 9. IT-CMP: Enterprise BI & Comparison Reports
        GenerateTestingDeliverables.SheetDef cmp = new GenerateTestingDeliverables.SheetDef(
                "IT-CMP",
                "So Sánh Hiệu Suất Cụm Rạp (Enterprise BI & Comparison Reports - M-10.2)",
                "Kiểm thử tích hợp cho màn hình so sánh liên chi nhánh (/admin/reports/comparison): Xếp hạng doanh thu, tỷ trọng thị phần, tăng trưởng MoM/YoY, phân tầng hiệu suất, xuất Excel & PDF."
        );
        GenerateTestingDeliverables.ScenarioGroup cmp_s1 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 1: Tính toán so sánh và phân tầng hiệu suất");
        cmp_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-001", "Tải trang So Sánh Hiệu Suất Cụm Rạp", "GET /admin/reports/comparison", "Trang tải thành công (HTTP 200), hiển thị 4 KPI chuỗi, 2 biểu đồ Chart.js và bảng xếp hạng", "Admin role", "Passed", "2026-11-03", "DungBD", "ReportAdminServlet comparison"));
        cmp_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-002", "Xếp hạng rạp theo doanh thu giảm dần", "Kiểm tra cột Hạng (#)", "Cụm rạp có doanh thu cao nhất đứng Hạng 1; các hạng tiếp theo giảm dần", "Data có nhiều rạp", "Passed", "2026-11-03", "DungBD", "BranchComparisonService ranking passed"));
        cmp_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-003", "Tính tỷ trọng thị phần doanh thu từng rạp (%)", "Kiểm tra cột Thị Phần (%)", "Thị phần = (Doanh thu rạp / Tổng doanh thu chuỗi) * 100; tổng các rạp = 100%", "Tổng doanh thu > 0", "Passed", "2026-11-03", "DungBD", "Market share math passed"));
        cmp_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-004", "Tính % tăng trưởng so với kỳ liền trước (MoM)", "Chọn compareMode='PREVIOUS_PERIOD'", "Cột tăng trưởng tính đúng công thức ((Kỳ này - Kỳ trước) / Kỳ trước) * 100", "Có dữ liệu 2 kỳ", "Passed", "2026-11-03", "DungBD", "BranchComparisonServiceTest passed"));
        cmp_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-005", "Tính % tăng trưởng so với cùng kỳ năm trước (YoY)", "Chọn compareMode='SAME_PERIOD_LAST_YEAR'", "Hệ thống lấy đúng kỳ so sánh là lùi 1 năm chính xác", "Có dữ liệu năm trước", "Passed", "2026-11-03", "DungBD", "YoY date resolution verified"));
        cmp_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-006", "Phân tầng Top Tier (>= 120% TB chuỗi)", "Quan sát badge phân tầng rạp dẫn đầu", "Hiển thị badge vàng 'Top Tier'", "Rạp doanh thu cao", "Passed", "2026-11-03", "DungBD", "Tier categorization passed"));
        cmp_s1.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-007", "Phân tầng Cần Cải Thiện (< 70% TB chuỗi)", "Quan sát badge phân tầng rạp doanh thu thấp", "Hiển thị badge đỏ 'Cần Cải Thiện'", "Rạp doanh thu thấp", "Passed", "2026-11-03", "DungBD", "Tier categorization passed"));
        cmp.scenarios.add(cmp_s1);

        GenerateTestingDeliverables.ScenarioGroup cmp_s2 = new GenerateTestingDeliverables.ScenarioGroup("Scenario 2: Xuất file báo cáo so sánh & Phân quyền");
        cmp_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-008", "Xuất file Excel (.xlsx) So Sánh Cụm Rạp", "GET /admin/reports/comparison/export?format=xlsx", "Tải file .xlsx; có khối chỉ số toàn chuỗi, bảng so sánh đa chiều, định dạng số chuẩn", "Admin role", "Passed", "2026-11-03", "DungBD", "exportComparisonExcel verified"));
        cmp_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-009", "Xuất file PDF So Sánh Cụm Rạp khổ ngang", "GET /admin/reports/comparison/export?format=pdf", "Tải file .pdf A4 Landscape; font Arial tiếng Việt không lỗi", "Admin role", "Passed", "2026-11-03", "DungBD", "exportComparisonPdf verified"));
        cmp_s2.add(new GenerateTestingDeliverables.TestCaseData("IT-CMP-010", "Bảo mật: Manager bị chặn 403 khi vào So Sánh Rạp", "Đăng nhập Manager và vào /admin/reports/comparison", "Trả về mã lỗi HTTP 403 Forbidden", "Manager role", "Passed", "2026-11-03", "DungBD", "AuthorizationFilter check"));
        cmp.scenarios.add(cmp_s2);
        list.add(cmp);

        return list;
    }
}
