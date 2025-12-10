package com.payaza.nps.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for managing notification configuration settings
 * Allows runtime configuration updates for notification channels
 */
@Service
public class NotificationConfigurationService {
    
    private static final Logger logger = LoggerFactory.getLogger(NotificationConfigurationService.class);
    
    // Email Configuration
    @Value("${nps.notifications.email.api-url:http://localhost:8090/live/send-email-without-template}")
    private String emailApiUrl;
    
    @Value("${nps.notifications.email.api-key:yRWTlNCmDoacSTIJS3BcLM6kAQ9jL5E9TOqXdjld}")
    private String emailApiKey;
    
    @Value("${nps.notifications.email.sender:nps@payaza.africa}")
    private String emailSender;
    
    @Value("${nps.notifications.email.recipients:admin@payaza.africa}")
    private String emailRecipients;
    
    // Slack Configuration
    @Value("${nps.notifications.slack.webhook-url:}")
    private String slackWebhookUrl;
    
    // Webhook Configuration
    @Value("${nps.notifications.webhook.url:}")
    private String webhookUrl;
    
    @Value("${nps.notifications.webhook.api-key:}")
    private String webhookApiKey;
    
    // Retry Configuration
    @Value("${nps.notifications.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${nps.notifications.retry.interval-seconds:300}")
    private int retryIntervalSeconds;
    
    // Runtime configuration overrides
    private final Map<String, Object> runtimeOverrides = new HashMap<>();
    
    /**
     * Get current email API URL
     */
    public String getEmailApiUrl() {
        return (String) runtimeOverrides.getOrDefault("email.api-url", emailApiUrl);
    }
    
    /**
     * Get current email API key
     */
    public String getEmailApiKey() {
        return (String) runtimeOverrides.getOrDefault("email.api-key", emailApiKey);
    }
    
    /**
     * Get current email sender
     */
    public String getEmailSender() {
        return (String) runtimeOverrides.getOrDefault("email.sender", emailSender);
    }
    
    /**
     * Get current email recipients
     */
    public String getEmailRecipients() {
        return (String) runtimeOverrides.getOrDefault("email.recipients", emailRecipients);
    }
    
    /**
     * Get current Slack webhook URL
     */
    public String getSlackWebhookUrl() {
        return (String) runtimeOverrides.getOrDefault("slack.webhook-url", slackWebhookUrl);
    }
    
    /**
     * Get current webhook URL
     */
    public String getWebhookUrl() {
        return (String) runtimeOverrides.getOrDefault("webhook.url", webhookUrl);
    }
    
    /**
     * Get current webhook API key
     */
    public String getWebhookApiKey() {
        return (String) runtimeOverrides.getOrDefault("webhook.api-key", webhookApiKey);
    }
    
    /**
     * Get current max retry attempts
     */
    public int getMaxRetryAttempts() {
        return (Integer) runtimeOverrides.getOrDefault("retry.max-attempts", maxRetryAttempts);
    }
    
    /**
     * Get current retry interval in seconds
     */
    public int getRetryIntervalSeconds() {
        return (Integer) runtimeOverrides.getOrDefault("retry.interval-seconds", retryIntervalSeconds);
    }
    
    /**
     * Update email configuration at runtime
     */
    public void updateEmailConfiguration(String apiUrl, String apiKey, String sender, String recipients) {
        logger.info("Updating email configuration - URL: {}, Sender: {}, Recipients: {}", apiUrl, sender, recipients);
        
        if (apiUrl != null && !apiUrl.trim().isEmpty()) {
            runtimeOverrides.put("email.api-url", apiUrl);
        }
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            runtimeOverrides.put("email.api-key", apiKey);
        }
        if (sender != null && !sender.trim().isEmpty()) {
            runtimeOverrides.put("email.sender", sender);
        }
        if (recipients != null && !recipients.trim().isEmpty()) {
            runtimeOverrides.put("email.recipients", recipients);
        }
        
        logger.info("Email configuration updated successfully");
    }
    
    /**
     * Update Slack configuration at runtime
     */
    public void updateSlackConfiguration(String webhookUrl) {
        logger.info("Updating Slack configuration - Webhook URL: {}", webhookUrl);
        
        if (webhookUrl != null && !webhookUrl.trim().isEmpty()) {
            runtimeOverrides.put("slack.webhook-url", webhookUrl);
        } else {
            runtimeOverrides.remove("slack.webhook-url");
        }
        
        logger.info("Slack configuration updated successfully");
    }
    
    /**
     * Update webhook configuration at runtime
     */
    public void updateWebhookConfiguration(String url, String apiKey) {
        logger.info("Updating webhook configuration - URL: {}", url);
        
        if (url != null && !url.trim().isEmpty()) {
            runtimeOverrides.put("webhook.url", url);
        } else {
            runtimeOverrides.remove("webhook.url");
        }
        
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            runtimeOverrides.put("webhook.api-key", apiKey);
        } else {
            runtimeOverrides.remove("webhook.api-key");
        }
        
        logger.info("Webhook configuration updated successfully");
    }
    
    /**
     * Update retry configuration at runtime
     */
    public void updateRetryConfiguration(int maxAttempts, int intervalSeconds) {
        logger.info("Updating retry configuration - Max Attempts: {}, Interval: {} seconds", maxAttempts, intervalSeconds);
        
        if (maxAttempts > 0) {
            runtimeOverrides.put("retry.max-attempts", maxAttempts);
        }
        if (intervalSeconds > 0) {
            runtimeOverrides.put("retry.interval-seconds", intervalSeconds);
        }
        
        logger.info("Retry configuration updated successfully");
    }
    
    /**
     * Get all current configuration settings
     */
    public Map<String, Object> getAllConfiguration() {
        Map<String, Object> config = new HashMap<>();
        
        config.put("email.api-url", getEmailApiUrl());
        config.put("email.api-key", getEmailApiKey());
        config.put("email.sender", getEmailSender());
        config.put("email.recipients", getEmailRecipients());
        config.put("slack.webhook-url", getSlackWebhookUrl());
        config.put("webhook.url", getWebhookUrl());
        config.put("webhook.api-key", getWebhookApiKey());
        config.put("retry.max-attempts", getMaxRetryAttempts());
        config.put("retry.interval-seconds", getRetryIntervalSeconds());
        
        return config;
    }
    
    /**
     * Reset all runtime overrides to default values
     */
    public void resetToDefaults() {
        logger.info("Resetting notification configuration to defaults");
        runtimeOverrides.clear();
        logger.info("Configuration reset to defaults completed");
    }
}
