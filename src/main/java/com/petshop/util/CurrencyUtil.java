package com.petshop.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Tiện ích định dạng và phân tích tiền tệ Việt Nam (VNĐ).
 * Không sử dụng số thập phân; đơn vị là đồng nguyên.
 */
public class CurrencyUtil {

    /** Ký hiệu phân cách hàng nghìn theo quy chuẩn Việt Nam (dấu chấm) */
    private static final DecimalFormatSymbols VN_SYMBOLS;

    /** Định dạng số với dấu chấm phân nghìn */
    private static final DecimalFormat THOUSANDS_FORMAT;

    static {
        VN_SYMBOLS = new DecimalFormatSymbols(new Locale("vi", "VN"));
        VN_SYMBOLS.setGroupingSeparator('.');
        VN_SYMBOLS.setDecimalSeparator(',');

        THOUSANDS_FORMAT = new DecimalFormat("#,###", VN_SYMBOLS);
    }

    /** Lớp tiện ích – không cho phép khởi tạo */
    private CurrencyUtil() {
        throw new UnsupportedOperationException("CurrencyUtil là lớp tiện ích, không thể khởi tạo.");
    }

    /**
     * Định dạng số tiền sang chuỗi hiển thị VNĐ.
     * Ví dụ: {@code 1500000} → {@code "1.500.000 đ"}
     *
     * @param amount số tiền (đơn vị: đồng)
     * @return chuỗi đã định dạng kèm ký hiệu "đ"
     */
    public static String format(long amount) {
        return THOUSANDS_FORMAT.format(amount) + " đ";
    }

    /**
     * Phân tích chuỗi tiền tệ đã định dạng về giá trị long.
     * Chấp nhận: "1.500.000 đ", "1.500.000", "1500000".
     *
     * @param text chuỗi tiền tệ cần phân tích
     * @return giá trị long tương ứng
     * @throws NumberFormatException nếu chuỗi không hợp lệ
     */
    public static long parse(String text) {
        if (text == null || text.isBlank()) {
            throw new NumberFormatException("Chuỗi tiền tệ không được để trống.");
        }
        // Loại bỏ ký hiệu đơn vị và dấu phân cách hàng nghìn
        String cleaned = text
                .replace(" đ", "")
                .replace("đ", "")
                .replace(".", "")
                .replace(",", "")
                .trim();
        return Long.parseLong(cleaned);
    }

    /**
     * Định dạng rút gọn số tiền.
     * <ul>
     *   <li>Số >= 1.000.000: hiển thị dạng triệu, ví dụ {@code 1500000} → {@code "1.5tr"}</li>
     *   <li>Số >= 1.000: hiển thị dạng nghìn, ví dụ {@code 500000} → {@code "500k"}</li>
     *   <li>Số < 1.000: hiển thị nguyên, ví dụ {@code 500} → {@code "500đ"}</li>
     * </ul>
     *
     * @param amount số tiền (đơn vị: đồng)
     * @return chuỗi rút gọn
     */
    public static String formatCompact(long amount) {
        if (amount >= 1_000_000L) {
            double millions = amount / 1_000_000.0;
            // Loại bỏ phần thập phân thừa (ví dụ 2.0tr → 2tr)
            if (millions == Math.floor(millions)) {
                return String.format("%.0ftr", millions);
            }
            return String.format("%.1ftr", millions);
        } else if (amount >= 1_000L) {
            double thousands = amount / 1_000.0;
            if (thousands == Math.floor(thousands)) {
                return String.format("%.0fk", thousands);
            }
            return String.format("%.1fk", thousands);
        } else {
            return amount + "đ";
        }
    }
}
