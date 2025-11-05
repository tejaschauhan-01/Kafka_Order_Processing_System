package com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message="product name required")
    private String productName;
    @Min(value=1, message = "Quantity must be at least 1")
    private int quantity;

    private String status;

}
