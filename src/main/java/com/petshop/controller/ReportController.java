package com.petshop.controller;

import com.petshop.App;
import com.petshop.model.Accessory;
import com.petshop.service.ReportService;
import com.petshop.util.AlertUtil;
import com.petshop.util.CurrencyUtil;
import com.petshop.util.ExcelExporter;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Controller cho màn hình Báo cáo Thống kê & Lợi nhuận.
 */
public class ReportController {

    @FXML private Button btnExportExcel;
    @FXML private DatePicker dpFrom;
    @FXML private DatePicker dpTo;
    @FXML private Label lblSummaryRevenue;

    // Tab 1: Daily Revenue
    @FXML private TableView<Map<String, Object>> tableDailyRevenue;
    @FXML private TableColumn<Map<String, Object>, String> colRevDate;
    @FXML private TableColumn<Map<String, Object>, String> colRevCount;
    @FXML private TableColumn<Map<String, Object>, String> colRevTotal;

    // Tab 2: Profit
    @FXML private TableView<Map<String, Object>> tableProfit;
    @FXML private TableColumn<Map<String, Object>, String> colProfitBreed;
    @FXML private TableColumn<Map<String, Object>, String> colProfitCount;
    @FXML private TableColumn<Map<String, Object>, String> colProfitImport;
    @FXML private TableColumn<Map<String, Object>, String> colProfitSell;
    @FXML private TableColumn<Map<String, Object>, String> colProfitNet;

    // Tab 3: Top Breeds
    @FXML private TableView<Map<String, Object>> tableTopBreeds;
    @FXML private TableColumn<Map<String, Object>, String> colTopBreed;
    @FXML private TableColumn<Map<String, Object>, String> colTopCount;
    @FXML private TableColumn<Map<String, Object>, String> colTopSales;

    // Tab 4: Low Stock
    @FXML private TableView<Accessory> tableLowStock;
    @FXML private TableColumn<Accessory, String> colLowName;
    @FXML private TableColumn<Accessory, String> colLowCategory;
    @FXML private TableColumn<Accessory, String> colLowQty;
    @FXML private TableColumn<Accessory, String> colLowMin;

    private final ReportService reportService = new ReportService();

    private final ObservableList<Map<String, Object>> revenueList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> profitList = FXCollections.observableArrayList();
    private final ObservableList<Map<String, Object>> topBreedList = FXCollections.observableArrayList();
    private final ObservableList<Accessory> lowStockList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        dpFrom.setValue(LocalDate.now().minusDays(30));
        dpTo.setValue(LocalDate.now());

        configureTables();
        loadReports();
    }

    private void configureTables() {
        // Tab 1: Revenue
        colRevDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().get("date") != null ? c.getValue().get("date").toString() : ""));
        colRevCount.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("count"))));
        colRevTotal.setCellValueFactory(c -> {
            long rev = c.getValue().get("revenue") != null ? ((Number) c.getValue().get("revenue")).longValue() : 0L;
            return new SimpleStringProperty(CurrencyUtil.format(rev));
        });
        tableDailyRevenue.setItems(revenueList);

        // Tab 2: Profit
        colProfitBreed.setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue().get("breed")));
        colProfitCount.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("soldCount"))));
        colProfitImport.setCellValueFactory(c -> {
            long imp = c.getValue().get("totalImport") != null ? ((Number) c.getValue().get("totalImport")).longValue() : 0L;
            return new SimpleStringProperty(CurrencyUtil.format(imp));
        });
        colProfitSell.setCellValueFactory(c -> {
            long sell = c.getValue().get("totalSell") != null ? ((Number) c.getValue().get("totalSell")).longValue() : 0L;
            return new SimpleStringProperty(CurrencyUtil.format(sell));
        });
        colProfitNet.setCellValueFactory(c -> {
            long profit = c.getValue().get("profit") != null ? ((Number) c.getValue().get("profit")).longValue() : 0L;
            return new SimpleStringProperty(CurrencyUtil.format(profit));
        });
        tableProfit.setItems(profitList);

        // Tab 3: Top Breeds
        colTopBreed.setCellValueFactory(c -> new SimpleStringProperty((String) c.getValue().get("breed")));
        colTopCount.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().get("soldCount"))));
        colTopSales.setCellValueFactory(c -> {
            long sales = c.getValue().get("totalSales") != null ? ((Number) c.getValue().get("totalSales")).longValue() : 0L;
            return new SimpleStringProperty(CurrencyUtil.format(sales));
        });
        tableTopBreeds.setItems(topBreedList);

        // Tab 4: Low Stock
        colLowName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colLowCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colLowQty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getQuantity())));
        colLowMin.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getMinQuantity())));
        tableLowStock.setItems(lowStockList);
    }

    @FXML
    public void loadReports() {
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<Map<String, Object>> rev = reportService.getRevenueReport(from, to);
                List<Map<String, Object>> prof = reportService.getProfitByPetBreed(from, to);
                List<Map<String, Object>> top = reportService.getTopSellingBreeds(from, to, 10);
                List<Accessory> low = reportService.getLowStockAccessories();

                long grandTotalRev = 0;
                for (Map<String, Object> r : rev) {
                    grandTotalRev += ((Number) r.get("revenue")).longValue();
                }
                final long finalTotal = grandTotalRev;

                javafx.application.Platform.runLater(() -> {
                    revenueList.setAll(rev);
                    profitList.setAll(prof);
                    topBreedList.setAll(top);
                    lowStockList.setAll(low);
                    lblSummaryRevenue.setText("Tổng doanh thu: " + CurrencyUtil.format(finalTotal));
                });
                return null;
            }
        };

        task.setOnFailed(e -> {
            Throwable ex = task.getException();
            AlertUtil.showError("Lỗi tải báo cáo", ex != null ? ex.getMessage() : "");
        });

        new Thread(task).start();
    }

    @FXML
    public void onExportExcel() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Lưu Báo cáo Doanh thu Excel");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files (*.xlsx)", "*.xlsx"));
        chooser.setInitialFileName("Bao_cao_doanh_thu_" + dpFrom.getValue() + "_den_" + dpTo.getValue() + ".xlsx");

        File file = chooser.showSaveDialog(App.getPrimaryStage());
        if (file != null) {
            try {
                ExcelExporter.exportRevenue(revenueList, file);
                AlertUtil.showInfo("Thành công", "Đã xuất báo cáo doanh thu ra Excel:\n" + file.getAbsolutePath());
            } catch (Exception ex) {
                AlertUtil.showException("Lỗi xuất Excel", ex);
            }
        }
    }
}
