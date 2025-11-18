package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.OrderStatus;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.exception.OrderProcessingException;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.OrderProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderProducerServiceImpl implements OrderProducerService {

    // Kafka template used to publish orders to the Kafka topic.
    private final KafkaTemplate<String, Order> kafkaTemplate;

    private final OrderRepository orderRepository;

    private final WarehouseRepository warehouseRepository;

    private static final String TOPIC = "orders";  // Kafka topic name

    @Override
    @Transactional
    public void submitOrder(Order order) {
        log.info("Received order request: {}", order);
        
        // Check if the product exists in warehouse stock
        WarehouseStock stock = warehouseRepository.findById(order.getProductName())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Check if the product is out of stock
        if (stock.getAvailableQuantity() <= 0) {
            handleOrderStatus(order, OrderStatus.FAILED, "Out of Stock", true);
        }
        // Check if requested quantity exceeds available stock
        else if (order.getQuantity() > stock.getAvailableQuantity()) {
            handleOrderStatus(order, OrderStatus.FAILED,
                    "Order Quantity exceeds available stock: " + stock.getAvailableQuantity(),
                    true);
        }
        // Otherwise, mark order as processed successfully
        else {
            handleOrderStatus(order, OrderStatus.PROCESSED, null, false);
        }

        try {
            log.info("data has been saved in Order database");

            // Send order to a Kafka topic
            kafkaTemplate.send(TOPIC, order.getOrderId(), order);
            log.info("Order saved (RECEIVED) and sent to Kafka: {}", order);

        } catch (Exception e) {
            log.error("Error while sending order to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }

    }

    // Helper method to handle setting order status and error messages
    // Industry practice: keep method responsibilities small, persist and optionally signal error
    private void handleOrderStatus(Order order, OrderStatus status, String message, boolean throwException) {
        // Persist status as String to be compatible with current DB schema
        order.setStatus(status.name());
        orderRepository.save(order);

        if (throwException) {
            throw new RuntimeException(message);
        }
    }

}
