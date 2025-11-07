package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.utils.WarehouseStockUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.NoSuchElementException;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WarehouseStockUpdateTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseStockUpdate warehouseStockUpdate;


    @Test
    void ProcessOrder_ShouldUpdateStockAndSaveTest() {

        Order order = new Order();
        order.setProductName("Laptop");
        order.setQuantity(2);

        WarehouseStock existingStock = new WarehouseStock();
        existingStock.setProductName("Laptop");
        existingStock.setAvailableQuantity(10);

        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(existingStock));

        warehouseStockUpdate.processOrder(order);

        assertEquals(8, existingStock.getAvailableQuantity());
        verify(warehouseRepository, times(1)).save(existingStock);
    }

    @Test
    void ProcessOrder_StockNotFound_ShouldThrowExceptionTest() {

        Order order = new Order();
        order.setProductName("Mouse");
        order.setQuantity(1);

        when(warehouseRepository.findById("Mouse")).thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> warehouseStockUpdate.processOrder(order)
        );

        verify(warehouseRepository, never()).save(any());
    }
}
