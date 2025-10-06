package com.payaza.nps.dto;

import java.time.LocalDateTime;

/**
 * DTO for inbound PACS.008 request from NIBSS
 */
public class InboundPacs008Request {
    
    private String transactionId;
    private String messageId;
    private String xmlMessage;
    private String senderBankCode;
    private String receiverBankCode;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private Double amount;
    private String currency;
    private String narration;
    private LocalDateTime receivedAt;
    private String originalTransactionId;
    private String originalMessageId;

    // Constructors
    public InboundPacs008Request() {}

    public InboundPacs008Request(String transactionId, String messageId, String xmlMessage) {
        this.transactionId = transactionId;
        this.messageId = messageId;
        this.xmlMessage = xmlMessage;
        this.receivedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getXmlMessage() {
        return xmlMessage;
    }

    public void setXmlMessage(String xmlMessage) {
        this.xmlMessage = xmlMessage;
    }

    public String getSenderBankCode() {
        return senderBankCode;
    }

    public void setSenderBankCode(String senderBankCode) {
        this.senderBankCode = senderBankCode;
    }

    public String getReceiverBankCode() {
        return receiverBankCode;
    }

    public void setReceiverBankCode(String receiverBankCode) {
        this.receiverBankCode = receiverBankCode;
    }

    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getNarration() {
        return narration;
    }

    public void setNarration(String narration) {
        this.narration = narration;
    }

    public LocalDateTime getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(LocalDateTime receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public String getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

    @Override
    public String toString() {
        return "InboundPacs008Request{" +
                "transactionId='" + transactionId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", senderBankCode='" + senderBankCode + '\'' +
                ", receiverBankCode='" + receiverBankCode + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", receivedAt=" + receivedAt +
                '}';
    }
}
