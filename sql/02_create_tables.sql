-- ============================================================
-- PET SHOP MANAGEMENT — TABLE DEFINITIONS
-- Chạy trong context: USE PetShopDB
-- ============================================================

USE PetShopDB;
GO

-- ============================================================
-- 1. USERS — Tài khoản người dùng hệ thống
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Users')
BEGIN
    CREATE TABLE Users (
        id            INT IDENTITY(1,1)  PRIMARY KEY,
        username      VARCHAR(50)        NOT NULL UNIQUE,
        password_hash VARCHAR(100)       NOT NULL,
        full_name     NVARCHAR(100)      NOT NULL,
        role          VARCHAR(10)        NOT NULL
                      CONSTRAINT CHK_Users_Role CHECK (role IN ('ADMIN','STAFF')),
        is_active     BIT                NOT NULL DEFAULT 1,
        created_at    DATETIME           NOT NULL DEFAULT GETDATE()
    );
    PRINT 'Table Users created.';
END
GO

-- ============================================================
-- 2. CUSTOMERS — Khách hàng
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Customers')
BEGIN
    CREATE TABLE Customers (
        id          INT IDENTITY(1,1)  PRIMARY KEY,
        full_name   NVARCHAR(100)      NOT NULL,
        phone       VARCHAR(15)        NOT NULL,
        email       VARCHAR(100),
        address     NVARCHAR(200),
        note        NVARCHAR(500),
        created_at  DATETIME           NOT NULL DEFAULT GETDATE()
    );
    CREATE INDEX IX_Customers_Phone ON Customers(phone);
    CREATE INDEX IX_Customers_Name  ON Customers(full_name);
    PRINT 'Table Customers created.';
END
GO

-- ============================================================
-- 3. SUPPLIERS — Nhà cung cấp / Trại giống
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Suppliers')
BEGIN
    CREATE TABLE Suppliers (
        id          INT IDENTITY(1,1)  PRIMARY KEY,
        name        NVARCHAR(150)      NOT NULL,
        phone       VARCHAR(15),
        address     NVARCHAR(200),
        contact     NVARCHAR(100),     -- Tên người liên hệ
        note        NVARCHAR(500),
        is_active   BIT                NOT NULL DEFAULT 1,
        created_at  DATETIME           NOT NULL DEFAULT GETDATE()
    );
    PRINT 'Table Suppliers created.';
END
GO

-- ============================================================
-- 4. PETS — Cá thể thú cưng (mỗi con là 1 bản ghi)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Pets')
BEGIN
    CREATE TABLE Pets (
        id            INT IDENTITY(1,1)  PRIMARY KEY,
        pet_code      VARCHAR(20),                        -- Mã thú: TC000001, TC000002...
        name          NVARCHAR(100),                      -- Tên đặt cho thú (tuỳ chọn)
        species       NVARCHAR(50)       NOT NULL,        -- Chó, Mèo, Thỏ, Hamster, Chim...
        breed         NVARCHAR(100)      NOT NULL,        -- Poodle, Corgi, Mèo Anh lông ngắn...
        gender        VARCHAR(10)        NOT NULL DEFAULT 'UNKNOWN'
                      CONSTRAINT CHK_Pets_Gender CHECK (gender IN ('MALE','FEMALE','UNKNOWN')),
        age_months    INT                NOT NULL DEFAULT 0,
        color         NVARCHAR(50),
        weight_kg     DECIMAL(5,2),
        health_note   NVARCHAR(500),
        vaccinated    BIT                NOT NULL DEFAULT 0,
        import_price  DECIMAL(18,0)      NOT NULL DEFAULT 0,
        sell_price    DECIMAL(18,0)      NOT NULL DEFAULT 0,
        status        VARCHAR(20)        NOT NULL DEFAULT 'AVAILABLE'
                      CONSTRAINT CHK_Pets_Status CHECK (status IN ('AVAILABLE','SOLD','RESERVED')),
        import_date   DATE               NOT NULL DEFAULT GETDATE(),
        supplier_id   INT                REFERENCES Suppliers(id),
        image_path    VARCHAR(500),       -- Đường dẫn ảnh thú (local)
        created_at    DATETIME           NOT NULL DEFAULT GETDATE(),
        updated_at    DATETIME           NOT NULL DEFAULT GETDATE()
    );
    CREATE INDEX IX_Pets_Status   ON Pets(status);
    CREATE UNIQUE INDEX UX_Pets_pet_code ON Pets(pet_code) WHERE pet_code IS NOT NULL;
    CREATE INDEX IX_Pets_Species  ON Pets(species);
    CREATE INDEX IX_Pets_Breed    ON Pets(breed);
    CREATE INDEX IX_Pets_Price    ON Pets(sell_price);
    PRINT 'Table Pets created.';
