# 🧠 Kafka Order Processing System (Spring Boot + Kafka + MongoDB + Docker)

## 🚀 Overview

This project is a **Spring Boot based system** demonstrating **event driven order processing** using **Apache Kafka (KRaft mode)** and **MongoDB**.

The system simulates an **e-commerce order pipeline**:
- The **Order Service** produces order events to a Kafka topic (`orders`).
- The **Warehouse Service** consumes those events to process inventory and update stock.
- Each order goes through status changes: `PENDING → PROCESSED`.

The entire system runs in **Docker containers** - no ZooKeeper required (Kafka KRaft mode).

---

## 🧩 Features

✅ Event-driven architecture using Apache Kafka  
✅ Kafka KRaft mode (no ZooKeeper dependency)  
✅ MongoDB for persistence of orders and stock  
✅ Real-time warehouse stock updates  
✅ Order status tracking (`PENDING`, `PROCESSED`, `FAILED`)  
✅ Clean DTO-based REST API design  
✅ Fully Dockerized setup (Kafka, MongoDB, Spring Boot)

---

## 🧰 Tech Stack

| Component | Technology                      |
|------------|---------------------------------|
| Language | Java 21                         |
| Framework | Spring Boot 3.x                 |
| Messaging | Apache Kafka 3.8.0 (KRaft mode) |
| Database | MongoDB 7.0                     |
| Build Tool | Maven                           |
| Containerization | Docker & Docker Compose         |

---

## 🧩 Project Structure Overview

The project follows a **modular layered architecture**, organized into the following components:

### **1. Controller Layer (`controller/`)**
Handles incoming REST API requests and delegates business logic to the service layer.
- **`OrderController`** - Exposes endpoints for placing orders and checking order status.
- `InventoryController` - Exposes endpoints for adding stock to the warehouse.

### **2. DTO Layer (`dto/`)**
Contains Data Transfer Objects used for communication between layers.
- `OrderRequestDTO` – Request payload for placing an order.
- `OrderResponseDTO` – Response structure for order details.
- `WarehouseStockDTO` – Represents stock update information.

### **3. Entity Layer (`entity/`)**
Defines domain entities and enums mapped to the database.
- `Order`, `WarehouseStock`.

### **4. Repository Layer (`repository/`)**
Manages database access using Spring Data JPA.
- `OrderRepository`, `WarehouseRepository`.

### **5. Service Layer (`service/Impl/`)**
Contains the core business logic and Kafka message handling.
- `InventoryServiceImpl` – Interacts to perform crud operations in warehouse.
- `OrderProducerServiceImpl` – Sends order events to Kafka topics.
- `WarehouseConsumerServiceImpl` – Listens to stock updates from Kafka.

### **6. Configuration & Bootstrapping**
- `KafkaOrderProcessingSystemApplication.java` – Main entry point for Spring Boot.
- `application.yml` – Contains Kafka, database, and application configurations.

---

## 🐳 Running the Application with Docker

### 1️⃣ Build and Start Containers
```bash
docker-compose build
docker-compose up -d

```

--- 

### 2️⃣ Verify Services

Spring Boot App → http://localhost:8080

Kafka Broker → http://localhost:9092

Kafka UI → http://localhost:8081

MongoDB → http://localhost:27017