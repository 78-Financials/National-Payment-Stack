package com.payaza.nps.service;

import com.payaza.nps.model.Alert;
import com.payaza.nps.model.AlertSeverity;
import com.payaza.nps.model.AlertStatus;
import com.payaza.nps.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Shared Alert Service for all message processing services
 * Provides centralized alert triggering with configurable suppression and filtering
 */
@Service
public class SharedAlertService {
    
    private static final Logger logger = LoggerFactory.getLogger(SharedAlertService.class);
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private AlertConfigurationService alertConfigService;
    
    /**
     * Trigger alert for message processing failure
     */
    @Transactional
    public void triggerMessageProcessingAlert(String messageType, String transactionId, 
                                            String errorMessage, AlertSeverity severity) {
        try {
            // Check if alerts are suppressed
            if (alertConfigService.isAlertSuppressed()) {
                logger.info("Alerts are currently suppressed, skipping alert for {} - Transaction: {}", 
                    messageType, transactionId);
                return;
            }
            
            // Check if this message type is enabled for alerts
            if (!alertConfigService.isMessageTypeEnabled(messageType)) {
                logger.info("Alerts disabled for message type {}, skipping alert - Transaction: {}", 
                    messageType, transactionId);
                return;
            }
            
            // Check if we should suppress this specific alert type
            if (alertConfigService.isAlertTypeSuppressed(messageType, severity)) {
                logger.info("Alert type {} with severity {} is suppressed, skipping - Transaction: {}", 
                    messageType, severity, transactionId);
                return;
            }
            
            // Create and save alert
            Alert alert = createAlert(messageType, transactionId, errorMessage, severity);
            Alert savedAlert = alertRepository.save(alert);
            
            // Send notifications
            notificationService.sendAlert(savedAlert);
            
            // Record alert analytics
            alertConfigService.recordAlertTriggered(messageType, severity);
            
            logger.info("Alert triggered successfully: {} - {} - Transaction: {} - Alert ID: {}", 
                messageType, severity, transactionId, savedAlert.getId());
            
        } catch (Exception e) {
            logger.error("Error triggering alert for {} - Transaction {}: {}", 
                messageType, transactionId, e.getMessage(), e);
        }
    }
    
    /**
     * Trigger critical alert for message processing failure
     */
    public void triggerCriticalAlert(String messageType, String transactionId, String errorMessage) {
        triggerMessageProcessingAlert(messageType, transactionId, errorMessage, AlertSeverity.CRITICAL);
    }
    
    /**
     * Trigger warning alert for message processing failure
     */
    public void triggerWarningAlert(String messageType, String transactionId, String errorMessage) {
        triggerMessageProcessingAlert(messageType, transactionId, errorMessage, AlertSeverity.WARNING);
    }
    
    /**
     * Trigger info alert for message processing failure
     */
    public void triggerInfoAlert(String messageType, String transactionId, String errorMessage) {
        triggerMessageProcessingAlert(messageType, transactionId, errorMessage, AlertSeverity.INFO);
    }
    
    /**
     * Create alert object
     */
    private Alert createAlert(String messageType, String transactionId, String errorMessage, AlertSeverity severity) {
        Alert alert = new Alert();
        
        // Set basic alert information
        alert.setName(String.format("%s Processing Failure", messageType.toUpperCase()));
        alert.setMessage(String.format("Error processing %s message for transaction %s: %s", 
            messageType, transactionId != null ? transactionId : "UNKNOWN", errorMessage));
        alert.setSeverity(severity);
        alert.setStatus(AlertStatus.ACTIVE);
        alert.setMetricType("ERROR");
        alert.setMetricName(String.format("%s_processing_failure", messageType.toLowerCase()));
        
        // Set context with detailed information
        String context = String.format(
            "{\"messageType\":\"%s\",\"transactionId\":\"%s\",\"errorType\":\"%s_PROCESSING_ERROR\",\"timestamp\":\"%s\",\"severity\":\"%s\"}", 
            messageType, 
            transactionId != null ? transactionId : "UNKNOWN", 
            messageType.toUpperCase(),
            LocalDateTime.now(),
            severity.name()
        );
        alert.setContext(context);
        
        // Set notification channels based on severity and configuration
        List<String> channels = alertConfigService.getNotificationChannelsForSeverity(severity);
        alert.setNotificationChannels(channels);
        
        return alert;
    }
    
    /**
     * Get alert statistics for analytics
     */
    public Map<String, Object> getAlertStatistics() {
        return alertConfigService.getAlertStatistics();
    }
    
    /**
     * Get alerts by message type
     */
    public List<Alert> getAlertsByMessageType(String messageType, int limit) {
        return alertRepository.findByMetricNameContainingOrderByCreatedAtDesc(
            messageType.toLowerCase() + "_processing_failure", 
            PageRequest.of(0, limit)
        ).getContent();
    }
    
    /**
     * Get recent alerts
     */
    public List<Alert> getRecentAlerts(int limit) {
        return alertRepository.findByOrderByCreatedAtDesc(
            PageRequest.of(0, limit)
        ).getContent();
    }
}
