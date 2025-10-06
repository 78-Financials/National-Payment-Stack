package com.payaza.nps.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for tracking live payment transactions (current day only)
 * This table is optimized for real-time queries and dashboard performance
 */
@Entity
@Table(name = "payment_transactions_live", indexes = {
    @Index(name = "idx_live_original_message_id", columnList = "originalMessageId"),
    @Index(name = "idx_live_transaction_id", columnList = "transactionId"),
    @Index(name = "idx_live_client_id", columnList = "clientId"),
    @Index(name = "idx_live_status", columnList = "status"),
    @Index(name = "idx_live_created_at", columnList = "requestCreatedAt"),
    @Index(name = "idx_live_client_status", columnList = "clientId, status"),
    @Index(name = "idx_live_bank_status", columnList = "creditorBank, status")
})
public class PaymentTransactionLive {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    @Column(name = "transaction_id", unique = true, nullable = false, length = 100)
    private String transactionId;
    
    @NotBlank(message = "Original message ID is required")
    @Size(max = 100, message = "Original message ID must not exceed 100 characters")
    @Column(name = "original_message_id", nullable = false, length = 100)
    private String originalMessageId;
    
    @NotBlank(message = "Client ID is required")
    @Size(max = 50, message = "Client ID must not exceed 50 characters")
    @Column(name = "client_id", nullable = false, length = 50)
    private String clientId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @NotBlank(message = "Currency is required")
    @Size(max = 3, message = "Currency must not exceed 3 characters")
    @Column(name = "currency", nullable = false, length = 3)
    private String currency;
    
    @Size(max = 50, message = "Debtor bank must not exceed 50 characters")
    @Column(name = "debtor_bank", length = 50)
    private String debtorBank;
    
    @Size(max = 50, message = "Creditor bank must not exceed 50 characters")
    @Column(name = "creditor_bank", length = 50)
    private String creditorBank;
    
    @Size(max = 50, message = "Debtor account must not exceed 50 characters")
    @Column(name = "debtor_account", length = 50)
    private String debtorAccount;
    
    @Size(max = 50, message = "Creditor account must not exceed 50 characters")
    @Column(name = "creditor_account", length = 50)
    private String creditorAccount;
    
    @NotBlank(message = "Status is required")
    @Size(max = 20, message = "Status must not exceed 20 characters")
    @Column(name = "status", nullable = false, length = 20)
    private String status = "PENDING"; // PENDING, SUCCESS, FAILED, TIMEOUT
    
    @Size(max = 10, message = "Response code must not exceed 10 characters")
    @Column(name = "response_code", length = 10)
    private String responseCode;
    
    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @NotNull(message = "Request created at is required")
    @CreationTimestamp
    @Column(name = "request_created_at", nullable = false, updatable = false)
    private LocalDateTime requestCreatedAt;
    
    @Column(name = "response_received_at")
    private LocalDateTime responseReceivedAt;
    
    @UpdateTimestamp
    @Column(name = "last_updated_at", nullable = false)
    private LocalDateTime lastUpdatedAt;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Size(max = 50, message = "Error category must not exceed 50 characters")
    @Column(name = "error_category", length = 50)
    private String errorCategory;
    
    @Size(max = 1000, message = "Error details must not exceed 1000 characters")
    @Column(name = "error_details", length = 1000)
    private String errorDetails;
    
    // New fields for inbound PACS.008 support
    @Enumerated(EnumType.STRING)
    @Column(name = "direction", length = 20)
    private TransactionDirection direction = TransactionDirection.OUTBOUND;
    
    @Column(name = "queue_status", length = 50)
    private String queueStatus; // QUEUED, PROCESSING, COMPLETED, ERROR
    
    @Column(name = "queued_at")
    private LocalDateTime queuedAt;
    
    @Column(name = "processed_at")
    private LocalDateTime processedAt;
    
    @Column(name = "response_sent_at")
    private LocalDateTime responseSentAt;
    
    @Column(name = "response_message_id", length = 100)
    private String responseMessageId;
    
    @Column(name = "received_at")
    private LocalDateTime receivedAt;
    
    @Column(name = "sender_bank_code", length = 50)
    private String senderBankCode;
    
    @Column(name = "receiver_bank_code", length = 50)
    private String receiverBankCode;
    
    @Column(name = "sender_account_number", length = 50)
    private String senderAccountNumber;
    
    @Column(name = "receiver_account_number", length = 50)
    private String receiverAccountNumber;
    
    // Constructors
    public PaymentTransactionLive() {}
    
