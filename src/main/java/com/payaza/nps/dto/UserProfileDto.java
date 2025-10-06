package com.payaza.nps.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * User profile DTO
 */
public class UserProfileDto {
    
    @JsonProperty("id")
    private String id;
    
    @NotBlank(message = "Client name is required")
    @JsonProperty("clientName")
    private String clientName;
    
    @Email(message = "Valid email is required")
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("phone")
    private String phone;
    
    @JsonProperty("clientType")
    private String clientType;
    
    @JsonProperty("isActive")
    private boolean isActive;
    
    @JsonProperty("lastLogin")
    private LocalDateTime lastLogin;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserProfileDto() {}
    
    public UserProfileDto(String id, String clientName, String email, String phone, 
                         String clientType, boolean isActive) {
        this.id = id;
        this.clientName = clientName;
        this.email = email;
        this.phone = phone;
        this.clientType = clientType;
        this.isActive = isActive;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
