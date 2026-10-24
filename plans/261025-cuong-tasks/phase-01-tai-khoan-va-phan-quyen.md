---
phase: 1
title: "Tai khoan va phan quyen"
status: completed
priority: P1
dependencies: []
effort: "1-2 ngay"
---

# Phase 1: Tài khoản và phân quyền

## Overview

Đóng M-01.2 bằng kiểm tra luồng quên mật khẩu đang chạy. Làm M-01.3 và cả hai ý của M-01.4 trên một màn admin: tìm tài khoản, khóa/mở, gán vai trò và chi nhánh.

## Requirements

- Functional: khách đã đăng nhập sửa họ tên, số điện thoại, avatar URL và đổi mật khẩu khi nhập đúng mật khẩu cũ. Số điện thoại giữ nguyên hoặc vẫn thuộc đúng user thì lưu được. Số của user khác thì từ chối. Admin tìm theo tên hoặc email, lọc vai trò và trạng thái, khóa/mở, gán `STAFF` hoặc `MANAGER` vào một rạp. Không gán `ADMIN` và không hạ về `CUSTOMER` trên màn này.
- Non-functional: servlet chỉ gọi `UserService`. Mật khẩu đi qua `PasswordUtil`. Không khóa chính mình. Không khóa admin active cuối cùng. Không đổi vai trò của user đang là ADMIN.

## Architecture

`/customer/account/profile` đã nằm trong `AuthenticationFilter` (`web.xml` map `/customer/account/*`). `/admin/identity/users` đã bị `AuthorizationFilter` chặn Manager nếu không phải đường `/admin/identity/staff`. Admin đi tiếp vì filter cho Admin toàn quyền.

`UserDAO` đã có `updateProfile`, `updateStatus`, `findAll`, `findByPhone`. `isPhoneTaken` nằm ở `UserServiceImpl`, và trả true cả khi số thuộc chính user đó. Thiếu tìm kiếm có lọc và thiếu cập nhật `role_id` + `cinema_id`. Thêm hai method đó vào DAO, bọc bằng `UserService`. Servlet không import DAO.

<!-- Updated: Validation Session 1 - phone check excludes current user; assignable roles are STAFF and MANAGER only -->

## Related Code Files

- Create: `src/main/java/com/cinema/modules/identity/controller/ProfileServlet.java`
- Create: `src/main/webapp/WEB-INF/views/customer/account/profile.jsp`
- Create: `src/main/java/com/cinema/modules/identity/controller/UserAdminServlet.java`
- Create: `src/main/webapp/WEB-INF/views/admin/identity/users.jsp`
- Modify: `UserService.java`, `UserServiceImpl.java`, `UserDAO.java`
- Modify: `src/main/webapp/WEB-INF/views/common/header.jsp` — thêm mục Hồ sơ trong dropdown
- Modify: `src/main/webapp/WEB-INF/views/common/sidebar.jsp` — thêm link Quản lý tài khoản cho Admin
- Không sửa: `AuthServlet` trừ khi bước kiểm tra quên mật khẩu phát hiện lỗi thật

## Implementation Steps

1. Kiểm tra quên mật khẩu, không viết lại nếu đủ các điều kiện sau: `/forgot-password` gửi link token, token hết hạn sau 15 phút, `/reset-password` từ chối token sai hoặc hết hạn, mật khẩu mới được hash BCrypt. Nếu thiếu một điều kiện thì sửa đúng chỗ đó trong `UserServiceImpl` / `AuthServlet`.
2. Thêm `UserService.updateProfile` gọi `UserDAO.updateProfile`. Lấy chủ số bằng `findByPhone`. Từ chối chỉ khi chủ số là user khác. Số không đổi thì lưu. Không gọi thẳng `isPhoneTaken` cho case này.
3. `ProfileServlet` map `/customer/account/profile`. GET đổ user trong session. POST `action=profile` cập nhật hồ sơ rồi ghi lại `currentUser` trong session. POST `action=password` gọi `changePassword` đã có.
4. Thêm `UserDAO.search(keyword, roleId, status, page, pageSize)` và `updateRoleAndCinema(userId, roleId, cinemaId)`. `CUSTOMER` thì `cinema_id` để null. `STAFF` và `MANAGER` bắt buộc có `cinema_id` trỏ tới rạp đang active.
5. `UserAdminServlet` map `/admin/identity/users`. GET danh sách có lọc. POST `toggleStatus` chỉ đổi `ACTIVE` và `LOCKED`. Từ chối nếu đó là chính mình hoặc là admin active cuối cùng. POST `assign` chỉ nhận `STAFF` hoặc `MANAGER`, bắt buộc có rạp. Từ chối nếu user hiện tại đang là `ADMIN`, hoặc role mới là `ADMIN` hay `CUSTOMER`.
6. JSP admin theo layout sidebar sẵn (xem `cinemas.jsp`). JSP hồ sơ dùng header khách, không dùng sidebar admin.
7. Header dropdown thêm "Hồ sơ" tới `/customer/account/profile` cho mọi user đã đăng nhập.

## Success Criteria

- [x] Quên mật khẩu vẫn gửi token 15 phút và không nhận token hết hạn. Luồng đã có từ trước, chưa test lại SMTP trong phiên này.
- [x] Khách sửa tên và giữ nguyên điện thoại của mình thì lưu được. Điện thoại của user khác bị từ chối. Đổi mật khẩu sai mật khẩu cũ thì thất bại.
- [x] Admin tìm được user, khóa được user khác. Đăng nhập đúng mật khẩu của tài khoản LOCKED hiện "Tài khoản đã bị khóa".
- [x] Admin gán Staff hoặc Manager vào một rạp. CUSTOMER và ADMIN không có form gán chi nhánh. Gán ADMIN hoặc CUSTOMER bị từ chối.
- [x] Admin không tự khóa mình và không khóa admin active cuối cùng. User đang là ADMIN không bị đổi vai trò trên màn này.
- [x] Manager mở `/admin/identity/users` nhận 403.
- [x] Danh sách tài khoản có cột STT liên tục theo trang.

## Risk Assessment

Khóa admin active cuối cùng sẽ mất cửa vào hệ thống. Chặn ở service, không chỉ ở nút trên JSP. Màn này không đổi vai trò ADMIN vì không có đường gán lại ADMIN. Gán Staff mà quên `cinema_id` làm POS và lịch chiếu của Manager lệch rạp. Bắt buộc chọn rạp trước khi lưu.
