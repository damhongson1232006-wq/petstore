package com.petshop.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Quản lý kết nối cơ sở dữ liệu sử dụng HikariCP connection pool.
 * Sử dụng mẫu Singleton với static initializer block.
 * Cấu hình được đọc từ file /db.properties trên classpath.
 */
public class DatabaseManager {

    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

    /** Nguồn dữ liệu HikariCP dùng chung cho toàn ứng dụng */
    private static final HikariDataSource dataSource;

    // Khởi tạo static: tải cấu hình và tạo connection pool
    static {
        HikariDataSource ds;
        try {
            // Đọc file cấu hình từ classpath
            Properties props = new Properties();
            try (InputStream is = DatabaseManager.class.getResourceAsStream("/db.properties")) {
                if (is == null) {
                    throw new IOException("Không tìm thấy file cấu hình /db.properties trên classpath.");
                }
                props.load(is);
            }

            // Cấu hình HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setIdleTimeout(30_000);
            config.setConnectionTimeout(30_000);
            config.setPoolName("PetShopPool");

            // Cài đặt thêm cho SQL Server với NVARCHAR
            config.addDataSourceProperty("sendStringParametersAsUnicode", "true");
            config.addDataSourceProperty("applicationName", "PetShopApp");

            ds = new HikariDataSource(config);
            LOGGER.info("Khởi tạo connection pool thành công: " + config.getPoolName());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Lỗi đọc file cấu hình cơ sở dữ liệu.", e);
            throw new ExceptionInInitializerError(
                    "Không thể tải cấu hình CSDL từ /db.properties: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Lỗi khởi tạo connection pool cơ sở dữ liệu.", e);
            throw new ExceptionInInitializerError(
                    "Không thể khởi tạo HikariCP connection pool. Kiểm tra cấu hình db.properties và SQL Server. Chi tiết: "
                            + e.getMessage());
        }
        dataSource = ds;
    }

    /** Lớp tiện ích – không cho phép khởi tạo */
    private DatabaseManager() {
        throw new UnsupportedOperationException("DatabaseManager là lớp tiện ích, không thể khởi tạo.");
    }

    /**
     * Lấy một kết nối từ connection pool.
     *
     * @return {@link Connection} đang hoạt động
     * @throws SQLException nếu không thể lấy kết nối
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Đóng toàn bộ connection pool khi ứng dụng thoát.
     * Nên gọi tại điểm thoát của ứng dụng (Application.stop()).
     */
    public static void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LOGGER.info("Connection pool đã được đóng.");
        }
    }

    /**
     * Kiểm tra kết nối CSDL có hoạt động không.
     *
     * @return {@code true} nếu kết nối thành công, {@code false} nếu thất bại
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && conn.isValid(5);
        } catch (Throwable e) { // gồm cả ExceptionInInitializerError khi pool khởi tạo thất bại
            LOGGER.log(Level.WARNING, "Kiểm tra kết nối CSDL thất bại.", e);
            return false;
        }
    }
}
