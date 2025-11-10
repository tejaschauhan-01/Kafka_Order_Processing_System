package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.OrderProducerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class OrderProducerServiceImpl implements OrderProducerService {

    // Kafka template used to publish orders to the Kafka topic.
    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    private static final String TOPIC = "orders";  // Kafka topic name

    @Override
    public void submitOrder(Order order) {
        // Check if the product exists in warehouse stock
        Optional<WarehouseStock> stockOpt = warehouseRepository.findById(order.getProductName());
        if (stockOpt.isEmpty()) {
            throw new RuntimeException("Product not found");
        }

        // Retrieve the existing product record
        WarehouseStock stock = stockOpt.get();

        // Check if the product is out of stock
        if (stock.getAvailableQuantity() <= 0) {
            handleOrderStatus(order, "FAILED", "Out of Stock", true);
        }
        // Check if requested quantity exceeds available stock
        else if (order.getQuantity() > stock.getAvailableQuantity()) {
            handleOrderStatus(order, "FAILED",
                    "Order Quantity exceeds available stock: " + stock.getAvailableQuantity(),
                    true);
        }
        // Otherwise, mark order as processed successfully
        else {
            handleOrderStatus(order, "PROCESSED", null, false);
        }

        try {
            log.info("data hase been save in Order database");

            // Send order to a Kafka topic
            kafkaTemplate.send(TOPIC, order.getOrderId(), order);

            log.info("Order saved (RECEIVED) and sent to Kafka: " + order);
        } catch (Exception e) {
            log.info("error is the " + e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    // Helper method to handle setting order status and error messages
    private void handleOrderStatus(Order order, String status, String message, boolean throwException) {
        order.setStatus(status);
        orderRepository.save(order);

        if (throwException) {
            throw new RuntimeException(message);
        }
    }

}
