# Pet Shop Management

Ứng dụng desktop quản lý cửa hàng thú cưng, Java 21 + JavaFX + SQL Server.

## Luồng nghiệp vụ chính

1. Nhà cung cấp → Tạo phiếu nhập.
2. Phiếu nhập có thể chứa thú cưng và phụ kiện.
3. Mỗi thú cưng nhập kho là một cá thể riêng, có mã `TCxxxxxx` và trạng thái `AVAILABLE`.
4. Phụ kiện nhập sẽ tăng tồn kho.
5. POS chỉ cho bán thú cưng đang `AVAILABLE` và phụ kiện còn đủ tồn theo ngưỡng tối thiểu.
6. Thanh toán thành công chuyển thú cưng sang `SOLD`, trừ phụ kiện và ghi nhật ký tồn kho.
7. Hủy hóa đơn hoàn trả trạng thái/tồn kho và ghi giao dịch hoàn tác.
8. ADMIN có thể hủy phiếu nhập; hệ thống chỉ cho phép hoàn tác khi hàng vẫn còn đủ điều kiện.
9. Báo cáo dùng dữ liệu hóa đơn đã thanh toán để tính doanh thu/lợi nhuận.

## Công nghệ

- Java 21
- JavaFX 21
- AtlantaFX
- SQL Server
- JDBC + HikariCP
- Apache POI
- iText 7
- BCrypt
- Maven

## Cài đặt

1. Cài JDK 21 và SQL Server.
2. Mở `sql/01_create_database.sql`, sau đó `02_create_tables.sql`, `03_seed_data.sql`.
3. Chạy `sql/04_migration_pet_code.sql` nếu database cũ chưa có `Pets.pet_code`.
4. Có thể chạy `sql/05_business_upgrade.sql` để áp dụng đầy đủ nâng cấp nghiệp vụ. Ứng dụng cũng tự chạy các migration nhỏ khi khởi động.
5. Chỉnh `src/main/resources/db.properties` theo SQL Server của máy.
6. Mở project bằng NetBeans/IntelliJ và chạy Maven goal `javafx:run`.

## Nghiệp vụ nhập thú cưng

Không nhập thú cưng bằng cách tăng một số lượng chung. Mỗi con là một record `Pets`. Phiếu nhập là chứng từ đầu vào; sau khi lưu, Pet được tạo với nhà cung cấp, giá nhập, giá bán, ngày nhập và trạng thái `AVAILABLE`.

## Tài khoản mẫu

Xem `sql/03_seed_data.sql` để biết tài khoản seed. Không dùng mật khẩu mẫu trong môi trường thực tế.
