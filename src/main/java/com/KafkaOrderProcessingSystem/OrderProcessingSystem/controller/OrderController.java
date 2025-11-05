package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.OrderProducerServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    @Autowired
    private final OrderProducerServiceImpl orderProducerService;

    @GetMapping
    public String getstr()
    {
        return "tejas";
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {

        orderProducerService.submitOrder(order);
        return ResponseEntity.ok().build();
    }

}
