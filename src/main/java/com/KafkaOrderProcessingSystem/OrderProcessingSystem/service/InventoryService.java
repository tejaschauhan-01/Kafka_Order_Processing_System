package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface InventoryService {

    void addInventory(WarehouseStock warehouseStock);

    Page<WarehouseStock> getInventory(int page, int size, String sortBy);

    WarehouseStock updateInventory(String existingProductName, int additionalQuantity);
}
