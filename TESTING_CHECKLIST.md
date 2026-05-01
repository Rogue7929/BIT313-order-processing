# TESTING CHECKLIST - Phase 1 Reactive Implementation

## Pre-Testing

- [ ] Java 21 installed
- [ ] Maven 3.8+ installed
- [ ] Project cloned/extracted
- [ ] Network connection available (for API testing)

---

## Build Verification

### Step 1: Build Project
```bash
mvn clean package
```

**Expected Output**:
```
[INFO] Building jar: target/reactive-orders-1.0.0.jar
[INFO] BUILD SUCCESS
```

**Checks**:
- [ ] No compilation errors
- [ ] JAR file created (~50MB)
- [ ] All dependencies downloaded

---

## Unit Tests - API Endpoints

### Health Check Endpoint

**Request**:
```bash
curl -X GET http://localhost:8080/api/orders/health
```

**Expected Response**:
```
HTTP 200 OK
Body: "Reactive Order Service is running!"
```

**Checks**:
- [ ] Server responds within 1 second
- [ ] Status code is 200
- [ ] Response text is exactly as shown

---

## Functional Tests - Order Processing

### Test 1: Successful Order

**Request**:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"Mouse","quantity":1,"price":25.00}'
```

**Expected Response** (HTTP 200):
```json
{
  "orderId": "<UUID>",
  "status": "SUCCESS",
  "message": "Order processed successfully",
  "processingTimeMs": 150,
  "totalPrice": 25.00
}
```

**Checks**:
- [ ] Status is "SUCCESS"
- [ ] Processing time is 150-500ms (realistic latency)
- [ ] Total price = quantity × price
- [ ] OrderId is non-null UUID format

---

### Test 2: Out of Stock

**Request** (attempt to buy more than available inventory):
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"Laptop","quantity":1000,"price":3500}'
```

**Expected Response** (HTTP 400):
```json
{
  "orderId": "<UUID>",
  "status": "OUT_OF_STOCK",
  "message": "Insufficient stock for Laptop",
  "processingTimeMs": 100,
  "totalPrice": 3500000
}
```

**Checks**:
- [ ] Status is "OUT_OF_STOCK"
- [ ] Processing time < 200ms (fast failure)
- [ ] OrderId generated but no stock reserved
- [ ] Inventory not modified

---

### Test 3: Stock Depletion

**Scenario**: Exhaust laptop inventory (initially 500)

**Commands**:
```bash
# Run 500 successful orders for laptops
for i in {1..500}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{"productId":"Laptop","quantity":1,"price":3500}' &
done
wait

# Next request should fail
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"Laptop","quantity":1,"price":3500}'
```

**Expected Result**:
- [ ] First 500 orders: SUCCESS
- [ ] Order 501+: OUT_OF_STOCK
- [ ] Inventory atomically prevents over-selling

---

### Test 4: Invalid Request

**Request** (missing required field):
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"Laptop"}'
```

**Expected Response** (HTTP 400):
```json
{
  "orderId": "<UUID>",
  "status": "ERROR",
  "message": "Invalid order request",
  "processingTimeMs": 10
}
```

**Checks**:
- [ ] Status is "ERROR"
- [ ] Processing time < 100ms (immediate validation)
- [ ] Response sent without processing order

---

## Reactive Pattern Tests

### Test 5: Timeout Handling

**Mechanism**: PaymentService has 5% timeout failure rate

**Expected** (across many requests):
- [ ] ~5% of orders show FAILED_PAYMENT with timeout message
- [ ] System recovers and processes next order
- [ ] No cascading failures

**Verify**:
```bash
# Run 100 requests and check for timeouts
for i in {1..100}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d "{\"productId\":\"Mouse\",\"quantity\":$((RANDOM%5+1)),\"price\":$((RANDOM%100))}" 2>/dev/null | \
    jq '.status'
done | sort | uniq -c
```

**Expected Output**:
```
~95  SUCCESS
~3   FAILED_PAYMENT
~2   OUT_OF_STOCK
```

**Checks**:
- [ ] Mix of statuses reflects probabilistic model
- [ ] No crashes or errors in logs
- [ ] Each response is valid JSON

---

### Test 6: Concurrent Requests (Manual)

**Objective**: Verify non-blocking behavior

**Commands** (submit 10 orders simultaneously):
```bash
for i in {1..10}; do
  curl -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d "{\"productId\":\"Product$i\",\"quantity\":1,\"price\":100}" &
done
wait
```

**Expected Result**:
- [ ] All 10 requests processed in parallel
- [ ] Total time ~500ms (not 5000ms)
- [ ] Each gets response
- [ ] No request timeouts

---

### Test 7: Backpressure Verification

**Objective**: Verify system remains responsive under load

**Commands**:
```bash
# Terminal 1: Monitor thread count
watch -n 1 'jps -l && curl http://localhost:8080/actuator/metrics/jvm.threads.live 2>/dev/null | grep value'

