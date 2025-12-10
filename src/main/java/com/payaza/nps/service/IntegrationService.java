package com.payaza.nps.service;

import com.payaza.nps.dto.WebhookConfigDto;
import com.payaza.nps.dto.IntegrationConfigDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Integration Service for webhook and third-party integration management
 */
@Service
public class IntegrationService {
    
    private static final Logger logger = LoggerFactory.getLogger(IntegrationService.class);
    
    @Autowired
    private WebClient webClient;
    
    @Autowired
    private WebhookDeliveryService webhookDeliveryService;
    
    @Autowired
    private RateLimitService rateLimitService;
    
    // In-memory storage for demo purposes (in production, use database)
    private final Map<String, WebhookConfigDto> webhookConfigs = new HashMap<>();
    private final Map<String, IntegrationConfigDto> thirdPartyIntegrations = new HashMap<>();
    private final Map<String, Map<String, Object>> rateLimitConfigs = new HashMap<>();
    
    /**
     * Get all webhook configurations
     */
    public List<WebhookConfigDto> getAllWebhookConfigurations() {
        return new ArrayList<>(webhookConfigs.values());
    }
    
    /**
     * Create new webhook configuration
     */
    public WebhookConfigDto createWebhookConfiguration(WebhookConfigDto webhook) {
        String webhookId = UUID.randomUUID().toString();
        webhook.setId(webhookId);
        webhook.setCreatedAt(LocalDateTime.now());
        webhook.setUpdatedAt(LocalDateTime.now());
        
        webhookConfigs.put(webhookId, webhook);
        
        logger.info("Webhook configuration created: {}", webhookId);
        return webhook;
    }
    
    /**
     * Update webhook configuration
     */
    public WebhookConfigDto updateWebhookConfiguration(String webhookId, WebhookConfigDto webhook) {
        WebhookConfigDto existingWebhook = webhookConfigs.get(webhookId);
        if (existingWebhook == null) {
            throw new RuntimeException("Webhook configuration not found: " + webhookId);
        }
        
        webhook.setId(webhookId);
        webhook.setCreatedAt(existingWebhook.getCreatedAt());
        webhook.setUpdatedAt(LocalDateTime.now());
        
        webhookConfigs.put(webhookId, webhook);
        
        logger.info("Webhook configuration updated: {}", webhookId);
        return webhook;
    }
    
    /**
     * Delete webhook configuration
     */
    public void deleteWebhookConfiguration(String webhookId) {
        WebhookConfigDto webhook = webhookConfigs.remove(webhookId);
        if (webhook == null) {
            throw new RuntimeException("Webhook configuration not found: " + webhookId);
        }
        
        logger.info("Webhook configuration deleted: {}", webhookId);
    }
    
    /**
     * Test webhook configuration
     */
    public Map<String, Object> testWebhookConfiguration(String webhookId) {
        WebhookConfigDto webhook = webhookConfigs.get(webhookId);
        if (webhook == null) {
            throw new RuntimeException("Webhook configuration not found: " + webhookId);
        }
        
        Map<String, Object> testPayload = createTestPayload();
        
        try {
            // Send test webhook
            String response = webClient.post()
                .uri(webhook.getUrl())
                .header("Content-Type", "application/json")
                .header("X-API-Key", webhook.getApiKey() != null ? webhook.getApiKey() : "")
                .bodyValue(testPayload)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(java.time.Duration.ofSeconds(webhook.getTimeoutSeconds()))
                .block();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("response", response);
            result.put("responseTime", System.currentTimeMillis());
            result.put("timestamp", LocalDateTime.now());
            
            logger.info("Webhook test successful: {}", webhookId);
            return result;
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            
            logger.error("Webhook test failed: {}", webhookId, e);
            return result;
        }
    }
    
    /**
     * Get webhook delivery history
     */
    public List<Map<String, Object>> getWebhookDeliveries(String webhookId, int page, int size) {
        return webhookDeliveryService.getWebhookDeliveries(webhookId, page, size);
    }
    
