package com.project.reactiveorders.controller;

import com.project.reactiveorders.model.OrderRequest;
import com.project.reactiveorders.model.OrderResponse;
import com.project.reactiveorders.service.OrderService;
import com.project.reactiveorders.util.ReactiveLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * REST API Controller for Order Processing
 * 
 * Demonstrates:
 * - Spring WebFlux reactive endpoints
 * - Non-blocking request/response handling
 * - Mono-based response composition
 * - Error handling for reactive flows
 * 
 * Endpoints:
 * - POST /api/orders: Submit new order
 * - GET /api/health: Health check
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ReactiveLogger logger;

    /**
     * POST endpoint to process a new order
     * 
     * Receives order request and orchestrates reactive processing flow:
     * 1. Validates input
     * 2. Calls OrderService for reactive processing
     * 3. Returns OrderResponse with result
     * 
     * Request Body Example:
     * {
     *   "productId": "Laptop",
     *   "quantity": 1,
     *   "price": 3500
     * }
     * 
     * Response Example:
     * {
     *   "orderId": "550e8400-e29b-41d4-a716-446655440000",
     *   "status": "SUCCESS",
     *   "message": "Order processed successfully",
     *   "processingTimeMs": 234,
     *   "totalPrice": 3500
     * }
     * 
     * @param request order request with product details
     * @return Mono<ResponseEntity<OrderResponse>> with order processing result
     */
    @PostMapping
    public Mono<ResponseEntity<OrderResponse>> submitOrder(@RequestBody OrderRequest request) {
        logger.info("Order Controller", "Received order request for product: " + request.getProductId());

        return orderService.processOrder(request)
                .map(response -> {
                    HttpStatus status = "SUCCESS".equals(response.getStatus()) ? 
                            HttpStatus.OK : HttpStatus.BAD_REQUEST;
                    logger.info("Order Controller", "Sending response for order: " + response.getOrderId() + 
                            " with status: " + response.getStatus());
                    return new ResponseEntity<>(response, status);
                })
                .onErrorResume(throwable -> {
                    logger.error("Order Controller", "Error processing order: " + throwable.getMessage());
                    OrderResponse errorResponse = OrderResponse.builder()
                            .status("ERROR")
                            .message("Internal server error: " + throwable.getMessage())
                            .build();
                    return Mono.just(new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    /**
     * GET health check endpoint
     * Returns service health status
     * 
     * @return Mono<ResponseEntity<String>> with health status
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<String>> healthCheck() {
        return Mono.just(ResponseEntity.ok("Reactive Order Service is running!"));
    }
}
