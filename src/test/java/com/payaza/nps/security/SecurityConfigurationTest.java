package com.payaza.nps.security;

import com.payaza.nps.service.InternalClientRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security configuration tests
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InternalClientRegistry clientRegistry;

    @Test
    void publicEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        // Health check endpoint
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

        // API documentation
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoints_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/admin/clients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpoints_WithUserRole_ShouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/admin/clients"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/admin/clients"))
                .andExpect(status().isOk());
    }

    @Test
    void apiEndpoints_WithoutApiKey_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/payments/transfer"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/identification/verify"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void apiEndpoints_WithInvalidApiKey_ShouldReturn401() throws Exception {
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "invalid_key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void callbackEndpoints_ShouldBeAccessibleWithoutAuthentication() throws Exception {
        // NIBSS callbacks should be accessible without authentication
        mockMvc.perform(post("/api/v1/callbacks/nibss"))
                .andExpect(status().isOk());
    }

    @Test
    void analyticsEndpoints_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/analytics/transactions"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void analyticsEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/analytics/dashboard"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/analytics/transactions"))
                .andExpect(status().isOk());
    }

    @Test
    void alertEndpoints_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/rules"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/alerts/rules"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void alertEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/alerts/rules"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/alerts/active"))
                .andExpect(status().isOk());
    }

    @Test
    void auditEndpoints_WithoutAuthentication_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void auditEndpoints_WithAdminRole_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/api/v1/audit/logs"))
                .andExpect(status().isOk());
    }

    @Test
    void corsHeaders_ShouldBePresent() throws Exception {
        mockMvc.perform(get("/actuator/health")
                .header("Origin", "https://example.com"))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String corsHeader = result.getResponse().getHeader("Access-Control-Allow-Origin");
                    assert corsHeader != null;
                });
    }

    @Test
    void csrfProtection_ShouldBeEnabledForStateChangingOperations() throws Exception {
        mockMvc.perform(post("/api/v1/admin/clients")
                .header("X-API-Key", "valid_key"))
                .andExpect(status().isForbidden()); // CSRF token required
    }
}
