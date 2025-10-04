package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

/**
 * DTO for PACS.028 Payment Status Request
 */
public class Pacs028RequestDto {

    @NotBlank(message = "Message ID is required")
    @JsonProperty("messageId")
    private String messageId;

    @NotBlank(message = "Original Message ID is required")
    @JsonProperty("originalMessageId")
    private String originalMessageId;

    @NotBlank(message = "Original Message Name ID is required")
    @JsonProperty("originalMessageNameId")
    private String originalMessageNameId; // pacs.008.001.12

    @NotBlank(message = "Institution Code is required")
    @JsonProperty("institutionCode")
    private String institutionCode;

    @NotBlank(message = "Original Transaction ID is required")
    @JsonProperty("originalTransactionId")
    private String originalTransactionId;

    @NotBlank(message = "Status Request ID is required")
    @JsonProperty("statusRequestId")
    private String statusRequestId;

    @JsonProperty("originalCreationDateTime")
    private LocalDateTime originalCreationDateTime;

    @JsonProperty("settlementDate")
    private String settlementDate;

    // Getters and Setters
    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getOriginalMessageId() { return originalMessageId; }
    public void setOriginalMessageId(String originalMessageId) { this.originalMessageId = originalMessageId; }

    public String getOriginalMessageNameId() { return originalMessageNameId; }
    public void setOriginalMessageNameId(String originalMessageNameId) { this.originalMessageNameId = originalMessageNameId; }

    public String getInstitutionCode() { return institutionCode; }
    public void setInstitutionCode(String institutionCode) { this.institutionCode = institutionCode; }

    public String getOriginalTransactionId() { return originalTransactionId; }
    public void setOriginalTransactionId(String originalTransactionId) { this.originalTransactionId = originalTransactionId; }

    public String getStatusRequestId() { return statusRequestId; }
    public void setStatusRequestId(String statusRequestId) { this.statusRequestId = statusRequestId; }

    public LocalDateTime getOriginalCreationDateTime() { return originalCreationDateTime; }
    public void setOriginalCreationDateTime(LocalDateTime originalCreationDateTime) { this.originalCreationDateTime = originalCreationDateTime; }

    public String getSettlementDate() { return settlementDate; }
    public void setSettlementDate(String settlementDate) { this.settlementDate = settlementDate; }
}
