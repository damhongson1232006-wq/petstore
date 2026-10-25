package com.petshop.util;

import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Lưu trữ và nạp hình ảnh thú cưng.
 * Ảnh được sao chép vào thư mục {@code ~/PetShopApp/images}; CSDL chỉ lưu tên file.
 */
public final class ImageUtil {

    private static final Path IMAGE_DIR = Paths.get(System.getProperty("user.home"), "PetShopApp", "images");
    private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;
    private static final Map<String, Image> CACHE = new HashMap<>();

    private ImageUtil() {
    }

    public static Path getImageDir() {
        return IMAGE_DIR;
    }

    /**
     * Sao chép ảnh người dùng chọn vào thư mục lưu trữ của ứng dụng.
     *
     * @return tên file đã lưu (lưu vào cột image_path)
     */
    public static String store(File source) throws IOException {
        if (source == null || !source.isFile()) {
            throw new IOException("Không tìm thấy file ảnh.");
        }
        if (source.length() > MAX_SIZE_BYTES) {
            throw new IOException("Ảnh quá lớn (tối đa 10 MB).");
        }
        String fileName = source.getName();
        int dot = fileName.lastIndexOf('.');
        String ext = dot >= 0 ? fileName.substring(dot).toLowerCase(Locale.ROOT) : "";
        if (!ext.matches("\\.(png|jpg|jpeg|gif|bmp)")) {
            throw new IOException("Định dạng ảnh không được hỗ trợ (chỉ nhận png, jpg, jpeg, gif, bmp).");
        }
        Files.createDirectories(IMAGE_DIR);
        String stored = UUID.randomUUID().toString().replace("-", "") + ext;
        Files.copy(source.toPath(), IMAGE_DIR.resolve(stored), StandardCopyOption.REPLACE_EXISTING);
        return stored;
    }

    /**
     * Nạp ảnh đã lưu theo tên file, thu nhỏ về kích thước yêu cầu.
     *
     * @return ảnh, hoặc {@code null} nếu chưa có ảnh / file không tồn tại / ảnh lỗi
     */
    public static Image load(String name, double width, double height) {
        if (name == null || name.isBlank()
                || name.contains("/") || name.contains("\\") || name.contains("..")) {
            return null;
        }
        String key = name + "@" + width + "x" + height;
        Image cached = CACHE.get(key);
        if (cached != null) {
            return cached;
        }
        Path path = IMAGE_DIR.resolve(name);
        if (!Files.isRegularFile(path)) {
            return null;
        }
        Image image = new Image(path.toUri().toString(), width, height, true, true);
        if (image.isError()) {
            return null;
        }
        CACHE.put(key, image);
        return image;
    }
}
