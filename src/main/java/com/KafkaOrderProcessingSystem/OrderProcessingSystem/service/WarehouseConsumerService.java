package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WarehouseConsumerService {

    private final WarehouseRepository warehouseRepository;

    @KafkaListener(topics = "orders", groupId = "warehouse-group")
    public void consumeOrder(Order order) {
        System.out.println("📦 Received Order: " + order);

        WarehouseStock stock = warehouseRepository.findById(order.getProductName())
                .orElse(new WarehouseStock(order.getProductName(), 100));

        stock.setAvailableQuantity(stock.getAvailableQuantity() - order.getQuantity());
        warehouseRepository.save(stock);

        System.out.println("🏪 Updated stock for a " + order.getProductName() + ": " + stock.getAvailableQuantity());
    }
}
