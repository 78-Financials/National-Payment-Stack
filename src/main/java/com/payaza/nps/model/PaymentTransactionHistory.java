package com.payaza.nps.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for archived payment transactions (historical data)
 * This table is optimized for analytics and reporting queries
 */
@Entity
@Table(name = "payment_transactions_history", indexes = {
    @Index(name = "idx_history_original_message_id", columnList = "originalMessageId"),
    @Index(name = "idx_history_transaction_id", columnList = "transactionId"),
    @Index(name = "idx_history_client_id", columnList = "clientId"),
    @Index(name = "idx_history_final_status", columnList = "finalStatus"),
    @Index(name = "idx_history_created_at", columnList = "requestCreatedAt"),
    @Index(name = "idx_history_archived_at", columnList = "archivedAt"),
    @Index(name = "idx_history_bank_date", columnList = "creditorBank, requestCreatedAt"),
    @Index(name = "idx_history_client_date", columnList = "clientId, requestCreatedAt")
})
public class PaymentTransactionHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Transaction ID is required")
    @Size(max = 100, message = "Transaction ID must not exceed 100 characters")
    @Column(name = "transaction_id", nullable = false, length = 100)
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
    
    @NotBlank(message = "Final status is required")
    @Size(max = 20, message = "Final status must not exceed 20 characters")
    @Column(name = "final_status", nullable = false, length = 20)
    private String finalStatus; // SUCCESS, FAILED, TIMEOUT
    
    @Size(max = 10, message = "Response code must not exceed 10 characters")
    @Column(name = "response_code", length = 10)
    private String responseCode;
    
    @Column(name = "response_message", columnDefinition = "TEXT")
    private String responseMessage;
    
    @Column(name = "processing_time_ms")
    private Long processingTimeMs;
    
    @NotNull(message = "Request created at is required")
    @Column(name = "request_created_at", nullable = false)
    private LocalDateTime requestCreatedAt;
    
    @Column(name = "response_received_at")
    private LocalDateTime responseReceivedAt;
    
    @NotNull(message = "Completed at is required")
    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;
    
    @CreationTimestamp
    @Column(name = "archived_at", nullable = false, updatable = false)
    private LocalDateTime archivedAt;
    
    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;
    
    @Size(max = 50, message = "Error category must not exceed 50 characters")
    @Column(name = "error_category", length = 50)
    private String errorCategory;
    
    @Size(max = 1000, message = "Error details must not exceed 1000 characters")
    @Column(name = "error_details", length = 1000)
    private String errorDetails;
    
    // Constructors
    public PaymentTransactionHistory() {}
    
    public PaymentTransactionHistory(PaymentTransactionLive liveTransaction) {
        this.transactionId = liveTransaction.getTransactionId();
        this.originalMessageId = liveTransaction.getOriginalMessageId();
        this.clientId = liveTransaction.getClientId();
        this.amount = liveTransaction.getAmount();
        this.currency = liveTransaction.getCurrency();
        this.debtorBank = liveTransaction.getDebtorBank();
        this.creditorBank = liveTransaction.getCreditorBank();
        this.debtorAccount = liveTransaction.getDebtorAccount();
        this.creditorAccount = liveTransaction.getCreditorAccount();
        this.finalStatus = liveTransaction.getStatus();
        this.responseCode = liveTransaction.getResponseCode();
        this.responseMessage = liveTransaction.getResponseMessage();
        this.processingTimeMs = liveTransaction.getProcessingTimeMs();
        this.requestCreatedAt = liveTransaction.getRequestCreatedAt();
        this.responseReceivedAt = liveTransaction.getResponseReceivedAt();
        this.retryCount = liveTransaction.getRetryCount();
        this.errorCategory = liveTransaction.getErrorCategory();
        this.errorDetails = liveTransaction.getErrorDetails();
        this.completedAt = LocalDateTime.now();
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

    public String getFinalStatus() { return finalStatus; }
    public void setFinalStatus(String finalStatus) { this.finalStatus = finalStatus; }

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

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public LocalDateTime getArchivedAt() { return archivedAt; }
    public void setArchivedAt(LocalDateTime archivedAt) { this.archivedAt = archivedAt; }

    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    public String getErrorCategory() { return errorCategory; }
    public void setErrorCategory(String errorCategory) { this.errorCategory = errorCategory; }

    public String getErrorDetails() { return errorDetails; }
    public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }

    // Helper methods
    public boolean isSuccessful() {
        return "SUCCESS".equals(finalStatus);
    }

    public boolean isFailed() {
        return "FAILED".equals(finalStatus);
    }

    public boolean isTimeout() {
        return "TIMEOUT".equals(finalStatus);
    }

    @Override
    public String toString() {
        return "PaymentTransactionHistory{" +
               "id=" + id +
               ", transactionId='" + transactionId + '\'' +
               ", originalMessageId='" + originalMessageId + '\'' +
               ", clientId='" + clientId + '\'' +
               ", amount=" + amount +
               ", currency='" + currency + '\'' +
               ", finalStatus='" + finalStatus + '\'' +
               ", responseCode='" + responseCode + '\'' +
               ", processingTimeMs=" + processingTimeMs +
               ", completedAt=" + completedAt +
               '}';
    }
}
