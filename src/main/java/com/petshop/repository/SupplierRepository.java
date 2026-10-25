package com.petshop.repository;

import com.petshop.model.Supplier;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý các thao tác CRUD với bảng Suppliers.
 * Caller chịu trách nhiệm quản lý vòng đời của Connection.
 */
public class SupplierRepository {

    public List<Supplier> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Suppliers ORDER BY name";
        List<Supplier> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Supplier> findActive(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Suppliers WHERE is_active = 1 ORDER BY name";
        List<Supplier> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Supplier> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM Suppliers WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Connection conn, Supplier supplier) throws SQLException {
        String sql = """
                INSERT INTO Suppliers (name, phone, address, contact, note, is_active, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, supplier.getName());
            ps.setString(2, supplier.getPhone());
            ps.setNString(3, supplier.getAddress());
            ps.setNString(4, supplier.getContact());
            ps.setNString(5, supplier.getNote());
            ps.setBoolean(6, supplier.isActive());
            ps.setTimestamp(7, Timestamp.valueOf(supplier.getCreatedAt() != null
                    ? supplier.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    supplier.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Không thể lấy ID tự sinh sau khi thêm Supplier.");
    }

    public void update(Connection conn, Supplier supplier) throws SQLException {
        String sql = """
                UPDATE Suppliers SET
                    name      = ?,
                    phone     = ?,
                    address   = ?,
                    contact   = ?,
                    note      = ?,
                    is_active = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, supplier.getName());
            ps.setString(2, supplier.getPhone());
            ps.setNString(3, supplier.getAddress());
            ps.setNString(4, supplier.getContact());
            ps.setNString(5, supplier.getNote());
            ps.setBoolean(6, supplier.isActive());
            ps.setInt(7, supplier.getId());
            ps.executeUpdate();
        }
    }

    public void setActive(Connection conn, int id, boolean active) throws SQLException {
        String sql = "UPDATE Suppliers SET is_active = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier s = new Supplier();
        s.setId(rs.getInt("id"));
        s.setName(rs.getNString("name"));
        s.setPhone(rs.getString("phone"));
        s.setAddress(rs.getNString("address"));
        s.setContact(rs.getNString("contact"));
        s.setNote(rs.getNString("note"));
        s.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        return s;
    }
}
