package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.OrderProducerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class OrderProducerServiceImpl implements OrderProducerService {

    private final KafkaTemplate<String, Order> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    private static final String TOPIC = "orders";

    @Override
    public void submitOrder(Order order) {
//        order.setStatus("PENDING");
        orderRepository.save(order);

        kafkaTemplate.send(TOPIC, order.getOrderId() , order);
        log.info("Order saved (RECEIVED) and sent to Kafka: " + order);
    }
}
