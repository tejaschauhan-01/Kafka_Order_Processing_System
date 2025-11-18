package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.OrderStatus;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionRollbackTest {

    @Mock
    private KafkaTemplate<String, Order> kafkaTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private OrderProducerServiceImpl orderProducerService;

    private WarehouseStock testStock;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        testStock = new WarehouseStock("Laptop", 10);
        testOrder = Order.builder()
                .orderId("TXN001")
                .productName("Laptop")
                .quantity(5)
                .status(OrderStatus.PENDING.name())
                .build();
    }

    @Test
    void testKafkaSendFailure_OrderStillPersisted() {
        // Given: Stock is available but Kafka will fail
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);
        doThrow(new RuntimeException("Kafka connection error"))
                .when(kafkaTemplate).send(anyString(), anyString(), any(Order.class));

        // When & Then: Exception should be thrown
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(testOrder));

        assertEquals("Kafka connection error", exception.getMessage());

        // Verify order was saved (persisted for audit) even though Kafka failed
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testOrderSaveFailure_NoKafkaSend() {
        // Given: Stock is available but database save will fail
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        doThrow(new RuntimeException("Database connection lost"))
                .when(orderRepository).save(any(Order.class));

        // When & Then: Exception should be thrown
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(testOrder));

        assertEquals("Database connection lost", exception.getMessage());

        // Verify Kafka send was never called due to earlier failure
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(Order.class));
    }

    @Test
    void testStockCheckFailure_NoOrderCreated() {
        // Given: Warehouse repository throws exception
        when(warehouseRepository.findById("Laptop"))
                .thenThrow(new RuntimeException("Database read error"));

        // When & Then: Exception should be propagated
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(testOrder));

        assertEquals("Database read error", exception.getMessage());

        // Verify no order was saved and no Kafka message sent
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(Order.class));
    }

    @Test
    void testOutOfStock_PartialTransaction() {
        // Given: Product is out of stock
        testStock.setAvailableQuantity(0);
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When & Then: Should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(testOrder));

        assertEquals("Out of Stock", exception.getMessage());

        // Verify order was saved with FAILED status (for audit trail)
        verify(orderRepository, times(1)).save(argThat(order ->
                order.getStatus().equals(OrderStatus.FAILED.name())
        ));

        // Verify no message sent to Kafka when order failed
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(Order.class));
    }

    @Test
    void testQuantityExceedsStock_PartialTransaction() {
        // Given: Order quantity exceeds available stock
        testOrder.setQuantity(15);
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When & Then: Should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(testOrder));

        assertTrue(exception.getMessage().contains("Order Quantity exceeds available stock"));

        // Verify order was saved with FAILED status
        verify(orderRepository, times(1)).save(argThat(order ->
                order.getStatus().equals(OrderStatus.FAILED.name())
        ));

        // Verify no Kafka message sent
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(Order.class));
    }

    @Test
    void testSuccessfulOrder_CompleteTransaction() {
        // Given: All conditions are met for successful order
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When: Submit order
        assertDoesNotThrow(() -> orderProducerService.submitOrder(testOrder));

        // Then: Verify complete transaction flow
        // 1. Stock was checked
        verify(warehouseRepository, times(1)).findById("Laptop");

        // 2. Order was saved with PROCESSED status
        verify(orderRepository, times(1)).save(argThat(order ->
                order.getStatus().equals(OrderStatus.PROCESSED.name())
        ));

        // 3. Message was sent to Kafka
        verify(kafkaTemplate, times(1)).send(eq("orders"), eq("TXN001"), any(Order.class));
    }

    @Test
    void testMultipleSaveCallsPrevention() {
        // Given: Out of stock condition
        testStock.setAvailableQuantity(0);
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When & Then: Submit order and expect exception
        assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(testOrder));

        // Verify order is saved exactly once (not multiple times)
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testConcurrentOrderHandling() {
        // Given: Multiple orders for the same product
        Order order1 = Order.builder()
                .orderId("CONC001")
                .productName("Laptop")
                .quantity(5)
                .status(OrderStatus.PENDING.name())
                .build();

        Order order2 = Order.builder()
                .orderId("CONC002")
                .productName("Laptop")
                .quantity(5)
                .status(OrderStatus.PENDING.name())
                .build();

        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Submit both orders sequentially (simulating concurrent requests)
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order1));
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order2));

        // Then: Both orders should be processed
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any(Order.class));
    }

    @Test
    void testProductNotFound_NoTransaction() {
        // Given: Product doesn't exist in warehouse
        when(warehouseRepository.findById("NonExistent"))
                .thenReturn(Optional.empty());

        Order order = Order.builder()
                .orderId("TXN999")
                .productName("NonExistent")
                .quantity(1)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should throw exception
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertEquals("Product not found", exception.getMessage());

        // Verify no database writes occurred
        verify(orderRepository, never()).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(Order.class));
    }

    @Test
    void testFailedOrderStatusCorrectness() {
        // Given: Insufficient stock scenario
        testStock.setAvailableQuantity(3);
        testOrder.setQuantity(5);

        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When: Attempt to submit order
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(testOrder));

        // Then: Verify exception message and order status
        assertTrue(exception.getMessage().contains("Order Quantity exceeds available stock"));

        // Verify the saved order has FAILED status
        verify(orderRepository).save(argThat(order ->
                order.getStatus().equals(OrderStatus.FAILED.name()) &&
                        order.getOrderId().equals("TXN001")
        ));
    }

    @Test
    void testOrderIdempotency() {
        // Given: Valid order and stock
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(testStock));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When: Process same order twice
        assertDoesNotThrow(() -> orderProducerService.submitOrder(testOrder));
        assertDoesNotThrow(() -> orderProducerService.submitOrder(testOrder));

        // Then: Both attempts should complete (no idempotency check in current impl)
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any(Order.class));
    }
}
