package com.petshop.controller;

import com.petshop.App;
import com.petshop.model.Pet;
import com.petshop.service.PetService;
import com.petshop.util.AlertUtil;
import com.petshop.util.CurrencyUtil;
import com.petshop.util.ImageUtil;
import com.petshop.util.TableMenuUtil;
import com.petshop.util.ExcelExporter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;

/**
 * Controller cho màn hình Danh sách và Quản lý Thú cưng.
 */
public class PetController {

    @FXML private Button btnAdd;
    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cmbSpecies;
    @FXML private ComboBox<String> cmbStatus;
    @FXML private Button btnRefresh;
    @FXML private Button btnExport;

    @FXML private TableView<Pet> tablePets;
    @FXML private TableColumn<Pet, Void> colStt;
    @FXML private TableColumn<Pet, Void> colImage;
    @FXML private TableColumn<Pet, String> colCode;
    @FXML private TableColumn<Pet, String> colName;
    @FXML private TableColumn<Pet, String> colSpecies;
    @FXML private TableColumn<Pet, String> colBreed;
    @FXML private TableColumn<Pet, String> colGender;
    @FXML private TableColumn<Pet, String> colAge;
    @FXML private TableColumn<Pet, String> colPrice;
    @FXML private TableColumn<Pet, String> colStatus;
    @FXML private TableColumn<Pet, String> colImportDate;
    @FXML private TableColumn<Pet, Void> colAction;

    @FXML private Label lblStatus;