    /**
     * Retry failed webhook delivery
     */
    public void retryWebhookDelivery(String webhookId, String deliveryId) {
        webhookDeliveryService.retryWebhookDelivery(webhookId, deliveryId);
        logger.info("Webhook delivery retry initiated: {} - {}", webhookId, deliveryId);
    }
    
    /**
     * Get API rate limiting configuration
     */
    public List<Map<String, Object>> getRateLimitConfigurations() {
        List<Map<String, Object>> configs = new ArrayList<>();
        
        for (Map.Entry<String, Map<String, Object>> entry : rateLimitConfigs.entrySet()) {
            Map<String, Object> config = new HashMap<>(entry.getValue());
            config.put("clientId", entry.getKey());
            configs.add(config);
        }
        
        return configs;
    }
    
    /**
     * Update API rate limiting configuration
     */
    public void updateRateLimitConfiguration(String clientId, Map<String, Object> rateLimitConfig) {
        rateLimitConfigs.put(clientId, rateLimitConfig);
        rateLimitService.updateClientRateLimit(clientId, rateLimitConfig);
        
        logger.info("Rate limit configuration updated for client: {}", clientId);
    }
    
    /**
     * Get current API usage for client
     */
    public Map<String, Object> getClientApiUsage(String clientId) {
        return rateLimitService.getClientUsage(clientId);
    }
    
    /**
     * Get third-party integrations
     */
    public List<IntegrationConfigDto> getThirdPartyIntegrations() {
        return new ArrayList<>(thirdPartyIntegrations.values());
    }
    
    /**
     * Create third-party integration
     */
    public IntegrationConfigDto createThirdPartyIntegration(IntegrationConfigDto integration) {
        String integrationId = UUID.randomUUID().toString();
        integration.setId(integrationId);
        integration.setCreatedAt(LocalDateTime.now());
        integration.setUpdatedAt(LocalDateTime.now());
        
        thirdPartyIntegrations.put(integrationId, integration);
        
        logger.info("Third-party integration created: {}", integrationId);
        return integration;
    }
    
    /**
     * Test third-party integration
     */
    public Map<String, Object> testThirdPartyIntegration(String integrationId) {
        IntegrationConfigDto integration = thirdPartyIntegrations.get(integrationId);
        if (integration == null) {
            throw new RuntimeException("Third-party integration not found: " + integrationId);
        }
        
        Map<String, Object> testPayload = createIntegrationTestPayload(integration);
        
        try {
            // Test integration based on type
            String response = testIntegrationConnection(integration, testPayload);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("response", response);
            result.put("responseTime", System.currentTimeMillis());
            result.put("timestamp", LocalDateTime.now());
            
            logger.info("Third-party integration test successful: {}", integrationId);
            return result;
            
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            
            logger.error("Third-party integration test failed: {}", integrationId, e);
            return result;
        }
    }
    
    /**
     * Create test payload for webhook
     */
    private Map<String, Object> createTestPayload() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("event", "test");
        payload.put("timestamp", LocalDateTime.now().toString());
        payload.put("data", Map.of(
            "message", "This is a test webhook",
            "source", "NPS System"
        ));
        return payload;
    }
    
    /**
     * Create test payload for integration
     */
    private Map<String, Object> createIntegrationTestPayload(IntegrationConfigDto integration) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("test", true);
        payload.put("integrationType", integration.getType());
        payload.put("timestamp", LocalDateTime.now().toString());
        return payload;
    }
    
    /**
     * Test integration connection
     */
    private String testIntegrationConnection(IntegrationConfigDto integration, Map<String, Object> payload) {
        return webClient.post()
            .uri(integration.getEndpoint())
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + integration.getApiKey())
            .bodyValue(payload)
            .retrieve()
            .bodyToMono(String.class)
            .timeout(java.time.Duration.ofSeconds(30))
            .block();
    }
}
