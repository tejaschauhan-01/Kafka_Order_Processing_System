package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final WarehouseRepository warehouseRepository;

    @Override
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
    public List<WarehouseStock> getInventory() {
        // Retrieve all products from inventory
        List<WarehouseStock> stock = warehouseRepository.findAll();
        return stock;
    }

    @Override
    public WarehouseStock updateInventory(String existingProductName, String newProductName, int additionalQuantity) {
        // Check if the existing product is present in inventory
        Optional<WarehouseStock> existingOpt = warehouseRepository.findById(existingProductName);
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Product not found in inventory: " + existingProductName);
        }

        // Get the existing product record
        WarehouseStock existing = existingOpt.get();

        // Increase the available quantity by the additional amount
        existing.setAvailableQuantity(existing.getAvailableQuantity() + additionalQuantity);

        // Save updated product details to a database

        return warehouseRepository.save(existing);
    }


}
