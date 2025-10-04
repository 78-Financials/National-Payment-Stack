package com.payaza.nps.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * JSON DTO for payment status request API
 */
public class PaymentStatusRequestJsonDto {

    @NotBlank(message = "Original message ID is required")
    private String originalMessageId;

    @NotBlank(message = "Original end-to-end ID is required")
    private String originalEndToEndId;

    private String originalInstructionId;
    private String originalTransactionId;
    private String originalUETR;
    private String originalClearSystemReference;

    // Constructors
    public PaymentStatusRequestJsonDto() {}

    public PaymentStatusRequestJsonDto(String originalMessageId, String originalEndToEndId) {
        this.originalMessageId = originalMessageId;
        this.originalEndToEndId = originalEndToEndId;
    }

    // Getters and Setters
    public String getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

    public String getOriginalEndToEndId() {
        return originalEndToEndId;
    }

    public void setOriginalEndToEndId(String originalEndToEndId) {
        this.originalEndToEndId = originalEndToEndId;
    }

    public String getOriginalInstructionId() {
        return originalInstructionId;
    }

    public void setOriginalInstructionId(String originalInstructionId) {
        this.originalInstructionId = originalInstructionId;
    }

    public String getOriginalTransactionId() {
        return originalTransactionId;
    }

    public void setOriginalTransactionId(String originalTransactionId) {
        this.originalTransactionId = originalTransactionId;
    }

    public String getOriginalUETR() {
        return originalUETR;
    }

    public void setOriginalUETR(String originalUETR) {
        this.originalUETR = originalUETR;
    }

    public String getOriginalClearSystemReference() {
        return originalClearSystemReference;
    }

    public void setOriginalClearSystemReference(String originalClearSystemReference) {
        this.originalClearSystemReference = originalClearSystemReference;
    }
}
