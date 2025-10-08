package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for PACS.008 Payment Response
 */
public class Pacs008ResponseDto {
    
    @JsonProperty("messageId")
    private String messageId;
    
    @JsonProperty("transactionId")
    private String transactionId;
    
    @JsonProperty("status")
    private String status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("responseCode")
    private String responseCode;
    
    @JsonProperty("responseMessage")
    private String responseMessage;
    
    @JsonProperty("creationDateTime")
    private String creationDateTime;
    
    @JsonProperty("batchBooking")
    private Boolean batchBooking;
    
    @JsonProperty("numberOfTransactions")
    private String numberOfTransactions;
    
    @JsonProperty("settlementMethod")
    private String settlementMethod;
    
    @JsonProperty("instructionId")
    private String instructionId;
    
    @JsonProperty("endToEndId")
    private String endToEndId;
    
    @JsonProperty("clearingChannel")
    private String clearingChannel;
    
    @JsonProperty("serviceLevel")
    private String serviceLevel;
    
    @JsonProperty("localInstrument")
    private String localInstrument;
    
    @JsonProperty("categoryPurpose")
    private String categoryPurpose;
    
    @JsonProperty("amount")
    private BigDecimal amount;
    
    @JsonProperty("currency")
    private String currency;
    
    @JsonProperty("settlementDate")
    private String settlementDate;
    
    @JsonProperty("chargeBearer")
    private String chargeBearer;
    
    @JsonProperty("senderAccountName")
    private String senderAccountName;
    
    @JsonProperty("senderAccountNumber")
    private String senderAccountNumber;
    
    @JsonProperty("receiverAccountName")
    private String receiverAccountName;
    
    @JsonProperty("receiverAccountNumber")
    private String receiverAccountNumber;
    
    @JsonProperty("instructionsForNextAgent")
    private String instructionsForNextAgent;
    
    @JsonProperty("remittanceInformation")
    private String remittanceInformation;
    
    @JsonProperty("debtorAccountDesignation")
    private String debtorAccountDesignation;
    
    @JsonProperty("debtorAccountTier")
    private String debtorAccountTier;
    
    @JsonProperty("debtorBvn")
    private String debtorBvn;
    
    @JsonProperty("creditorAccountDesignation")
    private String creditorAccountDesignation;
    
    @JsonProperty("creditorAccountTier")
    private String creditorAccountTier;
    
    @JsonProperty("creditorBvn")
    private String creditorBvn;
    
    @JsonProperty("transactionLocation")
    private String transactionLocation;
    
    @JsonProperty("nameEnquiryMsgId")
    private String nameEnquiryMsgId;
    
    @JsonProperty("channelCode")
    private String channelCode;
    
    @JsonProperty("riskRating")
    private String riskRating;
    
    @JsonProperty("instgAgentBicfi")
    private String instgAgentBicfi;
    
    @JsonProperty("instgAgentMemberId")
    private String instgAgentMemberId;
    
    @JsonProperty("instdAgentBicfi")
    private String instdAgentBicfi;
    
    @JsonProperty("instdAgentMemberId")
    private String instdAgentMemberId;
    
    @JsonProperty("dbtrAgentMemberId")
    private String dbtrAgentMemberId;
    
    @JsonProperty("cdtrAgentMemberId")
    private String cdtrAgentMemberId;
    
    @JsonProperty("processedAt")
    private LocalDateTime processedAt;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    // Constructors
    public Pacs008ResponseDto() {}
    
