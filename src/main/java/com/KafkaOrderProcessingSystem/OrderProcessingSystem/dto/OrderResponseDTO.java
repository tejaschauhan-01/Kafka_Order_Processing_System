package com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponseDTO {

    private String orderId;

    private String productName;

    private int quantity;

    private String status;

    private String message;
}
