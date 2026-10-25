package com.petshop.util;

import com.petshop.model.User;

/**
 * Quản lý phiên đăng nhập của người dùng hiện tại.
 * Lớp static, không cần khởi tạo.
 * Lưu trạng thái người dùng đang đăng nhập trong suốt vòng đời ứng dụng.
 */
public class SessionManager {

    private static volatile User currentUser = null;
    private static final SessionManager INSTANCE = new SessionManager();

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public SessionManager() {
    }

    /**
     * Đăng nhập: lưu thông tin người dùng vào phiên làm việc.
     *
     * @param user đối tượng User đã xác thực (không được null)
     * @throws IllegalArgumentException nếu user là null
     */
    public static synchronized void login(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Thông tin người dùng không được để trống.");
        }
        currentUser = user;
    }

    /**
     * Đăng xuất: xóa thông tin phiên làm việc hiện tại.
     */
    public static synchronized void logout() {
        currentUser = null;
    }

    /**
     * Lấy đối tượng người dùng đang đăng nhập.
     *
     * @return {@link User} hiện tại hoặc {@code null} nếu chưa đăng nhập
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * Kiểm tra có người dùng đang đăng nhập không.
     *
     * @return {@code true} nếu đã đăng nhập
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Kiểm tra người dùng hiện tại có vai trò Quản trị viên không.
     *
     * @return {@code true} nếu là admin
     */
    public static boolean isAdmin() {
        return isLoggedIn() && "ADMIN".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Kiểm tra người dùng hiện tại có vai trò Nhân viên không.
     *
     * @return {@code true} nếu là nhân viên (STAFF)
     */
    public static boolean isStaff() {
        return isLoggedIn() && "STAFF".equalsIgnoreCase(currentUser.getRole());
    }

    /**
     * Lấy ID của người dùng đang đăng nhập.
     *
     * @return ID người dùng, hoặc -1 nếu chưa đăng nhập
     */
    public static int getCurrentUserId() {
        return isLoggedIn() ? currentUser.getUserId() : -1;
    }

    /**
     * Lấy tên hiển thị của người dùng đang đăng nhập.
     *
     * @return tên người dùng, hoặc chuỗi rỗng nếu chưa đăng nhập
     */
    public static String getCurrentUserName() {
        return isLoggedIn() ? currentUser.getFullName() : "";
    }
}
