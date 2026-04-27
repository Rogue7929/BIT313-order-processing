# BIT313 Phase 2 — Virtual Threads & REST API Implementation

## Architecture Overview

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT / STRESS TEST                 │
└─────────────────────┬───────────────────────────────────┘
                      │ POST /api/orders/process
                      ▼
┌─────────────────────────────────────────────────────────┐
│              ORDER SERVICE  :8080                       │
│   VirtualThreadConfig → Executors.newVirtualThread...   │
│                                                         │
│   OrderController → OrderService                        │
│        │                    │                           │
│        │ callWithRetry()    │ callWithRetry()           │
│        ▼                    ▼                           │
│  POST :8081/check    POST :8082/process                 │
└────────┬────────────────────┬────────────────────────── ┘
         │                    │
         ▼                    ▼
┌─────────────────┐  ┌─────────────────────┐
│ INVENTORY SVC   │  │   PAYMENT SERVICE   │
│    :8081        │  │       :8082         │
│                 │  │                     │
│ - Stock check   │  │ - Charge customer   │
│ - 50ms latency  │  │ - 100ms latency     │
│ - VirtualThread │  │ - 5% failure rate   │
└─────────────────┘  └─────────────────────┘
```

## How to Run

### Prerequisites
- Java 21 installed (`java -version` should show 21)
- Maven installed (`mvn -version`)
- VS Code with Extension Pack for Java

### Step 1: Start All Three Services

Open **three separate terminals** in VS Code and run:

**Terminal 1 — Inventory Service**
```bash
cd inventory-service
mvn spring-boot:run
# Starts on http://localhost:8081
```

**Terminal 2 — Payment Service**
```bash
cd payment-service
mvn spring-boot:run
# Starts on http://localhost:8082
```

**Terminal 3 — Order Service**
```bash
cd order-service
mvn spring-boot:run
# Starts on http://localhost:8080
```

Wait until all three show: `Started [ServiceName] in X seconds`

---

### Step 2: Test a Single Order

Use any REST client (Postman, curl, or VS Code REST Client):

```bash
curl -X POST http://localhost:8080/api/orders/process \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": "ORD-001",
    "productId": "PROD-1",
    "quantity": 2,
    "totalPrice": 49.99
  }'
```

**Expected Response:**
```json
{
  "orderId": "ORD-001",
  "productId": "PROD-1",
  "quantity": 2,
  "totalPrice": 49.99,
  "status": "CONFIRMED"
}
```

---

### Step 3: Health Checks

Verify all services are running:
```bash
curl http://localhost:8081/api/inventory/health
curl http://localhost:8082/api/payment/health
```

---

### Step 4: Run the Phase 3 Stress Test

```bash
curl -X POST "http://localhost:8080/api/orders/stress-test?count=5000"
```

**Expected Response (example):**
```json
{
  "totalOrders": 5000,
  "confirmed": 4748,
  "failed": 252,
  "executionTimeMs": 3241,
  "throughputPerSecond": 1542.7
}
```

Record these numbers for your Phase 3 benchmarking report.

---

## Key Concepts Implemented

### 1. Virtual Thread Executor (Rubric Requirement)
```java
// In VirtualThreadConfig.java
protocolHandler.setExecutor(
    Executors.newVirtualThreadPerTaskExecutor()
);
```
Every HTTP request to every service runs on its own virtual thread.

### 2. Thread-per-Request Blocking Style (Rubric Requirement)
```java
// Simple, readable blocking code — no callbacks, no reactive operators
ServiceResponse response = restTemplate.postForObject(url, order, String.class);
```
This is the "simplified, readable blocking style" the rubric asks for.

### 3. Retry Logic with Timeouts (Rubric Requirement)
- Connect timeout: 3 seconds (RestTemplateConfig)
- Read timeout: 5 seconds (RestTemplateConfig)
- Max retries: 3 attempts per service call
- Retry delay: 1 second between attempts

### 4. Simulated Network Latency
- Inventory Service: 50ms delay (simulates DB query)
- Payment Service: 100ms delay (simulates payment gateway)
- Payment failure rate: 5% (simulates declined payments)

---

## Project File Structure

```
BIT313-Phase2/
├── order-service/
│   ├── pom.xml
│   └── src/main/java/com/bit313/
│       ├── OrderServiceApplication.java     ← Main class
│       ├── config/
│       │   ├── VirtualThreadConfig.java     ← Enables virtual threads
│       │   └── RestTemplateConfig.java      ← HTTP client with timeouts
│       ├── controller/
│       │   └── OrderController.java         ← REST endpoints + stress test
│       ├── service/
│       │   └── OrderService.java            ← Core logic + retry
│       └── model/
│           ├── Order.java
│           └── ServiceResponse.java
│
├── inventory-service/
│   ├── pom.xml
│   └── src/main/java/com/bit313/
│       ├── InventoryServiceApplication.java
│       ├── controller/InventoryController.java
│       ├── service/InventoryService.java     ← Stock check + 50ms latency
│       └── model/
│
└── payment-service/
    ├── pom.xml
    └── src/main/java/com/bit313/
        ├── PaymentServiceApplication.java
        ├── controller/PaymentController.java
        ├── service/PaymentService.java       ← Payment + 100ms latency + 5% failure
        └── model/
```

---

## Phase 3 — Data to Collect

When running the stress test, record:
1. `executionTimeMs` — total time for all orders
2. `throughputPerSecond` — orders processed per second  
3. `confirmed` / `failed` — success rate
4. CPU usage — check Task Manager during the test
5. Memory usage — check Task Manager during the test
6. Thread count — check with: `jcmd <pid> Thread.print | grep "virtual" | wc -l`

Compare these numbers directly against Member A's Reactive implementation results.
