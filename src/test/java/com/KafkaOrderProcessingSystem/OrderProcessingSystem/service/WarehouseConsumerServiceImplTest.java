package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.WarehouseConsumerServiceImpl;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.utils.WarehouseStockUpdate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseConsumerServiceImplTest {

    @Mock
    private WarehouseStockUpdate warehouseStockUpdate;

    @InjectMocks
    private WarehouseConsumerServiceImpl warehouseConsumerService;

    @Test
    void ConsumeOrder_ShouldCallWarehouseStockUpdateTest() {
        Order mockOrder = new Order();
        mockOrder.setOrderId("223");
        mockOrder.setProductName("Laptop");
        mockOrder.setQuantity(2);

        warehouseConsumerService.consumeOrder(mockOrder);

        verify(warehouseStockUpdate, times(1)).processOrder(mockOrder);
    }
}
