package com.petshop.repository;

import com.petshop.model.Pet;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Repository xử lý các thao tác CRUD với bảng Pets.
 */
public class PetRepository {

    public List<Pet> findAll(Connection conn) throws SQLException {
        String sql = """
            SELECT p.*, s.name AS supplier_name
            FROM Pets p
            LEFT JOIN Suppliers s ON p.supplier_id = s.id
            ORDER BY p.id DESC
            """;
        List<Pet> list = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    public List<Pet> findByStatus(Connection conn, String status) throws SQLException {
        String sql = """
            SELECT p.*, s.name AS supplier_name
            FROM Pets p
            LEFT JOIN Suppliers s ON p.supplier_id = s.id
            WHERE p.status = ?
            ORDER BY p.id DESC
            """;
        List<Pet> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public List<Pet> findAvailable(Connection conn) throws SQLException {
        return findByStatus(conn, "AVAILABLE");
    }

    public List<Pet> search(Connection conn, String keyword, String species, String status) throws SQLException {
        StringBuilder sql = new StringBuilder("""
            SELECT p.*, s.name AS supplier_name
            FROM Pets p
            LEFT JOIN Suppliers s ON p.supplier_id = s.id
            WHERE 1=1
            """);

        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.pet_code LIKE ? OR p.breed LIKE ? OR p.name LIKE ?)");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }

        if (species != null && !species.isBlank() && !"Tất cả".equalsIgnoreCase(species)) {
            sql.append(" AND p.species = ?");
            params.add(species.trim());
        }

        if (status != null && !status.isBlank() && !"Tất cả".equalsIgnoreCase(status)) {
            sql.append(" AND p.status = ?");
            params.add(status.trim());
        }

        sql.append(" ORDER BY p.id DESC");

        List<Pet> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String s) {
                    ps.setNString(i + 1, s);
                } else {
                    ps.setObject(i + 1, p);
                }
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    public Optional<Pet> findById(Connection conn, int id) throws SQLException {
        String sql = """
            SELECT p.*, s.name AS supplier_name
            FROM Pets p
            LEFT JOIN Suppliers s ON p.supplier_id = s.id
            WHERE p.id = ?
            """;
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

    public int insert(Connection conn, Pet pet) throws SQLException {
        String sql = """
            INSERT INTO Pets (name, species, breed, gender, age_months, color, weight_kg,
                             health_note, vaccinated, import_price, sell_price, status,
                             import_date, supplier_id, image_path, created_at, updated_at, pet_code)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setNString(1, pet.getName());
            ps.setNString(2, pet.getSpecies());
            ps.setNString(3, pet.getBreed());
            ps.setString(4, pet.getGender() != null ? pet.getGender() : "UNKNOWN");
            ps.setInt(5, pet.getAgeMonths());
            ps.setNString(6, pet.getColor());
            ps.setBigDecimal(7, pet.getWeightKg());
            ps.setNString(8, pet.getHealthNote());
            ps.setBoolean(9, pet.isVaccinated());
            ps.setBigDecimal(10, java.math.BigDecimal.valueOf(pet.getImportPrice()));
            ps.setBigDecimal(11, java.math.BigDecimal.valueOf(pet.getSellPrice()));
            ps.setString(12, pet.getStatus() != null ? pet.getStatus() : "AVAILABLE");
            ps.setDate(13, java.sql.Date.valueOf(pet.getImportDate() != null ? pet.getImportDate() : LocalDate.now()));
            if (pet.getSupplierId() > 0) {
                ps.setInt(14, pet.getSupplierId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            ps.setString(15, pet.getImagePath());
            ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
            ps.setTimestamp(17, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(18, pet.getPetCode());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    pet.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Không thể thêm Pet vào cơ sở dữ liệu.");
    }

    public void update(Connection conn, Pet pet) throws SQLException {
        String sql = """
            UPDATE Pets SET
                name         = ?,
                species      = ?,
                breed        = ?,
                gender       = ?,
                age_months   = ?,
                color        = ?,
                weight_kg    = ?,
                health_note  = ?,
                vaccinated   = ?,
                import_price = ?,
                sell_price   = ?,
                status       = ?,
                import_date  = ?,
                supplier_id  = ?,
                image_path   = ?,
                updated_at   = ?
            WHERE id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, pet.getName());
            ps.setNString(2, pet.getSpecies());
            ps.setNString(3, pet.getBreed());
            ps.setString(4, pet.getGender());
            ps.setInt(5, pet.getAgeMonths());
            ps.setNString(6, pet.getColor());
            ps.setBigDecimal(7, pet.getWeightKg());
            ps.setNString(8, pet.getHealthNote());
            ps.setBoolean(9, pet.isVaccinated());
            ps.setBigDecimal(10, java.math.BigDecimal.valueOf(pet.getImportPrice()));
            ps.setBigDecimal(11, java.math.BigDecimal.valueOf(pet.getSellPrice()));
            ps.setString(12, pet.getStatus());
            ps.setDate(13, java.sql.Date.valueOf(pet.getImportDate() != null ? pet.getImportDate() : LocalDate.now()));
            if (pet.getSupplierId() > 0) {
                ps.setInt(14, pet.getSupplierId());
            } else {
                ps.setNull(14, Types.INTEGER);
            }
            ps.setString(15, pet.getImagePath());
            ps.setTimestamp(16, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(17, pet.getId());

            ps.executeUpdate();
        }
    }

    public void updateStatus(Connection conn, int id, String status) throws SQLException {
        String sql = "UPDATE Pets SET status = ?, updated_at = GETDATE() WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /** Đánh dấu đã bán chỉ khi thú cưng vẫn còn AVAILABLE (chống bán trùng). */
    public boolean markSoldIfAvailable(Connection conn, int id) throws SQLException {
        String sql = "UPDATE Pets SET status = 'SOLD', updated_at = GETDATE() WHERE id = ? AND status = 'AVAILABLE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM Pets WHERE id = ? AND status = 'AVAILABLE'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public long countAvailable(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Pets WHERE status = 'AVAILABLE'";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getLong(1);
        }
        return 0L;
    }

    public Map<String, Long> countBySpecies(Connection conn) throws SQLException {
        Map<String, Long> map = new LinkedHashMap<>();
        String sql = "SELECT species, COUNT(*) FROM Pets WHERE status = 'AVAILABLE' GROUP BY species";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getNString(1), rs.getLong(2));
            }
        }
        return map;
    }

    public List<Pet> getSoldBetween(Connection conn, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT p.*, s.name AS supplier_name
            FROM Pets p
            INNER JOIN InvoiceDetails d ON d.item_id = p.id AND d.item_type = 'PET'
            INNER JOIN Invoices i ON i.id = d.invoice_id
            LEFT JOIN Suppliers s ON p.supplier_id = s.id
            WHERE CAST(i.invoice_date AS DATE) BETWEEN ? AND ?
              AND i.status = 'PAID'
            ORDER BY i.invoice_date DESC
            """;
        List<Pet> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    private Pet mapRow(ResultSet rs) throws SQLException {
        Pet p = new Pet();
        p.setId(rs.getInt("id"));
        p.setPetCode(rs.getString("pet_code"));
        p.setName(rs.getNString("name"));
        p.setSpecies(rs.getNString("species"));
        p.setBreed(rs.getNString("breed"));
        p.setGender(rs.getString("gender"));
        p.setAgeMonths(rs.getInt("age_months"));
        p.setColor(rs.getNString("color"));
        p.setWeightKg(rs.getBigDecimal("weight_kg"));
        p.setHealthNote(rs.getNString("health_note"));
        p.setVaccinated(rs.getBoolean("vaccinated"));
        p.setImportPrice(rs.getBigDecimal("import_price") != null ? rs.getBigDecimal("import_price").longValue() : 0L);
        p.setSellPrice(rs.getBigDecimal("sell_price") != null ? rs.getBigDecimal("sell_price").longValue() : 0L);
        p.setStatus(rs.getString("status"));

        java.sql.Date impDate = rs.getDate("import_date");
        if (impDate != null) p.setImportDate(impDate.toLocalDate());

        p.setSupplierId(rs.getInt("supplier_id"));
        try {
            p.setSupplierName(rs.getNString("supplier_name"));
        } catch (SQLException ignored) {}

        p.setImagePath(rs.getString("image_path"));

        Timestamp cTs = rs.getTimestamp("created_at");
        if (cTs != null) p.setCreatedAt(cTs.toLocalDateTime());

        Timestamp uTs = rs.getTimestamp("updated_at");
        if (uTs != null) p.setUpdatedAt(uTs.toLocalDateTime());

        return p;
    }
}