    public Pacs008ResponseDto(String transactionId, String status) {
        this.transactionId = transactionId;
        this.status = status;
    }
    
    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }
    
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getResponseCode() { return responseCode; }
    public void setResponseCode(String responseCode) { this.responseCode = responseCode; }
    
    public String getResponseMessage() { return responseMessage; }
    public void setResponseMessage(String responseMessage) { this.responseMessage = responseMessage; }
    
    public String getCreationDateTime() { return creationDateTime; }
    public void setCreationDateTime(String creationDateTime) { this.creationDateTime = creationDateTime; }
    
    public Boolean getBatchBooking() { return batchBooking; }
    public void setBatchBooking(Boolean batchBooking) { this.batchBooking = batchBooking; }
    
    public String getNumberOfTransactions() { return numberOfTransactions; }
    public void setNumberOfTransactions(String numberOfTransactions) { this.numberOfTransactions = numberOfTransactions; }
    
    public String getSettlementMethod() { return settlementMethod; }
    public void setSettlementMethod(String settlementMethod) { this.settlementMethod = settlementMethod; }
    
    public String getInstructionId() { return instructionId; }
    public void setInstructionId(String instructionId) { this.instructionId = instructionId; }
    
    public String getEndToEndId() { return endToEndId; }
    public void setEndToEndId(String endToEndId) { this.endToEndId = endToEndId; }
    
    public String getClearingChannel() { return clearingChannel; }
    public void setClearingChannel(String clearingChannel) { this.clearingChannel = clearingChannel; }
    
    public String getServiceLevel() { return serviceLevel; }
    public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }
    
    public String getLocalInstrument() { return localInstrument; }
    public void setLocalInstrument(String localInstrument) { this.localInstrument = localInstrument; }
    
    public String getCategoryPurpose() { return categoryPurpose; }
    public void setCategoryPurpose(String categoryPurpose) { this.categoryPurpose = categoryPurpose; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getSettlementDate() { return settlementDate; }
    public void setSettlementDate(String settlementDate) { this.settlementDate = settlementDate; }
    
    public String getChargeBearer() { return chargeBearer; }
    public void setChargeBearer(String chargeBearer) { this.chargeBearer = chargeBearer; }
    
    public String getSenderAccountName() { return senderAccountName; }
    public void setSenderAccountName(String senderAccountName) { this.senderAccountName = senderAccountName; }
    
    public String getSenderAccountNumber() { return senderAccountNumber; }
    public void setSenderAccountNumber(String senderAccountNumber) { this.senderAccountNumber = senderAccountNumber; }
    
    public String getReceiverAccountName() { return receiverAccountName; }
    public void setReceiverAccountName(String receiverAccountName) { this.receiverAccountName = receiverAccountName; }
    
    public String getReceiverAccountNumber() { return receiverAccountNumber; }
    public void setReceiverAccountNumber(String receiverAccountNumber) { this.receiverAccountNumber = receiverAccountNumber; }
    
    public String getInstructionsForNextAgent() { return instructionsForNextAgent; }
    public void setInstructionsForNextAgent(String instructionsForNextAgent) { this.instructionsForNextAgent = instructionsForNextAgent; }
    
    public String getRemittanceInformation() { return remittanceInformation; }
    public void setRemittanceInformation(String remittanceInformation) { this.remittanceInformation = remittanceInformation; }
    
    public String getDebtorAccountDesignation() { return debtorAccountDesignation; }
    public void setDebtorAccountDesignation(String debtorAccountDesignation) { this.debtorAccountDesignation = debtorAccountDesignation; }
    
    public String getDebtorAccountTier() { return debtorAccountTier; }
    public void setDebtorAccountTier(String debtorAccountTier) { this.debtorAccountTier = debtorAccountTier; }
    
    public String getDebtorBvn() { return debtorBvn; }
    public void setDebtorBvn(String debtorBvn) { this.debtorBvn = debtorBvn; }
    
    public String getCreditorAccountDesignation() { return creditorAccountDesignation; }
    public void setCreditorAccountDesignation(String creditorAccountDesignation) { this.creditorAccountDesignation = creditorAccountDesignation; }
    
    public String getCreditorAccountTier() { return creditorAccountTier; }
    public void setCreditorAccountTier(String creditorAccountTier) { this.creditorAccountTier = creditorAccountTier; }
    
    public String getCreditorBvn() { return creditorBvn; }
    public void setCreditorBvn(String creditorBvn) { this.creditorBvn = creditorBvn; }
    
    public String getTransactionLocation() { return transactionLocation; }
    public void setTransactionLocation(String transactionLocation) { this.transactionLocation = transactionLocation; }
    
    public String getNameEnquiryMsgId() { return nameEnquiryMsgId; }
    public void setNameEnquiryMsgId(String nameEnquiryMsgId) { this.nameEnquiryMsgId = nameEnquiryMsgId; }
    
    public String getChannelCode() { return channelCode; }
    public void setChannelCode(String channelCode) { this.channelCode = channelCode; }
    
    public String getRiskRating() { return riskRating; }
    public void setRiskRating(String riskRating) { this.riskRating = riskRating; }
    
    public String getInstgAgentBicfi() { return instgAgentBicfi; }
    public void setInstgAgentBicfi(String instgAgentBicfi) { this.instgAgentBicfi = instgAgentBicfi; }
    
    public String getInstgAgentMemberId() { return instgAgentMemberId; }
    public void setInstgAgentMemberId(String instgAgentMemberId) { this.instgAgentMemberId = instgAgentMemberId; }
    
    public String getInstdAgentBicfi() { return instdAgentBicfi; }
    public void setInstdAgentBicfi(String instdAgentBicfi) { this.instdAgentBicfi = instdAgentBicfi; }
    
    public String getInstdAgentMemberId() { return instdAgentMemberId; }
    public void setInstdAgentMemberId(String instdAgentMemberId) { this.instdAgentMemberId = instdAgentMemberId; }
    
    public String getDbtrAgentMemberId() { return dbtrAgentMemberId; }
    public void setDbtrAgentMemberId(String dbtrAgentMemberId) { this.dbtrAgentMemberId = dbtrAgentMemberId; }
    
    public String getCdtrAgentMemberId() { return cdtrAgentMemberId; }
    public void setCdtrAgentMemberId(String cdtrAgentMemberId) { this.cdtrAgentMemberId = cdtrAgentMemberId; }
    
    public LocalDateTime getProcessedAt() { return processedAt; }
    public void setProcessedAt(LocalDateTime processedAt) { this.processedAt = processedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}