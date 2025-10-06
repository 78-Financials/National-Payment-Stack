package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.InternalClientRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminController
 */
@WebMvcTest(AdminController.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InternalClientRepository clientRepository;

    @MockBean
    private InternalClientRegistry clientRegistry;

    @MockBean
    private AuditService auditService;

    @Autowired
    private ObjectMapper objectMapper;

    private InternalClient testClient;

    @BeforeEach
    void setUp() {
        testClient = new InternalClient();
        testClient.setId(1L);
        testClient.setClientId("TEST_CLIENT");
        testClient.setClientName("Test Client");
        testClient.setApiKey("test_api_key_12345");
        testClient.setActive(true);
        testClient.setTransactionPrefix("TST");
        testClient.setRateLimit(1000);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllClients_ShouldReturnClientList() throws Exception {
        // Given
        when(clientRepository.findAll()).thenReturn(Arrays.asList(testClient));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/clients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].clientId").value("TEST_CLIENT"))
                .andExpect(jsonPath("$[0].clientName").value("Test Client"))
                .andExpect(jsonPath("$[0].active").value(true));

        verify(auditService).logAdminAction(anyString(), any(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClientById_ShouldReturnClient() throws Exception {
        // Given
        when(clientRepository.findByClientId("TEST_CLIENT")).thenReturn(Optional.of(testClient));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/clients/TEST_CLIENT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("TEST_CLIENT"))
                .andExpect(jsonPath("$.clientName").value("Test Client"));

        verify(auditService).logAdminAction(anyString(), any(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getClientById_NotFound_ShouldReturn404() throws Exception {
        // Given
        when(clientRepository.findByClientId("NON_EXISTENT")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/admin/clients/NON_EXISTENT"))
                .andExpect(status().isNotFound());

        verify(auditService).logError(anyString(), anyString(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createClient_ShouldCreateNewClient() throws Exception {
        // Given
        InternalClient newClient = new InternalClient();
        newClient.setClientId("NEW_CLIENT");
        newClient.setClientName("New Client");
        newClient.setTransactionPrefix("NEW");
        newClient.setRateLimit(500);

        when(clientRepository.findByClientId("NEW_CLIENT")).thenReturn(Optional.empty());
        when(clientRepository.save(any(InternalClient.class))).thenReturn(newClient);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/clients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newClient)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId").value("NEW_CLIENT"))
                .andExpect(jsonPath("$.clientName").value("New Client"))
                .andExpect(jsonPath("$.apiKey").exists());

        verify(clientRepository).save(any(InternalClient.class));
        verify(auditService).logAdminAction(anyString(), any(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createClient_DuplicateClientId_ShouldReturn400() throws Exception {
        // Given
        InternalClient existingClient = new InternalClient();
        existingClient.setClientId("EXISTING_CLIENT");
        existingClient.setClientName("Existing Client");

        when(clientRepository.findByClientId("EXISTING_CLIENT")).thenReturn(Optional.of(existingClient));

        // When & Then
        mockMvc.perform(post("/api/v1/admin/clients")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(existingClient)))
                .andExpect(status().isBadRequest());

        verify(auditService).logError(anyString(), anyString(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateClient_ShouldUpdateExistingClient() throws Exception {
        // Given
        InternalClient updatedClient = new InternalClient();
        updatedClient.setClientId("TEST_CLIENT");
        updatedClient.setClientName("Updated Client Name");
        updatedClient.setTransactionPrefix("TST");
        updatedClient.setRateLimit(2000);

        when(clientRepository.findByClientId("TEST_CLIENT")).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(InternalClient.class))).thenReturn(updatedClient);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/clients/TEST_CLIENT")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedClient)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("TEST_CLIENT"))
                .andExpect(jsonPath("$.clientName").value("Updated Client Name"));

        verify(clientRepository).save(any(InternalClient.class));
        verify(auditService).logAdminAction(anyString(), any(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteClient_ShouldDeleteClient() throws Exception {
        // Given
        when(clientRepository.findByClientId("TEST_CLIENT")).thenReturn(Optional.of(testClient));

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/clients/TEST_CLIENT")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Client deleted successfully"));

        verify(clientRepository).delete(any(InternalClient.class));
        verify(auditService).logAdminAction(anyString(), any(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void regenerateApiKey_ShouldGenerateNewApiKey() throws Exception {
        // Given
        when(clientRepository.findByClientId("TEST_CLIENT")).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(InternalClient.class))).thenReturn(testClient);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/clients/TEST_CLIENT/regenerate-api-key")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiKey").exists());

        verify(clientRepository).save(any(InternalClient.class));
        verify(auditService).logAdminAction(anyString(), any(), anyString(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllClients_WithoutAdminRole_ShouldReturn403() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/admin/clients"))
                .andExpect(status().isForbidden());
    }
}