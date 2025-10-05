package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for ACMT.023 Identification Verification Request
 */
public class Acmt023RequestDto {

    @NotBlank(message = "Message ID is required")
    @JsonProperty("messageId")
    private String messageId;

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

    @JsonProperty("creatorBankName")
    private String creatorBankName;

    @JsonProperty("assignorBankName")
    private String assignorBankName;

    @JsonProperty("assignorBicfi")
    private String assignorBicfi;

    @JsonProperty("assignorMemberId")
    private String assignorMemberId;

    @JsonProperty("assigneeBicfi")
    private String assigneeBicfi;

    @JsonProperty("assigneeMemberId")
    private String assigneeMemberId;

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

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

    public String getCreatorBankName() { return creatorBankName; }
    public void setCreatorBankName(String creatorBankName) { this.creatorBankName = creatorBankName; }

    public String getAssignorBankName() { return assignorBankName; }
    public void setAssignorBankName(String assignorBankName) { this.assignorBankName = assignorBankName; }

    public String getAssignorBicfi() { return assignorBicfi; }
    public void setAssignorBicfi(String assignorBicfi) { this.assignorBicfi = assignorBicfi; }

    public String getAssignorMemberId() { return assignorMemberId; }
    public void setAssignorMemberId(String assignorMemberId) { this.assignorMemberId = assignorMemberId; }

    public String getAssigneeBicfi() { return assigneeBicfi; }
    public void setAssigneeBicfi(String assigneeBicfi) { this.assigneeBicfi = assigneeBicfi; }

    public String getAssigneeMemberId() { return assigneeMemberId; }
    public void setAssigneeMemberId(String assigneeMemberId) { this.assigneeMemberId = assigneeMemberId; }
}
