package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Login response DTO
 */
public class LoginResponseDto {
    
    @JsonProperty("token")
    private String token;
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("clientName")
    private String clientName;
    
    @JsonProperty("clientType")
    private String clientType;
    
    @JsonProperty("permissions")
    private List<String> permissions;
    
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
    
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    // Constructors
    public LoginResponseDto() {}
    
    public LoginResponseDto(String token, String clientId, String clientName, 
                          String clientType, List<String> permissions, LocalDateTime expiresAt) {
        this.token = token;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientType = clientType;
        this.permissions = permissions;
        this.expiresAt = expiresAt;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    
    public List<String> getPermissions() { return permissions; }
    public void setPermissions(List<String> permissions) { this.permissions = permissions; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
