package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.OrderProducerService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class OrderProducerServiceImpl implements OrderProducerService {
    // @Bean
    private final KafkaTemplate<String, Order> kafkaTemplate;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProducerFactory<?, ?> producerFactory;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @PostConstruct
    public void printKafkaConfig() {
        System.out.println("Producer value serializer: " +
                producerFactory.getConfigurationProperties().get("value.serializer"));
    }

    private static final String TOPIC = "orders";

    @Override
    public void submitOrder(Order order) {
        Optional<WarehouseStock> stockOpt = warehouseRepository.findById(order.getProductName());
        if(stockOpt.isEmpty()){
            throw new RuntimeException("Product not found");
        }

        WarehouseStock stock = stockOpt.get();

        if (stock.getAvailableQuantity() <= 0) {
            handleOrderStatus(order, "FAILED", "Out of Stock", true);
        }
        else if (order.getQuantity() > stock.getAvailableQuantity()) {
            handleOrderStatus(order, "FAILED",
                    "Order Quantity exceeds available stock: " + stock.getAvailableQuantity(),
                    true);
        }
        else {
            handleOrderStatus(order, "PROCESSED", null, false);
        }

        try{
            log.info("data hase been save in Order database");
           kafkaTemplate.send(TOPIC, order.getOrderId() , order);
            log.info("Order saved (RECEIVED) and sent to Kafka: " + order);
        }
        catch (Exception e){
            log.info("error is the "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }
    private void handleOrderStatus(Order order, String status, String message, boolean throwException) {
        order.setStatus(status);
        orderRepository.save(order);

        if (throwException) {
            throw new RuntimeException(message);
        }
    }

}
