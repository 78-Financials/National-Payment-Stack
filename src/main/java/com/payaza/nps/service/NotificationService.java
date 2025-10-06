package com.payaza.nps.service;

import com.payaza.nps.model.Alert;
import com.payaza.nps.model.AlertSeverity;
import com.payaza.nps.model.AlertAction;
import com.payaza.nps.repository.AlertRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for sending notifications through various channels
 */
@Service
public class NotificationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private AlertRepository alertRepository;
    
    @Autowired
    private AlertHistoryService alertHistoryService;
    
    private final DateTimeFormatter timestampFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Send alert notifications through configured channels
     */
    @Transactional
    public void sendAlert(Alert alert) {
        try {
            List<String> channels = alert.getNotificationChannels();
            if (channels == null || channels.isEmpty()) {
                logger.warn("No notification channels configured for alert: {}", alert.getName());
                return;
            }
            
            boolean allChannelsSuccessful = true;
            
            for (String channel : channels) {
                try {
                    boolean success = sendNotification(alert, channel);
                    if (!success) {
                        allChannelsSuccessful = false;
                    }
                } catch (Exception e) {
                    logger.error("Error sending notification via {} for alert {}: {}", 
                               channel, alert.getName(), e.getMessage(), e);
                    allChannelsSuccessful = false;
                }
            }
            
            // Update notification count and timestamp
            alert.incrementNotificationCount();
            alertRepository.save(alert);
            
            // Record notification in history
            AlertAction action = allChannelsSuccessful ? 
                AlertAction.NOTIFICATION_SENT : 
                AlertAction.NOTIFICATION_FAILED;
            
            alertHistoryService.recordAlertAction(alert.getId(), action, "SYSTEM", 
                "Notification sent via: " + String.join(", ", channels));
            
            logger.info("Alert notification sent: {} via channels: {}", 
                       alert.getName(), String.join(", ", channels));
            
        } catch (Exception e) {
            logger.error("Error sending alert notification: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Send notification via specific channel
     */
    private boolean sendNotification(Alert alert, String channel) {
        switch (channel.toLowerCase()) {
            case "email":
                return sendEmailNotification(alert);
            case "sms":
                return sendSmsNotification(alert);
            case "slack":
                return sendSlackNotification(alert);
            case "webhook":
                return sendWebhookNotification(alert);
            default:
                logger.warn("Unknown notification channel: {}", channel);
                return false;
        }
    }
    
    /**
     * Send email notification
     */
    private boolean sendEmailNotification(Alert alert) {
        try {
            // TODO: Implement actual email sending (using JavaMailSender or similar)
            String subject = generateEmailSubject(alert);
            String body = generateEmailBody(alert);
            
            logger.info("EMAIL NOTIFICATION: {} - {}", subject, body);
            
            // For now, just log the email - in production, this would send actual email
            // emailSender.sendEmail(recipients, subject, body);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error sending email notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send SMS notification
     */
    private boolean sendSmsNotification(Alert alert) {
        try {
            // TODO: Implement actual SMS sending (using Twilio, AWS SNS, or similar)
            String message = generateSmsMessage(alert);
            
            logger.info("SMS NOTIFICATION: {}", message);
            
            // For now, just log the SMS - in production, this would send actual SMS
            // smsService.sendSms(phoneNumbers, message);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error sending SMS notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send Slack notification
     */
    private boolean sendSlackNotification(Alert alert) {
        try {
            // TODO: Implement actual Slack webhook sending
            Map<String, Object> slackMessage = generateSlackMessage(alert);
            
            logger.info("SLACK NOTIFICATION: {}", slackMessage);
            
            // For now, just log the Slack message - in production, this would send to Slack
            // slackService.sendMessage(slackMessage);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error sending Slack notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Send webhook notification
     */
    private boolean sendWebhookNotification(Alert alert) {
        try {
            // TODO: Implement actual webhook sending
            Map<String, Object> webhookPayload = generateWebhookPayload(alert);
            
            logger.info("WEBHOOK NOTIFICATION: {}", webhookPayload);
            
            // For now, just log the webhook payload - in production, this would send HTTP POST
            // webhookService.sendWebhook(webhookUrl, webhookPayload);
            
            return true;
            
        } catch (Exception e) {
            logger.error("Error sending webhook notification: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Generate email subject
     */
    private String generateEmailSubject(Alert alert) {
        return String.format("[%s] %s - %s", 
                           alert.getSeverity().getDisplayName(),
                           "NPS Alert",
                           alert.getName());
    }
    
    /**
     * Generate email body
     */
    private String generateEmailBody(Alert alert) {
        StringBuilder body = new StringBuilder();
        
        body.append("Alert Details:\n");
        body.append("==============\n\n");
        body.append("Name: ").append(alert.getName()).append("\n");
        body.append("Severity: ").append(alert.getSeverity().getDisplayName()).append("\n");
        body.append("Status: ").append(alert.getStatus()).append("\n");
        body.append("Timestamp: ").append(alert.getCreatedAt().format(timestampFormatter)).append("\n\n");
        
        if (alert.getMessage() != null) {
            body.append("Message: ").append(alert.getMessage()).append("\n\n");
        }
        
        if (alert.getMetricName() != null && alert.getMetricValue() != null) {
            body.append("Metric Information:\n");
            body.append("  Metric: ").append(alert.getMetricName()).append("\n");
            body.append("  Current Value: ").append(String.format("%.2f", alert.getMetricValue())).append("\n");
            if (alert.getThresholdValue() != null) {
                body.append("  Threshold: ").append(String.format("%.2f", alert.getThresholdValue())).append("\n");
            }
            body.append("\n");
        }
        
        body.append("Please investigate this issue promptly.\n\n");
        body.append("Best regards,\n");
        body.append("NPS Alerting System");
        
        return body.toString();
    }
    
    /**
     * Generate SMS message
     */
    private String generateSmsMessage(Alert alert) {
        String severity = alert.getSeverity().getDisplayName();
        String message = alert.getMessage();
        if (message != null && message.length() > 100) {
            message = message.substring(0, 97) + "...";
        }
        
        return String.format("[%s] %s: %s", severity, "NPS", message);
    }
    
    /**
     * Generate Slack message
     */
    private Map<String, Object> generateSlackMessage(Alert alert) {
        Map<String, Object> message = new HashMap<>();
        
        // Set color based on severity
        String color = alert.getSeverity().getColor();
        
        // Create attachment
        Map<String, Object> attachment = new HashMap<>();
        attachment.put("color", color);
        attachment.put("title", alert.getName());
        attachment.put("text", alert.getMessage());
        attachment.put("timestamp", System.currentTimeMillis() / 1000);
        
        // Add fields
        List<Map<String, Object>> fields = new java.util.ArrayList<>();
        
        Map<String, Object> severityField = new HashMap<>();
        severityField.put("title", "Severity");
        severityField.put("value", alert.getSeverity().getDisplayName());
        severityField.put("short", true);
        fields.add(severityField);
        
        Map<String, Object> statusField = new HashMap<>();
        statusField.put("title", "Status");
        statusField.put("value", alert.getStatus().getDisplayName());
        statusField.put("short", true);
        fields.add(statusField);
        
        if (alert.getMetricValue() != null) {
            Map<String, Object> metricField = new HashMap<>();
            metricField.put("title", "Metric Value");
            metricField.put("value", String.format("%.2f", alert.getMetricValue()));
            metricField.put("short", true);
            fields.add(metricField);
        }
        
        attachment.put("fields", fields);
        
        message.put("text", String.format("🚨 *%s Alert*", alert.getSeverity().getDisplayName()));
        message.put("attachments", List.of(attachment));
        
        return message;
    }
    
    /**
     * Generate webhook payload
     */
    private Map<String, Object> generateWebhookPayload(Alert alert) {
        Map<String, Object> payload = new HashMap<>();
        
        payload.put("alertId", alert.getId());
        payload.put("name", alert.getName());
        payload.put("message", alert.getMessage());
        payload.put("severity", alert.getSeverity().name());
        payload.put("status", alert.getStatus().name());
        payload.put("timestamp", alert.getCreatedAt().format(timestampFormatter));
        payload.put("metricName", alert.getMetricName());
        payload.put("metricValue", alert.getMetricValue());
        payload.put("thresholdValue", alert.getThresholdValue());
        payload.put("context", alert.getContext());
        
        return payload;
    }
    
    /**
     * Send recovery notification when alert is resolved
     */
    @Transactional
    public void sendRecoveryNotification(Alert alert) {
        try {
            // Modify alert for recovery notification
            Alert recoveryAlert = new Alert();
            recoveryAlert.setAlertRuleId(alert.getAlertRuleId());
            recoveryAlert.setName("RECOVERY: " + alert.getName());
            recoveryAlert.setMessage("Alert condition has been resolved: " + alert.getMessage());
            recoveryAlert.setSeverity(com.payaza.nps.model.AlertSeverity.INFO);
            recoveryAlert.setStatus(com.payaza.nps.model.AlertStatus.ACTIVE);
            recoveryAlert.setNotificationChannels(alert.getNotificationChannels());
            recoveryAlert.setMetricType(alert.getMetricType());
            recoveryAlert.setMetricName(alert.getMetricName());
            
            // Send recovery notification
            sendAlert(recoveryAlert);
            
            logger.info("Recovery notification sent for alert: {}", alert.getName());
            
        } catch (Exception e) {
            logger.error("Error sending recovery notification: {}", e.getMessage(), e);
        }
    }
}
