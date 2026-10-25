package com.petshop.controller;

import com.petshop.App;
import com.petshop.model.Accessory;
import com.petshop.model.ImportOrder;
import com.petshop.model.ImportOrderDetail;
import com.petshop.model.Pet;
import com.petshop.model.Supplier;
import com.petshop.service.AccessoryService;
import com.petshop.service.SupplierService;
import com.petshop.util.AlertUtil;
import com.petshop.util.CurrencyUtil;
import com.petshop.util.TableMenuUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Hộp thoại tạo phiếu nhập hàng nhiều dòng: có thể nhập cả phụ kiện lẫn thú cưng.
 * Thú cưng nhập vào sẽ được tạo bản ghi (kèm mã thú) khi phiếu nhập được lưu.
 */
public class ImportOrderDialog {

    private final SupplierService supplierService = new SupplierService();
    private final AccessoryService accessoryService = new AccessoryService();
    private final ObservableList<ImportOrderDetail> lines = FXCollections.observableArrayList();
    private final Label lblTotal = new Label("Tổng tiền: 0 đ");

    /**
     * Hiển thị hộp thoại.
     *
     * @return phiếu nhập đã nhập liệu (chưa lưu vào CSDL), hoặc rỗng nếu người dùng hủy
     */
    public Optional<ImportOrder> showAndBuild() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Tạo phiếu nhập hàng mới");
        dialog.setHeaderText("Chọn nhà cung cấp rồi thêm các mặt hàng (phụ kiện, thú cưng) vào phiếu");
        dialog.initOwner(App.getPrimaryStage());
        dialog.setResizable(true);
        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        pane.setPrefWidth(780);

