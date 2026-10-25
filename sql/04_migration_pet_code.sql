-- =====================================================================
-- 04_migration_pet_code.sql
-- Nâng cấp CSDL đã có: thêm MÃ THÚ (Pets.pet_code).
-- Ứng dụng tự chạy các lệnh này khi khởi động; file này dùng để chạy thủ công
-- bằng SSMS nếu ứng dụng không đủ quyền ALTER TABLE. An toàn khi chạy nhiều lần.
-- =====================================================================
USE PetShopDB;
GO

IF COL_LENGTH('Pets', 'pet_code') IS NULL
    ALTER TABLE Pets ADD pet_code VARCHAR(20) NULL;
GO

-- Gán mã cho các thú cưng cũ: TC + 6 chữ số theo id
UPDATE Pets
SET pet_code = 'TC' + RIGHT('000000' + CAST(id AS VARCHAR(10)), 6)
WHERE pet_code IS NULL;
GO

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_Pets_pet_code' AND object_id = OBJECT_ID('Pets'))
    CREATE UNIQUE INDEX UX_Pets_pet_code ON Pets(pet_code) WHERE pet_code IS NOT NULL;
GO
