package com.KafkaOrderProcessingSystem.OrderProcessingSystem.service;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.InventoryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class InventoryServiceImplTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private WarehouseStock productStock;

    @BeforeEach
    void setUp() {
        productStock = new WarehouseStock();
        productStock.setProductName("Laptop");
        productStock.setAvailableQuantity(10);
    }
    @Test
    void testAddInventory_NewProduct_Success() {
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.empty());
        inventoryService.addInventory(productStock);
        verify(warehouseRepository, times(1)).save(productStock);
    }
    @Test
    void testAddInventory_ProductAlreadyExists_ThrowsException() {
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(productStock));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> inventoryService.addInventory(productStock));

        assertEquals("The product name already exists", exception.getMessage());
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void testGetInventory_ReturnsAllStocks() {
        List<WarehouseStock> mockList = Arrays.asList(productStock);
        when(warehouseRepository.findAll()).thenReturn(mockList);

        Page<WarehouseStock> mockPage = new PageImpl<>(mockList, PageRequest.of(0, 10, Sort.by("productName")), mockList.size());

        when(warehouseRepository.findAll(any(Pageable.class))).thenReturn(mockPage);

        Page<WarehouseStock> result = inventoryService.getInventory(0, 10, "productName");

        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getProductName());
        assertEquals(10, result.getPageable().getPageSize());
        assertEquals(0, result.getPageable().getPageNumber());
        verify(warehouseRepository, times(1)).findAll(any(Pageable.class));
    }

    @Test
    void testUpdateInventory_Success() {
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.of(productStock));
        when(warehouseRepository.save(any(WarehouseStock.class))).thenAnswer(inv -> inv.getArgument(0));

        WarehouseStock updated = inventoryService.updateInventory("Laptop",  5);

        assertEquals(15, updated.getAvailableQuantity());
        verify(warehouseRepository, times(1)).findById("Laptop");
        verify(warehouseRepository, times(1)).save(productStock);
    }

    @Test
    void testUpdateInventory_ProductNotFound_ThrowsException() {
        when(warehouseRepository.findById("Laptop")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> inventoryService.updateInventory("Laptop",  5));

        assertTrue(exception.getMessage().contains("Product not found in inventory"));
        verify(warehouseRepository, never()).save(any());
    }
}
