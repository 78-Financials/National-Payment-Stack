package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for API key response (one-time display)
 */
public class ApiKeyResponseDto {
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("clientName")
    private String clientName;
    
    @JsonProperty("apiKey")
    private String apiKey;
    
    @JsonProperty("generatedAt")
    private String generatedAt;
    
    @JsonProperty("warning")
    private String warning = "⚠️ IMPORTANT: This API key is shown only once. Please save it securely. It will not be displayed again.";
    
    // Constructors
    public ApiKeyResponseDto() {}
    
    public ApiKeyResponseDto(String clientId, String clientName, String apiKey) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.apiKey = apiKey;
        this.generatedAt = java.time.LocalDateTime.now().toString();
    }
    
    // Getters and Setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public String getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(String generatedAt) { this.generatedAt = generatedAt; }
    
    public String getWarning() { return warning; }
    public void setWarning(String warning) { this.warning = warning; }
}
