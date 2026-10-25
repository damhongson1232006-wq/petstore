package com.petshop.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Model đại diện cho hóa đơn bán hàng.
 * Một hóa đơn có thể chứa nhiều dòng chi tiết (thú cưng, phụ kiện, dịch vụ).
 */
public class Invoice {

    /** ID hóa đơn (khóa chính) */
    private int id;

    /** Mã hóa đơn có dạng HDyyyyMMdd-xxx */
    private String invoiceCode;

    /** Ngày giờ lập hóa đơn */
    private LocalDateTime invoiceDate;

    /** ID khách hàng – nullable (khách vãng lai) */
    private Integer customerId;

    /** Tên khách hàng (snapshot hoặc nhập tay) */
    private String customerName;

    /** Số điện thoại khách hàng (snapshot hoặc nhập tay) */
    private String customerPhone;

    /** Tổng tiền hàng trước giảm giá (VND) */
    private long subtotal;

    /** Số tiền giảm giá (VND) */
    private long discount;

    /** Tổng tiền phải thanh toán = subtotal - discount (VND) */
    private long totalAmount;

    /** Phương thức thanh toán: CASH / TRANSFER */
    private String paymentMethod;

    /** Trạng thái hóa đơn: PAID / CANCELLED */
    private String status;

    /** Ghi chú hóa đơn */
    private String note;

    /** ID người tạo hóa đơn */
    private int createdBy;

    /**
     * Tên người tạo – trường tạm thời, không lưu vào DB,
     * dùng để hiển thị trên giao diện.
     */
    private transient String createdByName;

    /** Thời điểm tạo bản ghi trong DB */
    private LocalDateTime createdAt;

    /** Danh sách các dòng chi tiết hóa đơn */
    private List<InvoiceDetail> details = new ArrayList<>();

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public Invoice() {}

    // =========================================================
    // Business Logic
    // =========================================================

    /**
     * Tính lại tổng tiền hàng và tổng thanh toán dựa trên danh sách chi tiết.
     * Gọi phương thức này sau khi thêm/xóa/sửa chi tiết hóa đơn.
     */
    public void recalculate() {
        subtotal = details.stream()
                .mapToLong(InvoiceDetail::getSubtotal)
                .sum();
        totalAmount = subtotal - discount;
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

    public String getInvoiceCode() {
        return invoiceCode;
    }

    public void setInvoiceCode(String invoiceCode) {
        this.invoiceCode = invoiceCode;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public long getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(long subtotal) {
        this.subtotal = subtotal;
    }

    public long getDiscount() {
        return discount;
    }

    public void setDiscount(long discount) {
        this.discount = discount;
    }

    public long getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(long totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    public List<InvoiceDetail> getDetails() {
        return details;
    }

    public void setDetails(List<InvoiceDetail> details) {
        this.details = details != null ? details : new ArrayList<>();
    }

    // =========================================================
    // toString
    // =========================================================

    @Override
    public String toString() {
        return "Invoice{" +
                "id=" + id +
                ", invoiceCode='" + invoiceCode + '\'' +
                ", invoiceDate=" + invoiceDate +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", detailCount=" + details.size() +
                '}';
    }
}
