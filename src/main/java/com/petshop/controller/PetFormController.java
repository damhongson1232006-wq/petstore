package com.petshop.controller;

import com.petshop.model.Pet;
import com.petshop.model.Supplier;
import com.petshop.service.PetService;
import com.petshop.service.SupplierService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import com.petshop.util.ImageUtil;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller cho hộp thoại Thêm / Chỉnh sửa Thú cưng.
 */
public class PetFormController {

    @FXML private Label lblFormTitle;
    @FXML private ComboBox<String> cmbSpecies;
    @FXML private TextField txtBreed;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cmbGender;
    @FXML private Spinner<Integer> spnAge;
    @FXML private TextField txtColor;
    @FXML private TextField txtWeight;
    @FXML private CheckBox chkVaccinated;
    @FXML private TextField txtImportPrice;
    @FXML private TextField txtSellPrice;
    @FXML private ComboBox<Supplier> cmbSupplier;
    @FXML private DatePicker dpImportDate;
    @FXML private TextArea txtHealthNote;
    @FXML private Label lblError;
    @FXML private Button btnSave;
    @FXML private ImageView imgPreview;
    @FXML private Button btnChooseImage;
    @FXML private Button btnClearImage;
    @FXML private Label lblImageName;
    @FXML private Label lblSupplierCap;
    @FXML private Label lblImportDateCap;
    @FXML private Label lblQtyCap;
    @FXML private Spinner<Integer> spnQty;

    private final PetService petService = new PetService();
    private final SupplierService supplierService = new SupplierService();

    private Pet editingPet;
    private boolean saved = false;
    private File selectedImageFile;
    private boolean imageCleared;
    private boolean collectOnly;
    private Pet resultPet;

