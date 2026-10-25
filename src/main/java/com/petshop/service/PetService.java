package com.petshop.service;

import com.petshop.model.Pet;
import com.petshop.repository.PetRepository;
import com.petshop.util.DatabaseManager;

import java.sql.Connection;
import java.util.*;

/**
 * Service quản lý nghiệp vụ Thú cưng.
 */
public class PetService {

    private final PetRepository petRepository = new PetRepository();

    public List<Pet> getAll() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return petRepository.findAll(conn);
        }
    }

    public List<Pet> getAvailable() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return petRepository.findAvailable(conn);
        }
    }

    public List<Pet> search(String keyword, String species, String status) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return petRepository.search(conn, keyword, species, status);
        }
    }

    public Optional<Pet> getById(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return petRepository.findById(conn, id);
        }
    }

    public Pet add(Pet pet) throws Exception {
        validate(pet);
        try (Connection conn = DatabaseManager.getConnection()) {
            if (pet.getPetCode() == null || pet.getPetCode().isBlank()) {
                pet.setPetCode(com.petshop.util.CodeGenerator.generatePetCode(conn));
            }
            int id = petRepository.insert(conn, pet);
            pet.setId(id);
            return pet;
        }
    }

    public void update(Pet pet) throws Exception {
        validate(pet);
        try (Connection conn = DatabaseManager.getConnection()) {
            petRepository.update(conn, pet);
        }
    }

    public void delete(int id) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            Optional<Pet> opt = petRepository.findById(conn, id);
            if (opt.isPresent()) {
                Pet pet = opt.get();
                if (!"AVAILABLE".equalsIgnoreCase(pet.getStatus())) {
                    throw new IllegalStateException("Chỉ xóa được thú cưng đang ở trạng thái 'Có sẵn'. Thú cưng đã bán/đặt trước: hãy hủy hóa đơn trước.");
                }
            }
            petRepository.delete(conn, id);
        }
    }

    public long countAvailable() throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            return petRepository.countAvailable(conn);
        }
    }

    public List<String> getDistinctSpecies() throws Exception {
        Set<String> speciesSet = new TreeSet<>(List.of("Chó", "Mèo", "Thỏ", "Hamster", "Chim", "Bò sát", "Khác"));
        try (Connection conn = DatabaseManager.getConnection()) {
            List<Pet> all = petRepository.findAll(conn);
            for (Pet p : all) {
                if (p.getSpecies() != null && !p.getSpecies().isBlank()) {
                    speciesSet.add(p.getSpecies().trim());
                }
            }
        }
        return new ArrayList<>(speciesSet);
    }

    private void validate(Pet pet) {
        // Tên riêng là tùy chọn: để trống thì lưu NULL
        if (pet.getName() != null && pet.getName().isBlank()) {
            pet.setName(null);
        }
        if (pet.getSpecies() == null || pet.getSpecies().isBlank()) {
            throw new IllegalArgumentException("Loài thú cưng không được để trống.");
        }
        if (pet.getBreed() == null || pet.getBreed().isBlank()) {
            throw new IllegalArgumentException("Giống thú cưng không được để trống.");
        }
        if (pet.getSellPrice() < 0) {
            throw new IllegalArgumentException("Giá bán không được là số âm.");
        }
    }
}
