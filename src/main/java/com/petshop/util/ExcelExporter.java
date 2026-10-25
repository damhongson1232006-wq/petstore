package com.petshop.util;

import com.petshop.model.Accessory;
import com.petshop.model.Pet;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Tiện ích xuất dữ liệu ra file Microsoft Excel (.xlsx) sử dụng Apache POI.
 */
public class ExcelExporter {

    /**
     * Xuất danh sách thú cưng ra file Excel.
     */
    public static void exportPets(List<Pet> pets, File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Danh sách Thú Cưng");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            // Currency Style
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("#,##0"));

            // Create Headers
            String[] headers = {"STT", "Mã thú", "Tên", "Loài", "Giống", "Giới tính", "Tuổi", "Màu sắc", "Giá nhập", "Giá bán", "Trạng thái", "Ngày nhập"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate Data
            int rowIdx = 1;
            for (Pet p : pets) {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowIdx);
                row.createCell(1).setCellValue(p.getPetCode() != null ? p.getPetCode() : String.valueOf(p.getId()));
                row.createCell(2).setCellValue(p.getName() != null ? p.getName() : "");
                row.createCell(3).setCellValue(p.getSpecies());
                row.createCell(4).setCellValue(p.getBreed());
                row.createCell(5).setCellValue("MALE".equalsIgnoreCase(p.getGender()) ? "Đực" : "FEMALE".equalsIgnoreCase(p.getGender()) ? "Cái" : "Chưa rõ");
                row.createCell(6).setCellValue(p.getAgeDisplay());
                row.createCell(7).setCellValue(p.getColor() != null ? p.getColor() : "");

                Cell cImp = row.createCell(8);
                cImp.setCellValue(p.getImportPrice());
                cImp.setCellStyle(currencyStyle);

                Cell cSell = row.createCell(9);
                cSell.setCellValue(p.getSellPrice());
                cSell.setCellStyle(currencyStyle);

                String st = switch (p.getStatus() != null ? p.getStatus().toUpperCase() : "") {
                    case "AVAILABLE" -> "Đang có sẵn";
                    case "SOLD" -> "Đã bán";
                    case "RESERVED" -> "Đã đặt trước";
                    default -> p.getStatus();
                };
                row.createCell(10).setCellValue(st);
                row.createCell(11).setCellValue(p.getImportDate() != null ? p.getImportDate().toString() : "");

                rowIdx++;
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }

    /**
     * Xuất báo cáo doanh thu theo ngày.
     */
    public static void exportRevenue(List<Map<String, Object>> records, File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Doanh Thu Theo Ngày");

            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            font.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.TEAL.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0"));

            String[] headers = {"STT", "Ngày", "Số lượng hóa đơn", "Tổng doanh thu (VNĐ)"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            long grandTotal = 0;
            for (Map<String, Object> r : records) {
                Row row = sheet.createRow(rowIdx);
                row.createCell(0).setCellValue(rowIdx);
                row.createCell(1).setCellValue(r.get("date") != null ? r.get("date").toString() : "");
                row.createCell(2).setCellValue(r.get("count") != null ? ((Number) r.get("count")).intValue() : 0);

                long rev = r.get("revenue") != null ? ((Number) r.get("revenue")).longValue() : 0L;
                grandTotal += rev;
                Cell cRev = row.createCell(3);
                cRev.setCellValue(rev);
                cRev.setCellStyle(currencyStyle);

                rowIdx++;
            }

            // Total row
            Row totalRow = sheet.createRow(rowIdx);
            totalRow.createCell(1).setCellValue("TỔNG CỘNG");
            Cell cTot = totalRow.createCell(3);
            cTot.setCellValue(grandTotal);
            cTot.setCellStyle(currencyStyle);

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        }
    }
}
