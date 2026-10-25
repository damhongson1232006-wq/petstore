package com.petshop.util;

import java.util.regex.Pattern;

/**
 * Tiện ích kiểm tra và xác thực dữ liệu đầu vào.
 * Sử dụng trong tầng Controller và Service để đảm bảo dữ liệu hợp lệ trước khi ghi CSDL.
 */
public class ValidationUtil {

    /**
     * Biểu thức chính quy số điện thoại Việt Nam:
     * - Bắt đầu bằng 0
     * - 10 hoặc 11 chữ số tổng cộng
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0\\d{9,10}$");

    /**
     * Biểu thức chính quy địa chỉ email cơ bản theo chuẩn RFC 5322 đơn giản hóa.
     */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$");

    /** Lớp tiện ích – không cho phép khởi tạo */
    private ValidationUtil() {
        throw new UnsupportedOperationException("ValidationUtil là lớp tiện ích, không thể khởi tạo.");
    }

    /**
     * Kiểm tra chuỗi có null hoặc rỗng (kể cả chỉ chứa khoảng trắng) không.
     *
     * @param s chuỗi cần kiểm tra
     * @return {@code true} nếu chuỗi là null, rỗng hoặc chỉ chứa khoảng trắng
     */
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.isBlank();
    }

    /**
     * Kiểm tra số điện thoại có hợp lệ không.
     * Điều kiện: bắt đầu bằng 0 và có 10 hoặc 11 chữ số.
     *
     * @param phone số điện thoại cần kiểm tra
     * @return {@code true} nếu hợp lệ
     */
    public static boolean isValidPhone(String phone) {
        if (isNullOrEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Kiểm tra địa chỉ email có hợp lệ không.
     *
     * @param email địa chỉ email cần kiểm tra
     * @return {@code true} nếu hợp lệ
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Kiểm tra giá trị có dương (> 0) không.
     *
     * @param value giá trị cần kiểm tra
     * @return {@code true} nếu value > 0
     */
    public static boolean isPositive(long value) {
        return value > 0;
    }

    /**
     * Kiểm tra giá trị có không âm (>= 0) không.
     *
     * @param value giá trị cần kiểm tra
     * @return {@code true} nếu value >= 0
     */
    public static boolean isNonNegative(long value) {
        return value >= 0;
    }

    /**
     * Yêu cầu chuỗi không được rỗng; ném ngoại lệ với thông báo tiếng Việt nếu vi phạm.
     *
     * @param val       giá trị cần kiểm tra
     * @param fieldName tên trường hiển thị trong thông báo lỗi (tiếng Việt)
     * @throws IllegalArgumentException nếu val là null, rỗng hoặc chỉ chứa khoảng trắng
     */
    public static void requireNonEmpty(String val, String fieldName) {
        if (isNullOrEmpty(val)) {
            throw new IllegalArgumentException(
                    "Trường [" + fieldName + "] không được để trống.");
        }
    }
}
