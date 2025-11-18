package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.OrderStatus;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.InventoryServiceImpl;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.OrderProducerServiceImpl;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.utils.WarehouseStockUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Edge Case Testing for Order Processing System
 *
 * Industry Practice: Edge cases represent boundary conditions and unusual scenarios
 * that may not occur frequently but can cause system failures if not handled properly.
 * Testing these scenarios ensures system robustness and reliability.
 */
@ExtendWith(MockitoExtension.class)
class EdgeCaseTest {

    @Mock
    private KafkaTemplate<String, Order> kafkaTemplate;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private OrderProducerServiceImpl orderProducerService;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @InjectMocks
    private WarehouseStockUpdate warehouseStockUpdate;

    /**
     * Test Case: Order with zero quantity
     * Edge Case: Validates system behavior when order quantity is 0
     */
    @Test
    void testSubmitOrder_WithZeroQuantity() {
        // Given: Product exists with stock
        WarehouseStock stock = new WarehouseStock("Laptop", 10);
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(stock));

        Order order = Order.builder()
                .orderId("EDGE001")
                .productName("Laptop")
                .quantity(0)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should process successfully as quantity doesn't exceed stock
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(Order.class));
    }

    /**
     * Test Case: Order with negative quantity
     * Edge Case: System should handle negative quantities gracefully
     */
    @Test
    void testSubmitOrder_WithNegativeQuantity() {
        // Given: Product exists with stock
        WarehouseStock stock = new WarehouseStock("Mouse", 5);
        when(warehouseRepository.findById("Mouse")).thenReturn(Optional.of(stock));

        Order order = Order.builder()
                .orderId("EDGE002")
                .productName("Mouse")
                .quantity(-5)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Negative quantity doesn't exceed stock, so it processes
        // Note: In production, validation layer should prevent negative quantities
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order));

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    /**
     * Test Case: Very large order quantity (Integer.MAX_VALUE)
     * Edge Case: Tests boundary of integer range
     */
    @Test
    void testSubmitOrder_WithMaxIntegerQuantity() {
        // Given: Product with limited stock
        WarehouseStock stock = new WarehouseStock("Keyboard", 100);
        when(warehouseRepository.findById("Keyboard")).thenReturn(Optional.of(stock));

        Order order = Order.builder()
                .orderId("EDGE003")
                .productName("Keyboard")
                .quantity(Integer.MAX_VALUE)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should fail as quantity exceeds available stock
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertTrue(exception.getMessage().contains("Order Quantity exceeds available stock"));
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any(Order.class));
    }

    /**
     * Test Case: Order with null or empty product name
     * Edge Case: Tests handling of invalid product identifiers
     */
    @Test
    void testSubmitOrder_WithNullProductName() {
        // Given: Order with null product name
        Order order = Order.builder()
                .orderId("EDGE004")
                .productName(null)
                .quantity(5)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should throw exception
        when(warehouseRepository.findById(null)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertEquals("Product not found", exception.getMessage());
    }

    /**
     * Test Case: Order with empty string product name
     * Edge Case: Tests validation of empty strings
     */
    @Test
    void testSubmitOrder_WithEmptyProductName() {
        // Given: Order with empty product name
        Order order = Order.builder()
                .orderId("EDGE005")
                .productName("")
                .quantity(5)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should throw exception
        when(warehouseRepository.findById("")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertEquals("Product not found", exception.getMessage());
    }

    /**
     * Test Case: Order with very long product name
     * Edge Case: Tests system with unusually long strings
     */
    @Test
    void testSubmitOrder_WithVeryLongProductName() {
        // Given: Product name with 1000 characters
        String longProductName = "A".repeat(1000);
        WarehouseStock stock = new WarehouseStock(longProductName, 10);

        when(warehouseRepository.findById(longProductName)).thenReturn(Optional.of(stock));

        Order order = Order.builder()
                .orderId("EDGE006")
                .productName(longProductName)
                .quantity(1)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should process successfully
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(Order.class));
    }

    /**
     * Test Case: Stock with exactly matching order quantity
     * Edge Case: Boundary test where stock equals order quantity
     */
    @Test
    void testSubmitOrder_ExactStockMatch() {
        // Given: Stock quantity exactly matches order quantity
        WarehouseStock stock = new WarehouseStock("Monitor", 10);
        when(warehouseRepository.findById("Monitor")).thenReturn(Optional.of(stock));

        Order order = Order.builder()
                .orderId("EDGE007")
                .productName("Monitor")
                .quantity(10)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should process successfully
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(Order.class));
    }

    /**
     * Test Case: Stock with quantity one more than order
     * Edge Case: Boundary test with minimal remaining stock
     */
    @Test
    void testSubmitOrder_StockOneMoreThanOrder() {
        // Given: Stock is one unit more than order
        WarehouseStock stock = new WarehouseStock("Tablet", 11);
        when(warehouseRepository.findById("Tablet")).thenReturn(Optional.of(stock));

        Order order = Order.builder()
                .orderId("EDGE008")
                .productName("Tablet")
                .quantity(10)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should process successfully
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order));

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaTemplate, times(1)).send(anyString(), anyString(), any(Order.class));
    }

    /**
     * Test Case: Stock with quantity one less than order
     * Edge Case: Boundary test where order just exceeds stock
     */
    @Test
    void testSubmitOrder_StockOneLessThanOrder() {
        // Given: Stock is one unit less than order
        WarehouseStock stock = new WarehouseStock("Phone", 9);
        when(warehouseRepository.findById("Phone")).thenReturn(Optional.of(stock));

        Order order = Order.builder()
                .orderId("EDGE009")
                .productName("Phone")
                .quantity(10)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should fail
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderProducerService.submitOrder(order));

        assertTrue(exception.getMessage().contains("Order Quantity exceeds available stock"));
    }

    /**
     * Test Case: Update warehouse stock resulting in zero
     * Edge Case: Stock reduction leaves exactly zero items
     */
    @Test
    void testWarehouseStockUpdate_ResultsInZeroStock() {
        // Given: Stock will be reduced to zero
        WarehouseStock stock = new WarehouseStock("Camera", 5);
        when(warehouseRepository.findById("Camera")).thenReturn(Optional.of(stock));
        when(warehouseRepository.save(any(WarehouseStock.class))).thenReturn(stock);

        Order order = Order.builder()
                .orderId("EDGE010")
                .productName("Camera")
                .quantity(5)
                .status(OrderStatus.PROCESSED.name())
                .build();

        // When: Process order
        warehouseStockUpdate.processOrder(order);

        // Then: Stock should be zero
        assertEquals(0, stock.getAvailableQuantity());
        verify(warehouseRepository, times(1)).save(stock);
    }

    /**
     * Test Case: Update warehouse stock resulting in negative (system error)
     * Edge Case: Tests data inconsistency scenario
     */
    @Test
    void testWarehouseStockUpdate_ResultsInNegativeStock() {
        // Given: Order quantity exceeds stock (data inconsistency)
        WarehouseStock stock = new WarehouseStock("Headphones", 3);
        when(warehouseRepository.findById("Headphones")).thenReturn(Optional.of(stock));
        when(warehouseRepository.save(any(WarehouseStock.class))).thenReturn(stock);

        Order order = Order.builder()
                .orderId("EDGE011")
                .productName("Headphones")
                .quantity(5)
                .status(OrderStatus.PROCESSED.name())
                .build();

        // When: Process order (shouldn't happen in production but tests robustness)
        warehouseStockUpdate.processOrder(order);

        // Then: Stock becomes negative (indicates need for better validation)
        assertEquals(-2, stock.getAvailableQuantity());
        verify(warehouseRepository, times(1)).save(stock);
    }

    /**
     * Test Case: Add inventory with zero quantity
     * Edge Case: Adding product with no stock
     */
    @Test
    void testAddInventory_WithZeroQuantity() {
        // Given: New product with zero quantity
        WarehouseStock stock = new WarehouseStock("NewProduct", 0);
        when(warehouseRepository.findById("NewProduct")).thenReturn(Optional.empty());

        // When: Add inventory
        assertDoesNotThrow(() -> inventoryService.addInventory(stock));

        // Then: Should save successfully
        verify(warehouseRepository, times(1)).save(stock);
    }

    /**
     * Test Case: Update inventory with negative quantity change
     * Edge Case: Tests if update can handle negative adjustments
     */
    @Test
    void testUpdateInventory_WithNegativeQuantityChange() {
        // Given: Existing product
        WarehouseStock existingStock = new WarehouseStock("ExistingProduct", 20);
        when(warehouseRepository.findById("ExistingProduct")).thenReturn(Optional.of(existingStock));
        when(warehouseRepository.save(any(WarehouseStock.class))).thenReturn(existingStock);

        // When: Update with negative quantity (reduction)
        WarehouseStock updated = inventoryService.updateInventory("ExistingProduct", -5);

        // Then: Quantity should be reduced
        assertEquals(15, updated.getAvailableQuantity());
        verify(warehouseRepository, times(1)).save(existingStock);
    }

    /**
     * Test Case: Product name with special characters
     * Edge Case: Tests handling of special characters in identifiers
     */
    @Test
    void testSubmitOrder_WithSpecialCharactersInProductName() {
        // Given: Product name with special characters
        String specialProductName = "Product@#$%^&*()_+-={}[]|:;\"'<>,.?/~`";
        WarehouseStock stock = new WarehouseStock(specialProductName, 10);

        when(warehouseRepository.findById(specialProductName)).thenReturn(Optional.of(stock));

        Order order = Order.builder()
                .orderId("EDGE012")
                .productName(specialProductName)
                .quantity(1)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Should process successfully
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order));

        verify(orderRepository, times(1)).save(any(Order.class));
    }

    /**
     * Test Case: Duplicate order ID submission
     * Edge Case: Tests idempotency concern
     */
    @Test
    void testSubmitOrder_DuplicateOrderId() {
        // Given: Product with sufficient stock
        WarehouseStock stock = new WarehouseStock("Printer", 10);
        when(warehouseRepository.findById("Printer")).thenReturn(Optional.of(stock));

        Order order1 = Order.builder()
                .orderId("DUP001")
                .productName("Printer")
                .quantity(2)
                .status(OrderStatus.PENDING.name())
                .build();

        Order order2 = Order.builder()
                .orderId("DUP001") // Same ID
                .productName("Printer")
                .quantity(3)
                .status(OrderStatus.PENDING.name())
                .build();

        // When: Submit both orders
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order1));
        assertDoesNotThrow(() -> orderProducerService.submitOrder(order2));

        // Then: Both should be processed (MongoDB will overwrite)
        verify(orderRepository, times(2)).save(any(Order.class));
        verify(kafkaTemplate, times(2)).send(anyString(), anyString(), any(Order.class));
    }
}

