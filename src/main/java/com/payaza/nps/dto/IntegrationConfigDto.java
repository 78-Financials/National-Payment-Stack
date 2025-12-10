package com.payaza.nps.dto;

import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Integration configuration DTO
 */
public class IntegrationConfigDto {
    
    @JsonProperty("id")
    private String id;
    
    @NotBlank(message = "Integration name is required")
    @JsonProperty("name")
    private String name;
    
    @NotBlank(message = "Integration type is required")
    @JsonProperty("type")
    private String type; // SLACK, TEAMS, CUSTOM_API, etc.
    
    @NotBlank(message = "Endpoint URL is required")
    @JsonProperty("endpoint")
    private String endpoint;
    
    @JsonProperty("apiKey")
    private String apiKey;
    
    @JsonProperty("configuration")
    private Map<String, Object> configuration;
    
    @JsonProperty("isActive")
    private boolean isActive = true;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;
    
    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
    
    // Constructors
    public IntegrationConfigDto() {}
    
    public IntegrationConfigDto(String name, String type, String endpoint) {
        this.name = name;
        this.type = type;
        this.endpoint = endpoint;
        this.isActive = true;
    }
    
    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }
    
    public Map<String, Object> getConfiguration() { return configuration; }
    public void setConfiguration(Map<String, Object> configuration) { this.configuration = configuration; }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
