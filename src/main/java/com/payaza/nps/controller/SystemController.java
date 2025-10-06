package com.payaza.nps.controller;

import com.payaza.nps.service.SystemMonitoringService;
import com.payaza.nps.service.LogAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * System Monitoring and Health Check Controller
 */
@RestController
@RequestMapping("/api/v1/admin/system")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class SystemController {
    
    private static final Logger logger = LoggerFactory.getLogger(SystemController.class);
    
    @Autowired
    private SystemMonitoringService systemMonitoringService;
    
    @Autowired
    private LogAggregationService logAggregationService;
    
    /**
     * Get system health status
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        try {
            Map<String, Object> health = systemMonitoringService.getSystemHealth();
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            logger.error("Failed to get system health: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get detailed system metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        try {
            Map<String, Object> metrics = systemMonitoringService.getSystemMetrics();
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            logger.error("Failed to get system metrics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get performance metrics
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            Map<String, Object> performance = systemMonitoringService.getPerformanceMetrics(from, to);
            return ResponseEntity.ok(performance);
        } catch (Exception e) {
            logger.error("Failed to get performance metrics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get database health
     */
    @GetMapping("/database/health")
    public ResponseEntity<Map<String, Object>> getDatabaseHealth() {
        try {
            Map<String, Object> dbHealth = systemMonitoringService.getDatabaseHealth();
            return ResponseEntity.ok(dbHealth);
        } catch (Exception e) {
            logger.error("Failed to get database health: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get external service health
     */
    @GetMapping("/external-services/health")
    public ResponseEntity<Map<String, Object>> getExternalServicesHealth() {
        try {
            Map<String, Object> servicesHealth = systemMonitoringService.getExternalServicesHealth();
            return ResponseEntity.ok(servicesHealth);
        } catch (Exception e) {
            logger.error("Failed to get external services health: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get system logs
     */
    @GetMapping("/logs")
    public ResponseEntity<List<Map<String, Object>>> getSystemLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String search) {
        try {
            List<Map<String, Object>> logs = logAggregationService.getSystemLogs(
                page, size, level, source, from, to, search);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Failed to get system logs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Search logs
     */
    @GetMapping("/logs/search")
    public ResponseEntity<List<Map<String, Object>>> searchLogs(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            List<Map<String, Object>> logs = logAggregationService.searchLogs(query, page, size, from, to);
            return ResponseEntity.ok(logs);
        } catch (Exception e) {
            logger.error("Failed to search logs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get log statistics
     */
    @GetMapping("/logs/statistics")
    public ResponseEntity<Map<String, Object>> getLogStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        try {
            Map<String, Object> stats = logAggregationService.getLogStatistics(from, to);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Failed to get log statistics: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Export logs
     */
    @GetMapping("/logs/export")
    public ResponseEntity<byte[]> exportLogs(
            @RequestParam String format,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String source) {
        try {
            byte[] logData = logAggregationService.exportLogs(format, from, to, level, source);
            
            String filename = String.format("system_logs_%s.%s", 
                LocalDateTime.now().toString().replace(":", "-"), format.toLowerCase());
            
            return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=" + filename)
                .body(logData);
        } catch (Exception e) {
            logger.error("Failed to export logs: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get system alerts
     */
    @GetMapping("/alerts")
    public ResponseEntity<List<Map<String, Object>>> getSystemAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String severity) {
        try {
            List<Map<String, Object>> alerts = systemMonitoringService.getSystemAlerts(page, size, severity);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Failed to get system alerts: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Acknowledge system alert
     */
    @PostMapping("/alerts/{alertId}/acknowledge")
    public ResponseEntity<Map<String, String>> acknowledgeSystemAlert(@PathVariable String alertId) {
        try {
            systemMonitoringService.acknowledgeSystemAlert(alertId);
            logger.info("System alert acknowledged: {}", alertId);
            return ResponseEntity.ok(Map.of("message", "Alert acknowledged successfully"));
        } catch (Exception e) {
            logger.error("Failed to acknowledge system alert: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get system configuration
     */
    @GetMapping("/configuration")
    public ResponseEntity<Map<String, Object>> getSystemConfiguration() {
        try {
            Map<String, Object> config = systemMonitoringService.getSystemConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Failed to get system configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update system configuration
     */
    @PutMapping("/configuration")
    public ResponseEntity<Map<String, String>> updateSystemConfiguration(@RequestBody Map<String, Object> config) {
        try {
            systemMonitoringService.updateSystemConfiguration(config);
            logger.info("System configuration updated");
            return ResponseEntity.ok(Map.of("message", "System configuration updated successfully"));
        } catch (Exception e) {
            logger.error("Failed to update system configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
