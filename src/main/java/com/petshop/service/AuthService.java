package com.petshop.service;

import at.favre.lib.crypto.bcrypt.BCrypt;
import com.petshop.model.User;
import com.petshop.repository.UserRepository;
import com.petshop.util.DatabaseManager;
import com.petshop.util.SessionManager;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Service xử lý xác thực và quản lý tài khoản người dùng.
 */
public class AuthService {

    private final UserRepository userRepository = new UserRepository();

    /**
     * Xác thực thông tin đăng nhập.
     * @return true nếu thành công, false nếu sai mật khẩu hoặc không tìm thấy user
     */
    public boolean login(String username, String password) throws Exception {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return false;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            Optional<User> opt = userRepository.findByUsername(conn, username.trim());
            if (opt.isEmpty()) {
                return false;
            }
            User user = opt.get();
            if (!user.isActive()) {
                throw new IllegalStateException("Tài khoản đã bị vô hiệu hóa.");
            }

            // Kiểm tra mật khẩu băm BCrypt
            var result = BCrypt.verifyer().verify(password.toCharArray(), user.getPasswordHash());
            if (result.verified) {
                SessionManager.login(user);
                return true;
            }
            return false;
        }
    }

    /**
     * Đăng xuất khỏi hệ thống.
     */
    public void logout() {
        SessionManager.logout();
    }

    /**
     * Lấy danh sách tất cả người dùng (Admin only).
     */
    public List<User> getAllUsers() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return userRepository.findAll(conn);
        }
    }

    /**
     * Tạo tài khoản mới với mật khẩu được băm BCrypt.
     */
    public User createUser(String username, String rawPassword, String fullName, String role) throws Exception {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.length() < 6) {
            throw new IllegalArgumentException("Tên đăng nhập hoặc mật khẩu không hợp lệ (mật khẩu tối thiểu 6 ký tự).");
        }
        String hash = BCrypt.withDefaults().hashToString(10, rawPassword.toCharArray());
        User u = new User(0, username.trim(), hash, fullName.trim(), role, true, java.time.LocalDateTime.now());
        try (Connection conn = DatabaseManager.getConnection()) {
            int id = userRepository.insert(conn, u);
            u.setId(id);
            return u;
        }
    }

    /**
     * Bật / tắt trạng thái kích hoạt tài khoản.
     */
    public void toggleActive(int userId, boolean active) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            userRepository.setActive(conn, userId, active);
        }
    }

    /**
     * Đổi mật khẩu cho người dùng.
     */
    public void changePassword(int userId, String newPassword) throws Exception {
        String hash = BCrypt.withDefaults().hashToString(10, newPassword.toCharArray());
        try (Connection conn = DatabaseManager.getConnection()) {
            Optional<User> opt = userRepository.findById(conn, userId);
            if (opt.isPresent()) {
                User u = opt.get();
                u.setPasswordHash(hash);
                userRepository.update(conn, u);
            }
        }
    }
}
