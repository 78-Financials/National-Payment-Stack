package com.payaza.nps.controller;

import com.payaza.nps.dto.WebhookConfigDto;
import com.payaza.nps.dto.IntegrationConfigDto;
import com.payaza.nps.service.IntegrationService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Integration and Webhook Management Controller
 */
@RestController
@RequestMapping("/api/v1/admin/integrations")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class IntegrationController {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationController.class);
    
    @Autowired
    private IntegrationService integrationService;
    
    /**
     * Get all webhook configurations
     */
    @GetMapping("/webhooks")
    public ResponseEntity<List<WebhookConfigDto>> getWebhookConfigurations() {
        try {
            List<WebhookConfigDto> webhooks = integrationService.getAllWebhookConfigurations();
            return ResponseEntity.ok(webhooks);
        } catch (Exception e) {
            logger.error("Failed to get webhook configurations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Create new webhook configuration
     */
    @PostMapping("/webhooks")
    public ResponseEntity<WebhookConfigDto> createWebhookConfiguration(@Valid @RequestBody WebhookConfigDto webhook) {
        try {
            WebhookConfigDto createdWebhook = integrationService.createWebhookConfiguration(webhook);
            logger.info("Webhook configuration created: {}", createdWebhook.getId());
            return ResponseEntity.ok(createdWebhook);
        } catch (Exception e) {
            logger.error("Failed to create webhook configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update webhook configuration
     */
    @PutMapping("/webhooks/{webhookId}")
    public ResponseEntity<WebhookConfigDto> updateWebhookConfiguration(
            @PathVariable String webhookId,
            @Valid @RequestBody WebhookConfigDto webhook) {
        try {
            WebhookConfigDto updatedWebhook = integrationService.updateWebhookConfiguration(webhookId, webhook);
            logger.info("Webhook configuration updated: {}", webhookId);
            return ResponseEntity.ok(updatedWebhook);
        } catch (Exception e) {
            logger.error("Failed to update webhook configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete webhook configuration
     */
    @DeleteMapping("/webhooks/{webhookId}")
    public ResponseEntity<Map<String, String>> deleteWebhookConfiguration(@PathVariable String webhookId) {
        try {
            integrationService.deleteWebhookConfiguration(webhookId);
            logger.info("Webhook configuration deleted: {}", webhookId);
            return ResponseEntity.ok(Map.of("message", "Webhook configuration deleted successfully"));
        } catch (Exception e) {
            logger.error("Failed to delete webhook configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Test webhook configuration
     */
    @PostMapping("/webhooks/{webhookId}/test")
    public ResponseEntity<Map<String, Object>> testWebhookConfiguration(@PathVariable String webhookId) {
        try {
            Map<String, Object> testResult = integrationService.testWebhookConfiguration(webhookId);
            logger.info("Webhook test completed for: {}", webhookId);
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            logger.error("Failed to test webhook configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get webhook delivery history
     */
    @GetMapping("/webhooks/{webhookId}/deliveries")
    public ResponseEntity<List<Map<String, Object>>> getWebhookDeliveries(
            @PathVariable String webhookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            List<Map<String, Object>> deliveries = integrationService.getWebhookDeliveries(webhookId, page, size);
            return ResponseEntity.ok(deliveries);
        } catch (Exception e) {
            logger.error("Failed to get webhook deliveries: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Retry failed webhook delivery
     */
    @PostMapping("/webhooks/{webhookId}/deliveries/{deliveryId}/retry")
    public ResponseEntity<Map<String, String>> retryWebhookDelivery(
            @PathVariable String webhookId,
            @PathVariable String deliveryId) {
        try {
            integrationService.retryWebhookDelivery(webhookId, deliveryId);
            logger.info("Webhook delivery retry initiated: {}", deliveryId);
            return ResponseEntity.ok(Map.of("message", "Webhook delivery retry initiated"));
        } catch (Exception e) {
            logger.error("Failed to retry webhook delivery: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get API rate limiting configuration
     */
    @GetMapping("/rate-limits")
    public ResponseEntity<List<Map<String, Object>>> getRateLimitConfigurations() {
        try {
            List<Map<String, Object>> rateLimits = integrationService.getRateLimitConfigurations();
            return ResponseEntity.ok(rateLimits);
        } catch (Exception e) {
            logger.error("Failed to get rate limit configurations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update API rate limiting configuration
     */
    @PutMapping("/rate-limits/{clientId}")
    public ResponseEntity<Map<String, String>> updateRateLimitConfiguration(
            @PathVariable String clientId,
            @RequestBody Map<String, Object> rateLimitConfig) {
        try {
            integrationService.updateRateLimitConfiguration(clientId, rateLimitConfig);
            logger.info("Rate limit configuration updated for client: {}", clientId);
            return ResponseEntity.ok(Map.of("message", "Rate limit configuration updated successfully"));
        } catch (Exception e) {
            logger.error("Failed to update rate limit configuration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get current API usage for client
     */
    @GetMapping("/rate-limits/{clientId}/usage")
    public ResponseEntity<Map<String, Object>> getClientApiUsage(@PathVariable String clientId) {
        try {
            Map<String, Object> usage = integrationService.getClientApiUsage(clientId);
            return ResponseEntity.ok(usage);
        } catch (Exception e) {
            logger.error("Failed to get client API usage: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get third-party integrations
     */
    @GetMapping("/third-party")
    public ResponseEntity<List<IntegrationConfigDto>> getThirdPartyIntegrations() {
        try {
            List<IntegrationConfigDto> integrations = integrationService.getThirdPartyIntegrations();
            return ResponseEntity.ok(integrations);
        } catch (Exception e) {
            logger.error("Failed to get third-party integrations: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Create third-party integration
     */
    @PostMapping("/third-party")
    public ResponseEntity<IntegrationConfigDto> createThirdPartyIntegration(
            @Valid @RequestBody IntegrationConfigDto integration) {
        try {
            IntegrationConfigDto createdIntegration = integrationService.createThirdPartyIntegration(integration);
            logger.info("Third-party integration created: {}", createdIntegration.getId());
            return ResponseEntity.ok(createdIntegration);
        } catch (Exception e) {
            logger.error("Failed to create third-party integration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Test third-party integration
     */
    @PostMapping("/third-party/{integrationId}/test")
    public ResponseEntity<Map<String, Object>> testThirdPartyIntegration(@PathVariable String integrationId) {
        try {
            Map<String, Object> testResult = integrationService.testThirdPartyIntegration(integrationId);
            logger.info("Third-party integration test completed: {}", integrationId);
            return ResponseEntity.ok(testResult);
        } catch (Exception e) {
            logger.error("Failed to test third-party integration: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }
}
