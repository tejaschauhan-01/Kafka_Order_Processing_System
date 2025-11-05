package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/add_inventory")
    public ResponseEntity<?> addInventory(@RequestBody WarehouseStock warehouseStock){
        inventoryService.addInventory(warehouseStock);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/getInventory")
    public ResponseEntity<?> getInventory(){
        List <WarehouseStock> stocks = inventoryService.getInventory();
        return ResponseEntity.ok().body(stocks);
    }
}
