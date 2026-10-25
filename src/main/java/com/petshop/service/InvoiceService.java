package com.petshop.service;

import com.petshop.model.Invoice;
import com.petshop.model.InvoiceDetail;
import com.petshop.model.Pet;
import com.petshop.repository.AccessoryRepository;
import com.petshop.repository.InvoiceRepository;
import com.petshop.repository.PetRepository;
import com.petshop.repository.InventoryTransactionRepository;
import com.petshop.util.CodeGenerator;
import com.petshop.util.DatabaseManager;
import com.petshop.util.SessionManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý Hóa đơn bán hàng với xử lý giao dịch (Transaction Management).
 */
public class InvoiceService {

    private final InvoiceRepository invoiceRepository = new InvoiceRepository();
    private final PetRepository petRepository = new PetRepository();
    private final AccessoryRepository accessoryRepository = new AccessoryRepository();
    private final InventoryTransactionRepository inventoryTransactionRepository = new InventoryTransactionRepository();

    /**
     * Tạo hóa đơn mới trong một Transaction an toàn:
     * - Kiểm tra tính khả dụng của thú cưng
     * - Cập nhật trạng thái thú cưng sang SOLD
     * - Trừ số lượng tồn kho phụ kiện
     * - Lưu header và details hóa đơn
     */
    public Invoice createInvoice(Invoice invoice) throws Exception {
        if (invoice.getDetails() == null || invoice.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Hóa đơn phải có ít nhất một mặt hàng hoặc dịch vụ.");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Kiểm tra tồn kho & thú cưng
                java.util.Set<Integer> seenPets = new java.util.HashSet<>();
                java.util.Map<Integer, Integer> accNeeded = new java.util.HashMap<>();
                for (InvoiceDetail d : invoice.getDetails()) {
                    if (d.getQuantity() <= 0) {
                        throw new IllegalArgumentException("Số lượng của '" + d.getItemName() + "' phải lớn hơn 0.");
                    }
                    if ("PET".equalsIgnoreCase(d.getItemType())) {
                        if (!seenPets.add(d.getItemId())) {
                            throw new IllegalStateException("Thú cưng mã #" + d.getItemId() + " bị thêm trùng trong hóa đơn.");
                        }
                        Optional<Pet> petOpt = petRepository.findById(conn, d.getItemId());
                        if (petOpt.isEmpty()) {
                            throw new IllegalStateException("Không tìm thấy thú cưng mã #" + d.getItemId());
                        }
                        Pet pet = petOpt.get();
                        if (!"AVAILABLE".equalsIgnoreCase(pet.getStatus())) {
                            throw new IllegalStateException("Thú cưng '" + pet.getLabel() + "' không còn khả dụng để bán.");
                        }
                    } else if ("ACCESSORY".equalsIgnoreCase(d.getItemType())) {
                        accNeeded.merge(d.getItemId(), d.getQuantity(), Integer::sum);
                    }
                }
                for (java.util.Map.Entry<Integer, Integer> en : accNeeded.entrySet()) {
                    var accOpt = accessoryRepository.findById(conn, en.getKey());
                    if (accOpt.isEmpty() || !accOpt.get().isActive()) {
                        throw new IllegalStateException("Không tìm thấy phụ kiện mã #" + en.getKey() + " hoặc đã ngừng bán.");
                    }
                    if (accOpt.get().getSellableQuantity() < en.getValue()) {
                        throw new IllegalStateException("Phụ kiện '" + accOpt.get().getName() + "' chỉ bán được tối đa "
                                + accOpt.get().getSellableQuantity() + " (kho còn " + accOpt.get().getQuantity()
                                + ", phải giữ lại tối thiểu " + accOpt.get().getMinQuantity() + "); đang cần "
                                + en.getValue() + ".");
                    }
                }

                // 2. Tính toán lại tổng tiền
                invoice.recalculate();
                if (invoice.getDiscount() < 0 || invoice.getTotalAmount() < 0) {
                    throw new IllegalArgumentException("Giảm giá không được lớn hơn tổng tiền hàng.");
                }

                // 3. Sinh mã hóa đơn tự động
                if (invoice.getInvoiceCode() == null || invoice.getInvoiceCode().isBlank()) {
                    invoice.setInvoiceCode(CodeGenerator.generateInvoiceCode(conn));
                }
                if (invoice.getInvoiceDate() == null) {
                    invoice.setInvoiceDate(LocalDateTime.now());
                }
                if (invoice.getCreatedBy() <= 0) {
                    invoice.setCreatedBy(SessionManager.getCurrentUserId() > 0 ? SessionManager.getCurrentUserId() : 1);
                }

                // 4. Lưu Hóa đơn
                int invoiceId = invoiceRepository.insert(conn, invoice);
                invoice.setId(invoiceId);

                // 5. Lưu chi tiết
                invoiceRepository.insertDetails(conn, invoiceId, invoice.getDetails());

                // 6. Cập nhật trạng thái thú cưng & kho phụ kiện + ghi nhật ký kho
                for (InvoiceDetail d : invoice.getDetails()) {
                    if ("PET".equalsIgnoreCase(d.getItemType())) {
                        if (!petRepository.markSoldIfAvailable(conn, d.getItemId())) {
                            throw new IllegalStateException("Thú cưng mã #" + d.getItemId() + " vừa được bán ở nơi khác.");
                        }
                        inventoryTransactionRepository.insert(conn, CodeGenerator.generateInventoryTransactionCode(),
                                "SALE", "PET", d.getItemId(), d.getItemName(), -d.getQuantity(),
                                "INVOICE", invoiceId, "Bán theo hóa đơn " + invoice.getInvoiceCode(), invoice.getCreatedBy());
                    } else if ("ACCESSORY".equalsIgnoreCase(d.getItemType())) {
                        if (!accessoryRepository.decreaseIfEnough(conn, d.getItemId(), d.getQuantity())) {
                            throw new IllegalStateException("Phụ kiện '" + d.getItemName() + "' không đủ tồn kho (phải giữ lại mức tối thiểu).");
                        }
                        inventoryTransactionRepository.insert(conn, CodeGenerator.generateInventoryTransactionCode(),
                                "SALE", "ACCESSORY", d.getItemId(), d.getItemName(), -d.getQuantity(),
                                "INVOICE", invoiceId, "Bán theo hóa đơn " + invoice.getInvoiceCode(), invoice.getCreatedBy());
                    }
                }

                conn.commit();
                return invoice;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Hủy hóa đơn và hoàn trả hàng về kho / chuyển thú cưng về AVAILABLE.
     */
    public void cancelInvoice(int invoiceId) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<Invoice> opt = invoiceRepository.findById(conn, invoiceId);
                if (opt.isEmpty()) {
                    throw new IllegalArgumentException("Không tìm thấy hóa đơn #" + invoiceId);
                }
                Invoice invoice = opt.get();
                if ("CANCELLED".equalsIgnoreCase(invoice.getStatus())) {
                    throw new IllegalStateException("Hóa đơn này đã được hủy trước đó.");
                }

                // Hoàn tác các mặt hàng
                List<InvoiceDetail> details = invoiceRepository.findDetailsByInvoiceId(conn, invoiceId);
                for (InvoiceDetail d : details) {
                    if ("PET".equalsIgnoreCase(d.getItemType())) {
                        petRepository.updateStatus(conn, d.getItemId(), "AVAILABLE");
                    } else if ("ACCESSORY".equalsIgnoreCase(d.getItemType())) {
                        accessoryRepository.updateQuantity(conn, d.getItemId(), d.getQuantity());
                    }
                    inventoryTransactionRepository.insert(conn, CodeGenerator.generateInventoryTransactionCode(),
                            "SALE_CANCEL", d.getItemType(), d.getItemId(), d.getItemName(), d.getQuantity(),
                            "INVOICE", invoiceId, "Hoàn tác hóa đơn " + invoice.getInvoiceCode(), SessionManager.getCurrentUserId());
                }

                // Đổi trạng thái hóa đơn
                invoiceRepository.cancel(conn, invoiceId);

                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<Invoice> getAll() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return invoiceRepository.findAll(conn);
        }
    }

    public List<Invoice> getByDateRange(LocalDate from, LocalDate to) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return invoiceRepository.findByDateRange(conn, from, to);
        }
    }

    public Optional<Invoice> getById(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return invoiceRepository.findById(conn, id);
        }
    }
}
