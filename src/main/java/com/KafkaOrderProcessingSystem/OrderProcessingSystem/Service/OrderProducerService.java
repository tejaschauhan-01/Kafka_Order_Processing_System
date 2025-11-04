package com.KafkaOrderProcessingSystem.OrderProcessingSystem.Service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.Entity.Order;

public interface OrderProducerService {

    void submitOrder(Order order);

}
