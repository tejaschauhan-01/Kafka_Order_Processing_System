package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import org.springframework.kafka.annotation.KafkaListener;

public interface WarehouseConsumerService {

    void consumeOrder(Order order);
}
