package com.petshop.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho dịch vụ (service) mà cửa hàng cung cấp.
 * Ví dụ: tắm rửa, cắt tỉa lông, tiêm phòng, gửi thú cưng, ...
 */
public class Service {

    /** ID dịch vụ (khóa chính) */
    private int id;

    /** Tên dịch vụ */
    private String name;

    /** Mô tả chi tiết dịch vụ */
    private String description;

    /** Đơn giá dịch vụ (VND) */
    private long price;

    /** Thời gian thực hiện dịch vụ (phút) */
    private int duration;

    /** Trạng thái hoạt động của dịch vụ */
    private boolean isActive;

    /** Thời điểm tạo dịch vụ */
    private LocalDateTime createdAt;

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng */
    public Service() {}

    /**
     * Khởi tạo đầy đủ tất cả các trường.
     *
     * @param id          ID dịch vụ
     * @param name        Tên dịch vụ
     * @param description Mô tả
     * @param price       Đơn giá (VND)
     * @param duration    Thời lượng (phút)
     * @param isActive    Trạng thái hoạt động
     * @param createdAt   Thời điểm tạo
     */
    public Service(int id, String name, String description,
                   long price, int duration, boolean isActive,
                   LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.duration = duration;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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
        return "Service{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", duration=" + duration +
                ", isActive=" + isActive +
                '}';
    }
}
