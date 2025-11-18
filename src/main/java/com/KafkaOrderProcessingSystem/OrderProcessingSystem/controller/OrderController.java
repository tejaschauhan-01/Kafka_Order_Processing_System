package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderRequestDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderResponseDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.OrderProducerServiceImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// REST controller for order processing operations.
// Handles requests related to creating and submitting orders.
@Tag(name = "Order Endpoints")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    // Service for producing and submitting orders to Kafka broker.
    private final OrderProducerServiceImpl orderProducerService;

    // Endpoint to create and submit a new order.
    // Receives an OrderRequestDTO in the request body and returns a response indicating success or failure.
    @PostMapping("/create_order")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        Order order = new Order(
                orderRequestDTO.getOrderId(),
                orderRequestDTO.getProductName(),
                orderRequestDTO.getQuantity(),
                orderRequestDTO.getStatus()
        );
        try{
            // Submit the order using the order producer service.
            orderProducerService.submitOrder(order);
            order.setStatus("PROCESSED");
            return ResponseEntity.ok(new OrderResponseDTO(
                    order.getOrderId(),
                    order.getProductName(),
                    order.getQuantity(),
                    order.getStatus(),
                    "Order submitted successfully"
            ));
        }
        catch (IllegalArgumentException e)
        {
            // Return a bad request response with the error message if an exception occurs.
            return ResponseEntity.badRequest().body("Invalid order data: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to submit order: " + e.getMessage());
        }
    }
}
