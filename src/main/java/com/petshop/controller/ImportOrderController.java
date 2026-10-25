package com.petshop.controller;

import com.petshop.model.Accessory;
import com.petshop.model.ImportOrder;
import com.petshop.model.ImportOrderDetail;
import com.petshop.model.Supplier;
import com.petshop.service.AccessoryService;
import com.petshop.service.ImportOrderService;
import com.petshop.service.SupplierService;
import com.petshop.util.AlertUtil;
import com.petshop.util.TableMenuUtil;
import com.petshop.util.CurrencyUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import com.petshop.util.SessionManager;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller cho màn hình Quản lý Nhập hàng.
 */
public class ImportOrderController {

    @FXML private Button btnAdd;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private TextField txtSearch;
    @FXML private TableView<ImportOrder> tableOrders;
    @FXML private TableColumn<ImportOrder, String> colCode;
    @FXML private TableColumn<ImportOrder, String> colDate;
    @FXML private TableColumn<ImportOrder, String> colSupplier;
    @FXML private TableColumn<ImportOrder, String> colTotal;
    @FXML private TableColumn<ImportOrder, String> colStaff;
    @FXML private TableColumn<ImportOrder, String> colNote;
    @FXML private TableColumn<ImportOrder, String> colStatus;
    @FXML private TableColumn<ImportOrder, Void> colAction;

    @FXML private Label lblStatus;
    @FXML private Label lblTotalCost;

    private final ImportOrderService importOrderService = new ImportOrderService();
    private final SupplierService supplierService = new SupplierService();
    private final AccessoryService accessoryService = new AccessoryService();

    private final ObservableList<ImportOrder> orderList = FXCollections.observableArrayList();
    private List<ImportOrder> allOrders = List.of();

    @FXML
    public void initialize() {
        dpFrom.setValue(LocalDate.now().minusDays(30));
        dpTo.setValue(LocalDate.now());

        configureTable();
        txtSearch.textProperty().addListener((obs, old, val) -> filterData());
        loadData();
    }