    @FXML
    public void initialize() {
        // Khởi tạo Spinner tuổi (0 - 240 tháng)
        spnAge.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 240, 2));

        // Khởi tạo ComboBox loài
        cmbSpecies.getItems().addAll("Chó", "Mèo", "Thỏ", "Hamster", "Chim", "Bò sát", "Khác");
        cmbSpecies.setEditable(true);

        // Khởi tạo ComboBox giới tính
        cmbGender.getItems().addAll("Đực", "Cái", "Chưa rõ");
        cmbGender.setValue("Đực");

        dpImportDate.setValue(LocalDate.now());
        spnQty.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));

        // Tải danh sách nhà cung cấp
        loadSuppliers();
    }

    private void loadSuppliers() {
        Task<List<Supplier>> task = new Task<>() {
            @Override
            protected List<Supplier> call() throws Exception {
                return supplierService.getActive();
            }
        };
        task.setOnSucceeded(e -> {
            cmbSupplier.setItems(FXCollections.observableArrayList(task.getValue()));
            // Custom cell rendering cho Supplier name
            cmbSupplier.setCellFactory(lv -> new ListCell<>() {
                @Override
                protected void updateItem(Supplier item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });
            cmbSupplier.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Supplier item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? "" : item.getName());
                }
            });

            if (editingPet != null && editingPet.getSupplierId() > 0) {
                for (Supplier s : cmbSupplier.getItems()) {
                    if (s.getId() == editingPet.getSupplierId()) {
                        cmbSupplier.setValue(s);
                        break;
                    }
                }
            }
        });
        new Thread(task).start();
    }

    public void setPet(Pet pet) {
        this.editingPet = pet;
        if (pet != null) {
            lblFormTitle.setText("Chỉnh sửa thú cưng "
                    + (pet.getPetCode() != null ? pet.getPetCode() : "#" + pet.getId()));
            showPreview(ImageUtil.load(pet.getImagePath(), 220, 220),
                    pet.getImagePath() != null && !pet.getImagePath().isBlank() ? "Đã có ảnh" : "Chưa có ảnh");
            cmbSpecies.setValue(pet.getSpecies());
            txtBreed.setText(pet.getBreed());
            txtName.setText(pet.getName());

            String g = "MALE".equalsIgnoreCase(pet.getGender()) ? "Đực"
                    : "FEMALE".equalsIgnoreCase(pet.getGender()) ? "Cái" : "Chưa rõ";
            cmbGender.setValue(g);

            spnAge.getValueFactory().setValue(pet.getAgeMonths());
            txtColor.setText(pet.getColor());
            txtWeight.setText(pet.getWeightKg() != null ? pet.getWeightKg().toString() : "");
            chkVaccinated.setSelected(pet.isVaccinated());
            txtImportPrice.setText(String.valueOf(pet.getImportPrice()));
            txtSellPrice.setText(String.valueOf(pet.getSellPrice()));
            if (pet.getImportDate() != null) {
                dpImportDate.setValue(pet.getImportDate());
            }
            txtHealthNote.setText(pet.getHealthNote());
        }
    }

    /**
     * Chế độ dùng trong phiếu nhập: chỉ thu thập thông tin thú cưng (và số lượng),
     * không lưu vào CSDL. Nhà cung cấp và ngày nhập lấy theo phiếu nhập.
     */
    public void setCollectOnly(boolean collectOnly) {
        this.collectOnly = collectOnly;
        if (collectOnly) {
            lblFormTitle.setText("Nhập thú cưng vào phiếu nhập");
            for (Node n : List.of(lblSupplierCap, cmbSupplier, lblImportDateCap, dpImportDate)) {
                n.setVisible(false);
                n.setManaged(false);
            }
            for (Node n : List.of(lblQtyCap, spnQty)) {
                n.setVisible(true);
                n.setManaged(true);
            }
            btnSave.setText("Thêm vào phiếu");
        }
    }

    public Pet getResultPet() {
        return resultPet;
    }

    public int getQuantity() {
        Integer q = spnQty.getValue();
        return q == null || q < 1 ? 1 : q;
    }

    private void showPreview(javafx.scene.image.Image image, String caption) {
        imgPreview.setImage(image);
        lblImageName.setText(caption);
    }

    @FXML
    public void onChooseImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Chọn ảnh thú cưng");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Ảnh (*.png, *.jpg, *.jpeg, *.gif, *.bmp)", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"));
        File file = chooser.showOpenDialog(btnSave.getScene().getWindow());
        if (file != null) {
            selectedImageFile = file;
            imageCleared = false;
            showPreview(new javafx.scene.image.Image(file.toURI().toString(), 220, 220, true, true),
                    file.getName());
        }
    }

    @FXML
    public void onClearImage() {
        selectedImageFile = null;
        imageCleared = true;
        showPreview(null, "Chưa có ảnh");
    }

    @FXML
    public void onSave() {
        String species = cmbSpecies.getValue();
        String breed = txtBreed.getText();
        String sellPriceStr = txtSellPrice.getText();

        if (species == null || species.isBlank()) {
            showError("Vui lòng chọn hoặc nhập loài thú cưng.");
            return;
        }
        if (breed == null || breed.isBlank()) {
            showError("Vui lòng nhập giống thú cưng.");
            return;
        }

        long importPrice = 0;
        long sellPrice = 0;
        try {
            if (txtImportPrice.getText() != null && !txtImportPrice.getText().isBlank()) {
                importPrice = Long.parseLong(txtImportPrice.getText().replaceAll("[^0-9]", ""));
            }
        } catch (Exception e) {
            showError("Giá nhập không hợp lệ.");
            return;
        }

        try {
            if (sellPriceStr != null && !sellPriceStr.isBlank()) {
                sellPrice = Long.parseLong(sellPriceStr.replaceAll("[^0-9]", ""));
            } else {
                showError("Vui lòng nhập giá bán.");
                return;
            }
        } catch (Exception e) {
            showError("Giá bán không hợp lệ.");
            return;
        }

        BigDecimal weight = null;
        if (txtWeight.getText() != null && !txtWeight.getText().isBlank()) {
            try {
                weight = new BigDecimal(txtWeight.getText().trim().replace(',', '.'));
                if (weight.signum() < 0 || weight.compareTo(new BigDecimal("999.99")) > 0) {
                    showError("Cân nặng phải từ 0 đến 999.99 kg.");
                    return;
                }
            } catch (Exception ex) {
                showError("Cân nặng không hợp lệ (ví dụ: 2.5).");
                return;
            }
        }

        Pet pet = editingPet != null ? editingPet : new Pet();
        pet.setSpecies(species.trim());
        pet.setBreed(breed.trim());
        String nameVal = txtName.getText() != null ? txtName.getText().trim() : "";
        pet.setName(nameVal.isEmpty() ? null : nameVal);

        String genderVal = "UNKNOWN";
        if ("Đực".equals(cmbGender.getValue())) genderVal = "MALE";
        else if ("Cái".equals(cmbGender.getValue())) genderVal = "FEMALE";
        pet.setGender(genderVal);

        pet.setAgeMonths(spnAge.getValue());
        pet.setColor(txtColor.getText() != null ? txtColor.getText().trim() : "");
        pet.setWeightKg(weight);
        pet.setVaccinated(chkVaccinated.isSelected());
        pet.setImportPrice(importPrice);
        pet.setSellPrice(sellPrice);
        pet.setImportDate(dpImportDate.getValue());
        pet.setHealthNote(txtHealthNote.getText());

        Supplier sup = cmbSupplier.getValue();
        if (sup != null) {
            pet.setSupplierId(sup.getId());
        }

        try {
            if (selectedImageFile != null) {
                pet.setImagePath(ImageUtil.store(selectedImageFile));
            } else if (imageCleared) {
                pet.setImagePath(null);
            }
        } catch (Exception ex) {
            showError("Không lưu được ảnh: " + ex.getMessage());
            return;
        }

        if (collectOnly) {
            resultPet = pet;
            saved = true;
            close();
            return;
        }

        btnSave.setDisable(true);
        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                if (editingPet == null) {
                    petService.add(pet);
                } else {
                    petService.update(pet);
                }
                return null;
            }
        };

        saveTask.setOnSucceeded(e -> {
            saved = true;
            close();
        });

        saveTask.setOnFailed(e -> {
            btnSave.setDisable(false);
            Throwable ex = saveTask.getException();
            showError("Lỗi lưu dữ liệu: " + (ex != null ? ex.getMessage() : ""));
        });

        new Thread(saveTask).start();
    }

    @FXML
    public void onCancel() {
        close();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setManaged(true);
        lblError.setVisible(true);
    }

    private void close() {
        Stage stage = (Stage) btnSave.getScene().getWindow();
        stage.close();
    }

    public boolean isSaved() {
        return saved;
    }
}
