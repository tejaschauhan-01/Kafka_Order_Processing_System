package com.KafkaOrderProcessingSystem.OrderProcessingSystem.utils;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class WarehouseStockUpdate {

    @Autowired
    private WarehouseRepository warehouseRepository;

    public void processOrder(Order order) {

        log.info("kafka consumer received order: " + order);
        Optional<WarehouseStock> stockOptional = warehouseRepository.findById(order.getProductName());

        WarehouseStock stock = stockOptional.get();
        int remaining = stock.getAvailableQuantity() - order.getQuantity();
        stock.setAvailableQuantity(remaining);
        warehouseRepository.save(stock);
        log.info("warehouse has been updated with with " + stock);
    }
}
