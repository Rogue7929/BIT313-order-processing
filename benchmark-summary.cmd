@echo off
echo ================================================
echo  BIT313 Phase 2 - Virtual Threads Benchmark
echo  Summary Report
echo ================================================
echo.
echo --- STRESS TEST RESULTS ---
echo  Total Orders Submitted : 5,000
echo  Confirmed              : 4,998
echo  Failed                 : 2
echo  Success Rate           : 99.96%%
echo  Execution Time         : 15,827ms (15.8 seconds)
echo  Throughput             : ~316 orders/second
echo.
echo --- SIMULATION SETTINGS ---
echo  Concurrent Orders      : 5,000
echo  Stock Per Product      : 10,000
echo  Number of Products     : 10 (PROD-0 to PROD-9)
echo  Payment Failure Rate   : 5%%
echo  Inventory Latency      : 50ms
echo  Payment Latency        : 100ms
echo.
echo --- TIMEOUT AND RETRY SETTINGS ---
echo  HTTP Connect Timeout   : 3 seconds
echo  HTTP Read Timeout      : 5 seconds
echo  Max Retry Attempts     : 3
echo  Retry Delay            : 1,000ms
echo.
echo --- SERVICE PORTS ---
echo  Order Service          : 8080
echo  Inventory Service      : 8081
echo  Payment Service        : 8082
echo.
echo --- VIRTUAL THREAD CONFIGURATION ---
echo  Thread Model    : Executors.newVirtualThreadPerTaskExecutor()
echo  Applied To      : All three services via Tomcat customizer
echo  HTTP Client     : RestTemplate (blocking)
echo  Java Version    : 21
echo  Spring Boot     : 3.2.0
echo.
echo --- SETTINGS FOR MEMBER A TO MATCH ---
echo  1. Run stress test with exactly 5,000 concurrent orders
echo  2. Inventory latency : .delayElement(Duration.ofMillis(50))
echo  3. Payment latency   : .delayElement(Duration.ofMillis(100))
echo  4. Payment failures  : Math.random() less than 0.05
echo  5. Stock levels      : 10,000 units per product
echo  6. Metrics to record : executionTimeMs, confirmed,
echo                         failed, throughput
echo.
echo ================================================
echo  Generated for BIT313 Concurrent Programming
echo ================================================
pause