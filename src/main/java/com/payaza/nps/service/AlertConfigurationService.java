package com.payaza.nps.service;

import com.payaza.nps.model.AlertSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing alert configuration, suppression, and analytics
 */
@Service
public class AlertConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AlertConfigurationService.class);
    
    // Alert suppression settings
    private LocalDateTime suppressionEndTime = null;
    private String suppressionReason = null;
    private String suppressedBy = null;
    
    // Message type filtering
    private final Set<String> enabledMessageTypes = new HashSet<>();
    
    // Alert type suppression (messageType + severity combinations)
    private final Set<String> suppressedAlertTypes = new HashSet<>();
    
    // Analytics tracking
    private final Map<String, Integer> alertCounts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lastAlertTimes = new ConcurrentHashMap<>();
    
    // Default configuration
    @Value("${nps.alerts.default-enabled-message-types:PACS008,PACS002,ACMT023,ACMT024,PACS028}")
    private String defaultEnabledMessageTypes;
    
    @Value("${nps.alerts.default-suppression-duration-hours:0}")
    private int defaultSuppressionDurationHours;
    
    @Value("${nps.alerts.max-alerts-per-hour:100}")
    private int maxAlertsPerHour;
    
    @Value("${nps.alerts.analytics-retention-days:30}")
    private int analyticsRetentionDays;
    
    /**
     * Initialize default configuration
     */
    public void initializeDefaults() {
        // Set default enabled message types
        String[] types = defaultEnabledMessageTypes.split(",");
        enabledMessageTypes.addAll(Arrays.asList(types));
        
        logger.info("Alert configuration initialized with enabled message types: {}", enabledMessageTypes);
    }
    
    /**
     * Check if alerts are currently suppressed
     */
    public boolean isAlertSuppressed() {
        if (suppressionEndTime == null) {
            return false;
        }
        
        boolean suppressed = LocalDateTime.now().isBefore(suppressionEndTime);
        if (!suppressed && suppressionEndTime != null) {
            // Suppression period has ended, clear it
            clearSuppression();
        }
        
        return suppressed;
    }
    
    /**
     * Suppress all alerts for a specified duration
     */
    public void suppressAlerts(int hours, String reason, String suppressedBy) {
        this.suppressionEndTime = LocalDateTime.now().plusHours(hours);
        this.suppressionReason = reason;
        this.suppressedBy = suppressedBy;
        
        logger.info("Alerts suppressed until {} by {} - Reason: {}", 
            suppressionEndTime, suppressedBy, reason);
    }
    
    /**
     * Suppress alerts for default duration
     */
    public void suppressAlerts(String reason, String suppressedBy) {
        suppressAlerts(defaultSuppressionDurationHours, reason, suppressedBy);
    }
    
    /**
     * Clear alert suppression
     */
    public void clearSuppression() {
        this.suppressionEndTime = null;
        this.suppressionReason = null;
        this.suppressedBy = null;
        
        logger.info("Alert suppression cleared");
    }
    
    /**
     * Check if a specific message type is enabled for alerts
     */
    public boolean isMessageTypeEnabled(String messageType) {
        return enabledMessageTypes.contains(messageType.toUpperCase());
    }
    
    /**
     * Enable alerts for a message type
     */
    public void enableMessageType(String messageType) {
        enabledMessageTypes.add(messageType.toUpperCase());
        logger.info("Alerts enabled for message type: {}", messageType);
    }
    
    /**
     * Disable alerts for a message type
     */
    public void disableMessageType(String messageType) {
        enabledMessageTypes.remove(messageType.toUpperCase());
        logger.info("Alerts disabled for message type: {}", messageType);
    }
    
    /**
     * Get all enabled message types
     */
    public Set<String> getEnabledMessageTypes() {
        return new HashSet<>(enabledMessageTypes);
    }
    
    /**
     * Set enabled message types
     */
    public void setEnabledMessageTypes(Set<String> messageTypes) {
        enabledMessageTypes.clear();
        enabledMessageTypes.addAll(messageTypes);
        logger.info("Enabled message types updated: {}", enabledMessageTypes);
    }
    
    /**
     * Check if a specific alert type (messageType + severity) is suppressed
     */
    public boolean isAlertTypeSuppressed(String messageType, AlertSeverity severity) {
        String alertType = messageType.toUpperCase() + "_" + severity.name();
        return suppressedAlertTypes.contains(alertType);
    }
    
    /**
     * Suppress a specific alert type
     */
    public void suppressAlertType(String messageType, AlertSeverity severity) {
        String alertType = messageType.toUpperCase() + "_" + severity.name();
        suppressedAlertTypes.add(alertType);
        logger.info("Alert type suppressed: {}", alertType);
    }
    
    /**
     * Unsuppress a specific alert type
     */
    public void unsuppressAlertType(String messageType, AlertSeverity severity) {
        String alertType = messageType.toUpperCase() + "_" + severity.name();
        suppressedAlertTypes.remove(alertType);
        logger.info("Alert type unsuppressed: {}", alertType);
    }
    
    /**
     * Get notification channels for a severity level
     */
    public List<String> getNotificationChannelsForSeverity(AlertSeverity severity) {
        switch (severity) {
            case CRITICAL:
                return Arrays.asList("email", "slack", "webhook");
            case WARNING:
                return Arrays.asList("email", "slack");
            case INFO:
            default:
                return Arrays.asList("email");
        }
    }
    
    /**
     * Record that an alert was triggered (for analytics)
     */
    public void recordAlertTriggered(String messageType, AlertSeverity severity) {
        String key = messageType.toUpperCase() + "_" + severity.name();
        
        // Update count
        alertCounts.merge(key, 1, Integer::sum);
        
        // Update last alert time
        lastAlertTimes.put(key, LocalDateTime.now());
        
        // Check rate limiting
        checkRateLimit(messageType, severity);
    }
    
    /**
     * Check if we're hitting rate limits
     */
    private void checkRateLimit(String messageType, AlertSeverity severity) {
        String key = messageType.toUpperCase() + "_" + severity.name();
        LocalDateTime lastAlert = lastAlertTimes.get(key);
        
        if (lastAlert != null) {
            long alertsInLastHour = alertCounts.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(messageType.toUpperCase()))
                .mapToInt(Map.Entry::getValue)
                .sum();
            
            if (alertsInLastHour > maxAlertsPerHour) {
                logger.warn("Rate limit exceeded for {}: {} alerts in the last hour", 
                    messageType, alertsInLastHour);
                // Could implement additional rate limiting logic here
            }
        }
    }
    
    /**
     * Get alert statistics for analytics
     */
    public Map<String, Object> getAlertStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Basic statistics
        stats.put("totalAlerts", alertCounts.values().stream().mapToInt(Integer::intValue).sum());
        stats.put("enabledMessageTypes", new ArrayList<>(enabledMessageTypes));
        stats.put("suppressedAlertTypes", new ArrayList<>(suppressedAlertTypes));
        stats.put("isSuppressed", isAlertSuppressed());
        
        if (isAlertSuppressed()) {
            stats.put("suppressionEndTime", suppressionEndTime);
            stats.put("suppressionReason", suppressionReason);
            stats.put("suppressedBy", suppressedBy);
        }
        
        // Alert counts by type
        Map<String, Integer> alertCountsByType = new HashMap<>();
        for (Map.Entry<String, Integer> entry : alertCounts.entrySet()) {
            alertCountsByType.put(entry.getKey(), entry.getValue());
        }
        stats.put("alertCountsByType", alertCountsByType);
        
        // Last alert times
        Map<String, String> lastAlertTimesFormatted = new HashMap<>();
        for (Map.Entry<String, LocalDateTime> entry : lastAlertTimes.entrySet()) {
            lastAlertTimesFormatted.put(entry.getKey(), entry.getValue().toString());
        }
        stats.put("lastAlertTimes", lastAlertTimesFormatted);
        
        return stats;
    }
    
    /**
     * Get current configuration
     */
    public Map<String, Object> getCurrentConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("enabledMessageTypes", new ArrayList<>(enabledMessageTypes));
        config.put("suppressedAlertTypes", new ArrayList<>(suppressedAlertTypes));
        config.put("isSuppressed", isAlertSuppressed());
        config.put("suppressionEndTime", suppressionEndTime);
        config.put("suppressionReason", suppressionReason);
        config.put("suppressedBy", suppressedBy);
        config.put("maxAlertsPerHour", maxAlertsPerHour);
        config.put("analyticsRetentionDays", analyticsRetentionDays);
        
        return config;
    }
    
    /**
     * Reset analytics data
     */
    public void resetAnalytics() {
        alertCounts.clear();
        lastAlertTimes.clear();
        logger.info("Alert analytics data reset");
    }
    
    /**
     * Clean up old analytics data
     */
    public void cleanupOldAnalytics() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(analyticsRetentionDays);
        
        lastAlertTimes.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        
        logger.info("Cleaned up analytics data older than {} days", analyticsRetentionDays);
    }
}
