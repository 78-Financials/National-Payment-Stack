package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

/**
 * DTO for account status information
 */
public class AccountStatusDto {
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("clientName")
    private String clientName;
    
    @JsonProperty("active")
    private Boolean active;
    
    @JsonProperty("accountLocked")
    private Boolean accountLocked;
    
    @JsonProperty("loginAttempts")
    private Integer loginAttempts;
    
    @JsonProperty("lastLogin")
    private LocalDateTime lastLogin;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    // Constructors
    public AccountStatusDto() {}
    
    public AccountStatusDto(String email, String clientId, String clientName, Boolean active, 
                           Boolean accountLocked, Integer loginAttempts, LocalDateTime lastLogin, 
                           LocalDateTime createdAt) {
        this.email = email;
        this.clientId = clientId;
        this.clientName = clientName;
        this.active = active;
        this.accountLocked = accountLocked;
        this.loginAttempts = loginAttempts;
        this.lastLogin = lastLogin;
        this.createdAt = createdAt;
    }
    
    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
    
    public Integer getLoginAttempts() { return loginAttempts; }
    public void setLoginAttempts(Integer loginAttempts) { this.loginAttempts = loginAttempts; }
    
    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
