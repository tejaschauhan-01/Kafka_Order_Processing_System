package com.KafkaOrderProcessingSystem.OrderProcessingSystem.Repository;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.Entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {

}
