package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * DTO for PACS.008 Payment Request
 */
public class Pacs008RequestDto {

    @NotBlank(message = "Message ID is required")
    @JsonProperty("messageId")
    private String messageId;

    @NotBlank(message = "Transaction ID is required")
    @JsonProperty("transactionId")
    private String transactionId;

    @NotBlank(message = "Sender Institution Code is required")
    @JsonProperty("senderInstitutionCode")
    private String senderInstitutionCode;

    @NotBlank(message = "Receiver Institution Code is required")
    @JsonProperty("receiverInstitutionCode")
    private String receiverInstitutionCode;

    @NotBlank(message = "Sender Account Number is required")
    @JsonProperty("senderAccountNumber")
    private String senderAccountNumber;

    @NotBlank(message = "Receiver Account Number is required")
    @JsonProperty("receiverAccountNumber")
    private String receiverAccountNumber;

    @NotBlank(message = "Sender Account Name is required")
    @JsonProperty("senderAccountName")
    private String senderAccountName;

    @NotBlank(message = "Receiver Account Name is required")
    @JsonProperty("receiverAccountName")
    private String receiverAccountName;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid 3-letter currency code (e.g., NGN, USD)")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "Payment Purpose is required")
    @JsonProperty("paymentPurpose")
    private String paymentPurpose;

    @JsonProperty("narration")
    private String narration;

    @NotBlank(message = "Reference Number is required")
    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("senderBankCode")
    private String senderBankCode;

    @JsonProperty("receiverBankCode")
    private String receiverBankCode;

    @JsonProperty("senderBicfi")
    private String senderBicfi;

    @JsonProperty("senderMemberId")
    private String senderMemberId;

    @JsonProperty("receiverMemberId")
    private String receiverMemberId;

    @JsonProperty("instructionId")
    private String instructionId;

    @JsonProperty("endToEndId")
    private String endToEndId;

    @JsonProperty("settlementDate")
    private String settlementDate;

    @JsonProperty("debtorBvn")
    private String debtorBvn;

    @JsonProperty("creditorBvn")
    private String creditorBvn;

    @JsonProperty("transactionLocation")
    private String transactionLocation;

    @JsonProperty("nameEnquiryMsgId")
    private String nameEnquiryMsgId;

    @JsonProperty("riskRating")
    private String riskRating;

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getSenderInstitutionCode() { return senderInstitutionCode; }
    public void setSenderInstitutionCode(String senderInstitutionCode) { this.senderInstitutionCode = senderInstitutionCode; }

    public String getReceiverInstitutionCode() { return receiverInstitutionCode; }
    public void setReceiverInstitutionCode(String receiverInstitutionCode) { this.receiverInstitutionCode = receiverInstitutionCode; }

    public String getSenderAccountNumber() { return senderAccountNumber; }
    public void setSenderAccountNumber(String senderAccountNumber) { this.senderAccountNumber = senderAccountNumber; }

    public String getReceiverAccountNumber() { return receiverAccountNumber; }
    public void setReceiverAccountNumber(String receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }

    public String getSenderAccountName() { return senderAccountName; }
    public void setSenderAccountName(String senderAccountName) { this.senderAccountName = senderAccountName; }

    public String getReceiverAccountName() { return receiverAccountName; }
    public void setReceiverAccountName(String receiverAccountName) { this.receiverAccountName = receiverAccountName; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getPaymentPurpose() { return paymentPurpose; }
    public void setPaymentPurpose(String paymentPurpose) { this.paymentPurpose = paymentPurpose; }

    public String getNarration() { return narration; }
    public void setNarration(String narration) { this.narration = narration; }

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getSenderBankCode() { return senderBankCode; }
    public void setSenderBankCode(String senderBankCode) { this.senderBankCode = senderBankCode; }

    public String getReceiverBankCode() { return receiverBankCode; }
    public void setReceiverBankCode(String receiverBankCode) { this.receiverBankCode = receiverBankCode; }

    public String getSenderBicfi() { return senderBicfi; }
    public void setSenderBicfi(String senderBicfi) { this.senderBicfi = senderBicfi; }

    public String getSenderMemberId() { return senderMemberId; }
    public void setSenderMemberId(String senderMemberId) { this.senderMemberId = senderMemberId; }

    public String getReceiverMemberId() { return receiverMemberId; }
    public void setReceiverMemberId(String receiverMemberId) { this.receiverMemberId = receiverMemberId; }

    public String getInstructionId() { return instructionId; }
    public void setInstructionId(String instructionId) { this.instructionId = instructionId; }

    public String getEndToEndId() { return endToEndId; }
    public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }

    public String getSettlementDate() { return settlementDate; }
    public void setSettlementDate(String settlementDate) { this.settlementDate = settlementDate; }

    public String getDebtorBvn() { return debtorBvn; }
    public void setDebtorBvn(String debtorBvn) { this.debtorBvn = debtorBvn; }

    public String getCreditorBvn() { return creditorBvn; }
    public void setCreditorBvn(String creditorBvn) { this.creditorBvn = creditorBvn; }

    public String getTransactionLocation() { return transactionLocation; }
    public void setTransactionLocation(String transactionLocation) { this.transactionLocation = transactionLocation; }

    public String getNameEnquiryMsgId() { return nameEnquiryMsgId; }
    public void setNameEnquiryMsgId(String nameEnquiryMsgId) { this.nameEnquiryMsgId = nameEnquiryMsgId; }

    public String getRiskRating() { return riskRating; }
    public void setRiskRating(String riskRating) { this.riskRating = riskRating; }
}
