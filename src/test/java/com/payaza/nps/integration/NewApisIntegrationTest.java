package com.payaza.nps.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.LoginRequestDto;
import com.payaza.nps.dto.ReportRequestDto;
import com.payaza.nps.dto.WebhookConfigDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for new APIs
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class NewApisIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternalClientRepository clientRepository;

    private InternalClient testClient;
    private String authToken;

    @BeforeEach
    void setUp() {
        // Create test client
        testClient = new InternalClient();
        testClient.setClientId("TEST_CLIENT");
        testClient.setClientName("Test Client");
        testClient.setApiKey("test_api_key_12345");
        testClient.setTransactionPrefix("TST");
        testClient.setClientType("BANK");
        testClient.setActive(true);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        
        clientRepository.save(testClient);
    }

    @Test
    void authenticationFlow_ShouldWorkEndToEnd() throws Exception {
        // Given
        LoginRequestDto loginRequest = new LoginRequestDto();
        loginRequest.setClientId("TEST_CLIENT");
        loginRequest.setApiKey("test_api_key_12345");

        // When - Login
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.clientId").value("TEST_CLIENT"))
                .andExpect(jsonPath("$.clientName").value("Test Client"))
                .andReturn();

        // Extract token from response
        String responseContent = loginResult.getResponse().getContentAsString();
        authToken = objectMapper.readTree(responseContent).get("token").asText();
        assertThat(authToken).isNotNull();

        // When - Get Profile
        mockMvc.perform(get("/api/v1/auth/profile")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("TEST_CLIENT"))
                .andExpect(jsonPath("$.clientName").value("Test Client"));

        // When - Refresh Token
        mockMvc.perform(post("/api/v1/auth/refresh")
                .with(csrf())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.expiresAt").exists());

        // When - Logout
        mockMvc.perform(post("/api/v1/auth/logout")
                .with(csrf())
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reportGenerationFlow_ShouldWorkEndToEnd() throws Exception {
        // Given
        ReportRequestDto reportRequest = new ReportRequestDto();
        reportRequest.setReportType("transaction_summary");
        reportRequest.setReportName("Test Report");
        reportRequest.setDescription("Test report for integration testing");
        reportRequest.setFromDate(LocalDate.now().minusDays(1));
        reportRequest.setToDate(LocalDate.now());
        reportRequest.setFormat("json");

        // When - Generate Report
        MvcResult reportResult = mockMvc.perform(post("/api/v1/admin/reports/generate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reportRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reportId").exists())
                .andExpect(jsonPath("$.reportName").value("Test Report"))
                .andExpect(jsonPath("$.reportType").value("transaction_summary"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andReturn();

        // Extract report ID
        String responseContent = reportResult.getResponse().getContentAsString();
        String reportId = objectMapper.readTree(responseContent).get("reportId").asText();

        // When - Export Report
        mockMvc.perform(get("/api/v1/admin/reports/export/" + reportId)
                .param("format", "csv"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Content-Disposition"));

        // When - Get Report Templates
        mockMvc.perform(get("/api/v1/admin/reports/templates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists());

        // When - Get Real-time Metrics
        mockMvc.perform(get("/api/v1/admin/reports/metrics/realtime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.systemUptime").exists())
                .andExpect(jsonPath("$.activeConnections").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void webhookManagementFlow_ShouldWorkEndToEnd() throws Exception {
        // Given
        WebhookConfigDto webhookConfig = new WebhookConfigDto();
        webhookConfig.setName("Test Webhook");
        webhookConfig.setUrl("https://example.com/webhook");
        webhookConfig.setApiKey("webhook_api_key");
        webhookConfig.setEventTypes(Arrays.asList("PAYMENT_SUCCESS", "PAYMENT_FAILED"));
        webhookConfig.setActive(true);
        webhookConfig.setRetryAttempts(3);
        webhookConfig.setTimeoutSeconds(30);

        // When - Create Webhook
        MvcResult createResult = mockMvc.perform(post("/api/v1/admin/integrations/webhooks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Webhook"))
                .andExpect(jsonPath("$.url").value("https://example.com/webhook"))
                .andReturn();

        // Extract webhook ID
        String responseContent = createResult.getResponse().getContentAsString();
        String webhookId = objectMapper.readTree(responseContent).get("id").asText();

        // When - Get All Webhooks
        mockMvc.perform(get("/api/v1/admin/integrations/webhooks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(webhookId));

        // When - Test Webhook
        mockMvc.perform(post("/api/v1/admin/integrations/webhooks/" + webhookId + "/test")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").exists());

        // When - Get Webhook Deliveries
        mockMvc.perform(get("/api/v1/admin/integrations/webhooks/" + webhookId + "/deliveries")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // When - Update Webhook
        webhookConfig.setName("Updated Webhook Name");
        mockMvc.perform(put("/api/v1/admin/integrations/webhooks/" + webhookId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(webhookConfig)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Webhook Name"));

        // When - Delete Webhook
        mockMvc.perform(delete("/api/v1/admin/integrations/webhooks/" + webhookId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Webhook configuration deleted successfully"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void systemMonitoringFlow_ShouldWorkEndToEnd() throws Exception {
        // When - Get System Health
        mockMvc.perform(get("/api/v1/admin/system/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.uptime").exists())
                .andExpect(jsonPath("$.version").exists());

        // When - Get System Metrics
        mockMvc.perform(get("/api/v1/admin/system/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resources").exists())
                .andExpect(jsonPath("$.application").exists());

        // When - Get Performance Metrics
        mockMvc.perform(get("/api/v1/admin/system/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageResponseTime").exists())
                .andExpect(jsonPath("$.throughput").exists());

        // When - Get Database Health
        mockMvc.perform(get("/api/v1/admin/system/database/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());

        // When - Get External Services Health
        mockMvc.perform(get("/api/v1/admin/system/external-services/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());

        // When - Get System Logs
        mockMvc.perform(get("/api/v1/admin/system/logs")
                .param("page", "0")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // When - Search Logs
        mockMvc.perform(get("/api/v1/admin/system/logs/search")
                .param("query", "test")
                .param("page", "0")
                .param("size", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // When - Get Log Statistics
        mockMvc.perform(get("/api/v1/admin/system/logs/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLogs").exists());

        // When - Get System Alerts
        mockMvc.perform(get("/api/v1/admin/system/alerts")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        // When - Get System Configuration
        mockMvc.perform(get("/api/v1/admin/system/configuration"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxConnections").exists());
    }

    @Test
    void rateLimitingFlow_ShouldWorkEndToEnd() throws Exception {
        // When - Get Rate Limit Configurations
        mockMvc.perform(get("/api/v1/admin/integrations/rate-limits"))
                .andExpect(status().isForbidden()); // Should require admin role

        // When - Get Client API Usage
        mockMvc.perform(get("/api/v1/admin/integrations/rate-limits/TEST_CLIENT/usage"))
                .andExpect(status().isForbidden()); // Should require admin role
    }

    @Test
    void securityFlow_ShouldEnforceAuthentication() throws Exception {
        // When - Try to access protected endpoint without authentication
        mockMvc.perform(get("/api/v1/auth/profile"))
                .andExpect(status().isUnauthorized());

        // When - Try to access admin endpoint without admin role
        mockMvc.perform(get("/api/v1/admin/reports/templates"))
                .andExpect(status().isForbidden());

        // When - Try to access system endpoint without admin role
        mockMvc.perform(get("/api/v1/admin/system/health"))
                .andExpect(status().isForbidden());
    }

    @Test
    void errorHandlingFlow_ShouldReturnAppropriateErrors() throws Exception {
        // Given - Invalid login request
        LoginRequestDto invalidLoginRequest = new LoginRequestDto();
        invalidLoginRequest.setClientId("INVALID_CLIENT");
        invalidLoginRequest.setApiKey("invalid_api_key");

        // When - Try to login with invalid credentials
        mockMvc.perform(post("/api/v1/auth/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidLoginRequest)))
                .andExpect(status().isBadRequest());

        // Given - Invalid report request
        ReportRequestDto invalidReportRequest = new ReportRequestDto();
        // Missing required fields

        // When - Try to generate report with invalid data
        mockMvc.perform(post("/api/v1/admin/reports/generate")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidReportRequest)))
                .andExpect(status().isForbidden()); // Should require admin role
    }
}
