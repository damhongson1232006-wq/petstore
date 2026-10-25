package com.petshop.controller;

import com.petshop.App;
import com.petshop.model.Invoice;
import com.petshop.model.User;
import com.petshop.service.DashboardService;
import com.petshop.util.SessionManager;
import com.petshop.util.CurrencyUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller cho màn hình Dashboard chính.
 * Hiển thị KPI, biểu đồ, danh sách hóa đơn gần đây và menu điều hướng sidebar.
 */
public class DashboardController {

    // ==================== Thông tin người dùng ====================
    @FXML private Label lblUserInfo;

    // ==================== KPI Labels ====================
    @FXML private Label kpiRevenue;
    @FXML private Label kpiPetsAvailable;
    @FXML private Label kpiInvoicesToday;
    @FXML private Label kpiLowStock;

    // ==================== Biểu đồ ====================
    @FXML private BarChart<String, Number> chartRevenue;
    @FXML private PieChart chartPetStock;

    // ==================== Bảng hóa đơn gần đây ====================
    @FXML private TableView<Invoice> tableRecentInvoices;
    @FXML private TableColumn<Invoice, String>  colInvCode;
    @FXML private TableColumn<Invoice, String>  colInvDate;
    @FXML private TableColumn<Invoice, String>  colInvCustomer;
    @FXML private TableColumn<Invoice, String>  colInvTotal;
    @FXML private TableColumn<Invoice, String>  colInvPayment;
    @FXML private TableColumn<Invoice, String>  colInvStatus;

    // ==================== Layout ====================
    @FXML private StackPane contentArea;
    @FXML private VBox dashboardView;

    // ==================== Sidebar buttons ====================
    @FXML private Button btnDashboard;
    @FXML private Button btnInvoice;
    @FXML private Button btnPet;
    @FXML private Button btnAccessory;
    @FXML private Button btnService;
    @FXML private Button btnCustomer;
    @FXML private Button btnSupplier;
    @FXML private Button btnImportOrder;
    @FXML private Button btnReport;
    @FXML private Button btnUsers;

    // ==================== Phân quyền ====================
    @FXML private Label lblAdminSection;

    private final DashboardService dashboardService = new DashboardService();
    private List<Button> allMenuButtons;

