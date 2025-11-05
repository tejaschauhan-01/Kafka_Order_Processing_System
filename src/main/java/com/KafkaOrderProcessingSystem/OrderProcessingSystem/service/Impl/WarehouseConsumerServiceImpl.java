package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.WarehouseConsumerService;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.WarehouseService;
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
    private WarehouseService warehouseService;
    private final WarehouseRepository warehouseRepository;

    @KafkaListener(topics = "orders", groupId = "warehouse-group")
    @Override
    public void consumeOrder(Order order) {
      log.info("kafka consumer received order:"+order);
            try{
                warehouseService.processOrder(order);
            }
            catch (Exception e){
                log.error("Exception while processing order "+e.getMessage());
                throw new RuntimeException(e.getMessage());
            }
    }
}
