package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

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

import java.util.Optional;

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

    @GetMapping
    public String getstr()
    {
        return "tejas";
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@Valid @RequestBody Order order) {
//        Optional<WarehouseStock> stockOpt = warehouseRepository.findById(order.getProductName());
//        if(stockOpt.isEmpty()){
//            return ResponseEntity.badRequest().body("Product '" + order.getOrderId() + "' not ofund in stock!");
//        }
        try{
            orderProducerService.submitOrder(order);
            return ResponseEntity.ok("Order submitted successfully");
        }
        catch (IllegalArgumentException e)
        {
            return ResponseEntity.badRequest().body("Invalid order data: " + e.getMessage());
        } catch (RuntimeException e) {
            // For business or service exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to submit order: " + e.getMessage());
        }


    }

}
