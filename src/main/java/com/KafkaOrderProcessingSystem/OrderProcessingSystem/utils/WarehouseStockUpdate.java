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

    // Method to process the received order and update warehouse stock accordingly
    public void updateStock(Order order) {

        // Log that the consumer has received an order from Kafka
        log.info("kafka consumer received order: " + order);

        // Fetch product stock from a database based on product name
        Optional<WarehouseStock> stockOptional = warehouseRepository.findById(order.getProductName());

        // Retrieve the actual stock record (assuming product exists)
        WarehouseStock stock = stockOptional.get();

        // Calculate the remaining stock after fulfilling the order
        int remaining = stock.getAvailableQuantity() - order.getQuantity();
        stock.setAvailableQuantity(remaining);

        // Save updated stock details back to the database
        warehouseRepository.save(stock);
        log.info("warehouse has been updated with with " + stock);
    }
}
