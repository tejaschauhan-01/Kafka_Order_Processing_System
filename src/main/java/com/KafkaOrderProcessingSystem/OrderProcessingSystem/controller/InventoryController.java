package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderResponseDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.WarehouseStockDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import jakarta.validation.Valid;
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
    public ResponseEntity<?> addInventory(@Valid @RequestBody WarehouseStockDTO warehouseStockDTO){
        WarehouseStock warehouseStock = new WarehouseStock(
                warehouseStockDTO.getProductName(),
                warehouseStockDTO.getAvailableQuantity()
        );
        inventoryService.addInventory(warehouseStock);
        return ResponseEntity.ok(new WarehouseStockDTO(
                warehouseStockDTO.getProductName(),
                warehouseStockDTO.getAvailableQuantity(),
                "Stock Added Succesfully"
        ));
    }

    @GetMapping("/getInventory")
    public ResponseEntity<?> getInventory(){
        List <WarehouseStock> stocks = inventoryService.getInventory();
        return ResponseEntity.ok().body(stocks);
    }
}
