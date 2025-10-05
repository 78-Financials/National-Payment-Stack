package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for ACMT.024 Identification Verification Report
 */
public class Acmt024RequestDto {

    @NotBlank(message = "Message ID is required")
    @JsonProperty("messageId")
    private String messageId;

    @NotBlank(message = "Original Message ID is required")
    @JsonProperty("originalMessageId")
    private String originalMessageId;

    @NotBlank(message = "Institution Code is required")
    @JsonProperty("institutionCode")
    private String institutionCode;

    @NotBlank(message = "Account Number is required")
    @JsonProperty("accountNumber")
    private String accountNumber;

    @NotBlank(message = "Bank Code is required")
    @JsonProperty("bankCode")
    private String bankCode;

    @NotBlank(message = "Account Name is required")
    @JsonProperty("accountName")
    private String accountName;

    @NotNull(message = "Amount is required")
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "Reference Number is required")
    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @NotBlank(message = "Verification Status is required")
    @JsonProperty("verificationStatus")
    private String verificationStatus; // SUCCESS, FAILED

    @JsonProperty("errorCode")
    private String errorCode;

    @JsonProperty("errorMessage")
    private String errorMessage;

    @JsonProperty("assignorBicfi")
    private String assignorBicfi;

    @JsonProperty("assignorMemberId")
    private String assignorMemberId;

    @JsonProperty("assigneeBankName")
    private String assigneeBankName;

    @JsonProperty("assigneeBicfi")
    private String assigneeBicfi;

    @JsonProperty("assigneeMemberId")
    private String assigneeMemberId;

    @JsonProperty("originalCreationDateTime")
    private LocalDateTime originalCreationDateTime;

    @JsonProperty("verificationResult")
    private Boolean verificationResult;

    @JsonProperty("bvn")
    private String bvn;

    @JsonProperty("riskRating")
    private String riskRating;

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getOriginalMessageId() { return originalMessageId; }
    public void setOriginalMessageId(String originalMessageId) { this.originalMessageId = originalMessageId; }

    public String getInstitutionCode() { return institutionCode; }
    public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public String getBankCode() { return bankCode; }
    public void setBankCode(String bankCode) { this.bankCode = bankCode; }

    public String getAccountName() { return accountName; }
    public void setAccountName(String accountName) { this.accountName = accountName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getVerificationStatus() { return verificationStatus; }
    public void setVerificationStatus(String verificationStatus) { this.verificationStatus = verificationStatus; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getAssignorBicfi() { return assignorBicfi; }
    public void setAssignorBicfi(String assignorBicfi) { this.assignorBicfi = assignorBicfi; }

    public String getAssignorMemberId() { return assignorMemberId; }
    public void setAssignorMemberId(String assignorMemberId) { this.assignorMemberId = assignorMemberId; }

    public String getAssigneeBankName() { return assigneeBankName; }
    public void setAssigneeBankName(String assigneeBankName) { this.assigneeBankName = assigneeBankName; }

    public String getAssigneeBicfi() { return assigneeBicfi; }
    public void setAssigneeBicfi(String assigneeBicfi) { this.assigneeBicfi = assigneeBicfi; }

    public String getAssigneeMemberId() { return assigneeMemberId; }
    public void setAssigneeMemberId(String assigneeMemberId) { this.assigneeMemberId = assigneeMemberId; }

    public LocalDateTime getOriginalCreationDateTime() { return originalCreationDateTime; }
    public void setOriginalCreationDateTime(LocalDateTime originalCreationDateTime) { this.originalCreationDateTime = originalCreationDateTime; }

    public Boolean getVerificationResult() { return verificationResult; }
    public void setVerificationResult(Boolean verificationResult) { this.verificationResult = verificationResult; }

    public String getBvn() { return bvn; }
    public void setBvn(String bvn) { this.bvn = bvn; }

    public String getRiskRating() { return riskRating; }
    public void setRiskRating(String riskRating) { this.riskRating = riskRating; }
}
