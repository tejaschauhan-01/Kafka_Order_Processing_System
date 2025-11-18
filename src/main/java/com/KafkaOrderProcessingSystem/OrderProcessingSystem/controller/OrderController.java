package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderRequestDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderResponseDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.OrderProducerServiceImpl;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Tag(name = "Order Management", description = "APIs for managing and processing orders")
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;

    private final OrderProducerServiceImpl orderProducerService;

    @Operation(
            summary = "Create a new order",
            description = "Creates and submits a new order to the processing queue. The order will be validated and sent to Kafka for asynchronous processing."
    )
    @PostMapping("/create_order")
    public ResponseEntity<OrderResponseDTO> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {

        // Map DTO to entity using builder pattern (more readable than constructor)
        Order order = Order.builder()
                .orderId(orderRequestDTO.getOrderId())
                .productName(orderRequestDTO.getProductName())
                .quantity(orderRequestDTO.getQuantity())
                .status(orderRequestDTO.getStatus())
                .build();

        orderProducerService.submitOrder(order);
        // Retrieve the persisted order from database to confirm successful processing
       order.setStatus("PROCESSED");
        Order receivedOrder = orderRepository.findById(order.getOrderId()).get();

        // Build response using builder pattern for clarity
        OrderResponseDTO response = OrderResponseDTO.builder()
                .orderId(receivedOrder.getOrderId())
                .productName(receivedOrder.getProductName())
                .quantity(receivedOrder.getQuantity())
                .status(receivedOrder.getStatus())
                .message("Order submitted successfully and queued for processing")
                .build();

        // Return 201 CREATED to indicate resource was successfully created
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
