package com.petshop.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Tiện ích sinh mã tự động (hóa đơn, phiếu nhập) dựa vào dữ liệu trong CSDL.
 * Thread-safe nhờ sử dụng từ khóa synchronized.
 *
 * <p>Quy tắc sinh mã:</p>
 * <ul>
 *   <li>Hóa đơn: {@code HD} + yyyyMMdd + 3 chữ số thứ tự trong ngày (ví dụ: {@code HD20241024001})</li>
 *   <li>Phiếu nhập: {@code PN} + yyyyMMdd + 3 chữ số thứ tự trong ngày (ví dụ: {@code PN20241024001})</li>
 * </ul>
 */
public class CodeGenerator {

    /** Định dạng ngày dùng trong mã: năm tháng ngày liền */
    private static final DateTimeFormatter CODE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /** Lớp tiện ích – không cho phép khởi tạo */
    private CodeGenerator() {
        throw new UnsupportedOperationException("CodeGenerator là lớp tiện ích, không thể khởi tạo.");
    }

    /**
     * Sinh mã hóa đơn mới dạng {@code HD} + yyyyMMdd + 3 chữ số thứ tự.
     * Truy vấn CSDL để tìm mã lớn nhất trong ngày hôm nay rồi tăng thêm 1.
     *
     * @param conn kết nối CSDL đang hoạt động (không được null)
     * @return mã hóa đơn mới, ví dụ {@code "HD20241024001"}
     * @throws SQLException nếu có lỗi truy vấn CSDL
     */
    public static synchronized String generateInvoiceCode(Connection conn) throws SQLException {
        return generateCode(conn, "HD", "Invoices", "invoice_code");
    }

    /**
     * Sinh mã phiếu nhập mới dạng {@code PN} + yyyyMMdd + 3 chữ số thứ tự.
     * Truy vấn CSDL để tìm mã lớn nhất trong ngày hôm nay rồi tăng thêm 1.
     *
     * @param conn kết nối CSDL đang hoạt động (không được null)
     * @return mã phiếu nhập mới, ví dụ {@code "PN20241024001"}
     * @throws SQLException nếu có lỗi truy vấn CSDL
     */
    public static synchronized String generateImportCode(Connection conn) throws SQLException {
        return generateCode(conn, "PN", "ImportOrders", "order_code");
    }

    /**
     * Sinh mã thú cưng mới dạng {@code TC} + 6 chữ số tăng dần (ví dụ {@code TC000012}).
     * Mã liền sau mã lớn nhất hiện có trong bảng Pets.
     */
    public static synchronized String generatePetCode(Connection conn) throws SQLException {
        String sql = "SELECT TOP 1 pet_code FROM Pets WHERE pet_code LIKE 'TC%' "
                + "ORDER BY LEN(pet_code) DESC, pet_code DESC";
        int next = 1;
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next() && rs.getString(1) != null) {
                try {
                    next = Integer.parseInt(rs.getString(1).substring(2)) + 1;
                } catch (NumberFormatException e) {
                    next = 1;
                }
            }
        }
        return String.format("TC%06d", next);
    }

    /** Sinh mã giao dịch kho ngắn, đủ duy nhất cho nhật ký tồn kho. */
    public static String generateInventoryTransactionCode() {
        return "TX" + java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
                .format(java.time.LocalDateTime.now()) + "-"
                + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    /**
     * Hàm sinh mã nội bộ dùng chung cho hóa đơn và phiếu nhập.
     *
     * @param conn        kết nối CSDL
     * @param prefix      tiền tố mã ("HD" hoặc "PN")
     * @param tableName   tên bảng chứa mã (ví dụ: "HoaDon", "PhieuNhap")
     * @param columnName  tên cột chứa mã (ví dụ: "MaHoaDon", "MaPhieuNhap")
     * @return mã mới theo định dạng {@code prefix + yyyyMMdd + 3-digit-seq}
     * @throws SQLException nếu có lỗi truy vấn CSDL
     */
    private static String generateCode(Connection conn,
                                        String prefix,
                                        String tableName,
                                        String columnName) throws SQLException {
        String today = LocalDate.now().format(CODE_DATE_FORMAT);
        String todayPrefix = prefix + today; // ví dụ: HD20241024

        // Truy vấn mã lớn nhất trong ngày hôm nay
        String sql = "SELECT TOP 1 " + columnName + " FROM " + tableName
                + " WHERE " + columnName + " LIKE ?"
                + " ORDER BY LEN(" + columnName + ") DESC, " + columnName + " DESC";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, todayPrefix + "%");

            try (ResultSet rs = ps.executeQuery()) {
                int nextSeq = 1;
                if (rs.next() && rs.getString(1) != null) {
                    String maxCode = rs.getString(1);
                    // Lấy phần số thứ tự (3 ký tự cuối) và tăng thêm 1
                    String seqPart = maxCode.substring(todayPrefix.length());
                    try {
                        nextSeq = Integer.parseInt(seqPart) + 1;
                    } catch (NumberFormatException e) {
                        // Nếu không parse được, bắt đầu lại từ 1
                        nextSeq = 1;
                    }
                }
                // Định dạng số thứ tự thành 3 chữ số (001, 002, ...)
                return String.format("%s%03d", todayPrefix, nextSeq);
            }
        }
    }
}
