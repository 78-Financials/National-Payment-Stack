package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.dto.Pacs008ResponseDto;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.PaymentStatusTrackingService;
import com.payaza.nps.service.SimplePacs008Service;
import com.payaza.nps.validation.ClientPermissionValidator;
import com.payaza.nps.validation.TransactionIdValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for Pacs008Controller
 */
@WebMvcTest(Pacs008Controller.class)
class Pacs008ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SimplePacs008Service pacs008Service;

    @MockBean
    private ClientPermissionValidator permissionValidator;

    @MockBean
    private TransactionIdValidator transactionIdValidator;

    @MockBean
    private AuditService auditService;

    @MockBean
    private PaymentStatusTrackingService statusTrackingService;

    @Autowired
    private ObjectMapper objectMapper;

    private Pacs008RequestDto validRequest;
    private Pacs008ResponseDto successResponse;

    @BeforeEach
    void setUp() {
        // Setup valid request
        validRequest = new Pacs008RequestDto();
        validRequest.setMessageId("MSG123456789");
        validRequest.setTransactionId("BAN-123456789");
        validRequest.setSenderInstitutionCode("001");
        validRequest.setReceiverInstitutionCode("002");
        validRequest.setSenderAccountNumber("1234567890");
        validRequest.setReceiverAccountNumber("0987654321");
        validRequest.setSenderAccountName("John Doe");
        validRequest.setReceiverAccountName("Jane Smith");
        validRequest.setAmount(new BigDecimal("1000.00"));
        validRequest.setCurrency("NGN");
        validRequest.setNarration("Test payment");

        // Setup success response
        successResponse = new Pacs008ResponseDto();
        successResponse.setMessageId("MSG123456789");
        successResponse.setTransactionId("BAN-123456789");
        successResponse.setStatus("SUCCESS");
        successResponse.setResponseCode("00");
        successResponse.setResponseMessage("Payment processed successfully");

        // Setup ClientContext
        ClientContext.setCurrentClientId("BANK001");
    }

    @Test
    void processPayment_ValidRequest_ShouldReturnSuccess() throws Exception {
        // Given
        when(permissionValidator.validateClientPermission("BANK001", "PACS008")).thenReturn(true);
        when(transactionIdValidator.validateTransactionId("BANK001", "BAN-123456789")).thenReturn(true);
        when(pacs008Service.processPayment(any(Pacs008RequestDto.class))).thenReturn(successResponse);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "test_api_key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageId").value("MSG123456789"))
                .andExpect(jsonPath("$.transactionId").value("BAN-123456789"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.responseCode").value("00"));

        verify(pacs008Service).processPayment(any(Pacs008RequestDto.class));
        verify(statusTrackingService).trackNewPayment(any(Pacs008RequestDto.class), eq("BANK001"));
        verify(auditService).logApiCall(anyString(), anyString(), anyString(), any());
    }

    @Test
    void processPayment_InvalidClientPermission_ShouldReturn403() throws Exception {
        // Given
        when(permissionValidator.validateClientPermission("BANK001", "PACS008")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "test_api_key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isForbidden());

        verify(pacs008Service, never()).processPayment(any(Pacs008RequestDto.class));
        verify(auditService).logError(anyString(), anyString(), any(), any());
    }

    @Test
    void processPayment_InvalidTransactionId_ShouldReturn400() throws Exception {
        // Given
        validRequest.setTransactionId("INVALID-123456789");
        when(permissionValidator.validateClientPermission("BANK001", "PACS008")).thenReturn(true);
        when(transactionIdValidator.validateTransactionId("BANK001", "INVALID-123456789")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "test_api_key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isBadRequest());

        verify(pacs008Service, never()).processPayment(any(Pacs008RequestDto.class));
        verify(auditService).logError(anyString(), anyString(), any(), any());
    }

    @Test
    void processPayment_ServiceException_ShouldReturn500() throws Exception {
        // Given
        when(permissionValidator.validateClientPermission("BANK001", "PACS008")).thenReturn(true);
        when(transactionIdValidator.validateTransactionId("BANK001", "BAN-123456789")).thenReturn(true);
        when(pacs008Service.processPayment(any(Pacs008RequestDto.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "test_api_key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.responseMessage").exists());

        verify(statusTrackingService).trackNewPayment(any(Pacs008RequestDto.class), eq("BANK001"));
        verify(auditService).logError(anyString(), anyString(), any(), any());
    }

    @Test
    void processPayment_MissingRequiredFields_ShouldReturn400() throws Exception {
        // Given
        Pacs008RequestDto invalidRequest = new Pacs008RequestDto();
        invalidRequest.setMessageId("MSG123456789");
        // Missing transactionId, amount, etc.

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .header("X-API-Key", "test_api_key")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(pacs008Service, never()).processPayment(any(Pacs008RequestDto.class));
    }

    @Test
    void processPayment_MissingApiKey_ShouldReturn401() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isUnauthorized());
    }
}
