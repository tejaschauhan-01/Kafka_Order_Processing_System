package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.WarehouseConsumerService;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.utils.WarehouseStockUpdate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WarehouseConsumerServiceImpl implements WarehouseConsumerService {

    @Autowired
    public WarehouseStockUpdate warehousestockupdate;

    // Kafka listener to consume order messages from the "orders" topic
    @KafkaListener(topics = "orders", groupId = "warehouse-group")
    @Override
    public void consumeOrder(Order order) {
        log.info("order is in progress state and ready for consumer service to update warehouse stock");
        // Process the received order and update warehouse stock accordingly
        warehousestockupdate.processOrder(order);
    }
}
