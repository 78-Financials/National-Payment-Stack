package com.payaza.nps.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for inbound subscription requests
 */
public class InboundSubscriptionRequestDto {
    
    @NotBlank(message = "Client ID is required")
    @Size(max = 50, message = "Client ID must not exceed 50 characters")
    private String clientId;
    
    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;

    // Constructors
    public InboundSubscriptionRequestDto() {}

    public InboundSubscriptionRequestDto(String clientId) {
        this.clientId = clientId;
    }

    public InboundSubscriptionRequestDto(String clientId, String notes) {
        this.clientId = clientId;
        this.notes = notes;
    }

    // Getters and Setters
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "InboundSubscriptionRequestDto{" +
                "clientId='" + clientId + '\'' +
                ", notes='" + notes + '\'' +
                '}';
    }
}
