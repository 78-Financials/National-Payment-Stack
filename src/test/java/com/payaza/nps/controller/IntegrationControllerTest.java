package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.WebhookConfigDto;
import com.payaza.nps.dto.IntegrationConfigDto;
import com.payaza.nps.service.IntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for IntegrationController
 */
@WebMvcTest(IntegrationController.class)
class IntegrationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IntegrationService integrationService;

    @Autowired
    private ObjectMapper objectMapper;

    private WebhookConfigDto webhookConfig;
    private IntegrationConfigDto integrationConfig;

    @BeforeEach
    void setUp() {
        // Setup webhook config
        webhookConfig = new WebhookConfigDto();
        webhookConfig.setId("WEBHOOK001");
        webhookConfig.setName("Test Webhook");
        webhookConfig.setUrl("https://example.com/webhook");
        webhookConfig.setApiKey("webhook_api_key");
        webhookConfig.setEventTypes(Arrays.asList("PAYMENT_SUCCESS", "PAYMENT_FAILED"));
        webhookConfig.setActive(true);
        webhookConfig.setRetryAttempts(3);
        webhookConfig.setTimeoutSeconds(30);
        webhookConfig.setCreatedAt(LocalDateTime.now());

        // Setup integration config
        integrationConfig = new IntegrationConfigDto();
        integrationConfig.setId("INT001");
        integrationConfig.setName("Test Integration");
        integrationConfig.setType("SLACK");
        integrationConfig.setEndpoint("https://hooks.slack.com/services/xxx");
        integrationConfig.setApiKey("slack_api_key");
        integrationConfig.setActive(true);
        integrationConfig.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWebhookConfigurations_ShouldReturnWebhookList() throws Exception {
        // Given
        List<WebhookConfigDto> webhooks = Arrays.asList(webhookConfig);
        when(integrationService.getAllWebhookConfigurations()).thenReturn(webhooks);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/integrations/webhooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("WEBHOOK001"))
                .andExpect(jsonPath("$[0].name").value("Test Webhook"))
                .andExpect(jsonPath("$[0].url").value("https://example.com/webhook"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(integrationService).getAllWebhookConfigurations();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createWebhookConfiguration_WithValidData_ShouldCreateWebhook() throws Exception {
        // Given
        when(integrationService.createWebhookConfiguration(any(WebhookConfigDto.class))).thenReturn(webhookConfig);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/webhooks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("WEBHOOK001"))
                .andExpect(jsonPath("$.name").value("Test Webhook"))
                .andExpect(jsonPath("$.url").value("https://example.com/webhook"));

        verify(integrationService).createWebhookConfiguration(any(WebhookConfigDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateWebhookConfiguration_WithValidData_ShouldUpdateWebhook() throws Exception {
        // Given
        webhookConfig.setName("Updated Webhook Name");
        when(integrationService.updateWebhookConfiguration(anyString(), any(WebhookConfigDto.class))).thenReturn(webhookConfig);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/integrations/webhooks/WEBHOOK001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("WEBHOOK001"))
                .andExpect(jsonPath("$.name").value("Updated Webhook Name"));

        verify(integrationService).updateWebhookConfiguration(eq("WEBHOOK001"), any(WebhookConfigDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteWebhookConfiguration_WithValidId_ShouldDeleteWebhook() throws Exception {
        // Given
        doNothing().when(integrationService).deleteWebhookConfiguration(anyString());

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/integrations/webhooks/WEBHOOK001")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Webhook configuration deleted successfully"));

        verify(integrationService).deleteWebhookConfiguration("WEBHOOK001");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testWebhookConfiguration_WithValidId_ShouldReturnTestResult() throws Exception {
        // Given
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("success", true);
        testResult.put("response", "OK");
        testResult.put("responseTime", 150);
        testResult.put("timestamp", LocalDateTime.now());
        
        when(integrationService.testWebhookConfiguration(anyString())).thenReturn(testResult);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/webhooks/WEBHOOK001/test")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("OK"))
                .andExpect(jsonPath("$.responseTime").value(150));

        verify(integrationService).testWebhookConfiguration("WEBHOOK001");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getWebhookDeliveries_ShouldReturnDeliveryHistory() throws Exception {
        // Given
        List<Map<String, Object>> deliveries = new ArrayList<>();
        Map<String, Object> delivery = new HashMap<>();
        delivery.put("deliveryId", "DEL001");
        delivery.put("status", "SUCCESS");
        delivery.put("responseCode", 200);
        delivery.put("responseTime", 150);
        delivery.put("timestamp", LocalDateTime.now());
        deliveries.add(delivery);
        
        when(integrationService.getWebhookDeliveries(anyString(), anyInt(), anyInt())).thenReturn(deliveries);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/integrations/webhooks/WEBHOOK001/deliveries")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].deliveryId").value("DEL001"))
                .andExpect(jsonPath("$[0].status").value("SUCCESS"));

        verify(integrationService).getWebhookDeliveries("WEBHOOK001", 0, 20);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void retryWebhookDelivery_WithValidIds_ShouldReturnSuccess() throws Exception {
        // Given
        doNothing().when(integrationService).retryWebhookDelivery(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/webhooks/WEBHOOK001/deliveries/DEL001/retry")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Webhook delivery retry initiated"));

        verify(integrationService).retryWebhookDelivery("WEBHOOK001", "DEL001");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getRateLimitConfigurations_ShouldReturnRateLimits() throws Exception {
        // Given
        List<Map<String, Object>> rateLimits = new ArrayList<>();
        Map<String, Object> rateLimit = new HashMap<>();
        rateLimit.put("clientId", "CLIENT001");
        rateLimit.put("hourlyLimit", 1000);
        rateLimit.put("dailyLimit", 10000);
        rateLimits.add(rateLimit);
        
        when(integrationService.getRateLimitConfigurations()).thenReturn(rateLimits);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/integrations/rate-limits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].clientId").value("CLIENT001"))
                .andExpect(jsonPath("$[0].hourlyLimit").value(1000));

        verify(integrationService).getRateLimitConfigurations();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateRateLimitConfiguration_WithValidData_ShouldUpdateRateLimit() throws Exception {
        // Given
        Map<String, Object> rateLimitConfig = new HashMap<>();
        rateLimitConfig.put("hourlyLimit", 2000);
        rateLimitConfig.put("dailyLimit", 20000);
        
        doNothing().when(integrationService).updateRateLimitConfiguration(anyString(), any(Map.class));

        // When & Then
        mockMvc.perform(put("/api/v1/admin/integrations/rate-limits/CLIENT001")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rateLimitConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rate limit configuration updated successfully"));

        verify(integrationService).updateRateLimitConfiguration(eq("CLIENT001"), any(Map.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClientApiUsage_ShouldReturnUsageData() throws Exception {
        // Given
        Map<String, Object> usage = new HashMap<>();
        usage.put("requestsToday", 500);
        usage.put("requestsThisHour", 50);
        usage.put("rateLimitRemaining", 950);
        usage.put("lastRequest", LocalDateTime.now());
        
        when(integrationService.getClientApiUsage(anyString())).thenReturn(usage);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/integrations/rate-limits/CLIENT001/usage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requestsToday").value(500))
                .andExpect(jsonPath("$.requestsThisHour").value(50))
                .andExpect(jsonPath("$.rateLimitRemaining").value(950));

        verify(integrationService).getClientApiUsage("CLIENT001");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getThirdPartyIntegrations_ShouldReturnIntegrations() throws Exception {
        // Given
        List<IntegrationConfigDto> integrations = Arrays.asList(integrationConfig);
        when(integrationService.getThirdPartyIntegrations()).thenReturn(integrations);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/integrations/third-party"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("INT001"))
                .andExpect(jsonPath("$[0].name").value("Test Integration"))
                .andExpect(jsonPath("$[0].type").value("SLACK"));

        verify(integrationService).getThirdPartyIntegrations();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createThirdPartyIntegration_WithValidData_ShouldCreateIntegration() throws Exception {
        // Given
        when(integrationService.createThirdPartyIntegration(any(IntegrationConfigDto.class))).thenReturn(integrationConfig);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/third-party")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(integrationConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("INT001"))
                .andExpect(jsonPath("$.name").value("Test Integration"))
                .andExpect(jsonPath("$.type").value("SLACK"));

        verify(integrationService).createThirdPartyIntegration(any(IntegrationConfigDto.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testThirdPartyIntegration_WithValidId_ShouldReturnTestResult() throws Exception {
        // Given
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("success", true);
        testResult.put("response", "Connection successful");
        testResult.put("responseTime", 200);
        testResult.put("timestamp", LocalDateTime.now());
        
        when(integrationService.testThirdPartyIntegration(anyString())).thenReturn(testResult);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/third-party/INT001/test")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.response").value("Connection successful"));

        verify(integrationService).testThirdPartyIntegration("INT001");
    }

    @Test
    void getWebhookConfigurations_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/integrations/webhooks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createWebhookConfiguration_WithoutAdminRole_ShouldReturnForbidden() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/webhooks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookConfig)))
                .andExpect(status().isForbidden());
    }
}
