package com.petshop.constant;

/**
 * Các hằng số dùng chung trong toàn bộ ứng dụng Quản lý Cửa hàng Thú cưng.
 */
public final class AppConstants {

    // Ngăn khởi tạo
    private AppConstants() {}

    // =========================================================
    // Thông tin ứng dụng
    // =========================================================
    /** Tên ứng dụng hiển thị trên thanh tiêu đề */
    public static final String APP_NAME = "Quản lý Cửa hàng Thú cưng";

    /** Phiên bản ứng dụng */
    public static final String APP_VERSION = "1.0.0";

    // =========================================================
    // Định dạng ngày / giờ
    // =========================================================
    /** Định dạng ngày: ngày/tháng/năm */
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    /** Định dạng ngày giờ: ngày/tháng/năm giờ:phút */
    public static final String DATETIME_FORMAT = "dd/MM/yyyy HH:mm";

    // =========================================================
    // Tiền tệ
    // =========================================================
    /** Ký hiệu tiền tệ Việt Nam */
    public static final String CURRENCY_SYMBOL = "đ";

    // =========================================================
    // Trạng thái thú cưng
    // =========================================================
    /** Thú cưng đang có sẵn để bán */
    public static final String STATUS_AVAILABLE = "AVAILABLE";

    /** Thú cưng đã được bán */
    public static final String STATUS_SOLD = "SOLD";

    /** Thú cưng đang được đặt trước */
    public static final String STATUS_RESERVED = "RESERVED";

    // =========================================================
    // Giới tính thú cưng
    // =========================================================
    /** Giới tính đực */
    public static final String GENDER_MALE = "MALE";

    /** Giới tính cái */
    public static final String GENDER_FEMALE = "FEMALE";

    /** Giới tính chưa xác định */
    public static final String GENDER_UNKNOWN = "UNKNOWN";

    // =========================================================
    // Vai trò người dùng
    // =========================================================
    /** Vai trò quản trị viên */
    public static final String ROLE_ADMIN = "ADMIN";

    /** Vai trò nhân viên */
    public static final String ROLE_STAFF = "STAFF";

    // =========================================================
    // Phương thức thanh toán
    // =========================================================
    /** Thanh toán bằng tiền mặt */
    public static final String PAYMENT_CASH = "CASH";

    /** Thanh toán bằng chuyển khoản ngân hàng */
    public static final String PAYMENT_TRANSFER = "TRANSFER";

    // =========================================================
    // Loại mặt hàng trong hóa đơn / phiếu nhập
    // =========================================================
    /** Loại mặt hàng: Thú cưng */
    public static final String ITEM_PET = "PET";

    /** Loại mặt hàng: Phụ kiện */
    public static final String ITEM_ACCESSORY = "ACCESSORY";

    /** Loại mặt hàng: Dịch vụ */
    public static final String ITEM_SERVICE = "SERVICE";

    // =========================================================
    // Trạng thái hóa đơn
    // =========================================================
    /** Hóa đơn đã thanh toán */
    public static final String INVOICE_PAID = "PAID";

    /** Hóa đơn đã hủy */
    public static final String INVOICE_CANCELLED = "CANCELLED";

    // =========================================================
    // Tiền tố mã chứng từ
    // =========================================================
    /** Tiền tố mã hóa đơn bán hàng */
    public static final String PREFIX_INVOICE = "HD";

    /** Tiền tố mã phiếu nhập kho */
    public static final String PREFIX_IMPORT = "PN";
}
