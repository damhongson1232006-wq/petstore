package com.petshop.repository;

import com.petshop.model.Accessory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository xử lý các thao tác CRUD với bảng Accessories.
 */
public class AccessoryRepository {

    public List<Accessory> findAll(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Accessories ORDER BY name";
        List<Accessory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Accessory> findActive(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Accessories WHERE is_active = 1 ORDER BY name";
        List<Accessory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Accessory> findLowStock(Connection conn) throws SQLException {
        String sql = "SELECT * FROM Accessories WHERE is_active = 1 AND quantity <= min_quantity ORDER BY quantity ASC";
        List<Accessory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Accessory> search(Connection conn, String keyword, String category) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM Accessories WHERE is_active = 1");
        List<String> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND name LIKE ?");
            params.add("%" + keyword.trim() + "%");
        }

        if (category != null && !category.isBlank() && !"Tất cả".equalsIgnoreCase(category)) {
            sql.append(" AND category = ?");
            params.add(category.trim());
        }

        sql.append(" ORDER BY name");

        List<Accessory> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setNString(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Optional<Accessory> findById(Connection conn, int id) throws SQLException {
        String sql = "SELECT * FROM Accessories WHERE id = ?";
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

    public int insert(Connection conn, Accessory acc) throws SQLException {
        String sql = """
            INSERT INTO Accessories (name, category, unit, import_price, sell_price, quantity,
                                    min_quantity, supplier_id, description, is_active, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, acc.getName());
            ps.setNString(2, acc.getCategory());
            ps.setNString(3, acc.getUnit() != null ? acc.getUnit() : "Cái");
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(acc.getImportPrice()));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(acc.getSellPrice()));
            ps.setInt(6, acc.getQuantity());
            ps.setInt(7, acc.getMinQuantity());
            if (acc.getSupplierId() > 0) {
                ps.setInt(8, acc.getSupplierId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setNString(9, acc.getDescription());
            ps.setBoolean(10, acc.isActive());
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(12, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    acc.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Thêm phụ kiện thất bại.");
    }

    public void update(Connection conn, Accessory acc) throws SQLException {
        String sql = """
            UPDATE Accessories SET
                name         = ?,
                category     = ?,
                unit         = ?,
                import_price = ?,
                sell_price   = ?,
                quantity     = ?,
                min_quantity = ?,
                supplier_id  = ?,
                description  = ?,
                is_active    = ?,
                updated_at   = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, acc.getName());
            ps.setNString(2, acc.getCategory());
            ps.setNString(3, acc.getUnit());
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(acc.getImportPrice()));
            ps.setBigDecimal(5, java.math.BigDecimal.valueOf(acc.getSellPrice()));
            ps.setInt(6, acc.getQuantity());
            ps.setInt(7, acc.getMinQuantity());
            if (acc.getSupplierId() > 0) {
                ps.setInt(8, acc.getSupplierId());
            } else {
                ps.setNull(8, Types.INTEGER);
            }
            ps.setNString(9, acc.getDescription());
            ps.setBoolean(10, acc.isActive());
            ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(12, acc.getId());

            ps.executeUpdate();
        }
    }

    public void updateQuantity(Connection conn, int id, int delta) throws SQLException {
        String sql = "UPDATE Accessories SET quantity = quantity + ?, updated_at = GETDATE() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Trừ kho chỉ khi sau khi bán vẫn còn ≥ mức tồn tối thiểu (min_quantity) của phụ kiện. */
    public boolean decreaseIfEnough(Connection conn, int id, int qty) throws SQLException {
        String sql = "UPDATE Accessories SET quantity = quantity - ?, updated_at = GETDATE() WHERE id = ? AND quantity - ? >= min_quantity";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, qty);
            ps.setInt(2, id);
            ps.setInt(3, qty);
            return ps.executeUpdate() > 0;
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "UPDATE Accessories SET is_active = 0, updated_at = GETDATE() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public long countLowStock(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Accessories WHERE is_active = 1 AND quantity <= min_quantity";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0L;
    }

    private Accessory mapRow(ResultSet rs) throws SQLException {
        Accessory a = new Accessory();
        a.setId(rs.getInt("id"));
        a.setName(rs.getNString("name"));
        a.setCategory(rs.getNString("category"));
        a.setUnit(rs.getNString("unit"));
        a.setImportPrice(rs.getBigDecimal("import_price") != null ? rs.getBigDecimal("import_price").longValue() : 0L);
        a.setSellPrice(rs.getBigDecimal("sell_price") != null ? rs.getBigDecimal("sell_price").longValue() : 0L);
        a.setQuantity(rs.getInt("quantity"));
        a.setMinQuantity(rs.getInt("min_quantity"));
        a.setSupplierId(rs.getInt("supplier_id"));
        a.setDescription(rs.getNString("description"));
        a.setActive(rs.getBoolean("is_active"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) a.setCreatedAt(cTs.toLocalDateTime());
        Timestamp uTs = rs.getTimestamp("updated_at");
        if (uTs != null) a.setUpdatedAt(uTs.toLocalDateTime());

        return a;
    }
}
