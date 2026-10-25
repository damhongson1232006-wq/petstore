package com.petshop;

import com.petshop.service.AuthService;
import com.petshop.service.PetService;
import com.petshop.util.DatabaseManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTest {

    @Test
    public void testDatabaseConnection() {
        boolean connected = DatabaseManager.testConnection();
        System.out.println(">>> Database connected: " + connected);
        assertTrue(connected, "Kết nối CSDL thất bại");
    }

    @Test
    public void testAdminLogin() throws Exception {
        AuthService authService = new AuthService();
        // Generate actual valid bcrypt hash for admin123
        String hashAdmin = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(10, "admin123".toCharArray());
        String hashStaff = at.favre.lib.crypto.bcrypt.BCrypt.withDefaults().hashToString(10, "staff123".toCharArray());
        
        try (var conn = DatabaseManager.getConnection();
             var ps = conn.prepareStatement("UPDATE Users SET password_hash = ? WHERE username = ?")) {
            ps.setString(1, hashAdmin);
            ps.setString(2, "admin");
            ps.executeUpdate();

            ps.setString(1, hashStaff);
            ps.setString(2, "nhanvien");
            ps.executeUpdate();
        }

        boolean ok = authService.login("admin", "admin123");
        System.out.println(">>> Admin login success: " + ok);
        assertTrue(ok, "Đăng nhập admin thất bại");
    }

    @Test
    public void testPetService() throws Exception {
        PetService petService = new PetService();
        var list = petService.getAll();
        System.out.println(">>> Total pets loaded: " + list.size());
        assertNotNull(list);
    }
}
