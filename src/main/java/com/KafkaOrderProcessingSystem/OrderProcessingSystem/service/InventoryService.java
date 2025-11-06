package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InventoryService {
    void addInventory(WarehouseStock warehouseStock);

    List<WarehouseStock> getInventory();

    WarehouseStock updateInventory(String existingProductName, String newProductName, int additionalQuantity);
}
