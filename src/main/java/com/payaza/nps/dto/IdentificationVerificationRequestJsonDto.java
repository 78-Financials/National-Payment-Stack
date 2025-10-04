package com.payaza.nps.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * JSON DTO for identification verification request API
 */
public class IdentificationVerificationRequestJsonDto {

    @NotBlank(message = "Account ID is required")
    private String accountId;

    private String accountOwnerName;
    private String beneficiaryBankId;
    private String originalTransactionRef;
    private String additionalInformation;

    // Constructors
    public IdentificationVerificationRequestJsonDto() {}

    public IdentificationVerificationRequestJsonDto(String accountId, String accountOwnerName, String beneficiaryBankId) {
        this.accountId = accountId;
        this.accountOwnerName = accountOwnerName;
        this.beneficiaryBankId = beneficiaryBankId;
    }

    // Getters and Setters
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAccountOwnerName() {
        return accountOwnerName;
    }

    public void setAccountOwnerName(String accountOwnerName) {
        this.accountOwnerName = accountOwnerName;
    }

    public String getBeneficiaryBankId() {
        return beneficiaryBankId;
    }

    public void setBeneficiaryBankId(String beneficiaryBankId) {
        this.beneficiaryBankId = beneficiaryBankId;
    }

    public String getOriginalTransactionRef() {
        return originalTransactionRef;
    }

    public void setOriginalTransactionRef(String originalTransactionRef) {
        this.originalTransactionRef = originalTransactionRef;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
    }
}
