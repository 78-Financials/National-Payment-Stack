package com.payaza.nps.dto;

import com.payaza.nps.model.PaymentType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Data Transfer Object for payment requests
 */
public class PaymentRequestDto {

    @NotBlank(message = "Payment ID is required")
    private String paymentId;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Sender account is required")
    private String senderAccount;

    @NotBlank(message = "Receiver account is required")
    private String receiverAccount;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3, message = "Currency must be 3 characters")
    private String currency;

    @Size(max = 500, message = "Payment purpose cannot exceed 500 characters")
    private String paymentPurpose;

    @NotNull(message = "Payment type is required")
    private PaymentType paymentType;

    @Size(max = 35, message = "Reference number cannot exceed 35 characters")
    private String referenceNumber;

    // Constructors
    public PaymentRequestDto() {}

    public PaymentRequestDto(String paymentId, String transactionId, String senderAccount, 
                           String receiverAccount, BigDecimal amount, String currency, 
                           PaymentType paymentType) {
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.senderAccount = senderAccount;
        this.receiverAccount = receiverAccount;
        this.amount = amount;
        this.currency = currency;
        this.paymentType = paymentType;
    }

    // Getters and Setters
    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getReceiverAccount() {
        return receiverAccount;
    }

    public void setReceiverAccount(String receiverAccount) {
        this.receiverAccount = receiverAccount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getPaymentPurpose() {
        return paymentPurpose;
    }

    public void setPaymentPurpose(String paymentPurpose) {
        this.paymentPurpose = paymentPurpose;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }
}
