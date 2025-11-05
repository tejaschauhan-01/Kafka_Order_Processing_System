package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Override
    public void addInventory(WarehouseStock warehouseStock) {
        warehouseRepository.save(warehouseStock);
    }

    @Override
    public List<WarehouseStock> getInventory() {
        List<WarehouseStock> stock = warehouseRepository.findAll();
        return stock;
    }
}
