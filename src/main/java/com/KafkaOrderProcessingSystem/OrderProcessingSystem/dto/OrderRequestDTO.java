package com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

// DTO for Order Request
public class OrderRequestDTO {

    private String orderId;

    @NotBlank(message = "product  name required")
    @Pattern(regexp = "^[A-Za-z\\s]+$", message = "product name should not be number")
    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "status required")
    @Pattern(regexp = "^(PENDING|COMPLETED|FAILED)$", message = "Status must be PENDING, COMPLETED, or FAILED")
    private String status;
}
