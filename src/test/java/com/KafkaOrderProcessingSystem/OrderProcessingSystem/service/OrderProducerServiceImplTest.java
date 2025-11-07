package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.OrderProducerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class OrderProducerServiceImplTest {

    @Mock
    private KafkaTemplate<String, Order> kafkaTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private OrderProducerServiceImpl orderProducerService;

    private Order order;
    private WarehouseStock warehouseStock;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId("1001");
        order.setProductName("ProductA");
        order.setQuantity(10);

        warehouseStock = new WarehouseStock();
        warehouseStock.setProductName("ProductA");
        warehouseStock.setAvailableQuantity(10);
    }

    @Test
    void testSubmitOrder_Success() {

        when(warehouseRepository.findById(order.getProductName())).thenReturn(Optional.of(warehouseStock));

        orderProducerService.submitOrder(order);
        assertEquals("PROCESSED", order.getStatus());
        verify(orderRepository, times(1)).save(order);
        verify(kafkaTemplate, times(1)).send("orders", order.getOrderId(), order);
    }

    @Test
    void testSubmitOrder_OutOfStock() {
        warehouseStock.setAvailableQuantity(0);
        when(warehouseRepository.findById(order.getProductName()))
                .thenReturn(Optional.of(warehouseStock));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertEquals("Out of Stock", exception.getMessage());
        verify(orderRepository, times(1)).save(order);
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void testSubmitOrder_ProductNotFound() {
        when(warehouseRepository.findById(order.getProductName()))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertEquals("Product not found", exception.getMessage());
        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void testSubmitOrder_QuantityExceedsStock() {
        warehouseStock.setAvailableQuantity(3);
        when(warehouseRepository.findById(order.getProductName()))
                .thenReturn(Optional.of(warehouseStock));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertEquals("Order Quantity exceeds available stock: 3", exception.getMessage());
        verify(orderRepository, times(1)).save(order);
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    void testSubmitOrder_KafkaSendFailure() {
        when(warehouseRepository.findById(order.getProductName()))
                .thenReturn(Optional.of(warehouseStock));

        doThrow(new RuntimeException("Kafka error"))
                .when(kafkaTemplate).send(anyString(), anyString(), any(Order.class));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertEquals("Kafka error", exception.getMessage());
        verify(orderRepository, times(1)).save(order);
    }


}
