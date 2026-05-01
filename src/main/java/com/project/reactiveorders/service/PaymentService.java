package com.project.reactiveorders.service;

import com.project.reactiveorders.exception.PaymentFailedException;
import com.project.reactiveorders.model.PaymentResult;
import com.project.reactiveorders.util.ReactiveLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * Reactive Payment Service
 * 
 * Demonstrates:
 * - Simulated payment processing with probabilistic outcomes
 * - Timeout handling with retryWhen()
 * - Error recovery with onErrorResume()
 * - Realistic latency simulation (100-500ms)
 * - subscribeOn() for non-blocking execution
 * 
 * Failure Scenarios:
 * - 90% success rate
 * - 5% timeout failures
 * - 5% general failures
 */
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final ReactiveLogger logger;

    /**
     * Reactively process payment with simulated latency and failure scenarios
     * 
     * Reactive Flow:
     * 1. Generate transaction ID
     * 2. Simulate payment processing (100-500ms)
     * 3. Simulate realistic failure rates
     * 4. Retry on transient failures (max 2 retries)
     * 5. Apply 3-second timeout
     * 
     * Failure Distribution:
     * - 90% success
     * - 5% timeout
     * - 5% general failure
     * 
     * @param orderId order identifier
     * @param amount payment amount
     * @return Mono<PaymentResult> with payment transaction result
     */
    public Mono<PaymentResult> processPayment(String orderId, Double amount) {
        return Mono.defer(() -> {
                    String transactionId = UUID.randomUUID().toString();
                    logger.info("Payment Service", "Processing payment for order: " + orderId + 
                            ", amount: " + amount + ", txn: " + transactionId);

                    // Simulate processing latency: 100-500ms
                    long latency = 100 + (long)(Math.random() * 400);

                    return Mono.fromCallable(() -> {
                                // Simulate realistic payment scenarios
                                double random = Math.random();

                                if (random < 0.05) {  // 5% timeout scenario
                                    logger.warn("Payment Service", "Simulating payment timeout for order: " + orderId);
                                    throw new TimeoutException("Payment processing timeout");
                                } else if (random < 0.10) {  // 5% failure scenario
                                    logger.warn("Payment Service", "Payment declined for order: " + orderId);
                                    throw new PaymentFailedException("Payment declined by processor");
                                } else {  // 90% success scenario
                                    logger.info("Payment Service", "Payment successful for order: " + orderId);
                                    return PaymentResult.builder()
                                            .transactionId(transactionId)
                                            .status("SUCCESS")
                                            .message("Payment processed successfully")
                                            .processingTimeMs(latency)
                                            .build();
                                }
                            })
                            .delayElement(Duration.ofMillis(latency))  // Simulate network/processing latency
                            .subscribeOn(Schedulers.boundedElastic());  // Execute on elastic thread pool
                })
                .timeout(Duration.ofSeconds(3))  // 3-second absolute timeout
                .retryWhen(Retry.backoff(2, Duration.ofMillis(100)))
                .onErrorResume(throwable -> {
                    logger.error("Payment Service", "Payment failed: " + throwable.getMessage());
                    if (throwable instanceof TimeoutException) {
                        return Mono.just(PaymentResult.builder()
                                .status("TIMEOUT")
                                .message("Payment processing timed out")
                                .build());
                    } else {
                        return Mono.just(PaymentResult.builder()
                                .status("FAILED")
                                .message(throwable.getMessage())
                                .build());
                    }
                });
    }
}
