package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.WarehouseStockDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//  REST controller for inventory operations.
// Handles requests related to adding, retrieving, and updating warehouse stock.
@Tag(name = "Inventory Endpoints")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    // Service for inventory management operations.
    private final InventoryService inventoryService;

    // Endpoint to add new stock to the inventory.
    // Receives a WarehouseStockDTO in the request body and returns a response indicating success or failure.
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
            // Return a bad request response with the error message if an exception occurs.
            return ResponseEntity.badRequest().body(new WarehouseStockDTO(
                    warehouseStockDTO.getProductName(),
                    warehouseStockDTO.getAvailableQuantity(),
                    e.getMessage()
            ));
        }
    }

    // Endpoint to retrieve the list of all stock items in the inventory.
    @GetMapping("/stock_list")
    public ResponseEntity<?> getInventory(){
        // Fetch the list of warehouse stocks from the inventory service.
        List <WarehouseStock> stocks = inventoryService.getInventory();
        return ResponseEntity.ok().body(stocks);
    }

    // Endpoint to update the stock of a specific product in the inventory.
    // Receives a WarehouseStockDTO in the request body and the product name as a path variable.
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