    private void configureTable() {
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getOrderCode()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getImportDate() != null ? c.getValue().getImportDate().toString() : ""));
        colSupplier.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSupplierName() != null ? c.getValue().getSupplierName() : "---"));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getTotalAmount())));
        colStaff.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedByName() != null ? c.getValue().getCreatedByName() : "Admin"));
        colNote.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNote() != null ? c.getValue().getNote() : ""));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(
                "CANCELLED".equalsIgnoreCase(c.getValue().getStatus()) ? "Đã hủy" : "Hoàn tất"));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnView = new Button("Xem");
            private final Button btnCancel = new Button("Hủy");
            private final HBox box = new HBox(6, btnView, btnCancel);
            {
                btnView.getStyleClass().add("btn-outline");
                btnCancel.getStyleClass().add("btn-danger");
                btnView.setOnAction(e -> {
                    ImportOrder o = getTableRow().getItem();
                    if (o != null) onViewDetails(o);
                });
                btnCancel.setOnAction(e -> {
                    ImportOrder o = getTableRow().getItem();
                    if (o != null) onCancel(o);
                });
                box.setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                ImportOrder o = empty ? null : getTableRow().getItem();
                btnCancel.setDisable(o == null || !SessionManager.isAdmin() || "CANCELLED".equalsIgnoreCase(o.getStatus()));
                setGraphic(empty ? null : box);
            }
        });

        tableOrders.setItems(orderList);

        TableMenuUtil.attach(tableOrders,
                new TableMenuUtil.Item<ImportOrder>("🔍 Xem chi tiết phiếu nhập", this::onViewDetails),
                new TableMenuUtil.Item<ImportOrder>("↩ Hủy phiếu nhập (ADMIN)", this::onCancel));
    }

    public void loadData() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        Task<List<ImportOrder>> task = new Task<>() {
            @Override
            protected List<ImportOrder> call() throws Exception {
                if (from != null && to != null) {
                    return importOrderService.getByDateRange(from, to);
                } else {
                    return importOrderService.getAll();
                }
            }
        };

        task.setOnSucceeded(e -> {
            allOrders = task.getValue();
            filterData();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải phiếu nhập", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    private void filterData() {
        String kw = txtSearch.getText();
        long totalCost = 0;

        List<ImportOrder> filtered = allOrders.stream()
                .filter(o -> {
                    if (kw == null || kw.isBlank()) return true;
                    String lower = kw.toLowerCase().trim();
                    return o.getOrderCode().toLowerCase().contains(lower)
                            || (o.getSupplierName() != null && o.getSupplierName().toLowerCase().contains(lower));
                })
                .toList();

        for (ImportOrder o : filtered) {
            totalCost += o.getTotalAmount();
        }

        orderList.setAll(filtered);
        lblStatus.setText("Tổng: " + orderList.size() + " phiếu nhập");
        lblTotalCost.setText("Tổng tiền nhập: " + CurrencyUtil.format(totalCost));
    }

    @FXML
    public void onRefresh() {
        txtSearch.clear();
        dpFrom.setValue(LocalDate.now().minusDays(30));
        dpTo.setValue(LocalDate.now());
        loadData();
    }

    @FXML
    public void onNewImportOrder() {
        Optional<ImportOrder> built = new ImportOrderDialog().showAndBuild();
        if (built.isEmpty()) {
            return;
        }
        ImportOrder order = built.get();
        boolean hasPet = order.getDetails().stream()
                .anyMatch(d -> "PET".equalsIgnoreCase(d.getItemType()));
        try {
            importOrderService.createOrder(order);
            AlertUtil.showInfo("Thành công", "Đã tạo phiếu nhập " + order.getOrderCode()
                    + (hasPet ? ", thêm thú cưng vào kho (kèm mã thú)" : "")
                    + " và cập nhật tồn kho phụ kiện.");
            loadData();
        } catch (Exception ex) {
            AlertUtil.showError("Lỗi", "Không thể tạo phiếu nhập: " + ex.getMessage());
        }
    }

    private void onCancel(ImportOrder o) {
        if (o == null) return;
        if (!SessionManager.isAdmin()) {
            AlertUtil.showError("Không đủ quyền", "Chỉ ADMIN mới được hủy phiếu nhập.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hủy phiếu nhập");
        confirm.setHeaderText("Hủy phiếu " + o.getOrderCode() + "?");
        confirm.setContentText("Hệ thống sẽ hoàn tác tồn phụ kiện và đưa thú cưng của phiếu về trạng thái không bán. Thao tác không thể hoàn tác.");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            importOrderService.cancelOrder(o.getId());
            AlertUtil.showInfo("Đã hủy", "Phiếu nhập " + o.getOrderCode() + " đã được hủy và ghi nhận giao dịch hoàn tác.");
            loadData();
        } catch (Exception ex) {
            AlertUtil.showError("Không thể hủy phiếu", ex.getMessage());
        }
    }

    private void onViewDetails(ImportOrder o) {
        try {
            Optional<ImportOrder> fullOpt = importOrderService.getById(o.getId());
            if (fullOpt.isPresent()) {
                ImportOrder full = fullOpt.get();
                StringBuilder sb = new StringBuilder();
                sb.append("Mã phiếu: ").append(full.getOrderCode()).append("\n");
                sb.append("Ngày nhập: ").append(full.getImportDate()).append("\n");
                sb.append("Nhà cung cấp: ").append(full.getSupplierName()).append("\n");
                sb.append("Tổng tiền: ").append(CurrencyUtil.format(full.getTotalAmount())).append("\n\n");
                sb.append("CHI TIẾT MẶT HÀNG:\n");
                if (full.getDetails() != null) {
                    int i = 1;
                    for (ImportOrderDetail d : full.getDetails()) {
                        sb.append(i++).append(". ")
                                .append(d.getItemName())
                                .append(" | SL: ").append(d.getQuantity())
                                .append(" | Đơn giá: ").append(CurrencyUtil.format(d.getUnitPrice()))
                                .append(" | Thành tiền: ").append(CurrencyUtil.format(d.getSubtotal()))
                                .append("\n");
                    }
                }
                AlertUtil.showInfo("Chi tiết phiếu nhập #" + full.getOrderCode(), sb.toString());
            }
        } catch (Exception ex) {
            AlertUtil.showException("Lỗi xem chi tiết", ex);
        }
    }
}
