package com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class OrderRequestDTO {
    private String orderId;
    private String productName;
    private int quantity;
    private String status;
}
