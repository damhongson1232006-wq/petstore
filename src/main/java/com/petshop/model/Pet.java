package com.petshop.model;

import com.petshop.constant.AppConstants;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Model đại diện cho một thú cưng trong cửa hàng.
 * Chứa đầy đủ thông tin sinh học, y tế, giá cả và trạng thái kinh doanh.
 */
public class Pet {

    /** ID thú cưng (khóa chính) */
    private int id;
    private String petCode;

    /** Tên riêng của thú cưng */
    private String name;

    /** Loài vật (chó, mèo, thỏ, ...) */
    private String species;

    /** Giống loài cụ thể */
    private String breed;

    /** Giới tính: MALE / FEMALE / UNKNOWN */
    private String gender;

    /** Tuổi tính theo tháng */
    private int ageMonths;

    /** Màu lông / màu sắc */
    private String color;

    /** Cân nặng (kg) */
    private BigDecimal weightKg;

    /** Ghi chú sức khỏe, bệnh sử */
    private String healthNote;

    /** Đã tiêm phòng hay chưa */
    private boolean vaccinated;

    /** Giá nhập (VND) */
    private long importPrice;

    /** Giá bán (VND) */
    private long sellPrice;

    /** Trạng thái: AVAILABLE / SOLD / RESERVED */
    private String status;

    /** Ngày nhập kho */
    private LocalDate importDate;

    /** ID nhà cung cấp */
    private int supplierId;

    /**
     * Tên nhà cung cấp – trường tạm thời, không lưu vào DB,
     * dùng cho hiển thị trên giao diện.
     */
    private transient String supplierName;

    /** Đường dẫn ảnh thú cưng */
    private String imagePath;

    /** Thời điểm tạo bản ghi */
    private LocalDateTime createdAt;

    /** Thời điểm cập nhật bản ghi lần cuối */
    private LocalDateTime updatedAt;

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public Pet() {}

    // =========================================================
    // Helper Methods
    // =========================================================

    /**
     * Kiểm tra thú cưng có đang sẵn sàng bán không.
     *
     * @return true nếu trạng thái là AVAILABLE
     */
    public boolean isAvailable() {
        return AppConstants.STATUS_AVAILABLE.equals(status);
    }

    /**
     * Kiểm tra thú cưng đã được bán chưa.
     *
     * @return true nếu trạng thái là SOLD
     */
    public boolean isSold() {
        return AppConstants.STATUS_SOLD.equals(status);
    }

    /**
     * Trả về chuỗi hiển thị tuổi thú cưng theo định dạng tiếng Việt.
     * Ví dụ: "3 tháng", "1 năm", "1 năm 2 tháng"
     *
     * @return Chuỗi mô tả tuổi
     */
    public String getAgeDisplay() {
        if (ageMonths <= 0) {
            return "Chưa rõ";
        }
        int years = ageMonths / 12;
        int months = ageMonths % 12;

        if (years == 0) {
            return months + " tháng";
        } else if (months == 0) {
            return years + " năm";
        } else {
            return years + " năm " + months + " tháng";
        }
    }

    // =========================================================
    // Getters & Setters
    // =========================================================

    public String getPetCode() {
        return petCode;
    }

    public void setPetCode(String petCode) {
        this.petCode = petCode;
    }

    /** Tên hiển thị ngắn: giống (và tên riêng nếu có). */
    public String getDisplayName() {
        String b = breed != null ? breed : "";
        return (name != null && !name.isBlank()) ? b + " - " + name : b;
    }

    /** Nhãn đầy đủ: giống (tên) kèm mã thú, ví dụ "Poodle - Bông [TC000012]". */
    public String getLabel() {
        String d = getDisplayName();
        return (petCode != null && !petCode.isBlank()) ? d + " [" + petCode + "]" : d;
    }

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

    public String getSpecies() {
        return species;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public int getAgeMonths() {
        return ageMonths;
    }

    public void setAgeMonths(int ageMonths) {
        this.ageMonths = ageMonths;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public void setWeightKg(BigDecimal weightKg) {
        this.weightKg = weightKg;
    }

    public String getHealthNote() {
        return healthNote;
    }

    public void setHealthNote(String healthNote) {
        this.healthNote = healthNote;
    }

    public boolean isVaccinated() {
        return vaccinated;
    }

    public void setVaccinated(boolean vaccinated) {
        this.vaccinated = vaccinated;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
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
        return "Pet{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", species='" + species + '\'' +
                ", breed='" + breed + '\'' +
                ", gender='" + gender + '\'' +
                ", age=" + getAgeDisplay() +
                ", status='" + status + '\'' +
                ", sellPrice=" + sellPrice +
                '}';
    }
}
