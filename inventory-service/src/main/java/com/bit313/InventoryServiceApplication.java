package com.bit313;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;

/**
 * BIT313 Phase 2 — Inventory Service
 * Runs on port 8081.
 *
 * Virtual Threads enabled inline here for brevity —
 * replaces Tomcat's platform thread pool.
 */
@SpringBootApplication
public class InventoryServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryServiceApplication.class, args);
    }

    /**
     * Enable Virtual Threads for all incoming HTTP requests.
     * Same pattern as Order Service's VirtualThreadConfig.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> virtualThreadCustomizer() {
        return protocolHandler ->
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    }
}
