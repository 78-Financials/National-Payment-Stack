package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.CreateClientRequestDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import com.payaza.nps.service.ApiKeyGenerationService;
import com.payaza.nps.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for AdminController
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private InternalClientRepository clientRepository;

    @Mock
    private ApiKeyGenerationService apiKeyGenerationService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private AdminController adminController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private InternalClient testClient;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
        objectMapper = new ObjectMapper();
        
        testClient = new InternalClient();
        testClient.setId(1L);
        testClient.setClientId("test");
        testClient.setClientName("Test Client");
        testClient.setApiKey("test-api-key-12345678901234567890");
        testClient.setContactEmail("test@example.com");
        testClient.setTransactionPrefix("TST");
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setActive(true);
    }

    @Test
    void createClient_ShouldCreateNewClient() throws Exception {
        // Given
        CreateClientRequestDto requestDto = new CreateClientRequestDto();
        requestDto.setClientId("new");
        requestDto.setClientName("New Client");
        requestDto.setTransactionPrefix("NEW");
        requestDto.setAllowedEndpoints(new HashSet<>(Arrays.asList("pacs.008", "pacs.002")));
        
        InternalClient newClient = new InternalClient();
        newClient.setClientId("new");
        newClient.setClientName("New Client");
        newClient.setTransactionPrefix("NEW");
        
        when(clientRepository.existsByClientId("new")).thenReturn(false);
        when(clientRepository.existsByTransactionPrefix("NEW")).thenReturn(false);
        when(clientRepository.existsByApiKey(anyString())).thenReturn(false);
        when(apiKeyGenerationService.generateClientApiKey("new")).thenReturn("generated-api-key-12345678901234567890");
        when(clientRepository.save(any(InternalClient.class))).thenReturn(newClient);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(clientRepository).save(any(InternalClient.class));
        verify(auditService).logAdminAction(eq("CREATE_CLIENT"), eq("InternalClient"), eq("ADMIN"), 
                                          any(), anyString(), any());
    }

    @Test
    void createClient_DuplicateClientId_ShouldReturn400() throws Exception {
        // Given
        CreateClientRequestDto requestDto = new CreateClientRequestDto();
        requestDto.setClientId("test");
        requestDto.setClientName("Duplicate Client");
        requestDto.setTransactionPrefix("DUP");
        requestDto.setAllowedEndpoints(new HashSet<>(Arrays.asList("pacs.008")));
        
        when(clientRepository.existsByClientId("test")).thenReturn(true);
        // Don't mock save() since it shouldn't be called

        // When & Then
        mockMvc.perform(post("/api/v1/admin/clients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(clientRepository, never()).save(any(InternalClient.class));
    }

    @Test
    void getAllClients_ShouldReturnClientList() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Page<InternalClient> page = new PageImpl<>(Arrays.asList(testClient), pageable, 1);
        when(clientRepository.findAll(any(Pageable.class))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/v1/admin/clients"))
                .andExpect(status().isOk());

        verify(clientRepository).findAll(any(Pageable.class));
    }

    @Test
    void getClientById_ShouldReturnClient() throws Exception {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));

        // When & Then
        mockMvc.perform(get("/api/v1/admin/clients/1"))
                .andExpect(status().isOk());

        verify(clientRepository).findById(1L);
    }

    @Test
    void getClientById_NotFound_ShouldReturn404() throws Exception {
        // Given
        when(clientRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/admin/clients/999"))
                .andExpect(status().isNotFound());

        verify(clientRepository).findById(999L);
    }

    @Test
    void updateClient_ShouldUpdateExistingClient() throws Exception {
        // Given
        CreateClientRequestDto requestDto = new CreateClientRequestDto();
        requestDto.setClientId("test");
        requestDto.setClientName("Updated Client Name");
        requestDto.setTransactionPrefix("UPD");
        requestDto.setAllowedEndpoints(new HashSet<>(Arrays.asList("pacs.008")));
        
        InternalClient updatedClient = new InternalClient();
        updatedClient.setClientId("test");
        updatedClient.setClientName("Updated Client Name");
        updatedClient.setTransactionPrefix("UPD");
        
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(InternalClient.class))).thenReturn(updatedClient);

        // When & Then
        mockMvc.perform(put("/api/v1/admin/clients/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(clientRepository).save(any(InternalClient.class));
    }

    @Test
    void deleteClient_ShouldDeleteClient() throws Exception {
        // Given
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        doNothing().when(clientRepository).deleteById(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/admin/clients/1"))
                .andExpect(status().isOk());

        verify(clientRepository).deleteById(1L);
    }

    @Test
    void regenerateApiKey_ShouldGenerateNewApiKey() throws Exception {
        // Given
        String newApiKey = "new-api-key-12345678901234567890";
        when(clientRepository.findById(1L)).thenReturn(Optional.of(testClient));
        when(apiKeyGenerationService.generateClientApiKey(anyString())).thenReturn(newApiKey);
        when(clientRepository.save(any(InternalClient.class))).thenReturn(testClient);

        // When & Then
        mockMvc.perform(post("/api/v1/admin/clients/1/regenerate-api-key"))
                .andExpect(status().isOk());

        verify(apiKeyGenerationService).generateClientApiKey(anyString());
        verify(clientRepository).save(any(InternalClient.class));
    }
}