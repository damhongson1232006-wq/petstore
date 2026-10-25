package com.petshop.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho khách hàng của cửa hàng thú cưng.
 */
public class Customer {

    /** ID khách hàng (khóa chính) */
    private int id;

    /** Họ và tên đầy đủ của khách hàng */
    private String fullName;

    /** Số điện thoại liên hệ */
    private String phone;

    /** Địa chỉ email */
    private String email;

    /** Địa chỉ nhà */
    private String address;

    /** Ghi chú thêm về khách hàng */
    private String note;

    /** Thời điểm tạo hồ sơ khách hàng */
    private LocalDateTime createdAt;

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public Customer() {}

    /**
     * Khởi tạo đầy đủ tất cả các trường.
     *
     * @param id        ID khách hàng
     * @param fullName  Họ tên đầy đủ
     * @param phone     Số điện thoại
     * @param email     Email
     * @param address   Địa chỉ
     * @param note      Ghi chú
     * @param createdAt Thời điểm tạo
     */
    public Customer(int id, String fullName, String phone,
                    String email, String address, String note,
                    LocalDateTime createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.note = note;
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

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
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
        return "Customer{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
