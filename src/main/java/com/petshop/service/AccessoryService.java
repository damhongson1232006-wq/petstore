package com.petshop.service;

import com.petshop.model.Accessory;
import com.petshop.repository.AccessoryRepository;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý Phụ kiện và Sản phẩm kèm theo.
 */
public class AccessoryService {

    private final AccessoryRepository accessoryRepository = new AccessoryRepository();

    public List<Accessory> getAll() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return accessoryRepository.findAll(conn);
        }
    }

    public List<Accessory> getActive() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return accessoryRepository.findActive(conn);
        }
    }

    public List<Accessory> getLowStock() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return accessoryRepository.findLowStock(conn);
        }
    }

    public List<Accessory> search(String keyword, String category) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return accessoryRepository.search(conn, keyword, category);
        }
    }

    public Optional<Accessory> getById(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return accessoryRepository.findById(conn, id);
        }
    }

    public Accessory add(Accessory acc) throws Exception {
        validate(acc);
        try (Connection conn = DatabaseManager.getConnection()) {
            int id = accessoryRepository.insert(conn, acc);
            acc.setId(id);
            return acc;
        }
    }

    public void update(Accessory acc) throws Exception {
        validate(acc);
        try (Connection conn = DatabaseManager.getConnection()) {
            accessoryRepository.update(conn, acc);
        }
    }

    public void delete(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            accessoryRepository.delete(conn, id);
        }
    }

    public void updateStock(int id, int delta) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            accessoryRepository.updateQuantity(conn, id, delta);
        }
    }

    private void validate(Accessory acc) {
        if (acc.getName() == null || acc.getName().isBlank()) {
            throw new IllegalArgumentException("Tên phụ kiện không được để trống.");
        }
        if (acc.getSellPrice() < 0) {
            throw new IllegalArgumentException("Giá bán không được là số âm.");
        }
        if (acc.getQuantity() < 0) {
            throw new IllegalArgumentException("Số lượng tồn không được là số âm.");
        }
    }
}
