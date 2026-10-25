package com.petshop.util;

import java.sql.Connection;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Tự động nâng cấp cấu trúc CSDL khi khởi động. */
public final class SchemaMigrator {

    private static final Logger LOGGER = Logger.getLogger(SchemaMigrator.class.getName());

    private SchemaMigrator() {}

    /**
     * Áp dụng các migration nhỏ cần thiết cho database cũ.
     * Script đầy đủ tương ứng nằm trong sql/05_business_upgrade.sql.
     */
    public static String run() {
        try (Connection conn = DatabaseManager.getConnection(); Statement st = conn.createStatement()) {
            st.execute("IF COL_LENGTH('Pets', 'pet_code') IS NULL "
                    + "ALTER TABLE Pets ADD pet_code VARCHAR(20) NULL");
            st.execute("UPDATE Pets SET pet_code = 'TC' + RIGHT('000000' + CAST(id AS VARCHAR(10)), 6) "
                    + "WHERE pet_code IS NULL");
            st.execute("IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = 'UX_Pets_pet_code' "
                    + "AND object_id = OBJECT_ID('Pets')) "
                    + "CREATE UNIQUE INDEX UX_Pets_pet_code ON Pets(pet_code) WHERE pet_code IS NOT NULL");

            // Trạng thái UNAVAILABLE dùng cho thú cưng đã nhập nhưng bị loại khỏi tồn bán.
            st.execute("IF OBJECT_ID('Pets', 'U') IS NOT NULL AND "
                    + "NOT EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CHK_Pets_Status_Upgrade') "
                    + "BEGIN "
                    + "IF EXISTS (SELECT 1 FROM sys.check_constraints WHERE name = 'CHK_Pets_Status') "
                    + "ALTER TABLE Pets DROP CONSTRAINT CHK_Pets_Status; "
                    + "ALTER TABLE Pets ADD CONSTRAINT CHK_Pets_Status_Upgrade "
                    + "CHECK (status IN ('AVAILABLE','SOLD','RESERVED','UNAVAILABLE')); "
                    + "END");

            // Nhật ký biến động tồn kho.
            st.execute("IF OBJECT_ID('InventoryTransactions', 'U') IS NULL BEGIN "
                    + "CREATE TABLE InventoryTransactions ("
                    + "id INT IDENTITY(1,1) PRIMARY KEY, "
                    + "transaction_code VARCHAR(30) NOT NULL UNIQUE, "
                    + "transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('IMPORT','SALE','SALE_CANCEL','IMPORT_CANCEL','ADJUSTMENT')), "
                    + "item_type VARCHAR(15) NOT NULL CHECK (item_type IN ('PET','ACCESSORY')), "
                    + "item_id INT NOT NULL, item_name NVARCHAR(150) NOT NULL, quantity_change INT NOT NULL, "
                    + "reference_type VARCHAR(20), reference_id INT, note NVARCHAR(500), "
                    + "created_by INT NULL REFERENCES Users(id), created_at DATETIME NOT NULL DEFAULT GETDATE()); "
                    + "CREATE INDEX IX_InventoryTransactions_Item ON InventoryTransactions(item_type, item_id, created_at); "
                    + "CREATE INDEX IX_InventoryTransactions_Reference ON InventoryTransactions(reference_type, reference_id); "
                    + "CREATE INDEX IX_InventoryTransactions_Date ON InventoryTransactions(created_at); "
                    + "END");
            return null;
        } catch (Throwable e) {
            LOGGER.log(Level.SEVERE, "Không thể nâng cấp cấu trúc CSDL.", e);
            return e.getMessage();
        }
    }
}
