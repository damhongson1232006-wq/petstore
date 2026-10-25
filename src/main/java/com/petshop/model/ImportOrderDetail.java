package com.petshop.model;

/**
 * Model đại diện cho một dòng chi tiết trong phiếu nhập kho.
 * Mỗi dòng có thể là thú cưng hoặc phụ kiện được nhập từ nhà cung cấp.
 */
public class ImportOrderDetail {

    /** ID chi tiết phiếu nhập (khóa chính) */
    private int id;

    /** ID phiếu nhập kho cha */
    private int importOrderId;

    /** Loại mặt hàng: PET / ACCESSORY */
    private String itemType;

    /** ID của mặt hàng trong bảng tương ứng */
    private int itemId;

    /** Tên mặt hàng tại thời điểm nhập (snapshot) */
    private String itemName;

    /** Số lượng nhập */
    private int quantity;

    /** Đơn giá nhập (VND) */
    private long unitPrice;
    /** Thông tin thú cưng sẽ được tạo khi lưu phiếu nhập (chỉ dùng cho dòng PET). */
    private transient Pet pet;

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public ImportOrderDetail() {}

    /**
     * Khởi tạo đầy đủ tất cả các trường.
     *
     * @param id            ID chi tiết
     * @param importOrderId ID phiếu nhập cha
     * @param itemType      Loại mặt hàng
     * @param itemId        ID mặt hàng
     * @param itemName      Tên mặt hàng (snapshot)
     * @param quantity      Số lượng
     * @param unitPrice     Đơn giá nhập
     */
    public ImportOrderDetail(int id, int importOrderId, String itemType,
                             int itemId, String itemName,
                             int quantity, long unitPrice) {
        this.id = id;
        this.importOrderId = importOrderId;
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
     * Tính thành tiền của dòng chi tiết phiếu nhập này.
     *
     * @return quantity × unitPrice (VND)
     */
    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public long getSubtotal() {
        return (long) quantity * unitPrice;
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

    public int getImportOrderId() {
        return importOrderId;
    }

    public void setImportOrderId(int importOrderId) {
        this.importOrderId = importOrderId;
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
        return "ImportOrderDetail{" +
                "id=" + id +
                ", importOrderId=" + importOrderId +
                ", itemType='" + itemType + '\'' +
                ", itemName='" + itemName + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", subtotal=" + getSubtotal() +
                '}';
    }
}
