package com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "orders")
public class Order {

    @Id
    private String orderId;

    @NotBlank(message = "product name required")
    @Pattern(regexp = "^[A-Za-z\\s]+$", message = "product name should not be number")
    private String productName;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    @NotBlank(message = "status required")
    @Pattern(regexp = "^(PENDING|COMPLETED|FAILED)$", message = "Status must be PENDING, COMPLETED, or FAILED")
    private String status;

}
