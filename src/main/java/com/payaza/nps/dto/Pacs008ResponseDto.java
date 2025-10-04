package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for PACS.008 Payment Request Response
 */
public class Pacs008ResponseDto {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("transactionId")
    private String transactionId;

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("responseMessage")
    private String responseMessage;

    @JsonProperty("status")
    private String status;

    @JsonProperty("npsReference")
    private String npsReference;

    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("senderAccountNumber")
    private String senderAccountNumber;

    @JsonProperty("receiverAccountNumber")
    private String receiverAccountNumber;

    @JsonProperty("processedAt")
    private LocalDateTime processedAt;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNpsReference() { return npsReference; }
    public void setNpsReference(String npsReference) { this.npsReference = npsReference; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getSenderAccountNumber() { return senderAccountNumber; }
    public void setSenderAccountNumber(String senderAccountNumber) { this.senderAccountNumber = senderAccountNumber; }

    public String getReceiverAccountNumber() { return receiverAccountNumber; }
    public void setReceiverAccountNumber(String receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
