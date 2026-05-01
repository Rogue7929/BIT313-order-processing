package com.project.reactiveorders.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

/**
 * WebFlux Configuration
 * Configures Spring WebFlux for reactive web requests
 * 
 * Features:
 * - CORS configuration for cross-origin requests
 * - Default reactive settings
 */
@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    /**
     * Configure CORS settings for all endpoints
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .maxAge(3600);
    }
}
