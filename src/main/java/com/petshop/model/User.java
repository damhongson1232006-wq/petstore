package com.petshop.model;

import java.time.LocalDateTime;

/**
 * Model đại diện cho tài khoản người dùng trong hệ thống.
 */
public class User {

    /** ID người dùng (khóa chính) */
    private int id;

    /** Tên đăng nhập (duy nhất) */
    private String username;

    /** Mật khẩu đã băm (bcrypt hoặc tương đương) */
    private String passwordHash;

    /** Họ và tên đầy đủ */
    private String fullName;

    /** Vai trò: ADMIN hoặc STAFF */
    private String role;

    /** Trạng thái kích hoạt tài khoản */
    private boolean isActive;

    /** Thời điểm tạo tài khoản */
    private LocalDateTime createdAt;

    // =========================================================
    // Constructors
    // =========================================================

    /** Khởi tạo rỗng – dùng khi map dữ liệu từ ResultSet */
    public User() {}

    /**
     * Khởi tạo đầy đủ tất cả các trường.
     *
     * @param id          ID người dùng
     * @param username    Tên đăng nhập
     * @param passwordHash Mật khẩu đã băm
     * @param fullName    Họ tên đầy đủ
     * @param role        Vai trò (ADMIN / STAFF)
     * @param isActive    Trạng thái kích hoạt
     * @param createdAt   Thời điểm tạo
     */
    public User(int id, String username, String passwordHash,
                String fullName, String role, boolean isActive,
                LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.fullName = fullName;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // =========================================================
    // Getters & Setters
    // =========================================================

    public int getId() {
        return id;
    }

    public int getUserId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
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
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", fullName='" + fullName + '\'' +
                ", role='" + role + '\'' +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                '}';
    }
}
