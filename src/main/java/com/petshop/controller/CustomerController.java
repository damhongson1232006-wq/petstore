package com.petshop.controller;

import com.petshop.model.Customer;
import com.petshop.service.CustomerService;
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

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller cho màn hình Quản lý Khách hàng.
 */
public class CustomerController {

    @FXML private Button btnAdd;
    @FXML private TextField txtSearch;
    @FXML private Button btnRefresh;

    @FXML private TableView<Customer> tableCustomers;
    @FXML private TableColumn<Customer, Void> colStt;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colPhone;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colAddress;
    @FXML private TableColumn<Customer, String> colCreated;
    @FXML private TableColumn<Customer, Void> colAction;

    @FXML private Label lblStatus;

    private final CustomerService customerService = new CustomerService();
    private final ObservableList<Customer> customerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadData());
        loadData();
    }

    private void configureTable() {
        TableMenuUtil.indexColumn(colStt);
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail() != null ? c.getValue().getEmail() : ""));
        colAddress.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAddress() != null ? c.getValue().getAddress() : ""));
        colCreated.setCellValueFactory(c -> {
            var dt = c.getValue().getCreatedAt();
            return new SimpleStringProperty(dt != null ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
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
                    Customer c = getTableRow().getItem();
                    if (c != null) showFormDialog(c);
                });

                btnDelete.setOnAction(e -> {
                    Customer c = getTableRow().getItem();
                    if (c != null) onDelete(c);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        tableCustomers.setItems(customerList);

        TableMenuUtil.attach(tableCustomers,
                new TableMenuUtil.Item<Customer>("✏️ Sửa", this::showFormDialog),
                new TableMenuUtil.Item<Customer>("🗑 Xóa", this::onDelete, true));
    }

    public void loadData() {
        String keyword = txtSearch.getText();
        Task<List<Customer>> task = new Task<>() {
            @Override
            protected List<Customer> call() throws Exception {
                if (keyword != null && !keyword.isBlank()) {
                    return customerService.search(keyword);
                } else {
                    return customerService.getAll();
                }
            }
        };

        task.setOnSucceeded(e -> {
            customerList.setAll(task.getValue());
            lblStatus.setText("Tổng: " + customerList.size() + " khách hàng");
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải khách hàng", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    @FXML
    public void onRefresh() {
        txtSearch.clear();
        loadData();
    }

    @FXML
    public void onAddCustomer() {
        showFormDialog(null);
    }

    private void showFormDialog(Customer editing) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(editing == null ? "Thêm khách hàng mới" : "Chỉnh sửa khách hàng #" + editing.getId());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtName = new TextField(editing != null ? editing.getFullName() : "");
        TextField txtPhone = new TextField(editing != null ? editing.getPhone() : "");
        TextField txtEmail = new TextField(editing != null && editing.getEmail() != null ? editing.getEmail() : "");
        TextField txtAddress = new TextField(editing != null && editing.getAddress() != null ? editing.getAddress() : "");
        TextArea txtNote = new TextArea(editing != null && editing.getNote() != null ? editing.getNote() : "");
        txtNote.setPrefRowCount(3);

        grid.add(new Label("Họ và tên *:"), 0, 0);
        grid.add(txtName, 1, 0);
        grid.add(new Label("Số điện thoại *:"), 0, 1);
        grid.add(txtPhone, 1, 1);
        grid.add(new Label("Email:"), 0, 2);
        grid.add(txtEmail, 1, 2);
        grid.add(new Label("Địa chỉ:"), 0, 3);
        grid.add(txtAddress, 1, 3);
        grid.add(new Label("Ghi chú:"), 0, 4);
        grid.add(txtNote, 1, 4);

        pane.setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                String name = txtName.getText();
                String phone = txtPhone.getText();
                if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
                    AlertUtil.showError("Lỗi", "Họ tên và số điện thoại không được để trống.");
                    return;
                }

                Customer c = editing != null ? editing : new Customer();
                c.setFullName(name.trim());
                c.setPhone(phone.trim());
                c.setEmail(txtEmail.getText() != null ? txtEmail.getText().trim() : "");
                c.setAddress(txtAddress.getText() != null ? txtAddress.getText().trim() : "");
                c.setNote(txtNote.getText());

                if (editing == null) {
                    customerService.add(c);
                } else {
                    customerService.update(c);
                }
                loadData();
            } catch (Exception ex) {
                AlertUtil.showError("Lỗi", "Không thể lưu khách hàng: " + ex.getMessage());
            }
        }
    }

    private void onDelete(Customer c) {
        if (!AlertUtil.showConfirm("Xác nhận xóa", "Bạn có chắc chắn muốn xóa khách hàng '" + c.getFullName() + "' không?")) {
            return;
        }
        try {
            customerService.delete(c.getId());
            AlertUtil.showInfo("Thành công", "Đã xóa khách hàng thành công.");
            loadData();
        } catch (Exception ex) {
            AlertUtil.showError("Lỗi xóa", ex.getMessage());
        }
    }
}
