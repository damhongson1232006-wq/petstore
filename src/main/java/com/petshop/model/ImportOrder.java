package com.petshop.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho phiếu nhập kho.
 * Ghi nhận thông tin nhập hàng từ nhà cung cấp (thú cưng hoặc phụ kiện).
 */
public class ImportOrder {

    /** ID phiếu nhập (khóa chính) */
    private int id;

    /** Mã phiếu nhập có dạng PNyyyyMMdd-xxx */
    private String orderCode;

    /** Ngày nhập hàng */
    private LocalDate importDate;

    /** ID nhà cung cấp */
    private int supplierId;

    /**
     * Tên nhà cung cấp – trường tạm thời, không lưu vào DB,
     * dùng để hiển thị trên giao diện.
     */
    private transient String supplierName;

    /** Tổng tiền nhập kho (VND) */
    private long totalAmount;

    /** Ghi chú phiếu nhập */
    private String note;

    /** Trạng thái phiếu nhập (COMPLETED / CANCELLED, ...) */
    private String status;

    /** ID người tạo phiếu nhập */
    private int createdBy;

    /**
     * Tên người tạo – trường tạm thời, không lưu vào DB,
     * dùng để hiển thị trên giao diện.
     */
    private transient String createdByName;

    /** Thời điểm tạo bản ghi trong DB */
    private LocalDateTime createdAt;

    /** Danh sách các dòng chi tiết phiếu nhập */
    private List<ImportOrderDetail> details = new ArrayList<>();

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public ImportOrder() {}

    // =========================================================
    // Getters & Setters
    // =========================================================

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
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

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedByName() {
        return createdByName;
    }

    public void setCreatedByName(String createdByName) {
        this.createdByName = createdByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<ImportOrderDetail> getDetails() {
        return details;
    }

    public void setDetails(List<ImportOrderDetail> details) {
        this.details = details != null ? details : new ArrayList<>();
    }

    // =========================================================
    // toString
    // =========================================================

    @Override
    public String toString() {
        return "ImportOrder{" +
                "id=" + id +
                ", orderCode='" + orderCode + '\'' +
                ", importDate=" + importDate +
                ", supplierName='" + supplierName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", detailCount=" + details.size() +
                '}';
    }
}
