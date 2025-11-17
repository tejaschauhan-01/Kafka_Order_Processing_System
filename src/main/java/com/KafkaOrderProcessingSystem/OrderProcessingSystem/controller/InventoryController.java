package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.WarehouseStockDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Inventory Management", description = "APIs for managing warehouse stock and inventory operations")
@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {


    private final InventoryService inventoryService;

    @Operation(
            summary = "Add new stock to inventory",
            description = "Creates a new product entry in the warehouse. Product name must be unique."
    )
    @PostMapping("/add_stock")
    public ResponseEntity<WarehouseStockDTO> addInventory(@Valid @RequestBody WarehouseStockDTO warehouseStockDTO) {

        // Map DTO to entity (following DTO pattern for layer separation)
        WarehouseStock warehouseStock = new WarehouseStock(
                warehouseStockDTO.getProductName(),
                warehouseStockDTO.getAvailableQuantity()
        );

        inventoryService.addInventory(warehouseStock);
        // Build success response
        WarehouseStockDTO response = new WarehouseStockDTO(
                warehouseStockDTO.getProductName(),
                warehouseStockDTO.getAvailableQuantity(),
                "Stock added successfully"
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get all inventory items",
            description = "Retrieves a complete list of all products in the warehouse inventory"
    )
    @GetMapping("/stock_list")
    public ResponseEntity<List<WarehouseStock>> getInventory() {
        List<WarehouseStock> stocks = inventoryService.getInventory();
        return ResponseEntity.ok(stocks);
    }

    @Operation(
            summary = "Update existing stock",
            description = "Updates product name and/or quantity for an existing inventory item"
    )
    @PutMapping("/update_stock/{productName}")
    public ResponseEntity<WarehouseStockDTO> updateInventory(
            @Valid @RequestBody WarehouseStockDTO warehouseStockDTO,
            @Parameter(description = "Current product name to update", required = true)
            @PathVariable String productName) {
        WarehouseStock updatedStock = inventoryService.updateInventory(
                productName,
                warehouseStockDTO.getProductName(),
                warehouseStockDTO.getAvailableQuantity()
        );
        // Build success response with updated details
        WarehouseStockDTO response = new WarehouseStockDTO(
                updatedStock.getProductName(),
                updatedStock.getAvailableQuantity(),
                "Inventory updated successfully"
        );

        // Return 200 OK with updated stock details
        return ResponseEntity.ok(response);
    }
}
