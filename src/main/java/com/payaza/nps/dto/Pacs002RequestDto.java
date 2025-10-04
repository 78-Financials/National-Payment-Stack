package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for PACS.002 Payment Status Report
 */
public class Pacs002RequestDto {

    @NotBlank(message = "Message ID is required")
    @JsonProperty("messageId")
    private String messageId;

    @NotBlank(message = "Original Message ID is required")
    @JsonProperty("originalMessageId")
    private String originalMessageId;

    @NotBlank(message = "Institution Code is required")
    @JsonProperty("institutionCode")
    private String institutionCode;

    @NotBlank(message = "Original Transaction ID is required")
    @JsonProperty("originalTransactionId")
    private String originalTransactionId;

    @NotBlank(message = "Status is required")
    @JsonProperty("status")
    private String status; // ACSC, RJCT, PDNG, etc.

    @JsonProperty("statusReason")
    private String statusReason;

    @JsonProperty("statusReasonCode")
    private String statusReasonCode;

    @JsonProperty("additionalInformation")
    private String additionalInformation;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("processedAt")
    private LocalDateTime processedAt;

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getOriginalMessageId() { return originalMessageId; }
    public void setOriginalMessageId(String originalMessageId) { this.originalMessageId = originalMessageId; }

    public String getInstitutionCode() { return institutionCode; }
    public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }

    public String getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(String originalTransactionId) { this.originalTransactionId = originalTransactionId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getStatusReason() { return statusReason; }
    public void setStatusReason(String statusReason) { this.statusReason = statusReason; }

    public String getStatusReasonCode() { return statusReasonCode; }
    public void setStatusReasonCode(String statusReasonCode) { this.statusReasonCode = statusReasonCode; }

    public String getAdditionalInformation() { return additionalInformation; }
    public void setAdditionalInformation(String additionalInformation) { this.additionalInformation = additionalInformation; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
}
