package com.payaza.nps.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JSON DTO for payment response API
 */
public class PaymentResponseJsonDto {

    private String messageId;
    private String endToEndId;
    private String instructionId;
    private String transactionId;
    private String status;
    private String statusCode;
    private String statusReason;
    private String additionalInformation;
    private BigDecimal amount;
    private String currency;
    private String debtorAccount;
    private String creditorAccount;
    private String debtorName;
    private String creditorName;
    private String remittanceInfo;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String npsReference;
    private String clearingSystemReference;

    // Constructors
    public PaymentResponseJsonDto() {}

    public PaymentResponseJsonDto(String messageId, String endToEndId, String status, String statusCode) {
        this.messageId = messageId;
        this.endToEndId = endToEndId;
        this.status = status;
        this.statusCode = statusCode;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getEndToEndId() {
        return endToEndId;
    }

    public void setEndToEndId(String endToEndId) {
        this.endToEndId = endToEndId;
    }

    public String getInstructionId() {
        return instructionId;
    }

    public void setInstructionId(String instructionId) {
        this.instructionId = instructionId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public void setStatusReason(String statusReason) {
        this.statusReason = statusReason;
    }

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
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

    public String getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(String debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public String getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(String creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public void setDebtorName(String debtorName) {
        this.debtorName = debtorName;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public String getRemittanceInfo() {
        return remittanceInfo;
    }

    public void setRemittanceInfo(String remittanceInfo) {
        this.remittanceInfo = remittanceInfo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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

    public String getClearingSystemReference() {
        return clearingSystemReference;
    }

    public void setClearingSystemReference(String clearingSystemReference) {
        this.clearingSystemReference = clearingSystemReference;
    }
}
