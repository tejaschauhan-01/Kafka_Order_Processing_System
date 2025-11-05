package com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WarehouseStockDTO {

    private String productName;
    private int availableQuantity;
    private String message;

}
