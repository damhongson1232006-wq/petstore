package com.petshop.repository;

import com.petshop.model.ImportOrder;
import com.petshop.model.ImportOrderDetail;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho Phiếu nhập hàng (ImportOrders và ImportOrderDetails).
 */
public class ImportOrderRepository {

    public int insert(Connection conn, ImportOrder order) throws SQLException {
        String sql = """
            INSERT INTO ImportOrders (order_code, import_date, supplier_id, total_amount, note, status, created_by, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, order.getOrderCode());
            ps.setDate(2, java.sql.Date.valueOf(order.getImportDate() != null ? order.getImportDate() : LocalDate.now()));
            if (order.getSupplierId() > 0) {
                ps.setInt(3, order.getSupplierId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(order.getTotalAmount()));
            ps.setNString(5, order.getNote());
            ps.setString(6, order.getStatus() != null ? order.getStatus() : "COMPLETED");
            ps.setInt(7, order.getCreatedBy() > 0 ? order.getCreatedBy() : 1);
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    order.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Thêm phiếu nhập thất bại, không lấy được ID.");
    }

    public void insertDetails(Connection conn, int orderId, List<ImportOrderDetail> details) throws SQLException {
        String sql = """
            INSERT INTO ImportOrderDetails (import_order_id, item_type, item_id, item_name, quantity, unit_price)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ImportOrderDetail d : details) {
                ps.setInt(1, orderId);
                ps.setString(2, d.getItemType());
                ps.setInt(3, d.getItemId());
                ps.setNString(4, d.getItemName());
                ps.setInt(5, d.getQuantity());
                ps.setBigDecimal(6, java.math.BigDecimal.valueOf(d.getUnitPrice()));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public Optional<ImportOrder> findById(Connection conn, int id) throws SQLException {
        String sql = """
            SELECT io.*, s.name AS supplier_name, u.full_name AS staff_name
            FROM ImportOrders io
            LEFT JOIN Suppliers s ON io.supplier_id = s.id
            LEFT JOIN Users u ON io.created_by = u.id
            WHERE io.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ImportOrder o = mapOrder(rs);
                    o.setDetails(findDetailsByOrderId(conn, id));
                    return Optional.of(o);
                }
            }
        }
        return Optional.empty();
    }

    public List<ImportOrderDetail> findDetailsByOrderId(Connection conn, int orderId) throws SQLException {
        List<ImportOrderDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM ImportOrderDetails WHERE import_order_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ImportOrderDetail d = new ImportOrderDetail();
                    d.setId(rs.getInt("id"));
                    d.setImportOrderId(rs.getInt("import_order_id"));
                    d.setItemType(rs.getString("item_type"));
                    d.setItemId(rs.getInt("item_id"));
                    d.setItemName(rs.getNString("item_name"));
                    d.setQuantity(rs.getInt("quantity"));
                    d.setUnitPrice(rs.getBigDecimal("unit_price").longValue());
                    list.add(d);
                }
            }
        }
        return list;
    }

    public void cancel(Connection conn, int orderId) throws SQLException {
        String sql = "UPDATE ImportOrders SET status = 'CANCELLED' WHERE id = ? AND status = 'COMPLETED'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            if (ps.executeUpdate() == 0) {
                throw new SQLException("Không thể hủy phiếu nhập hoặc phiếu đã được hủy.");
            }
        }
    }

    public List<ImportOrder> findAll(Connection conn) throws SQLException {
        List<ImportOrder> list = new ArrayList<>();
        String sql = """
            SELECT io.*, s.name AS supplier_name, u.full_name AS staff_name
            FROM ImportOrders io
            LEFT JOIN Suppliers s ON io.supplier_id = s.id
            LEFT JOIN Users u ON io.created_by = u.id
            ORDER BY io.import_date DESC
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapOrder(rs));
            }
        }
        return list;
    }

    public List<ImportOrder> findByDateRange(Connection conn, LocalDate from, LocalDate to) throws SQLException {
        List<ImportOrder> list = new ArrayList<>();
        String sql = """
            SELECT io.*, s.name AS supplier_name, u.full_name AS staff_name
            FROM ImportOrders io
            LEFT JOIN Suppliers s ON io.supplier_id = s.id
            LEFT JOIN Users u ON io.created_by = u.id
            WHERE io.import_date BETWEEN ? AND ?
            ORDER BY io.import_date DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrder(rs));
                }
            }
        }
        return list;
    }

    private ImportOrder mapOrder(ResultSet rs) throws SQLException {
        ImportOrder o = new ImportOrder();
        o.setId(rs.getInt("id"));
        o.setOrderCode(rs.getString("order_code"));
        Date d = rs.getDate("import_date");
        if (d != null) {
            o.setImportDate(d.toLocalDate());
        }
        o.setSupplierId(rs.getInt("supplier_id"));
        try {
            o.setSupplierName(rs.getNString("supplier_name"));
        } catch (SQLException ignored) {}
        o.setTotalAmount(rs.getBigDecimal("total_amount").longValue());
        o.setNote(rs.getNString("note"));
        o.setStatus(rs.getString("status"));
        o.setCreatedBy(rs.getInt("created_by"));
        try {
            o.setCreatedByName(rs.getNString("staff_name"));
        } catch (SQLException ignored) {}
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            o.setCreatedAt(createdTs.toLocalDateTime());
        }
        return o;
    }
}