    /**
     * Khởi tạo controller sau khi FXML được nạp.
     * Thiết lập thông tin người dùng, phân quyền, cột bảng và tải dữ liệu.
     */
    @FXML
    public void initialize() {
        // Danh sách tất cả nút sidebar để quản lý trạng thái active
        allMenuButtons = List.of(
                btnDashboard, btnInvoice, btnPet, btnAccessory,
                btnService, btnCustomer, btnSupplier,
                btnImportOrder, btnReport, btnUsers
        );

        // Hiển thị thông tin người dùng đang đăng nhập
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null) {
            lblUserInfo.setText(currentUser.getFullName() + " (" + currentUser.getRole() + ")");

            // Ẩn mục Quản lý người dùng nếu là nhân viên thường
            boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole())
                           || "MANAGER".equalsIgnoreCase(currentUser.getRole());
            lblAdminSection.setVisible(isAdmin);
            lblAdminSection.setManaged(isAdmin);
            btnUsers.setVisible(isAdmin);
            btnUsers.setManaged(isAdmin);
        }

        // Cấu hình cột bảng hóa đơn
        configureTableColumns();

        // Đặt nút Dashboard là active mặc định
        setActiveButton(btnDashboard);

        // Tải dữ liệu dashboard trên luồng nền
        loadDashboardData();
    }

    /**
     * Cấu hình các cột của bảng hóa đơn gần đây.
     */
    private void configureTableColumns() {
        colInvCode.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getInvoiceCode()));

        colInvDate.setCellValueFactory(data -> {
            java.time.LocalDateTime date = data.getValue().getInvoiceDate();
            String formatted = date != null
                    ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "";
            return new javafx.beans.property.SimpleStringProperty(formatted);
        });

        colInvCustomer.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getCustomerName() != null
                                ? data.getValue().getCustomerName()
                                : "Khách lẻ"
                ));

        colInvTotal.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        CurrencyUtil.format(data.getValue().getTotalAmount())
                ));

        colInvPayment.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getPaymentMethod()
                ));

        colInvStatus.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        translateInvoiceStatus(data.getValue().getStatus())
                ));

        // Màu sắc dòng theo trạng thái hóa đơn
        tableRecentInvoices.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Invoice item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-paid", "row-pending", "row-cancelled");
                if (!empty && item != null) {
                    switch (item.getStatus()) {
                        case "PAID"      -> getStyleClass().add("row-paid");
                        case "PENDING"   -> getStyleClass().add("row-pending");
                        case "CANCELLED" -> getStyleClass().add("row-cancelled");
                    }
                }
            }
        });
    }

    /**
     * Tải dữ liệu KPI và biểu đồ trên luồng nền.
     */
    public void loadDashboardData() {
        Task<Map<String, Object>> task = new Task<>() {
            @Override
            protected Map<String, Object> call() throws Exception {
                return dashboardService.loadDashboardSummary();
            }
        };

        task.setOnSucceeded(event -> {
            Map<String, Object> data = task.getValue();

            // Cập nhật KPI
            kpiRevenue.setText(CurrencyUtil.format(
                    (Long) data.getOrDefault("revenue", 0L)));
            kpiPetsAvailable.setText(
                    String.valueOf(data.getOrDefault("petsAvailable", 0)));
            kpiInvoicesToday.setText(
                    String.valueOf(data.getOrDefault("invoicesToday", 0)));
            kpiLowStock.setText(
                    String.valueOf(data.getOrDefault("lowStock", 0)));

            // Cập nhật biểu đồ doanh thu
            @SuppressWarnings("unchecked")
            Map<String, Long> revenueByDay =
                    (Map<String, Long>) data.getOrDefault("revenueByDay", Map.of());
            setupRevenueChart(revenueByDay);

            // Cập nhật biểu đồ tồn kho thú cưng
            @SuppressWarnings("unchecked")
            Map<String, Integer> stockBySpecies =
                    (Map<String, Integer>) data.getOrDefault("stockBySpecies", Map.of());
            setupPetStockChart(stockBySpecies);

            // Cập nhật bảng hóa đơn gần đây
            @SuppressWarnings("unchecked")
            List<Invoice> recentInvoices =
                    (List<Invoice>) data.getOrDefault("recentInvoices", List.of());
            tableRecentInvoices.setItems(
                    FXCollections.observableArrayList(recentInvoices));
        });

        task.setOnFailed(event -> {
            Throwable ex = task.getException();
            System.err.println("[Dashboard] Lỗi tải dữ liệu: "
                    + (ex != null ? ex.getMessage() : "Không xác định"));
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Cấu hình BarChart doanh thu 7 ngày gần nhất.
     *
     * @param revenueByDay map ngày -> doanh thu
     */
    private void setupRevenueChart(Map<String, Long> revenueByDay) {
        chartRevenue.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu (VNĐ)");

        revenueByDay.forEach((day, amount) ->
                series.getData().add(new XYChart.Data<>(day, amount)));

        chartRevenue.getData().add(series);
        chartRevenue.setTitle("Doanh thu 7 ngày gần nhất");
        chartRevenue.setAnimated(false);
    }

    /**
     * Cấu hình PieChart tồn kho thú cưng theo loài.
     *
     * @param stockBySpecies map loài -> số lượng
     */
    private void setupPetStockChart(Map<String, Integer> stockBySpecies) {
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        stockBySpecies.forEach((species, count) ->
                pieData.add(new PieChart.Data(species + " (" + count + ")", count)));

        chartPetStock.setData(pieData);
        chartPetStock.setTitle("Tồn kho theo loài");
        chartPetStock.setAnimated(false);
    }

    /**
     * Nạp một trang FXML vào khu vực nội dung chính.
     * Ẩn dashboardView và hiển thị trang được chọn.
     *
     * @param fxmlName tên file FXML (không có đường dẫn, không có phần mở rộng)
     */
    public void loadPage(String fxmlName) {
        try {
            String name = fxmlName.endsWith("View") ? fxmlName.substring(0, fxmlName.length() - 4) : fxmlName;
            var resource = App.class.getResource("/fxml/" + name + ".fxml");
            if (resource == null) {
                resource = App.class.getResource("/fxml/" + fxmlName + ".fxml");
            }
            if (resource == null) {
                throw new IOException("Không tìm thấy tệp giao diện: " + fxmlName);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Node page = loader.load();

            // Thay thế nội dung trong contentArea
            contentArea.getChildren().clear();
            contentArea.getChildren().add(page);

            // Ẩn widget dashboard mặc định khi đang xem trang con
            dashboardView.setVisible(false);
            dashboardView.setManaged(false);

        } catch (Exception e) {
            System.err.println("[Dashboard] Không thể nạp trang: " + fxmlName);
            e.printStackTrace();
            showErrorAlert("Không thể mở trang: " + fxmlName + "\n" + e + (e.getCause() != null ? "\nNguyên nhân: " + e.getCause() : ""));
        }
    }

    // ==================== Xử lý sidebar ====================

    @FXML
    public void showDashboard() {
        setActiveButton(btnDashboard);
        // Hiện lại view dashboard mặc định
        contentArea.getChildren().setAll(dashboardView);
        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
        loadDashboardData();
    }

    @FXML
    public void showInvoice() {
        setActiveButton(btnInvoice);
        loadPage("InvoiceView");
    }

    @FXML
    public void showPet() {
        setActiveButton(btnPet);
        loadPage("PetView");
    }

    @FXML
    public void showAccessory() {
        setActiveButton(btnAccessory);
        loadPage("AccessoryView");
    }

    @FXML
    public void showService() {
        setActiveButton(btnService);
        loadPage("ServiceView");
    }

    @FXML
    public void showCustomer() {
        setActiveButton(btnCustomer);
        loadPage("CustomerView");
    }

    @FXML
    public void showSupplier() {
        setActiveButton(btnSupplier);
        loadPage("SupplierView");
    }

    @FXML
    public void showImportOrder() {
        setActiveButton(btnImportOrder);
        loadPage("ImportOrderView");
    }

    @FXML
    public void showReport() {
        setActiveButton(btnReport);
        loadPage("ReportView");
    }

    @FXML
    public void showUsers() {
        setActiveButton(btnUsers);
        loadPage("UserView");
    }

    /**
     * Xử lý đăng xuất: hiển thị hộp thoại xác nhận, sau đó quay về màn hình đăng nhập.
     */
    @FXML
    public void onLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận đăng xuất");
        confirm.setHeaderText("Bạn có chắc muốn đăng xuất không?");
        confirm.setContentText("Phiên làm việc hiện tại sẽ kết thúc.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            SessionManager.getInstance().logout();
            try {
                App.navigateTo("Login");
            } catch (Exception e) {
                showErrorAlert("Không thể quay lại màn hình đăng nhập: " + e.getMessage());
            }
        }
    }

    /**
     * Đặt nút sidebar được chọn là active, loại bỏ active khỏi các nút còn lại.
     *
     * @param activeBtn nút cần đặt trạng thái active
     */
    public void setActiveButton(Button activeBtn) {
        allMenuButtons.forEach(btn ->
                btn.getStyleClass().remove("active"));
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("active");
        }
    }

    /**
     * Dịch trạng thái hóa đơn từ tiếng Anh sang tiếng Việt.
     *
     * @param status trạng thái tiếng Anh
     * @return chuỗi tiếng Việt tương ứng
     */
    private String translateInvoiceStatus(String status) {
        if (status == null) return "";
        return switch (status) {
            case "PAID"      -> "Đã thanh toán";
            case "PENDING"   -> "Chờ thanh toán";
            case "CANCELLED" -> "Đã hủy";
            default          -> status;
        };
    }

    /**
     * Hiển thị thông báo lỗi dạng Alert.
     *
     * @param message nội dung lỗi
     */
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Lỗi");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
