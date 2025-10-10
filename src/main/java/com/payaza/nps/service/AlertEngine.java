package com.payaza.nps.service;

import com.payaza.nps.dto.MetricsDto;
import com.payaza.nps.model.Alert;
import com.payaza.nps.model.AlertRule;
import com.payaza.nps.model.AlertStatus;
import com.payaza.nps.repository.AlertRepository;
import com.payaza.nps.repository.AlertRuleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for evaluating alert rules and triggering alerts
 */
@Service
public class AlertEngine {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertEngine.class);
    
    @Autowired
    private AlertRuleRepository alertRuleRepository;
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AlertHistoryService alertHistoryService;
    
    // Pattern for parsing condition expressions
    private static final Pattern CONDITION_PATTERN = Pattern.compile(
        "([a-zA-Z_]+(?:\\.[a-zA-Z_]+)*)\\s*(<|>|<=|>=|==|!=)\\s*([\\d.]+)"
    );
    
    /**
     * Evaluate all enabled alert rules against current metrics
     */
    @Transactional
    public void evaluateRules(MetricsDto.CombinedMetrics metrics) {
        try {
            List<AlertRule> activeRules = alertRuleRepository.findByEnabledTrue();
            logger.debug("Evaluating {} active alert rules", activeRules.size());
            
            for (AlertRule rule : activeRules) {
                try {
                    evaluateRule(rule, metrics);
                } catch (Exception e) {
                    logger.error("Error evaluating rule '{}': {}", rule.getName(), e.getMessage(), e);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating alert rules: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Evaluate a specific alert rule
     */
    @Transactional
    public void evaluateRule(AlertRule rule, MetricsDto.CombinedMetrics metrics) {
        try {
            boolean conditionMet = evaluateCondition(rule.getConditionExpression(), metrics);
            
            if (conditionMet) {
                // Check if we already have an active alert for this rule and metric
                if (!hasActiveAlert(rule, metrics)) {
                    triggerAlert(rule, metrics);
                }
            } else {
                // Condition not met - check if we need to resolve existing alerts
                resolveExistingAlerts(rule, metrics);
            }
            
        } catch (Exception e) {
            logger.error("Error evaluating rule '{}': {}", rule.getName(), e.getMessage(), e);
        }
    }
    
    /**
     * Evaluate a condition expression against metrics
     */
    public boolean evaluateCondition(String condition, MetricsDto.CombinedMetrics metrics) {
        try {
            Matcher matcher = CONDITION_PATTERN.matcher(condition);
            if (!matcher.find()) {
                logger.warn("Invalid condition format: {}", condition);
                return false;
            }
            
            String metricPath = matcher.group(1);
            String operator = matcher.group(2);
            double threshold = Double.parseDouble(matcher.group(3));
            
            Double actualValue = getMetricValue(metricPath, metrics);
            if (actualValue == null) {
                logger.debug("Metric value not found for path: {}", metricPath);
                return false;
            }
            
            boolean result = compareValues(actualValue, operator, threshold);
            logger.debug("Condition '{}': {} {} {} = {}", condition, actualValue, operator, threshold, result);
            
            return result;
            
        } catch (Exception e) {
            logger.error("Error evaluating condition '{}': {}", condition, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get metric value by path (e.g., "transaction_metrics.success_rate")
     */
    private Double getMetricValue(String metricPath, MetricsDto.CombinedMetrics metrics) {
        try {
            String[] parts = metricPath.split("\\.");
            String mainMetric = parts[0];
            String subMetric = parts.length > 1 ? parts[1] : null;
            
            switch (mainMetric.toLowerCase()) {
                case "transaction_metrics":
                case "transaction":
                    return getTransactionMetricValue(subMetric, metrics.getTransactionMetrics());
                    
                case "system_metrics":
                case "system":
                    return getSystemMetricValue(subMetric, metrics.getSystemMetrics());
                    
                case "bank_metrics":
                case "bank":
                    return getBankMetricValue(subMetric, metrics.getBankMetrics());
                    
                default:
                    logger.warn("Unknown metric category: {}", mainMetric);
                    return null;
            }
            
        } catch (Exception e) {
            logger.error("Error getting metric value for path '{}': {}", metricPath, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Get transaction metric value
     */
    private Double getTransactionMetricValue(String metricName, MetricsDto.TransactionMetrics txMetrics) {
        if (txMetrics == null) return null;
        
        switch (metricName.toLowerCase()) {
            case "success_rate": return txMetrics.getSuccessRate();
            case "failure_rate":
            case "error_rate": return txMetrics.getFailureRate();
            case "timeout_rate": return txMetrics.getTimeoutRate();
            case "total_transactions": return txMetrics.getTotalTransactions().doubleValue();
            case "successful_transactions": return txMetrics.getSuccessfulTransactions().doubleValue();
            case "failed_transactions": return txMetrics.getFailedTransactions().doubleValue();
            case "timeout_transactions": return txMetrics.getTimeoutTransactions().doubleValue();
            case "avg_processing_time_ms": return txMetrics.getAverageProcessingTimeMs();
            case "avg_processing_time_seconds": return txMetrics.getAverageProcessingTimeSeconds();
            case "max_processing_time_ms": return txMetrics.getMaxProcessingTimeMs();
            case "min_processing_time_ms": return txMetrics.getMinProcessingTimeMs();
            case "high_value_transactions": return txMetrics.getHighValueTransactions().doubleValue();
            case "total_volume": return txMetrics.getTotalVolume().doubleValue();
            default:
                logger.warn("Unknown transaction metric: {}", metricName);
                return null;
        }
    }
    
    /**
     * Get system metric value
     */
    private Double getSystemMetricValue(String metricName, MetricsDto.SystemMetrics sysMetrics) {
        if (sysMetrics == null) return null;
        
        switch (metricName.toLowerCase()) {
            case "cpu_usage": return sysMetrics.getCpuUsage();
            case "memory_usage": return sysMetrics.getMemoryUsage();
            case "active_threads": return sysMetrics.getActiveThreads().doubleValue();
            case "database_connections": return sysMetrics.getDatabaseConnections().doubleValue();
            case "heap_memory_used": return sysMetrics.getHeapMemoryUsed().doubleValue();
            case "heap_memory_max": return sysMetrics.getHeapMemoryMax().doubleValue();
            case "response_time_ms": return sysMetrics.getResponseTimeMs();
            case "error_rate": return sysMetrics.getErrorRate().doubleValue();
            default:
                logger.warn("Unknown system metric: {}", metricName);
                return null;
        }
    }
    
    /**
     * Get bank metric value
     */
    private Double getBankMetricValue(String metricName, MetricsDto.BankMetrics bankMetrics) {
        if (bankMetrics == null) return null;
        
        switch (metricName.toLowerCase()) {
            case "overall_success_rate": return bankMetrics.getOverallBankSuccessRate();
            case "total_banks": return bankMetrics.getTotalBanks().doubleValue();
            default:
                logger.warn("Unknown bank metric: {}", metricName);
                return null;
        }
    }
    
    /**
     * Compare values based on operator
     */
    private boolean compareValues(Double actualValue, String operator, double threshold) {
        switch (operator) {
            case "<": return actualValue < threshold;
            case ">": return actualValue > threshold;
            case "<=": return actualValue <= threshold;
            case ">=": return actualValue >= threshold;
            case "==": return Math.abs(actualValue - threshold) < 0.001;
            case "!=": return Math.abs(actualValue - threshold) >= 0.001;
            default:
                logger.warn("Unknown operator: {}", operator);
                return false;
        }
    }
    
    /**
     * Check if there's already an active alert for this rule and metric
     */
    private boolean hasActiveAlert(AlertRule rule, MetricsDto.CombinedMetrics metrics) {
        try {
            LocalDateTime since = LocalDateTime.now().minusSeconds(rule.getSuppressionWindowSeconds());
            List<Alert> existingAlerts = alertRepository.findByAlertRuleIdAndStatus(rule.getId(), AlertStatus.ACTIVE);
            
            // Check if any existing alerts are within the suppression window
            for (Alert alert : existingAlerts) {
                if (alert.getCreatedAt().isAfter(since)) {
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error checking for existing alerts: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Trigger a new alert
     */
    @Transactional
    public void triggerAlert(AlertRule rule, MetricsDto.CombinedMetrics metrics) {
        try {
            // Create new alert
            Alert alert = new Alert();
            alert.setAlertRuleId(rule.getId());
            alert.setName(rule.getName());
            alert.setMessage(generateAlertMessage(rule, metrics));
            alert.setSeverity(rule.getSeverity());
            alert.setStatus(AlertStatus.ACTIVE);
            alert.setNotificationChannels(rule.getNotificationChannels());
            alert.setMetricType(rule.getMetricType());
            alert.setMetricName(rule.getMetricName());
            alert.setContext(generateAlertContext(metrics));
            
            // Set metric and threshold values
            Double metricValue = getMetricValue(extractMetricPath(rule.getConditionExpression()), metrics);
            alert.setMetricValue(BigDecimal.valueOf((metricValue) == null ? 0 : metricValue));
            Double thresholdValue  = extractThresholdValue(rule.getConditionExpression());
            alert.setThresholdValue((thresholdValue) == null ? BigDecimal.valueOf(0L) : BigDecimal.valueOf(thresholdValue));
            
            // Save alert
            Alert savedAlert = alertRepository.save(alert);
            
            // Record in history
            alertHistoryService.recordAlertAction(savedAlert.getId(), 
                com.payaza.nps.model.AlertAction.CREATED, null, "Alert triggered by rule evaluation");
            
            // Send notifications
            notificationService.sendAlert(savedAlert);
            
            logger.info("Alert triggered: {} ({} - {})", savedAlert.getName(), 
                       savedAlert.getSeverity(), savedAlert.getMetricValue());
            
        } catch (Exception e) {
            logger.error("Error triggering alert for rule '{}': {}", rule.getName(), e.getMessage(), e);
        }
    }
    
    /**
     * Resolve existing alerts when condition is no longer met
     */
    @Transactional
    public void resolveExistingAlerts(AlertRule rule, MetricsDto.CombinedMetrics metrics) {
        try {
            List<Alert> activeAlerts = alertRepository.findByAlertRuleIdAndStatus(rule.getId(), AlertStatus.ACTIVE);
            
            for (Alert alert : activeAlerts) {
                alert.resolve("SYSTEM");
                alertRepository.save(alert);
                
                // Record in history
                alertHistoryService.recordAlertAction(alert.getId(), 
                    com.payaza.nps.model.AlertAction.RESOLVED, AlertStatus.ACTIVE, AlertStatus.RESOLVED, 
                    "SYSTEM", "Condition no longer met");
                
                logger.info("Alert resolved: {} (condition no longer met)", alert.getName());
            }
            
        } catch (Exception e) {
            logger.error("Error resolving alerts for rule '{}': {}", rule.getName(), e.getMessage(), e);
        }
    }
    
    /**
     * Generate alert message
     */
    private String generateAlertMessage(AlertRule rule, MetricsDto.CombinedMetrics metrics) {
        String baseMessage = rule.getDescription() != null ? rule.getDescription() : rule.getName();
        
        // Add metric information if available
        String metricPath = extractMetricPath(rule.getConditionExpression());
        Double metricValue = getMetricValue(metricPath, metrics);
        
        if (metricValue != null) {
            return String.format("%s - Current value: %.2f", baseMessage, metricValue);
        }
        
        return baseMessage;
    }
    
    /**
     * Generate alert context (JSON string with additional information)
     */
    private String generateAlertContext(MetricsDto.CombinedMetrics metrics) {
        try {
            Map<String, Object> context = new java.util.HashMap<>();
            context.put("timestamp", LocalDateTime.now().toString());
            context.put("evaluation_time", LocalDateTime.now().toString());
            
            // Add relevant metrics to context
            if (metrics.getTransactionMetrics() != null) {
                context.put("total_transactions", metrics.getTransactionMetrics().getTotalTransactions());
                context.put("success_rate", metrics.getTransactionMetrics().getSuccessRate());
            }
            
            if (metrics.getSystemMetrics() != null) {
                context.put("cpu_usage", metrics.getSystemMetrics().getCpuUsage());
                context.put("memory_usage", metrics.getSystemMetrics().getMemoryUsage());
            }
            
            // Convert to JSON string (simplified)
            return context.toString();
            
        } catch (Exception e) {
            logger.error("Error generating alert context: {}", e.getMessage(), e);
            return "{}";
        }
    }
    
    /**
     * Extract metric path from condition expression
     */
    private String extractMetricPath(String condition) {
        Matcher matcher = CONDITION_PATTERN.matcher(condition);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    
    /**
     * Extract threshold value from condition expression
     */
    private Double extractThresholdValue(String condition) {
        Matcher matcher = CONDITION_PATTERN.matcher(condition);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(3));
            } catch (NumberFormatException e) {
                logger.warn("Invalid threshold value in condition: {}", condition);
            }
        }
        return null;
    }
}
