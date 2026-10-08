# Project
Vẽ UML Use Case Diagram cho đồ án SWP391 - Hệ Thống Quản Lý Chuỗi Rạp Chiếu Phim Đa Chi Nhánh (CineMax System), dùng đúng 4 tác nhân con người (Human Actors) và các Use Case trong SRS mục 4.1 Actors và 4.2 Use Cases.

# Stack
1 file HTML tự chứa (index.html), inline SVG viết tay bằng toạ độ, không dùng thư viện ngoài (không mermaid, không draw.io export, không CDN).
Không build step, mở trực tiếp bằng trình duyệt là ra kết quả cuối cùng với đầy đủ công cụ tải SVG và ảnh PNG 2K.

# UML convention bắt buộc
- **Actor**: stick figure (đầu tròn + thân + 2 tay + 2 chân), tên actor in đậm ngay dưới chân hình.
  - 4 Tác nhân con người: **Guest**, **Customer**, **Cinema Staff**, **Administrator**.
  - Các dịch vụ kết nối bên ngoài (**VNPay / MoMo Payment Gateway**, **Email Service / SMTP**, **Google OAuth 2.0**) được thể hiện bằng khối hình chữ nhật khuôn mẫu `<<System>>` hoặc `<<External Service>>` ở biên phải sơ đồ.
- **Use case**: ellipse viền đen nét liền, nền trắng (hoặc tô màu pastel nhẹ), tên UC canh giữa, ellipse phải đủ rộng để không đè chữ ra ngoài viền.
- **Association (actor-UC)**: đường thẳng liền nét, không mũi tên.
- **<<extend>>**: đường nét đứt, mũi tên hở (open arrowhead) chỉ từ UC mở rộng về UC gốc, nhãn "<<extend>>" đặt giữa đường, chữ nghiêng nhỏ.
- **<<include>>**: đường nét đứt, mũi tên hở chỉ từ UC gốc tới UC được include, nhãn "<<include>>" đặt giữa đường, chữ nghiêng nhỏ.
- **Generalization (actor kế thừa actor)**: đường liền nét, đầu mũi tên là tam giác rỗng (hollow triangle), chỉ từ actor con lên actor cha (Customer kế thừa Guest).
- **Note box (dùng cho use case dạng CRUD gộp)**: hình chữ nhật góc trên-phải bị gập (dog-ear), viền đen, nối tới use case bằng đường nét đứt KHÔNG mũi tên, nội dung note chỉ gồm 1 dòng liệt kê thao tác con (KHÔNG có chữ "Note:").

# Output
- 4 <section> tương ứng 4 Actor theo chuẩn SRS:
  - 4.3.1 UCs for Guest (7 Use Cases & Google OAuth)
  - 4.3.2 UCs for Customer (Kế thừa Guest, Đặt vé trực tuyến, Giữ ghế 300s, Combo F&B, Thanh toán VNPay, Vé điện tử QR, Loyalty)
  - 4.3.3 UCs for Cinema Staff (Bán vé quầy POS, Két tiền mặt, Soát vé cổng QR code, Đối soát tuổi T13/T16/T18, Xếp lịch chiếu, Quản lý kho F&B)
  - 4.3.4 UCs for Administrator (Quản trị tài khoản, Phân quyền RBAC, Danh mục phim, Chi nhánh & Phòng chiếu, Lưới ghế Designer, Bảng giá vé, Báo cáo BI)
- Layout: actor bên trái, use case toả sang phải, đủ khoảng cách (tối thiểu 40px) giữa các phần tử để không đè lên nhau.
- Tích hợp thanh công cụ: Chuyển tab xem từng sơ đồ, tải từng file vector SVG, tải toàn bộ 4 SVG, kết xuất ảnh PNG độ phân giải 2K.
