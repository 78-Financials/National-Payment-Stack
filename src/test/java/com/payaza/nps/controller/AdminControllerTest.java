package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.CreateClientRequestDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import com.payaza.nps.service.ApiKeyGenerationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminController
 */
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@ActiveProfiles("test")
@Transactional
public class AdminControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private InternalClientRepository clientRepository;

    @Autowired
    private ApiKeyGenerationService apiKeyGenerationService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
        
        // Clean up test data
        clientRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateClient_Success() throws Exception {
        // Arrange
        CreateClientRequestDto request = new CreateClientRequestDto();
        request.setClientId("TEST");
        request.setClientName("Test Client");
        request.setTransactionPrefix("TST");
        request.setAllowedEndpoints(Set.of("pacs008", "acmt023"));
        request.setActive(true);
        request.setRateLimitPerMinute(100);
        request.setContactEmail("test@example.com");

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clientId", is("TEST")))
                .andExpect(jsonPath("$.clientName", is("Test Client")))
                .andExpect(jsonPath("$.apiKey", notNullValue()))
                .andExpect(jsonPath("$.warning", containsString("IMPORTANT")));

        // Verify client was saved to database
        assert clientRepository.findByClientId("TEST").isPresent();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateClient_DuplicateClientId() throws Exception {
        // Arrange - Create existing client
        InternalClient existingClient = new InternalClient();
        existingClient.setClientId("EXIST");
        existingClient.setClientName("Existing Client");
        existingClient.setApiKey("existing_api_key");
        existingClient.setTransactionPrefix("EXT");
        existingClient.setAllowedEndpoints(Set.of("pacs008"));
        existingClient.setActive(true);
        existingClient.setRateLimitPerMinute(100);
        existingClient.setCreatedBy("TEST");
        existingClient.setUpdatedBy("TEST");
        clientRepository.save(existingClient);

        CreateClientRequestDto request = new CreateClientRequestDto();
        request.setClientId("EXIST"); // Duplicate
        request.setClientName("New Client");
        request.setTransactionPrefix("NEW");
        request.setAllowedEndpoints(Set.of("pacs008"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("CLIENT_ERROR")))
                .andExpect(jsonPath("$.message", containsString("already exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateClient_DuplicateTransactionPrefix() throws Exception {
        // Arrange - Create existing client with transaction prefix
        InternalClient existingClient = new InternalClient();
        existingClient.setClientId("EXIST");
        existingClient.setClientName("Existing Client");
        existingClient.setApiKey("existing_api_key");
        existingClient.setTransactionPrefix("EXT");
        existingClient.setAllowedEndpoints(Set.of("pacs008"));
        existingClient.setActive(true);
        existingClient.setRateLimitPerMinute(100);
        existingClient.setCreatedBy("TEST");
        existingClient.setUpdatedBy("TEST");
        clientRepository.save(existingClient);

        CreateClientRequestDto request = new CreateClientRequestDto();
        request.setClientId("NEW");
        request.setClientName("New Client");
        request.setTransactionPrefix("EXT"); // Duplicate prefix
        request.setAllowedEndpoints(Set.of("pacs008"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("CLIENT_ERROR")))
                .andExpect(jsonPath("$.message", containsString("Transaction prefix")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateClient_ValidationErrors() throws Exception {
        // Arrange - Invalid request (missing required fields)
        CreateClientRequestDto request = new CreateClientRequestDto();
        request.setClientId(""); // Empty client ID
        request.setClientName("Test Client");
        // Missing transaction prefix
        request.setAllowedEndpoints(new HashSet<>()); // Empty endpoints

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllClients_Success() throws Exception {
        // Arrange - Create test clients
        createTestClients();

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/clients")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].clientId", isOneOf("CLIENT1", "CLIENT2")))
                .andExpect(jsonPath("$.content[0].apiKey", nullValue())); // API key should not be exposed
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetClientById_Success() throws Exception {
        // Arrange
        InternalClient client = createTestClient("TEST", "Test Client", "TST");
        InternalClient savedClient = clientRepository.save(client);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/clients/{id}", savedClient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is("TEST")))
                .andExpect(jsonPath("$.clientName", is("Test Client")))
                .andExpect(jsonPath("$.transactionPrefix", is("TST")))
                .andExpect(jsonPath("$.apiKey", nullValue())); // API key should not be exposed
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetClientById_NotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/clients/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateClient_Success() throws Exception {
        // Arrange
        InternalClient client = createTestClient("TEST", "Test Client", "TST");
        InternalClient savedClient = clientRepository.save(client);

        CreateClientRequestDto updateRequest = new CreateClientRequestDto();
        updateRequest.setClientId("TEST");
        updateRequest.setClientName("Updated Test Client");
        updateRequest.setTransactionPrefix("TST");
        updateRequest.setAllowedEndpoints(Set.of("pacs008", "pacs002"));
        updateRequest.setActive(true);
        updateRequest.setRateLimitPerMinute(200);

        // Act & Assert
        mockMvc.perform(put("/api/v1/admin/clients/{id}", savedClient.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientName", is("Updated Test Client")))
                .andExpect(jsonPath("$.rateLimitPerMinute", is(200)))
                .andExpect(jsonPath("$.allowedEndpoints", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteClient_Success() throws Exception {
        // Arrange
        InternalClient client = createTestClient("TEST", "Test Client", "TST");
        InternalClient savedClient = clientRepository.save(client);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/admin/clients/{id}", savedClient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Client deleted successfully")));

        // Verify client was deleted
        assert clientRepository.findById(savedClient.getId()).isEmpty();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testRegenerateApiKey_Success() throws Exception {
        // Arrange
        InternalClient client = createTestClient("TEST", "Test Client", "TST");
        InternalClient savedClient = clientRepository.save(client);
        String originalApiKey = savedClient.getApiKey();

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/clients/{id}/regenerate-api-key", savedClient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId", is("TEST")))
                .andExpect(jsonPath("$.apiKey", notNullValue()))
                .andExpect(jsonPath("$.apiKey", not(is(originalApiKey))))
                .andExpect(jsonPath("$.warning", containsString("IMPORTANT")));

        // Verify API key was updated in database
        InternalClient updatedClient = clientRepository.findById(savedClient.getId()).orElseThrow();
        assert !updatedClient.getApiKey().equals(originalApiKey);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testToggleClientStatus_Success() throws Exception {
        // Arrange
        InternalClient client = createTestClient("TEST", "Test Client", "TST");
        client.setActive(true);
        InternalClient savedClient = clientRepository.save(client);

        // Act & Assert
        mockMvc.perform(patch("/api/v1/admin/clients/{id}/toggle-status", savedClient.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active", is(false)));

        // Verify status was toggled
        InternalClient updatedClient = clientRepository.findById(savedClient.getId()).orElseThrow();
        assert !updatedClient.isActive();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetClientStatistics_Success() throws Exception {
        // Arrange - Create test clients
        InternalClient activeClient = createTestClient("ACTIVE", "Active Client", "ACT");
        activeClient.setActive(true);
        clientRepository.save(activeClient);

        InternalClient inactiveClient = createTestClient("INACTIVE", "Inactive Client", "INA");
        inactiveClient.setActive(false);
        clientRepository.save(inactiveClient);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/clients/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalClients", is(2)))
                .andExpect(jsonPath("$.activeClients", is(1)))
                .andExpect(jsonPath("$.inactiveClients", is(1)));
    }

    @Test
    @WithMockUser(roles = "USER") // Non-admin user
    void testCreateClient_Unauthorized() throws Exception {
        // Arrange
        CreateClientRequestDto request = new CreateClientRequestDto();
        request.setClientId("TEST");
        request.setClientName("Test Client");
        request.setTransactionPrefix("TST");
        request.setAllowedEndpoints(Set.of("pacs008"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // Helper methods
    private InternalClient createTestClient(String clientId, String clientName, String transactionPrefix) {
        InternalClient client = new InternalClient();
        client.setClientId(clientId);
        client.setClientName(clientName);
        client.setApiKey(apiKeyGenerationService.generateClientApiKey(clientId));
        client.setTransactionPrefix(transactionPrefix);
        client.setAllowedEndpoints(Set.of("pacs008", "acmt023"));
        client.setActive(true);
        client.setRateLimitPerMinute(100);
        client.setCreatedBy("TEST");
        client.setUpdatedBy("TEST");
        return client;
    }

    private void createTestClients() {
        InternalClient client1 = createTestClient("CLIENT1", "Client One", "CL1");
        InternalClient client2 = createTestClient("CLIENT2", "Client Two", "CL2");
        clientRepository.save(client1);
        clientRepository.save(client2);
    }
}
