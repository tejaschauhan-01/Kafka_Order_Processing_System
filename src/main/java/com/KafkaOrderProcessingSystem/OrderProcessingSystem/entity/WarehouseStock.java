package com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "warehouse_stock")
// Entity class for Warehouse Stock
public class WarehouseStock {

    @Id
    private String productName;
    private int availableQuantity;
}