END
GO

-- ============================================================
-- 5. ACCESSORIES — Phụ kiện (thức ăn, lồng, đồ chơi...)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Accessories')
BEGIN
    CREATE TABLE Accessories (
        id            INT IDENTITY(1,1)  PRIMARY KEY,
        name          NVARCHAR(150)      NOT NULL,
        category      NVARCHAR(100),                     -- Thức ăn | Lồng | Đồ chơi | Vệ sinh | Y tế
        unit          NVARCHAR(30)       NOT NULL DEFAULT N'Cái',
        import_price  DECIMAL(18,0)      NOT NULL DEFAULT 0,
        sell_price    DECIMAL(18,0)      NOT NULL DEFAULT 0,
        quantity      INT                NOT NULL DEFAULT 0,
        min_quantity  INT                NOT NULL DEFAULT 5, -- Ngưỡng cảnh báo hết hàng
        supplier_id   INT                REFERENCES Suppliers(id),
        description   NVARCHAR(500),
        is_active     BIT                NOT NULL DEFAULT 1,
        created_at    DATETIME           NOT NULL DEFAULT GETDATE(),
        updated_at    DATETIME           NOT NULL DEFAULT GETDATE()
    );
    CREATE INDEX IX_Accessories_Category ON Accessories(category);
    PRINT 'Table Accessories created.';
END
GO

-- ============================================================
-- 6. SERVICES — Danh mục dịch vụ (tắm, cắt tỉa, khám bệnh...)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Services')
BEGIN
    CREATE TABLE Services (
        id          INT IDENTITY(1,1)  PRIMARY KEY,
        name        NVARCHAR(150)      NOT NULL,
        description NVARCHAR(500),
        price       DECIMAL(18,0)      NOT NULL DEFAULT 0,
        duration    INT,                                  -- Thời gian thực hiện (phút)
        is_active   BIT                NOT NULL DEFAULT 1,
        created_at  DATETIME           NOT NULL DEFAULT GETDATE()
    );
    PRINT 'Table Services created.';
END
GO

-- ============================================================
-- 7. IMPORT_ORDERS — Phiếu nhập hàng
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ImportOrders')
BEGIN
    CREATE TABLE ImportOrders (
        id            INT IDENTITY(1,1)  PRIMARY KEY,
        order_code    VARCHAR(25)        NOT NULL UNIQUE,  -- PN20241024001
        import_date   DATE               NOT NULL DEFAULT GETDATE(),
        supplier_id   INT                REFERENCES Suppliers(id),
        total_amount  DECIMAL(18,0)      NOT NULL DEFAULT 0,
        note          NVARCHAR(500),
        status        VARCHAR(20)        NOT NULL DEFAULT 'COMPLETED'
                      CONSTRAINT CHK_ImportOrders_Status CHECK (status IN ('COMPLETED','CANCELLED')),
        created_by    INT                NOT NULL REFERENCES Users(id),
        created_at    DATETIME           NOT NULL DEFAULT GETDATE()
    );
    CREATE INDEX IX_ImportOrders_Date ON ImportOrders(import_date);
    PRINT 'Table ImportOrders created.';
END
GO

