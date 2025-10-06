package com.payaza.nps.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Login request DTO
 */
public class LoginRequestDto {
    
    @NotBlank(message = "Client ID is required")
    @JsonProperty("clientId")
    private String clientId;
    
    @NotBlank(message = "API Key is required")
    @JsonProperty("apiKey")
    private String apiKey;
    
    // Constructors
    public LoginRequestDto() {}
    
    public LoginRequestDto(String clientId, String apiKey) {
        this.clientId = clientId;
        this.apiKey = apiKey;
    }
    
    // Getters and Setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
}
