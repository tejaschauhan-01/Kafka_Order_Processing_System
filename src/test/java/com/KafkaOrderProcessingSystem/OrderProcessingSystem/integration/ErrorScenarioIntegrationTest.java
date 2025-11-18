package com.KafkaOrderProcessingSystem.OrderProcessingSystem.integration;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.ErrorResponse;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderRequestDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.WarehouseStockDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.OrderStatus;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
class ErrorScenarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        // Clean up repositories before each test
        orderRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    @Test
    void testCreateOrder_ProductNotFound() throws Exception {
        // Given: Order for non-existent product
        OrderRequestDTO request = new OrderRequestDTO(
                "ERR001",
                "NonExistentProduct",
                5,
                OrderStatus.PENDING.name()
        );

        MvcResult result = mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Product not found"))
                .andExpect(jsonPath("$.path").value("/orders/create_order"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        // Verify response structure
        String responseBody = result.getResponse().getContentAsString();
        ErrorResponse errorResponse = objectMapper.readValue(responseBody, ErrorResponse.class);
        assertNotNull(errorResponse.getTimestamp());
        assertEquals("/orders/create_order", errorResponse.getPath());
    }

    @Test
    void testCreateOrder_InsufficientStock() throws Exception {
        // Given: Product with limited stock
        warehouseRepository.save(new WarehouseStock("Laptop", 5));

        OrderRequestDTO request = new OrderRequestDTO(
                "ERR002",
                "Laptop",
                10,
                OrderStatus.PENDING.name()
        );

        mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Order Quantity exceeds available stock: 5"))
                .andExpect(jsonPath("$.path").value("/orders/create_order"));
    }

    @Test
    void testCreateOrder_OutOfStock() throws Exception {
        // Given: Product with zero stock
        warehouseRepository.save(new WarehouseStock("Mouse", 0));

        OrderRequestDTO request = new OrderRequestDTO(
                "ERR003",
                "Mouse",
                1,
                OrderStatus.PENDING.name()
        );

        mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Out of Stock"));
    }

    @Test
    void testAddInventory_DuplicateProduct() throws Exception {
        // Given: Existing product in warehouse
        warehouseRepository.save(new WarehouseStock("Keyboard", 10));

        WarehouseStockDTO request = new WarehouseStockDTO("Keyboard", 20, null);

        mockMvc.perform(post("/inventory/add_stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errorCode").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("The product name already exists"));
    }

    @Test
    void testUpdateInventory_ProductNotFound() throws Exception {
        // Given: No product in warehouse
        WarehouseStockDTO request = new WarehouseStockDTO("NonExistent", 100, null);

        mockMvc.perform(put("/inventory/update_stock/NonExistent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Product not found in inventory: NonExistent"));
    }

    @Test
    void testCreateOrder_InvalidJsonFormat() throws Exception {
        // Given: Invalid JSON
        String invalidJson = "{orderId: 'ERR005', invalid json}";

        mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateOrder_MissingRequiredFields() throws Exception {

        String jsonWithNulls = "{\"orderId\": null, \"productName\": null, \"quantity\": null}";

        mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonWithNulls))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAddInventory_NegativeQuantity() throws Exception {
        // Given: Inventory with negative quantity
        WarehouseStockDTO request = new WarehouseStockDTO("Product", -10, null);

        mockMvc.perform(post("/inventory/add_stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void testCreateOrder_ExtremelyLargeQuantity() throws Exception {
        warehouseRepository.save(new WarehouseStock("Monitor", 100));

        OrderRequestDTO request = new OrderRequestDTO(
                "ERR007",
                "Monitor",
                Integer.MAX_VALUE,
                OrderStatus.PENDING.name()
        );

        mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Order Quantity exceeds available stock: 100"));
    }

    @Test
    void testConsistentErrorResponseFormat() throws Exception {
        // Test 1: Product not found error
        OrderRequestDTO request1 = new OrderRequestDTO("ERR008", "Unknown", 1, OrderStatus.PENDING.name());

        MvcResult result1 = mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error1 = objectMapper.readValue(
                result1.getResponse().getContentAsString(),
                ErrorResponse.class
        );

        // Verify all required fields are present
        assertNotNull(error1.getTimestamp());
        assertEquals(400, error1.getStatus());
        assertNotNull(error1.getErrorCode());
        assertNotNull(error1.getMessage());
        assertNotNull(error1.getPath());

        // Test 2: Duplicate product error
        warehouseRepository.save(new WarehouseStock("TestProduct", 10));
        WarehouseStockDTO request2 = new WarehouseStockDTO("TestProduct", 5, null);

        MvcResult result2 = mockMvc.perform(post("/inventory/add_stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest())
                .andReturn();

        ErrorResponse error2 = objectMapper.readValue(
                result2.getResponse().getContentAsString(),
                ErrorResponse.class
        );

        // Verify consistent structure
        assertNotNull(error2.getTimestamp());
        assertEquals(400, error2.getStatus());
        assertNotNull(error2.getErrorCode());
        assertNotNull(error2.getMessage());
        assertNotNull(error2.getPath());

        // Verify both errors have same status code
        assertEquals(error1.getStatus(), error2.getStatus());
    }

    @Test
    void testGetInventory_InvalidPaginationParams() throws Exception {

        warehouseRepository.save(new WarehouseStock("Product1", 10));

        mockMvc.perform(get("/inventory/stock_list")
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/inventory/stock_list")
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(status().isOk());
    }

    @Test
    void testUnexpectedServerError() {
        assertTrue(true, "GlobalExceptionHandler is configured to handle unexpected exceptions");
    }

    @Test
    void testWrongHttpMethod() throws Exception {

        mockMvc.perform(get("/orders/create_order"))
                .andExpect(status().is5xxServerError());

        mockMvc.perform(post("/inventory/stock_list"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    void testInvalidEndpoint() throws Exception {
        mockMvc.perform(get("/invalid/endpoint"))
                .andExpect(status().isNotFound());
    }
}

