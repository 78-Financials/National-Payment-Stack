package com.payaza.nps.filter;

import com.payaza.nps.model.AuditLog;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.AuditService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * Filter to log all HTTP requests and responses
 */
@Component
@Order(1)
public class RequestLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Autowired
    private AuditService auditService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        long startTime = System.currentTimeMillis();
        String requestId = UUID.randomUUID().toString();
        
        // Add request ID to headers for tracing
        httpResponse.setHeader("X-Request-ID", requestId);
        
        // Wrap request and response to cache content
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(httpRequest);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(httpResponse);
        
        try {
            // Log request start
            logRequestStart(httpRequest, requestId);
            
            // Continue with the filter chain
            chain.doFilter(requestWrapper, responseWrapper);
            
            // Log successful response
            long executionTime = System.currentTimeMillis() - startTime;
            logRequestSuccess(httpRequest, httpResponse, requestId, executionTime);
            
        } catch (Exception e) {
            // Log error response
            long executionTime = System.currentTimeMillis() - startTime;
            logRequestError(httpRequest, httpResponse, requestId, executionTime, e);
            throw e;
            
        } finally {
            // Copy response content back to original response
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequestStart(HttpServletRequest request, String requestId) {
        logger.debug("Request started - ID: {}, Method: {}, URI: {}, IP: {}, User-Agent: {}", 
                    requestId, request.getMethod(), request.getRequestURI(), 
                    getClientIpAddress(request), request.getHeader("User-Agent"));
    }

    private void logRequestSuccess(HttpServletRequest request, HttpServletResponse response, 
                                  String requestId, long executionTime) {
        String clientId = ClientContext.getCurrentClientId();
        
        logger.info("Request completed - ID: {}, Method: {}, URI: {}, Status: {}, Time: {}ms, Client: {}", 
                   requestId, request.getMethod(), request.getRequestURI(), 
                   response.getStatus(), executionTime, clientId);
        
        // Log API call in audit service
        auditService.logApiCall(
            request.getRequestURI(),
            request.getMethod(),
            clientId,
            executionTime,
            response.getStatus(),
            "Request completed successfully"
        );
    }

    private void logRequestError(HttpServletRequest request, HttpServletResponse response,
                                String requestId, long executionTime, Exception error) {
        String clientId = ClientContext.getCurrentClientId();
        
        logger.error("Request failed - ID: {}, Method: {}, URI: {}, Status: {}, Time: {}ms, Client: {}, Error: {}", 
                    requestId, request.getMethod(), request.getRequestURI(), 
                    response.getStatus(), executionTime, clientId, error.getMessage(), error);
        
        // Log API call error in audit service
        auditService.logError(
            request.getMethod() + " " + request.getRequestURI(),
            request.getRequestURI(),
            AuditLog.ActionType.API_CALL,
            null,
            clientId,
            "Request failed: " + error.getMessage(),
            error.getClass().getSimpleName(),
            error.getMessage(),
            Map.of("executionTimeMs", executionTime, "statusCode", response.getStatus())
        );
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
