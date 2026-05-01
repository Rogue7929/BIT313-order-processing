package com.project.reactiveorders.benchmark;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Benchmark Result Model
 * Aggregates all performance metrics collected during load testing
 * 
 * Metrics Include:
 * - Request counts and success rates
 * - Latency percentiles (P50, P95, P99)
 * - Throughput (orders/sec)
 * - Resource usage (CPU, memory, threads)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BenchmarkResult {
    private int totalOrders;
    private int successCount;
    private int failureCount;
    private long totalTimeMs;
    private double throughputOrdersPerSec;
    private double avgLatencyMs;
    private long p50LatencyMs;
    private long p95LatencyMs;
    private long p99LatencyMs;
    private double cpuUsagePercent;
    private double memoryUsageMB;
    private int startThreadCount;
    private int endThreadCount;
    private int peakThreadCount;
    private Map<String, AtomicInteger> statusCounts;

    /**
     * Get success rate as percentage
     */
    public double getSuccessRatePercent() {
        return (successCount / (double) totalOrders) * 100;
    }

    /**
     * Get failure rate as percentage
     */
    public double getFailureRatePercent() {
        return (failureCount / (double) totalOrders) * 100;
    }
}
