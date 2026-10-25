package com.petshop.service;

import com.petshop.model.Invoice;
import com.petshop.repository.AccessoryRepository;
import com.petshop.repository.InvoiceRepository;
import com.petshop.repository.PetRepository;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.util.*;

/**
 * Service tổng hợp dữ liệu thống kê cho Dashboard.
 */
public class DashboardService {

    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final PetRepository petRepository = new PetRepository();
    private final AccessoryRepository accessoryRepository = new AccessoryRepository();

    public Map<String, Object> loadDashboardSummary() throws Exception {
        Map<String, Object> result = new HashMap<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            // 1. KPI Doanh thu hôm nay
            long todayRevenue = invoiceRepository.getTodayRevenue(conn);
            result.put("revenue", todayRevenue);

            // 2. KPI Thú đang tồn
            long petsAvailable = petRepository.countAvailable(conn);
            result.put("petsAvailable", (int) petsAvailable);

            // 3. KPI Số hóa đơn hôm nay
            long invoicesToday = invoiceRepository.countTodayInvoices(conn);
            result.put("invoicesToday", (int) invoicesToday);

            // 4. KPI Số phụ kiện sắp hết hàng
            long lowStock = accessoryRepository.countLowStock(conn);
            result.put("lowStock", (int) lowStock);

            // 5. Biểu đồ doanh thu 7 ngày gần nhất
            Map<String, Long> revenueByDay = invoiceRepository.getRevenueByDayLast7Days(conn);
            result.put("revenueByDay", revenueByDay);

            // 6. Biểu đồ thú cưng theo loài
            Map<String, Long> speciesMap = petRepository.countBySpecies(conn);
            Map<String, Integer> stockBySpecies = new LinkedHashMap<>();
            speciesMap.forEach((k, v) -> stockBySpecies.put(k, v.intValue()));
            result.put("stockBySpecies", stockBySpecies);

            // 7. Bảng hóa đơn gần đây (top 10)
            List<Invoice> recent = invoiceRepository.getRecentInvoices(conn, 10);
            result.put("recentInvoices", recent);
        }

        return result;
    }
}
