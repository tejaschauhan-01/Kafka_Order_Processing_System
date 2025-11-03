package com.KafkaOrderProcessingSystem.OrderProcessingSystem.Entity;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "warehouse_stock")
public class WarehouseStock {

    @Id
    private String productName;
    
    private int availableQuantity;
}
