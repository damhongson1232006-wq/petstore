-- ============================================================
-- PET SHOP MANAGEMENT — DATABASE CREATION
-- Chạy script này với quyền sa hoặc dbcreator
-- ============================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = 'PetShopDB')
BEGIN
    CREATE DATABASE PetShopDB
    COLLATE Vietnamese_CI_AS;
    PRINT 'Database PetShopDB created successfully.';
END
ELSE
BEGIN
    PRINT 'Database PetShopDB already exists.';
END
GO

USE PetShopDB;
GO
