package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final WarehouseRepository warehouseRepository;

    @Override
    @Transactional
    public void addInventory(WarehouseStock warehouseStock) {
        // Check if a product with the same name already exists
        Optional<WarehouseStock> existingProduct = warehouseRepository.findById(warehouseStock.getProductName());
        if (existingProduct.isPresent()) {
            throw new IllegalArgumentException("The product name already exists");
        }
        // Save new product to the database
        warehouseRepository.save(warehouseStock);
    }

    @Override
    public Page<WarehouseStock> getInventory(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        // Retrieve all products from inventory
        return warehouseRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public WarehouseStock updateInventory(String existingProductName, int additionalQuantity) {
        // Check if the existing product is present in inventory
        Optional<WarehouseStock> existingOpt = warehouseRepository.findById(existingProductName);
        if (existingOpt.isEmpty()) {
            throw new RuntimeException("Product not found in inventory: " + existingProductName);
        }

        // Get the existing product record
        WarehouseStock existing = existingOpt.get();

        // Increase the available quantity by the additional amount
        existing.setAvailableQuantity(existing.getAvailableQuantity() + additionalQuantity);

        // Save updated product details to a database
        return warehouseRepository.save(existing);
    }
}
