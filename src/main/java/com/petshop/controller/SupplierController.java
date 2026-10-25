package com.petshop.controller;

import com.petshop.model.Supplier;
import com.petshop.service.SupplierService;
import com.petshop.util.AlertUtil;
import com.petshop.util.TableMenuUtil;
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
 * Controller cho màn hình Quản lý Nhà cung cấp.
 */
public class SupplierController {

    @FXML private Button btnAdd;
    @FXML private TextField txtSearch;
    @FXML private Button btnRefresh;

    @FXML private TableView<Supplier> tableSuppliers;
    @FXML private TableColumn<Supplier, Void> colStt;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colAddress;
    @FXML private TableColumn<Supplier, String> colContact;
    @FXML private TableColumn<Supplier, Void> colAction;

    @FXML private Label lblStatus;

    private final SupplierService supplierService = new SupplierService();
    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        loadData();
    }

    private void configureTable() {
        TableMenuUtil.indexColumn(colStt);
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone() != null ? c.getValue().getPhone() : ""));
        colAddress.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAddress() != null ? c.getValue().getAddress() : ""));
        colContact.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getContact() != null ? c.getValue().getContact() : ""));

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnToggle = new Button("Khóa/Mở");
            private final HBox pane = new HBox(6, btnEdit, btnToggle);

            {
                btnEdit.getStyleClass().add("btn-outline");
                btnToggle.getStyleClass().add("btn-outline");
                pane.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Supplier s = getTableRow().getItem();
                    if (s != null) showFormDialog(s);
                });

                btnToggle.setOnAction(e -> {
                    Supplier s = getTableRow().getItem();
                    if (s != null) onToggle(s);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Supplier s = getTableRow().getItem();
                    btnToggle.setText(s.isActive() ? "Ngừng" : "Kích hoạt");
                    setGraphic(pane);
                }
            }
        });

        tableSuppliers.setItems(supplierList);

        TableMenuUtil.attach(tableSuppliers,
                new TableMenuUtil.Item<Supplier>("✏️ Sửa", this::showFormDialog),
                new TableMenuUtil.Item<Supplier>("⏯ Ngừng / Kích hoạt", this::onToggle, true));
    }

    private List<Supplier> allSuppliers = List.of();

    public void loadData() {
        Task<List<Supplier>> task = new Task<>() {
            @Override
            protected List<Supplier> call() throws Exception {
                return supplierService.getAll();
            }
        };

        task.setOnSucceeded(e -> {
            allSuppliers = task.getValue();
            filterData();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải nhà cung cấp", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    private void filterData() {
        String kw = txtSearch.getText();
        if (kw == null || kw.isBlank()) {
            supplierList.setAll(allSuppliers);
        } else {
            String lower = kw.toLowerCase().trim();
            supplierList.setAll(allSuppliers.stream()
                    .filter(s -> (s.getName() != null && s.getName().toLowerCase().contains(lower))
                            || (s.getPhone() != null && s.getPhone().contains(lower))
                            || (s.getContact() != null && s.getContact().toLowerCase().contains(lower)))
                    .toList());
        }
        lblStatus.setText("Tổng: " + supplierList.size() + " nhà cung cấp");
    }

    @FXML
    public void onRefresh() {
        txtSearch.clear();
        loadData();
    }

    @FXML
    public void onAddSupplier() {
        showFormDialog(null);
    }

    private void showFormDialog(Supplier editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Thêm nhà cung cấp mới" : "Chỉnh sửa nhà cung cấp #" + editing.getId());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField(editing != null ? editing.getName() : "");
        TextField txtPhone = new TextField(editing != null && editing.getPhone() != null ? editing.getPhone() : "");
        TextField txtAddress = new TextField(editing != null && editing.getAddress() != null ? editing.getAddress() : "");
        TextField txtContact = new TextField(editing != null && editing.getContact() != null ? editing.getContact() : "");
        TextArea txtNote = new TextArea(editing != null && editing.getNote() != null ? editing.getNote() : "");
        txtNote.setPrefRowCount(3);

        grid.add(new Label("Tên nhà cung cấp *:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Số điện thoại:"), 0, 1);
        grid.add(txtPhone, 1, 1);
        grid.add(new Label("Địa chỉ:"), 0, 2);
        grid.add(txtAddress, 1, 2);
        grid.add(new Label("Người liên hệ:"), 0, 3);
        grid.add(txtContact, 1, 3);
        grid.add(new Label("Ghi chú:"), 0, 4);
        grid.add(txtNote, 1, 4);

        pane.setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String name = txtName.getText();
                if (name == null || name.isBlank()) {
                    AlertUtil.showError("Lỗi", "Tên nhà cung cấp không được để trống.");
                    return;
                }

                Supplier s = editing != null ? editing : new Supplier();
                s.setName(name.trim());
                s.setPhone(txtPhone.getText() != null ? txtPhone.getText().trim() : "");
                s.setAddress(txtAddress.getText() != null ? txtAddress.getText().trim() : "");
                s.setContact(txtContact.getText() != null ? txtContact.getText().trim() : "");
                s.setNote(txtNote.getText());
                s.setActive(true);

                if (editing == null) {
                    supplierService.add(s);
                } else {
                    supplierService.update(s);
                }
                loadData();
            } catch (Exception ex) {
                AlertUtil.showError("Lỗi", "Không thể lưu nhà cung cấp: " + ex.getMessage());
            }
        }
    }

    private void onToggle(Supplier s) {
        try {
            supplierService.setActive(s.getId(), !s.isActive());
            loadData();
        } catch (Exception ex) {
            AlertUtil.showError("Lỗi", ex.getMessage());
        }
    }
}
