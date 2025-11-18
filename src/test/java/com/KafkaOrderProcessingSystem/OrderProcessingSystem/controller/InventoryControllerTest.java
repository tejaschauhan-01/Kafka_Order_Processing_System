package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.GlobalExceptionHandler;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.WarehouseStockDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InventoryController.class)
@Import(GlobalExceptionHandler.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void AddInventory_SuccessTest() throws Exception {
        // Given: DTO for request (what controller expects)
        WarehouseStockDTO dto = new WarehouseStockDTO("Laptop", 10, null);

        // When: Service is called, it should succeed
        doNothing().when(inventoryService).addInventory(any(WarehouseStock.class));

        // Then: Request should return 201 CREATED with success response
        mockMvc.perform(post("/inventory/add_stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))  // Send DTO, not entity
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.availableQuantity").value(10))
                .andExpect(jsonPath("$.message").value("Stock added successfully"));
    }

    @Test
    void AddInventory_FailureTest() throws Exception {
        WarehouseStockDTO dto = new WarehouseStockDTO("Laptop", 10, null);

        doThrow(new RuntimeException("Duplicate product")).when(inventoryService)
                .addInventory(any(WarehouseStock.class));

        mockMvc.perform(post("/inventory/add_stock")
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

        Page<WarehouseStock> pageResponse = new PageImpl<>(stocks);

        when(inventoryService.getInventory(0,10, "productName")).thenReturn(pageResponse);

        mockMvc.perform(get("/inventory/stock_list")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "productName"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].productName").value("Laptop"))
                .andExpect(jsonPath("$.content[1].productName").value("Mouse"))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void UpdateInventory_SuccessTest() throws Exception {
        WarehouseStockDTO dto = new WarehouseStockDTO("Laptop", 15, null);
        WarehouseStock updated = new WarehouseStock("Laptop", 15);

        when(inventoryService.updateInventory(eq("Laptop"), anyInt()))
                .thenReturn(updated);

        mockMvc.perform(put("/inventory/update_stock/Laptop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Laptop"))
                .andExpect(jsonPath("$.availableQuantity").value(15))
                .andExpect(jsonPath("$.message").value("Stock updated successfully"));
    }

    @Test
    void UpdateInventory_FailureTest() throws Exception {
        WarehouseStockDTO dto = new WarehouseStockDTO("Laptop", 15, null);

        when(inventoryService.updateInventory(eq("Laptop"),  anyInt()))
                .thenThrow(new IllegalArgumentException("Product not found"));

        mockMvc.perform(put("/inventory/update_stock/Laptop")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Product not found"));
    }
}
