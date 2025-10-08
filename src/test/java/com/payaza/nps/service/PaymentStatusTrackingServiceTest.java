package com.payaza.nps.service;

import com.payaza.nps.dto.Pacs002ResponseDto;
import com.payaza.nps.dto.Pacs008RequestDto;
import com.payaza.nps.model.PaymentTransactionLive;
import com.payaza.nps.repository.PaymentTransactionLiveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentStatusTrackingService
 */
@ExtendWith(MockitoExtension.class)
class PaymentStatusTrackingServiceTest {

    @Mock
    private PaymentTransactionLiveRepository liveRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private PaymentStatusTrackingService trackingService;

    private Pacs008RequestDto testRequest;
    private Pacs002ResponseDto testResponse;
    private PaymentTransactionLive testTransaction;

    @BeforeEach
    void setUp() {
        // Setup test request
        testRequest = new Pacs008RequestDto();
        testRequest.setMessageId("MSG123456789");
        testRequest.setTransactionId("BAN-123456789");
        testRequest.setSenderInstitutionCode("001");
        testRequest.setReceiverInstitutionCode("002");
        testRequest.setSenderAccountNumber("1234567890");
        testRequest.setReceiverAccountNumber("0987654321");
        testRequest.setAmount(new BigDecimal("1000.00"));
        testRequest.setCurrency("NGN");
        testRequest.setNarration("Test payment");

        // Setup test response
        testResponse = new Pacs002ResponseDto();
        testResponse.setMessageId("MSG123456789");
        testResponse.setTransactionId("BAN-123456789");
        testResponse.setStatus("SUCCESS");
        testResponse.setResponseCode("00");
        testResponse.setResponseMessage("Payment processed successfully");

        // Setup test transaction
        testTransaction = new PaymentTransactionLive();
        testTransaction.setId(1L);
        testTransaction.setTransactionId("BAN-123456789");
        testTransaction.setOriginalMessageId("MSG123456789");
        testTransaction.setClientId("BANK001");
        testTransaction.setAmount(new BigDecimal("1000.00"));
        testTransaction.setCurrency("NGN");
        testTransaction.setDebtorBank("001");
        testTransaction.setCreditorBank("002");
        testTransaction.setDebtorAccount("1234567890");
        testTransaction.setCreditorAccount("0987654321");
        testTransaction.setStatus("PENDING");
        testTransaction.setRequestCreatedAt(LocalDateTime.now());
    }

    @Test
    void trackNewPayment_ShouldCreateTransactionRecord() {
        // Given
        when(liveRepository.save(any(PaymentTransactionLive.class))).thenReturn(testTransaction);

        // When
        trackingService.trackNewPayment(testRequest, "BANK001");

        // Then
        verify(liveRepository).save(any(PaymentTransactionLive.class));
        verify(auditService).logClientAction(anyString(), anyString(), anyString(), any(), anyString(), any());
    }

    @Test
    void updatePaymentStatus_WithExistingTransaction_ShouldUpdateStatus() {
        // Given
        when(liveRepository.findByOriginalMessageId("MSG123456789")).thenReturn(Optional.of(testTransaction));
        when(liveRepository.save(any(PaymentTransactionLive.class))).thenReturn(testTransaction);

        // When
        trackingService.updatePaymentStatus("MSG123456789", testResponse);

        // Then
        verify(liveRepository).save(testTransaction);
        verify(auditService).logSystemEvent(anyString(), anyString(), anyString(), any());
    }

    @Test
    void updatePaymentStatus_WithNonExistingTransaction_ShouldLogWarning() {
        // Given
        when(liveRepository.findByOriginalMessageId("MSG123456789")).thenReturn(Optional.empty());

        // When
        trackingService.updatePaymentStatus("MSG123456789", testResponse);

        // Then
        verify(liveRepository, never()).save(any(PaymentTransactionLive.class));
        verify(auditService).logSystemEvent(anyString(), anyString(), anyString(), any());
    }

    @Test
    void updatePaymentStatus_WithFailureResponse_ShouldUpdateStatusToFailed() {
        // Given
        testResponse.setStatus("FAILED");
        testResponse.setResponseCode("96");
        testResponse.setResponseMessage("Transaction failed");

        when(liveRepository.findByOriginalMessageId("MSG123456789")).thenReturn(Optional.of(testTransaction));
        when(liveRepository.save(any(PaymentTransactionLive.class))).thenReturn(testTransaction);

        // When
        trackingService.updatePaymentStatus("MSG123456789", testResponse);

        // Then
        verify(liveRepository).save(testTransaction);
        verify(auditService).logSystemEvent(anyString(), anyString(), anyString(), any());
    }

