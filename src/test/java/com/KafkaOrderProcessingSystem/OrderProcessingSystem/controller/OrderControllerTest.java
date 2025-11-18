package com.KafkaOrderProcessingSystem.OrderProcessingSystem.controller;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.dto.OrderRequestDTO;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.OrderProducerServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderProducerServiceImpl orderProducerService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateOrder_Success() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO("O1", "Laptop", 5, "PENDING");
        Order order = new Order("O1", "Laptop", 5, "PROCESSED");

        Mockito.doNothing().when(orderProducerService).submitOrder(any(Order.class));
        Mockito.when(orderRepository.findById("O1")).thenReturn(Optional.of(order));

        mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId", is("O1")))
                .andExpect(jsonPath("$.productName", is("Laptop")))
                .andExpect(jsonPath("$.quantity", is(5)))
                .andExpect(jsonPath("$.status", is("PROCESSED")))
                .andExpect(jsonPath("$.message", is("Order submitted successfully and queued for processing")));
    }

    @Test
    void testCreateOrder_InvalidData() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO("O2", "Phone", 10, "PENDING");

        Mockito.doThrow(new IllegalArgumentException("Invalid product name"))
                .when(orderProducerService).submitOrder(any(Order.class));

        mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid product name"));

    }

    @Test
    void testCreateOrder_RuntimeException() throws Exception {
        OrderRequestDTO requestDTO = new OrderRequestDTO("O3", "Tablet", 2, "PENDING");

        Mockito.doThrow(new RuntimeException("Out of stock"))
                .when(orderProducerService).submitOrder(any(Order.class));

        mockMvc.perform(post("/orders/create_order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Out of stock"));
    }
}
