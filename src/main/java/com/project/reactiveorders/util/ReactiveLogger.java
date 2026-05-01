package com.project.reactiveorders.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Reactive Logger Utility
 * Provides convenient logging methods with context
 * Used across all reactive services for consistent logging
 */
@Component
public class ReactiveLogger {
    private static final Logger logger = LoggerFactory.getLogger(ReactiveLogger.class);

    /**
     * Log info level message with context
     * @param context component/service name
     * @param message log message
     */
    public void info(String context, String message) {
        logger.info("[{}] {}", context, message);
    }

    /**
     * Log warn level message with context
     * @param context component/service name
     * @param message log message
     */
    public void warn(String context, String message) {
        logger.warn("[{}] {}", context, message);
    }

    /**
     * Log error level message with context
     * @param context component/service name
     * @param message log message
     */
    public void error(String context, String message) {
        logger.error("[{}] {}", context, message);
    }

    /**
     * Log debug level message with context
     * @param context component/service name
     * @param message log message
     */
    public void debug(String context, String message) {
        logger.debug("[{}] {}", context, message);
    }
}