-- ============================================================
-- 8. IMPORT_ORDER_DETAILS — Chi tiết phiếu nhập
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ImportOrderDetails')
BEGIN
    CREATE TABLE ImportOrderDetails (
        id               INT IDENTITY(1,1)  PRIMARY KEY,
        import_order_id  INT                NOT NULL REFERENCES ImportOrders(id) ON DELETE CASCADE,
        item_type        VARCHAR(15)        NOT NULL
                         CONSTRAINT CHK_IOD_ItemType CHECK (item_type IN ('PET','ACCESSORY')),
        item_id          INT                NOT NULL,   -- Pet.id hoặc Accessory.id
        item_name        NVARCHAR(150)      NOT NULL,   -- Snapshot tên tại thời điểm nhập
        quantity         INT                NOT NULL DEFAULT 1,
        unit_price       DECIMAL(18,0)      NOT NULL DEFAULT 0,
        subtotal         AS (CAST(quantity AS BIGINT) * unit_price) PERSISTED
    );
    PRINT 'Table ImportOrderDetails created.';
END
GO

-- ============================================================
-- 9. INVOICES — Hóa đơn bán hàng
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'Invoices')
BEGIN
    CREATE TABLE Invoices (
        id              INT IDENTITY(1,1)  PRIMARY KEY,
        invoice_code    VARCHAR(25)        NOT NULL UNIQUE,  -- HD20241024001
        invoice_date    DATETIME           NOT NULL DEFAULT GETDATE(),
        customer_id     INT                REFERENCES Customers(id),
        customer_name   NVARCHAR(100),    -- Snapshot tên khách (hoặc "Khách lẻ")
        customer_phone  VARCHAR(15),      -- Snapshot SĐT
        subtotal        DECIMAL(18,0)      NOT NULL DEFAULT 0,
        discount        DECIMAL(18,0)      NOT NULL DEFAULT 0,
        total_amount    DECIMAL(18,0)      NOT NULL DEFAULT 0,
        payment_method  VARCHAR(15)        NOT NULL DEFAULT 'CASH'
                        CONSTRAINT CHK_Invoices_Payment CHECK (payment_method IN ('CASH','TRANSFER')),
        status          VARCHAR(15)        NOT NULL DEFAULT 'PAID'
                        CONSTRAINT CHK_Invoices_Status CHECK (status IN ('PAID','CANCELLED')),
        note            NVARCHAR(500),
        created_by      INT                NOT NULL REFERENCES Users(id),
        created_at      DATETIME           NOT NULL DEFAULT GETDATE()
    );
    CREATE INDEX IX_Invoices_Date       ON Invoices(invoice_date);
    CREATE INDEX IX_Invoices_Customer   ON Invoices(customer_id);
    CREATE INDEX IX_Invoices_Status     ON Invoices(status);
    PRINT 'Table Invoices created.';
END
GO

-- ============================================================
-- 10. INVOICE_DETAILS — Chi tiết hóa đơn
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'InvoiceDetails')
BEGIN
    CREATE TABLE InvoiceDetails (
        id          INT IDENTITY(1,1)  PRIMARY KEY,
        invoice_id  INT                NOT NULL REFERENCES Invoices(id) ON DELETE CASCADE,
        item_type   VARCHAR(15)        NOT NULL
                    CONSTRAINT CHK_ID_ItemType CHECK (item_type IN ('PET','ACCESSORY','SERVICE')),
        item_id     INT                NOT NULL,
        item_name   NVARCHAR(150)      NOT NULL,   -- Snapshot tên hàng tại thời điểm bán
        quantity    INT                NOT NULL DEFAULT 1,
        unit_price  DECIMAL(18,0)      NOT NULL DEFAULT 0,
        subtotal    AS (CAST(quantity AS BIGINT) * unit_price) PERSISTED
    );
    PRINT 'Table InvoiceDetails created.';
END
GO

PRINT '========================================';
PRINT 'All tables created successfully!';
PRINT '========================================';
