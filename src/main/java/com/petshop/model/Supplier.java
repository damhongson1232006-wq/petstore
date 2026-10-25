package com.petshop.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho nhà cung cấp (supplier) của cửa hàng thú cưng.
 */
public class Supplier {

    /** ID nhà cung cấp (khóa chính) */
    private int id;

    /** Tên nhà cung cấp / công ty */
    private String name;

    /** Số điện thoại liên hệ chính */
    private String phone;

    /** Địa chỉ nhà cung cấp */
    private String address;

    /** Tên người liên hệ tại nhà cung cấp */
    private String contact;

    /** Ghi chú bổ sung */
    private String note;

    /** Trạng thái hoạt động của nhà cung cấp */
    private boolean isActive;

    /** Thời điểm tạo hồ sơ nhà cung cấp */
    private LocalDateTime createdAt;

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public Supplier() {}

    /**
     * Khởi tạo đầy đủ tất cả các trường.
     *
     * @param id        ID nhà cung cấp
     * @param name      Tên nhà cung cấp
     * @param phone     Số điện thoại
     * @param address   Địa chỉ
     * @param contact   Người liên hệ
     * @param note      Ghi chú
     * @param isActive  Trạng thái hoạt động
     * @param createdAt Thời điểm tạo
     */
    public Supplier(int id, String name, String phone, String address,
                    String contact, String note, boolean isActive,
                    LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.contact = contact;
        this.note = note;
        this.isActive = isActive;
        this.createdAt = createdAt;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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

    // =========================================================
    // toString
    // =========================================================

    @Override
    public String toString() {
        return "Supplier{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", contact='" + contact + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
