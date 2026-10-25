package com.petshop.repository;

import com.petshop.model.Customer;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý các thao tác CRUD với bảng Customers.
 * Caller chịu trách nhiệm quản lý vòng đời của Connection.
 */
public class CustomerRepository {

    public List<Customer> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Customers ORDER BY id DESC";
        List<Customer> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Customer> search(Connection conn, String keyword) throws SQLException {
        String sql = """
                SELECT * FROM Customers
                WHERE full_name LIKE ? OR phone LIKE ?
                ORDER BY id DESC
                """;
        List<Customer> list = new ArrayList<>();
        String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, like);
            ps.setNString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Optional<Customer> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM Customers WHERE id = ?";
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

    public Optional<Customer> findByPhone(Connection conn, String phone) throws SQLException {
        String sql = "SELECT * FROM Customers WHERE phone = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone == null ? null : phone.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }

    public int insert(Connection conn, Customer customer) throws SQLException {
        String sql = """
                INSERT INTO Customers (full_name, phone, email, address, note, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setNString(4, customer.getAddress());
            ps.setNString(5, customer.getNote());
            ps.setTimestamp(6, Timestamp.valueOf(customer.getCreatedAt() != null
                    ? customer.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    customer.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Không thể lấy ID tự sinh sau khi thêm Customer.");
    }

    public void update(Connection conn, Customer customer) throws SQLException {
        String sql = """
                UPDATE Customers SET
                    full_name = ?,
                    phone     = ?,
                    email     = ?,
                    address   = ?,
                    note      = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, customer.getFullName());
            ps.setString(2, customer.getPhone());
            ps.setString(3, customer.getEmail());
            ps.setNString(4, customer.getAddress());
            ps.setNString(5, customer.getNote());
            ps.setInt(6, customer.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM Customers WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public long count(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customers";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0L;
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer c = new Customer();
        c.setId(rs.getInt("id"));
        c.setFullName(rs.getNString("full_name"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAddress(rs.getNString("address"));
        c.setNote(rs.getNString("note"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        c.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        return c;
    }
}
