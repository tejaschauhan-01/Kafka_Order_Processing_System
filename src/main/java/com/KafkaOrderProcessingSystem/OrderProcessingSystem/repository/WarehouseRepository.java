package com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository;


import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WarehouseRepository extends MongoRepository<WarehouseStock, String> {
}
