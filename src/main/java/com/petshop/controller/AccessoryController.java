package com.petshop.controller;

import com.petshop.model.Accessory;
import com.petshop.service.AccessoryService;
import com.petshop.util.AlertUtil;
import com.petshop.util.TableMenuUtil;
import com.petshop.util.CurrencyUtil;
import javafx.application.Platform;
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
 * Controller cho màn hình Quản lý Phụ kiện & Sản phẩm.
 */
public class AccessoryController {

    @FXML private Button btnAdd;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbCategory;
    @FXML private CheckBox chkLowStock;
    @FXML private Button btnRefresh;

    @FXML private TableView<Accessory> tableAccessories;
    @FXML private TableColumn<Accessory, String> colName;
    @FXML private TableColumn<Accessory, String> colCategory;
    @FXML private TableColumn<Accessory, String> colUnit;
    @FXML private TableColumn<Accessory, String> colImport;
    @FXML private TableColumn<Accessory, String> colSell;
    @FXML private TableColumn<Accessory, String> colQty;
    @FXML private TableColumn<Accessory, String> colMinQty;
    @FXML private TableColumn<Accessory, String> colStatus;
    @FXML private TableColumn<Accessory, Void> colAction;

    @FXML private Label lblStatus;

    private final AccessoryService accessoryService = new AccessoryService();
    private final ObservableList<Accessory> accessoryList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        initFilters();
        loadData();
    }

    private void initFilters() {
        cmbCategory.getItems().addAll("Tất cả", "Thức ăn", "Lồng", "Đồ chơi", "Phụ kiện", "Vệ sinh", "Y tế");
        cmbCategory.setValue("Tất cả");
        cmbCategory.setOnAction(e -> loadData());

        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadData());
    }

    private void configureTable() {
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory() != null ? c.getValue().getCategory() : ""));
        colUnit.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnit() != null ? c.getValue().getUnit() : ""));
        colImport.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getImportPrice())));
        colSell.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getSellPrice())));
        colQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colMinQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getMinQuantity())));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Accessory acc = getTableRow().getItem();
                Label badge = new Label();
                badge.getStyleClass().add("badge");
                if (acc.getQuantity() <= 0) {
                    badge.setText("Hết hàng");
                    badge.getStyleClass().add("badge-danger");
                } else if (acc.isLowStock()) {
                    badge.setText("Sắp hết (" + acc.getQuantity() + ")");
                    badge.getStyleClass().add("badge-warning");
                } else {
                    badge.setText("Còn hàng");
                    badge.getStyleClass().add("badge-success");
                }
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox pane = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().add("btn-outline");
                btnDelete.getStyleClass().add("btn-danger");
                pane.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Accessory acc = getTableRow().getItem();
                    if (acc != null) showFormDialog(acc);
                });

                btnDelete.setOnAction(e -> {
                    Accessory acc = getTableRow().getItem();
                    if (acc != null) onDelete(acc);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tableAccessories.setItems(accessoryList);

        TableMenuUtil.attach(tableAccessories,
                new TableMenuUtil.Item<Accessory>("✏️ Sửa", this::showFormDialog),
                new TableMenuUtil.Item<Accessory>("🗑 Xóa", this::onDelete, true));
    }

    public void loadData() {
        String keyword = txtSearch.getText();
        String cat = cmbCategory.getValue();
        if ("Tất cả".equals(cat)) cat = null;

        String finalCat = cat;
        boolean onlyLowStock = chkLowStock.isSelected();

        Task<List<Accessory>> task = new Task<>() {
            @Override
            protected List<Accessory> call() throws Exception {
                if (onlyLowStock) {
                    return accessoryService.getLowStock();
                } else {
                    return accessoryService.search(keyword, finalCat);
                }
            }
        };

        task.setOnSucceeded(e -> {
            accessoryList.setAll(task.getValue());
            lblStatus.setText("Tổng: " + accessoryList.size() + " sản phẩm");
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải phụ kiện", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    @FXML
    public void onRefresh() {
        txtSearch.clear();
        cmbCategory.setValue("Tất cả");
        chkLowStock.setSelected(false);
        loadData();
    }

    @FXML
    public void onAdd() {
        showFormDialog(null);
    }

    private void showFormDialog(Accessory editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Thêm phụ kiện mới" : "Chỉnh sửa phụ kiện #" + editing.getId());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField(editing != null ? editing.getName() : "");
        ComboBox<String> cmbCat = new ComboBox<>();
        cmbCat.getItems().addAll("Thức ăn", "Lồng", "Đồ chơi", "Phụ kiện", "Vệ sinh", "Y tế");
        cmbCat.setValue(editing != null && editing.getCategory() != null ? editing.getCategory() : "Thức ăn");
        TextField txtUnit = new TextField(editing != null && editing.getUnit() != null ? editing.getUnit() : "Cái");
        TextField txtImport = new TextField(editing != null ? String.valueOf(editing.getImportPrice()) : "0");
        TextField txtSell = new TextField(editing != null ? String.valueOf(editing.getSellPrice()) : "0");
        TextField txtQty = new TextField(editing != null ? String.valueOf(editing.getQuantity()) : "1");
        TextField txtMinQty = new TextField(editing != null ? String.valueOf(editing.getMinQuantity()) : "5");

        grid.add(new Label("Tên sản phẩm *:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Danh mục:"), 0, 1);
        grid.add(cmbCat, 1, 1);
        grid.add(new Label("Đơn vị tính:"), 0, 2);
        grid.add(txtUnit, 1, 2);
        grid.add(new Label("Giá nhập (VNĐ):"), 0, 3);
        grid.add(txtImport, 1, 3);
        grid.add(new Label("Giá bán (VNĐ) *:"), 0, 4);
        grid.add(txtSell, 1, 4);
        grid.add(new Label("Số lượng tồn:"), 0, 5);
        grid.add(txtQty, 1, 5);
        grid.add(new Label("Ngưỡng tối thiểu:"), 0, 6);
        grid.add(txtMinQty, 1, 6);

        pane.setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String name = txtName.getText();
                if (name == null || name.isBlank()) {
                    AlertUtil.showError("Lỗi", "Tên sản phẩm không được để trống.");
                    return;
                }
                long imp = Long.parseLong(txtImport.getText().replaceAll("[^0-9]", ""));
                long sell = Long.parseLong(txtSell.getText().replaceAll("[^0-9]", ""));
                int qty = Integer.parseInt(txtQty.getText().trim());
                int minQty = Integer.parseInt(txtMinQty.getText().trim());

                Accessory acc = editing != null ? editing : new Accessory();
                acc.setName(name.trim());
                acc.setCategory(cmbCat.getValue());
                acc.setUnit(txtUnit.getText().trim());
                acc.setImportPrice(imp);
                acc.setSellPrice(sell);
                acc.setQuantity(qty);
                acc.setMinQuantity(minQty);

                if (editing == null) {
                    accessoryService.add(acc);
                } else {
                    accessoryService.update(acc);
                }
                loadData();
            } catch (Exception ex) {
                AlertUtil.showError("Lỗi", "Không thể lưu phụ kiện: " + ex.getMessage());
            }
        }
    }

    private void onDelete(Accessory acc) {
        if (!AlertUtil.showConfirm("Xác nhận xóa", "Bạn có chắc chắn muốn xóa '" + acc.getName() + "' không?")) {
            return;
        }
        try {
            accessoryService.delete(acc.getId());
            AlertUtil.showInfo("Thành công", "Đã xóa phụ kiện thành công.");
            loadData();
        } catch (Exception ex) {
            AlertUtil.showError("Lỗi xóa", ex.getMessage());
        }
    }
}
