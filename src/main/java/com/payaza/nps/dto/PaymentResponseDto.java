package com.payaza.nps.dto;

import com.payaza.nps.model.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for payment responses
 */
public class PaymentResponseDto {

    private String paymentId;
    private String transactionId;
    private String npsReference;
    private PaymentStatus status;
    private String responseCode;
    private String responseMessage;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;

    // Constructors
    public PaymentResponseDto() {}

    public PaymentResponseDto(String paymentId, String transactionId, PaymentStatus status, 
                            String responseCode, String responseMessage) {
        this.paymentId = paymentId;
        this.transactionId = transactionId;
        this.status = status;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.createdAt = LocalDateTime.now();
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

    public String getNpsReference() {
        return npsReference;
    }

    public void setNpsReference(String npsReference) {
        this.npsReference = npsReference;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
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

    public LocalDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(LocalDateTime processedAt) {
        this.processedAt = processedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
