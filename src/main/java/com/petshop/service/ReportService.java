package com.petshop.service;

import com.petshop.model.Accessory;
import com.petshop.model.Pet;
import com.petshop.repository.AccessoryRepository;
import com.petshop.repository.InvoiceRepository;
import com.petshop.repository.PetRepository;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.*;

/**
 * Service phục vụ Báo cáo & Thống kê doanh thu, lợi nhuận, tồn kho.
 */
public class ReportService {

    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final PetRepository petRepository = new PetRepository();
    private final AccessoryRepository accessoryRepository = new AccessoryRepository();

    public List<Map<String, Object>> getRevenueReport(LocalDate from, LocalDate to) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return invoiceRepository.getRevenueReport(conn, from, to);
        }
    }

    public List<Pet> getSoldPets(LocalDate from, LocalDate to) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return petRepository.getSoldBetween(conn, from, to);
        }
    }

    public List<Pet> getAvailablePets() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return petRepository.findAvailable(conn);
        }
    }

    public List<Accessory> getLowStockAccessories() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return accessoryRepository.findLowStock(conn);
        }
    }

    /**
     * Báo cáo lợi nhuận từ bán thú cưng (Doanh thu - Giá nhập).
     */
    public List<Map<String, Object>> getProfitByPetBreed(LocalDate from, LocalDate to) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT p.breed,
                   COUNT(*) AS sold_count,
                   SUM(p.import_price) AS total_import,
                   SUM(d.unit_price) AS total_sell,
                   (SUM(d.unit_price) - SUM(p.import_price)) AS profit
            FROM Pets p
            INNER JOIN InvoiceDetails d ON d.item_id = p.id AND d.item_type = 'PET'
            INNER JOIN Invoices i ON i.id = d.invoice_id
            WHERE CAST(i.invoice_date AS DATE) BETWEEN ? AND ?
              AND i.status = 'PAID'
            GROUP BY p.breed
            ORDER BY profit DESC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(from));
            ps.setDate(2, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("breed", rs.getNString("breed"));
                    map.put("soldCount", rs.getInt("sold_count"));
                    map.put("totalImport", rs.getBigDecimal("total_import").longValue());
                    map.put("totalSell", rs.getBigDecimal("total_sell").longValue());
                    map.put("profit", rs.getBigDecimal("profit").longValue());
                    list.add(map);
                }
            }
        }
        return list;
    }

    /**
     * Top các giống thú bán chạy nhất trong khoảng ngày.
     */
    public List<Map<String, Object>> getTopSellingBreeds(LocalDate from, LocalDate to, int limit) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        String sql = """
            SELECT TOP (?) p.breed, COUNT(*) AS total_sold, SUM(d.unit_price) AS total_sales
            FROM InvoiceDetails d
            INNER JOIN Invoices i ON i.id = d.invoice_id
            INNER JOIN Pets p ON p.id = d.item_id
            WHERE d.item_type = 'PET'
              AND CAST(i.invoice_date AS DATE) BETWEEN ? AND ?
              AND i.status = 'PAID'
            GROUP BY p.breed
            ORDER BY total_sold DESC
            """;
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setDate(2, java.sql.Date.valueOf(from));
            ps.setDate(3, java.sql.Date.valueOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("breed", rs.getNString("breed"));
                    map.put("soldCount", rs.getInt("total_sold"));
                    map.put("totalSales", rs.getBigDecimal("total_sales").longValue());
                    list.add(map);
                }
            }
        }
        return list;
    }
}