    public PaymentTransactionLive(String transactionId, String originalMessageId, String clientId, 
                                BigDecimal amount, String currency) {
        this.transactionId = transactionId;
        this.originalMessageId = originalMessageId;
        this.clientId = clientId;
        this.amount = amount;
        this.currency = currency;
        this.status = "PENDING";
        this.requestCreatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getOriginalMessageId() { return originalMessageId; }
    public void setOriginalMessageId(String originalMessageId) { this.originalMessageId = originalMessageId; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getDebtorBank() { return debtorBank; }
    public void setDebtorBank(String debtorBank) { this.debtorBank = debtorBank; }

    public String getCreditorBank() { return creditorBank; }
    public void setCreditorBank(String creditorBank) { this.creditorBank = creditorBank; }

    public String getDebtorAccount() { return debtorAccount; }
    public void setDebtorAccount(String debtorAccount) { this.debtorAccount = debtorAccount; }

    public String getCreditorAccount() { return creditorAccount; }
    public void setCreditorAccount(String creditorAccount) { this.creditorAccount = creditorAccount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }

    public Long getProcessingTimeMs() { return processingTimeMs; }
    public void setProcessingTimeMs(Long processingTimeMs) { this.processingTimeMs = processingTimeMs; }

    public LocalDateTime getRequestCreatedAt() { return requestCreatedAt; }
    public void setRequestCreatedAt(LocalDateTime requestCreatedAt) { this.requestCreatedAt = requestCreatedAt; }

    public LocalDateTime getResponseReceivedAt() { return responseReceivedAt; }
    public void setResponseReceivedAt(LocalDateTime responseReceivedAt) { this.responseReceivedAt = responseReceivedAt; }

    public LocalDateTime getLastUpdatedAt() { return lastUpdatedAt; }
    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) { this.lastUpdatedAt = lastUpdatedAt; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getErrorCategory() { return errorCategory; }
    public void setErrorCategory(String errorCategory) { this.errorCategory = errorCategory; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }

    // New field getters and setters
    public TransactionDirection getDirection() { return direction; }
    public void setDirection(TransactionDirection direction) { this.direction = direction; }

    public String getQueueStatus() { return queueStatus; }
    public void setQueueStatus(String queueStatus) { this.queueStatus = queueStatus; }

    public LocalDateTime getQueuedAt() { return queuedAt; }
    public void setQueuedAt(LocalDateTime queuedAt) { this.queuedAt = queuedAt; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getResponseSentAt() { return responseSentAt; }
    public void setResponseSentAt(LocalDateTime responseSentAt) { this.responseSentAt = responseSentAt; }

    public String getResponseMessageId() { return responseMessageId; }
    public void setResponseMessageId(String responseMessageId) { this.responseMessageId = responseMessageId; }

    public LocalDateTime getReceivedAt() { return receivedAt; }
    public void setReceivedAt(LocalDateTime receivedAt) { this.receivedAt = receivedAt; }

    public String getSenderBankCode() { return senderBankCode; }
    public void setSenderBankCode(String senderBankCode) { this.senderBankCode = senderBankCode; }

    public String getReceiverBankCode() { return receiverBankCode; }
    public void setReceiverBankCode(String receiverBankCode) { this.receiverBankCode = receiverBankCode; }

    public String getSenderAccountNumber() { return senderAccountNumber; }
    public void setSenderAccountNumber(String senderAccountNumber) { this.senderAccountNumber = senderAccountNumber; }

    public String getReceiverAccountNumber() { return receiverAccountNumber; }
    public void setReceiverAccountNumber(String receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }

    // Helper methods
    public boolean isCompleted() {
        return "SUCCESS".equals(status) || "FAILED".equals(status) || "TIMEOUT".equals(status);
    }

    public boolean isPending() {
        return "PENDING".equals(status);
    }

    public void markAsSuccess(String responseCode, String responseMessage) {
        this.status = "SUCCESS";
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.responseReceivedAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        if (this.requestCreatedAt != null && this.responseReceivedAt != null) {
            this.processingTimeMs = java.time.Duration.between(this.requestCreatedAt, this.responseReceivedAt).toMillis();
        }
    }

    public void markAsFailed(String responseCode, String responseMessage, String errorCategory) {
        this.status = "FAILED";
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.errorCategory = errorCategory;
        this.responseReceivedAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        if (this.requestCreatedAt != null && this.responseReceivedAt != null) {
            this.processingTimeMs = java.time.Duration.between(this.requestCreatedAt, this.responseReceivedAt).toMillis();
        }
    }

    public void markAsTimeout() {
        this.status = "TIMEOUT";
        this.responseReceivedAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
        if (this.requestCreatedAt != null && this.responseReceivedAt != null) {
            this.processingTimeMs = java.time.Duration.between(this.requestCreatedAt, this.responseReceivedAt).toMillis();
        }
    }

    @Override
    public String toString() {
        return "PaymentTransactionLive{" +
               "id=" + id +
               ", transactionId='" + transactionId + '\'' +
               ", originalMessageId='" + originalMessageId + '\'' +
               ", clientId='" + clientId + '\'' +
               ", amount=" + amount +
               ", currency='" + currency + '\'' +
               ", status='" + status + '\'' +
               ", responseCode='" + responseCode + '\'' +
               ", processingTimeMs=" + processingTimeMs +
               '}';
    }
}
