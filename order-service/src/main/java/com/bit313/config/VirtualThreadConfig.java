package com.bit313.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;

import java.util.concurrent.Executors;

/**
 * ============================================================
 * VIRTUAL THREAD CONFIGURATION — Core of Phase 2
 * ============================================================
 *
 * This single configuration class is what makes Phase 2 work.
 * It replaces Tomcat's default platform thread pool with
 * Java 21 Virtual Threads via:
 *
 *   Executors.newVirtualThreadPerTaskExecutor()
 *
 * What this means:
 * - Every incoming HTTP request gets its OWN virtual thread
 * - Virtual threads are lightweight (cheap to create, ~1KB each)
 * - Blocking calls (REST, DB, sleep) do NOT waste OS threads
 * - The JVM can handle thousands of concurrent requests this way
 *
 * Compare this to Reactive (Member A):
 * - Reactive avoids blocking entirely using async operators
 * - Virtual Threads embrace blocking but make it cheap
 * - Both achieve high concurrency — different philosophy
 */
@Configuration
public class VirtualThreadConfig {

    /**
     * Replaces Tomcat's platform thread pool with a virtual thread executor.
     * This is the key line for the entire Phase 2 implementation.
     */
    @Bean
    public TomcatProtocolHandlerCustomizer<?> virtualThreadTomcatCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(
                Executors.newVirtualThreadPerTaskExecutor()
            );
        };
    }
}
