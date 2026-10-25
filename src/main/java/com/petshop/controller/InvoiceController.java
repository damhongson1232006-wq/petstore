package com.petshop.controller;

import com.petshop.App;
import com.petshop.model.Invoice;
import com.petshop.model.InvoiceDetail;
import com.petshop.service.InvoiceService;
import com.petshop.util.AlertUtil;
import com.petshop.util.TableMenuUtil;
import com.petshop.util.CurrencyUtil;
import com.petshop.util.PdfExporter;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller cho màn hình Quản lý danh sách Hóa đơn.
 */
public class InvoiceController {

    @FXML private Button btnNewInvoice;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private TextField txtSearch;
    @FXML private Button btnRefresh;

    @FXML private TableView<Invoice> tableInvoices;
    @FXML private TableColumn<Invoice, String> colCode;
    @FXML private TableColumn<Invoice, String> colDate;
    @FXML private TableColumn<Invoice, String> colCustomer;
    @FXML private TableColumn<Invoice, String> colPhone;
    @FXML private TableColumn<Invoice, String> colSubtotal;
    @FXML private TableColumn<Invoice, String> colDiscount;
    @FXML private TableColumn<Invoice, String> colTotal;
    @FXML private TableColumn<Invoice, String> colPayment;
    @FXML private TableColumn<Invoice, String> colStaff;
    @FXML private TableColumn<Invoice, String> colStatus;
    @FXML private TableColumn<Invoice, Void> colAction;

    @FXML private Label lblStatus;
    @FXML private Label lblTotalRevenue;

    private final InvoiceService invoiceService = new InvoiceService();
    private final ObservableList<Invoice> invoiceList = FXCollections.observableArrayList();
    private List<Invoice> allInvoices = List.of();

    @FXML
    public void initialize() {
        dpFrom.setValue(LocalDate.now().minusDays(30));
        dpTo.setValue(LocalDate.now());

        configureTable();
        txtSearch.textProperty().addListener((obs, old, val) -> filterData());
        loadData();
    }

