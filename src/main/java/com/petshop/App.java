package com.petshop;

import atlantafx.base.theme.PrimerLight;
import com.petshop.util.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * JavaFX Application — điểm khởi động giao diện.
 */
public class App extends Application {

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;

        // Áp dụng theme AtlantaFX PrimerLight (modern, clean UI)
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        // Cấu hình cửa sổ chính
        stage.setTitle("Quản lý Cửa hàng Thú cưng");
        stage.setMinWidth(1024);
        stage.setMinHeight(700);
        stage.setWidth(1280);
        stage.setHeight(800);

        // Cố gắng đặt icon app
        try {
            stage.getIcons().add(new Image(
                Objects.requireNonNull(App.class.getResourceAsStream("/images/logo.png"))
            ));
        } catch (Exception e) {
            // Icon không bắt buộc
        }

        // Kiểm tra kết nối DB trước khi load UI
        if (!DatabaseManager.testConnection()) {
            showConnectionError(stage);
            return;
        }

        // Nâng cấp cấu trúc CSDL (thêm mã thú cưng nếu chưa có)
        String migrationError = com.petshop.util.SchemaMigrator.run();
        if (migrationError != null) {
            com.petshop.util.AlertUtil.showError("Không thể nâng cấp CSDL",
                    "Chạy file sql/04_migration_pet_code.sql thủ công bằng SSMS rồi mở lại ứng dụng.\n\nChi tiết: " + migrationError);
        }

        // Load màn hình Login
        navigateTo("Login");
        stage.show();
    }

    @Override
    public void stop() {
        // Đóng connection pool khi app thoát
        DatabaseManager.closePool();
    }

    /**
     * Chuyển màn hình — dùng được từ bất kỳ Controller nào.
     * @param fxmlName Tên file FXML (không có .fxml), ví dụ: "Login", "Dashboard"
     */
    public static void navigateTo(String fxmlName) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            App.class.getResource("/fxml/" + fxmlName + ".fxml")
        );
        Scene scene = new Scene(loader.load());
        primaryStage.setScene(scene);

        // Tối ưu: Dashboard chiếm toàn màn hình, Login thì không
        if ("Dashboard".equals(fxmlName)) {
            primaryStage.setMaximized(true);
        } else {
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
        }
    }

    /**
     * Lấy Stage chính để các Controller có thể tham chiếu.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Hiển thị lỗi kết nối DB và thoát.
     */
    private void showConnectionError(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(
            App.class.getResource("/fxml/ConnectionError.fxml")
        );
        // Fallback: dùng Alert thay vì FXML
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.ERROR
        );
        alert.setTitle("Lỗi kết nối CSDL");
        alert.setHeaderText("Không thể kết nối SQL Server");
        alert.setContentText(
            "Vui lòng kiểm tra:\n" +
            "1. SQL Server đang chạy\n" +
            "2. Thông tin kết nối trong file db.properties\n" +
            "   (server, database, username, password)\n\n" +
            "Đường dẫn file cấu hình: src/main/resources/db.properties"
        );
        alert.showAndWait();
        System.exit(1);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
