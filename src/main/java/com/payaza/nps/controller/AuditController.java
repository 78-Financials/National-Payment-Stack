package com.payaza.nps.controller;

import com.payaza.nps.annotation.Auditable;
import com.payaza.nps.model.AuditLog;
import com.payaza.nps.service.AuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller for viewing audit logs and system activity
 */
@RestController
@RequestMapping("/api/v1/admin/audit")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AuditController {

    private static final Logger logger = LoggerFactory.getLogger(AuditController.class);

    @Autowired
    private AuditService auditService;

    /**
     * Get audit logs with filtering and pagination
     */
    @GetMapping
    @Auditable(action = "VIEW_AUDIT_LOGS", resource = "AuditLog", actionType = AuditLog.ActionType.READ, message = "Admin viewed audit logs")
    public ResponseEntity<Page<AuditLog>> getAuditLogs(
            @RequestParam(required = false) String clientId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) AuditLog.ActionType actionType,
            @RequestParam(required = false) AuditLog.AuditStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            logger.info("Fetching audit logs - clientId: {}, userId: {}, actionType: {}, status: {}", 
                       clientId, userId, actionType, status);
            
            Page<AuditLog> auditLogs = auditService.getAuditLogs(
                clientId, userId, actionType, status, startTime, endTime, page, size);
            
            return ResponseEntity.ok(auditLogs);
            
        } catch (Exception e) {
            logger.error("Error fetching audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audit statistics for a date range
     */
    @GetMapping("/statistics")
    @Auditable(action = "VIEW_AUDIT_STATISTICS", resource = "AuditLog", actionType = AuditLog.ActionType.READ, message = "Admin viewed audit statistics")
    public ResponseEntity<Map<String, Object>> getAuditStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        
        try {
            logger.info("Fetching audit statistics from {} to {}", startTime, endTime);
            
            Map<String, Object> statistics = auditService.getAuditStatistics(startTime, endTime);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error fetching audit statistics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get recent audit logs for a specific client
     */
    @GetMapping("/client/{clientId}/recent")
    @Auditable(action = "VIEW_CLIENT_AUDIT_LOGS", resource = "AuditLog", actionType = AuditLog.ActionType.READ, message = "Admin viewed client audit logs")
    public ResponseEntity<Page<AuditLog>> getRecentClientLogs(
            @PathVariable String clientId,
            @RequestParam(defaultValue = "10") int limit) {
        
        try {
            logger.info("Fetching recent audit logs for client: {} (limit: {})", clientId, limit);
            
            Page<AuditLog> auditLogs = auditService.getRecentClientLogs(clientId, limit);
            
            return ResponseEntity.ok(auditLogs);
            
        } catch (Exception e) {
            logger.error("Error fetching recent client audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get error logs
     */
    @GetMapping("/errors")
    @Auditable(action = "VIEW_ERROR_LOGS", resource = "AuditLog", actionType = AuditLog.ActionType.READ, message = "Admin viewed error logs")
    public ResponseEntity<Page<AuditLog>> getErrorLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            logger.info("Fetching error logs (page: {}, size: {})", page, size);
            
            Page<AuditLog> errorLogs = auditService.getErrorLogs(page, size);
            
            return ResponseEntity.ok(errorLogs);
            
        } catch (Exception e) {
            logger.error("Error fetching error logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get slow operations
     */
    @GetMapping("/slow-operations")
    @Auditable(action = "VIEW_SLOW_OPERATIONS", resource = "AuditLog", actionType = AuditLog.ActionType.READ, message = "Admin viewed slow operations")
    public ResponseEntity<Page<AuditLog>> getSlowOperations(
            @RequestParam(defaultValue = "1000") long thresholdMs,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            logger.info("Fetching slow operations (threshold: {}ms, page: {}, size: {})", thresholdMs, page, size);
            
            Page<AuditLog> slowOperations = auditService.getSlowOperations(thresholdMs, page, size);
            
            return ResponseEntity.ok(slowOperations);
            
        } catch (Exception e) {
            logger.error("Error fetching slow operations: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Clean up old audit logs
     */
    @PostMapping("/cleanup")
    @Auditable(action = "CLEANUP_AUDIT_LOGS", resource = "AuditLog", actionType = AuditLog.ActionType.DELETE, message = "Admin cleaned up old audit logs")
    public ResponseEntity<Map<String, String>> cleanupOldLogs(
            @RequestParam(defaultValue = "90") int retentionDays) {
        
        try {
            logger.info("Cleaning up audit logs older than {} days", retentionDays);
            
            auditService.cleanupOldLogs(retentionDays);
            
            Map<String, String> response = Map.of(
                "message", "Audit log cleanup initiated",
                "retentionDays", String.valueOf(retentionDays),
                "timestamp", LocalDateTime.now().toString()
            );
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error cleaning up audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get audit log by ID
     */
    @GetMapping("/{id}")
    @Auditable(action = "VIEW_AUDIT_LOG_DETAILS", resource = "AuditLog", actionType = AuditLog.ActionType.READ, message = "Admin viewed audit log details")
    public ResponseEntity<AuditLog> getAuditLogById(@PathVariable Long id) {
        try {
            logger.info("Fetching audit log details for ID: {}", id);
            
            // This would need to be implemented in AuditService
            // For now, return a placeholder response
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error fetching audit log details: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Search audit logs by action pattern
     */
    @GetMapping("/search")
    @Auditable(action = "SEARCH_AUDIT_LOGS", resource = "AuditLog", actionType = AuditLog.ActionType.READ, message = "Admin searched audit logs")
    public ResponseEntity<Page<AuditLog>> searchAuditLogs(
            @RequestParam String actionPattern,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            logger.info("Searching audit logs for action pattern: {}", actionPattern);
            
            // This would need to be implemented in AuditService
            // For now, return a placeholder response
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error searching audit logs: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
