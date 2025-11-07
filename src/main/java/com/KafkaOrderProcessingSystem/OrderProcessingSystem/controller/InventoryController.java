package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.WarehouseStockDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory Endpoints")
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/add_stock")
    public ResponseEntity<?> addInventory(@Valid @RequestBody WarehouseStockDTO warehouseStockDTO){
        try{
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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new WarehouseStockDTO(
                    warehouseStockDTO.getProductName(),
                    warehouseStockDTO.getAvailableQuantity(),
                    e.getMessage()
            ));
        }
    }

    @GetMapping("/stock_list")
    public ResponseEntity<?> getInventory(){
        List <WarehouseStock> stocks = inventoryService.getInventory();
        return ResponseEntity.ok().body(stocks);
    }

    @PutMapping("/update_stock/{productName}")
    public ResponseEntity<?> updateInventory(
            @Valid @RequestBody WarehouseStockDTO warehouseStockDTO,
            @PathVariable String productName){
        try {
            WarehouseStock updateStock = inventoryService.updateInventory(
                    productName,
                    warehouseStockDTO.getProductName(),
                    warehouseStockDTO.getAvailableQuantity()
            );
            return ResponseEntity.ok(new WarehouseStockDTO(
                    updateStock.getProductName(),
                    updateStock.getAvailableQuantity(),
                    "Inventory updated Succesfully"
            ));
        } catch (IllegalArgumentException e){
            return ResponseEntity.badRequest().body(new WarehouseStockDTO(
                    warehouseStockDTO.getProductName(),
                    warehouseStockDTO.getAvailableQuantity(),
                    e.getMessage()
            ));
        }
    }
}
