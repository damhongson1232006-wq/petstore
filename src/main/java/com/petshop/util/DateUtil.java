package com.petshop.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Tiện ích định dạng và chuyển đổi ngày tháng cho ứng dụng Pet Shop.
 * Hỗ trợ định dạng Việt Nam (dd/MM/yyyy) và tích hợp với iText/Excel.
 */
public class DateUtil {

    /** Định dạng ngày: ngày/tháng/năm */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Định dạng ngày giờ: ngày/tháng/năm giờ:phút */
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Lớp tiện ích – không cho phép khởi tạo */
    private DateUtil() {
        throw new UnsupportedOperationException("DateUtil là lớp tiện ích, không thể khởi tạo.");
    }

    /**
     * Định dạng {@link LocalDate} thành chuỗi dd/MM/yyyy.
     *
     * @param date ngày cần định dạng
     * @return chuỗi ngày, ví dụ "24/10/2024"; trả về chuỗi rỗng nếu date là null
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * Định dạng {@link LocalDateTime} thành chuỗi dd/MM/yyyy HH:mm.
     *
     * @param dt ngày giờ cần định dạng
     * @return chuỗi ngày giờ, ví dụ "24/10/2024 15:30"; trả về chuỗi rỗng nếu dt là null
     */
    public static String format(LocalDateTime dt) {
        if (dt == null) {
            return "";
        }
        return dt.format(DATETIME_FORMATTER);
    }

    /**
     * Phân tích chuỗi dd/MM/yyyy thành {@link LocalDate}.
     *
     * @param s chuỗi ngày cần phân tích
     * @return đối tượng LocalDate tương ứng
     * @throws DateTimeParseException  nếu chuỗi không đúng định dạng
     * @throws IllegalArgumentException nếu chuỗi null hoặc rỗng
     */
    public static LocalDate parseDate(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Chuỗi ngày không được để trống.");
        }
        return LocalDate.parse(s.trim(), DATE_FORMATTER);
    }

    /**
     * Phân tích chuỗi dd/MM/yyyy HH:mm thành {@link LocalDateTime}.
     *
     * @param s chuỗi ngày giờ cần phân tích
     * @return đối tượng LocalDateTime tương ứng
     * @throws DateTimeParseException  nếu chuỗi không đúng định dạng
     * @throws IllegalArgumentException nếu chuỗi null hoặc rỗng
     */
    public static LocalDateTime parseDateTime(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("Chuỗi ngày giờ không được để trống.");
        }
        return LocalDateTime.parse(s.trim(), DATETIME_FORMATTER);
    }

    /**
     * Chuyển đổi {@link LocalDate} thành {@link java.util.Date} để dùng với iText/Apache POI.
     *
     * @param ld LocalDate cần chuyển đổi
     * @return đối tượng {@link Date} tương ứng theo múi giờ hệ thống; null nếu ld là null
     */
    public static Date toDate(LocalDate ld) {
        if (ld == null) {
            return null;
        }
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Tính số ngày giữa hai mốc thời gian.
     *
     * @param from ngày bắt đầu (không được null)
     * @param to   ngày kết thúc (không được null)
     * @return số ngày từ {@code from} đến {@code to} (âm nếu {@code to} trước {@code from})
     */
    public static long getDaysBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Ngày bắt đầu và kết thúc không được để trống.");
        }
        return ChronoUnit.DAYS.between(from, to);
    }
}
