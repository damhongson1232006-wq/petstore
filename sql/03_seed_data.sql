-- ============================================================
-- PET SHOP MANAGEMENT — SEED DATA
-- Dữ liệu mẫu khởi đầu
-- ============================================================

USE PetShopDB;
GO

-- ============================================================
-- Users — Tài khoản mặc định
-- password: admin123  → BCrypt hash
-- password: staff123  → BCrypt hash
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'admin')
BEGIN
    INSERT INTO Users (username, password_hash, full_name, role)
    VALUES ('admin',
            '$2a$10$tDVK5YHv/SHlCkBgWm6Gfe1Pnck/9vv/2tJVh/p.LSeIjHdiA94qS',
            N'Quản trị viên', 'ADMIN');
END

IF NOT EXISTS (SELECT 1 FROM Users WHERE username = 'nhanvien')
BEGIN
    INSERT INTO Users (username, password_hash, full_name, role)
    VALUES ('nhanvien',
            '$2a$10$chDmfKO3mX1.qZttFHBNQeGYoY9J2aZXLiLma5z7xzpQhzrUi9LC.',
            N'Nhân Viên Mẫu', 'STAFF');
END
GO

-- ============================================================
-- Suppliers — Nhà cung cấp mẫu
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Suppliers WHERE name = N'Trại Giống Thú Cưng Hà Nội')
BEGIN
    INSERT INTO Suppliers (name, phone, address, contact) VALUES
    (N'Trại Giống Thú Cưng Hà Nội',   '0912345678', N'123 Nguyễn Trãi, Hà Nội',       N'Anh Minh'),
    (N'Chuỗi Cung Ứng Pet Express',    '0923456789', N'45 Lê Lợi, TP. Hồ Chí Minh',    N'Chị Lan'),
    (N'Trại Mèo Thuần Chủng Đà Lạt',  '0934567890', N'78 Trần Phú, Đà Lạt',            N'Anh Hùng');
END
GO

-- ============================================================
-- Services — Dịch vụ mẫu
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Services WHERE name = N'Tắm thú cưng cơ bản')
BEGIN
    INSERT INTO Services (name, description, price, duration) VALUES
    (N'Tắm thú cưng cơ bản',     N'Tắm sạch, sấy khô, vệ sinh tai',                     80000,  60),
    (N'Tắm + Cắt tỉa lông',      N'Tắm sạch, cắt tỉa lông theo yêu cầu, sấy khô',      200000, 120),
    (N'Vệ sinh răng miệng',       N'Đánh răng, kiểm tra nướu',                            50000,  30),
    (N'Cắt móng',                 N'Cắt móng chân tất cả 4 chân',                         30000,  15),
    (N'Khám sức khỏe tổng quát', N'Kiểm tra sức khỏe, tư vấn dinh dưỡng',               150000,  45),
    (N'Tiêm phòng dại',           N'Vaccine phòng bệnh dại',                              200000,  20),
    (N'Tiêm phòng 5-7 bệnh',     N'Vaccine phòng 5 bệnh cơ bản (chó/mèo)',              250000,  20),
    (N'Trị ve rận',               N'Tắm trị ve, rận, bọ chét',                           120000,  60);
END
GO

-- ============================================================
-- Accessories — Phụ kiện mẫu
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Accessories WHERE name = N'Thức ăn Royal Canin Mini Adult 2kg')
BEGIN
    INSERT INTO Accessories (name, category, unit, import_price, sell_price, quantity, min_quantity) VALUES
    (N'Thức ăn Royal Canin Mini Adult 2kg',  N'Thức ăn',  N'Túi',  180000, 260000, 20, 5),
    (N'Thức ăn Whiskas Mèo Trưởng Thành 1.5kg', N'Thức ăn', N'Túi', 90000, 135000, 15, 5),
    (N'Pate Mèo Ciao 85g',                  N'Thức ăn',  N'Hộp',   8000,  15000, 50, 10),
    (N'Bánh thưởng chó Milk-Bone 400g',     N'Thức ăn',  N'Hộp',  45000,  75000, 20,  5),
    (N'Lồng sắt inox cho chó nhỏ 60cm',    N'Lồng',     N'Cái', 250000, 420000, 10,  3),
    (N'Lồng nhựa vận chuyển mèo',           N'Lồng',     N'Cái', 180000, 295000,  8,  2),
    (N'Bàn cào móng mèo',                   N'Đồ chơi',  N'Cái',  60000, 110000, 15,  5),
    (N'Bóng đồ chơi cho mèo (3 bóng)',     N'Đồ chơi',  N'Bộ',   20000,  39000, 30, 10),
    (N'Dây xích + vòng cổ chó 1.5m',       N'Phụ kiện', N'Bộ',   35000,  65000, 20,  5),
    (N'Nước hoa khử mùi thú cưng 200ml',   N'Vệ sinh',  N'Chai',  45000,  89000, 15,  5),
    (N'Dầu gội chó mèo BioGroom 355ml',    N'Vệ sinh',  N'Chai',  80000, 145000, 12,  3),
    (N'Máng ăn + máng nước inox đôi',      N'Phụ kiện', N'Bộ',   25000,  49000, 25,  5);
END
GO

PRINT '========================================';
PRINT 'Seed data inserted successfully!';
PRINT 'Default accounts:';
PRINT '  admin / admin123  (ADMIN)';
PRINT '  nhanvien / staff123  (STAFF)';
PRINT '========================================';
