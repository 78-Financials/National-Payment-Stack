package com.payaza.nps.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security audit tests to verify security configurations
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityAuditTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void securityHeaders_ShouldBePresent() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Content-Type-Options"))
                .andExpect(header().exists("X-Frame-Options"))
                .andExpect(header().exists("X-XSS-Protection"))
                .andExpect(header().exists("Strict-Transport-Security"))
                .andExpect(header().exists("Content-Security-Policy"));
    }

    @Test
    void apiEndpoints_ShouldRequireAuthentication() throws Exception {
        // Test payment endpoint requires authentication
        mockMvc.perform(post("/api/v1/payments/transfer"))
                .andExpect(status().isUnauthorized());

        // Test identification endpoint requires authentication
        mockMvc.perform(post("/api/v1/identification/verify"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoints_ShouldRequireAdminRole() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/admin/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void publicEndpoints_ShouldBeAccessible() throws Exception {
        // Health check should be accessible
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        // NIBSS callbacks should be accessible
        mockMvc.perform(post("/api/v1/callbacks/nibss"))
                .andExpect(status().isOk());
    }

    @Test
    void csrfProtection_ShouldBeEnabled() throws Exception {
        // CSRF protection should be enabled for state-changing operations
        mockMvc.perform(post("/api/v1/admin/clients")
                .header("X-API-Key", "valid_key"))
                .andExpect(status().isForbidden()); // CSRF token required
    }

    @Test
    void corsConfiguration_ShouldBePresent() throws Exception {
        mockMvc.perform(options("/api/v1/payments/transfer")
                .header("Origin", "https://example.com")
                .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"))
                .andExpect(header().exists("Access-Control-Allow-Headers"));
    }

    @Test
    void errorResponses_ShouldNotLeakInformation() throws Exception {
        // Test that error responses don't leak sensitive information
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "invalid_key")
                .contentType("application/json")
                .content("{\"invalid\": \"json\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.stackTrace").doesNotExist())
                .andExpect(jsonPath("$.path").doesNotExist())
                .andExpect(jsonPath("$.exception").doesNotExist());
    }

    @Test
    void rateLimiting_ShouldBeConfigured() throws Exception {
        // Test rate limiting headers
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"));
    }

    @Test
    void contentTypeValidation_ShouldBeEnforced() throws Exception {
        // Test that content type validation is enforced
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "valid_key")
                .contentType("text/plain")
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void inputValidation_ShouldRejectMaliciousInput() throws Exception {
        // Test SQL injection protection
        String maliciousInput = "'; DROP TABLE users; --";
        
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "valid_key")
                .contentType("application/json")
                .content(String.format("{\"transactionId\": \"%s\"}", maliciousInput)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sessionManagement_ShouldBeSecure() throws Exception {
        // Test that sessions are properly managed
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("JSESSIONID"))
                .andExpect(cookie().httpOnly("JSESSIONID", true))
                .andExpect(cookie().secure("JSESSIONID", true));
    }
}
