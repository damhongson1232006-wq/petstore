package com.petshop.util;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Tiện ích hiển thị hộp thoại Alert cho JavaFX với chủ đề AtlantaFX PrimerLight.
 * Tất cả phương thức đều là static. Tự động chuyển sang FX Application Thread nếu cần.
 */
public class AlertUtil {

    /** Lớp tiện ích – không cho phép khởi tạo */
    private AlertUtil() {
        throw new UnsupportedOperationException("AlertUtil là lớp tiện ích, không thể khởi tạo.");
    }

    /**
     * Hiển thị hộp thoại thông tin (Information).
     *
     * @param title   tiêu đề hộp thoại
     * @param message nội dung thông báo
     */
    public static void showInfo(String title, String message) {
        runOnFxThread(() -> {
            Alert alert = createAlert(AlertType.INFORMATION, title, message);
            alert.showAndWait();
        });
    }

    /**
     * Hiển thị hộp thoại lỗi (Error).
     *
     * @param title   tiêu đề hộp thoại
     * @param message nội dung thông báo lỗi
     */
    public static void showError(String title, String message) {
        runOnFxThread(() -> {
            Alert alert = createAlert(AlertType.ERROR, title, message);
            alert.showAndWait();
        });
    }

    /**
     * Hiển thị hộp thoại cảnh báo (Warning).
     *
     * @param title   tiêu đề hộp thoại
     * @param message nội dung cảnh báo
     */
    public static void showWarning(String title, String message) {
        runOnFxThread(() -> {
            Alert alert = createAlert(AlertType.WARNING, title, message);
            alert.showAndWait();
        });
    }

    /**
     * Hiển thị hộp thoại xác nhận (Confirmation) với nút Có / Không.
     * Có thể gọi từ bất kỳ thread nào; chặn đến khi người dùng chọn.
     *
     * @param title   tiêu đề hộp thoại
     * @param message nội dung câu hỏi xác nhận
     * @return {@code true} nếu người dùng nhấn "Có"; {@code false} nếu chọn "Không" hoặc đóng
     */
    public static boolean showConfirm(String title, String message) {
        AtomicBoolean result = new AtomicBoolean(false);

        if (Platform.isFxApplicationThread()) {
            result.set(showConfirmInternal(title, message));
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    result.set(showConfirmInternal(title, message));
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return result.get();
    }

    /**
     * Hiển thị hộp thoại lỗi kèm chi tiết stack trace của Exception.
     *
     * @param title tiêu đề hộp thoại
     * @param e     ngoại lệ cần hiển thị
     */
    public static void showException(String title, Exception e) {
        runOnFxThread(() -> {
            Alert alert = createAlert(AlertType.ERROR, title,
                    e.getMessage() != null ? e.getMessage() : "Đã xảy ra lỗi không xác định.");

            // Tạo vùng chi tiết stack trace có thể mở rộng
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));

            TextArea textArea = new TextArea(sw.toString());
            textArea.setEditable(false);
            textArea.setWrapText(false);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            GridPane.setVgrow(textArea, Priority.ALWAYS);
            GridPane.setHgrow(textArea, Priority.ALWAYS);

            Label label = new Label("Chi tiết lỗi:");

            GridPane expContent = new GridPane();
            expContent.setMaxWidth(Double.MAX_VALUE);
            expContent.add(label, 0, 0);
            expContent.add(textArea, 0, 1);

            alert.getDialogPane().setExpandableContent(expContent);
            alert.showAndWait();
        });
    }

    // -------------------------------------------------------------------------
    // Phương thức nội bộ
    // -------------------------------------------------------------------------

    /**
     * Tạo Alert được áp dụng chủ đề AtlantaFX PrimerLight.
     */
    private static Alert createAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Áp dụng stylesheet AtlantaFX PrimerLight cho dialog pane
        alert.getDialogPane().getStylesheets().add(
                new PrimerLight().getUserAgentStylesheet()
        );
        return alert;
    }

    /**
     * Logic xác nhận nội bộ (phải chạy trên FX thread).
     */
    private static boolean showConfirmInternal(String title, String message) {
        ButtonType btnYes = new ButtonType("Có");
        ButtonType btnNo  = new ButtonType("Không");

        Alert alert = new Alert(AlertType.CONFIRMATION, message, btnYes, btnNo);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.getDialogPane().getStylesheets().add(
                new PrimerLight().getUserAgentStylesheet()
        );

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnYes;
    }

    /**
     * Đảm bảo Runnable chạy trên JavaFX Application Thread.
     * Nếu đang ở FX thread thì chạy trực tiếp; ngược lại dùng Platform.runLater.
     */
    private static void runOnFxThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            Platform.runLater(action);
        }
    }
}
