package com.petshop.service;

import com.petshop.model.Customer;
import com.petshop.repository.CustomerRepository;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * Service quản lý khách hàng.
 */
public class CustomerService {

    private final CustomerRepository customerRepository = new CustomerRepository();

    public List<Customer> getAll() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return customerRepository.findAll(conn);
        }
    }

    public List<Customer> search(String keyword) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return customerRepository.search(conn, keyword);
        }
    }

    public Optional<Customer> getById(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return customerRepository.findById(conn, id);
        }
    }

    public Optional<Customer> getByPhone(String phone) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return customerRepository.findByPhone(conn, phone);
        }
    }

    public Customer add(Customer customer) throws Exception {
        validate(customer);
        try (Connection conn = DatabaseManager.getConnection()) {
            int id = customerRepository.insert(conn, customer);
            customer.setId(id);
            return customer;
        }
    }

    public void update(Customer customer) throws Exception {
        validate(customer);
        try (Connection conn = DatabaseManager.getConnection()) {
            customerRepository.update(conn, customer);
        }
    }

    public void delete(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            customerRepository.delete(conn, id);
        }
    }

    private void validate(Customer customer) {
        if (customer.getFullName() == null || customer.getFullName().isBlank()) {
            throw new IllegalArgumentException("Tên khách hàng không được để trống.");
        }
        if (customer.getPhone() == null || customer.getPhone().isBlank()) {
            throw new IllegalArgumentException("Số điện thoại không được để trống.");
        }
    }
}