    private final PetService petService = new PetService();
    private final ObservableList<Pet> petList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configureTable();
        initFilters();
        loadData();
    }

    private void initFilters() {
        // Cmb Status
        cmbStatus.getItems().addAll("Tất cả", "Đang có sẵn", "Đã bán", "Đã đặt trước");
        cmbStatus.setValue("Tất cả");
        cmbStatus.setOnAction(e -> loadData());

        // Cmb Species
        cmbSpecies.getItems().add("Tất cả");
        cmbSpecies.setValue("Tất cả");
        cmbSpecies.setOnAction(e -> loadData());

        // Load species distinct list
        Task<List<String>> speciesTask = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return petService.getDistinctSpecies();
            }
        };
        speciesTask.setOnSucceeded(e -> {
            cmbSpecies.getItems().clear();
            cmbSpecies.getItems().add("Tất cả");
            cmbSpecies.getItems().addAll(speciesTask.getValue());
            cmbSpecies.setValue("Tất cả");
        });
        new Thread(speciesTask).start();

        // Search text change
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadData());
    }

    private void configureTable() {
        TableMenuUtil.indexColumn(colStt);
        colCode.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPetCode() != null ? c.getValue().getPetCode() : ""));
        colImage.setCellFactory(col -> new TableCell<>() {
            private final ImageView view = new ImageView();

            {
                view.setFitWidth(48);
                view.setFitHeight(48);
                view.setPreserveRatio(true);
                setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Image img = ImageUtil.load(getTableRow().getItem().getImagePath(), 96, 96);
                if (img == null) {
                    setGraphic(null);
                    setText("🐾");
                } else {
                    view.setImage(img);
                    setText(null);
                    setGraphic(view);
                }
            }
        });
        tablePets.setFixedCellSize(56);

        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName() != null ? c.getValue().getName() : ""));
        colSpecies.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSpecies()));
        colBreed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBreed()));
        colGender.setCellValueFactory(c -> {
            String g = "MALE".equalsIgnoreCase(c.getValue().getGender()) ? "Đực"
                    : "FEMALE".equalsIgnoreCase(c.getValue().getGender()) ? "Cái" : "Chưa rõ";
            return new SimpleStringProperty(g);
        });
        colAge.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAgeDisplay()));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getSellPrice())));
        colImportDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getImportDate() != null ? c.getValue().getImportDate().toString() : ""));

        // Custom status badge
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                Pet pet = getTableRow().getItem();
                Label badge = new Label();
                badge.getStyleClass().add("badge");
                String status = pet.getStatus() != null ? pet.getStatus().toUpperCase() : "";
                switch (status) {
                    case "AVAILABLE" -> {
                        badge.setText("Có sẵn");
                        badge.getStyleClass().add("badge-success");
                    }
                    case "SOLD" -> {
                        badge.setText("Đã bán");
                        badge.getStyleClass().add("badge-neutral");
                    }
                    case "RESERVED" -> {
                        badge.setText("Đặt trước");
                        badge.getStyleClass().add("badge-warning");
                    }
                    default -> badge.setText(status);
                }
                setGraphic(badge);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER);
            }
        });

        // Custom actions column
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Sửa");
            private final Button btnDelete = new Button("Xóa");
            private final HBox pane = new HBox(6, btnEdit, btnDelete);

            {
                btnEdit.getStyleClass().addAll("btn-outline");
                btnDelete.getStyleClass().addAll("btn-danger");
                pane.setAlignment(Pos.CENTER);

                btnEdit.setOnAction(e -> {
                    Pet pet = getTableRow().getItem();
                    if (pet != null) onEditPet(pet);
                });

                btnDelete.setOnAction(e -> {
                    Pet pet = getTableRow().getItem();
                    if (pet != null) onDeletePet(pet);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Pet p = getTableRow().getItem();
                    btnDelete.setDisable("SOLD".equalsIgnoreCase(p.getStatus()));
                    setGraphic(pane);
                }
            }
        });

        tablePets.setItems(petList);

        // Menu chuột phải: thao tác nhanh trên dòng đang chọn
        TableMenuUtil.attach(tablePets,
                new TableMenuUtil.Item<Pet>("🔍 Xem chi tiết / ảnh", this::onViewPet),
                new TableMenuUtil.Item<Pet>("✏️ Sửa thông tin", this::onEditPet),
                new TableMenuUtil.Item<Pet>("📋 Sao chép mã thú", this::onCopyCode),
                new TableMenuUtil.Item<Pet>("🗑 Xóa", this::onDeletePet, true));
    }

    public void loadData() {
        String keyword = txtSearch.getText();
        String species = cmbSpecies.getValue();
        if ("Tất cả".equals(species)) species = null;

        String statusRaw = cmbStatus.getValue();
        String status = null;
        if ("Đang có sẵn".equals(statusRaw)) status = "AVAILABLE";
        else if ("Đã bán".equals(statusRaw)) status = "SOLD";
        else if ("Đã đặt trước".equals(statusRaw)) status = "RESERVED";

        String finalSpecies = species;
        String finalStatus = status;

        Task<List<Pet>> task = new Task<>() {
            @Override
            protected List<Pet> call() throws Exception {
                return petService.search(keyword, finalSpecies, finalStatus);
            }
        };

        task.setOnSucceeded(e -> {
            petList.setAll(task.getValue());
            lblStatus.setText("Tổng: " + petList.size() + " con thú");
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải dữ liệu", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    @FXML
    public void onRefresh() {
        txtSearch.clear();
        cmbSpecies.setValue("Tất cả");
        cmbStatus.setValue("Tất cả");
        loadData();
    }

    @FXML
    public void onAddPet() {
        openPetForm(null);
    }

    private void onEditPet(Pet pet) {
        openPetForm(pet);
    }

    private void openPetForm(Pet pet) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/PetForm.fxml"));
            Scene scene = new Scene(loader.load());

            PetFormController controller = loader.getController();
            controller.setPet(pet);

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(App.getPrimaryStage());
            dialog.setTitle(pet == null ? "Thêm thú cưng mới" : "Chỉnh sửa thú cưng " + (pet.getPetCode() != null ? pet.getPetCode() : "#" + pet.getId()));
            dialog.setScene(scene);
            dialog.showAndWait();

            if (controller.isSaved()) {
                loadData();
            }
        } catch (Exception e) {
            AlertUtil.showException("Lỗi mở biểu mẫu thú cưng", e);
        }
    }

    private void onCopyCode(Pet pet) {
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(pet.getPetCode() != null ? pet.getPetCode() : "");
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(content);
    }

    private static String nz(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    private void onViewPet(Pet pet) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Thông tin thú cưng " + nz(pet.getPetCode()));
        dialog.initOwner(App.getPrimaryStage());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        Node picture;
        Image img = ImageUtil.load(pet.getImagePath(), 480, 480);
        if (img != null) {
            ImageView iv = new ImageView(img);
            iv.setFitWidth(240);
            iv.setFitHeight(240);
            iv.setPreserveRatio(true);
            picture = iv;
        } else {
            Label none = new Label("🐾\nChưa có ảnh");
            none.setStyle("-fx-font-size: 18px; -fx-text-fill: #6c757d;");
            none.setMinWidth(240);
            none.setAlignment(Pos.CENTER);
            picture = none;
        }

        String gender = "MALE".equalsIgnoreCase(pet.getGender()) ? "Đực"
                : "FEMALE".equalsIgnoreCase(pet.getGender()) ? "Cái" : "Chưa rõ";
        String status = "AVAILABLE".equalsIgnoreCase(pet.getStatus()) ? "Có sẵn"
                : "SOLD".equalsIgnoreCase(pet.getStatus()) ? "Đã bán"
                : "RESERVED".equalsIgnoreCase(pet.getStatus()) ? "Đặt trước"
                : "Không kinh doanh";
        VBox info = new VBox(6);
        String[] lines = {
                "Mã thú: " + nz(pet.getPetCode()),
                "Loài: " + nz(pet.getSpecies()),
                "Giống: " + nz(pet.getBreed()),
                "Tên: " + nz(pet.getName()),
                "Giới tính: " + gender,
                "Tuổi: " + pet.getAgeDisplay(),
                "Màu sắc: " + nz(pet.getColor()),
                "Cân nặng: " + (pet.getWeightKg() != null ? pet.getWeightKg() + " kg" : "—"),
                "Tiêm phòng: " + (pet.isVaccinated() ? "Đã tiêm" : "Chưa tiêm"),
                "Giá bán: " + CurrencyUtil.format(pet.getSellPrice()),
                "Trạng thái: " + status,
                "Ngày nhập: " + (pet.getImportDate() != null ? pet.getImportDate().toString() : "—"),
                "Nhà cung cấp: " + nz(pet.getSupplierName()),
                "Sức khỏe: " + nz(pet.getHealthNote())
        };
        for (String line : lines) {
            Label l = new Label(line);
            l.setWrapText(true);
            l.setMaxWidth(320);
            info.getChildren().add(l);
        }

        HBox box = new HBox(20, picture, info);
        box.setPadding(new Insets(16));
        dialog.getDialogPane().setContent(box);
        dialog.showAndWait();
    }

    private void onDeletePet(Pet pet) {
        if (!AlertUtil.showConfirm("Xác nhận xóa", "Bạn có chắc chắn muốn xóa thú cưng '" + pet.getLabel() + "' không?")) {
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                petService.delete(pet.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("Thành công", "Đã xóa thú cưng thành công.");
            loadData();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Không thể xóa", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    @FXML
    public void onExport() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu danh sách Thú cưng");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName("Danh_sach_thu_cung_" + java.time.LocalDate.now() + ".xlsx");

        File file = chooser.showSaveDialog(App.getPrimaryStage());
        if (file != null) {
            try {
                ExcelExporter.exportPets(petList, file);
                AlertUtil.showInfo("Xuất file thành công", "Đã lưu danh sách ra:\n" + file.getAbsolutePath());
            } catch (Exception e) {
                AlertUtil.showException("Lỗi xuất file Excel", e);
            }
        }
    }
}
