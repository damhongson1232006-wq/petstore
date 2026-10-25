package com.petshop.controller;

import com.petshop.model.Service;
import com.petshop.service.ServiceService;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.List;
import java.util.Optional;

/**
 * Controller cho màn hình Quản lý Dịch vụ Thú cưng.
 */
public class ServiceController {

    @FXML private Button btnAdd;
    @FXML private TextField txtSearch;
    @FXML private Button btnRefresh;

    @FXML private TableView<Service> tableServices;
    @FXML private TableColumn<Service, Void> colStt;
    @FXML private TableColumn<Service, String> colName;
    @FXML private TableColumn<Service, String> colDesc;
    @FXML private TableColumn<Service, String> colPrice;
    @FXML private TableColumn<Service, Void> colAction;

    @FXML private Label lblStatus;

    private final ServiceService serviceService = new ServiceService();
    private final ObservableList<Service> serviceList = FXCollections.observableArrayList();
    private List<Service> allServices = List.of();

    @FXML
    public void initialize() {
        configureTable();
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        loadData();
    }

    private void configureTable() {
        TableMenuUtil.indexColumn(colStt);
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescription() != null ? c.getValue().getDescription() : ""));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getPrice())));
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnToggle = new Button("Xóa");
            private final HBox pane = new HBox(6, btnEdit, btnToggle);

            {
                btnEdit.getStyleClass().add("btn-outline");
                btnToggle.getStyleClass().add("btn-outline");
                pane.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Service s = getTableRow().getItem();
                    if (s != null) showFormDialog(s);
                });

                btnToggle.setOnAction(e -> {
                    Service s = getTableRow().getItem();
                    if (s != null) onDelete(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Service s = getTableRow().getItem();
                    setGraphic(pane);
                }
            }
        });

        tableServices.setItems(serviceList);

        TableMenuUtil.attach(tableServices,
                new TableMenuUtil.Item<Service>("✏️ Sửa giá / thông tin", this::showFormDialog),
                new TableMenuUtil.Item<Service>("🗑 Xóa", this::onDelete, true));
    }

    public void loadData() {
        Task<List<Service>> task = new Task<>() {
            @Override
            protected List<Service> call() throws Exception {
                return serviceService.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            allServices = task.getValue();
            filterData();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải dịch vụ", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    private void filterData() {
        String kw = txtSearch.getText();

        serviceList.setAll(allServices.stream()
                .filter(Service::isActive)
                .filter(s -> {
                    if (kw == null || kw.isBlank()) return true;
                    String lower = kw.toLowerCase().trim();
                    return s.getName().toLowerCase().contains(lower)
                            || (s.getDescription() != null && s.getDescription().toLowerCase().contains(lower));
                })
                .toList());

        lblStatus.setText("Tổng: " + serviceList.size() + " dịch vụ");
    }

    @FXML
    public void onRefresh() {
        txtSearch.clear();
        loadData();
    }

    @FXML
    public void onAddService() {
        showFormDialog(null);
    }

    private void showFormDialog(Service editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Thêm dịch vụ mới" : "Chỉnh sửa dịch vụ #" + editing.getId());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField(editing != null ? editing.getName() : "");
        TextField txtPrice = new TextField(editing != null ? String.valueOf(editing.getPrice()) : "0");
        TextArea txtDesc = new TextArea(editing != null && editing.getDescription() != null ? editing.getDescription() : "");
        txtDesc.setPrefRowCount(3);

        grid.add(new Label("Tên dịch vụ *:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Giá dịch vụ (VNĐ) *:"), 0, 1);
        grid.add(txtPrice, 1, 1);
        grid.add(new Label("Mô tả:"), 0, 2);
        grid.add(txtDesc, 1, 2);

        pane.setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String name = txtName.getText();
                if (name == null || name.isBlank()) {
                    AlertUtil.showError("Lỗi", "Tên dịch vụ không được để trống.");
                    return;
                }
                long price = Long.parseLong(txtPrice.getText().replaceAll("[^0-9]", ""));

                Service s = editing != null ? editing : new Service();
                s.setName(name.trim());
                s.setPrice(price);
                s.setDescription(txtDesc.getText());
                s.setActive(true);

                if (editing == null) {
                    serviceService.add(s);
                } else {
                    serviceService.update(s);
                }
                loadData();
            } catch (Exception ex) {
                AlertUtil.showError("Lỗi", "Không thể lưu dịch vụ: " + ex.getMessage());
            }
        }
    }

    private void onDelete(Service s) {
        if (!AlertUtil.showConfirm("Xác nhận xóa", "Bạn có chắc chắn muốn xóa dịch vụ '" + s.getName() + "' không?")) {
            return;
        }
        try {
            serviceService.setActive(s.getId(), false);
            loadData();
        } catch (Exception ex) {
            AlertUtil.showError("Lỗi", ex.getMessage());
        }
    }
}
