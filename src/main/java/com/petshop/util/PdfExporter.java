package com.petshop.util;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.BorderRadius;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.petshop.constant.AppConstants;
import com.petshop.model.Invoice;
import com.petshop.model.InvoiceDetail;

import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích in / xuất Hóa đơn bán hàng ra định dạng PDF chuẩn đẹp bằng iText 7.
 */
public class PdfExporter {

    public static void exportInvoice(Invoice invoice, File file) throws Exception {
        try (PdfWriter writer = new PdfWriter(new FileOutputStream(file));
             PdfDocument pdf = new PdfDocument(writer);
             Document document = new Document(pdf, PageSize.A5)) {
            document.setFont(loadVietnameseFont());

            document.setMargins(20, 25, 20, 25);

            // Store Info Header
            Paragraph header = new Paragraph("CỬA HÀNG THÚ CƯNG PET SHOP")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(new DeviceRgb(9, 105, 218));
            document.add(header);

            Paragraph subHeader = new Paragraph("Địa chỉ: 123 Đường Thú Cưng, TP. Hà Nội\nHotline: 0988 888 888 | Website: petshop.vn")
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY);
            document.add(subHeader);

            document.add(new Paragraph("--------------------------------------------------------------------------------")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.LIGHT_GRAY));

            // Invoice Title
            Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Invoice Info Table
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
            infoTable.setBorder(null);

            String code = invoice.getInvoiceCode() != null ? invoice.getInvoiceCode() : "";
            String date = invoice.getInvoiceDate() != null
                    ? invoice.getInvoiceDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    : "";
            String cust = invoice.getCustomerName() != null ? invoice.getCustomerName() : "Khách lẻ";
            String phone = invoice.getCustomerPhone() != null ? invoice.getCustomerPhone() : "---";

            infoTable.addCell(new Cell().add(new Paragraph("Số HĐ: " + code)).setBorder(null).setFontSize(9));
            infoTable.addCell(new Cell().add(new Paragraph("Ngày: " + date)).setBorder(null).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));
            infoTable.addCell(new Cell().add(new Paragraph("Khách hàng: " + cust)).setBorder(null).setFontSize(9));
            infoTable.addCell(new Cell().add(new Paragraph("SĐT: " + phone)).setBorder(null).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));

            document.add(infoTable);
            document.add(new Paragraph("\n"));

            // Items Table
            Table itemTable = new Table(UnitValue.createPercentArray(new float[]{10, 45, 15, 30})).useAllAvailableWidth();
            DeviceRgb thBg = new DeviceRgb(240, 244, 248);

            itemTable.addHeaderCell(new Cell().add(new Paragraph("STT").setBold()).setBackgroundColor(thBg).setFontSize(9).setTextAlignment(TextAlignment.CENTER));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Tên mặt hàng / Dịch vụ").setBold()).setBackgroundColor(thBg).setFontSize(9));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("SL").setBold()).setBackgroundColor(thBg).setFontSize(9).setTextAlignment(TextAlignment.CENTER));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Thành tiền").setBold()).setBackgroundColor(thBg).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));

            int stt = 1;
            if (invoice.getDetails() != null) {
                for (InvoiceDetail d : invoice.getDetails()) {
                    itemTable.addCell(new Cell().add(new Paragraph(String.valueOf(stt++))).setFontSize(9).setTextAlignment(TextAlignment.CENTER));
                    itemTable.addCell(new Cell().add(new Paragraph(d.getItemName())).setFontSize(9));
                    itemTable.addCell(new Cell().add(new Paragraph(String.valueOf(d.getQuantity()))).setFontSize(9).setTextAlignment(TextAlignment.CENTER));
                    itemTable.addCell(new Cell().add(new Paragraph(CurrencyUtil.format(d.getSubtotal()))).setFontSize(9).setTextAlignment(TextAlignment.RIGHT));
                }
            }
            document.add(itemTable);
            document.add(new Paragraph("\n"));

            // Summary Table
            Table summaryTable = new Table(UnitValue.createPercentArray(new float[]{60, 40})).useAllAvailableWidth();
            summaryTable.setBorder(null);

            summaryTable.addCell(new Cell().add(new Paragraph("Tổng tiền hàng:")).setBorder(null).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
            summaryTable.addCell(new Cell().add(new Paragraph(CurrencyUtil.format(invoice.getSubtotal()))).setBorder(null).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));

            if (invoice.getDiscount() > 0) {
                summaryTable.addCell(new Cell().add(new Paragraph("Giảm giá:")).setBorder(null).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
                summaryTable.addCell(new Cell().add(new Paragraph("-" + CurrencyUtil.format(invoice.getDiscount()))).setBorder(null).setFontSize(10).setTextAlignment(TextAlignment.RIGHT));
            }

            summaryTable.addCell(new Cell().add(new Paragraph("THANH TOÁN:").setBold()).setBorder(null).setFontSize(12).setTextAlignment(TextAlignment.RIGHT));
            summaryTable.addCell(new Cell().add(new Paragraph(CurrencyUtil.format(invoice.getTotalAmount())).setBold().setFontColor(new DeviceRgb(9, 105, 218))).setBorder(null).setFontSize(12).setTextAlignment(TextAlignment.RIGHT));

            String pMethod = "TRANSFER".equalsIgnoreCase(invoice.getPaymentMethod()) ? "Chuyển khoản / QR" : "Tiền mặt";
            summaryTable.addCell(new Cell().add(new Paragraph("Phương thức TT: " + pMethod)).setBorder(null).setFontSize(9));
            summaryTable.addCell(new Cell().add(new Paragraph("")).setBorder(null));

            document.add(summaryTable);

            // Footer notes
            Paragraph footer = new Paragraph("\nCảm ơn quý khách và hẹn gặp lại!\n(Vui lòng giữ lại hóa đơn để được hỗ trợ bảo hành / đổi trả trong 7 ngày)")
                    .setFontSize(8)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY);
            document.add(footer);
        }
    }

    /** Nạp font DejaVu Sans (hỗ trợ đầy đủ tiếng Việt) từ resources và nhúng vào PDF. */
    private static PdfFont loadVietnameseFont() throws java.io.IOException {
        try (java.io.InputStream is = PdfExporter.class.getResourceAsStream("/fonts/DejaVuSans.ttf")) {
            if (is == null) {
                throw new java.io.IOException("Không tìm thấy font /fonts/DejaVuSans.ttf trong resources.");
            }
            com.itextpdf.io.font.FontProgram fp = com.itextpdf.io.font.FontProgramFactory.createFont(is.readAllBytes());
            return PdfFontFactory.createFont(fp, com.itextpdf.io.font.PdfEncodings.IDENTITY_H);
        }
    }
}
