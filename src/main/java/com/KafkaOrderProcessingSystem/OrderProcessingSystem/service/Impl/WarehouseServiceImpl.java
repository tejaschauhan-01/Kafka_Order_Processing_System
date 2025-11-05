package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.WarehouseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WarehouseServiceImpl implements WarehouseService {
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Override
    public void processOrder(Order order) {
        Optional<WarehouseStock>  stockOptional=warehouseRepository.findById(order.getProductName());
        if(stockOptional.isEmpty()){
            order.setStatus("FAILED");
            orderRepository.save(order);
            throw new RuntimeException("not able to find product"+order.getProductName());
//            return;
        }
        WarehouseStock stock = stockOptional.get();
        int remaining= stock.getAvailableQuantity()-order.getQuantity();
        if(remaining<0){
            order.setStatus("FAILED");
            orderRepository.save(order);
            throw new RuntimeException("Out of Stock");
//            return;
        }
        stock.setAvailableQuantity(remaining);
        warehouseRepository.save(stock);
        order.setStatus("PROCESSED");
        orderRepository.save(order);
    }
}
