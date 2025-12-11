package com.ecommerce.productorder.logging;

import com.ecommerce.productorder.config.LoggingProperties;
import com.ecommerce.productorder.constant.ApplicationConstant;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Unified filter for correlation ID and HTTP request/response logging
 */
@Component
@Order(1)
@Slf4j
public class LoggingFilter extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoggingProperties loggingProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Skip logging for excluded paths
        if (shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Generate or get correlation ID
        String correlationId = request.getHeader(ApplicationConstant.CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Add correlation ID to MDC and response header
        MDC.put(ApplicationConstant.CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(ApplicationConstant.CORRELATION_ID_HEADER, correlationId);

        // Wrap request and response to cache body content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log request
            logRequest(requestWrapper);

            // Process the request
            filterChain.doFilter(requestWrapper, responseWrapper);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            logResponse(responseWrapper, duration);

            // Copy response body to actual response
            responseWrapper.copyBodyToResponse();

            // Clean up MDC
            MDC.clear();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        try {
            Map<String, Object> requestData = new LinkedHashMap<>();
            String url = request.getRequestURL().toString();
            String queryString = request.getQueryString();
            if (queryString != null) {
                url = url + "?" + queryString;
            }

            requestData.put("method", request.getMethod());
            requestData.put("url", url);
            requestData.put("uri", request.getRequestURI());
            requestData.put("queryString", request.getQueryString());
            requestData.put("headers", getHeaders(request));
            requestData.put("remoteAddr", request.getRemoteAddr());

            // Log request body for POST, PUT, PATCH
            if (hasBody(request.getMethod())) {
                String body = getRequestBody(request);
                if (body != null && !body.isEmpty()) {
                    requestData.put("body", parseJsonBody(body));
                }
            }

            // Store URL in MDC for response logging
            MDC.put("url", url);

            // Log using StructuredArguments for proper JSON formatting
            log.info("HTTP_REQUEST", StructuredArguments.keyValue("request", requestData));

        } catch (Exception e) {
            log.error("Error logging request", e);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        try {
            Map<String, Object> responseData = new LinkedHashMap<>();

            // Get URL from MDC (set during request logging)
            String url = MDC.get("url");

            responseData.put("url", url);
            responseData.put("status", response.getStatus());
            responseData.put("headers", getHeaders(response));
            responseData.put("durationMs", duration);

            // Log response body
            String body = getResponseBody(response);
            if (body != null && !body.isEmpty()) {
                responseData.put("body", parseJsonBody(body));
            }

            // Log using StructuredArguments for proper JSON formatting
            log.info("HTTP_RESPONSE", StructuredArguments.keyValue("response", responseData));

        } catch (Exception e) {
            log.error("Error logging response", e);
        } finally {
            // Clean up URL from MDC
            MDC.remove("url");
        }
    }

    private Map<String, String> getHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Exclude sensitive headers
            if (!isSensitiveHeader(headerName)) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        return headers;
    }

    private Map<String, String> getHeaders(HttpServletResponse response) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String headerName : response.getHeaderNames()) {
            if (!isSensitiveHeader(headerName)) {
                headers.put(headerName, response.getHeader(headerName));
            }
        }
        return headers;
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCaseName = headerName.toLowerCase();
        return loggingProperties.getSensitiveFields().stream()
                .anyMatch(sensitiveField -> lowerCaseName.contains(sensitiveField.toLowerCase()));
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length > 0) {
            try {
                return new String(content, request.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                log.error("Error reading request body", e);
            }
        }
        return null;
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] content = response.getContentAsByteArray();
        if (content.length > 0) {
            try {
                return new String(content, response.getCharacterEncoding());
            } catch (UnsupportedEncodingException e) {
                log.error("Error reading response body", e);
            }
        }
        return null;
    }

    private Object parseJsonBody(String body) {
        // Try to parse as JSON and sanitize sensitive fields
        try {
            Object jsonObject = objectMapper.readValue(body, Object.class);
            return sanitizeJsonObject(jsonObject);
        } catch (Exception e) {
            // Not JSON or parse error, return as-is (truncated if too long)
            return body.length() > 1000 ? body.substring(0, 1000) + "..." : body;
        }
    }

    @SuppressWarnings("unchecked")
    private Object sanitizeJsonObject(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) obj;
            Map<String, Object> sanitized = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String key = entry.getKey().toLowerCase();
                boolean isSensitive = loggingProperties.getSensitiveFields().stream()
                        .anyMatch(sensitiveField -> key.contains(sensitiveField.toLowerCase()));

                if (isSensitive) {
                    sanitized.put(entry.getKey(), ApplicationConstant.MASKED_VALUE);
                } else {
                    sanitized.put(entry.getKey(), sanitizeJsonObject(entry.getValue()));
                }
            }
            return sanitized;
        } else if (obj instanceof List) {
            List<Object> list = (List<Object>) obj;
            List<Object> sanitized = new ArrayList<>();
            for (Object item : list) {
                sanitized.add(sanitizeJsonObject(item));
            }
            return sanitized;
        }
        return obj;
    }

    private boolean hasBody(String method) {
        return "POST".equalsIgnoreCase(method) ||
               "PUT".equalsIgnoreCase(method) ||
               "PATCH".equalsIgnoreCase(method);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return ApplicationConstant.EXCLUDED_LOG_PATHS.stream().anyMatch(path::startsWith);
    }
}
