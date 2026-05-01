package com.project.reactiveorders.benchmark;

import com.project.reactiveorders.util.ReactiveLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Benchmark Runner
 * Executes the load test when application starts with specific command-line argument
 * 
 * Usage:
 * java -jar app.jar --runBenchmark
 */
@Component
@RequiredArgsConstructor
public class BenchmarkRunner implements CommandLineRunner {
    private final ReactiveLoadTester loadTester;
    private final ReactiveLogger logger;

    @Override
    public void run(String... args) throws Exception {
        // Check if benchmark should be run
        boolean shouldRunBenchmark = false;
        for (String arg : args) {
            if ("--runBenchmark".equals(arg)) {
                shouldRunBenchmark = true;
                break;
            }
        }

        if (shouldRunBenchmark) {
            logger.info("Benchmark Runner", "Starting load test...");
            try {
                BenchmarkResult result = loadTester.runLoadTest();
                logger.info("Benchmark Runner", "Load test completed successfully");
            } catch (Exception e) {
                logger.error("Benchmark Runner", "Load test failed: " + e.getMessage());
            }
        } else {
            logger.info("Benchmark Runner", "Service started in normal mode. " +
                    "Use '--runBenchmark' argument to run load test.");
        }
    }
}
