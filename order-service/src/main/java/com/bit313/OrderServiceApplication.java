package com.bit313;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * BIT313 Phase 2 — Order Service
 *
 * Entry point for the Order microservice.
 * Runs on port 8080 (see application.properties).
 *
 * Virtual Threads are enabled via VirtualThreadConfig.java
 * which replaces Tomcat's thread pool with:
 *   Executors.newVirtualThreadPerTaskExecutor()
 */
@SpringBootApplication
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}
