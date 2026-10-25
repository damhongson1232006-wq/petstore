package com.petshop.repository;

import com.petshop.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý các thao tác với bảng Users.
 */
public class UserRepository {

    public List<User> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Users ORDER BY id";
        List<User> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<User> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM Users WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<User> findByUsername(Connection conn, String username) throws SQLException {
        String sql = "SELECT * FROM Users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapRow(rs));
            }
        }
        return Optional.empty();
    }

    public int insert(Connection conn, User user) throws SQLException {
        String sql = """
            INSERT INTO Users (username, password_hash, full_name, role, is_active, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername().trim());
            ps.setString(2, user.getPasswordHash());
            ps.setNString(3, user.getFullName().trim());
            ps.setString(4, user.getRole() != null ? user.getRole().toUpperCase() : "STAFF");
            ps.setBoolean(5, user.isActive());
            ps.setTimestamp(6, Timestamp.valueOf(user.getCreatedAt() != null ? user.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    user.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Thêm tài khoản người dùng thất bại.");
    }

    public void update(Connection conn, User user) throws SQLException {
        String sql = """
            UPDATE Users SET
                password_hash = ?,
                full_name     = ?,
                role          = ?,
                is_active     = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getPasswordHash());
            ps.setNString(2, user.getFullName());
            ps.setString(3, user.getRole());
            ps.setBoolean(4, user.isActive());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        }
    }

    public void setActive(Connection conn, int id, boolean active) throws SQLException {
        String sql = "UPDATE Users SET is_active = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getNString("full_name"));
        u.setRole(rs.getString("role"));
        u.setActive(rs.getBoolean("is_active"));

        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) u.setCreatedAt(ts.toLocalDateTime());

        return u;
    }
}
