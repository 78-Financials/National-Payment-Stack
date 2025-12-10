package com.payaza.nps.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.dto.Pacs008ResponseDto;
import com.payaza.nps.security.ClientContext;
import com.payaza.nps.model.InternalClient;
import com.payaza.nps.service.AuditService;
import com.payaza.nps.service.PaymentStatusTrackingService;
import com.payaza.nps.service.SimplePacs008Service;
import com.payaza.nps.validation.ClientPermissionValidator;
import com.payaza.nps.validation.TransactionIdValidator;
import com.payaza.nps.repository.PaymentTransactionLiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for Pacs008Controller
 */
@ExtendWith(MockitoExtension.class)
class Pacs008ControllerTest {

    @Mock
    private SimplePacs008Service pacs008Service;

    @Mock
    private ClientPermissionValidator permissionValidator;

    @Mock
    private TransactionIdValidator transactionIdValidator;

    @Mock
    private AuditService auditService;

    @Mock
    private PaymentStatusTrackingService statusTrackingService;

    @Mock
    private PaymentTransactionLiveRepository paymentTransactionRepository;

    @InjectMocks
    private Pacs008Controller pacs008Controller;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private Pacs008RequestDto requestDto;
    private Pacs008ResponseDto responseDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(pacs008Controller).build();
        objectMapper = new ObjectMapper();

        // Setup mock client context
        InternalClient mockClient = new InternalClient();
        mockClient.setClientId("test-client");
        mockClient.setTransactionPrefix("TEST");
        ClientContext.setCurrentClient(mockClient);

        // Setup request DTO
        requestDto = new Pacs008RequestDto();
        requestDto.setMessageId("MSG123456789");
        requestDto.setTransactionId("TEST-123456789");
        requestDto.setSenderInstitutionCode("BANK001");
        requestDto.setReceiverInstitutionCode("BANK002");
        requestDto.setSenderAccountNumber("1234567890");
        requestDto.setReceiverAccountNumber("0987654321");
        requestDto.setSenderAccountName("John Doe");
        requestDto.setReceiverAccountName("Jane Smith");
        requestDto.setAmount(new BigDecimal("1000.00"));
        requestDto.setCurrency("NGN");
        requestDto.setPaymentPurpose("Payment for services");
        requestDto.setNarration("Payment for services");
        requestDto.setReferenceNumber("REF123456789");

        // Setup response DTO
        responseDto = new Pacs008ResponseDto();
        responseDto.setTransactionId("TEST-123456789");
        responseDto.setStatus("SUCCESS");
        responseDto.setMessage("Payment processed successfully");
        responseDto.setAmount(new BigDecimal("1000.00"));
        responseDto.setCurrency("NGN");
        responseDto.setResponseCode("00");
    }

    @AfterEach
    void tearDown() {
        ClientContext.clearCurrentClient();
    }

    @Test
    void processPayment_ShouldProcessPaymentSuccessfully() throws Exception {
        // Given
        when(permissionValidator.hasPermission("pacs008")).thenReturn(true);
        when(transactionIdValidator.isValidTransactionId(anyString())).thenReturn(true);
        when(paymentTransactionRepository.findByTransactionId(anyString())).thenReturn(java.util.Optional.empty());
        when(pacs008Service.processPaymentRequest(any(Pacs008RequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("TEST-123456789"))
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.amount").value(1000.00));

        verify(permissionValidator).hasPermission("pacs008");
        verify(transactionIdValidator).isValidTransactionId(anyString());
        verify(paymentTransactionRepository).findByTransactionId(anyString());
        verify(pacs008Service).processPaymentRequest(any(Pacs008RequestDto.class));
    }

    @Test
    void processPayment_InvalidPermission_ShouldReturn400() throws Exception {
        // Given
        when(permissionValidator.hasPermission("pacs008")).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(permissionValidator).hasPermission("pacs008");
        verify(transactionIdValidator, never()).isValidTransactionId(anyString());
        verify(paymentTransactionRepository, never()).findByTransactionId(anyString());
        verify(pacs008Service, never()).processPaymentRequest(any(Pacs008RequestDto.class));
    }

    @Test
    void processPayment_InvalidTransactionId_ShouldReturn400() throws Exception {
        // Given
        when(permissionValidator.hasPermission("pacs008")).thenReturn(true);
        when(transactionIdValidator.isValidTransactionId(anyString())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());

        verify(permissionValidator).hasPermission("pacs008");
        verify(transactionIdValidator).isValidTransactionId(anyString());
        verify(paymentTransactionRepository, never()).findByTransactionId(anyString());
        verify(pacs008Service, never()).processPaymentRequest(any(Pacs008RequestDto.class));
    }

    @Test
    void processPayment_ServiceException_ShouldReturn500() throws Exception {
        // Given
        when(permissionValidator.hasPermission("pacs008")).thenReturn(true);
        when(transactionIdValidator.isValidTransactionId(anyString())).thenReturn(true);
        when(paymentTransactionRepository.findByTransactionId(anyString())).thenReturn(java.util.Optional.empty());
        when(pacs008Service.processPaymentRequest(any(Pacs008RequestDto.class)))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isInternalServerError());

        verify(permissionValidator).hasPermission("pacs008");
        verify(transactionIdValidator).isValidTransactionId(anyString());
        verify(paymentTransactionRepository).findByTransactionId(anyString());
        verify(pacs008Service).processPaymentRequest(any(Pacs008RequestDto.class));
    }

    @Test
    void processPayment_InvalidRequest_ShouldReturn400() throws Exception {
        // Given - Create invalid request (missing required fields)
        Pacs008RequestDto invalidRequest = new Pacs008RequestDto();
        invalidRequest.setTransactionId(""); // Empty transaction ID

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(permissionValidator, never()).hasPermission(anyString());
        verify(transactionIdValidator, never()).isValidTransactionId(anyString());
        verify(pacs008Service, never()).processPaymentRequest(any(Pacs008RequestDto.class));
    }

    @Test
    void processPayment_WithClientContext_ShouldUseClientInfo() throws Exception {
        // Given
        when(permissionValidator.hasPermission("pacs008")).thenReturn(true);
        when(transactionIdValidator.isValidTransactionId(anyString())).thenReturn(true);
        when(paymentTransactionRepository.findByTransactionId(anyString())).thenReturn(java.util.Optional.empty());
        when(pacs008Service.processPaymentRequest(any(Pacs008RequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/v1/payments/transfer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(permissionValidator).hasPermission("pacs008");
        verify(transactionIdValidator).isValidTransactionId(anyString());
        verify(paymentTransactionRepository).findByTransactionId(anyString());
        verify(pacs008Service).processPaymentRequest(any(Pacs008RequestDto.class));
    }
}