    private void configureTable() {
        colCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceCode()));
        colDate.setCellValueFactory(c -> {
            var dt = c.getValue().getInvoiceDate();
            return new SimpleStringProperty(dt != null ? dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
        });
        colCustomer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerName() != null ? c.getValue().getCustomerName() : "Khách lẻ"));
        colPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerPhone() != null ? c.getValue().getCustomerPhone() : ""));
        colSubtotal.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getSubtotal())));
        colDiscount.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getDiscount())));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(CurrencyUtil.format(c.getValue().getTotalAmount())));
        colPayment.setCellValueFactory(c -> new SimpleStringProperty("TRANSFER".equalsIgnoreCase(c.getValue().getPaymentMethod()) ? "CK / QR" : "Tiền mặt"));
        colStaff.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedByName() != null ? c.getValue().getCreatedByName() : "Admin"));

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    return;
                }
                Invoice inv = getTableRow().getItem();
                Label badge = new Label("PAID".equalsIgnoreCase(inv.getStatus()) ? "Đã TT" : "Đã hủy");
                badge.getStyleClass().add("badge");
                badge.getStyleClass().add("PAID".equalsIgnoreCase(inv.getStatus()) ? "badge-success" : "badge-danger");
                setGraphic(badge);
                setAlignment(Pos.CENTER);
            }
        });

        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button btnPrint = new Button("📄 In");
            private final Button btnCancel = new Button("✕ Hủy");
            private final HBox pane = new HBox(6, btnPrint, btnCancel);

            {
                btnPrint.getStyleClass().add("btn-outline");
                btnCancel.getStyleClass().add("btn-danger");
                pane.setAlignment(Pos.CENTER);

                btnPrint.setOnAction(e -> {
                    Invoice inv = getTableRow().getItem();
                    if (inv != null) onPrintInvoice(inv);
                });

                btnCancel.setOnAction(e -> {
                    Invoice inv = getTableRow().getItem();
                    if (inv != null) onCancelInvoice(inv);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    Invoice inv = getTableRow().getItem();
                    btnCancel.setDisable("CANCELLED".equalsIgnoreCase(inv.getStatus()));
                    setGraphic(pane);
                }
            }
        });

        tableInvoices.setItems(invoiceList);

        TableMenuUtil.attach(tableInvoices,
                new TableMenuUtil.Item<Invoice>("🔍 Xem chi tiết", this::onViewInvoice),
                new TableMenuUtil.Item<Invoice>("🖨 In hóa đơn (PDF)", this::onPrintInvoice),
                new TableMenuUtil.Item<Invoice>("❌ Hủy hóa đơn", this::onCancelInvoice, true));
    }

    public void loadData() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        Task<List<Invoice>> task = new Task<>() {
            @Override
            protected List<Invoice> call() throws Exception {
                if (from != null && to != null) {
                    return invoiceService.getByDateRange(from, to);
                } else {
                    return invoiceService.getAll();
                }
            }
        };

        task.setOnSucceeded(e -> {
            allInvoices = task.getValue();
            filterData();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải hóa đơn", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    private void filterData() {
        String kw = txtSearch.getText();
        long totalRevenue = 0;

        List<Invoice> filtered = allInvoices.stream()
                .filter(i -> {
                    if (kw == null || kw.isBlank()) return true;
                    String lower = kw.toLowerCase().trim();
                    return i.getInvoiceCode().toLowerCase().contains(lower)
                            || (i.getCustomerName() != null && i.getCustomerName().toLowerCase().contains(lower))
                            || (i.getCustomerPhone() != null && i.getCustomerPhone().contains(lower));
                })
                .toList();

        for (Invoice i : filtered) {
            if ("PAID".equalsIgnoreCase(i.getStatus())) {
                totalRevenue += i.getTotalAmount();
            }
        }

        invoiceList.setAll(filtered);
        lblStatus.setText("Tổng: " + invoiceList.size() + " hóa đơn");
        lblTotalRevenue.setText("Tổng doanh thu: " + CurrencyUtil.format(totalRevenue));
    }

    @FXML
    public void onRefresh() {
        txtSearch.clear();
        dpFrom.setValue(LocalDate.now().minusDays(30));
        dpTo.setValue(LocalDate.now());
        loadData();
    }

    @FXML
    public void onNewInvoice() {
        try {
            App.navigateTo("InvoiceForm");
        } catch (Exception e) {
            AlertUtil.showException("Không thể mở màn hình Bán hàng", e);
        }
    }

    private void onViewInvoice(Invoice inv) {
        Task<Optional<Invoice>> task = new Task<>() {
            @Override
            protected Optional<Invoice> call() throws Exception {
                return invoiceService.getById(inv.getId());
            }
        };
        task.setOnSucceeded(e -> {
            Optional<Invoice> opt = task.getValue();
            if (opt.isEmpty()) {
                return;
            }
            Invoice full = opt.get();
            StringBuilder sb = new StringBuilder();
            sb.append("Mã hóa đơn: ").append(full.getInvoiceCode()).append("\n");
            if (full.getInvoiceDate() != null) {
                sb.append("Ngày: ").append(full.getInvoiceDate()
                        .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            }
            sb.append("Khách hàng: ").append(full.getCustomerName() != null ? full.getCustomerName() : "Khách lẻ").append("\n");
            sb.append("Thanh toán: ").append("TRANSFER".equalsIgnoreCase(full.getPaymentMethod()) ? "Chuyển khoản" : "Tiền mặt").append("\n");
            sb.append("Trạng thái: ").append("CANCELLED".equalsIgnoreCase(full.getStatus()) ? "Đã hủy" : "Đã thanh toán").append("\n\n");
            sb.append("CHI TIẾT:\n");
            if (full.getDetails() != null) {
                int i = 1;
                for (com.petshop.model.InvoiceDetail d : full.getDetails()) {
                    sb.append(i++).append(". ").append(d.getItemName())
                            .append(" | SL: ").append(d.getQuantity())
                            .append(" | Đơn giá: ").append(CurrencyUtil.format(d.getUnitPrice()))
                            .append(" | Thành tiền: ").append(CurrencyUtil.format(d.getSubtotal())).append("\n");
                }
            }
            sb.append("\nTạm tính: ").append(CurrencyUtil.format(full.getSubtotal())).append("\n");
            sb.append("Giảm giá: ").append(CurrencyUtil.format(full.getDiscount())).append("\n");
            sb.append("TỔNG CỘNG: ").append(CurrencyUtil.format(full.getTotalAmount()));
            AlertUtil.showInfo("Chi tiết hóa đơn " + full.getInvoiceCode(), sb.toString());
        });
        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi xem chi tiết", ex != null ? ex.getMessage() : "");
        });
        new Thread(task).start();
    }

    private void onPrintInvoice(Invoice inv) {
        Task<Optional<Invoice>> task = new Task<>() {
            @Override
            protected Optional<Invoice> call() throws Exception {
                return invoiceService.getById(inv.getId());
            }
        };

        task.setOnSucceeded(e -> {
            Optional<Invoice> fullOpt = task.getValue();
            if (fullOpt.isPresent()) {
                Invoice full = fullOpt.get();
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Lưu hóa đơn PDF");
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files (*.pdf)", "*.pdf"));
                chooser.setInitialFileName("Hoa_don_" + full.getInvoiceCode() + ".pdf");
                File file = chooser.showSaveDialog(App.getPrimaryStage());
                if (file != null) {
                    try {
                        PdfExporter.exportInvoice(full, file);
                        AlertUtil.showInfo("Thành công", "Đã xuất hóa đơn PDF:\n" + file.getAbsolutePath());
                    } catch (Exception ex) {
                        AlertUtil.showException("Lỗi in PDF", ex);
                    }
                }
            }
        });

        new Thread(task).start();
    }

    private void onCancelInvoice(Invoice inv) {
        if (!AlertUtil.showConfirm("Xác nhận hủy hóa đơn",
                "Bạn có chắc muốn hủy hóa đơn " + inv.getInvoiceCode() + "?\n"
                + "Thú cưng trong hóa đơn sẽ được hoàn trả trạng thái 'AVAILABLE' và phụ kiện sẽ được cộng lại kho.")) {
            return;
        }

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                invoiceService.cancelInvoice(inv.getId());
                return null;
            }
        };

        task.setOnSucceeded(e -> {
            AlertUtil.showInfo("Thành công", "Đã hủy hóa đơn và hoàn trả tồn kho.");
            loadData();
        });

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Không thể hủy hóa đơn", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }
}
