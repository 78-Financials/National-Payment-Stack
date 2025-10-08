package com.payaza.nps.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

/**
 * DTO for creating a new internal client
 */
public class CreateClientRequestDto {
    
    @NotBlank(message = "Client ID is required")
    @Size(min = 3, max = 10, message = "Client ID must be between 3 and 10 characters")
    private String clientId;
    
    @NotBlank(message = "Client name is required")
    @Size(max = 100, message = "Client name must not exceed 100 characters")
    private String clientName;
    
    @NotBlank(message = "Transaction prefix is required")
    @Size(min = 3, max = 10, message = "Transaction prefix must be between 3 and 10 characters")
    private String transactionPrefix;
    
    @NotNull(message = "Allowed endpoints are required")
    @Size(min = 1, message = "At least one endpoint must be specified")
    private Set<String> allowedEndpoints;
    
    @NotNull(message = "Active status is required")
    private Boolean active = true;
    
    @Positive(message = "Rate limit must be positive")
    private Integer rateLimitPerMinute = 100;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
    
    @Email(message = "Invalid contact email format")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    private String contactEmail;
    
    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String contactPhone;
    
    // Getters and Setters
    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }
    
    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }
    
    public String getTransactionPrefix() { return transactionPrefix; }
    public void setTransactionPrefix(String transactionPrefix) { this.transactionPrefix = transactionPrefix; }
    
    public Set<String> getAllowedEndpoints() { return allowedEndpoints; }
    public void setAllowedEndpoints(Set<String> allowedEndpoints) { this.allowedEndpoints = allowedEndpoints; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
    
    public Integer getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
