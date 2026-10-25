-- ============================================================
-- 05_BUSINESS_UPGRADE.sql
-- Nâng cấp nghiệp vụ cửa hàng: nhật ký nhập/xuất tồn kho.
-- Chạy sau 02_create_tables.sql và 04_migration_pet_code.sql.
-- ============================================================
USE PetShopDB;
GO

IF NOT EXISTS (SELECT 1 FROM sys.tables WHERE name = 'InventoryTransactions')
BEGIN
    CREATE TABLE InventoryTransactions (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        transaction_code VARCHAR(30) NOT NULL UNIQUE,
        transaction_type VARCHAR(20) NOT NULL
            CONSTRAINT CHK_InventoryTransactions_Type CHECK
            (transaction_type IN ('IMPORT','SALE','SALE_CANCEL','IMPORT_CANCEL','ADJUSTMENT')),
        item_type       VARCHAR(15) NOT NULL
            CONSTRAINT CHK_InventoryTransactions_ItemType CHECK
            (item_type IN ('PET','ACCESSORY')),
        item_id         INT NOT NULL,
        item_name       NVARCHAR(150) NOT NULL,
        quantity_change INT NOT NULL,
        reference_type  VARCHAR(20),
        reference_id    INT,
        note            NVARCHAR(500),
        created_by      INT NULL REFERENCES Users(id),
        created_at      DATETIME NOT NULL DEFAULT GETDATE()
    );
    CREATE INDEX IX_InventoryTransactions_Item ON InventoryTransactions(item_type, item_id, created_at);
    CREATE INDEX IX_InventoryTransactions_Reference ON InventoryTransactions(reference_type, reference_id);
    CREATE INDEX IX_InventoryTransactions_Date ON InventoryTransactions(created_at);
END
GO

-- Bổ sung trạng thái UNAVAILABLE cho thú cưng để xử lý các trường hợp
-- không còn bán nhưng vẫn cần giữ lịch sử nhập/xuất trong hệ thống.
IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CHK_Pets_Status' AND parent_object_id = OBJECT_ID('Pets'))
BEGIN
    ALTER TABLE Pets DROP CONSTRAINT CHK_Pets_Status;
END
GO

IF OBJECT_ID('Pets', 'U') IS NOT NULL
BEGIN
    ALTER TABLE Pets ADD CONSTRAINT CHK_Pets_Status
        CHECK (status IN ('AVAILABLE','SOLD','RESERVED','UNAVAILABLE'));
END
GO

PRINT '05_business_upgrade.sql completed.';
GO