        ComboBox<Supplier> cmbSup = new ComboBox<>();
        cmbSup.setMaxWidth(Double.MAX_VALUE);
        cmbSup.setPromptText("Chọn nhà cung cấp...");
        try {
            List<Supplier> sups = supplierService.getActive();
            cmbSup.getItems().addAll(sups);
        } catch (Exception e) {
            AlertUtil.showException("Lỗi tải nhà cung cấp", e);
        }
        cmbSup.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        cmbSup.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Supplier item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });

        TextField txtNote = new TextField();
        txtNote.setPromptText("Ghi chú phiếu nhập...");

        GridPane head = new GridPane();
        head.setHgap(10);
        head.setVgap(10);
        head.add(new Label("Nhà cung cấp *:"), 0, 0);
        head.add(cmbSup, 1, 0);
        head.add(new Label("Ghi chú:"), 0, 1);
        head.add(txtNote, 1, 1);
        GridPane.setHgrow(cmbSup, Priority.ALWAYS);
        GridPane.setHgrow(txtNote, Priority.ALWAYS);

        TableView<ImportOrderDetail> table = buildTable();

        Button btnAddAcc = new Button("+ Thêm phụ kiện");
        btnAddAcc.getStyleClass().add("btn-outline");
        btnAddAcc.setOnAction(e -> addAccessoryLine(pane.getScene().getWindow()));
        Button btnAddPet = new Button("+ Thêm thú cưng");
        btnAddPet.getStyleClass().add("btn-outline");
        btnAddPet.setOnAction(e -> addPetLines(pane.getScene().getWindow()));
        Button btnRemove = new Button("Xóa dòng đã chọn");
        btnRemove.getStyleClass().add("btn-outline");
        btnRemove.setOnAction(e -> removeLine(table.getSelectionModel().getSelectedItem()));

        HBox actions = new HBox(10, btnAddAcc, btnAddPet, btnRemove);
        actions.setAlignment(Pos.CENTER_LEFT);

        lblTotal.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        HBox totalBox = new HBox(lblTotal);
        totalBox.setAlignment(Pos.CENTER_RIGHT);

        VBox box = new VBox(12, head, actions, table, totalBox);
        box.setPadding(new Insets(16));
        VBox.setVgrow(table, Priority.ALWAYS);
        pane.setContent(box);

        // Kiểm tra dữ liệu: nếu thiếu thì giữ hộp thoại mở
        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            if (cmbSup.getValue() == null) {
                ev.consume();
                AlertUtil.showError("Thiếu thông tin", "Vui lòng chọn nhà cung cấp.");
            } else if (lines.isEmpty()) {
                ev.consume();
                AlertUtil.showError("Thiếu thông tin", "Phiếu nhập chưa có mặt hàng nào.");
            }
        });

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isEmpty() || res.get() != ButtonType.OK) {
            return Optional.empty();
        }

        ImportOrder order = new ImportOrder();
        order.setSupplierId(cmbSup.getValue().getId());
        order.setNote(txtNote.getText());
        order.setImportDate(LocalDate.now());
        order.setDetails(new ArrayList<>(lines));
        return Optional.of(order);
    }

    private TableView<ImportOrderDetail> buildTable() {
        TableView<ImportOrderDetail> table = new TableView<>(lines);
        table.setPrefHeight(280);
        table.setPlaceholder(new Label("Chưa có mặt hàng. Bấm \"+ Thêm phụ kiện\" hoặc \"+ Thêm thú cưng\"."));

        TableColumn<ImportOrderDetail, String> colType = new TableColumn<>("Loại");
        colType.setPrefWidth(90);
        colType.setCellValueFactory(c -> new SimpleStringProperty(
                "PET".equalsIgnoreCase(c.getValue().getItemType()) ? "Thú cưng" : "Phụ kiện"));

        TableColumn<ImportOrderDetail, String> colName = new TableColumn<>("Mặt hàng");
        colName.setPrefWidth(300);
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItemName()));

        TableColumn<ImportOrderDetail, String> colQty = new TableColumn<>("SL");
        colQty.setPrefWidth(60);
        colQty.setStyle("-fx-alignment: CENTER;");
        colQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));

        TableColumn<ImportOrderDetail, String> colPrice = new TableColumn<>("Đơn giá nhập");
        colPrice.setPrefWidth(130);
        colPrice.setStyle("-fx-alignment: CENTER_RIGHT;");
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getUnitPrice())));

        TableColumn<ImportOrderDetail, String> colSub = new TableColumn<>("Thành tiền");
        colSub.setPrefWidth(130);
        colSub.setStyle("-fx-alignment: CENTER_RIGHT;");
        colSub.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getSubtotal())));

        table.getColumns().add(colType);
        table.getColumns().add(colName);
        table.getColumns().add(colQty);
        table.getColumns().add(colPrice);
        table.getColumns().add(colSub);

        TableMenuUtil.attach(table,
                new TableMenuUtil.Item<ImportOrderDetail>("🗑 Xóa dòng này", this::removeLine));
        return table;
    }

    private void removeLine(ImportOrderDetail d) {
        if (d != null) {
            lines.remove(d);
            refreshTotal();
        }
    }

    private void refreshTotal() {
        long total = 0;
        for (ImportOrderDetail d : lines) {
            total += d.getSubtotal();
        }
        lblTotal.setText("Tổng tiền: " + CurrencyUtil.format(total));
    }

    // ---------------------------------------------------------------- phụ kiện

    private void addAccessoryLine(Window owner) {
        List<Accessory> accs;
        try {
            accs = accessoryService.getActive();
        } catch (Exception e) {
            AlertUtil.showException("Lỗi tải phụ kiện", e);
            return;
        }

        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Thêm phụ kiện nhập kho");
        d.initOwner(owner);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        ComboBox<Accessory> cmb = new ComboBox<>();
        cmb.setPrefWidth(340);
        cmb.setPromptText("Chọn phụ kiện...");
        cmb.getItems().addAll(accs);
        cmb.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Accessory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName() + " (tồn: " + item.getQuantity() + ")");
            }
        });
        cmb.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Accessory item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getName());
            }
        });
        TextField txtQty = new TextField("10");
        TextField txtPrice = new TextField();
        cmb.valueProperty().addListener((obs, old, val) -> {
            if (val != null) {
                txtPrice.setText(String.valueOf(val.getImportPrice()));
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(16));
        grid.add(new Label("Phụ kiện *:"), 0, 0);
        grid.add(cmb, 1, 0);
        grid.add(new Label("Số lượng *:"), 0, 1);
        grid.add(txtQty, 1, 1);
        grid.add(new Label("Đơn giá nhập (VNĐ) *:"), 0, 2);
        grid.add(txtPrice, 1, 2);
        d.getDialogPane().setContent(grid);

        Button ok = (Button) d.getDialogPane().lookupButton(ButtonType.OK);
        ok.addEventFilter(ActionEvent.ACTION, ev -> {
            try {
                if (cmb.getValue() == null) {
                    throw new IllegalArgumentException("Vui lòng chọn phụ kiện.");
                }
                int q = Integer.parseInt(txtQty.getText().trim());
                if (q <= 0) {
                    throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
                }
                Long.parseLong(txtPrice.getText().replaceAll("[^0-9]", ""));
            } catch (NumberFormatException ex) {
                ev.consume();
                AlertUtil.showError("Dữ liệu không hợp lệ", "Số lượng hoặc đơn giá không hợp lệ.");
            } catch (IllegalArgumentException ex) {
                ev.consume();
                AlertUtil.showError("Dữ liệu không hợp lệ", ex.getMessage());
            }
        });

        Optional<ButtonType> res = d.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            Accessory a = cmb.getValue();
            ImportOrderDetail line = new ImportOrderDetail();
            line.setItemType("ACCESSORY");
            line.setItemId(a.getId());
            line.setItemName(a.getName());
            line.setQuantity(Integer.parseInt(txtQty.getText().trim()));
            line.setUnitPrice(Long.parseLong(txtPrice.getText().replaceAll("[^0-9]", "")));
            lines.add(line);
            refreshTotal();
        }
    }

    // ---------------------------------------------------------------- thú cưng

    private void addPetLines(Window owner) {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/fxml/PetForm.fxml"));
            Scene scene = new Scene(loader.load());
            PetFormController controller = loader.getController();
            controller.setCollectOnly(true);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(owner);
            stage.setTitle("Nhập thú cưng vào phiếu");
            stage.setScene(scene);
            stage.showAndWait();

            if (controller.isSaved() && controller.getResultPet() != null) {
                Pet base = controller.getResultPet();
                int qty = controller.getQuantity();
                for (int i = 0; i < qty; i++) {
                    Pet p = copyOf(base);
                    ImportOrderDetail line = new ImportOrderDetail();
                    line.setItemType("PET");
                    line.setItemId(0);
                    line.setItemName(p.getDisplayName());
                    line.setQuantity(1);
                    line.setUnitPrice(p.getImportPrice());
                    line.setPet(p);
                    lines.add(line);
                }
                refreshTotal();
            }
        } catch (Exception e) {
            AlertUtil.showException("Lỗi mở biểu mẫu nhập thú cưng", e);
        }
    }

    private static Pet copyOf(Pet src) {
        Pet p = new Pet();
        p.setSpecies(src.getSpecies());
        p.setBreed(src.getBreed());
        p.setName(src.getName());
        p.setGender(src.getGender());
        p.setAgeMonths(src.getAgeMonths());
        p.setColor(src.getColor());
        p.setWeightKg(src.getWeightKg());
        p.setHealthNote(src.getHealthNote());
        p.setVaccinated(src.isVaccinated());
        p.setImportPrice(src.getImportPrice());
        p.setSellPrice(src.getSellPrice());
        p.setImagePath(src.getImagePath());
        return p;
    }
}
