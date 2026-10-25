package com.petshop.service;

import com.petshop.model.ImportOrder;
import com.petshop.model.ImportOrderDetail;
import com.petshop.repository.AccessoryRepository;
import com.petshop.repository.ImportOrderRepository;
import com.petshop.repository.PetRepository;
import com.petshop.repository.InventoryTransactionRepository;
import com.petshop.util.CodeGenerator;
import com.petshop.util.DatabaseManager;
import com.petshop.util.SessionManager;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý Nhập hàng từ nhà cung cấp.
 */
public class ImportOrderService {

    private final ImportOrderRepository importOrderRepository = new ImportOrderRepository();
    private final AccessoryRepository accessoryRepository = new AccessoryRepository();
    private final PetRepository petRepository = new PetRepository();
    private final InventoryTransactionRepository inventoryTransactionRepository = new InventoryTransactionRepository();

    public ImportOrder createOrder(ImportOrder order) throws Exception {
        if (order.getDetails() == null || order.getDetails().isEmpty()) {
            throw new IllegalArgumentException("Phiếu nhập phải có ít nhất một mặt hàng.");
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (ImportOrderDetail d : order.getDetails()) {
                    if ("PET".equalsIgnoreCase(d.getItemType()) && d.getPet() != null && d.getQuantity() != 1) {
                        throw new IllegalArgumentException("Mỗi dòng thú cưng phải có số lượng 1.");
                    }
                    if (d.getQuantity() <= 0) {
                        throw new IllegalArgumentException("Số lượng nhập của '" + d.getItemName() + "' phải lớn hơn 0.");
                    }
                    if (d.getUnitPrice() < 0) {
                        throw new IllegalArgumentException("Đơn giá nhập của '" + d.getItemName() + "' không được âm.");
                    }
                }
                if (order.getOrderCode() == null || order.getOrderCode().isBlank()) {
                    order.setOrderCode(CodeGenerator.generateImportCode(conn));
                }
                if (order.getImportDate() == null) {
                    order.setImportDate(LocalDate.now());
                }
                if (order.getCreatedBy() <= 0) {
                    order.setCreatedBy(SessionManager.getCurrentUserId() > 0 ? SessionManager.getCurrentUserId() : 1);
                }

                // Tính tổng tiền
                long total = 0;
                for (ImportOrderDetail d : order.getDetails()) {
                    total += (long) d.getQuantity() * d.getUnitPrice();
                }
                order.setTotalAmount(total);

                int orderId = importOrderRepository.insert(conn, order);
                order.setId(orderId);

                // Thú cưng nhập mới: tạo bản ghi Pets (kèm mã thú) trước khi lưu chi tiết phiếu
                com.petshop.repository.PetRepository petRepo = new com.petshop.repository.PetRepository();
                for (ImportOrderDetail d : order.getDetails()) {
                    if ("PET".equalsIgnoreCase(d.getItemType()) && d.getPet() != null) {
                        com.petshop.model.Pet pet = d.getPet();
                        if (pet.getSpecies() == null || pet.getSpecies().isBlank()
                                || pet.getBreed() == null || pet.getBreed().isBlank()) {
                            throw new IllegalArgumentException("Thú cưng nhập phải có loài và giống.");
                        }
                        if (pet.getName() != null && pet.getName().isBlank()) {
                            pet.setName(null);
                        }
                        pet.setSupplierId(order.getSupplierId());
                        pet.setImportDate(order.getImportDate());
                        pet.setImportPrice(d.getUnitPrice());
                        pet.setStatus("AVAILABLE");
                        pet.setPetCode(com.petshop.util.CodeGenerator.generatePetCode(conn));
                        d.setItemId(petRepo.insert(conn, pet));
                        d.setItemName(pet.getLabel());
                    }
                }
                importOrderRepository.insertDetails(conn, orderId, order.getDetails());

                // Cập nhật tồn kho phụ kiện + ghi nhật ký biến động kho.
                for (ImportOrderDetail d : order.getDetails()) {
                    if ("ACCESSORY".equalsIgnoreCase(d.getItemType())) {
                        accessoryRepository.updateQuantity(conn, d.getItemId(), d.getQuantity());
                    }
                    inventoryTransactionRepository.insert(
                            conn, CodeGenerator.generateInventoryTransactionCode(),
                            "IMPORT", d.getItemType(), d.getItemId(), d.getItemName(), d.getQuantity(),
                            "IMPORT_ORDER", orderId, "Nhập kho theo phiếu " + order.getOrderCode(), order.getCreatedBy());
                }

                conn.commit();
                return order;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /** Hủy phiếu nhập và hoàn tác tồn kho. Chỉ ADMIN được phép thực hiện. */
    public void cancelOrder(int orderId) throws Exception {
        if (!SessionManager.isAdmin()) {
            throw new IllegalStateException("Chỉ quản trị viên mới được hủy phiếu nhập.");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Optional<ImportOrder> opt = importOrderRepository.findById(conn, orderId);
                if (opt.isEmpty()) throw new IllegalArgumentException("Không tìm thấy phiếu nhập.");
                ImportOrder order = opt.get();
                if ("CANCELLED".equalsIgnoreCase(order.getStatus())) {
                    throw new IllegalStateException("Phiếu nhập này đã được hủy.");
                }
                if (inventoryTransactionRepository.hasReference(conn, "IMPORT_ORDER", orderId, "IMPORT_CANCEL")) {
                    throw new IllegalStateException("Phiếu nhập đã có giao dịch hoàn tác.");
                }

                for (ImportOrderDetail d : order.getDetails()) {
                    if ("PET".equalsIgnoreCase(d.getItemType())) {
                        Optional<com.petshop.model.Pet> pet = petRepository.findById(conn, d.getItemId());
                        if (pet.isEmpty()) throw new IllegalStateException("Không tìm thấy thú cưng " + d.getItemName() + ".");
                        if (!"AVAILABLE".equalsIgnoreCase(pet.get().getStatus())) {
                            throw new IllegalStateException("Không thể hủy phiếu vì thú cưng " + pet.get().getLabel() + " đã được bán/đặt trước.");
                        }
                        petRepository.updateStatus(conn, d.getItemId(), "UNAVAILABLE");
                    } else if ("ACCESSORY".equalsIgnoreCase(d.getItemType())) {
                        var acc = accessoryRepository.findById(conn, d.getItemId());
                        if (acc.isEmpty()) throw new IllegalStateException("Không tìm thấy phụ kiện " + d.getItemName() + ".");
                        if (acc.get().getQuantity() < d.getQuantity()) {
                            throw new IllegalStateException("Không thể hoàn tác phụ kiện '" + d.getItemName() + "' vì tồn kho hiện tại không đủ.");
                        }
                        accessoryRepository.updateQuantity(conn, d.getItemId(), -d.getQuantity());
                    }
                    inventoryTransactionRepository.insert(
                            conn, CodeGenerator.generateInventoryTransactionCode(),
                            "IMPORT_CANCEL", d.getItemType(), d.getItemId(), d.getItemName(), -d.getQuantity(),
                            "IMPORT_ORDER", orderId, "Hoàn tác phiếu " + order.getOrderCode(), SessionManager.getCurrentUserId());
                }

                importOrderRepository.cancel(conn, orderId);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public List<ImportOrder> getAll() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return importOrderRepository.findAll(conn);
        }
    }

    public List<ImportOrder> getByDateRange(LocalDate from, LocalDate to) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return importOrderRepository.findByDateRange(conn, from, to);
        }
    }

    public Optional<ImportOrder> getById(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return importOrderRepository.findById(conn, id);
        }
    }
}
