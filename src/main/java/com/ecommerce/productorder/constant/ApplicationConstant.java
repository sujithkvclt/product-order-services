package com.ecommerce.productorder.constant;

import java.util.Arrays;
import java.util.List;

/**
 * Application-wide constants
 */
public final class ApplicationConstant {

    private ApplicationConstant() {
        // Prevent instantiation
    }

    // Logging Constants
    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String CORRELATION_ID_MDC_KEY = "correlationId";

    public static final List<String> EXCLUDED_LOG_PATHS = Arrays.asList(
            "/actuator",
            "/h2-console",
            "/swagger-ui",
            "/api-docs",
            "/favicon.ico"
    );

    // Response Messages
    public static final String MASKED_VALUE = "******";
}
