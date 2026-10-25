package com.petshop.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** Ghi nhận lịch sử biến động tồn kho để có thể truy vết nghiệp vụ. */
public class InventoryTransactionRepository {

    public void insert(Connection conn, String code, String type, String itemType, int itemId,
                       String itemName, int quantityChange, String referenceType, Integer referenceId,
                       String note, int createdBy) throws SQLException {
        String sql = """
            INSERT INTO InventoryTransactions
                (transaction_code, transaction_type, item_type, item_id, item_name,
                 quantity_change, reference_type, reference_id, note, created_by, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, code);
            ps.setString(2, type);
            ps.setString(3, itemType);
            ps.setInt(4, itemId);
            ps.setNString(5, itemName);
            ps.setInt(6, quantityChange);
            ps.setString(7, referenceType);
            if (referenceId == null) ps.setNull(8, java.sql.Types.INTEGER); else ps.setInt(8, referenceId);
            ps.setNString(9, note);
            if (createdBy > 0) ps.setInt(10, createdBy); else ps.setNull(10, java.sql.Types.INTEGER);
            ps.executeUpdate();
        }
    }

    public boolean hasReference(Connection conn, String referenceType, int referenceId, String transactionType) throws SQLException {
        String sql = "SELECT 1 FROM InventoryTransactions WHERE reference_type = ? AND reference_id = ? AND transaction_type = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceType);
            ps.setInt(2, referenceId);
            ps.setString(3, transactionType);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
