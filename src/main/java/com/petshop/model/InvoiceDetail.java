package com.petshop.model;

import com.petshop.constant.AppConstants;

/**
 * Model đại diện cho một dòng chi tiết trong hóa đơn bán hàng.
 * Mỗi dòng có thể là thú cưng, phụ kiện hoặc dịch vụ.
 */
public class InvoiceDetail {

    /** ID chi tiết hóa đơn (khóa chính) */
    private int id;

    /** ID hóa đơn cha */
    private int invoiceId;

    /** Loại mặt hàng: PET / ACCESSORY / SERVICE */
    private String itemType;

    /** ID của mặt hàng trong bảng tương ứng */
    private int itemId;

    /** Tên mặt hàng tại thời điểm lập hóa đơn (snapshot) */
    private String itemName;

    /** Số lượng (với thú cưng thường là 1) */
    private int quantity;

    /** Đơn giá tại thời điểm lập hóa đơn (VND) */
    private long unitPrice;

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public InvoiceDetail() {}

    /**
     * Khởi tạo đầy đủ tất cả các trường.
     *
     * @param id        ID chi tiết
     * @param invoiceId ID hóa đơn cha
     * @param itemType  Loại mặt hàng
     * @param itemId    ID mặt hàng
     * @param itemName  Tên mặt hàng (snapshot)
     * @param quantity  Số lượng
     * @param unitPrice Đơn giá
     */
    public InvoiceDetail(int id, int invoiceId, String itemType,
                         int itemId, String itemName,
                         int quantity, long unitPrice) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.itemType = itemType;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // =========================================================
    // Computed Methods
    // =========================================================

    /**
     * Tính thành tiền của dòng chi tiết này.
     *
     * @return quantity × unitPrice (VND)
     */
    public long getSubtotal() {
        return (long) quantity * unitPrice;
    }

    // =========================================================
    // Static Factory Methods
    // =========================================================

    /**
     * Tạo một dòng chi tiết hóa đơn cho thú cưng.
     * Số lượng mặc định là 1, giá là giá bán của thú cưng.
     *
     * @param pet Đối tượng thú cưng
     * @return InvoiceDetail mới chưa có ID
     */
    public static InvoiceDetail forPet(Pet pet) {
        InvoiceDetail detail = new InvoiceDetail();
        detail.setItemType(AppConstants.ITEM_PET);
        detail.setItemId(pet.getId());
        // Giống (+ tên riêng nếu có) kèm mã thú, ví dụ "Poodle - Bông [TC000012]"
        String name = pet.getLabel();
        detail.setItemName(name);
        detail.setQuantity(1);
        detail.setUnitPrice(pet.getSellPrice());
        return detail;
    }

    /**
     * Tạo một dòng chi tiết hóa đơn cho phụ kiện.
     *
     * @param acc Đối tượng phụ kiện
     * @param qty Số lượng mua
     * @return InvoiceDetail mới chưa có ID
     */
    public static InvoiceDetail forAccessory(Accessory acc, int qty) {
        InvoiceDetail detail = new InvoiceDetail();
        detail.setItemType(AppConstants.ITEM_ACCESSORY);
        detail.setItemId(acc.getId());
        detail.setItemName(acc.getName());
        detail.setQuantity(qty);
        detail.setUnitPrice(acc.getSellPrice());
        return detail;
    }

    /**
     * Tạo một dòng chi tiết hóa đơn cho dịch vụ.
     *
     * @param svc Đối tượng dịch vụ
     * @param qty Số lần thực hiện dịch vụ
     * @return InvoiceDetail mới chưa có ID
     */
    public static InvoiceDetail forService(Service svc, int qty) {
        InvoiceDetail detail = new InvoiceDetail();
        detail.setItemType(AppConstants.ITEM_SERVICE);
        detail.setItemId(svc.getId());
        detail.setItemName(svc.getName());
        detail.setQuantity(qty);
        detail.setUnitPrice(svc.getPrice());
        return detail;
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

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public long getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(long unitPrice) {
        this.unitPrice = unitPrice;
    }

    // =========================================================
    // toString
    // =========================================================

    @Override
    public String toString() {
        return "InvoiceDetail{" +
                "id=" + id +
                ", invoiceId=" + invoiceId +
                ", itemType='" + itemType + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
