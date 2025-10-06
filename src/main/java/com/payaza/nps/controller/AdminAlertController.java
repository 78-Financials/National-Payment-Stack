package com.payaza.nps.controller;

import com.payaza.nps.model.Alert;
import com.payaza.nps.model.AlertSeverity;
import com.payaza.nps.service.AlertConfigurationService;
import com.payaza.nps.service.SharedAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Admin Controller for managing alert configuration, suppression, and analytics
 */
@RestController
@RequestMapping("/api/v1/admin/alerts")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminAlertController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminAlertController.class);
    
    @Autowired
    private AlertConfigurationService alertConfigService;
    
    @Autowired
    private SharedAlertService sharedAlertService;
    
    /**
     * Get current alert configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getAlertConfiguration() {
        try {
            Map<String, Object> config = alertConfigService.getCurrentConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Error getting alert configuration: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Suppress all alerts for a specified duration
     */
    @PostMapping("/suppress")
    public ResponseEntity<Map<String, String>> suppressAlerts(
            @RequestParam int hours,
            @RequestParam String reason,
            @RequestParam(required = false) String suppressedBy) {
        try {
            String admin = suppressedBy != null ? suppressedBy : "admin"; // Default to admin if not provided
            alertConfigService.suppressAlerts(hours, reason, admin);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alerts suppressed successfully");
            response.put("suppressedUntil", LocalDateTime.now().plusHours(hours).toString());
            response.put("reason", reason);
            response.put("suppressedBy", admin);
            
            logger.info("Alerts suppressed by {} for {} hours - Reason: {}", admin, hours, reason);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error suppressing alerts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Clear alert suppression
     */
    @PostMapping("/unsuppress")
    public ResponseEntity<Map<String, String>> clearSuppression() {
        try {
            alertConfigService.clearSuppression();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alert suppression cleared successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Alert suppression cleared");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error clearing alert suppression: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Enable alerts for a specific message type
     */
    @PostMapping("/message-types/{messageType}/enable")
    public ResponseEntity<Map<String, String>> enableMessageType(@PathVariable String messageType) {
        try {
            alertConfigService.enableMessageType(messageType);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alerts enabled for message type: " + messageType);
            response.put("messageType", messageType);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Alerts enabled for message type: {}", messageType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error enabling message type {}: {}", messageType, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Disable alerts for a specific message type
     */
    @PostMapping("/message-types/{messageType}/disable")
    public ResponseEntity<Map<String, String>> disableMessageType(@PathVariable String messageType) {
        try {
            alertConfigService.disableMessageType(messageType);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alerts disabled for message type: " + messageType);
            response.put("messageType", messageType);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Alerts disabled for message type: {}", messageType);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error disabling message type {}: {}", messageType, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Set enabled message types
     */
    @PutMapping("/message-types")
    public ResponseEntity<Map<String, String>> setEnabledMessageTypes(@RequestBody Set<String> messageTypes) {
        try {
            alertConfigService.setEnabledMessageTypes(messageTypes);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Enabled message types updated successfully");
            response.put("enabledTypes", messageTypes.toString());
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Enabled message types updated: {}", messageTypes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error setting enabled message types: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Suppress a specific alert type (messageType + severity)
     */
    @PostMapping("/types/{messageType}/{severity}/suppress")
    public ResponseEntity<Map<String, String>> suppressAlertType(
            @PathVariable String messageType,
            @PathVariable String severity) {
        try {
            AlertSeverity alertSeverity = AlertSeverity.valueOf(severity.toUpperCase());
            alertConfigService.suppressAlertType(messageType, alertSeverity);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alert type suppressed successfully");
            response.put("messageType", messageType);
            response.put("severity", severity);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Alert type suppressed: {} - {}", messageType, severity);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error suppressing alert type {} - {}: {}", messageType, severity, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Unsuppress a specific alert type
     */
    @PostMapping("/types/{messageType}/{severity}/unsuppress")
    public ResponseEntity<Map<String, String>> unsuppressAlertType(
            @PathVariable String messageType,
            @PathVariable String severity) {
        try {
            AlertSeverity alertSeverity = AlertSeverity.valueOf(severity.toUpperCase());
            alertConfigService.unsuppressAlertType(messageType, alertSeverity);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alert type unsuppressed successfully");
            response.put("messageType", messageType);
            response.put("severity", severity);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Alert type unsuppressed: {} - {}", messageType, severity);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error unsuppressing alert type {} - {}: {}", messageType, severity, e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get alert statistics and analytics
     */
    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAlertAnalytics() {
        try {
            Map<String, Object> analytics = alertConfigService.getAlertStatistics();
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            logger.error("Error getting alert analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Get recent alerts
     */
    @GetMapping("/recent")
    public ResponseEntity<List<Alert>> getRecentAlerts(@RequestParam(defaultValue = "50") int limit) {
        try {
            List<Alert> alerts = sharedAlertService.getRecentAlerts(limit);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error getting recent alerts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get alerts by message type
     */
    @GetMapping("/message-types/{messageType}")
    public ResponseEntity<List<Alert>> getAlertsByMessageType(
            @PathVariable String messageType,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<Alert> alerts = sharedAlertService.getAlertsByMessageType(messageType, limit);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error getting alerts for message type {}: {}", messageType, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Reset alert analytics
     */
    @PostMapping("/analytics/reset")
    public ResponseEntity<Map<String, String>> resetAnalytics() {
        try {
            alertConfigService.resetAnalytics();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Alert analytics reset successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Alert analytics reset");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error resetting analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Clean up old analytics data
     */
    @PostMapping("/analytics/cleanup")
    public ResponseEntity<Map<String, String>> cleanupAnalytics() {
        try {
            alertConfigService.cleanupOldAnalytics();
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Analytics cleanup completed successfully");
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Analytics cleanup completed");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error cleaning up analytics: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * Test alert system by triggering a test alert
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, String>> testAlert(
            @RequestParam String messageType,
            @RequestParam String severity,
            @RequestParam(required = false) String transactionId) {
        try {
            AlertSeverity alertSeverity = AlertSeverity.valueOf(severity.toUpperCase());
            String testTransactionId = transactionId != null ? transactionId : "TEST_" + System.currentTimeMillis();
            
            sharedAlertService.triggerMessageProcessingAlert(
                messageType, testTransactionId, "Test alert triggered by admin", alertSeverity);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Test alert triggered successfully");
            response.put("messageType", messageType);
            response.put("severity", severity);
            response.put("transactionId", testTransactionId);
            response.put("timestamp", LocalDateTime.now().toString());
            
            logger.info("Test alert triggered: {} - {} - {}", messageType, severity, testTransactionId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error triggering test alert: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
