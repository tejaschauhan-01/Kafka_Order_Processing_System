package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderRequestDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderResponseDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.OrderProducerServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private final OrderProducerServiceImpl orderProducerService;

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        Order order = new Order(
                orderRequestDTO.getOrderId(),
                orderRequestDTO.getProductName(),
                orderRequestDTO.getQuantity(),
                orderRequestDTO.getStatus()
        );
        try{
            orderProducerService.submitOrder(order);
            Order receivedOrder = orderRepository.findById(order.getOrderId()).get();
            return ResponseEntity.ok(new OrderResponseDTO(
                    receivedOrder.getOrderId(),
                    receivedOrder.getProductName(),
                    receivedOrder.getQuantity(),
                    receivedOrder.getStatus(),
                    "Order submitted successfully"
            ));
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body("Invalid order data: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to submit order: " + e.getMessage());
        }
    }
}
