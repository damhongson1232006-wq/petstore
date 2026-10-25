package com.petshop.service;

import com.petshop.model.Supplier;
import com.petshop.repository.SupplierRepository;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý nhà cung cấp.
 */
public class SupplierService {

    private final SupplierRepository supplierRepository = new SupplierRepository();

    public List<Supplier> getAll() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return supplierRepository.findAll(conn);
        }
    }

    public List<Supplier> getActive() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return supplierRepository.findActive(conn);
        }
    }

    public Optional<Supplier> getById(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return supplierRepository.findById(conn, id);
        }
    }

    public Supplier add(Supplier supplier) throws Exception {
        if (supplier.getName() == null || supplier.getName().isBlank()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống.");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            int id = supplierRepository.insert(conn, supplier);
            supplier.setId(id);
            return supplier;
        }
    }

    public void update(Supplier supplier) throws Exception {
        if (supplier.getName() == null || supplier.getName().isBlank()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được để trống.");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            supplierRepository.update(conn, supplier);
        }
    }

    public void setActive(int id, boolean active) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            supplierRepository.setActive(conn, id, active);
        }
    }
}
