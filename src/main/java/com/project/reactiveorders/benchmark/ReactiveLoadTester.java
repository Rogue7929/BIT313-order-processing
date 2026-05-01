package com.project.reactiveorders.benchmark;

import com.project.reactiveorders.model.OrderRequest;
import com.project.reactiveorders.model.OrderResponse;
import com.project.reactiveorders.service.OrderService;
import com.project.reactiveorders.util.ReactiveLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Reactive Load Tester - Phase 3 Benchmarking Module
 * 
 * Demonstrates:
 * - Flux.range() for creating concurrent workloads
 * - parallel() with Schedulers.parallel() for multi-core processing
 * - flatMap() for non-blocking request submission
 * - Performance measurement with JVM MXBeans
 * - CSV reporting of results
 * 
 * Features:
 * - Generates 5000+ concurrent order requests
 * - Measures execution time, throughput, latency
 * - Captures resource usage (CPU, memory, threads)
 * - Exports results to CSV file
 * - Provides detailed statistics
 */
@Component
@RequiredArgsConstructor
public class ReactiveLoadTester {
    private final OrderService orderService;
    private final ReactiveLogger logger;

    private static final int TOTAL_ORDERS = 5000;
    private static final int PARALLELISM = Runtime.getRuntime().availableProcessors();
    private static final String[] PRODUCTS = {"Laptop", "Mouse", "Keyboard", "Monitor", "Headphones"};

    /**
     * Execute load test with 5000 concurrent orders
     * 
     * Orchestration:
     * 1. Initialize metrics collectors
     * 2. Generate 5000 order requests
     * 3. Execute in parallel using reactive streams
     * 4. Collect performance metrics
     * 5. Export results to CSV
     * 
     * @return BenchmarkResult containing all performance metrics
     */
    public BenchmarkResult runLoadTest() {
        logger.info("Load Tester", "Starting load test with " + TOTAL_ORDERS + " concurrent orders");
        logger.info("Load Tester", "System parallelism: " + PARALLELISM);

        // Initialize metrics
        long startTime = System.currentTimeMillis();
        long startMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        int startThreadCount = threadMXBean.getThreadCount();
        double startCpuTime = osBean.getProcessCpuTime();

        // Atomic counters for tracking
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        ConcurrentHashMap<String, AtomicInteger> statusCounts = new ConcurrentHashMap<>();

        List<Long> responseTimes = new ArrayList<>();

        // Generate and execute orders in parallel
        Flux.range(1, TOTAL_ORDERS)
                .parallel(PARALLELISM)
                .runOn(Schedulers.parallel())
                .flatMap(i -> {
                    // Generate random order
                    String productId = PRODUCTS[i % PRODUCTS.length];
                    int quantity = (i % 5) + 1;
                    double price = 100.0 + (i % 3500.0);

                    OrderRequest request = OrderRequest.builder()
                            .productId(productId)
                            .quantity(quantity)
                            .price(price)
                            .build();

                    long orderStartTime = System.currentTimeMillis();

                    // Process order and collect metrics
                    return orderService.processOrder(request)
                            .doOnNext(response -> {
                                long responseTime = System.currentTimeMillis() - orderStartTime;
                                responseTimes.add(responseTime);

                                if ("SUCCESS".equals(response.getStatus())) {
                                    successCount.incrementAndGet();
                                } else {
                                    failureCount.incrementAndGet();
                                }

                                statusCounts.computeIfAbsent(response.getStatus(), k -> new AtomicInteger(0))
                                        .incrementAndGet();

                                if (i % 500 == 0) {
                                    logger.info("Load Tester", "Processed " + i + " orders");
                                }
                            })
                            .doOnError(throwable -> {
                                failureCount.incrementAndGet();
                                logger.error("Load Tester", "Error processing order " + i + 
                                        ": " + throwable.getMessage());
                            });
                })
                .sequential()
                .blockLast();  // Wait for all to complete

        // Calculate total time and metrics
        long endTime = System.currentTimeMillis();
        long totalTimeMs = endTime - startTime;

        // Collect JVM metrics
        int endThreadCount = threadMXBean.getThreadCount();
        int peakThreadCount = threadMXBean.getPeakThreadCount();
        double endCpuTime = osBean.getProcessCpuTime();
        long endMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long usedMemory = endMemory - startMemory;

        double cpuUsagePercent = (endCpuTime - startCpuTime) / 1_000_000_000.0;  // Convert to seconds
        double cpuUsagePercentage = (cpuUsagePercent / (totalTimeMs / 1000.0)) * 100;
        double memoryUsageMB = usedMemory / (1024.0 * 1024.0);

        // Calculate throughput and latency
        double throughputOrdersPerSec = (TOTAL_ORDERS / (totalTimeMs / 1000.0));
        double avgLatencyMs = totalTimeMs / (double) TOTAL_ORDERS;

        // Calculate percentiles
        responseTimes.sort(Long::compare);
        long p50 = responseTimes.size() > 0 ? responseTimes.get((int)(responseTimes.size() * 0.50)) : 0;
        long p95 = responseTimes.size() > 0 ? responseTimes.get((int)(responseTimes.size() * 0.95)) : 0;
        long p99 = responseTimes.size() > 0 ? responseTimes.get((int)(responseTimes.size() * 0.99)) : 0;

        // Build result
        BenchmarkResult result = BenchmarkResult.builder()
                .totalOrders(TOTAL_ORDERS)
                .successCount(successCount.get())
                .failureCount(failureCount.get())
                .totalTimeMs(totalTimeMs)
                .throughputOrdersPerSec(throughputOrdersPerSec)
                .avgLatencyMs(avgLatencyMs)
                .p50LatencyMs(p50)
                .p95LatencyMs(p95)
                .p99LatencyMs(p99)
                .cpuUsagePercent(cpuUsagePercentage)
                .memoryUsageMB(memoryUsageMB)
                .startThreadCount(startThreadCount)
                .endThreadCount(endThreadCount)
                .peakThreadCount(peakThreadCount)
                .statusCounts(statusCounts)
                .build();

        // Log and export results
        logResults(result);
        exportToCsv(result);

        return result;
    }

