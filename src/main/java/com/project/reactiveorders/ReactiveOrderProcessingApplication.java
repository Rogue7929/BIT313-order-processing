package com.project.reactiveorders;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BIT313 Concurrent Programming - Phase 1: Reactive Programming
 * High-Performance Order Processing System using Project Reactor
 * 
 * This application demonstrates non-blocking, reactive order processing
 * with Mono/Flux-based microservices using Spring WebFlux and Project Reactor.
 * 
 * Key Features:
 * - Non-blocking I/O with Publisher-Subscriber model
 * - Reactive backpressure handling for order spikes
 * - Simulated network latency and failure scenarios
 * - Comprehensive reactive operator usage (flatMap, zipWhen, retryWhen, etc.)
 * - Load testing with 5000+ concurrent orders
 * - Performance benchmarking with JVM metrics
 */
@SpringBootApplication
public class ReactiveOrderProcessingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveOrderProcessingApplication.class, args);
    }
}
