package com.petshop.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho phụ kiện thú cưng (thức ăn, đồ chơi, vòng cổ, ...).
 * Hỗ trợ quản lý tồn kho với cảnh báo hàng sắp hết.
 */
public class Accessory {

    /** ID phụ kiện (khóa chính) */
    private int id;

    /** Tên phụ kiện */
    private String name;

    /** Danh mục phụ kiện (thức ăn, đồ chơi, dụng cụ vệ sinh, ...) */
    private String category;

    /** Đơn vị tính (cái, gói, hộp, kg, ...) */
    private String unit;

    /** Giá nhập (VND) */
    private long importPrice;

    /** Giá bán (VND) */
    private long sellPrice;

    /** Số lượng tồn kho hiện tại */
    private int quantity;

    /** Số lượng tồn kho tối thiểu – dùng để cảnh báo hàng sắp hết */
    private int minQuantity;

    /** ID nhà cung cấp */
    private int supplierId;

    /**
     * Tên nhà cung cấp – trường tạm thời, không lưu vào DB,
     * dùng để hiển thị trên giao diện.
     */
    private transient String supplierName;

    /** Mô tả chi tiết phụ kiện */
    private String description;

    /** Trạng thái hoạt động của phụ kiện trong danh mục */
    private boolean isActive;

    /** Thời điểm tạo bản ghi */
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật bản ghi lần cuối */
    private LocalDateTime updatedAt;

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public Accessory() {}

    // =========================================================
    // Helper Methods
    // =========================================================

    /**
     * Kiểm tra phụ kiện có đang ở mức tồn kho thấp không.
     * Cảnh báo khi tồn kho <= mức tối thiểu.
     *
     * @return true nếu số lượng tồn kho <= minQuantity
     */
    public boolean isLowStock() {
        return quantity <= minQuantity;
    }

    // =========================================================
    // Getters & Setters
    // =========================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getImportPrice() {
        return importPrice;
    }

    public void setImportPrice(long importPrice) {
        this.importPrice = importPrice;
    }

    public long getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(long sellPrice) {
        this.sellPrice = sellPrice;
    }

    /** Số lượng được phép bán = tồn kho trừ mức tồn tối thiểu phải giữ lại. */
    public int getSellableQuantity() {
        return Math.max(0, quantity - minQuantity);
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getMinQuantity() {
        return minQuantity;
    }

    public void setMinQuantity(int minQuantity) {
        this.minQuantity = minQuantity;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // =========================================================
    // toString
    // =========================================================

    @Override
    public String toString() {
        return "Accessory{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", quantity=" + quantity +
                ", minQuantity=" + minQuantity +
                ", lowStock=" + isLowStock() +
                ", sellPrice=" + sellPrice +
                '}';
    }
}
