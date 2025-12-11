package com.ecommerce.productorder.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for logging
 */
@Data
@Component
@ConfigurationProperties(prefix = "logging")
public class LoggingProperties {

    /**
     * List of sensitive field names to mask in logs
     * These fields will be replaced with "***REDACTED***"
     */
    private List<String> sensitiveFields = new ArrayList<>();
}
