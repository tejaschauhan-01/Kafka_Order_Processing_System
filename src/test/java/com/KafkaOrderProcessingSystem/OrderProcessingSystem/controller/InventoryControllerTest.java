package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.WarehouseStockDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InventoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(inventoryController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void AddInventory_SuccessTest() throws Exception {
        WarehouseStockDTO dto = new WarehouseStockDTO("Laptop", 10, null);
        WarehouseStock stock = new WarehouseStock("Laptop", 10);

        doNothing().when(inventoryService).addInventory(any(WarehouseStock.class));

        mockMvc.perform(post("/inventory/add_inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.message").value("Stock Added Succesfully"));
    }

    @Test
    void AddInventory_FailureTest() throws Exception {
        WarehouseStockDTO dto = new WarehouseStockDTO("Laptop", 10, null);

        doThrow(new IllegalArgumentException("Duplicate product")).when(inventoryService)
                .addInventory(any(WarehouseStock.class));

        mockMvc.perform(post("/inventory/add_inventory")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Duplicate product"));
    }

    @Test
    void GetInventory_SuccessTest() throws Exception {
        List<WarehouseStock> stocks = Arrays.asList(
                new WarehouseStock("Laptop", 10),
                new WarehouseStock("Mouse", 20)
        );

        when(inventoryService.getInventory()).thenReturn(stocks);

        mockMvc.perform(get("/inventory/getInventory"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productName").value("Laptop"))
                .andExpect(jsonPath("$[1].productName").value("Mouse"));
    }

    @Test
    void UpdateInventory_SuccessTest() throws Exception {
        WarehouseStockDTO dto = new WarehouseStockDTO("Laptop", 15, null);
        WarehouseStock updated = new WarehouseStock("Laptop", 15);

        when(inventoryService.updateInventory(eq("Laptop"), anyString(), anyInt()))
                .thenReturn(updated);

        mockMvc.perform(put("/inventory/update_inventory/Laptop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.availableQuantity").value(15))
                .andExpect(jsonPath("$.message").value("Inventory updated Succesfully"));
    }

    @Test
    void UpdateInventory_FailureTest() throws Exception {
        WarehouseStockDTO dto = new WarehouseStockDTO("Laptop", 15, null);

        when(inventoryService.updateInventory(eq("Laptop"), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Product not found"));

        mockMvc.perform(put("/inventory/update_inventory/Laptop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }
}
