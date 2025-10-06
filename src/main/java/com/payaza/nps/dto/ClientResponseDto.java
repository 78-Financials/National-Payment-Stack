package com.payaza.nps.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.payaza.nps.model.InternalClient;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * DTO for client response (without sensitive information)
 */
public class ClientResponseDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("clientId")
    private String clientId;
    
    @JsonProperty("clientName")
    private String clientName;
    
    @JsonProperty("transactionPrefix")
    private String transactionPrefix;
    
    @JsonProperty("allowedEndpoints")
    private Set<String> allowedEndpoints;
    
    @JsonProperty("active")
    private Boolean active;
    
    @JsonProperty("rateLimitPerMinute")
    private Integer rateLimitPerMinute;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("contactEmail")
    private String contactEmail;
    
    @JsonProperty("contactPhone")
    private String contactPhone;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    @JsonProperty("lastAccessedAt")
    private LocalDateTime lastAccessedAt;
    
    @JsonProperty("createdBy")
    private String createdBy;
    
    @JsonProperty("updatedBy")
    private String updatedBy;
    
    // Constructors
    public ClientResponseDto() {}
    
    public ClientResponseDto(InternalClient client) {
        this.id = client.getId();
        this.clientId = client.getClientId();
        this.clientName = client.getClientName();
        this.transactionPrefix = client.getTransactionPrefix();
        this.allowedEndpoints = client.getAllowedEndpoints();
        this.active = client.getActive();
        this.rateLimitPerMinute = client.getRateLimitPerMinute();
        this.description = client.getDescription();
        this.contactEmail = client.getContactEmail();
        this.contactPhone = client.getContactPhone();
        this.createdAt = client.getCreatedAt();
        this.updatedAt = client.getUpdatedAt();
        this.lastAccessedAt = client.getLastAccessedAt();
        this.createdBy = client.getCreatedBy();
        this.updatedBy = client.getUpdatedBy();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
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
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) { this.lastAccessedAt = lastAccessedAt; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
