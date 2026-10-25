package com.petshop.controller;

import com.petshop.App;
import com.petshop.model.*;
import com.petshop.service.*;
import com.petshop.util.AlertUtil;
import com.petshop.util.TableMenuUtil;
import com.petshop.util.CurrencyUtil;
import com.petshop.util.PdfExporter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;
import java.util.Optional;

/**
 * Controller cho màn hình POS Bán hàng & Lập hóa đơn.
 */
public class InvoiceFormController {

    // Catalog: Pets
    @FXML private TextField txtSearchPet;
    @FXML private TableView<Pet> tablePets;
    @FXML private TableColumn<Pet, Void> colPetImage;
    @FXML private TableColumn<Pet, String> colPetCode;
    @FXML private TableColumn<Pet, String> colPetBreed;
    @FXML private TableColumn<Pet, String> colPetName;
    @FXML private TableColumn<Pet, String> colPetGender;
    @FXML private TableColumn<Pet, String> colPetAge;
    @FXML private TableColumn<Pet, String> colPetPrice;
    @FXML private TableColumn<Pet, Void> colPetAction;

    // Catalog: Accessories
    @FXML private TextField txtSearchAcc;
    @FXML private TableView<Accessory> tableAccessories;
    @FXML private TableColumn<Accessory, String> colAccName;
    @FXML private TableColumn<Accessory, String> colAccUnit;
    @FXML private TableColumn<Accessory, String> colAccQty;
    @FXML private TableColumn<Accessory, String> colAccPrice;
    @FXML private TableColumn<Accessory, Void> colAccAction;

    // Catalog: Services
    @FXML private TableView<Service> tableServices;
    @FXML private TableColumn<Service, String> colSvcName;
    @FXML private TableColumn<Service, String> colSvcPrice;
    @FXML private TableColumn<Service, Void> colSvcAction;

    // Cart & Payment
    @FXML private ComboBox<Customer> cmbCustomer;
    @FXML private TableView<InvoiceDetail> tableCart;
    @FXML private TableColumn<InvoiceDetail, String> colCartName;
    @FXML private TableColumn<InvoiceDetail, String> colCartQty;
    @FXML private TableColumn<InvoiceDetail, String> colCartSubtotal;
    @FXML private TableColumn<InvoiceDetail, Void> colCartAction;

    @FXML private Label lblSubtotal;
    @FXML private TextField txtDiscount;
    @FXML private RadioButton rbCash;
    @FXML private RadioButton rbTransfer;
    @FXML private Label lblTotalAmount;
    @FXML private TextField txtNote;
    @FXML private Button btnCheckout;

    private final PetService petService = new PetService();
    private final AccessoryService accessoryService = new AccessoryService();
    private final ServiceService serviceService = new ServiceService();
    private final CustomerService customerService = new CustomerService();
    private final InvoiceService invoiceService = new InvoiceService();

    private final ObservableList<Pet> petCatalog = FXCollections.observableArrayList();
    private final ObservableList<Accessory> accCatalog = FXCollections.observableArrayList();
    private final ObservableList<Service> svcCatalog = FXCollections.observableArrayList();
    private final ObservableList<InvoiceDetail> cartList = FXCollections.observableArrayList();

    private long currentSubtotal = 0;
    private long currentTotal = 0;

    @FXML
    public void initialize() {
        configurePetTable();
        configureAccTable();
        configureSvcTable();
        configureCartTable();
        initPaymentControls();
        loadCatalogs();
    }