# Terminal 2: Submit many orders rapidly
for i in {1..1000}; do
  curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{"productId":"Mouse","quantity":1,"price":25}' &
  
  # Submit 50 at a time, then wait
  if [ $((i % 50)) -eq 0 ]; then
    wait
  fi
done
```

**Expected Observation**:
- [ ] Thread count grows gradually (not spike to 1000)
- [ ] Memory usage stays <500MB
- [ ] CPU usage 60-70% (not 100%)
- [ ] Server stays responsive

**Checks**:
- [ ] Threads: ~50-100 max (CPU cores × backpressure factor)
- [ ] Memory: bounded growth, no OOM
- [ ] All 1000 orders eventually complete

---

## Benchmark Tests

### Test 8: Run Benchmark (5000 Concurrent Orders)

**Command**:
```bash
./run.sh benchmark
# or
mvn spring-boot:run -Dspring-boot.run.arguments="--runBenchmark"
```

**Expected Output**:
```
Starting load test with 5000 concurrent orders...
System parallelism: 8
[Progress: Processed 500 orders...]
[Progress: Processed 1000 orders...]
...
[Progress: Processed 5000 orders...]

================ BENCHMARK RESULTS ================
Total Orders: 5000
Success: 4520 (90.40%)
Failures: 480
Total Execution Time: 12500 ms
Throughput: 400.00 orders/sec
Average Latency: 2.50 ms
P50 Latency: 1.2 ms
P95 Latency: 8.5 ms
P99 Latency: 15.3 ms
CPU Usage: 65.43%
Memory Used: 125.50 MB
Thread Count - Start: 42, End: 89, Peak: 115
Status Breakdown:
  SUCCESS: 4520
  FAILED_PAYMENT: 240
  OUT_OF_STOCK: 240
===================================================
```

**File Generated**: `benchmark-results.csv`

**Checks**:
- [ ] Benchmark completes in 15-60 seconds
- [ ] All 5000 orders processed
- [ ] Success rate 85-95% (90% ± variability)
- [ ] Throughput > 300 orders/sec
- [ ] P99 latency < 30ms
- [ ] CPU usage 50-80%
- [ ] Memory delta < 200MB
- [ ] Thread count < 150 (bound)

---

### Test 9: CSV Results Validation

**Command**:
```bash
head -2 benchmark-results.csv
tail -1 benchmark-results.csv
```

**Expected Format**:
```csv
timestamp,totalOrders,success,failed,totalTimeMs,throughput,avgLatency,p50Latency,p95Latency,p99Latency,cpu,memoryMB,startThreads,endThreads,peakThreads
2024-05-01T10:30:45,5000,4520,480,12500,400.00,2.50,1.2,8.5,15.3,65.43,125.50,42,89,115
```

**Checks**:
- [ ] Header row matches expected columns
- [ ] Data row has all 15 values
- [ ] All numeric fields are valid numbers
- [ ] Timestamp is ISO 8601 format

---

## Reactive Patterns Verification

### Test 10: Error Recovery (Payment Retry)

**Objective**: Verify retryWhen() works correctly

**Observation** (in logs during benchmark):
```
[Payment Service] Processing payment for order: ... 
[Payment Service] Simulating payment timeout for order: ...
[Payment Service] Retrying payment...
[Payment Service] Retrying payment...
[Payment Service] Payment FAILED: Timeout
```

**Checks**:
- [ ] Retries appear in logs (2 retries seen)
- [ ] Order eventually fails with proper message
- [ ] Inventory rolled back after payment failure

---

### Test 11: Timeout Protection

**Objective**: Verify timeout() prevents hanging

**Setup**: Add extreme latency to PaymentService temporarily

**Expected**: 
- [ ] Requests complete within timeout (max 5 seconds)
- [ ] No requests hang indefinitely
- [ ] Timeouts converted to FAILED_PAYMENT status

---

### Test 12: Parallel Execution (Backpressure)

**Objective**: Verify parallel() limits concurrency

**Check logs** during benchmark run:

```bash
# Count concurrent operations from logs
grep "Inventory Service.*Checking stock" benchmark-results.csv | wc -l
```

**Expected**:
- [ ] Max 8 concurrent inventory checks (CPU core count)
- [ ] Orders queue naturally when service is busy
- [ ] Memory remains bounded

---

## Performance Tests

### Test 13: Latency Consistency

**Objective**: Verify latency doesn't degrade under load

**Run benchmark twice**:
```bash
./run.sh benchmark  # Run 1
./run.sh benchmark  # Run 2
```

**Compare results**:
```bash
cat benchmark-results.csv | tail -2
```

**Expected**:
- [ ] Latency metrics similar between runs (±10%)
- [ ] Throughput similar between runs
- [ ] No degradation on second run (no memory leaks)

---

### Test 14: CPU Efficiency

**Objective**: Verify CPU usage is optimal

**Expected Metrics**:
- Throughput: 400+ orders/sec = ✅ Efficient
- CPU usage: 60-70% = ✅ Good (not idle, not maxed)
- Memory: 100-150MB delta = ✅ Reasonable

**Poor Indicators**:
- ❌ Throughput < 200 orders/sec
- ❌ CPU usage < 20% (too conservative)
- ❌ CPU usage > 95% (constrained)
- ❌ Memory delta > 300MB (inefficient)

---

## Logging Tests

### Test 15: Verify Logging Output

**Expected logs during normal operation**:
```
[Order Service] Processing new order: ... for product: Laptop qty: 1
[Inventory Service] Checking stock for Laptop qty: 1
[Inventory Service] Stock reserved for Laptop. Remaining: 499
[Payment Service] Processing payment for order: ...
[Payment Service] Payment successful for order: ...
[Order Service] Order ... completed successfully
```

**Checks**:
- [ ] Logs show order flow clearly
- [ ] Timestamps and context present
- [ ] No warning/error messages unless expected

---

## Edge Cases

### Test 16: Zero/Negative Quantity

**Request**:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"Laptop","quantity":0,"price":3500}'
```

