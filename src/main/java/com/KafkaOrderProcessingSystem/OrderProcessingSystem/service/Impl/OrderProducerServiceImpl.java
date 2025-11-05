package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.OrderProducerService;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Service;

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

    @PostConstruct
    public void printKafkaConfig() {
        System.out.println("Producer value serializer: " +
                producerFactory.getConfigurationProperties().get("value.serializer"));
    }

    private static final String TOPIC = "orders";

    @Override
    public void submitOrder(Order order) {
//        order.setStatus("PENDING");
        orderRepository.save(order);
        try{
            log.info("data hase been save in database");
            kafkaTemplate.send(TOPIC, order.getOrderId() , order);
            log.info("Order saved (RECEIVED) and sent to Kafka: " + order);
        }
        catch (Exception e){
            log.info("error is the "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }
}