    private void configurePetTable() {
        colPetCode.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPetCode() != null ? c.getValue().getPetCode() : ""));
        colPetImage.setCellFactory(col -> new TableCell<>() {
            private final javafx.scene.image.ImageView view = new javafx.scene.image.ImageView();

            {
                view.setFitWidth(40);
                view.setFitHeight(40);
                view.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }
                javafx.scene.image.Image img = com.petshop.util.ImageUtil.load(getTableRow().getItem().getImagePath(), 80, 80);
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
        tablePets.setFixedCellSize(48);
        colPetBreed.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBreed()));
        colPetName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName() != null ? c.getValue().getName() : ""));
        colPetGender.setCellValueFactory(c -> {
            String g = "MALE".equalsIgnoreCase(c.getValue().getGender()) ? "Đực"
                    : "FEMALE".equalsIgnoreCase(c.getValue().getGender()) ? "Cái" : "Chưa rõ";
            return new SimpleStringProperty(g);
        });
        colPetAge.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAgeDisplay()));
        colPetPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getSellPrice())));

        colPetAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnAdd = new Button("+ Chọn");
            {
                btnAdd.getStyleClass().add("btn-outline");
                btnAdd.setOnAction(e -> {
                    Pet pet = getTableRow().getItem();
                    if (pet != null) addPetToCart(pet);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnAdd);
            }
        });

        tablePets.setItems(petCatalog);
        txtSearchPet.textProperty().addListener((obs, old, val) -> filterPets());
    }

    private void configureAccTable() {
        colAccName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colAccUnit.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUnit()));
        colAccQty.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getQuantity() + " / " + c.getValue().getSellableQuantity()));
        colAccPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getSellPrice())));

        colAccAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnAdd = new Button("+ Chọn");
            {
                btnAdd.getStyleClass().add("btn-outline");
                btnAdd.setOnAction(e -> {
                    Accessory acc = getTableRow().getItem();
                    if (acc != null) addAccessoryToCart(acc);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnAdd);
            }
        });

        tableAccessories.setItems(accCatalog);
        txtSearchAcc.textProperty().addListener((obs, old, val) -> filterAccessories());
    }

    private void configureSvcTable() {
        colSvcName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colSvcPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getPrice())));

        colSvcAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnAdd = new Button("+ Chọn");
            {
                btnAdd.getStyleClass().add("btn-outline");
                btnAdd.setOnAction(e -> {
                    Service svc = getTableRow().getItem();
                    if (svc != null) addServiceToCart(svc);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnAdd);
            }
        });

        tableServices.setItems(svcCatalog);
    }

    private void configureCartTable() {
        colCartName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItemName()));
        colCartQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colCartSubtotal.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getSubtotal())));

        colCartAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnRemove = new Button("✕");
            {
                btnRemove.setStyle("-fx-text-fill: #cf222e; -fx-font-weight: bold; -fx-background-color: transparent; -fx-cursor: hand;");
                btnRemove.setOnAction(e -> {
                    InvoiceDetail item = getTableRow().getItem();
                    if (item != null) {
                        cartList.remove(item);
                        recalculate();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnRemove);
            }
        });

        tableCart.setItems(cartList);

        TableMenuUtil.attach(tableCart,
                new TableMenuUtil.Item<InvoiceDetail>("➕ Tăng số lượng", this::onCartIncrease),
                new TableMenuUtil.Item<InvoiceDetail>("➖ Giảm số lượng", this::onCartDecrease),
                new TableMenuUtil.Item<InvoiceDetail>("🗑 Xóa khỏi giỏ", d -> {
                    cartList.remove(d);
                    recalculate();
                }, true));
    }

    private void initPaymentControls() {
        ToggleGroup tg = new ToggleGroup();
        rbCash.setToggleGroup(tg);
        rbTransfer.setToggleGroup(tg);
        rbCash.setSelected(true);

        txtDiscount.textProperty().addListener((obs, old, val) -> recalculate());
    }

    private List<Pet> rawPets = List.of();
    private List<Accessory> rawAccs = List.of();

    private void loadCatalogs() {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                rawPets = petService.getAvailable();
                rawAccs = accessoryService.getActive();
                List<Service> svcs = serviceService.getActive();
                List<Customer> custs = customerService.getAll();

                Platform.runLater(() -> {
                    petCatalog.setAll(rawPets);
                    accCatalog.setAll(rawAccs);
                    svcCatalog.setAll(svcs);
                    cmbCustomer.setItems(FXCollections.observableArrayList(custs));
                    cmbCustomer.setCellFactory(lv -> new ListCell<>() {
                        @Override
                        protected void updateItem(Customer item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? "" : item.getFullName() + " - " + item.getPhone());
                        }
                    });
                    cmbCustomer.setButtonCell(new ListCell<>() {
                        @Override
                        protected void updateItem(Customer item, boolean empty) {
                            super.updateItem(item, empty);
                            setText(empty || item == null ? "" : item.getFullName() + " (" + item.getPhone() + ")");
                        }
                    });
                });
                return null;
            }
        };
        new Thread(task).start();
    }

    private void filterPets() {
        String kw = txtSearchPet.getText();
        if (kw == null || kw.isBlank()) {
            petCatalog.setAll(rawPets);
        } else {
            String lower = kw.toLowerCase().trim();
            petCatalog.setAll(rawPets.stream()
                    .filter(p -> p.getBreed().toLowerCase().contains(lower) || (p.getName() != null && p.getName().toLowerCase().contains(lower)) || (p.getPetCode() != null && p.getPetCode().toLowerCase().contains(lower)))
                    .toList());
        }
    }

    private void filterAccessories() {
        String kw = txtSearchAcc.getText();
        if (kw == null || kw.isBlank()) {
            accCatalog.setAll(rawAccs);
        } else {
            String lower = kw.toLowerCase().trim();
            accCatalog.setAll(rawAccs.stream()
                    .filter(a -> a.getName().toLowerCase().contains(lower))
                    .toList());
        }
    }

    private void addPetToCart(Pet pet) {
        for (InvoiceDetail d : cartList) {
            if ("PET".equalsIgnoreCase(d.getItemType()) && d.getItemId() == pet.getId()) {
                AlertUtil.showWarning("Đã có trong giỏ", "Thú cưng này đã được thêm vào giỏ hàng.");
                return;
            }
        }
        cartList.add(InvoiceDetail.forPet(pet));
        recalculate();
    }

    private void onCartIncrease(InvoiceDetail d) {
        if ("PET".equalsIgnoreCase(d.getItemType())) {
            com.petshop.util.AlertUtil.showWarning("Thú cưng", "Mỗi thú cưng chỉ bán được 1 con trong một hóa đơn.");
            return;
        }
        if ("ACCESSORY".equalsIgnoreCase(d.getItemType())) {
            Accessory acc = accCatalog.stream().filter(a -> a.getId() == d.getItemId()).findFirst().orElse(null);
            if (acc != null && d.getQuantity() + 1 > acc.getSellableQuantity()) {
                showStockLimit(acc);
                return;
            }
        }
        d.setQuantity(d.getQuantity() + 1);
        tableCart.refresh();
        recalculate();
    }

    private void onCartDecrease(InvoiceDetail d) {
        if (d.getQuantity() > 1) {
            d.setQuantity(d.getQuantity() - 1);
            tableCart.refresh();
            recalculate();
        }
    }

    private void showStockLimit(Accessory acc) {
        com.petshop.util.AlertUtil.showWarning("Giới hạn tồn kho",
                "'" + acc.getName() + "' chỉ bán được tối đa " + acc.getSellableQuantity()
                        + " (kho còn " + acc.getQuantity() + ", phải giữ lại tối thiểu "
                        + acc.getMinQuantity() + ").");
    }

    private void addAccessoryToCart(Accessory acc) {
        for (InvoiceDetail d : cartList) {
            if ("ACCESSORY".equalsIgnoreCase(d.getItemType()) && d.getItemId() == acc.getId()) {
                if (d.getQuantity() + 1 > acc.getSellableQuantity()) {
                    showStockLimit(acc);
                    return;
                }
                d.setQuantity(d.getQuantity() + 1);
                tableCart.refresh();
                recalculate();
                return;
            }
        }
        if (acc.getSellableQuantity() <= 0) {
            showStockLimit(acc);
            return;
        }
        cartList.add(InvoiceDetail.forAccessory(acc, 1));
        recalculate();
    }

    private void addServiceToCart(Service svc) {
        for (InvoiceDetail d : cartList) {
            if ("SERVICE".equalsIgnoreCase(d.getItemType()) && d.getItemId() == svc.getId()) {
                d.setQuantity(d.getQuantity() + 1);
                tableCart.refresh();
                recalculate();
                return;
            }
        }
        cartList.add(InvoiceDetail.forService(svc, 1));
        recalculate();
    }

    private void recalculate() {
        currentSubtotal = 0;
        for (InvoiceDetail d : cartList) {
            currentSubtotal += d.getSubtotal();
        }

        long discount = 0;
        try {
            if (txtDiscount.getText() != null && !txtDiscount.getText().isBlank()) {
                discount = Long.parseLong(txtDiscount.getText().replaceAll("[^0-9]", ""));
            }
        } catch (Exception ignored) {}

        currentTotal = Math.max(0, currentSubtotal - discount);

        lblSubtotal.setText(CurrencyUtil.format(currentSubtotal));
        lblTotalAmount.setText(CurrencyUtil.format(currentTotal));
    }

    @FXML
    public void onClearCart() {
        cartList.clear();
        recalculate();
    }

    @FXML
    public void onQuickAddCustomer() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Thêm nhanh khách hàng");
        dialog.setHeaderText("Nhập Tên và Số điện thoại khách hàng");
        dialog.setContentText("Họ tên:");

        Optional<String> res = dialog.showAndWait();
        if (res.isPresent() && !res.get().isBlank()) {
            String name = res.get().trim();
            TextInputDialog phoneDialog = new TextInputDialog();
            phoneDialog.setTitle("Thêm nhanh khách hàng");
            phoneDialog.setHeaderText("Khách hàng: " + name);
            phoneDialog.setContentText("Số điện thoại:");

            Optional<String> phoneRes = phoneDialog.showAndWait();
            if (phoneRes.isPresent() && !phoneRes.get().isBlank()) {
                try {
                    Customer c = new Customer(0, name, phoneRes.get().trim(), "", "", "", java.time.LocalDateTime.now());
                    customerService.add(c);
                    cmbCustomer.getItems().add(c);
                    cmbCustomer.setValue(c);
                } catch (Exception ex) {
                    AlertUtil.showError("Lỗi", "Không thể thêm khách hàng: " + ex.getMessage());
                }
            }
        }
    }

    @FXML
    public void onCheckout() {
        if (cartList.isEmpty()) {
            AlertUtil.showWarning("Giỏ hàng trống", "Vui lòng chọn ít nhất một mặt hàng hoặc dịch vụ.");
            return;
        }

        Invoice invoice = new Invoice();
        Customer cust = cmbCustomer.getValue();
        if (cust != null) {
            invoice.setCustomerId(cust.getId());
            invoice.setCustomerName(cust.getFullName());
            invoice.setCustomerPhone(cust.getPhone());
        } else {
            invoice.setCustomerName("Khách lẻ");
        }

        long discount = 0;
        try {
            discount = Long.parseLong(txtDiscount.getText().replaceAll("[^0-9]", ""));
        } catch (Exception ignored) {}

        invoice.setSubtotal(currentSubtotal);
        invoice.setDiscount(Math.min(discount, currentSubtotal));
        invoice.setTotalAmount(currentTotal);
        invoice.setPaymentMethod(rbTransfer.isSelected() ? "TRANSFER" : "CASH");
        invoice.setStatus("PAID");
        invoice.setNote(txtNote.getText());
        invoice.setDetails(new java.util.ArrayList<>(cartList));

        btnCheckout.setDisable(true);
        Task<Invoice> task = new Task<>() {
            @Override
            protected Invoice call() throws Exception {
                return invoiceService.createInvoice(invoice);
            }
        };

        task.setOnSucceeded(e -> {
            btnCheckout.setDisable(false);
            Invoice created = task.getValue();

            boolean print = AlertUtil.showConfirm("Thanh toán thành công!",
                    "Hóa đơn " + created.getInvoiceCode() + " đã được tạo thành công.\n"
                    + "Tổng tiền: " + CurrencyUtil.format(created.getTotalAmount()) + "\n\n"
                    + "Bạn có muốn in / xuất hóa đơn PDF không?");

            if (print) {
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Lưu hóa đơn PDF");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
                chooser.setInitialFileName("Hoa_don_" + created.getInvoiceCode() + ".pdf");
                File file = chooser.showSaveDialog(App.getPrimaryStage());
                if (file != null) {
                    try {
                        PdfExporter.exportInvoice(created, file);
                        AlertUtil.showInfo("Thành công", "Đã lưu hóa đơn PDF tại:\n" + file.getAbsolutePath());
                    } catch (Exception ex) {
                        AlertUtil.showException("Lỗi in PDF", ex);
                    }
                }
            }

            onClearCart();
            loadCatalogs(); // Refresh tồn kho
        });

        task.setOnFailed(e -> {
            btnCheckout.setDisable(false);
            Throwable ex = task.getException();
            AlertUtil.showError("Thanh toán thất bại", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    @FXML
    public void onBack() {
        try {
            App.navigateTo("Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
