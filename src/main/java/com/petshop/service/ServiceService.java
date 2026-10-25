package com.petshop.service;

import com.petshop.model.Service;
import com.petshop.repository.ServiceRepository;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý danh mục dịch vụ thú cưng.
 */
public class ServiceService {

    private final ServiceRepository serviceRepository = new ServiceRepository();

    public List<Service> getAll() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return serviceRepository.findAll(conn);
        }
    }

    public List<Service> getActive() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return serviceRepository.findActive(conn);
        }
    }

    public Optional<Service> getById(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return serviceRepository.findById(conn, id);
        }
    }

    public Service add(Service service) throws Exception {
        if (service.getName() == null || service.getName().isBlank()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (service.getPrice() < 0) {
            throw new IllegalArgumentException("Giá dịch vụ không được là số âm.");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            int id = serviceRepository.insert(conn, service);
            service.setId(id);
            return service;
        }
    }

    public void update(Service service) throws Exception {
        if (service.getName() == null || service.getName().isBlank()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (service.getPrice() < 0) {
            throw new IllegalArgumentException("Giá dịch vụ không được là số âm.");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            serviceRepository.update(conn, service);
        }
    }

    public void setActive(int id, boolean active) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            serviceRepository.setActive(conn, id, active);
        }
    }
}
