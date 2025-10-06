package com.payaza.nps.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for comprehensive audit logging
 * Tracks all actions, API calls, and system events
 */
@Service
public class AuditService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    
    @Autowired
    private AuditLogRepository auditLogRepository;
    
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Log a successful action
     */
    @Async
    public void logSuccess(String action, String resource, AuditLog.ActionType actionType, 
                          String userId, String clientId, String message, Object details) {
        try {
            AuditLog auditLog = createBaseAuditLog(action, resource, actionType, userId, clientId);
            auditLog.setSuccess(message);
            auditLog.setDetails(serializeDetails(details));
            auditLog.setRequestInfo(getRequestInfo());
            
            auditLogRepository.save(auditLog);
            logger.info("Audit logged: {} - {} by {} (client: {})", action, resource, userId, clientId);
            
        } catch (Exception e) {
            logger.error("Failed to log audit: {}", e.getMessage(), e);
        }
    }

    /**
     * Log a failed action
     */
    @Async
    public void logFailure(String action, String resource, AuditLog.ActionType actionType,
                          String userId, String clientId, String message, String errorCode, Object details) {
        try {
            AuditLog auditLog = createBaseAuditLog(action, resource, actionType, userId, clientId);
            auditLog.setFailure(message, errorCode);
            auditLog.setDetails(serializeDetails(details));
            auditLog.setRequestInfo(getRequestInfo());
            
            auditLogRepository.save(auditLog);
            logger.warn("Audit logged (FAILURE): {} - {} by {} (client: {}): {}", action, resource, userId, clientId, message);
            
        } catch (Exception e) {
            logger.error("Failed to log audit failure: {}", e.getMessage(), e);
        }
    }

    /**
     * Log an error
     */
    @Async
    public void logError(String action, String resource, AuditLog.ActionType actionType,
                        String userId, String clientId, String message, String errorCode, 
                        String errorMessage, Object details) {
        try {
            AuditLog auditLog = createBaseAuditLog(action, resource, actionType, userId, clientId);
            auditLog.setError(message, errorCode, errorMessage);
            auditLog.setDetails(serializeDetails(details));
            auditLog.setRequestInfo(getRequestInfo());
            
            auditLogRepository.save(auditLog);
            logger.error("Audit logged (ERROR): {} - {} by {} (client: {}): {} - {}", 
                        action, resource, userId, clientId, message, errorMessage);
            
        } catch (Exception e) {
            logger.error("Failed to log audit error: {}", e.getMessage(), e);
        }
    }

    /**
     * Log API call with execution time
     */
    @Async
    public void logApiCall(String endpoint, String method, String clientId, long executionTimeMs,
                          int statusCode, String responseMessage) {
        try {
            AuditLog auditLog = createBaseAuditLog(
                method + " " + endpoint, 
                endpoint, 
                AuditLog.ActionType.API_CALL, 
                null, 
                clientId
            );
            
            if (statusCode >= 200 && statusCode < 300) {
                auditLog.setSuccess("API call completed successfully");
            } else if (statusCode >= 400 && statusCode < 500) {
                auditLog.setFailure("Client error", "HTTP_" + statusCode);
            } else if (statusCode >= 500) {
                auditLog.setError("Server error", "HTTP_" + statusCode, responseMessage);
            }
            
            auditLog.setExecutionTimeMs(executionTimeMs);
            auditLog.setRequestInfo(getRequestInfo());
            
            Map<String, Object> apiDetails = Map.of(
                "method", method,
                "endpoint", endpoint,
                "statusCode", statusCode,
                "executionTimeMs", executionTimeMs,
                "responseMessage", responseMessage
            );
            auditLog.setDetails(serializeDetails(apiDetails));
            
            auditLogRepository.save(auditLog);
            
        } catch (Exception e) {
            logger.error("Failed to log API call: {}", e.getMessage(), e);
        }
    }

    /**
     * Log admin actions
     */
    @Async
    public void logAdminAction(String action, String resource, String adminUserId, 
                              AuditLog.ActionType actionType, String message, Object details) {
        logSuccess(action, resource, actionType, adminUserId, "ADMIN", message, details);
    }

    /**
     * Log client actions
     */
    @Async
    public void logClientAction(String action, String resource, String clientId, 
                               AuditLog.ActionType actionType, String message, Object details) {
        logSuccess(action, resource, actionType, null, clientId, message, details);
    }

    /**
     * Log security events
     */
    @Async
    public void logSecurityEvent(String action, String resource, String userId, String clientId,
                                String message, String errorCode, Object details) {
        AuditLog auditLog = createBaseAuditLog(action, resource, AuditLog.ActionType.SECURITY_EVENT, userId, clientId);
        auditLog.setFailure(message, errorCode);
        auditLog.setDetails(serializeDetails(details));
        auditLog.setRequestInfo(getRequestInfo());
        
        auditLogRepository.save(auditLog);
        logger.warn("Security event logged: {} - {} by {} (client: {})", action, resource, userId, clientId);
    }

    /**
     * Log system events
     */
    @Async
    public void logSystemEvent(String action, String resource, String message, Object details) {
        AuditLog auditLog = createBaseAuditLog(action, resource, AuditLog.ActionType.SYSTEM_EVENT, "SYSTEM", null);
        auditLog.setSuccess(message);
        auditLog.setDetails(serializeDetails(details));
        
        auditLogRepository.save(auditLog);
        logger.info("System event logged: {} - {}", action, resource);
    }

    /**
     * Get audit logs with pagination and filtering
     */
    public Page<AuditLog> getAuditLogs(String clientId, String userId, AuditLog.ActionType actionType,
                                      AuditLog.AuditStatus status, LocalDateTime startTime, LocalDateTime endTime,
                                      int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        
        if (clientId != null && startTime != null && endTime != null) {
            return auditLogRepository.findByClientIdAndTimestampBetween(clientId, startTime, endTime, pageable);
        } else if (clientId != null) {
            return auditLogRepository.findByClientId(clientId, pageable);
        } else if (userId != null) {
            return auditLogRepository.findByUserId(userId, pageable);
        } else if (actionType != null) {
            return auditLogRepository.findByActionType(actionType, pageable);
        } else if (status != null) {
            return auditLogRepository.findByStatus(status, pageable);
        } else if (startTime != null && endTime != null) {
            return auditLogRepository.findByTimestampBetween(startTime, endTime, pageable);
        } else {
            return auditLogRepository.findAll(pageable);
        }
    }

    /**
     * Get audit statistics
     */
    public Map<String, Object> getAuditStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            Map<String, Object> stats = Map.of(
                "totalLogs", auditLogRepository.countByTimestampBetween(startTime, endTime),
                "successCount", auditLogRepository.countByStatus(AuditLog.AuditStatus.SUCCESS),
                "failureCount", auditLogRepository.countByStatus(AuditLog.AuditStatus.FAILURE),
                "errorCount", auditLogRepository.countByStatus(AuditLog.AuditStatus.ERROR),
                "actionTypeStats", auditLogRepository.getActionTypeStats(startTime, endTime),
                "statusStats", auditLogRepository.getStatusStats(startTime, endTime),
                "topClients", auditLogRepository.getTopClientsByActivity(startTime, endTime, PageRequest.of(0, 10))
            );
            return stats;
            
        } catch (Exception e) {
            logger.error("Failed to get audit statistics: {}", e.getMessage(), e);
            return Map.of("error", "Failed to retrieve statistics");
        }
    }

    /**
     * Get recent audit logs for a client
     */
    public Page<AuditLog> getRecentClientLogs(String clientId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findByClientId(clientId, pageable);
    }

    /**
     * Get error logs
     */
    public Page<AuditLog> getErrorLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return auditLogRepository.findErrors(pageable);
    }

    /**
     * Get slow operations
     */
    public Page<AuditLog> getSlowOperations(long thresholdMs, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "executionTimeMs"));
        return auditLogRepository.findSlowOperations(thresholdMs, pageable);
    }

    /**
     * Clean up old audit logs
     */
    @Async
    public void cleanupOldLogs(int retentionDays) {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
            long deletedCount = auditLogRepository.countByTimestampBefore(cutoffDate);
            auditLogRepository.deleteByTimestampBefore(cutoffDate);
            logger.info("Cleaned up {} old audit logs (older than {} days)", deletedCount, retentionDays);
        } catch (Exception e) {
            logger.error("Failed to cleanup old audit logs: {}", e.getMessage(), e);
        }
    }

    // Private helper methods
    private AuditLog createBaseAuditLog(String action, String resource, AuditLog.ActionType actionType,
                                       String userId, String clientId) {
        AuditLog auditLog = new AuditLog(action, resource, actionType, userId, clientId);
        auditLog.setRequestId(UUID.randomUUID().toString());
        return auditLog;
    }

    private String serializeDetails(Object details) {
        if (details == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException e) {
            logger.warn("Failed to serialize audit details: {}", e.getMessage());
            return details.toString();
        }
    }

    private void setRequestInfo(AuditLog auditLog) {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                auditLog.setIpAddress(getClientIpAddress(request));
                auditLog.setUserAgent(request.getHeader("User-Agent"));
                auditLog.setSessionId(request.getSession(false) != null ? request.getSession().getId() : null);
            }
        } catch (Exception e) {
            logger.debug("Could not set request info: {}", e.getMessage());
        }
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

    private Map<String, Object> getRequestInfo() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return Map.of(
                    "method", request.getMethod(),
                    "uri", request.getRequestURI(),
                    "queryString", request.getQueryString() != null ? request.getQueryString() : "",
                    "ipAddress", getClientIpAddress(request),
                    "userAgent", request.getHeader("User-Agent") != null ? request.getHeader("User-Agent") : "",
                    "referer", request.getHeader("Referer") != null ? request.getHeader("Referer") : ""
                );
            }
        } catch (Exception e) {
            logger.debug("Could not get request info: {}", e.getMessage());
        }
        return Map.of();
    }
}