    /**
     * Log benchmark results to console
     * @param result benchmark result
     */
    private void logResults(BenchmarkResult result) {
        logger.info("Load Tester", "================ BENCHMARK RESULTS ================");
        logger.info("Load Tester", "Total Orders: " + result.getTotalOrders());
        logger.info("Load Tester", "Success: " + result.getSuccessCount() + 
                " (" + String.format("%.2f", (result.getSuccessCount() / (double)result.getTotalOrders()) * 100) + "%)");
        logger.info("Load Tester", "Failures: " + result.getFailureCount());
        logger.info("Load Tester", "");
        logger.info("Load Tester", "Total Execution Time: " + result.getTotalTimeMs() + " ms");
        logger.info("Load Tester", "Throughput: " + String.format("%.2f", result.getThroughputOrdersPerSec()) + 
                " orders/sec");
        logger.info("Load Tester", "Average Latency: " + String.format("%.2f", result.getAvgLatencyMs()) + " ms");
        logger.info("Load Tester", "P50 Latency: " + result.getP50LatencyMs() + " ms");
        logger.info("Load Tester", "P95 Latency: " + result.getP95LatencyMs() + " ms");
        logger.info("Load Tester", "P99 Latency: " + result.getP99LatencyMs() + " ms");
        logger.info("Load Tester", "");
        logger.info("Load Tester", "CPU Usage: " + String.format("%.2f", result.getCpuUsagePercent()) + "%");
        logger.info("Load Tester", "Memory Used: " + String.format("%.2f", result.getMemoryUsageMB()) + " MB");
        logger.info("Load Tester", "Thread Count - Start: " + result.getStartThreadCount() + 
                ", End: " + result.getEndThreadCount() + ", Peak: " + result.getPeakThreadCount());
        logger.info("Load Tester", "");
        logger.info("Load Tester", "Status Breakdown:");
        result.getStatusCounts().forEach((status, count) -> 
                logger.info("Load Tester", "  " + status + ": " + count.get())
        );
        logger.info("Load Tester", "===================================================");
    }

    /**
     * Export benchmark results to CSV file
     * @param result benchmark result
     */
    private void exportToCsv(BenchmarkResult result) {
        String filename = "benchmark-results.csv";
        try (FileWriter writer = new FileWriter(filename)) {
            // Write header
            writer.write("timestamp,totalOrders,success,failed,totalTimeMs,throughput,avgLatency," +
                    "p50Latency,p95Latency,p99Latency,cpu,memoryMB,startThreads,endThreads,peakThreads\n");

            // Write data row
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            writer.write(String.format("%s,%d,%d,%d,%d,%.2f,%.2f,%d,%d,%d,%.2f,%.2f,%d,%d,%d\n",
                    timestamp,
                    result.getTotalOrders(),
                    result.getSuccessCount(),
                    result.getFailureCount(),
                    result.getTotalTimeMs(),
                    result.getThroughputOrdersPerSec(),
                    result.getAvgLatencyMs(),
                    result.getP50LatencyMs(),
                    result.getP95LatencyMs(),
                    result.getP99LatencyMs(),
                    result.getCpuUsagePercent(),
                    result.getMemoryUsageMB(),
                    result.getStartThreadCount(),
                    result.getEndThreadCount(),
                    result.getPeakThreadCount()
            ));

            logger.info("Load Tester", "Results exported to " + filename);
        } catch (IOException e) {
            logger.error("Load Tester", "Failed to export results to CSV: " + e.getMessage());
        }
    }
}
