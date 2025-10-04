package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO for PACS.028 Payment Status Request Response
 */
public class Pacs028ResponseDto {

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

    @JsonProperty("statusRequestId")
    private String statusRequestId;

    @JsonProperty("originalTransactionId")
    private String originalTransactionId;

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

    public String getStatusRequestId() { return statusRequestId; }
    public void setStatusRequestId(String statusRequestId) { this.statusRequestId = statusRequestId; }

    public String getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(String originalTransactionId) { this.originalTransactionId = originalTransactionId; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
