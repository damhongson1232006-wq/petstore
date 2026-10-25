package com.petshop.repository;

import com.petshop.model.Invoice;
import com.petshop.model.InvoiceDetail;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Repository truy xuất dữ liệu Hóa đơn (Invoices và InvoiceDetails).
 */
public class InvoiceRepository {

    /**
     * Thêm mới hóa đơn, trả về ID vừa sinh.
     */
    public int insert(Connection conn, Invoice invoice) throws SQLException {
        String sql = """
            INSERT INTO Invoices (invoice_code, invoice_date, customer_id, customer_name,
                                 customer_phone, subtotal, discount, total_amount,
                                 payment_method, status, note, created_by, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, invoice.getInvoiceCode());
            ps.setTimestamp(2, Timestamp.valueOf(invoice.getInvoiceDate() != null ? invoice.getInvoiceDate() : LocalDateTime.now()));
            if (invoice.getCustomerId() != null && invoice.getCustomerId() > 0) {
                ps.setInt(3, invoice.getCustomerId());
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            ps.setNString(4, invoice.getCustomerName() != null ? invoice.getCustomerName() : "Khách lẻ");
            ps.setString(5, invoice.getCustomerPhone());
            ps.setBigDecimal(6, java.math.BigDecimal.valueOf(invoice.getSubtotal()));
            ps.setBigDecimal(7, java.math.BigDecimal.valueOf(invoice.getDiscount()));
            ps.setBigDecimal(8, java.math.BigDecimal.valueOf(invoice.getTotalAmount()));
            ps.setString(9, invoice.getPaymentMethod() != null ? invoice.getPaymentMethod() : "CASH");
            ps.setString(10, invoice.getStatus() != null ? invoice.getStatus() : "PAID");
            ps.setNString(11, invoice.getNote());
            ps.setInt(12, invoice.getCreatedBy() > 0 ? invoice.getCreatedBy() : 1);
            ps.setTimestamp(13, Timestamp.valueOf(LocalDateTime.now()));

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    invoice.setId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Thêm hóa đơn thất bại, không lấy được ID.");
    }

    /**
     * Thêm danh sách dòng chi tiết hóa đơn.
     */
    public void insertDetails(Connection conn, int invoiceId, List<InvoiceDetail> details) throws SQLException {
        String sql = """
            INSERT INTO InvoiceDetails (invoice_id, item_type, item_id, item_name, quantity, unit_price)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (InvoiceDetail d : details) {
                ps.setInt(1, invoiceId);
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

    /**
     * Tìm hóa đơn theo ID kèm chi tiết.
     */
    public Optional<Invoice> findById(Connection conn, int id) throws SQLException {
        String sql = """
            SELECT i.*, u.full_name AS staff_name
            FROM Invoices i
            LEFT JOIN Users u ON i.created_by = u.id
            WHERE i.id = ?
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Invoice inv = mapInvoice(rs);
                    inv.setDetails(findDetailsByInvoiceId(conn, id));
                    return Optional.of(inv);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Lấy danh sách chi tiết của hóa đơn.
     */
    public List<InvoiceDetail> findDetailsByInvoiceId(Connection conn, int invoiceId) throws SQLException {
        List<InvoiceDetail> list = new ArrayList<>();
        String sql = "SELECT * FROM InvoiceDetails WHERE invoice_id = ? ORDER BY id";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InvoiceDetail d = new InvoiceDetail();
                    d.setId(rs.getInt("id"));
                    d.setInvoiceId(rs.getInt("invoice_id"));
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

    /**
     * Lấy tất cả hóa đơn (sắp xếp giảm dần theo ngày tạo).
     */
    public List<Invoice> findAll(Connection conn) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = """
            SELECT i.*, u.full_name AS staff_name
            FROM Invoices i
            LEFT JOIN Users u ON i.created_by = u.id
            ORDER BY i.invoice_date DESC
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapInvoice(rs));
            }
        }
        return list;
    }

    /**
     * Tìm hóa đơn theo khoảng ngày.
     */
    public List<Invoice> findByDateRange(Connection conn, LocalDate from, LocalDate to) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = """
            SELECT i.*, u.full_name AS staff_name
            FROM Invoices i
            LEFT JOIN Users u ON i.created_by = u.id
            WHERE CAST(i.invoice_date AS DATE) BETWEEN ? AND ?
            ORDER BY i.invoice_date DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInvoice(rs));
                }
            }
        }
        return list;
    }

    /**
     * Hủy hóa đơn (đổi trạng thái sang CANCELLED).
     */
    public void cancel(Connection conn, int id) throws SQLException {
        String sql = "UPDATE Invoices SET status = 'CANCELLED' WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Tổng doanh thu trong ngày hôm nay.
     */
    public long getTodayRevenue(Connection conn) throws SQLException {
        String sql = """
            SELECT ISNULL(SUM(total_amount), 0)
            FROM Invoices
            WHERE CAST(invoice_date AS DATE) = CAST(GETDATE() AS DATE)
              AND status = 'PAID'
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBigDecimal(1).longValue();
            }
        }
        return 0L;
    }

    /**
     * Số lượng hóa đơn lập trong hôm nay.
     */
    public long countTodayInvoices(Connection conn) throws SQLException {
        String sql = """
            SELECT COUNT(*)
            FROM Invoices
            WHERE CAST(invoice_date AS DATE) = CAST(GETDATE() AS DATE)
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0L;
    }

    /**
     * Lấy các hóa đơn gần đây nhất.
     */
    public List<Invoice> getRecentInvoices(Connection conn, int limit) throws SQLException {
        List<Invoice> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) i.*, u.full_name AS staff_name
            FROM Invoices i
            LEFT JOIN Users u ON i.created_by = u.id
            ORDER BY i.invoice_date DESC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInvoice(rs));
                }
            }
        }
        return list;
    }

    /**
     * Thống kê doanh thu theo từng ngày trong 7 ngày gần nhất.
     */
    public Map<String, Long> getRevenueByDayLast7Days(Connection conn) throws SQLException {
        Map<String, Long> result = new LinkedHashMap<>();
        String sql = """
            SELECT FORMAT(invoice_date, 'dd/MM') AS d, ISNULL(SUM(total_amount), 0) AS rev
            FROM Invoices
            WHERE invoice_date >= DATEADD(DAY, -6, CAST(GETDATE() AS DATE))
              AND status = 'PAID'
            GROUP BY FORMAT(invoice_date, 'dd/MM'), CAST(invoice_date AS DATE)
            ORDER BY CAST(invoice_date AS DATE) ASC
            """;
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.put(rs.getString("d"), rs.getBigDecimal("rev").longValue());
            }
        }
        return result;
    }

    /**
     * Thống kê doanh thu theo khoảng ngày (cho báo cáo).
     */
    public List<Map<String, Object>> getRevenueReport(Connection conn, LocalDate from, LocalDate to) throws SQLException {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT CAST(invoice_date AS DATE) AS sale_date,
                   COUNT(*) AS invoice_count,
                   ISNULL(SUM(total_amount), 0) AS total_revenue
            FROM Invoices
            WHERE CAST(invoice_date AS DATE) BETWEEN ? AND ?
              AND status = 'PAID'
            GROUP BY CAST(invoice_date AS DATE)
            ORDER BY CAST(invoice_date AS DATE) ASC
            """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("date", rs.getDate("sale_date").toLocalDate());
                    map.put("count", rs.getInt("invoice_count"));
                    map.put("revenue", rs.getBigDecimal("total_revenue").longValue());
                    list.add(map);
                }
            }
        }
        return list;
    }

    private Invoice mapInvoice(ResultSet rs) throws SQLException {
        Invoice inv = new Invoice();
        inv.setId(rs.getInt("id"));
        inv.setInvoiceCode(rs.getString("invoice_code"));
        Timestamp ts = rs.getTimestamp("invoice_date");
        if (ts != null) {
            inv.setInvoiceDate(ts.toLocalDateTime());
        }
        int custId = rs.getInt("customer_id");
        inv.setCustomerId(rs.wasNull() ? null : custId);
        inv.setCustomerName(rs.getNString("customer_name"));
        inv.setCustomerPhone(rs.getString("customer_phone"));
        inv.setSubtotal(rs.getBigDecimal("subtotal").longValue());
        inv.setDiscount(rs.getBigDecimal("discount").longValue());
        inv.setTotalAmount(rs.getBigDecimal("total_amount").longValue());
        inv.setPaymentMethod(rs.getString("payment_method"));
        inv.setStatus(rs.getString("status"));
        inv.setNote(rs.getNString("note"));
        inv.setCreatedBy(rs.getInt("created_by"));
        try {
            inv.setCreatedByName(rs.getNString("staff_name"));
        } catch (SQLException ignored) {}
        Timestamp createdTs = rs.getTimestamp("created_at");
        if (createdTs != null) {
            inv.setCreatedAt(createdTs.toLocalDateTime());
        }
        return inv;
    }
}