**Expected**: ERROR (validation should catch this)

**Checks**:
- [ ] Request rejected gracefully
- [ ] Response indicates validation error

---

### Test 17: Non-existent Product

**Request**:
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":"NonExistent","quantity":1,"price":100}'
```

**Expected**: OUT_OF_STOCK (product not in inventory)

**Checks**:
- [ ] Returns OUT_OF_STOCK (not ERROR)
- [ ] Graceful handling of unknown products

---

## Stress Tests

### Test 18: Rapid-Fire Orders

**Command** (10,000 orders in 10 seconds):
```bash
# Fire-and-forget approach
seq 1 10000 | xargs -I {} -P 100 \
  curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{"productId":"Mouse","quantity":1,"price":25}' \
    -o /dev/null &
wait

# Check if server still responsive
curl http://localhost:8080/api/orders/health
```

**Expected**:
- [ ] Server handles 10k requests without crashing
- [ ] Health check responds immediately
- [ ] No OutOfMemoryError
- [ ] No cascading failures

---

### Test 19: Long-Running Server

**Command** (run server for 1 hour with intermittent load):
```bash
# Terminal 1: Start server
./run.sh server

# Terminal 2: Send orders every 10 seconds for 1 hour
for i in {1..360}; do
  curl -s -X POST http://localhost:8080/api/orders \
    -H "Content-Type: application/json" \
    -d '{"productId":"Mouse","quantity":1,"price":25}' | jq '.status'
  sleep 10
done
```

**Expected**:
- [ ] Server runs stably for 1 hour
- [ ] All orders processed successfully
- [ ] Memory usage stable (no memory leaks)
- [ ] CPU usage normal
- [ ] No errors in logs

---

## Cleanup & Verification

### Test 20: Project Cleanup

**Commands**:
```bash
# Remove build artifacts
mvn clean

# Verify all source files still present
ls -la src/main/java/com/project/reactiveorders/**/*.java | wc -l
# Expected: 15+ files

# Verify CSV can be deleted
rm -f benchmark-results.csv

# Rebuild to verify clean build works
mvn clean package
```

**Checks**:
- [ ] Clean build completes successfully
- [ ] No files permanently removed
- [ ] Project ready for submission

---

## Summary Table

| Test | Status | Notes |
|------|--------|-------|
| Build | ✓ | No errors |
| Health Check | ✓ | Responds immediately |
| Successful Order | ✓ | SUCCESS status |
| Out of Stock | ✓ | Correct inventory tracking |
| Stock Depletion | ✓ | Atomic operations |
| Invalid Request | ✓ | Error handling |
| Timeout Handling | ✓ | Retry mechanism works |
| Concurrent Requests | ✓ | Non-blocking behavior |
| Backpressure | ✓ | Bounded resources |
| Benchmark 5000 | ✓ | > 300 orders/sec |
| CSV Export | ✓ | Correct format |
| Error Recovery | ✓ | Rollback works |
| Latency Consistency | ✓ | Stable metrics |
| CPU Efficiency | ✓ | 60-70% utilization |
| Logging | ✓ | Clear trace logs |
| Edge Cases | ✓ | Graceful handling |
| Stress Test | ✓ | Handles 10k+ orders |
| Long Running | ✓ | Stable for 1 hour |

---

## Test Results

**Date**: _________________

**Tester**: _________________

**Overall Status**: 
- [ ] All tests passing
- [ ] Ready for submission
- [ ] Issues found (see notes)

**Notes**: 
```
_________________________________
_________________________________
_________________________________
```

