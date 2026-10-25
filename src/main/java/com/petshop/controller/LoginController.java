package com.petshop.controller;

import com.petshop.App;
import com.petshop.service.AuthService;
import com.petshop.util.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Controller cho màn hình đăng nhập.
 * Xử lý xác thực người dùng trên luồng nền và điều hướng sang Dashboard.
 */
public class LoginController {

    @FXML private Label lblError;
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnLogin;
    @FXML private ProgressBar progressBar;

    private final AuthService authService = new AuthService();

    /**
     * Khởi tạo controller: ẩn lỗi, ẩn thanh tiến trình, focus vào ô tên đăng nhập.
     */
    @FXML
    public void initialize() {
        lblError.setVisible(false);
        progressBar.setVisible(false);

        // Đặt focus vào ô tên đăng nhập sau khi giao diện sẵn sàng
        Platform.runLater(() -> txtUsername.requestFocus());
    }

    /**
     * Xử lý sự kiện nhấn nút Đăng nhập.
     * Chạy xác thực trên luồng nền để không chặn UI thread.
     */
    @FXML
    public void onLogin() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText();

        // Kiểm tra đầu vào cơ bản
        if (username.isEmpty() || password.isEmpty()) {
            showError("Vui lòng nhập tên đăng nhập và mật khẩu.");
            return;
        }

        // Hiển thị trạng thái đang xử lý
        setLoadingState(true);

        // Task chạy xác thực trên luồng nền
        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                return authService.login(username, password);
            }
        };

        loginTask.setOnSucceeded(event -> {
            setLoadingState(false);
            boolean success = loginTask.getValue();
            if (success) {
                // Điều hướng đến màn hình Dashboard
                try {
                    App.navigateTo("Dashboard");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Không thể tải màn hình chính: " + ex);
                }
            } else {
                showError("Tên đăng nhập hoặc mật khẩu không đúng.");
                txtPassword.clear();
                txtPassword.requestFocus();
            }
        });

        loginTask.setOnFailed(event -> {
            setLoadingState(false);
            Throwable ex = loginTask.getException();
            showError("Lỗi kết nối: " + (ex != null ? ex.getMessage() : "Không xác định"));
        });

        // Chạy task trên daemon thread
        Thread thread = new Thread(loginTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Xử lý sự kiện nhấn phím: nhấn ENTER để đăng nhập.
     *
     * @param e sự kiện bàn phím
     */
    @FXML
    public void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ENTER) {
            onLogin();
        }
    }

    /**
     * Hiển thị hoặc ẩn trạng thái đang tải.
     *
     * @param loading true = đang tải, false = hoàn tất
     */
    private void setLoadingState(boolean loading) {
        progressBar.setManaged(loading);
        progressBar.setVisible(loading);
        btnLogin.setDisable(loading);
        txtUsername.setDisable(loading);
        txtPassword.setDisable(loading);
        if (loading) {
            lblError.setVisible(false);
            lblError.setManaged(false);
        }
    }

    /**
     * Hiển thị thông báo lỗi cho người dùng.
     *
     * @param message nội dung lỗi cần hiển thị
     */
    private void showError(String message) {
        lblError.setText(message);
        lblError.setManaged(true);
        lblError.setVisible(true);
    }
}
