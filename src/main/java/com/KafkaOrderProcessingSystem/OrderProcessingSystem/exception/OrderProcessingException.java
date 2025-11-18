package com.KafkaOrderProcessingSystem.OrderProcessingSystem.exception;

public class OrderProcessingException extends RuntimeException{
    public OrderProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
