package com.payaza.nps.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.repository.InternalClientRepository;
import com.payaza.nps.repository.PaymentTransactionLiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for complete payment flow
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@Transactional
class PaymentFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InternalClientRepository clientRepository;

    @Autowired
    private PaymentTransactionLiveRepository transactionRepository;

    private InternalClient testClient;
    private Pacs008RequestDto validRequest;

    @BeforeEach
    void setUp() {
        // Setup test client
        testClient = new InternalClient();
        testClient.setClientId("TEST_BANK");
        testClient.setClientName("Test Bank");
        testClient.setApiKey("test_api_key_integration_12345");
        testClient.setActive(true);
        testClient.setTransactionPrefix("TST");
        testClient.setRateLimit(1000);
        testClient.setCreatedAt(LocalDateTime.now());
        testClient.setUpdatedAt(LocalDateTime.now());
        clientRepository.save(testClient);

        // Setup valid request
        validRequest = new Pacs008RequestDto();
        validRequest.setMessageId("MSG" + System.currentTimeMillis());
        validRequest.setTransactionId("TST-" + System.currentTimeMillis());
        validRequest.setSenderInstitutionCode("001");
        validRequest.setReceiverInstitutionCode("002");
        validRequest.setSenderAccountNumber("1234567890");
        validRequest.setReceiverAccountNumber("0987654321");
        validRequest.setSenderAccountName("John Doe");
        validRequest.setReceiverAccountName("Jane Smith");
        validRequest.setAmount(new BigDecimal("1000.00"));
        validRequest.setCurrency("NGN");
        validRequest.setNarration("Integration test payment");
    }

    @Test
    void completePaymentFlow_ShouldCreateTransactionRecord() throws Exception {
        // When
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value(validRequest.getMessageId()))
                .andExpect(jsonPath("$.transactionId").value(validRequest.getTransactionId()));

        // Then - Verify transaction was recorded
        var transactions = transactionRepository.findByTransactionId(validRequest.getTransactionId());
        assertNotNull(transactions);
        assertEquals(validRequest.getTransactionId(), transactions.getTransactionId());
        assertEquals("PENDING", transactions.getStatus());
        assertEquals(testClient.getClientId(), transactions.getClientId());
    }

    @Test
    void paymentFlow_WithInvalidApiKey_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "invalid_api_key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void paymentFlow_WithInactiveClient_ShouldReturn403() throws Exception {
        // Given
        testClient.setActive(false);
        clientRepository.save(testClient);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void paymentFlow_WithInvalidTransactionPrefix_ShouldReturn400() throws Exception {
        // Given
        validRequest.setTransactionId("INVALID-" + System.currentTimeMillis());

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentFlow_WithMissingRequiredFields_ShouldReturn400() throws Exception {
        // Given
        validRequest.setAmount(null); // Missing required field

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentFlow_WithNegativeAmount_ShouldReturn400() throws Exception {
        // Given
        validRequest.setAmount(new BigDecimal("-100.00"));

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentFlow_WithZeroAmount_ShouldReturn400() throws Exception {
        // Given
        validRequest.setAmount(BigDecimal.ZERO);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentFlow_WithInvalidCurrency_ShouldReturn400() throws Exception {
        // Given
        validRequest.setCurrency("INVALID");

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void paymentFlow_WithDuplicateTransactionId_ShouldReturn400() throws Exception {
        // Given - Create first transaction
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk());

        // When - Try to create duplicate transaction
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", testClient.getApiKey())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());
    }
}
