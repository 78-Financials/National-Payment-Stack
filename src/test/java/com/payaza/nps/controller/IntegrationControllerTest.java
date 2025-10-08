package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payaza.nps.dto.WebhookConfigDto;
import com.payaza.nps.dto.IntegrationConfigDto;
import com.payaza.nps.service.IntegrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for IntegrationController
 */
@ExtendWith(MockitoExtension.class)
class IntegrationControllerTest {

    @Mock
    private IntegrationService integrationService;

    @InjectMocks
    private IntegrationController integrationController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private WebhookConfigDto webhookConfig;
    private IntegrationConfigDto integrationConfig;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(integrationController).build();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup webhook config
        webhookConfig = new WebhookConfigDto();
        webhookConfig.setId("WEBHOOK001");
        webhookConfig.setName("Test Webhook");
        webhookConfig.setUrl("https://example.com/webhook");
        webhookConfig.setApiKey("test-api-key");
        webhookConfig.setEventTypes(Arrays.asList("payment.completed", "payment.failed"));
        webhookConfig.setActive(true);
        webhookConfig.setCreatedAt(LocalDateTime.now());
        webhookConfig.setUpdatedAt(LocalDateTime.now());

        // Setup integration config
        integrationConfig = new IntegrationConfigDto();
        integrationConfig.setId("INTEGRATION001");
        integrationConfig.setName("Test Integration");
        integrationConfig.setType("REST_API");
        integrationConfig.setEndpoint("https://api.example.com");
        integrationConfig.setApiKey("test-api-key");
        integrationConfig.setConfiguration(new HashMap<>());
        integrationConfig.setActive(true);
        integrationConfig.setCreatedAt(LocalDateTime.now());
        integrationConfig.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createWebhook_ShouldCreateNewWebhook() throws Exception {
        // Given
        when(integrationService.createWebhookConfiguration(any(WebhookConfigDto.class))).thenReturn(webhookConfig);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/webhooks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookConfig)))
                .andExpect(status().isOk());

        verify(integrationService).createWebhookConfiguration(any(WebhookConfigDto.class));
    }

    @Test
    void getAllWebhooks_ShouldReturnWebhookList() throws Exception {
        // Given
        when(integrationService.getAllWebhookConfigurations()).thenReturn(Arrays.asList(webhookConfig));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/integrations/webhooks"))
                .andExpect(status().isOk());

        verify(integrationService).getAllWebhookConfigurations();
    }

    @Test
    void updateWebhook_ShouldUpdateWebhook() throws Exception {
        // Given
        when(integrationService.updateWebhookConfiguration(anyString(), any(WebhookConfigDto.class))).thenReturn(webhookConfig);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/integrations/webhooks/WEBHOOK001")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookConfig)))
                .andExpect(status().isOk());

        verify(integrationService).updateWebhookConfiguration(eq("WEBHOOK001"), any(WebhookConfigDto.class));
    }

    @Test
    void deleteWebhook_ShouldDeleteWebhook() throws Exception {
        // Given
        doNothing().when(integrationService).deleteWebhookConfiguration("WEBHOOK001");

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/integrations/webhooks/WEBHOOK001"))
                .andExpect(status().isOk());

        verify(integrationService).deleteWebhookConfiguration("WEBHOOK001");
    }

    @Test
    void testWebhook_ShouldTestWebhook() throws Exception {
        // Given
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("success", true);
        testResult.put("response", "Test successful");
        when(integrationService.testWebhookConfiguration("WEBHOOK001")).thenReturn(testResult);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/webhooks/WEBHOOK001/test"))
                .andExpect(status().isOk());

        verify(integrationService).testWebhookConfiguration("WEBHOOK001");
    }

    @Test
    void createIntegration_ShouldCreateNewIntegration() throws Exception {
        // Given
        when(integrationService.createThirdPartyIntegration(any(IntegrationConfigDto.class))).thenReturn(integrationConfig);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/third-party")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(integrationConfig)))
                .andExpect(status().isOk());

        verify(integrationService).createThirdPartyIntegration(any(IntegrationConfigDto.class));
    }

    @Test
    void getAllIntegrations_ShouldReturnIntegrationList() throws Exception {
        // Given
        when(integrationService.getThirdPartyIntegrations()).thenReturn(Arrays.asList(integrationConfig));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/integrations/third-party"))
                .andExpect(status().isOk());

        verify(integrationService).getThirdPartyIntegrations();
    }

    @Test
    void testIntegration_ShouldTestIntegration() throws Exception {
        // Given
        Map<String, Object> testResult = new HashMap<>();
        testResult.put("success", true);
        testResult.put("response", "Test successful");
        when(integrationService.testThirdPartyIntegration("INTEGRATION001")).thenReturn(testResult);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/integrations/third-party/INTEGRATION001/test"))
                .andExpect(status().isOk());

        verify(integrationService).testThirdPartyIntegration("INTEGRATION001");
    }
}