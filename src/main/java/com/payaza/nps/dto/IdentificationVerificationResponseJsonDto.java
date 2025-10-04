package com.payaza.nps.dto;

import java.time.LocalDateTime;

/**
 * JSON DTO for identification verification response API
 */
public class IdentificationVerificationResponseJsonDto {

    private String messageId;
    private String accountId;
    private String verificationStatus;
    private String verificationReason;
    private String additionalInformation;
    private LocalDateTime processedAt;
    private String npsReference;

    // Constructors
    public IdentificationVerificationResponseJsonDto() {}

    public IdentificationVerificationResponseJsonDto(String messageId, String accountId, String verificationStatus) {
        this.messageId = messageId;
        this.accountId = accountId;
        this.verificationStatus = verificationStatus;
        this.processedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public String getVerificationReason() {
        return verificationReason;
    }

    public void setVerificationReason(String verificationReason) {
        this.verificationReason = verificationReason;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public String getNpsReference() {
        return npsReference;
    }

    public void setNpsReference(String npsReference) {
        this.npsReference = npsReference;
    }
}
