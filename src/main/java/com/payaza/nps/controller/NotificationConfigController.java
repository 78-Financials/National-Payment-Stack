package com.payaza.nps.controller;

import com.payaza.nps.service.NotificationConfigurationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin Controller for managing notification configuration
 */
@RestController
@RequestMapping("/api/v1/admin/notifications")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class NotificationConfigController {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConfigController.class);

    @Autowired
    private NotificationConfigurationService configService;

    /**
     * Get current notification configuration
     */
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> getNotificationConfiguration() {
        try {
            Map<String, Object> config = configService.getAllConfiguration();
            return ResponseEntity.ok(config);
        } catch (Exception e) {
            logger.error("Error retrieving notification configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update email configuration
     */
    @PutMapping("/config/email")
    public ResponseEntity<String> updateEmailConfiguration(
            @RequestParam(required = false) String apiUrl,
            @RequestParam(required = false) String apiKey,
            @RequestParam(required = false) String sender,
            @RequestParam(required = false) String recipients) {
        try {
            configService.updateEmailConfiguration(apiUrl, apiKey, sender, recipients);
            return ResponseEntity.ok("Email configuration updated successfully");
        } catch (Exception e) {
            logger.error("Error updating email configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating email configuration: " + e.getMessage());
        }
    }

    /**
     * Update Slack configuration
     */
    @PutMapping("/config/slack")
    public ResponseEntity<String> updateSlackConfiguration(
            @RequestParam(required = false) String webhookUrl) {
        try {
            configService.updateSlackConfiguration(webhookUrl);
            return ResponseEntity.ok("Slack configuration updated successfully");
        } catch (Exception e) {
            logger.error("Error updating Slack configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating Slack configuration: " + e.getMessage());
        }
    }

    /**
     * Update webhook configuration
     */
    @PutMapping("/config/webhook")
    public ResponseEntity<String> updateWebhookConfiguration(
            @RequestParam(required = false) String url,
            @RequestParam(required = false) String apiKey) {
        try {
            configService.updateWebhookConfiguration(url, apiKey);
            return ResponseEntity.ok("Webhook configuration updated successfully");
        } catch (Exception e) {
            logger.error("Error updating webhook configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating webhook configuration: " + e.getMessage());
        }
    }

    /**
     * Update retry configuration
     */
    @PutMapping("/config/retry")
    public ResponseEntity<String> updateRetryConfiguration(
            @RequestParam(required = false) Integer maxAttempts,
            @RequestParam(required = false) Integer intervalSeconds) {
        try {
            if (maxAttempts == null) maxAttempts = 3;
            if (intervalSeconds == null) intervalSeconds = 300;
            
            configService.updateRetryConfiguration(maxAttempts, intervalSeconds);
            return ResponseEntity.ok("Retry configuration updated successfully");
        } catch (Exception e) {
            logger.error("Error updating retry configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error updating retry configuration: " + e.getMessage());
        }
    }

    /**
     * Reset configuration to defaults
     */
    @PostMapping("/config/reset")
    public ResponseEntity<String> resetConfiguration() {
        try {
            configService.resetToDefaults();
            return ResponseEntity.ok("Configuration reset to defaults successfully");
        } catch (Exception e) {
            logger.error("Error resetting configuration: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error resetting configuration: " + e.getMessage());
        }
    }
}
