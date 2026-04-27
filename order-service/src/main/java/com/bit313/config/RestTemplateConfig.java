package com.bit313.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * ============================================================
 * REST TEMPLATE CONFIGURATION — HTTP Client Setup
 * ============================================================
 *
 * Configures the RestTemplate used by OrderService to call
 * Inventory and Payment services.
 *
 * Timeout values chosen for this project:
 * - Connect timeout: 3 seconds  (time to establish TCP connection)
 * - Read timeout:    5 seconds  (time to wait for service response)
 *
 * If either timeout is exceeded, a ResourceAccessException is
 * thrown and caught by the retry logic in OrderService.
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(3000);      // 3 seconds in milliseconds
        factory.setReadTimeout(5000);         // 5 seconds in milliseconds
        return new BufferingClientHttpRequestFactory(factory);
    }
}
