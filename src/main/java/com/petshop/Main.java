package com.petshop;

/**
 * Launcher wrapper — tránh lỗi module khi chạy JavaFX từ fat JAR.
 * Lớp này KHÔNG extends Application.
 */
public class Main {
    public static void main(String[] args) {
        App.main(args);
    }
}
