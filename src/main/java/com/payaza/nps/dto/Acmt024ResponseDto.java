package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

/**
 * DTO for ACMT.024 Identification Verification Report Response
 */
public class Acmt024ResponseDto {

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

    @JsonProperty("verificationStatus")
    private String verificationStatus;

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

    @JsonProperty("verificationResult")
    private Boolean verificationResult;

    @JsonProperty("accountNumber")
    private String accountNumber;

    // Supplementary Data Fields
    @JsonProperty("bvn")
    private String bvn;

    @JsonProperty("riskRating")
    private String riskRating;

    @JsonProperty("accountDesignation")
    private String accountDesignation;

    @JsonProperty("accountTier")
    private String accountTier;

    @JsonProperty("idType")
    private String idType;

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

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

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

    public Boolean getVerificationResult() { return verificationResult; }
    public void setVerificationResult(Boolean verificationResult) { this.verificationResult = verificationResult; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    // Supplementary Data Getters and Setters
    public String getBvn() { return bvn; }
    public void setBvn(String bvn) { this.bvn = bvn; }

    public String getRiskRating() { return riskRating; }
    public void setRiskRating(String riskRating) { this.riskRating = riskRating; }

    public String getAccountDesignation() { return accountDesignation; }
    public void setAccountDesignation(String accountDesignation) { this.accountDesignation = accountDesignation; }

    public String getAccountTier() { return accountTier; }
    public void setAccountTier(String accountTier) { this.accountTier = accountTier; }

    public String getIdType() { return idType; }
    public void setIdType(String idType) { this.idType = idType; }
}
