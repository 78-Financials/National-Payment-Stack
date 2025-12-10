package com.payaza.nps.controller;

import com.payaza.nps.model.Alert;
import com.payaza.nps.model.AlertRule;
import com.payaza.nps.model.AlertSeverity;
import com.payaza.nps.model.AlertStatus;
import com.payaza.nps.repository.AlertRepository;
import com.payaza.nps.repository.AlertRuleRepository;
import com.payaza.nps.service.AlertEngine;
import com.payaza.nps.service.AlertHistoryService;
import com.payaza.nps.service.MetricsCollectionService;
import com.payaza.nps.service.NotificationService;
import com.payaza.nps.dto.MetricsDto;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for alert management and monitoring
 */
@RestController
@RequestMapping("/api/v1/alerts")
@CrossOrigin(origins = "*")
public class AlertController {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertController.class);
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private AlertRuleRepository alertRuleRepository;
    
    @Autowired
    private AlertEngine alertEngine;
    
    @Autowired
    private AlertHistoryService alertHistoryService;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private MetricsCollectionService metricsCollectionService;
    
    // ================================
    // ALERT RULES MANAGEMENT
    // ================================
    
    /**
     * Get all alert rules
     */
    @GetMapping("/rules")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<AlertRule>> getAllAlertRules() {
        try {
            List<AlertRule> rules = alertRuleRepository.findAll();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Error getting alert rules: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get enabled alert rules
     */
    @GetMapping("/rules/enabled")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<AlertRule>> getEnabledAlertRules() {
        try {
            List<AlertRule> rules = alertRuleRepository.findByEnabledTrue();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Error getting enabled alert rules: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get alert rule by ID
     */
    @GetMapping("/rules/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AlertRule> getAlertRule(@PathVariable Long id) {
        try {
            Optional<AlertRule> rule = alertRuleRepository.findById(id);
            return rule.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting alert rule {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Create new alert rule
     */
    @PostMapping("/rules")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AlertRule> createAlertRule(@Valid @RequestBody AlertRule rule) {
        try {
            rule.setCreatedAt(LocalDateTime.now());
            rule.setUpdatedAt(LocalDateTime.now());
            rule.setCreatedBy("ADMIN"); // TODO: Get from security context
            
            AlertRule savedRule = alertRuleRepository.save(rule);
            logger.info("Created alert rule: {}", savedRule.getName());
            
            return ResponseEntity.ok(savedRule);
        } catch (Exception e) {
            logger.error("Error creating alert rule: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Update alert rule
     */
    @PutMapping("/rules/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AlertRule> updateAlertRule(@PathVariable Long id, @Valid @RequestBody AlertRule rule) {
        try {
            Optional<AlertRule> existingRule = alertRuleRepository.findById(id);
            if (existingRule.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            AlertRule existing = existingRule.get();
            existing.setName(rule.getName());
            existing.setDescription(rule.getDescription());
            existing.setConditionExpression(rule.getConditionExpression());
            existing.setSeverity(rule.getSeverity());
            existing.setNotificationChannels(rule.getNotificationChannels());
            existing.setEscalationPolicy(rule.getEscalationPolicy());
            existing.setEnabled(rule.getEnabled());
            existing.setEvaluationIntervalSeconds(rule.getEvaluationIntervalSeconds());
            existing.setSuppressionWindowSeconds(rule.getSuppressionWindowSeconds());
            existing.setMaxAlertsPerHour(rule.getMaxAlertsPerHour());
            existing.setMetricType(rule.getMetricType());
            existing.setMetricName(rule.getMetricName());
            existing.setUpdatedAt(LocalDateTime.now());
            
            AlertRule savedRule = alertRuleRepository.save(existing);
            logger.info("Updated alert rule: {}", savedRule.getName());
            
            return ResponseEntity.ok(savedRule);
        } catch (Exception e) {
            logger.error("Error updating alert rule {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Delete alert rule
     */
    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteAlertRule(@PathVariable Long id) {
        try {
            Optional<AlertRule> rule = alertRuleRepository.findById(id);
            if (rule.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            alertRuleRepository.deleteById(id);
            logger.info("Deleted alert rule: {}", rule.get().getName());
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            logger.error("Error deleting alert rule {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Enable/disable alert rule
     */
    @PatchMapping("/rules/{id}/toggle")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<AlertRule> toggleAlertRule(@PathVariable Long id) {
        try {
            Optional<AlertRule> rule = alertRuleRepository.findById(id);
            if (rule.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            AlertRule existing = rule.get();
            existing.setEnabled(!existing.getEnabled());
            existing.setUpdatedAt(LocalDateTime.now());
            
            AlertRule savedRule = alertRuleRepository.save(existing);
            logger.info("Toggled alert rule {}: enabled={}", savedRule.getName(), savedRule.getEnabled());
            
            return ResponseEntity.ok(savedRule);
        } catch (Exception e) {
            logger.error("Error toggling alert rule {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ================================
    // ALERT MANAGEMENT
    // ================================
    
    /**
     * Get all alerts with pagination
     */
    @GetMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Page<Alert>> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) AlertStatus status,
            @RequestParam(required = false) AlertSeverity severity) {
        
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Alert> alerts;
            if (status != null && severity != null) {
                alerts = alertRepository.findBySeverityAndStatus(severity, status, pageable);
            } else if (status != null) {
                alerts = alertRepository.findByStatus(status, pageable);
            } else if (severity != null) {
                alerts = alertRepository.findBySeverity(severity, pageable);
            } else {
                alerts = alertRepository.findAll(pageable);
            }
            
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error getting alerts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get active alerts
     */
    @GetMapping("/active")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Alert>> getActiveAlerts() {
        try {
            List<Alert> alerts = alertRepository.findByStatusIn(List.of(AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED));
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error getting active alerts: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get alerts by severity
     */
    @GetMapping("/severity/{severity}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<Alert>> getAlertsBySeverity(@PathVariable AlertSeverity severity) {
        try {
            List<Alert> alerts = alertRepository.findBySeverity(severity);
            return ResponseEntity.ok(alerts);
        } catch (Exception e) {
            logger.error("Error getting alerts by severity {}: {}", severity, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get alert by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Alert> getAlert(@PathVariable Long id) {
        try {
            Optional<Alert> alert = alertRepository.findById(id);
            return alert.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting alert {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Acknowledge alert
     */
    @PatchMapping("/{id}/acknowledge")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Alert> acknowledgeAlert(@PathVariable Long id, @RequestParam String acknowledgedBy) {
        try {
            Optional<Alert> alertOpt = alertRepository.findById(id);
            if (alertOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Alert alert = alertOpt.get();
            alert.acknowledge(acknowledgedBy);
            Alert savedAlert = alertRepository.save(alert);
            
            // Record in history
            alertHistoryService.recordAlertAction(id, com.payaza.nps.model.AlertAction.ACKNOWLEDGED, 
                AlertStatus.ACTIVE, AlertStatus.ACKNOWLEDGED, acknowledgedBy, "Alert acknowledged");
            
            logger.info("Alert acknowledged: {} by {}", alert.getName(), acknowledgedBy);
            
            return ResponseEntity.ok(savedAlert);
        } catch (Exception e) {
            logger.error("Error acknowledging alert {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Resolve alert
     */
    @PatchMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Alert> resolveAlert(@PathVariable Long id, @RequestParam String resolvedBy) {
        try {
            Optional<Alert> alertOpt = alertRepository.findById(id);
            if (alertOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Alert alert = alertOpt.get();
            AlertStatus oldStatus = alert.getStatus();
            alert.resolve(resolvedBy);
            Alert savedAlert = alertRepository.save(alert);
            
            // Record in history
            alertHistoryService.recordAlertAction(id, com.payaza.nps.model.AlertAction.RESOLVED, 
                oldStatus, AlertStatus.RESOLVED, resolvedBy, "Alert resolved");
            
            // Send recovery notification
            notificationService.sendRecoveryNotification(alert);
            
            logger.info("Alert resolved: {} by {}", alert.getName(), resolvedBy);
            
            return ResponseEntity.ok(savedAlert);
        } catch (Exception e) {
            logger.error("Error resolving alert {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Suppress alert
     */
    @PatchMapping("/{id}/suppress")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Alert> suppressAlert(@PathVariable Long id, @RequestParam String suppressedBy) {
        try {
            Optional<Alert> alertOpt = alertRepository.findById(id);
            if (alertOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Alert alert = alertOpt.get();
            AlertStatus oldStatus = alert.getStatus();
            alert.suppress();
            Alert savedAlert = alertRepository.save(alert);
            
            // Record in history
            alertHistoryService.recordAlertAction(id, com.payaza.nps.model.AlertAction.SUPPRESSED, 
                oldStatus, AlertStatus.SUPPRESSED, suppressedBy, "Alert suppressed");
            
            logger.info("Alert suppressed: {} by {}", alert.getName(), suppressedBy);
            
            return ResponseEntity.ok(savedAlert);
        } catch (Exception e) {
            logger.error("Error suppressing alert {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get alert history
     */
    @GetMapping("/{id}/history")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<com.payaza.nps.model.AlertHistory>> getAlertHistory(@PathVariable Long id) {
        try {
            List<com.payaza.nps.model.AlertHistory> history = alertHistoryService.getAlertHistory(id);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error getting alert history for {}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ================================
    // ALERT DASHBOARD & ANALYTICS
    // ================================
    
    /**
     * Get alert dashboard summary
     */
    @GetMapping("/dashboard/summary")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, Object>> getAlertDashboardSummary() {
        try {
            Map<String, Object> summary = new java.util.HashMap<>();
            
            // Active alert counts by severity
            List<Object[]> activeCounts = alertRepository.findActiveAlertCountsBySeverity();
            Map<String, Long> activeAlerts = new java.util.HashMap<>();
            for (Object[] count : activeCounts) {
                activeAlerts.put(count[0].toString(), (Long) count[1]);
            }
            
            // Today's alert counts
            long todayTotal = alertRepository.countToday();
            long todayCritical = alertRepository.countTodayBySeverity(AlertSeverity.CRITICAL);
            long todayWarning = alertRepository.countTodayBySeverity(AlertSeverity.WARNING);
            long todayInfo = alertRepository.countTodayBySeverity(AlertSeverity.INFO);
            
            // Recent trends (last 7 days)
            List<Object[]> trends = alertRepository.findAlertTrends(LocalDateTime.now().minusDays(7));
            
            summary.put("activeAlerts", activeAlerts);
            summary.put("todayStats", Map.of(
                "total", todayTotal,
                "critical", todayCritical,
                "warning", todayWarning,
                "info", todayInfo
            ));
            summary.put("recentTrends", trends);
            summary.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            logger.error("Error getting alert dashboard summary: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get recent alert history
     */
    @GetMapping("/history/recent")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<com.payaza.nps.model.AlertHistory>> getRecentAlertHistory(
            @RequestParam(defaultValue = "24") int hours) {
        try {
            List<com.payaza.nps.model.AlertHistory> history = alertHistoryService.getRecentAlertHistory(hours);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error getting recent alert history: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // ================================
    // ALERT SYSTEM MANAGEMENT
    // ================================
    
    /**
     * Manually trigger alert evaluation
     */
    @PostMapping("/evaluate")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> triggerAlertEvaluation() {
        try {
            // Collect current metrics
            MetricsDto.TransactionMetrics txMetrics = metricsCollectionService.collectTransactionMetrics();
            MetricsDto.SystemMetrics sysMetrics = metricsCollectionService.collectSystemMetrics();
            MetricsDto.BankMetrics bankMetrics = metricsCollectionService.collectBankMetrics();
            
            MetricsDto.CombinedMetrics combinedMetrics = new MetricsDto.CombinedMetrics(
                txMetrics, sysMetrics, bankMetrics);
            
            // Evaluate rules
            alertEngine.evaluateRules(combinedMetrics);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Alert evaluation triggered successfully",
                "timestamp", LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Error triggering alert evaluation: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to trigger alert evaluation: " + e.getMessage()
            ));
        }
    }
    
    /**
     * Test notification channels
     */
    @PostMapping("/test-notification")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> testNotification(@RequestParam String channel) {
        try {
            // Create test alert
            Alert testAlert = new Alert();
            testAlert.setAlertRuleId(0L);
            testAlert.setName("Test Alert");
            testAlert.setMessage("This is a test notification from the NPS Alerting System");
            testAlert.setSeverity(AlertSeverity.INFO);
            testAlert.setStatus(AlertStatus.ACTIVE);
            testAlert.setNotificationChannels(List.of(channel));
            testAlert.setCreatedAt(LocalDateTime.now());
            
            // Send test notification
            notificationService.sendAlert(testAlert);
            
            return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Test notification sent via " + channel,
                "timestamp", LocalDateTime.now().toString()
            ));
        } catch (Exception e) {
            logger.error("Error sending test notification: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "status", "error",
                "message", "Failed to send test notification: " + e.getMessage()
            ));
        }
    }
}
