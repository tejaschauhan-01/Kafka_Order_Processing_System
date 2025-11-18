package com.KafkaOrderProcessingSystem.OrderProcessingSystem.integration;

import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.Order;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.OrderStatus;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.entity.WarehouseStock;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.OrderRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.repository.WarehouseRepository;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.service.Impl.OrderProducerServiceImpl;
import com.KafkaOrderProcessingSystem.OrderProcessingSystem.utils.WarehouseStockUpdate;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {"orders"}, ports = {9092})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class KafkaIntegrationTest {

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private OrderProducerServiceImpl orderProducerService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private WarehouseStockUpdate warehouseStockUpdate;

    private KafkaMessageListenerContainer<String, Order> container;
    private BlockingQueue<ConsumerRecord<String, Order>> records;

    @BeforeEach
    void setUp() {
        // Set up Kafka consumer to listen to messages
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        configs.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "*");

        DefaultKafkaConsumerFactory<String, Order> consumerFactory =
            new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(),
                new JsonDeserializer<>(Order.class));

        ContainerProperties containerProperties = new ContainerProperties("orders");
        container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();

        container.setupMessageListener((MessageListener<String, Order>) records::add);
        container.start();

        ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());

        // Clean up repositories before each test
        orderRepository.deleteAll();
        warehouseRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        if (container != null) {
            container.stop();
        }
    }

    @Test
    void testOrderProducerConsumerFlow_Success() throws InterruptedException {
        // Given: A product with sufficient stock
        WarehouseStock stock = new WarehouseStock("Laptop", 10);
        warehouseRepository.save(stock);

        Order order = Order.builder()
                .orderId("TEST001")
                .productName("Laptop")
                .quantity(3)
                .status(OrderStatus.PENDING.name())
                .build();

        // When: Order is submitted
        orderProducerService.submitOrder(order);

        // Then: Order should be published to Kafka
        ConsumerRecord<String, Order> record = records.poll(10, TimeUnit.SECONDS);
        assertNotNull(record, "Kafka message should be received");
        assertEquals("TEST001", record.value().getOrderId());
        assertEquals("Laptop", record.value().getProductName());
        assertEquals(3, record.value().getQuantity());
        assertEquals(OrderStatus.PROCESSED.name(), record.value().getStatus());

        // Verify order is persisted in database
        Optional<Order> savedOrder = orderRepository.findById("TEST001");
        assertTrue(savedOrder.isPresent());
        assertEquals(OrderStatus.PROCESSED.name(), savedOrder.get().getStatus());
    }

    @Test
    void testOrderProducerFlow_OutOfStock() throws InterruptedException {
        // Given: A product with zero stock
        WarehouseStock stock = new WarehouseStock("Mouse", 0);
        warehouseRepository.save(stock);

        Order order = Order.builder()
                .orderId("TEST002")
                .productName("Mouse")
                .quantity(5)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Order submission should fail
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> orderProducerService.submitOrder(order));

        assertEquals("Out of Stock", exception.getMessage());

        // Verify order is marked as FAILED in database
        Optional<Order> savedOrder = orderRepository.findById("TEST002");
        assertTrue(savedOrder.isPresent());
        assertEquals(OrderStatus.FAILED.name(), savedOrder.get().getStatus());
    }

    @Test
    void testOrderProducerFlow_QuantityExceedsStock() {
        // Given: A product with limited stock
        WarehouseStock stock = new WarehouseStock("Keyboard", 5);
        warehouseRepository.save(stock);

        Order order = Order.builder()
                .orderId("TEST003")
                .productName("Keyboard")
                .quantity(10)
                .status(OrderStatus.PENDING.name())
                .build();

        // When & Then: Order submission should fail
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> orderProducerService.submitOrder(order));

        assertTrue(exception.getMessage().contains("Order Quantity exceeds available stock"));

        // Verify order is marked as FAILED
        Optional<Order> savedOrder = orderRepository.findById("TEST003");
        assertTrue(savedOrder.isPresent());
        assertEquals(OrderStatus.FAILED.name(), savedOrder.get().getStatus());
    }

    @Test
    void testConsumerUpdatesWarehouseStock() {
        WarehouseStock stock = new WarehouseStock("Monitor", 20);
        warehouseRepository.save(stock);

        Order order = Order.builder()
                .orderId("TEST004")
                .productName("Monitor")
                .quantity(5)
                .status(OrderStatus.PROCESSED.name())
                .build();

        warehouseStockUpdate.processOrder(order);

        Optional<WarehouseStock> updatedStock = warehouseRepository.findById("Monitor");
        assertTrue(updatedStock.isPresent());
        assertEquals(15, updatedStock.get().getAvailableQuantity(),
            "Stock should be reduced by order quantity");
    }

    @Test
    void testMultipleOrdersSequentially() throws InterruptedException {
        // Given: A product with sufficient stock
        WarehouseStock stock = new WarehouseStock("Tablet", 50);
        warehouseRepository.save(stock);

        for (int i = 1; i <= 3; i++) {
            Order order = Order.builder()
                    .orderId("TEST00" + (4 + i))
                    .productName("Tablet")
                    .quantity(5)
                    .status(OrderStatus.PENDING.name())
                    .build();
            orderProducerService.submitOrder(order);
        }

        for (int i = 0; i < 3; i++) {
            ConsumerRecord<String, Order> record = records.poll(10, TimeUnit.SECONDS);
            assertNotNull(record, "Message " + (i + 1) + " should be received");
            assertEquals(OrderStatus.PROCESSED.name(), record.value().getStatus());
        }

        assertEquals(3, orderRepository.count());
    }

    @Test
    void testOrderProducerFlow_ProductNotFound() {
        // Given: No product in warehouse
        Order order = Order.builder()
                .orderId("TEST008")
                .productName("NonExistentProduct")
                .quantity(1)
                .status(OrderStatus.PENDING.name())
                .build();

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> orderProducerService.submitOrder(order));

        assertEquals("Product not found", exception.getMessage());

        Optional<Order> savedOrder = orderRepository.findById("TEST008");
        assertFalse(savedOrder.isPresent());
    }
}

