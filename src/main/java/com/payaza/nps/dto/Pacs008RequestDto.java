package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
    @JsonProperty("amount")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @JsonProperty("currency")
    private String currency;

    @NotBlank(message = "Payment Purpose is required")
    @JsonProperty("paymentPurpose")
    private String paymentPurpose;

    @NotBlank(message = "Reference Number is required")
    @JsonProperty("referenceNumber")
    private String referenceNumber;

    @JsonProperty("senderBankCode")
    private String senderBankCode;

    @JsonProperty("receiverBankCode")
    private String receiverBankCode;

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

    public String getReferenceNumber() { return referenceNumber; }
    public void setReferenceNumber(String referenceNumber) { this.referenceNumber = referenceNumber; }

    public String getSenderBankCode() { return senderBankCode; }
    public void setSenderBankCode(String senderBankCode) { this.senderBankCode = senderBankCode; }

    public String getReceiverBankCode() { return receiverBankCode; }
    public void setReceiverBankCode(String receiverBankCode) { this.receiverBankCode = receiverBankCode; }
}
