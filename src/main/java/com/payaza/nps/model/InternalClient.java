package com.payaza.nps.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.HashSet;

/**
 * JPA Entity for Internal Clients
 * Represents clients that can access the NPS API
 */
@Entity
@Table(name = "internal_clients", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = "client_id"),
           @UniqueConstraint(columnNames = "api_key"),
           @UniqueConstraint(columnNames = "transaction_prefix")
       })
public class InternalClient {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Client ID is required")
    @Size(min = 3, max = 10, message = "Client ID must be between 3 and 10 characters")
    @Column(name = "client_id", unique = true, nullable = false)
    private String clientId;
    
    @NotBlank(message = "Client name is required")
    @Size(max = 100, message = "Client name must not exceed 100 characters")
    @Column(name = "client_name", nullable = false)
    private String clientName;
    
    @NotBlank(message = "API key is required")
    @Size(min = 20, max = 100, message = "API key must be between 20 and 100 characters")
    @Column(name = "api_key", unique = true, nullable = false)
    private String apiKey;
    
    @NotBlank(message = "Transaction prefix is required")
    @Size(min = 3, max = 10, message = "Transaction prefix must be between 3 and 10 characters")
    @Column(name = "transaction_prefix", unique = true, nullable = false)
    private String transactionPrefix;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_allowed_endpoints", 
                     joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "endpoint")
    private Set<String> allowedEndpoints = new HashSet<>();
    
    @NotNull(message = "Active status is required")
    @Column(name = "active", nullable = false)
    private Boolean active = true;
    
    @Positive(message = "Rate limit must be positive")
    @Column(name = "rate_limit_per_minute", nullable = false)
    private Integer rateLimitPerMinute = 100;
    
    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 500)
    private String description;
    
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Contact email must not exceed 100 characters")
    @Column(name = "contact_email", length = 100)
    private String contactEmail;
    
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;
    
    @Column(name = "created_by", length = 50)
    private String createdBy = "SYSTEM";
    
    @Column(name = "updated_by", length = 50)
    private String updatedBy = "SYSTEM";
    
    @Column(name = "client_type", length = 20)
    private String clientType = "BANK";
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @Column(name = "password_reset_token", length = 100)
    private String passwordResetToken;
    
    @Column(name = "password_reset_expires")
    private LocalDateTime passwordResetExpires;

    // Constructors
    public InternalClient() {}

    public InternalClient(String clientId, String clientName, String apiKey, String transactionPrefix,
                          Set<String> allowedEndpoints, Boolean active, Integer rateLimitPerMinute) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.apiKey = apiKey;
        this.transactionPrefix = transactionPrefix;
        this.allowedEndpoints = allowedEndpoints != null ? allowedEndpoints : new HashSet<>();
        this.active = active != null ? active : true;
        this.rateLimitPerMinute = rateLimitPerMinute != null ? rateLimitPerMinute : 100;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClientId() { return clientId; }
    public void setClientId(String clientId) { this.clientId = clientId; }

    public String getClientName() { return clientName; }
    public void setClientName(String clientName) { this.clientName = clientName; }

    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getTransactionPrefix() { return transactionPrefix; }
    public void setTransactionPrefix(String transactionPrefix) { this.transactionPrefix = transactionPrefix; }

    public Set<String> getAllowedEndpoints() { return allowedEndpoints; }
    public void setAllowedEndpoints(Set<String> allowedEndpoints) { this.allowedEndpoints = allowedEndpoints; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public Integer getRateLimitPerMinute() { return rateLimitPerMinute; }
    public void setRateLimitPerMinute(Integer rateLimitPerMinute) { this.rateLimitPerMinute = rateLimitPerMinute; }
    
    // Convenience method for tests
    public void setRateLimit(int rateLimit) { this.rateLimitPerMinute = rateLimit; }

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
    
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
    
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    
    public String getPasswordResetToken() { return passwordResetToken; }
    public void setPasswordResetToken(String passwordResetToken) { this.passwordResetToken = passwordResetToken; }
    
    public LocalDateTime getPasswordResetExpires() { return passwordResetExpires; }
    public void setPasswordResetExpires(LocalDateTime passwordResetExpires) { this.passwordResetExpires = passwordResetExpires; }
    
    // Helper methods
    public boolean isActive() { 
        return active != null && active; 
    }

    public void addAllowedEndpoint(String endpoint) {
        if (this.allowedEndpoints == null) {
            this.allowedEndpoints = new HashSet<>();
        }
        this.allowedEndpoints.add(endpoint);
    }

    public void removeAllowedEndpoint(String endpoint) {
        if (this.allowedEndpoints != null) {
            this.allowedEndpoints.remove(endpoint);
        }
    }

    public boolean hasEndpointAccess(String endpoint) {
        return this.allowedEndpoints != null && endpoint != null && this.allowedEndpoints.contains(endpoint);
    }

    public boolean hasPermission(String endpoint) {
        return hasEndpointAccess(endpoint);
    }

    @Override
    public String toString() {
        return "InternalClient{" +
               "id=" + id +
               ", clientId='" + clientId + '\'' +
               ", clientName='" + clientName + '\'' +
               ", active=" + active +
               ", transactionPrefix='" + transactionPrefix + '\'' +
               ", allowedEndpoints=" + allowedEndpoints +
               ", rateLimitPerMinute=" + rateLimitPerMinute +
               ", createdAt=" + createdAt +
               '}';
    }
}