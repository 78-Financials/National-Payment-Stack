package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO for ACMT.023 Identification Verification Response
 */
public class Acmt023ResponseDto {

    @JsonProperty("messageId")
    private String messageId;

    @JsonProperty("responseCode")
    private String responseCode;

    @JsonProperty("responseMessage")
    private String responseMessage;

    @JsonProperty("status")
    private String status;

    @JsonProperty("accountVerified")
    private Boolean accountVerified;

    @JsonProperty("accountName")
    private String accountName;

    @JsonProperty("bankCode")
    private String bankCode;

    @JsonProperty("processedAt")
    private LocalDateTime processedAt;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }

    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getAccountVerified() { return accountVerified; }
    public void setAccountVerified(Boolean accountVerified) { this.accountVerified = accountVerified; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
