package com.petshop.repository;

import com.petshop.model.Service;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý các thao tác CRUD với bảng Services (dịch vụ thú cưng).
 * Caller chịu trách nhiệm quản lý vòng đời của Connection.
 */
public class ServiceRepository {

    public List<Service> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Services ORDER BY name";
        List<Service> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Service> findActive(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Services WHERE is_active = 1 ORDER BY name";
        List<Service> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public Optional<Service> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM Services WHERE id = ?";
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

    public int insert(Connection conn, Service svc) throws SQLException {
        String sql = """
                INSERT INTO Services (name, description, price, duration, is_active, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, svc.getName());
            ps.setNString(2, svc.getDescription());
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(svc.getPrice()));
            ps.setInt(4, svc.getDuration());
            ps.setBoolean(5, svc.isActive());
            ps.setTimestamp(6, Timestamp.valueOf(svc.getCreatedAt() != null ? svc.getCreatedAt() : LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    svc.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Không thể lấy ID tự sinh sau khi thêm Service.");
    }

    public void update(Connection conn, Service svc) throws SQLException {
        String sql = """
                UPDATE Services SET
                    name        = ?,
                    description = ?,
                    price       = ?,
                    duration    = ?,
                    is_active   = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, svc.getName());
            ps.setNString(2, svc.getDescription());
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(svc.getPrice()));
            ps.setInt(4, svc.getDuration());
            ps.setBoolean(5, svc.isActive());
            ps.setInt(6, svc.getId());
            ps.executeUpdate();
        }
    }

    public void setActive(Connection conn, int id, boolean active) throws SQLException {
        String sql = "UPDATE Services SET is_active = ? WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Service mapRow(ResultSet rs) throws SQLException {
        Service s = new Service();
        s.setId(rs.getInt("id"));
        s.setName(rs.getNString("name"));
        s.setDescription(rs.getNString("description"));
        s.setPrice(rs.getBigDecimal("price") != null ? rs.getBigDecimal("price").longValue() : 0L);
        s.setDuration(rs.getInt("duration"));
        s.setActive(rs.getBoolean("is_active"));

        Timestamp createdAt = rs.getTimestamp("created_at");
        s.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);

        return s;
    }
}
