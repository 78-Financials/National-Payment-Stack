package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for PACS.002 Payment Status Report Response
 */
public class Pacs002ResponseDto {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("originalMessageId")
    private String originalMessageId;

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("responseMessage")
    private String responseMessage;

    @JsonProperty("status")
    private String status;

    @JsonProperty("paymentStatus")
    private String paymentStatus;

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

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getOriginalMessageId() { return originalMessageId; }
    public void setOriginalMessageId(String originalMessageId) { this.originalMessageId = originalMessageId; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
