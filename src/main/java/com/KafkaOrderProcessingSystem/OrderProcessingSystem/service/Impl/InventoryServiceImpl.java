package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public void addInventory(WarehouseStock warehouseStock) {
        Optional<WarehouseStock> existingProduct = warehouseRepository.findById(warehouseStock.getProductName());
        if(existingProduct.isPresent()){
            throw new IllegalArgumentException("The product name already exists");
        }
        warehouseRepository.save(warehouseStock);
    }

    @Override
    public List<WarehouseStock> getInventory() {
        List<WarehouseStock> stock = warehouseRepository.findAll();
        return stock;
    }

    @Override
    public WarehouseStock updateInventory(String existingProductName, String newProductName, int additionalQuantity) {
        Optional<WarehouseStock> existingOpt = warehouseRepository.findById(existingProductName);
        if(existingOpt.isEmpty()){
            throw new IllegalArgumentException("Product not found in inventory: " + existingProductName);
        }
        WarehouseStock existing = existingOpt.get();
        existing.setAvailableQuantity(existing.getAvailableQuantity() + additionalQuantity);
        return warehouseRepository.save(existing);
    }


}