    @Test
    void updatePaymentStatus_WithTimeoutResponse_ShouldUpdateStatusToTimeout() {
        // Given
        testResponse.setStatus("TIMEOUT");
        testResponse.setResponseCode("68");
        testResponse.setResponseMessage("Transaction timeout");

        when(liveRepository.findByOriginalMessageId("MSG123456789")).thenReturn(Optional.of(testTransaction));
        when(liveRepository.save(any(PaymentTransactionLive.class))).thenReturn(testTransaction);

        // When
        trackingService.updatePaymentStatus("MSG123456789", testResponse);

        // Then
        verify(liveRepository).save(testTransaction);
        verify(auditService).logSystemEvent(anyString(), anyString(), anyString(), any());
    }

    @Test
    void checkForTimeouts_ShouldMarkOldPendingTransactionsAsTimeout() {
        // Given
        PaymentTransactionLive oldTransaction = new PaymentTransactionLive();
        oldTransaction.setId(2L);
        oldTransaction.setTransactionId("BAN-OLD123456");
        oldTransaction.setStatus("PENDING");
        oldTransaction.setRequestCreatedAt(LocalDateTime.now().minusMinutes(35));

        when(liveRepository.findByStatusAndRequestCreatedAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(java.util.Arrays.asList(oldTransaction));
        when(liveRepository.save(any(PaymentTransactionLive.class))).thenReturn(oldTransaction);

        // When
        trackingService.checkForTimeoutTransactions();

        // Then
        verify(liveRepository).save(oldTransaction);
        verify(auditService).logSystemEvent(anyString(), anyString(), anyString(), any());
    }

    @Test
    void checkForTimeouts_WithNoOldTransactions_ShouldDoNothing() {
        // Given
        when(liveRepository.findByStatusAndRequestCreatedAtBefore(eq("PENDING"), any(LocalDateTime.class)))
                .thenReturn(java.util.Collections.emptyList());

        // When
        trackingService.checkForTimeoutTransactions();

        // Then
        verify(liveRepository, never()).save(any(PaymentTransactionLive.class));
    }

    @Test
    void getTransactionStatus_WithExistingTransaction_ShouldReturnStatus() {
        // Given
        when(liveRepository.findByTransactionId("BAN-123456789")).thenReturn(Optional.of(testTransaction));

        // When
        String status = trackingService.getTransactionStatus("BAN-123456789");

        // Then
        assertEquals("PENDING", status);
    }

    @Test
    void getTransactionStatus_WithNonExistingTransaction_ShouldReturnNull() {
        // Given
        when(liveRepository.findByTransactionId("NON_EXISTENT")).thenReturn(Optional.empty());

        // When
        String status = trackingService.getTransactionStatus("NON_EXISTENT");

        // Then
        assertEquals("NOT_FOUND", status);
    }

    @Test
    void trackNewPayment_WithNullRequest_ShouldHandleGracefully() {
        // When
        trackingService.trackNewPayment(null, "BANK001");

        // Then
        verify(liveRepository, never()).save(any(PaymentTransactionLive.class));
        verify(auditService).logError(anyString(), anyString(), any(), isNull(), eq("BANK001"), anyString(), anyString(), anyString(), any());
    }

    @Test
    void updatePaymentStatus_WithNullResponse_ShouldHandleGracefully() {
        // When
        trackingService.updatePaymentStatus("MSG123456789", null);

        // Then
        verify(liveRepository, never()).save(any(PaymentTransactionLive.class));
        verify(auditService).logError(anyString(), anyString(), any(), isNull(), isNull(), anyString(), anyString(), anyString(), any());
    }

    @Test
    void updatePaymentStatus_WithRepositoryException_ShouldLogError() {
        // Given
        when(liveRepository.findByOriginalMessageId("MSG123456789")).thenReturn(Optional.of(testTransaction));
        when(liveRepository.save(any(PaymentTransactionLive.class))).thenThrow(new RuntimeException("Database error"));

        // When
        trackingService.updatePaymentStatus("MSG123456789", testResponse);

        // Then
        verify(auditService).logError(anyString(), anyString(), any(), isNull(), isNull(), anyString(), anyString(), anyString(), any());
    }
}
