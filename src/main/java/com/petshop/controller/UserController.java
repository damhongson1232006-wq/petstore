package com.petshop.controller;

import com.petshop.model.User;
import com.petshop.service.AuthService;
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
 * Controller cho màn hình Quản trị Người dùng & Phân quyền.
 */
public class UserController {

    @FXML private TextField txtSearch;
    @FXML private TableView<User> tableUsers;
    @FXML private TableColumn<User, String> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colFullName;
    @FXML private TableColumn<User, String> colRole;
    @FXML private TableColumn<User, String> colStatus;
    @FXML private TableColumn<User, Void> colAction;

    @FXML private Label lblStatus;

    private final AuthService authService = new AuthService();
    private final ObservableList<User> userList = FXCollections.observableArrayList();
    private List<User> allUsers = List.of();

    @FXML
    public void initialize() {
        configureTable();
        txtSearch.textProperty().addListener((obs, old, val) -> filterData());
        loadData();
    }

    private void configureTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getId())));
        colUsername.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsername()));
        colFullName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colRole.setCellValueFactory(c -> {
            String r = "ADMIN".equalsIgnoreCase(c.getValue().getRole()) ? "Quản trị viên" : "Nhân viên";
            return new SimpleStringProperty(r);
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                User u = getTableRow().getItem();
                Label badge = new Label(u.isActive() ? "Hoạt động" : "Bị khóa");
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add(u.isActive() ? "badge-success" : "badge-danger");
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnPass = new Button("Đổi MK");
            private final Button btnToggle = new Button("Khóa/Mở");
            private final HBox pane = new HBox(6, btnPass, btnToggle);

            {
                btnPass.getStyleClass().add("btn-outline");
                btnToggle.getStyleClass().add("btn-outline");
                pane.setAlignment(Pos.CENTER);

                btnPass.setOnAction(e -> {
                    User u = getTableRow().getItem();
                    if (u != null) onChangePassword(u);
                });

                btnToggle.setOnAction(e -> {
                    User u = getTableRow().getItem();
                    if (u != null) onToggleActive(u);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    User u = getTableRow().getItem();
                    btnToggle.setText(u.isActive() ? "Khóa" : "Mở khóa");
                    setGraphic(pane);
                }
            }
        });

        tableUsers.setItems(userList);

        TableMenuUtil.attach(tableUsers,
                new TableMenuUtil.Item<User>("🔑 Đổi mật khẩu", this::onChangePassword),
                new TableMenuUtil.Item<User>("🔒 Khóa / Mở khóa", this::onToggleActive, true));
    }

    public void loadData() {
        Task<List<User>> task = new Task<>() {
            @Override
            protected List<User> call() throws Exception {
                return authService.getAllUsers();
            }
        };

        task.setOnSucceeded(e -> {
            allUsers = task.getValue();
            filterData();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải danh sách người dùng", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    private void filterData() {
        String kw = txtSearch.getText();
        if (kw == null || kw.isBlank()) {
            userList.setAll(allUsers);
        } else {
            String lower = kw.toLowerCase().trim();
            userList.setAll(allUsers.stream()
                    .filter(u -> u.getUsername().toLowerCase().contains(lower)
                            || u.getFullName().toLowerCase().contains(lower))
                    .toList());
        }
        lblStatus.setText("Tổng: " + userList.size() + " tài khoản");
    }

    @FXML
    public void onAddUser() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thêm tài khoản người dùng mới");

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField txtUsername = new TextField();
        PasswordField txtPassword = new PasswordField();
        TextField txtFullName = new TextField();
        ComboBox<String> cmbRole = new ComboBox<>();
        cmbRole.getItems().addAll("STAFF", "ADMIN");
        cmbRole.setValue("STAFF");

        grid.add(new Label("Tên đăng nhập *:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Mật khẩu ban đầu *:"), 0, 1);
        grid.add(txtPassword, 1, 1);
        grid.add(new Label("Họ và tên *:"), 0, 2);
        grid.add(txtFullName, 1, 2);
        grid.add(new Label("Vai trò:"), 0, 3);
        grid.add(cmbRole, 1, 3);

        pane.setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            try {
                String u = txtUsername.getText();
                String p = txtPassword.getText();
                String f = txtFullName.getText();
                if (u == null || u.isBlank() || p == null || p.length() < 6 || f == null || f.isBlank()) {
                    AlertUtil.showError("Lỗi", "Vui lòng nhập đầy đủ thông tin (mật khẩu tối thiểu 6 ký tự).");
                    return;
                }
                authService.createUser(u.trim(), p, f.trim(), cmbRole.getValue());
                AlertUtil.showInfo("Thành công", "Đã tạo tài khoản thành công.");
                loadData();
            } catch (Exception ex) {
                AlertUtil.showError("Lỗi", ex.getMessage());
            }
        }
    }

    private void onChangePassword(User user) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Đổi mật khẩu: " + user.getUsername());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        PasswordField txtNewPass = new PasswordField();
        grid.add(new Label("Mật khẩu mới (tối thiểu 6 ký tự):"), 0, 0);
        grid.add(txtNewPass, 1, 0);

        pane.setContent(grid);

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            String p = txtNewPass.getText();
            if (p == null || p.length() < 6) {
                AlertUtil.showError("Lỗi", "Mật khẩu phải từ 6 ký tự trở lên.");
                return;
            }
            try {
                authService.changePassword(user.getId(), p);
                AlertUtil.showInfo("Thành công", "Đã cập nhật mật khẩu mới.");
            } catch (Exception ex) {
                AlertUtil.showError("Lỗi", ex.getMessage());
            }
        }
    }

    private void onToggleActive(User user) {
        if (user.isActive() && user.getId() == com.petshop.util.SessionManager.getCurrentUserId()) {
            AlertUtil.showError("Không thể thực hiện", "Bạn không thể tự khóa tài khoản đang đăng nhập.");
            return;
        }
        try {
            authService.toggleActive(user.getId(), !user.isActive());
            loadData();
        } catch (Exception ex) {
            AlertUtil.showError("Lỗi", ex.getMessage());
        }
    }
}
